package com.hbsh.bbs.dtos;

import com.hbsh.bbs.entities.ArticleEntity;

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
