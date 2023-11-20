package com.yhp.studybbs.mappers;

import com.yhp.studybbs.entities.ArticleEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleMapper {
    //int 는 영향을 받은 레코드의 개수다.
    int insertArticle(ArticleEntity article);
}
