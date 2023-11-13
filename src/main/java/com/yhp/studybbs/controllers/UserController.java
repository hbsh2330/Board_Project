package com.yhp.studybbs.controllers;

import com.yhp.studybbs.entities.ContactCompanyEntity;
import com.yhp.studybbs.entities.EmailAuthEntity;
import com.yhp.studybbs.entities.UserEntity;
import com.yhp.studybbs.results.user.LoginResult;
import com.yhp.studybbs.results.user.RegisterResult;
import com.yhp.studybbs.results.user.SendRegisterEmailResult;
import com.yhp.studybbs.results.user.VerifyRegisterEmailResult;
import com.yhp.studybbs.services.UserService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping(value = "user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "login",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getLogin(@SessionAttribute(value = "user", required = false)UserEntity user) { //세션저장소에서 가져옴
        ModelAndView modelAndView = new ModelAndView();
        if(user == null){ // 세션이 없으면
            modelAndView.setViewName("user/login");
        } else { //세션이 있으면
            modelAndView.setViewName("redirect:/");
        }
        return modelAndView;
    }

    @RequestMapping(value = "register",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRegister(@SessionAttribute(value = "user", required = false) UserEntity user) {
        ContactCompanyEntity[] contactCompanyEntities = this.userService.getContactCompanies();
        ModelAndView modelAndView = new ModelAndView();
        if (user == null){
            ContactCompanyEntity[] companyEntities = this.userService.getContactCompanies();
            modelAndView.setViewName("user/register");
            modelAndView.addObject("contactCompanies", companyEntities);
        }else {
            modelAndView.setViewName("redirect:/");
        }
        return modelAndView;
    }

    @RequestMapping(value = "recoverEmail", //이메일 찾기
    method = RequestMethod.GET,
    produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRecoverEmail(@SessionAttribute(value = "user", required = false)UserEntity user){
        ModelAndView modelAndView = new ModelAndView();
        if (user == null){
            modelAndView.setViewName("user/recoverEmail");
        }else {
            modelAndView.setViewName("redirect:/");
        }
        return modelAndView;
    }

    @RequestMapping(value = "register",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRegister(@RequestParam(value = "termMarketingAgreed") boolean termMarketingAgreed,
                               UserEntity user,
                               EmailAuthEntity emailAuth) {
        RegisterResult result = this.userService.registerResult(user, emailAuth, termMarketingAgreed);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        return responseObject.toString();
    }

    @RequestMapping(value = "registerEmail",
            method = RequestMethod.POST, //포스트 방식으로 서버로 전송
            produces = MediaType.APPLICATION_JSON_VALUE) //제이슨 방식으로 전달
    @ResponseBody
    public String postRegisterEmail(EmailAuthEntity emailAuth) throws MessagingException {
        SendRegisterEmailResult result = this.userService.sendRegisterEmail(emailAuth); //userService의 메서드 sendRegisterEmail를 호출해서 result에 넣음
        JSONObject responseObject = new JSONObject(); //제이슨 오브젝트 생성
        responseObject.put("result", result.name().toLowerCase()); // 제이슨 오브젝트에 키가 result이고 값이 SendRegisterEmailResult enum의 값을 가져와서 소문자로 변환
        if (result == SendRegisterEmailResult.SUCCESS) { //만약 result가 SUCCESS라면
            responseObject.put("salt", emailAuth.getSalt()); // responseObject 객체에 키가 salt이고 값이 emailAuth의getSalt를 추가함.
        }
        return responseObject.toString(); //그러한 responseObject의 문자를 반환함
    }

    @RequestMapping(value = "login",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postLogin(HttpSession session,
                  UserEntity user){
        LoginResult result = this.userService.login(session, user);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        return responseObject.toString();
    }

    @RequestMapping(value = "logout", method = RequestMethod.GET) //로그아웃 기능구현
    public ModelAndView getLogout(HttpSession session){
        session.setAttribute("user", null);
        return new ModelAndView("redirect:/");
    }

    @RequestMapping(value = "registerEmail",
            method = RequestMethod.PATCH,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchRegisterEmail(EmailAuthEntity emailAuth) { //값을 넘겨주기 위한 매개변수
        VerifyRegisterEmailResult result = this.userService.verifyRegisterEmail(emailAuth); //userService가 가지고 있는 매소드를 실행
        JSONObject responseObject = new JSONObject(); //json객체 생성
        responseObject.put("result", result.name().toLowerCase()); // json객체에 키값이 result이고 값이 VerifyRegisterEmailResult를 가져와서 소문자로 변환
        return responseObject.toString(); //받은 값을 문자열로 리턴시켜줌
    }

    @RequestMapping(value = "resetPassword", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getResetPasword(@SessionAttribute(value = "user", required = false)UserEntity user){
        ModelAndView modelAndView = new ModelAndView();
        if (user == null){
            modelAndView.setViewName("user/resetPassword");
        }else {
            modelAndView.setViewName("redirect:/");
        }
        return modelAndView;
    }
}

//TODO : 이메일 찾기()






