package com.yhp.studybbs.controllers;

import com.yhp.studybbs.dtos.ArticleDto;
import com.yhp.studybbs.entities.BoardEntity;
import com.yhp.studybbs.services.BoardService;
import com.yhp.studybbs.vos.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

@Controller
@RequestMapping(value = "board")
public class BoardController {
    private final BoardService boardService;

    @Autowired
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @RequestMapping(value = "list",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView getList(@RequestAttribute(value = "boards") BoardEntity[] boards,
                                @RequestParam(value = "code") String code,
                                @RequestParam(value = "page", required = false, defaultValue = "1") int page) {
        ModelAndView modelAndView = new ModelAndView();
        BoardEntity board = Arrays.stream(boards).filter(x -> x.getCode().equals(code)).findFirst().orElse(null);
        if (board != null){
            final int countPerPage = 20;
            final int pageButtonCount = 10;
            int totalCount = this.boardService.getArticleCount(board);
            PageVo pageVo = new PageVo(page, countPerPage, pageButtonCount, totalCount);
            ArticleDto[] articles = this.boardService.getArticles(board, pageVo);
            modelAndView.addObject("articles", articles);
            modelAndView.addObject("page", pageVo);
        }
        modelAndView.setViewName("board/list");
        modelAndView.addObject("board", board);
        return modelAndView;
    }

}
