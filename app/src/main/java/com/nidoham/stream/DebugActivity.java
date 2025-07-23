package com.nidoham.stream;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        TextView debugInfo = findViewById(R.id.debug_info);

        // Get crash details from intent
        String crashDetails = getIntent().getStringExtra("crash_details");
        if (crashDetails != null) {
            debugInfo.setText("Crash Detected: " + crashDetails);
        } else {
            // Display deprecated feature message
            debugInfo.setText("This feature is deprecated. Please use the updated functionality.");
        }
    }
}