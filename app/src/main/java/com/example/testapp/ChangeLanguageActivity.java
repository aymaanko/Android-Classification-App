package com.example.testapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import static com.example.testapp.MainActivity.LANGUAGE_KEY;
import static com.example.testapp.MainActivity.SETTINGS_PREF;

public class ChangeLanguageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_language);

        Button englishButton = findViewById(R.id.englishButton);
        Button frenchButton = findViewById(R.id.frenchButton);

        // Set up English button listener
        englishButton.setOnClickListener(v -> {
            changeLanguage("en");
        });

        // Set up French button listener
        frenchButton.setOnClickListener(v -> {
            changeLanguage("fr");
        });
    }

    private void changeLanguage(String languageCode) {
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LANGUAGE_KEY, languageCode);
        editor.apply();

        // Restart the app to apply the language change
        Intent intent = new Intent(ChangeLanguageActivity.this, MainActivity.class); // Or your main activity
        startActivity(intent);
        finish();
    }
}
