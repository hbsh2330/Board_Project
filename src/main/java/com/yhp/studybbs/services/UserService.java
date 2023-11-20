package com.yhp.studybbs.services;

import com.yhp.studybbs.entities.ContactCompanyEntity;
import com.yhp.studybbs.entities.EmailAuthEntity;
import com.yhp.studybbs.entities.UserEntity;
import com.yhp.studybbs.factories.MailFactory;
import com.yhp.studybbs.mappers.UserMapper;
import com.yhp.studybbs.regexes.EmailAuthRegex;
import com.yhp.studybbs.regexes.UserRegex;
import com.yhp.studybbs.results.user.*;
import com.yhp.studybbs.utils.CryptoUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Service
public class UserService {

    private static void randomize(EmailAuthEntity emailAuth) {
        UserService.randomize(emailAuth, 5);
    }

    private static void randomize(EmailAuthEntity emailAuth, int expDuration) {
        emailAuth.setCode(RandomStringUtils.randomNumeric(6));
        emailAuth.setSalt(CryptoUtil.hashSha512(String.format("%s%s%f%f",
                emailAuth.getEmail(),
                emailAuth.getCode(),
                Math.random(),
                Math.random())));
        emailAuth.setVerified(false);
        emailAuth.setCreatedAt(new Date());
        emailAuth.setExpiresAt(DateUtils.addMinutes(emailAuth.getCreatedAt(), expDuration));
    }

    private static ContactCompanyEntity[] contactCompanies;

