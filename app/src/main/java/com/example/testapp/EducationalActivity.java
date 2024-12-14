package com.example.testapp;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class EducationalActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_educational);

        TextView educationalTextView = findViewById(R.id.educationalText);
        educationalTextView.setText("Educational Resources Here");
    }
}
