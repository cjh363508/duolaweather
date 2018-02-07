package com.example.duolaweather.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.duolaweather.db.City;
import com.example.duolaweather.db.District;
import com.example.duolaweather.db.Province;
import com.example.duolaweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018/2/4.
 */

public class Utility {

    /*
    * 解析和处理服务器返回的省级数据
    * */
    public static boolean processProvinceJson(String strJson) {
        if (!TextUtils.isEmpty(strJson)) {
            try {
                JSONArray allProvinces = new JSONArray(strJson);
                for (int i = 0; i < allProvinces.length(); ++i) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setName(provinceObject.getString("name"));
                    province.setCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    * 解析和处理服务器返回的市级数据
    * */
    public static boolean processCityJson(String strJson, int provinceId) {
        if (!TextUtils.isEmpty(strJson)) {
            try {
                JSONArray allCities = new JSONArray(strJson);
                for (int i = 0; i < allCities.length(); ++i) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setName(cityObject.getString("name"));
                    city.setCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    * 解析和处理服务器返回的市级数据
    * */
    public static boolean processDistrictJson(String strJson, int cityId) {
        if (!TextUtils.isEmpty(strJson)) {
            try {
                JSONArray allDistricts = new JSONArray(strJson);
                for (int i = 0; i < allDistricts.length(); ++i) {
                    JSONObject districtObject = allDistricts.getJSONObject(i);
                    District district = new District();
                    district.setName(districtObject.getString("name"));
                    district.setWeatherId(districtObject.getString("weather_id"));
                    district.setCityId(cityId);
                    district.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    * 解析天气json数据
     */
    public static Weather processWeatherJson(String strJson){
        try{
            JSONObject jsonObject=new JSONObject(strJson);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
