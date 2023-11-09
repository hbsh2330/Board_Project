package com.yhp.studybbs.services;

import com.yhp.studybbs.entities.ContactCompanyEntity;
import com.yhp.studybbs.entities.EmailAuthEntity;
import com.yhp.studybbs.mappers.UserMapper;
import com.yhp.studybbs.regexes.EmailAuthRegex;
import com.yhp.studybbs.regexes.UserRegex;
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

    public ContactCompanyEntity[] getContactCompanies(){
        if (UserService.contactCompanies == null){
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

        Context context = new Context();
        context.setVariable("emailAuth", emailAuth);
        String textHtml = this.templateEngine.process("user/registerEmail", context);
        MimeMessage message = this.mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, false);
        messageHelper.setTo(emailAuth.getEmail());
        messageHelper.setSubject("[BBS] 회원가입 인증번호");
        messageHelper.setText(textHtml, true);
        this.mailSender.send(message);

        return this.userMapper.insertEmailAuth(emailAuth) > 0
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
                ?VerifyRegisterEmailResult.SUCCESS
                :VerifyRegisterEmailResult.FAILURE;
    }
}












