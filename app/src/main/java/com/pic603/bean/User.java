package com.pic603.bean;

import android.content.Intent;

import java.io.Serializable;

import cn.bmob.v3.BmobUser;

public class User extends BmobUser implements Serializable {
    private String briefIntro;
    private String headPortrait;
    private Integer followNum;

    public String getBriefIntro() {
        return briefIntro;
    }

    public void setBriefIntro(String briefIntro) {
        this.briefIntro = briefIntro;
    }

    public String getHeadPortrait() {
        return headPortrait;
    }

    public void setHeadPortrait(String headPortrait) {
        this.headPortrait = headPortrait;
    }

    public Integer getFollowNum() {
        return followNum;
    }

    public void setFollowNum(Integer followNum) {
        this.followNum = followNum;
    }
}
