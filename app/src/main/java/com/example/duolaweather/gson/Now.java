package com.example.duolaweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Administrator on 2018/2/6.
 */

public class Now {
    @SerializedName("tmp")
    public String temmperature;

    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
}
