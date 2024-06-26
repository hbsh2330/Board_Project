package com.hbsh.bbs.mappers;

import com.hbsh.bbs.dtos.ArticleDto;
import com.hbsh.bbs.dtos.CommentDto;
import com.hbsh.bbs.entities.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleMapper {
    //int 는 영향을 받은 레코드의 개수다.
    int insertArticle(ArticleEntity article);

    int insertImage(ImageEntity image);

    ImageEntity selectImageByIndex(@Param(value = "index") int index);

    ArticleDto selectArticleDtoByIndex(@Param(value = "index") int index);

    int insertFile(FileEntity file);

    FileEntity selectFileByIndexNoData(@Param(value = "index") int index); //데이터를 제외하고 샐렉트한다,.

    int updateFileNoData(FileEntity file);

    ArticleEntity selectArticleByIndex(@Param(value = "index") int index);

    FileEntity[] selectFilesByArticleIndexNoData(@Param(value = "articleIndex") int articleIndex);

    FileEntity selectFileByIndex(@Param(value = "index") int index);

    int updateArticle(ArticleEntity article);

    int insertComment(CommentEntity comment);

    CommentDto[] selectCommentDtosByArticleIndex(@Param(value = "articleIndex") int articleIndex,
                                                 @Param(value = "userEmail") String userEmail);

    CommentDto selectCommentDtoByArticleIndex(@Param(value = "commentIndex") int commentIndex,
                                              @Param(value = "userEmail") String userEmail);

    CommentLikeEntity selectCommentLike(@Param(value = "userEmail") String userEmail,
                                        @Param(value = "commentIndex") int commentIndex);

    int deleteCommentLike(@Param(value = "userEmail") String userEmail,
                          @Param(value = "commentIndex") int commentIndex);

    int insertCommentLike(CommentLikeEntity commentLike);

    CommentEntity selectCommentByIndex(@Param(value = "index") int index);

    int updateComment(CommentEntity comment);

    int deleteFileByIndex(@Param(value = "index") int index);
}
