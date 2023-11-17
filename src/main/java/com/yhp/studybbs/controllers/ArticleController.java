package com.yhp.studybbs.controllers;

import com.yhp.studybbs.entities.BoardEntity;
import com.yhp.studybbs.entities.UserEntity;
import com.yhp.studybbs.services.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.Array;
import java.util.Arrays;

@Controller
@RequestMapping(value = "article")
public class ArticleController {
    private final BoardService boardService;

    @Autowired
    public ArticleController(BoardService boardService) {
        this.boardService = boardService;
    }

    @RequestMapping(value = "write", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getWrite(@SessionAttribute(value = "user", required = false)UserEntity user, @RequestAttribute(value = "boards"
    ) BoardEntity[] boards, @RequestParam(value = "code", required = false, defaultValue = "")String code){
        ModelAndView modelAndView = new ModelAndView();
        if (user == null) {
            modelAndView.setViewName("redirect:/user/login");
        } else {

            BoardEntity board = null;
            for (BoardEntity b: boards){
                if (b.getCode().equals(code)){
                    board = b;
                    break;
                }
            }
            boolean allowed = board != null && (!board.isAdminWrite() || user.isAdmin());
            modelAndView.addObject("board", board);
            modelAndView.addObject("allowed", allowed);
            modelAndView.setViewName("/article/write");
        }
        return modelAndView;
    }
}
