package com.yhp.studybbs.mappers;

import com.yhp.studybbs.dtos.ArticleDto;
import com.yhp.studybbs.entities.ArticleEntity;
import com.yhp.studybbs.entities.CommentEntity;
import com.yhp.studybbs.entities.FileEntity;
import com.yhp.studybbs.entities.ImageEntity;
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
}
