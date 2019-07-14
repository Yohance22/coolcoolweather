package com.example.lenovo.coolcoolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.lenovo.coolcoolweather.gson.Forecast;
import com.example.lenovo.coolcoolweather.gson.Weather;
import com.example.lenovo.coolcoolweather.util.HttpUtil;
import com.example.lenovo.coolcoolweather.util.Utility;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weather_layout;
    private TextView title_city;
    private TextView title_update_time;
    private TextView degree_text;
    private TextView weather_info_text;
    private TextView pm25_text;
    private TextView aqi_text;
    private TextView comfort_text;
    private TextView car_wash_text;
    private TextView sport_text;
    private LinearLayout forcast_layout;
    private ImageView bing_pic_img;

    private TextView date_text;
    private TextView info_text;
    private TextView max_text;
    private TextView min_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        weather_layout = findViewById(R.id.weather_layout);
        title_city = findViewById(R.id.title_city);
        title_update_time = findViewById(R.id.title_update_time);
        degree_text = findViewById(R.id.degree_text);
        weather_info_text = findViewById(R.id.weather_info_text);
        pm25_text = findViewById(R.id.pm25_text);
        aqi_text = findViewById(R.id.aqi_text);
        comfort_text = findViewById(R.id.comfort_text);
        car_wash_text = findViewById(R.id.car_wash_text);
        sport_text = findViewById(R.id.sport_text);
        forcast_layout = findViewById(R.id.forcast_layout);
        bing_pic_img = findViewById(R.id.bing_pic_img);


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        if (weatherString != null) {
            //有缓存时候直接解析数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        } else {
            //五缓存时候去服务器解析数据
            String weatherId = getIntent().getStringExtra("weather_id");
            weather_layout.setVisibility(View.VISIBLE);
            requestWeather(weatherId);
        }
        String bingpic = prefs.getString("bing_pic", null);
        if (bingpic != null) {
            Glide.with(this).load(bingpic).into(bing_pic_img);

        } else {
            loadBingPic();
        }
    }

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bing_pic_img);
                    }
                });

            }
        });
    }

    //根据请求的id请求城市天气的信息
    private void requestWeather(String weatherId) {
        //http://guolin.tech/api/weather?cityid=CN101190401&key=de37aed2cb4140dcb67697e33e401c7d
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=de37aed2cb4140dcb67697e33e401c7d";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                    }
                });


            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

            }
        });
        loadBingPic();
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "°C";
        String weatherInfo = weather.now.more.info;
        title_city.setText(cityName);
        title_update_time.setText(updateTime);


        degree_text.setText(degree);
        weather_info_text.setText(weatherInfo);
        forcast_layout.removeAllViews();
        List<Forecast> forecastList = weather.forecastList;
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                    forcast_layout, false);
            TextView datetext = (TextView) view.findViewById(R.id.date_text);
            TextView infotext = (TextView) view.findViewById(R.id.info_text);
            TextView maxtext = (TextView) view.findViewById(R.id.max_text);
            TextView mintext = (TextView) view.findViewById(R.id.min_text);
            datetext.setText(forecast.date);
            infotext.setText(forecast.more.info);
            maxtext.setText(forecast.temperature.max);
            mintext.setText(forecast.temperature.min);
            forcast_layout.addView(view);
        }
        if (weather.aqi != null) {
            aqi_text.setText(weather.aqi.city.aqi);
            pm25_text.setText(weather.aqi.city.pm25);
        }
        String sport = "运动指数: " + weather.suggestion.sport.info;
        String comfort = "舒适度: " + weather.suggestion.comfort.info;
        String carWash = "洗车指数: " + weather.suggestion.carWash.info;
        comfort_text.setText(comfort);
        car_wash_text.setText(carWash);
        sport_text.setText(sport);
        weather_layout.setVisibility(View.VISIBLE);
    }

}
