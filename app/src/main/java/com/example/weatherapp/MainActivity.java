package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity {
    String API_KEY= "049e4da8d5eae3651344f968007651e3";
    String CITY = "Budapest";

    boolean isDegrees = true;
    String degreesValue = "°C";

    AutoCompleteTextView cityName;
    TextView degrees, cityTextView, mainWeatherValue, minTempValue, maxTempValue, windSpeedValue, humidityValue;

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
        minTempValue = findViewById(R.id.mintTempValue);
        maxTempValue = findViewById(R.id.maxTempValue);
        humidityValue = findViewById(R.id.humidityValue);
        windSpeedValue = findViewById(R.id.windSpeedValue);
        mainWeatherValue = findViewById(R.id.mainWeatherTextView);


        new weatherTask().execute("https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&units=metric&appid=" + API_KEY);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View v = getCurrentFocus();

        if (v != null && (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_MOVE) && v instanceof EditText &&
                !v.getClass().getName().startsWith("android.webkit.")) {
            int scrcoords[] = new int[2];
            v.getLocationOnScreen(scrcoords);
            float x = ev.getRawX() + v.getLeft() - scrcoords[0];
            float y = ev.getRawY() + v.getTop() - scrcoords[1];

            if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom())
                hideKeyboard();
        }
        return super.dispatchTouchEvent(ev);
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
        new weatherTask().execute("https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&units=metric&appid=" + API_KEY);
        hideKeyboard();

    }

    public void mapOnClick(View view) {
        if(CITY != null || !CITY.isEmpty()){
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            intent.putExtra("CITY", CITY);
            startActivity(intent);
        }
    }

    public void wikiOnClick(View view) {
        if (CITY == null || CITY.equals("")) return;
        String url = "https://hu.wikipedia.org/wiki/"+CITY;

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
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
                setEndOfTemp();
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                String temp = Integer.toString((int)Double.parseDouble(main.getString("temp")));

                String tempMin = ((int)Double.parseDouble(main.getString("temp_min"))) + degreesValue;
                String tempMax = ((int)Double.parseDouble(main.getString("temp_max"))) + degreesValue;
                String humidity = main.getString("humidity");

                String mainWeather = weather.getString("main");
                String windSpeed = jsonObj.getJSONObject("wind").getString("speed");

                degrees.setText(temp + degreesValue);
                minTempValue.setText(tempMin);
                maxTempValue.setText(tempMax);
                humidityValue.setText(humidity+"%");
                mainWeatherValue.setText(mainWeather);
                windSpeedValue.setText(windSpeed + " km/h");

                boolean isMorning = false;

                Date currentTime = Calendar.getInstance().getTime();

                Date morning = createDate(6);
                Date evening = createDate(18);


                if (currentTime.after(morning) && currentTime.before(evening))
                    isMorning = true;

                switch (mainWeather){
                    case "Thunderstorm":
                        weatherIcon.setImageResource(R.drawable.ic_thunderstorm);
                        break;
                    case "Drizzle":
                        weatherIcon.setImageResource(R.drawable.ic_drizzle);
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

            } catch (JSONException e) {
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

    private Date createDate(int hours){
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, hours);// for 6 hour
        calendar.set(Calendar.MINUTE, 0);// for 0 min
        calendar.set(Calendar.SECOND, 0);// for 0 sec
        return  calendar.getTime();
    }

    private void setEndOfTemp(){
        if(isDegrees){
            degreesValue = " °C";
        }
        else{
            degreesValue = " F";
        }
    }

}
