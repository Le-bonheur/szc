package com.ssc.szc.app.entity;

public class User {

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public User(String pid) {
        this.pid = pid;
    }

    private String pid;

}
