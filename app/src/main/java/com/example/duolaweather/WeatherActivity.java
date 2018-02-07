package com.example.duolaweather;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.duolaweather.gson.Forecast;
import com.example.duolaweather.gson.Weather;
import com.example.duolaweather.util.HttpUtil;
import com.example.duolaweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView scrollViewWeather;
    private TextView textTitleCity;
    private TextView textTitleUpdateTime;
    private TextView textDegree;
    private TextView textWeatherInfo;
    private LinearLayout layoutForecast;
    private TextView textAQI;
    private TextView textPM25;
    private TextView textComfort;
    private TextView textWashCar;
    private TextView textSport;
    public SwipeRefreshLayout swipeRefreshLayout;
    public DrawerLayout drawerLayout;
    private Button btnNav;
    String weatherId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        scrollViewWeather=(ScrollView)findViewById(R.id.layout_weather);
        textTitleCity=(TextView)findViewById(R.id.text_city);
        textTitleUpdateTime=(TextView)findViewById(R.id.text_update_time);
        textDegree=(TextView)findViewById(R.id.text_degree);
        textWeatherInfo=(TextView)findViewById(R.id.text_weather_info);
        layoutForecast=(LinearLayout)findViewById(R.id.linear_layout_forecast);
        textAQI=(TextView)findViewById(R.id.text_aqi);
        textPM25=(TextView)findViewById(R.id.text_pm25);
        textComfort=(TextView)findViewById(R.id.text_comfort);
        textWashCar=(TextView)findViewById(R.id.text_wash_car);
        textSport=(TextView)findViewById(R.id.text_sport);
        swipeRefreshLayout=(SwipeRefreshLayout)findViewById(R.id.swipe_refresh);
        drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
        btnNav=(Button)findViewById(R.id.btn_nav);
        btnNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherJson=prefs.getString("weather",null);
        if(weatherJson!=null){//天气数据缓存
            Weather weather= Utility.processWeatherJson(weatherJson);
            weatherId=weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            weatherId=getIntent().getStringExtra("weather_id");
            scrollViewWeather.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
    }

    public void requestWeather(final String weatherId) {
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=8ae1aab99bc841898faaf5453616f1ed";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this,R.string.get_weather_fail,Toast.LENGTH_SHORT).show();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String strResponse=response.body().string();
                final Weather weather=Utility.processWeatherJson(strResponse);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",strResponse);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,R.string.get_weather_fail,Toast.LENGTH_SHORT).show();
                        }
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName=weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];
        String degree=weather.now.temmperature+"℃";
        String weatherInfo=weather.now.more.info;
        textTitleCity.setText(cityName);
        textTitleUpdateTime.setText(updateTime);
        textDegree.setText(degree);
        textWeatherInfo.setText(weatherInfo);
        layoutForecast.removeAllViews();
        for(Forecast forecast : weather.listForecast){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,layoutForecast,false);
            TextView textDate=(TextView)view.findViewById(R.id.text_date);
            TextView textInfo=(TextView)view.findViewById(R.id.text_info);
            TextView textMaxTem=(TextView)view.findViewById(R.id.text_max_tem);
            TextView textMinTem=(TextView)view.findViewById(R.id.text_min_tem);
            textDate.setText(forecast.date);
            textInfo.setText(forecast.more.info);
            textMaxTem.setText(forecast.temperature.max);
            textMinTem.setText(forecast.temperature.min);
            layoutForecast.addView(view);
        }
        if(weather.aqi!=null){
            textAQI.setText(weather.aqi.city.aqi);
            textPM25.setText(weather.aqi.city.pm25);
        }
        String comfort=getResources().getString(R.string.comfort_index)+weather.suggestion.comf.info;
        String washCar=getResources().getString(R.string.wash_car_index)+weather.suggestion.carWash.info;
        String sport=getResources().getString(R.string.sport_suggesting)+weather.suggestion.sport.info;
        textComfort.setText(comfort);
        textWashCar.setText(washCar);
        textSport.setText(sport);
        scrollViewWeather.setVisibility(View.VISIBLE);
    }//showWeahterInfo
}