    private final UserMapper userMapper;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Autowired
    public UserService(UserMapper userMapper, JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.userMapper = userMapper;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public ContactCompanyEntity[] getContactCompanies() {
        if (UserService.contactCompanies == null) {
            UserService.contactCompanies = this.userMapper.selectContactCompanies();
        }
        return UserService.contactCompanies;
    }

    public LoginResult login(HttpSession session, UserEntity user) {
        if (!UserRegex.EMAIL.matches(user.getEmail()) || !UserRegex.PASSWORD.matches(user.getPassword())) {
            return LoginResult.FAILURE;
        }
        UserEntity dbUser = this.userMapper.selectUserByEmail(user.getEmail());
        if (dbUser == null) {
            return LoginResult.FAILURE;
        }
        if (!dbUser.getPassword().equals(CryptoUtil.hashSha512(user.getPassword()))) {
            return LoginResult.FAILURE;
        }
        if (dbUser.isSuspended()) {
            return LoginResult.FAILURE_SUSPENDED;
        }
        session.setAttribute("user", dbUser);
        return LoginResult.SUCCESS;
    }

    public RecoverEmailResult recoverEmail(UserEntity user) {
        if (!UserRegex.NAME.matches(user.getName()) ||
                !UserRegex.CONTACT_FIRST.matches(user.getContactFirst()) ||
                !UserRegex.CONTACT_SECOND.matches(user.getContactSecond()) ||
                !UserRegex.CONTACT_THIRD.matches(user.getContactThird())) {
            return RecoverEmailResult.FAILURE;
        }
        UserEntity dbUser = this.userMapper.selectUserByContact(
                user.getContactFirst(),
                user.getContactSecond(),
                user.getContactThird());
        if (dbUser == null) {
            return RecoverEmailResult.FAILURE;
        }
        if (!dbUser.getName().equals(user.getName())) {
            return RecoverEmailResult.FAILURE;
        }
        user.setEmail(dbUser.getEmail());
        return RecoverEmailResult.SUCCESS;
    }

    public RegisterResult register(UserEntity user, EmailAuthEntity emailAuth, boolean termMarketingAgreed) {
        if (!UserRegex.EMAIL.matches(user.getEmail()) ||
                !UserRegex.PASSWORD.matches(user.getPassword()) ||
                !UserRegex.NICKNAME.matches(user.getNickname()) ||
                !UserRegex.NAME.matches(user.getName()) ||
                !UserRegex.CONTACT_FIRST.matches(user.getContactFirst()) ||
                !UserRegex.CONTACT_SECOND.matches(user.getContactSecond()) ||
                !UserRegex.CONTACT_THIRD.matches(user.getContactThird()) ||
                !UserRegex.ADDRESS_POSTAL.matches(user.getAddressPostal()) ||
                !UserRegex.ADDRESS_PRIMARY.matches(user.getAddressPrimary()) ||
                !UserRegex.ADDRESS_SECONDARY.matches(user.getAddressSecondary()) ||
                !EmailAuthRegex.EMAIL.matches(emailAuth.getEmail()) ||
                !EmailAuthRegex.CODE.matches(emailAuth.getCode()) ||
                !EmailAuthRegex.SALT.matches(emailAuth.getSalt())) {
            return RegisterResult.FAILURE;
        }
        emailAuth = this.userMapper.selectEmailAuthByEmailCodeSalt(
                emailAuth.getEmail(),
                emailAuth.getCode(),
                emailAuth.getSalt());
        if (emailAuth == null || !emailAuth.isVerified()) {
            return RegisterResult.FAILURE;
        }
        if (this.userMapper.selectUserByEmail(user.getEmail()) != null) {
            return RegisterResult.FAILURE_DUPLICATE_EMAIL;
        }
        if (this.userMapper.selectUserByContact(
                user.getContactFirst(),
                user.getContactSecond(),
                user.getContactThird()) != null) {
            return RegisterResult.FAILURE_DUPLICATE_CONTACT;
        }
        if (this.userMapper.selectUserByNickname(user.getNickname()) != null) {
            return RegisterResult.FAILURE_DUPLICATE_NICKNAME;
        }
        user.setPassword(CryptoUtil.hashSha512(user.getPassword()));
        user.setAdmin(false);
        user.setDeleted(false);
        user.setSuspended(false);
        user.setRegisteredAt(new Date());
        user.setTermPolicyAt(user.getRegisteredAt());
        user.setTermPrivacyAt(user.getRegisteredAt());
        if (termMarketingAgreed) {
            user.setTermMarketingAt(user.getRegisteredAt());
        } else {
            user.setTermMarketingAt(null);
        }
        return this.userMapper.insertUser(user) > 0
                ? RegisterResult.SUCCESS
                : RegisterResult.FAILURE;
    }

    public SendRegisterEmailResult sendRegisterEmail(EmailAuthEntity emailAuth) throws MessagingException {
        if (!UserRegex.EMAIL.matches(emailAuth.getEmail())) {
            return SendRegisterEmailResult.FAILURE;
        }
        if (this.userMapper.selectUserByEmail(emailAuth.getEmail()) != null) {
            return SendRegisterEmailResult.FAILURE_DUPLICATE_EMAIL; // 이미 가입된 이용자
        }
        UserService.randomize(emailAuth); // 인증번호 6자

        new MailFactory(this.mailSender, this.templateEngine)
                .setContextVariable("emailAuth", emailAuth) //MailFactory
                .setTemplate("user/registerEmail")
                .setTo(emailAuth.getEmail())
                .setSubject("[BBS] 회원가입 인증번호")
                .send(); //회원 인증번호 메일로 보내는 Factory형식의 코드
        return this.userMapper.insertEmailAuth(emailAuth) > 0 //
                ? SendRegisterEmailResult.SUCCESS
                : SendRegisterEmailResult.FAILURE;
    }

    public VerifyRegisterEmailResult verifyRegisterEmail(EmailAuthEntity emailAuth) {
        if (!EmailAuthRegex.EMAIL.matches(emailAuth.getEmail()) ||
                !EmailAuthRegex.CODE.matches(emailAuth.getCode()) ||
                !EmailAuthRegex.SALT.matches(emailAuth.getSalt())) {
            return VerifyRegisterEmailResult.FAILURE;
        }
        emailAuth = this.userMapper.selectEmailAuthByEmailCodeSalt(
                emailAuth.getEmail(),
                emailAuth.getCode(),
                emailAuth.getSalt());
        if (emailAuth == null) {
            return VerifyRegisterEmailResult.FAILURE_INVALID_CODE;
        }
        if (new Date().compareTo(emailAuth.getExpiresAt()) > 0) {
            return VerifyRegisterEmailResult.FAILURE_EXPIRED;
        }
        emailAuth.setVerified(true);
        return this.userMapper.updateEmailAuth(emailAuth) > 0
                ? VerifyRegisterEmailResult.SUCCESS
                : VerifyRegisterEmailResult.FAILURE;
    }

    public SendResetPasswordEmailResult sendResetPasswordEmail(EmailAuthEntity emailAuth) throws MessagingException {
        if (!EmailAuthRegex.EMAIL.matches(emailAuth.getEmail())) {
            return SendResetPasswordEmailResult.FAILURE;
        }
        if (this.userMapper.selectUserByEmail(emailAuth.getEmail()) == null) { //db에서 준 이메일이 없다면
            return SendResetPasswordEmailResult.FAILURE_NON_EMAIL;
        }
        UserService.randomize(emailAuth);

        new MailFactory(this.mailSender, this.templateEngine)
                .setContextVariable("emailAuth", emailAuth) //MailFactory
                .setTemplate("user/resetPasswordEmail")
                .setTo(emailAuth.getEmail())
                .setSubject("[BBS] 비밀번호 재설정 인증번호")
                .send();
        return this.userMapper.insertEmailAuth(emailAuth) > 0
                ? SendResetPasswordEmailResult.SUCCESS
                : SendResetPasswordEmailResult.FAILURE;
    }

    public SendResetPasswordResult sendResetPassword(UserEntity user, EmailAuthEntity emailAuth) {
        if (!UserRegex.PASSWORD.matches(user.getPassword()) ||
                !EmailAuthRegex.EMAIL.matches(emailAuth.getEmail()) ||
                !EmailAuthRegex.CODE.matches(emailAuth.getCode()) ||
                !EmailAuthRegex.SALT.matches(emailAuth.getSalt())) {
            return SendResetPasswordResult.FAILURE;
        }
        emailAuth = this.userMapper.selectEmailAuthByEmailCodeSalt(
                emailAuth.getEmail(),
                emailAuth.getCode(),
                emailAuth.getSalt());
        if (emailAuth == null || !emailAuth.isVerified()) {
            return SendResetPasswordResult.FAILURE;
        }
        UserEntity dbUser = this.userMapper.selectUserByEmail(user.getEmail());
        dbUser.setPassword(CryptoUtil.hashSha512(user.getPassword()));
        return this.userMapper.updateUser(dbUser) > 0
                ? SendResetPasswordResult.SUCCESS
                : SendResetPasswordResult.FAILURE;
    }

}















