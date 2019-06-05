package com.ormediagroup.youngplus.bean;

/**
 * Created by Lau on 2019/5/27.
 */
public class SingleSelectBean {
    private int imageRes;
    private String title;
    private String content;

    public SingleSelectBean(int imageRes, String title, String content) {
        this.imageRes = imageRes;
        this.title = title;
        this.content = content;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
