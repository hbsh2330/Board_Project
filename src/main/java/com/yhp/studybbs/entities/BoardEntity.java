package com.yhp.studybbs.entities;

public class BoardEntity {
    private String code;
    private String text;
    private int order;
    private boolean isAdminWrite;

    public String getCode() {
        return code;
    }

    public BoardEntity setCode(String code) {
        this.code = code;
        return this;
    }

    public String getText() {
        return text;
    }

    public BoardEntity setText(String text) {
        this.text = text;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public BoardEntity setOrder(int order) {
        this.order = order;
        return this;
    }

    public boolean isAdminWrite() {
        return isAdminWrite;
    }

    public BoardEntity setAdminWrite(boolean adminWrite) {
        isAdminWrite = adminWrite;
        return this;
    }
}
