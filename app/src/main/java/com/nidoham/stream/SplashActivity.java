package com.nidoham.stream;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.nidoham.stream.databinding.ActivitySplashBinding;

public class SplashActivity extends AppCompatActivity {

    // ViewBinding দিয়ে splash layout এর ভিউ এক্সেস করার জন্য
    private ActivitySplashBinding binding;

    // স্প্ল্যাশ স্ক্রিন কত সময় থাকবে (মিলিসেকেন্ডে)
    private static final int SPLASH_TIME_MS = 2000; // এখানে ২০০০ মিলিসেকেন্ড = ২ সেকেন্ড

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // ViewBinding দিয়ে layout ইনফ্লেট করা হচ্ছে
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // স্প্ল্যাশ স্ক্রিন দেখানোর পর নির্দিষ্ট সময় অপেক্ষা করে পরবর্তী Activity-তে যাওয়ার জন্য Handler ব্যবহার করা হয়
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // এখানে MainActivity বা আপনার Home Screen এ যাওয়ার intent তৈরি করা হচ্ছে
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);

            // স্প্ল্যাশ স্ক্রিন Activity-টি ফিনিশ করে দিচ্ছি যাতে ব্যাক প্রেস করলে আর এখানে না ফিরে আসে
            finish();
        }, SPLASH_TIME_MS);
    }
}