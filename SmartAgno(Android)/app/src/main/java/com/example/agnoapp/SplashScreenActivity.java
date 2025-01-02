package com.example.agnoapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        // Splash ekranını belirli bir süre sonra ana aktiviteye geçirmek için bir Handler kullanılıyor
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // MainActivity'ye geçiş yapılıyor
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                // Splash ekranı kapatılıyor
                finish();
            }
        }, 2000);// 2000 ms (2 saniye) bekleme süresi
    }
}
