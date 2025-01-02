package com.example.agnoapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {
    // Eşik değerler için başlangıç değerleri
    int intSoilThresholdMin = 40;
    int intSoilThresholdMax = 70;
    int intAirThresholdMin = 100;
    int intAirThresholdMax = 200;
    float floatTempThresholdMin = 24.00F;
    float floatTempThresholdMax = 29.00F;
    TextView tvInfo; // Eşik değer bilgilerini gösteren TextView
    String text;// Bilgi metni için String değişkeni
    WebSocketClient webSocketClient;// WebSocket istemcisi
    SharedPreferences sharedPreferences;// Eşik değerlerin kalıcı olarak saklanması için SharedPreferences

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        sharedPreferences = getSharedPreferences("ThresholdValues", MODE_PRIVATE);// SharedPreferences'i başlat

        // Kaydedilmiş eşik değerleri yükle
        loadThresholdValues();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            // Arayüz elemanlarını tanımla
            tvInfo = findViewById(R.id.tvInfo);
            EditText minTemp = findViewById(R.id.edtMinTemp);
            EditText maxTemp = findViewById(R.id.edtMaxTemp);
            EditText minAir = findViewById(R.id.edtMinAir);
            EditText maxAir = findViewById(R.id.edtMaxAir);
            EditText minSoil = findViewById(R.id.edtMinSoil);
            EditText maxSoil = findViewById(R.id.edtMaxSoil);
            Button btnChange = findViewById(R.id.btnChangeValue);

            // Mevcut eşik değerleri güncelle ve ekranda göster
            updateInfoText();
            // WebSocket bağlantısını başlat
            webSocketClient = new WebSocketClient();
            webSocketClient.start("ws://3.73.144.4:3001");

            // "Değerleri Değiştir" butonuna tıklama işlemi
            btnChange.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Kullanıcıdan alınan değerleri metin kutularından oku
                    String soilThresholdMin = minSoil.getText().toString();
                    String soilThresholdMax = maxSoil.getText().toString();
                    String airThresholdMin = minAir.getText().toString();
                    String airThresholdMax = maxAir.getText().toString();
                    String tempThresholdMin = minTemp.getText().toString();
                    String tempThresholdMax = maxTemp.getText().toString();

                    // Boş değer kontrolü
                    if (soilThresholdMin.isEmpty() || soilThresholdMax.isEmpty() || airThresholdMin.isEmpty() || airThresholdMax.isEmpty()
                            || tempThresholdMin.isEmpty() || tempThresholdMax.isEmpty()) {
                        Toast.makeText(SettingsActivity.this, "Lütfen boş değer girmeyin", Toast.LENGTH_SHORT).show();
                    } else {
                        // Yeni değerleri güncelle
                        intSoilThresholdMin = Integer.parseInt(soilThresholdMin);
                        intSoilThresholdMax = Integer.parseInt(soilThresholdMax);
                        intAirThresholdMax = Integer.parseInt(airThresholdMax);
                        intAirThresholdMin = Integer.parseInt(airThresholdMin);
                        floatTempThresholdMax = Float.parseFloat(tempThresholdMax);
                        floatTempThresholdMin = Float.parseFloat(tempThresholdMin);

                        // Yeni değerleri kaydet
                        saveThresholdValues();
                        // Ekrandaki bilgi metnini güncelle
                        updateInfoText();

                        // WebSocket ile yeni değerleri gönder
                        String data = "soilMinThreshold," + soilThresholdMin + ",soilMaxThreshold," + soilThresholdMax +
                                ",airMinThreshold," + airThresholdMin + ",airMaxThreshold," + airThresholdMax +
                                ",tempThresholdMax," + tempThresholdMax + ",tempThresholdMin," + tempThresholdMin;
                        webSocketClient.sendMessage(data);
                    }
                }
            });
            return insets;
        });
    }

    // Mevcut eşik değerlerini ekranda günceller
    private void updateInfoText() {
        text = "Şu anki Eşik Değerler:\n" +
                "Minimum Toprak Nemi: %" + intSoilThresholdMin + "\nMaksimum Toprak Nemi: %" + intSoilThresholdMax +
                "\nMinimum CO2 Derişimi: " + intAirThresholdMin + "ppm\nMaksimum CO2 Derişimi: " + intAirThresholdMax + "ppm" +
                "\nMinimum Sıcaklık: " + floatTempThresholdMin + "C°\nMaksimum Sıcaklık: " + floatTempThresholdMax + "C°";
        tvInfo.setText(text);
    }

    // Eşik değerleri SharedPreferences kullanarak kaydeder
    private void saveThresholdValues() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("SoilMin", intSoilThresholdMin);
        editor.putInt("SoilMax", intSoilThresholdMax);
        editor.putInt("AirMin", intAirThresholdMin);
        editor.putInt("AirMax", intAirThresholdMax);
        editor.putFloat("TempMin", floatTempThresholdMin);
        editor.putFloat("TempMax", floatTempThresholdMax);
        editor.apply(); // Değerleri kaydeder
    }

    // Kaydedilmiş eşik değerleri yükler
    private void loadThresholdValues() {
        intSoilThresholdMin = sharedPreferences.getInt("SoilMin", 40);
        intSoilThresholdMax = sharedPreferences.getInt("SoilMax", 70);
        intAirThresholdMin = sharedPreferences.getInt("AirMin", 100);
        intAirThresholdMax = sharedPreferences.getInt("AirMax", 200);
        floatTempThresholdMin = sharedPreferences.getFloat("TempMin", 24.00F);
        floatTempThresholdMax = sharedPreferences.getFloat("TempMax", 29.00F);
    }
}
