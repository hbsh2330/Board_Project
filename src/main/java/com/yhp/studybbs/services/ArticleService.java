package com.yhp.studybbs.services;

import com.yhp.studybbs.entities.*;
import com.yhp.studybbs.mappers.ArticleMapper;
import com.yhp.studybbs.mappers.BoardMapper;
import com.yhp.studybbs.regexes.ArticleRegex;
import com.yhp.studybbs.results.article.UploadFileResult;
import com.yhp.studybbs.results.article.UploadImageResult;
import com.yhp.studybbs.results.article.WriteResult;
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

    public UploadImageResult uploadImage(ImageEntity image, UserEntity user){
        image.setUserEmail(user.getEmail())
                .setCreatedAt(new Date());
        return this.articleMapper.insertImage(image) > 0
                ? UploadImageResult.SUCCESS
                : UploadImageResult.FAILURE;
    }

    public UploadFileResult uploadFile(FileEntity file, UserEntity user){
        file.setUserEmail(user.getEmail())
                .setCreatedAt(new Date());
        return this.articleMapper.insertFile(file) > 0
                ? UploadFileResult.SUCCESS
                : UploadFileResult.FAILURE;
    }

    public ImageEntity getImage(int index){
        return this.articleMapper.selectImageByIndex(index);
    }
}
