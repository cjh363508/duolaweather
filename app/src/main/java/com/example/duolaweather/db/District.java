package com.example.duolaweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2018/2/2.
 */

public class District extends DataSupport {
    private int id;
    private String name;
    private int cityId;
    private String weatherId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
