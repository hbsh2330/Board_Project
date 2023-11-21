package com.yhp.studybbs.mappers;

import com.yhp.studybbs.entities.ArticleEntity;
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

    int insertFile(FileEntity file);
}
