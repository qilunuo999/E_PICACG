package com.pic603.bean;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobDate;

public class Following extends BmobObject {
    private String concernId;
    private String followedId;

    public String getConcernId() {
        return concernId;
    }

    public void setConcernId(String concernId) {
        this.concernId = concernId;
    }

    public String getFollowedId() {
        return followedId;
    }

    public void setFollowedId(String followId) {
        this.followedId = followId;
    }
}
