package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.androdocs.httprequest.HttpRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    String API_KEY= "049e4da8d5eae3651344f968007651e3";
    String CITY = "Budapest";

    boolean isDegrees = true;

    AutoCompleteTextView cityName;
    TextView degrees;

    ImageView weatherIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        String[] cities = getResources().getStringArray(R.array.cities_array);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cities);
        cityName.setAdapter(adapter);

        degrees = findViewById(R.id.degreesTextView);
        weatherIcon = findViewById(R.id.weatherIcon);

        new weatherTask().execute("https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&units=metric" + "&appid=" + API_KEY);
    }

    class weatherTask extends AsyncTask<String, Void, String>{
        @Override
        protected String doInBackground(String... params) {
            String response = HttpRequest.excuteGet(params[0]);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                Long updatedAt = jsonObj.getLong("dt");
                String temp = main.getString("temp");
                temp = temp.substring(0,temp.indexOf("."));
                String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
                String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");

                String mainWeather = weather.getString("main");
                String weatherDescription = weather.getString("description");

                if(isDegrees)
                    degrees.setText(temp + "°C");
                else
                    degrees.setText(temp + "F");

                boolean isMorning = false;

                Date currentTime = Calendar.getInstance().getTime();

                String string1 = "06:00:00";
                Date time1 = new SimpleDateFormat("HH:mm:ss").parse(string1);

                String string2 = "18:00:00";
                Date time2 = new SimpleDateFormat("HH:mm:ss").parse(string2);

                if (currentTime.after(time1) && currentTime.before(time2))
                    isMorning = true;

                switch (mainWeather){
                    case "Thunderstorm":
                        weatherIcon.setImageResource(R.drawable.ic_rainy);
                        break;
                    case "Drizzle":
                        weatherIcon.setImageResource(R.drawable.ic_rainy);
                        break;
                    case "Rain":
                        weatherIcon.setImageResource(R.drawable.ic_rainy);
                        break;
                    case "Snow":
                        weatherIcon.setImageResource(R.drawable.ic_snowing);
                        break;
                    case "Clear":
                        if (isMorning)
                            weatherIcon.setImageResource(R.drawable.ic_sunny);
                        else
                            weatherIcon.setImageResource(R.drawable.ic_night_clear);
                        break;
                    case "Clouds":
                        weatherIcon.setImageResource(R.drawable.ic_cloudy);
                        break;
                    default:
                        weatherIcon.setImageResource(R.drawable.ic_sunny);
                        break;
                }

            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
