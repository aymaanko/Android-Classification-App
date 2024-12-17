package com.example.testapp;

import static com.example.testapp.MainActivity.LANGUAGE_KEY;
import static com.example.testapp.MainActivity.SETTINGS_PREF;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Locale;

public class EducationalActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educational);

        // Combine everything into a single TextView
        TextView educationalCombined = findViewById(R.id.educational_combined);

        // Set the combined text to the TextView
        educationalCombined.setText(getString(R.string.educational_combined));
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences(SETTINGS_PREF, MODE_PRIVATE);
        String language = prefs.getString(LANGUAGE_KEY, "en");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = newBase.getResources().getConfiguration();
        config.setLocale(locale);

        super.attachBaseContext(newBase.createConfigurationContext(config));
    }
}
