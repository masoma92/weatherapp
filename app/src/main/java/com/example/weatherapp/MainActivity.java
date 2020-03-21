package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    TextView degrees, cityTextView;

    ImageView weatherIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        cityName = findViewById(R.id.cityNameEditText);
        cityName.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                cityName.showDropDown();
                return false;
            }
        });

        cityName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    changeCity(v);
                    return true;
                }
                return false;
            }
        });

        String[] cities = getResources().getStringArray(R.array.cities_array);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, cities);
        cityName.setAdapter(adapter);

        degrees = findViewById(R.id.degreesTextView);
        weatherIcon = findViewById(R.id.weatherIcon);
        cityTextView = findViewById(R.id.cityTextView);


        new weatherTask().execute("https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&units=metric" + "&appid=" + API_KEY);




    }

    public void changeCity(View v) {
        this.CITY = this.cityName.getText().toString();
        if(this.CITY.isEmpty()){
            showToast("No city added.");
            hideKeyboard();
            return;
        }

        this.cityTextView.setText(this.CITY);
        this.CITY = this.CITY.replaceAll(" ", "");
        new weatherTask().execute("https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&units=metric" + "&appid=" + API_KEY);
        hideKeyboard();

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
                showToast("No city found!");
                e.printStackTrace();
            }

        }


    }
    private void showToast(String msg){
        Context context = getApplicationContext();
        CharSequence text = msg;
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void hideKeyboard(){
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
