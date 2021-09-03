package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText user_field;
    private Button main_button;
    private TextView result_info;
    private TextView result_info2;
    private TextView disclaimer;
    private Animation anim;
    private SharedPreferences sPref;

    final String SAVED_TEXT = "saved_text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user_field = findViewById(R.id.user_field);
        main_button = findViewById(R.id.main_button);
        result_info = findViewById(R.id.result_info);
        result_info2 = findViewById(R.id.result_info2);
        disclaimer = findViewById(R.id.disclaimer);
        anim = AnimationUtils.loadAnimation(this, R.anim.alpha_trans);
        loadText();
        if (user_field.getText().toString().trim().equals(""))
            disclaimer.setText(R.string.disclaimer);

        main_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //скрытие клавиатуры
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(user_field.getWindowToken(), 0);

                if (user_field.getText().toString().trim().equals("")) {
                    Toast.makeText(MainActivity.this, R.string.no_user_input, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "btn.city_null");
                }
                else {
                    String city = user_field.getText().toString().replaceAll(" ","");
                    String key = "91e0c7e9abcc7fa923016d98442642b8";
                    String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + key + "&units=metric&lang=ru";
                    Log.d(TAG, "btn.city_full");
                    saveText();

                    new GetURLData().execute(url);
                }
            }
        });
    }

    private class GetURLData extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            result_info.setText("Ожидайте...");
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();

                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray = (JSONArray) jsonObject.get("weather");
                    JSONObject jsonObjectWeather = (JSONObject) jsonArray.get(0);
                    result_info.setText("" + jsonObjectWeather.get("description"));
                    result_info.startAnimation(anim);
                    result_info2.setText("Температура: " + jsonObject.getJSONObject("main").getInt("temp") +
                            "\n Ощущается, как " + jsonObject.getJSONObject("main").getInt("feels_like"));
                    Log.d(TAG, "result.ok");
                    result_info2.startAnimation(anim);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                result_info.setText("Такого города не существует(");
                result_info.startAnimation(anim);
                result_info2.setText("");
                Log.d(TAG, "result.city_bad");
            }

        }

    }

    void saveText() {
        sPref = getSharedPreferences("SaveData", MODE_PRIVATE);
        if (sPref.getString(SAVED_TEXT, "").equals(user_field.getText().toString().replaceAll(" ",""))) {
            Log.d(TAG, "btn.city_equals_saved");
        } else {
            SharedPreferences.Editor ed = sPref.edit();
            ed.putString(SAVED_TEXT, user_field.getText().toString().replaceAll(" ",""));
            ed.commit();
            Log.d(TAG, "btn.city_save");
        }
    }

    void loadText() {
        sPref = getSharedPreferences("SaveData", MODE_PRIVATE);
        String savedText = sPref.getString(SAVED_TEXT, "");
        user_field.setText(savedText);
        Log.d(TAG, "past_city");
    }
}
