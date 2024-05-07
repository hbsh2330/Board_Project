package com.hbsh.bbs.services;

import com.hbsh.bbs.dtos.ArticleDto;
import com.hbsh.bbs.entities.BoardEntity;
import com.hbsh.bbs.mappers.BoardMapper;
import com.hbsh.bbs.vos.PageVo;
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
