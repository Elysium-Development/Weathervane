package com.elysium.weathervane.Model;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.elysium.weathervane.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;

import static android.support.v7.appcompat.R.id.time;

/**
 * Created by jay on 3/11/17.
 */

public class DailyWeatherReport {

    private String cityName;
    private String country;
    private String weather;
    private String formattedDate;

    private String dateDay;
    private String dateDate;

    // Constants
    public static final String WEATHER_TYPE_CLOUDS = "Clouds";
    public static final String WEATHER_TYPE_CLEAR = "Clear";
    public static final String WEATHER_TYPE_RAIN = "Rain";
    public static final String WEATHER_TYPE_SNOW = "Snow";
    public static final String WEATHER_TYPE_WIND = "Wind";
    public static final String WEATHER_TYPE_PARTIALLY_CLOUDY = "Partial Clouds";

    public String getDateDay() {
        return dateDay;
    }

    public String getDateDate() {
        return dateDate;
    }


    public String getCityName() {
        return cityName;
    }

    public String getCountry() {
        return country;
    }

    public String getWeather() {
        return weather;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    private int currentTemp;
    private int maxTemp;
    private int minTemp;


    public DailyWeatherReport(String cityName, String country, String weather, String rawDate,
                              int currentTemp, int maxTemp, int minTemp, String dateDay, String dateDate) {

        this.cityName = cityName;
        this.country = country;
        this.weather = weather;
        this.formattedDate = rawDateToFormatted();
        this.currentTemp = currentTemp;
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.dateDay = dateDay;
        this.dateDate = dateDate;
    }

    public String rawDateToFormatted() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("h:mm"); // E, MMM d Locale.getDefault()
        String strDate = format.format(calendar.getTime());

//        dateDay = values[0];
//        dateDate = values[1];

        return strDate;

        /**
         * The below is sample code that (as it stands) returns the current time (when returning strDate) and a
         * time stamp that is simply too long [2017-03-13 06:00:00] when rawDate is subbed in...
         * TODO - get rawDate formatted properly, so only the hours and minutes are displayed (12 hr format)
         * */

    }
}
