package com.yhp.studybbs.entities;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;

public class FileEntity {
    private int index;
    private String name;
    private String type;
    private int size;
    private byte[] data;
    private Integer articleIndex;
    private String userEmail;
    private Date createdAt;


    public FileEntity(){
        super();
    }
    public FileEntity(MultipartFile file) throws IOException {
        super();
        this.name = file.getOriginalFilename();
        this.type = file.getContentType();
        this.size = (int) file.getSize();
        this.data = file.getBytes();
    }

    public int getIndex() {
        return index;
    }

    public FileEntity setIndex(int index) {
        this.index = index;
        return this;
    }

    public String getName() {
        return name;
    }

    public FileEntity setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public FileEntity setType(String type) {
        this.type = type;
        return this;
    }

    public int getSize() {
        return size;
    }

    public FileEntity setSize(int size) {
        this.size = size;
        return this;
    }

    public byte[] getData() {
        return data;
    }

    public FileEntity setData(byte[] data) {
        this.data = data;
        return this;
    }

    public Integer getArticleIndex() {
        return articleIndex;
    }

    public FileEntity setArticleIndex(Integer articleIndex) {
        this.articleIndex = articleIndex;
        return this;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public FileEntity setUserEmail(String userEmail) {
        this.userEmail = userEmail;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public FileEntity setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}

