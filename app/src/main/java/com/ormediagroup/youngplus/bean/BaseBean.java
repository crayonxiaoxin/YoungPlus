package com.ormediagroup.youngplus.bean;

/**
 * Created by Lau on 2018/12/6.
 */

public class BaseBean {

    private int type;

    public BaseBean(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
