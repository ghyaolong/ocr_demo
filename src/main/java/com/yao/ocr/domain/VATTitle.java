package com.yao.ocr.domain;

import com.yao.ocr.module.Location;

/**
 * Created by Administrator on 2017/11/24.
 */
public class VATTitle {
    private Location location;
    private String title;
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
