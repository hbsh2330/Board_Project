package com.yhp.studybbs.services;

import com.yhp.studybbs.dtos.ArticleDto;
import com.yhp.studybbs.dtos.CommentDto;
import com.yhp.studybbs.entities.*;
import com.yhp.studybbs.mappers.ArticleMapper;
import com.yhp.studybbs.mappers.BoardMapper;
import com.yhp.studybbs.regexes.ArticleRegex;
import com.yhp.studybbs.regexes.CommentRegex;
import com.yhp.studybbs.results.article.*;
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
        if (articleInsertResult == 0) {
            return WriteResult.FAILURE;
        }
        for (int fileIndex : fileIndexes) {
            FileEntity file = this.articleMapper.selectFileByIndexNoData(fileIndex);
            if (file == null) {
                continue;
            }
            file.setArticleIndex(article.getIndex());
            this.articleMapper.updateFileNoData(file);
        }
        return WriteResult.SUCCESS;
    }

    public UploadImageResult uploadImage(ImageEntity image, UserEntity user) {
        image.setUserEmail(user.getEmail())
                .setCreatedAt(new Date());
        return this.articleMapper.insertImage(image) > 0
                ? UploadImageResult.SUCCESS
                : UploadImageResult.FAILURE;
    }

    public UploadFileResult uploadFile(FileEntity file, UserEntity user) {
        file.setUserEmail(user.getEmail())
                .setCreatedAt(new Date());
        return this.articleMapper.insertFile(file) > 0
                ? UploadFileResult.SUCCESS
                : UploadFileResult.FAILURE;
    }

    public ImageEntity getImage(int index) {
        return this.articleMapper.selectImageByIndex(index);
    }

    public ArticleEntity getArticle(int index) {
        return this.articleMapper.selectArticleByIndex(index);
    }

    public FileEntity[] getFilesOf(ArticleEntity article) {
        return this.articleMapper.selectFilesByArticleIndexNoData(article.getIndex());
    }

    public FileEntity getFile(int index) {
        return this.articleMapper.selectFileByIndex(index);
    }

    public ArticleDto getArticleDto(int index) {
        ArticleDto article = this.articleMapper.selectArticleDtoByIndex(index);
        if (article != null) {
            article.setView(article.getView() + 1);
            this.articleMapper.updateArticle(article);

        }
        return article;
    }

    public WriteCommentResult writeComment(CommentEntity comment, UserEntity user) {
        if (!CommentRegex.CONTENT.matches(comment.getContent())) {
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

    public CommentDto[] getCommentDtos(int articleIndex, String userEmail) {
        return this.articleMapper.selectCommentDtosByArticleIndex(articleIndex, userEmail);
    }

    public AlterCommentLikeResult alterCommentLike(int commentIndex, Boolean status, UserEntity user) {
        // commentIndex & user.email 로 가져온 CommentLikeEntity가 없고(null)이고
        // status 가 true 라면 좋아요로 Insert
        // status 가 false 라면 싫어요로 Insert
        // status 가 null 이라면, 논리적 오류

        // commentIndex & user.email 로 가져온 CommentLikeEntity가 있고(null이 아니고)
        // status 가 true 라면 좋아요로 수정
        // status 가 false 라면 싫어요로 수정
        // status 가 null 이라면, DELETE
        CommentLikeEntity commentLike = this.articleMapper.selectCommentLike(user.getEmail(), commentIndex);
        if (commentLike == null) { //사용자가 좋아요, 싫어요 한적이 없다.
            commentLike = new CommentLikeEntity()
                    .setUserEmail(user.getEmail())
                    .setCommentIndex(commentIndex)
                    .setLike(status);
        } else {
            if (status == null) {
                return this.articleMapper.deleteCommentLike(user.getEmail(), commentIndex) > 0
                        ? AlterCommentLikeResult.SUCCESS
                        : AlterCommentLikeResult.FAILURE;
            }
            commentLike.setLike(status);
        }
        return this.articleMapper.insertCommentLike(commentLike) > 0
                ? AlterCommentLikeResult.SUCCESS
                : AlterCommentLikeResult.FAILURE;
    }

    public CommentDto getCommentDto(int commentIndex, String userEmail) {
        return this.articleMapper.selectCommentDtoByArticleIndex(commentIndex, userEmail);
    }

    public DeleteCommentResult deleteComment(int index, UserEntity user) {
        CommentEntity comment = this.articleMapper.selectCommentByIndex(index);
        if (!user.getEmail().equals(comment.getUserEmail()) && !user.isAdmin()) {
            System.out.println("0");
            return DeleteCommentResult.FAILURE;
        }
        comment.setDeleted(true);
        System.out.println("1");
        return this.articleMapper.updateComment(comment) > 0
                ? DeleteCommentResult.SUCCESS
                : DeleteCommentResult.FAILURE;
    }
}
