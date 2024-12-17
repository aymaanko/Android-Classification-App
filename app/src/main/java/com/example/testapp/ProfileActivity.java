package com.example.testapp;

import static com.example.testapp.MainActivity.LANGUAGE_KEY;
import static com.example.testapp.MainActivity.SETTINGS_PREF;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import database.UserDao;

public class ProfileActivity extends AppCompatActivity {
    private EditText nameEditText, passwordEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Apply the current language setting
        applyCurrentLocale();

        // Initialize EditText and Button only once
        nameEditText = findViewById(R.id.edit_name);
        passwordEditText = findViewById(R.id.edit_password);
        saveButton = findViewById(R.id.saveButton);

        // Initialize UserDao
        UserDao userDao = new UserDao(this);

        // Get user ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = sharedPreferences.getInt("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish(); // Close activity if no user ID is found
            return;
        }

        // Fetch the user's name from the database
        String username = userDao.getUserName(userId);
        if (username != null) {
            nameEditText.setText(username); // Set username in the EditText
        } else {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        }

        saveButton.setOnClickListener(v -> {
            // Get updated user inputs
            String updatedName = nameEditText.getText().toString();
            String updatedPassword = passwordEditText.getText().toString();

            // Update the username
            boolean nameUpdated = userDao.updateUserName(userId, updatedName);

            // Update the password
            boolean passwordUpdated = userDao.updateUserPassword(userId, updatedPassword);

            if (nameUpdated && passwordUpdated) {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyCurrentLocale() {
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PREF, MODE_PRIVATE);
        String language = prefs.getString(LANGUAGE_KEY, "en");
        setLocale(language);
    }
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale); // Set the locale to the desired language
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
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
