package com.yhp.studybbs.dtos;

import com.yhp.studybbs.entities.ArticleEntity;

public class ArticleDto extends ArticleEntity {
    private String userNickname;

    public String getUserNickname() {
        return userNickname;
    }

    public ArticleDto setUserNickname(String userNickname) {
        this.userNickname = userNickname;
        return this;
    }

}
