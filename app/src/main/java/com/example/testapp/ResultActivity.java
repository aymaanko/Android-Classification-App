package com.example.testapp;

import static com.example.testapp.MainActivity.LANGUAGE_KEY;
import static com.example.testapp.MainActivity.SETTINGS_PREF;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.util.List;
import java.util.Locale;

public class ResultActivity extends AppCompatActivity {

    private TextView resultTextView;
    private ImageView imageView;
    private Interpreter tflite;
    private List<String> labels;
    private static final String TAG = "ResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultTextView = findViewById(R.id.resultTextView);
        imageView = findViewById(R.id.imageView);

        try {
            tflite = new Interpreter(loadModelFile());
            labels = FileUtil.loadLabels(this, "labels.txt");
        } catch (Exception e) {
            Log.e(TAG, "Error loading model or labels", e);
            resultTextView.setText("Error loading model or labels");
            return;
        }

        byte[] byteArray = getIntent().getByteArrayExtra("image");
        if (byteArray != null) {
            Bitmap image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            imageView.setImageBitmap(image);

            Bitmap resizedImage = Bitmap.createScaledBitmap(image, 32, 32, true);
            String result = classifyImage(resizedImage);
            resultTextView.setText(result);
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


    private MappedByteBuffer loadModelFile() throws IOException {
        return FileUtil.loadMappedFile(this, "model_2_cifar10.tflite");
    }

    private String classifyImage(Bitmap bitmap) {
        TensorImage tensorImage = new TensorImage(DataType.UINT8);
        tensorImage.load(bitmap);

        TensorBuffer inputBuffer = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.UINT8);
        inputBuffer.loadBuffer(tensorImage.getBuffer());

        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, labels.size()}, DataType.FLOAT32);
        try {
            tflite.run(inputBuffer.getBuffer(), outputBuffer.getBuffer().rewind());
        } catch (Exception e) {
            Log.e(TAG, "Error during inference", e);
            return "Error during inference";
        }

        float[] scores = outputBuffer.getFloatArray();
        int maxIndex = getMaxScoreIndex(scores);
        return "Classification: " + labels.get(maxIndex);
    }

    private int getMaxScoreIndex(float[] scores) {
        int maxIndex = -1;
        float maxScore = -Float.MAX_VALUE;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
