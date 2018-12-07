package com.ormediagroup.youngplus.bean;

/**
 * Created by Lau on 2018/11/28.
 */

public class MenuBean {
    private String title;
    private int flag;

    public MenuBean(String title, int flag) {
        this.title = title;
        this.flag = flag;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
