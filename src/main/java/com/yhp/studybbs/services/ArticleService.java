package com.yhp.studybbs.services;

import com.yhp.studybbs.entities.ArticleEntity;
import com.yhp.studybbs.entities.BoardEntity;
import com.yhp.studybbs.entities.UserEntity;
import com.yhp.studybbs.mappers.ArticleMapper;
import com.yhp.studybbs.mappers.BoardMapper;
import com.yhp.studybbs.regexes.ArticleRegex;
import com.yhp.studybbs.results.article.WriteResult;
import com.yhp.studybbs.results.user.RegisterResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ArticleService {
    private final ArticleMapper articleMapper;
    private final BoardMapper boardMapper;

    @Autowired
    public ArticleService(ArticleMapper articleMapper, BoardMapper boardMapper) {
        this.articleMapper = articleMapper;
        this.boardMapper = boardMapper;
    }

    public WriteResult writeResult(ArticleEntity article, UserEntity user) {
        if (!ArticleRegex.TITLE.matches(article.getTitle())) {
            return WriteResult.FAILURE;
        }
        BoardEntity board = this.boardMapper.selectBoardByCode(article.getBoardCode());
        if (board == null) {
            return WriteResult.FAILURE;
        }
        article.setUserEmail(user.getEmail())
                .setView(0)
                .setWrittenAt(new Date())
                .setModifiedAt(null)
                .setDeleted(false);

        if (!board.isAdminWrite() && user.isAdmin()) { //만약 관리자 계정이 아니고, user.isAdmin이 false가 아니라면
            return WriteResult.FAILURE;
        }
            return this.articleMapper.insertArticle(article) > 0
                    ? WriteResult.SUCCESS
                    : WriteResult.FAILURE;
    }
}
