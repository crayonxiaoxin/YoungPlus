package com.ormediagroup.youngplus.bean;

import android.widget.ImageView;

/**
 * Created by Lau on 2019/5/27.
 */
public class ImageUploadBean {
    private ImageView imageView;
    private String field;
    private int rc1;
    private int rc2;

    public ImageUploadBean(ImageView imageView, String field, int rc1, int rc2) {
        this.imageView = imageView;
        this.field = field;
        this.rc1 = rc1;
        this.rc2 = rc2;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public int getRc1() {
        return rc1;
    }

    public void setRc1(int rc1) {
        this.rc1 = rc1;
    }

    public int getRc2() {
        return rc2;
    }

    public void setRc2(int rc2) {
        this.rc2 = rc2;
    }
}
