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
/*Bu aktivite günlük sensör verilerinin ortalamasını ekrana getirmekte.
Bunu bir recyleview CustomAdapter sınıfı yardımı ile yapmaktadır(GetSensorInfoAdapter)
Bu sayede sql de tablo olarak tutulan veri adapter sınıfı kullanılarak recyleview de androide
uygun halde getirilir.
*
* */
public class GetSensorInfoActivity extends AppCompatActivity implements IDataManagement{
    private String apiKey="https://6tjjieh482.execute-api.eu-central-1.amazonaws.com/prod/get-daily-data";
    private GetSensorInfoAdapter adapter;//kullanılacak olan adapter
    private ArrayList<Crops> crops=new ArrayList<>();//mahsül verilerini tutacka olan liste
    Button btnMain;//ana ekran butonu
    Button btnList;//liste butonu
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get_sensor_info);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            RecyclerView recyclerView=findViewById(R.id.recyclerViewSensorInfo);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            btnMain=findViewById(R.id.btnMainInfo);
            btnList=findViewById(R.id.btnAttemptsInfo);
            adapter = new GetSensorInfoAdapter(crops);
            recyclerView.setAdapter(adapter);//adapterı bağlama
            LoadData();//Veri tabanındaki verileri yüklemek için metod
            btnMain.setOnClickListener(new View.OnClickListener()
            //ana ekran butonuna tıklandığında ana ekrana yönlendiriyor
            {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(GetSensorInfoActivity.this,MainActivity.class);
                    //intenet ile pencere değiştirme
                    startActivity(intent);
                }
            });
            btnList.setOnClickListener(new View.OnClickListener()
            //liste butonuna tıklama olayı
            {
                @Override
                public void onClick(View view) {
                    //sensör olaylarının olduğu aktiviteye geçi
                    Intent intent=new Intent(GetSensorInfoActivity.this,SensorAttemptsActivity.class);
                    startActivity(intent);
                }
            });
            return insets;
        });
    }

    @Override
    public void LoadData() {
        //loaddata fonksyonu veri tabaından gelen liste verisini adaptere göndeiriyor
        DatabaseHandler.getData(apiKey, new ICallBack<List<Crops>>() {
            //api isteği
            @Override
            public void onSuccess(List<Crops> result) {
                crops.clear();
                crops.addAll(result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        adapter.notifyDataSetChanged();
                        //adaptere veri gönderme
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {

            }
        });
    }
    }