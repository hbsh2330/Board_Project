package com.yhp.studybbs.controllers;

import com.yhp.studybbs.entities.ArticleEntity;
import com.yhp.studybbs.entities.BoardEntity;
import com.yhp.studybbs.entities.UserEntity;
import com.yhp.studybbs.results.article.WriteResult;
import com.yhp.studybbs.services.ArticleService;
import com.yhp.studybbs.services.BoardService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping(value = "article")
public class ArticleController {
    private final BoardService boardService;
    private final ArticleService articleService;
    @Autowired
    public ArticleController(BoardService boardService, ArticleService articleService) {
        this.boardService = boardService;
        this.articleService = articleService;
    }

    @RequestMapping(value = "write", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getWrite(@SessionAttribute(value = "user", required = false)UserEntity user, @RequestAttribute(value = "boards"
    ) BoardEntity[] boards, @RequestParam(value = "code", required = false, defaultValue = "")String code){
        ModelAndView modelAndView = new ModelAndView();
        if (user == null) { //로그인을 하지않으면
            modelAndView.setViewName("redirect:/user/login"); //로그인 페이지로 리다이렉트
        } else {
            BoardEntity board = null; //
            for (BoardEntity b: boards){
                if (b.getCode().equals(code)){ //board에 있는 code값과 requestParam으로 받아온 code값이 같을경우
                    board = b; //board에 borad배열의 값을 집어 넣는다.
                    break;
                }
            }
            boolean allowed = board != null && (!board.isAdminWrite() || user.isAdmin()); //board에 파라미터로 입력한 코드의 값과 내부에 있는 코드의 값이 같거나, 관리자 전용쓰기 일경우, 관리자가  아닐경우 true를 반환
            modelAndView.addObject("board", board);
            modelAndView.addObject("allowed", allowed);
            modelAndView.setViewName("/article/write");
        }
        return modelAndView;
    }

    @RequestMapping(value = "write", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postWrite(@SessionAttribute(value = "user") UserEntity user, ArticleEntity article){
        WriteResult result = this.articleService.writeResult(article, user);
        JSONObject resultObject = new JSONObject();
        resultObject.put("result", result.name().toLowerCase());
        if (result == WriteResult.SUCCESS){
            resultObject.put("index", article.getIndex());
        }
        return resultObject.toString();
    }
}
