package com.example.lenovo.coolcoolweather.gson;

import com.google.gson.annotations.SerializedName;

public class Basic {
    //由于json的一些字段可能不适合直接作为java字段来命名，
    // 因此使用注解的方式来在json和java字段之间建立映射关系
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;

    public class Update {
        @SerializedName("loc")
        public String updateTime;
    }
}
