package com.example.agnoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// SensorAttemptsActivity sınıfı, sensör girişimlerini listeleyen bir etkinliktir.
// IDataManagement arayüzünü uygular ve verileri yükler.
public class SensorAttemptsActivity extends AppCompatActivity implements IDataManagement {
    // API anahtarını tanımlıyoruz. Bu anahtar, veri alımı için kullanılır.
    private String apiKey="https://vadjoveek4.execute-api.eu-central-1.amazonaws.com/prod/get-failure";
    // RecyclerView için kullanılan adaptör ve veri listesi.
    private SensorAttemptsAdapter adapter;
    private ArrayList<Crops> crops=new ArrayList<>();
    // Butonlar tanımlandı.
    Button btnMain;
    Button btnList;
    Button btnLoad;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Edge-to-Edge tasarımını etkinleştiriyoruz.
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sensor_attempts);
        // Sistem çubukları için kenar boşluklarını ayarlıyoruz.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            // Layout üzerindeki butonları ve RecyclerView'ı bağlıyoruz.
            btnMain=findViewById(R.id.btnMain);
            btnLoad=findViewById(R.id.btnAttempts1);
            btnList=findViewById(R.id.btnList);
            RecyclerView recyclerView=findViewById(R.id.recyclerViewSensor);

            // RecyclerView için layout manager ve adaptör ayarlarını yapıyoruz.
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new SensorAttemptsAdapter(crops);
            recyclerView.setAdapter(adapter);

            // İlk veri yüklemesi.
            LoadData();
            // Veri yükleme butonuna tıklanıldığında yeniden yükleme işlemi yapılır.
            btnLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LoadData();
                }
            });
            // Ana sayfaya yönlendiren butonun işlevi.
            btnMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(SensorAttemptsActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            });
            // Sensör bilgilerini listeleyen başka bir aktiviteye yönlendirme.
            btnList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(SensorAttemptsActivity.this,GetSensorInfoActivity.class);
                    startActivity(intent);
                }
            });
            return insets;
        });
    }

    @Override
    // Verileri yükleyen metod.
    public void LoadData() {
        // Veriler API'den alınır ve geri çağırma mekanizmasıyla işlenir.
        DatabaseHandler.getFailure(apiKey, new ICallBack<List<Crops>>() {
            @Override
            public void onSuccess(List<Crops> result) {
                // Mevcut liste temizlenir ve gelen veriler eklenir.
                crops.clear();
                crops.addAll(result);
                // Kullanıcı arayüzünü güncellemek için ana iş parçacığına geçilir.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        adapter.notifyDataSetChanged();// RecyclerView adaptörünü günceller.
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                // API çağrısı başarısız olduğunda yapılacaklar.
            }
        });
    }
}