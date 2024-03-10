package com.example.insectdetection;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.insectdetection.Login;
import com.example.insectdetection.R;

public class SplashScreen extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Load your main HTML file with embedded GSAP animation
        webView.loadUrl("file:///android_asset/gsap.html");

        // Delayed transition to the login activity after 2 seconds (adjust as needed)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start LoginActivity
                Intent intent = new Intent(SplashScreen.this, Login.class);
                startActivity(intent);
                finish(); // Finish the splash screen activity
            }
        }, 2000); // 2000 milliseconds = 2 seconds
    }
}
