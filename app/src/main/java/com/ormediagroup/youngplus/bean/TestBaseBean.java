package com.ormediagroup.youngplus.bean;

/**
 * Created by Lau on 2018/12/6.
 */

public class TestBaseBean extends BaseBean {
    private String title;
    private String image;

    public TestBaseBean(int type, String title, String image) {
        super(type);
        this.title = title;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
