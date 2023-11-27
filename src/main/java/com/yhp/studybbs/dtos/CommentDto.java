package com.yhp.studybbs.dtos;

import com.yhp.studybbs.entities.CommentEntity;

public class CommentDto extends CommentEntity {
    private String userNickname;

    public String getUserNickname() {
        return userNickname;
    }

    public CommentDto setUserNickname(String userNickname) {
        this.userNickname = userNickname;
        return this;
    }
}
