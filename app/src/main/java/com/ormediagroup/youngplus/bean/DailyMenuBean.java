package com.ormediagroup.youngplus.bean;

/**
 * Created by Lau on 2019/6/5.
 */
public class DailyMenuBean {
    private String title;
    private String time;
    private String content;

    public DailyMenuBean(String title, String time, String content) {
        this.title = title;
        this.time = time;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
