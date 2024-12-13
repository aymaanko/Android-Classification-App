package com.example.testapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_UPLOAD = 1;
    private static final String TAG = "MainActivity";
    private Interpreter interpreter;
    private List<String> labelsList;
    private TextView resultTextView;
    private ImageView imageView;
    private Bitmap selectedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        Button uploadButton = findViewById(R.id.uploadButton);
        Button classifyButton = findViewById(R.id.classifyButton);
        resultTextView = findViewById(R.id.result_text);

        uploadButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_UPLOAD);
        });

        classifyButton.setOnClickListener(v -> {
            if (selectedImage != null) {
                classifyImage(selectedImage);
            } else {
                Toast.makeText(MainActivity.this, "No image selected", Toast.LENGTH_SHORT).show();
            }
        });

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
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_UPLOAD && data != null) {
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
        }
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd("model_2_cifar10.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private List<String> loadLabels() throws IOException {
        List<String> labels = new ArrayList<>();
        InputStream is = getAssets().open("labels.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    private void classifyImage(Bitmap bitmap) {
        try {
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 32, 32, false);
            float[][][][] input = new float[1][32][32][3];
            for (int x = 0; x < 32; x++) {
                for (int y = 0; y < 32; y++) {
                    int pixel = resizedBitmap.getPixel(x, y);
                    input[0][y][x][0] = (pixel >> 16 & 0xFF) / 255.0f; // Red
                    input[0][y][x][1] = (pixel >> 8 & 0xFF) / 255.0f;  // Green
                    input[0][y][x][2] = (pixel & 0xFF) / 255.0f;       // Blue
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
}
