package com.yhp.studybbs.services;

import com.yhp.studybbs.dtos.ArticleDto;
import com.yhp.studybbs.entities.BoardEntity;
import com.yhp.studybbs.mappers.BoardMapper;
import com.yhp.studybbs.vos.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoardService {

    private final BoardMapper boardMapper;

    @Autowired
    public BoardService(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    public BoardEntity[] getBoards(){
        return this.boardMapper.selectBoards();
    }

    public ArticleDto[] getArticles(BoardEntity board, PageVo page){
        return this.boardMapper.selectArticleDtosByPage(board, page);
    }

    public int getArticleCount(BoardEntity board){
        return this.boardMapper.selectArticleCountByBoard(board);
    }
}
