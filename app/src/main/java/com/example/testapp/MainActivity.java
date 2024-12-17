package com.example.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import database.UserDao;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_IMAGE_UPLOAD = 1;
    private static final int REQUEST_LANGUAGE_CHANGE = 2;  // For language change activity
    private static final String TAG = "MainActivity";
    static final String SETTINGS_PREF = "settings";  // Centralized shared preferences name
    static final String LANGUAGE_KEY = "language";  // Key for storing language

    private DrawerLayout drawerLayout;
    private Interpreter interpreter;
    private List<String> labelsList;
    private TextView resultTextView;
    private ImageView imageView;
    private Bitmap selectedImage;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load locale before setting the content view
        loadLocale();

        setContentView(R.layout.activity_main);

        // Initialize UserDao
        userDao = new UserDao(this);

        // Set up the Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Set up DrawerLayout and NavigationView
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Set up navigation header
        updateNavigationHeader(navigationView);

        // Initialize UI components
        imageView = findViewById(R.id.imageView);
        resultTextView = findViewById(R.id.result_text);
        Button uploadButton = findViewById(R.id.uploadButton);
        Button classifyButton = findViewById(R.id.classifyButton);

        resultTextView.setText(getString(R.string.result_text));
        uploadButton.setText(getString(R.string.upload_image));
        classifyButton.setText(getString(R.string.classify_image));

        // Upload Image
        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_UPLOAD);
        });

        // Classify Image
        classifyButton.setOnClickListener(v -> {
            if (selectedImage != null) {
                classifyImage(selectedImage);
            } else {
                Toast.makeText(MainActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

        // Load TensorFlow Lite model and labels
        try {
            interpreter = new Interpreter(loadModelFile());
            labelsList = loadLabels();
            Log.d(TAG, "Interpreter and labels loaded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing TensorFlow Lite interpreter", e);
            Toast.makeText(this, "Error initializing TensorFlow Lite", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_UPLOAD && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try (InputStream imageStream = getContentResolver().openInputStream(selectedImageUri)) {
                        selectedImage = BitmapFactory.decodeStream(imageStream);
                        imageView.setImageBitmap(selectedImage);
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to load image", e);
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (requestCode == REQUEST_LANGUAGE_CHANGE && data != null) {
                String newLanguage = data.getStringExtra(LANGUAGE_KEY);
                if (newLanguage != null) {
                    setLocale(newLanguage, true); // Pass "true" to restart the activity
                }
            }
        }
    }

    private void updateNavigationHeader(NavigationView navigationView) {
        View headerView = navigationView.getHeaderView(0);
        TextView welcomeTextView = headerView.findViewById(R.id.header_title);
        TextView headerSubtitle = headerView.findViewById(R.id.header_subtitle);

        welcomeTextView.setText(getString(R.string.header_title));

        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String userName = sharedPreferences.getString("username", "Guest");

        if (userName != null && !userName.isEmpty()) {
            headerSubtitle.setText(userName);  // Display the username if available
        } else {
            headerSubtitle.setText("Guest");
        }

    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("model_2_cifar10.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    private List<String> loadLabels() throws IOException {
        List<String> labels = new ArrayList<>();
        try (InputStream is = getAssets().open("labels.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line);
            }
        }
        return labels;
    }

    private void classifyImage(Bitmap bitmap) {
        try {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 32, 32, false);
            float[][][][] input = new float[1][32][32][3];

            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    int pixel = resizedBitmap.getPixel(x, y);
                    input[0][y][x][0] = (pixel >> 16 & 0xFF) / 255.0f;
                    input[0][y][x][1] = (pixel >> 8 & 0xFF) / 255.0f;
                    input[0][y][x][2] = (pixel & 0xFF) / 255.0f;
                }
            }

            float[][] output = new float[1][labelsList.size()];
            interpreter.run(input, output);

            float[] probabilities = output[0];
            int maxIndex = 0;
            for (int i = 1; i < probabilities.length; i++) {
                if (probabilities[i] > probabilities[maxIndex]) {
                    maxIndex = i;
                }
            }

            String predictedClass = labelsList.get(maxIndex);
            float confidence = probabilities[maxIndex] * 100;
            resultTextView.setText(String.format("Predicted: %s\nConfidence: %.2f%%", predictedClass, confidence));
        } catch (Exception e) {
            Log.e(TAG, "Error during inference", e);
            resultTextView.setText("Error during inference: " + e.getMessage());
        }
    }

    private void loadLocale() {
        SharedPreferences sharedPreferences = getSharedPreferences(SETTINGS_PREF, MODE_PRIVATE);
        String language = sharedPreferences.getString(LANGUAGE_KEY, "en");
        setLocale(language, false); // Load without restarting
    }

    private void setLocale(String lang, boolean restart) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = new Configuration();
        config.setLocale(locale);

        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = getSharedPreferences(SETTINGS_PREF, MODE_PRIVATE).edit();
        editor.putString(LANGUAGE_KEY, lang);
        editor.apply();

        if (restart) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_change_language) {
            startActivityForResult(new Intent(this, ChangeLanguageActivity.class), REQUEST_LANGUAGE_CHANGE);
        } else if (id == R.id.nav_educational) {
            startActivity(new Intent(this, EducationalActivity.class));
        } else if (id == R.id.nav_support) {
            // Navigate to SupportActivity instead of email
            startActivity(new Intent(this, SupportActivity.class));
        } else if (id == R.id.nav_logout) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    public void onInformationClicked(View view) {
        String email = "akorchi07@gmail.com";
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Request");

        try {
            startActivity(Intent.createChooser(emailIntent, "Contact Support"));
        } catch (Exception e) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs = getSharedPreferences(SETTINGS_PREF, MODE_PRIVATE);
        String language = prefs.getString(LANGUAGE_KEY, "en");
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);

        // Update resources for the new locale
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}
