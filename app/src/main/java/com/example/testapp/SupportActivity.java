package com.example.testapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class SupportActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        TextView supportTextView = findViewById(R.id.supportText);
        supportTextView.setText("Support FAQs and Contact Options Here");
    }
}
