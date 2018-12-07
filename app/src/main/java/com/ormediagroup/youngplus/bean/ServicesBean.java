package com.ormediagroup.youngplus.bean;

/**
 * Created by Lau on 2018/11/26.
 */

public class ServicesBean {
    private int ID;
    private String title;
    private String img;
    private int detailID;

    public ServicesBean(int ID, String title, String img, int detailID) {
        this.ID = ID;
        this.title = title;
        this.img = img;
        this.detailID = detailID;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public int getDetailID() {
        return detailID;
    }

    public void setDetailID(int detailID) {
        this.detailID = detailID;
    }
}
