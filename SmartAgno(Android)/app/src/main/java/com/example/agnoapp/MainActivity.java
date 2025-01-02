package com.example.agnoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import okhttp3.WebSocket;

// Ana aktivite sınıfı: Uygulamanın ana ekranını yönetir
// IMessageListener arayüzünü uygulayarak WebSocket mesajlarını dinler
public class MainActivity extends AppCompatActivity implements IMessageListener {
    WebSocketClient webSocketClient; // WebSocket istemcisi
    TextView tx;
    ProgressBar progressBarTemp; // Sıcaklık göstergesi
    ProgressBar progressBarHum;// Nem göstergesi
    ProgressBar progressBarSoil; // Toprak nemi göstergesi
    ProgressBar progressBarAir; // Hava kalitesi göstergesi
    TextView tvsampleTime;// Ölçüm aralığını gösteren metin
    EditText changTime;// Kullanıcıdan ölçüm aralığı girişi
    TextView tvAir;// Hava kalitesi metni
    TextView tvSoil;// Toprak nemi metni
    TextView tvHum;// Nem metni
    TextView tvTemp;// Sıcaklık metni
    String time="1";
    Button btnchng;// Ölçüm aralığını değiştirme butonu
    Button btnSensorAttempts;// Sensör deneme geçmişi butonu
    Button btnSettings;// Ayarlar ekranı butonu
    Button btnSensorData;// Sensör verisi görüntüleme butonu
    int intSampleTime=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            // Arayüz elemanlarını başlat
            progressBarTemp = findViewById(R.id.circularProgressBarTemp);
            progressBarTemp.setMax(100); // Sıcaklık için 100 dereceyi referans alıyoruz
            progressBarHum = findViewById(R.id.circularProgressBarHum);
            progressBarHum.setMax(100); // Nem için %100
            progressBarAir = findViewById(R.id.circularProgressBarAir);
            progressBarAir.setMax(7000); // Hava kalitesi için 500 ppm
            progressBarSoil = findViewById(R.id.circularProgressBarSoil);
            progressBarSoil.setMax(100); // Toprak nemi için %100
            btnchng=findViewById(R.id.btnChangeSample);
            btnSensorAttempts=findViewById(R.id.btnAttempts);
            btnSensorData=findViewById(R.id.btnSensorData);
            tvsampleTime=findViewById(R.id.textViewSample);
            changTime=findViewById(R.id.editTextSampleTime);
            tvAir=findViewById(R.id.textViewAir);
            tvTemp=findViewById(R.id.textViewTemp);
            tvSoil=findViewById(R.id.textViewSoil);
            tvHum=findViewById(R.id.textViewHum);
            btnSettings=findViewById(R.id.btnSettings);

            // WebSocket bağlantısını başlatıyoruz
            webSocketClient = new WebSocketClient();
            tvsampleTime.setText("Ölçüm Aralığı:"+time+"sn");
            webSocketClient.setMessageListener(this);
            webSocketClient.start("ws://3.73.144.4:3001");

            // Ayarlar ekranına geçiş
            btnSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(MainActivity.this,SettingsActivity.class);
                    startActivity(intent);
                }
            });

            // Sensör deneme geçmişi ekranına geçiş
            btnSensorAttempts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(MainActivity.this,SensorAttemptsActivity.class);
                    startActivity(intent);
                }
            });

            // Sensör verisi görüntüleme ekranına geçiş
            btnchng.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    time=changTime.getText().toString();
                    if (time.isEmpty()){
                        Toast.makeText(MainActivity.this,"lütfen boş değer girmeyin", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        intSampleTime=Integer.parseInt(time);
                        tvsampleTime.setText("Ölçüm Aralığı:"+time+"sn");
                        intSampleTime=intSampleTime*1000;
                        webSocketClient.sendMessage("sampleTime,"+intSampleTime);

                    }
                }
            });
            btnSensorData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(MainActivity.this,GetSensorInfoActivity.class);
                    startActivity(intent);
                }
            });
           return  insets;

        });
        }

        // Gelen mesajları parçalayarak işleyen kısım.
    @Override
    public void onMessageReceived(String message) {
        if (message.startsWith("Data,100")){
        runOnUiThread(() -> {
            // Gelen mesajı virgülle ayıralım
            String[] parts = message.split(",");
            // Her iki elementten (anahtar ve değer) oluşan çiftleri işliyoruz
            for (int i = 0; i < parts.length; i += 2) {
                String key = parts[i].trim();
                String value = parts[i + 1].trim();
                // Anahtara göre değerleri ilgili ProgressBar'a atıyoruz
                switch (key) {
                    case "Temp":
                        // Sıcaklık (temp) için gelen veriyi float olarak alıyoruz
                        float tempValue = Float.parseFloat(value);
                        // Sıcaklık 0-100 arası bir değeri gösterecek şekilde ProgressBar'ı güncelliyoruz

                        if(tempValue!=-1){
                            progressBarTemp.setProgress((int) tempValue);
                            tvTemp.setText("Sıcaklık:\n"+Float.toString(tempValue));
                        }
                        break;
                    case "Hum":
                        // Nem (hum) için gelen veriyi float olarak alıyoruz
                        float humValue = Float.parseFloat(value);
                        // Nem 0-100 arası bir değeri gösterecek şekilde ProgressBar'ı güncelliyoruz

                        if(humValue!=-1){
                            progressBarHum.setProgress((int) humValue);
                            tvHum.setText("Nem:\n%"+Float.toString(humValue));}
                        break;
                    case "Soil":
                        // Toprak nemi (soil) için gelen veriyi float olarak alıyoruz
                        float soilValue = Float.parseFloat(value);
                        // Toprak nemi 0-100 arası bir değeri gösterecek şekilde ProgressBar'ı güncelliyoruz
                        progressBarSoil.setProgress((int) soilValue);
                        tvSoil.setText("Toprak Nemi:\n%"+Float.toString(soilValue));
                        break;
                    case "Air":
                        // Hava kalitesi (air) için gelen veriyi float olarak alıyoruz
                        float airValue = Float.parseFloat(value);
                        // Hava kalitesi 0-6000 ppm arası bir değeri gösterecek şekilde ProgressBar'ı güncelliyoruz
                        progressBarAir.setProgress((int) airValue);
                        tvAir.setText("CO2:\n"+Float.toString(airValue)+" ppm");
                        break;
                    case "sampleTime":
                        intSampleTime=Integer.parseInt(value);
                        tvsampleTime.setText("Ölçüm Aralığı:"+intSampleTime/1000+"sn");

                    default:
                        // Bilinmeyen anahtarlar için işlem yapmıyoruz
                        break;
                }
            }
        });
    }}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // WebSocket bağlantısını kapat
        if(webSocketClient!=null){
            webSocketClient.stop();
        }
    }
}

