package com.example.testapp;

import static com.example.testapp.MainActivity.LANGUAGE_KEY;
import static com.example.testapp.MainActivity.SETTINGS_PREF;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class SupportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        // Set title text
        TextView supportTitle = findViewById(R.id.support_title);
        supportTitle.setText(getString(R.string.support_title));

        // Set contact information
        TextView contactText = findViewById(R.id.contactText);
        contactText.setText(getString(R.string.contactText));

        TextView contactInfo = findViewById(R.id.informations);
        contactInfo.setText("Email: akorchi07@gmail.com\nPhone: +212 773069527");
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
    public void onInformationClicked(View view) {
        // Check which part of the TextView was clicked, for email or phone
        String text = ((TextView) view).getText().toString();

        if (text.contains("akorchi07@gmail.com")) {
            // Launch email intent if email is clicked
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:akorchi07@gmail.com"));
            startActivity(Intent.createChooser(emailIntent, "Send email"));
        } else if (text.contains("+212 773069527")) {
            // Launch phone dial intent if phone number is clicked
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+212773069527"));
            startActivity(phoneIntent);
        }
    }

}
