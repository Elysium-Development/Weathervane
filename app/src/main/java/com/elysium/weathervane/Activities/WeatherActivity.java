package com.elysium.weathervane.Activities;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elysium.weathervane.*;
import com.elysium.weathervane.Model.DailyWeatherReport;
import com.elysium.weathervane.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast";
    final String URL_COORDINATES = "?lat=";
    final String URL_UNITS = "&units=imperial";
    final String URL_API_KEY = "&APPID=a95179be3025ddbf5cd35baa6066d916";

    private ArrayList<DailyWeatherReport> weatherReportList = new ArrayList<>();

    private final int PERMISSION_LOCATION = 111;

    private GoogleApiClient mGoogleApiClient;

    private ImageView weatherIconBig;
    private ImageView weatherIconMini;

    private TextView currentTemp;
    private TextView lowTemp;
    private TextView cityCountry;
    private TextView currentConditions;

    private TextView dateDay;
    private TextView dateMonth;
    private TextView dateDate;
    private TextView dateYear;

    weatherAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.elysium.weathervane.R.layout.activity_weather);

        /**
         * TODO - consider creating a Singleton for this boilerplate code, to have it manage
         * all of your Google API requests. enableAutoManage  automates the lifecycle, so no
         * need to call onStart or onStop
         * */
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        weatherIconBig = (ImageView) findViewById(R.id.current_weather_icon);
        weatherIconMini = (ImageView) findViewById(R.id.weather_logo_icon);

        currentTemp = (TextView) findViewById(R.id.current_temp_txt);
        lowTemp = (TextView) findViewById(R.id.low_temp_txt);
        cityCountry = (TextView) findViewById(R.id.location_txt);
        currentConditions = (TextView) findViewById(R.id.conditions_txt);

        dateDay = (TextView) findViewById(R.id.date_day_txt);
        dateMonth = (TextView) findViewById(R.id.date_month_txt);
        dateDate = (TextView) findViewById(R.id.date_date_txt);
        dateYear = (TextView) findViewById(R.id.date_year_txt);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.content_weather_reports);

        adapter = new weatherAdapter(weatherReportList);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
    }

    public void downloadWeatherData(Location location) {

        final String fullCoords = URL_COORDINATES + location.getLatitude() + "&lon=" + location.getLongitude();

        final String url = BASE_URL + fullCoords + URL_UNITS + URL_API_KEY;

        Log.v("FUN2", "URL" + url);

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            // Getting city name
                            JSONObject city = response.getJSONObject("city");
                            String cityName = city.getString("name");
                            String country = city.getString("country");

                            JSONArray list = response.getJSONArray("list");

                            // Using this for loop to get the min and max temp, as well as the
                            // weather conditions (eg. Clear, Snow, etc), and date
                            for (int i = 0; i < 5; i++) {

                                JSONObject obj = list.getJSONObject(i);
                                JSONObject main = obj.getJSONObject("main");
                                Double currentTemp = main.getDouble("temp");
                                Double maxTemp = main.getDouble("temp_max");
                                Double minTemp = main.getDouble("temp_min");

                                JSONArray weatherArr = obj.getJSONArray("weather");
                                JSONObject weather = weatherArr.getJSONObject(0);
                                String weatherType = weather.getString("main");

                                String rawDate = obj.getString("dt_txt");
                                String dateDay = obj.getString("dt_txt");
                                String dateDate = obj.getString("dt_txt");
                                String dateTime = obj.getString("dt_txt");

                                DailyWeatherReport report = new DailyWeatherReport(
                                        cityName,
                                        country,
                                        weatherType,
                                        rawDate,
                                        currentTemp.intValue(),
                                        maxTemp.intValue(),
                                        minTemp.intValue(),
                                        dateDay,
                                        dateDate);

                                Log.v("JSON", "Printing from class: " + report.getWeather());
                                weatherReportList.add(report);
                            }

                            Log.v("CITY", cityName + ", " + country);

                        } catch (JSONException e) {

                            Log.v("JSON", "EXE: " + e.getLocalizedMessage());
                        }

                        updateUI();
                        adapter.notifyDataSetChanged();
                    }

                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Log.v("FUN", "Err: " + error.getLocalizedMessage());
            }
        });

        Volley.newRequestQueue(this).add(jsonRequest);
    }

    public void updateUI() {

        if (weatherReportList.size() > 0) {

            DailyWeatherReport report = weatherReportList.get(0);

            switch (report.getWeather()) {

                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:

                    weatherIconBig.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.cloudy));
                    break;

                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    weatherIconBig.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.rainy));
                    break;

                case DailyWeatherReport.WEATHER_TYPE_SNOW:
                    weatherIconBig.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.snow));
                    break;

                case DailyWeatherReport.WEATHER_TYPE_PARTIALLY_CLOUDY:
                    weatherIconBig.setImageDrawable(getResources().getDrawable(R.drawable.partially_cloudy));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.partially_cloudy));
                    break;
                // TODO - find corresponding icon for WINDY

                default:
                    weatherIconBig.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                    weatherIconMini.setImageDrawable(getResources().getDrawable(R.drawable.sunny));
                    break;
            }

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MMM/d/E", Locale.getDefault());
            String strDate = format.format(calendar.getTime());

            String[] values = strDate.split("/", 0);

            dateDay.setText(values[3]);
            dateMonth.setText(values[1] + ",");
            dateDate.setText(values[2]);
            dateYear.setText(values[0]);

            currentTemp.setText(Integer.toString(report.getCurrentTemp()) + "\u2109");
            lowTemp.setText(Integer.toString(report.getMinTemp()) + " \u2109");
            cityCountry.setText(report.getCityName() + ", " + report.getCountry());
            currentConditions.setText(report.getWeather());
        }
    }

    // Implemented from above
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);

        } else {

            startLocationServices();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

        downloadWeatherData(location);
    }

    public void startLocationServices() {

        try {

            LocationRequest request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);

        } catch (SecurityException exception) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case PERMISSION_LOCATION: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startLocationServices();

                } else {

                    Toast.makeText(this, "I can't run your location, dummy - you denied permissions", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public class weatherAdapter extends RecyclerView.Adapter<weatherAdapter.WeatherReportViewHolder> {

        public weatherAdapter(ArrayList<DailyWeatherReport> dailyWeatherReports) {
            this.dailyWeatherReports = dailyWeatherReports;
        }

        private ArrayList<DailyWeatherReport> dailyWeatherReports;

        @Override
        public WeatherReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, parent, false);

            return new WeatherReportViewHolder(card);
        }

        @Override
        public void onBindViewHolder(WeatherReportViewHolder holder, int position) {

            DailyWeatherReport report = dailyWeatherReports.get(position);
            holder.updateUI(report);
        }

        @Override
        public int getItemCount() {

            return dailyWeatherReports.size();
        }


        public class WeatherReportViewHolder extends RecyclerView.ViewHolder {

            private ImageView forecastIcon;

            private TextView forecastDate;
            private TextView forecastConditions;
            private TextView forecastTempHigh;
            private TextView forecastTempLow;
            private TextView forecastDay;

            public WeatherReportViewHolder(View itemView) {
                super(itemView);

                forecastIcon = (ImageView) itemView.findViewById(R.id.forecast_icon);

                forecastDate = (TextView) itemView.findViewById(R.id.forecast_day);
                forecastConditions = (TextView) itemView.findViewById(R.id.forecast_conditions);
                forecastTempHigh = (TextView) itemView.findViewById(R.id.forecast_high_temp);
                forecastTempLow = (TextView) itemView.findViewById(R.id.forecast_low_temp);
                forecastDay = (TextView) itemView.findViewById(R.id.forecast_day);
            }

            public void updateUI(DailyWeatherReport report) {

                forecastDate.setText(report.getFormattedDate());
                forecastConditions.setText(report.getWeather());
                forecastTempHigh.setText(Integer.toString(report.getMaxTemp()) + " \u2109"); // "\u2109" is used to set degrees
                forecastTempLow.setText(Integer.toString(report.getMinTemp()) + " \u2109");

                /**
                 * TODO - FIGURE OUT HOW TO RETURN INCREMENTAL (3 HOUR) VALUES ON EACH RECYCLERVIEW ITEM
                 * */
                forecastDay.setText("Forecast");

                switch (report.getWeather()) {

                    case DailyWeatherReport.WEATHER_TYPE_CLOUDS:

                        forecastIcon.setImageDrawable(getResources().getDrawable(R.drawable.cloudy_mini));
                        break;

                    case DailyWeatherReport.WEATHER_TYPE_RAIN:
                        forecastIcon.setImageDrawable(getResources().getDrawable(R.drawable.rainy_mini));
                        break;

                    case DailyWeatherReport.WEATHER_TYPE_SNOW:
                        forecastIcon.setImageDrawable(getResources().getDrawable(R.drawable.snow_mini));
                        break;

                    case DailyWeatherReport.WEATHER_TYPE_PARTIALLY_CLOUDY:
                        forecastIcon.setImageDrawable(getResources().getDrawable(R.drawable.partially_cloudy_mini));
                        break;

                    // TODO - find corresponding icon for WINDY

                    default:
                        forecastIcon.setImageDrawable(getResources().getDrawable(R.drawable.sunny_mini));
                }
            }
        }
    }
}
