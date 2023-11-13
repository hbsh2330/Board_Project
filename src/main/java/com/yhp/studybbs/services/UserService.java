package com.yhp.studybbs.services;

import com.yhp.studybbs.entities.ContactCompanyEntity;
import com.yhp.studybbs.entities.EmailAuthEntity;
import com.yhp.studybbs.entities.UserEntity;
import com.yhp.studybbs.mappers.UserMapper;
import com.yhp.studybbs.regexes.EmailAuthRegex;
import com.yhp.studybbs.regexes.UserRegex;
import com.yhp.studybbs.results.user.LoginResult;
import com.yhp.studybbs.results.user.RegisterResult;
import com.yhp.studybbs.results.user.SendRegisterEmailResult;
import com.yhp.studybbs.results.user.VerifyRegisterEmailResult;
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

    public SendRegisterEmailResult sendRegisterEmail(EmailAuthEntity emailAuth) throws MessagingException {
        if (!UserRegex.EMAIL.matches(emailAuth.getEmail())) { //만약
            return SendRegisterEmailResult.FAILURE;
        }
        if (this.userMapper.selectUserByEmail(emailAuth.getEmail()) != null) {
            return SendRegisterEmailResult.FAILURE_DUPLICATE_EMAIL;
        }
        emailAuth.setCode(RandomStringUtils.randomNumeric(6));
        emailAuth.setSalt(CryptoUtil.hashSha512(String.format("%s%s%f%f",
                emailAuth.getEmail(),
                emailAuth.getCode(),
                Math.random(),
                Math.random())));
        emailAuth.setVerified(false);
        emailAuth.setCreatedAt(new Date());
        emailAuth.setExpiresAt(DateUtils.addMinutes(emailAuth.getCreatedAt(), 5));

        Context context = new Context(); // emilAuth객체를 html파일에 넘겨주기 위해서 사용하는 객체
        context.setVariable("emailAuth", emailAuth); //emailAuth을 설정
        String textHtml = this.templateEngine.process("user/registerEmail", context); //html를 문자열로 textHtml에 대입 하기위해 html의 주소를 작성
        MimeMessage message = this.mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, false);
        messageHelper.setTo(emailAuth.getEmail());
        messageHelper.setSubject("[BBS] 회원가입 인증번호");
        messageHelper.setText(textHtml, true); //문자열을 setText로 전달
        this.mailSender.send(message);

        return this.userMapper.insertEmailAuth(emailAuth) > 0
                ? SendRegisterEmailResult.SUCCESS
                : SendRegisterEmailResult.FAILURE;
    }

    public VerifyRegisterEmailResult verifyRegisterEmail(EmailAuthEntity emailAuth) {
        if (!EmailAuthRegex.EMAIL.matches(emailAuth.getEmail()) || //만약 EmailAuthRex.Emal의 정규식이 emailAuth가 가지고 있는 Email과맞지않는다면
                !EmailAuthRegex.CODE.matches(emailAuth.getCode()) ||
                //만약 정규식이 emailAuth이 가지고 있는 코드와 맞지않는다면
                !EmailAuthRegex.SALT.matches(emailAuth.getSalt())) {
            //만약 salt 정규식이 emailAuth이 가지고 있는 코드와 맞지않는다면
            return VerifyRegisterEmailResult.FAILURE;
        } //실패를 반환
        emailAuth = this.userMapper.selectEmailAuthByEmailCodeSalt( //실패가 아니라면 //userMapper(데이터)의 파라미터로 받은 email, Code, Salt를 emailAuth로 넘겨준다
                emailAuth.getEmail(),
                emailAuth.getCode(),
                emailAuth.getSalt());
        //VerifyRegisterEmailResult의 FAILURE_INVALID_CODE;를 넘겨준다
        if (new Date().compareTo(emailAuth.getExpiresAt()) > 0) {
            return VerifyRegisterEmailResult.FAILURE_EXPIRED;
        } //현재날짜와 만료일시를 뺀것이 0보다 크면 즉 현재 일시가 만료 일시보다 더 크다면 즉 만료가 됬으면 FAILURE_EXPIRED를 넘겨준다.
        emailAuth.setVerified(true); //null도 안오고 만료일시가 완료가 안됬으면 setVerified를 true로 바꾸고
        return this.userMapper.updateEmailAuth(emailAuth) > 0
                ? VerifyRegisterEmailResult.SUCCESS
                : VerifyRegisterEmailResult.FAILURE;
    }

    public RegisterResult registerResult(UserEntity user, EmailAuthEntity emailAuth, boolean termMarketingAgreed) {
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
        emailAuth = this.userMapper.selectEmailAuthByEmailCodeSalt( //실패가 아니라면 //userMapper(데이터)의 파라미터로 받은 email, Code, Salt를 emailAuth로 넘겨준다
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

            return RegisterResult.FAILURE_DUPICATE_NICKNAME;
        }
        user.setPassword(CryptoUtil.hashSha512(user.getPassword())); //클라이언트로 부터 받지 않는 정보들은 백단에서 set해서 막아줄 필요가 있다.
        user.setAdmin(false);
        user.setDeleted(false);
        user.setSuspended(false);
        user.setRegisteredAt(new Date());
        user.setTermPolicyAt(user.getRegisteredAt());
        user.setTermPrivacyAt(user.getRegisteredAt());
        if (termMarketingAgreed){
            user.setTermPrivacyAt(user.getRegisteredAt());
        } else {
            user.setTermPrivacyAt(null);
        }
        return this.userMapper.insertUser(user) > 0
                ? RegisterResult.SUCCESS
                : RegisterResult.FAILURE;
        }

        public LoginResult login(HttpSession session, UserEntity user){ //
        if (!UserRegex.EMAIL.matches(user.getEmail())|| !UserRegex.PASSWORD.matches(user.getPassword())){
            return LoginResult.FAILURE;
        }
        UserEntity dbUser = this.userMapper.selectUserByEmail(user.getEmail()); //모든 데이터 다 가지고 있음
        if (dbUser == null) {
            return LoginResult.FAILURE;
        }// 너가 준 이메일 레코드가 없음, 이메일 잘못적음
        if (!dbUser.getPassword().equals(CryptoUtil.hashSha512(user.getPassword()))){
            return LoginResult.FAILURE; // 비밀번호 잘못적음
        }
        if(dbUser.isSuspended()){
            return LoginResult.FAILURE_SUSPENDED; //계정이 정지된 상태라면
        }
        session.setAttribute("user", dbUser);
        return LoginResult.SUCCESS;
        }
}












