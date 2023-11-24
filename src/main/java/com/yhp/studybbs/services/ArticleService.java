package com.yhp.studybbs.services;

import com.yhp.studybbs.dtos.ArticleDto;
import com.yhp.studybbs.entities.*;
import com.yhp.studybbs.mappers.ArticleMapper;
import com.yhp.studybbs.mappers.BoardMapper;
import com.yhp.studybbs.regexes.ArticleRegex;
import com.yhp.studybbs.regexes.CommentRegex;
import com.yhp.studybbs.results.article.UploadFileResult;
import com.yhp.studybbs.results.article.UploadImageResult;
import com.yhp.studybbs.results.article.WriteCommentResult;
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

    public WriteResult write(ArticleEntity article, int[] fileIndexes, UserEntity user) {
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
        int articleInsertResult = this.articleMapper.insertArticle(article);
        if (articleInsertResult == 0){
            return WriteResult.FAILURE;
        }
        for (int fileIndex : fileIndexes){
            FileEntity file = this.articleMapper.selectFileByIndexNoData(fileIndex);
            if (file == null){
                continue;
            }
            file.setArticleIndex(article.getIndex());
            this.articleMapper.updateFileNoData(file);
        }
        return WriteResult.SUCCESS;
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

    public ArticleEntity getArticle(int index){
        return this.articleMapper.selectArticleByIndex(index);
    }

    public FileEntity[] getFilesOf(ArticleEntity article){
        return this.articleMapper.selectFilesByArticleIndexNoData(article.getIndex());
    }

    public FileEntity getFile(int index){
        return this.articleMapper.selectFileByIndex(index);
    }

    public ArticleDto getArticleDto(int index){
        ArticleDto article = this.articleMapper.selectArticleDtoByIndex(index);
        if (article != null){
            article.setView(article.getView() + 1);
            this.articleMapper.updateArticle(article);

        }
        return article;
    }
    public WriteCommentResult writeComment(CommentEntity comment, UserEntity user){
        if (!CommentRegex.CONTENT.matches(comment.getContent())){
            return WriteCommentResult.FAILURE;
        }
        comment.setUserEmail(user.getEmail())
                .setWrittenAt(new Date())
                .setModifiedAt(null)
                .setDeleted(false);
        return this.articleMapper.insertComment(comment) > 0
                ? WriteCommentResult.SUCCESS
                : WriteCommentResult.FAILURE;
    }
}
