package com.example.agnoapp;

import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/*Bu sınıf veri tabanı işlemlerini yapmaktadır.
Androit de ağ işlemleri ana threadde yapıldığı zaman ana threadı kilitler ve uygulama hata verir
bunu çözmek için veri tabanı işlemlerini ExecutorService sınıfı yardımı ile çözdük
bu sayede veri tabanı işlemleri arka planda ana threadden ayrı bir şekide yapılmakta.
* Ayrıca bu işlemleri ICallBack interfacei soyutladık.
*
* */
public  class DatabaseHandler implements ICallBack {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    public static void getData(String apiUrl, ICallBack<List<Crops>> callback) {
        //veri tabanından veri getirme işlemi
        executorService.submit(() -> {
            List<Crops> cropsList = new ArrayList<>();
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        content.append(line);
                    }
                    // JSON yanıtını JSONObject olarak al
                    JSONObject responseObject = new JSONObject(content.toString());

                    // "daily_data" anahtarındaki JSONArray'i al
                    JSONArray jsonArray = responseObject.getJSONArray("daily_data");

                    // JSON array üzerinden döngü ile verileri işle
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject cropsObject = jsonArray.getJSONObject(i);
                        int cropId = cropsObject.getInt("id");
                        String date = cropsObject.getString("register_date");
                        String soilMoisture = cropsObject.getString("soil_moisture");
                        String airTemp = cropsObject.getString("air_temp");
                        String airQuality = cropsObject.getString("co2");
                        String airHumidity = cropsObject.getString("air_humidity");
                        //gün sonu verilerini ilgili tablodan çekiyor ve nesne oluşturuyor.
                        Crops crops = new Crops(cropId, soilMoisture, airTemp, airHumidity, airQuality, date);
                        //birden fazla veri olduğu için herbir nesneyi bir liste içerisine atıyor
                        cropsList.add(crops);
                    }
                }
                callback.onSuccess(cropsList);
            } catch (Exception e) {
                //eğer istekte hata olursa bu blok çalışıyor.
                e.printStackTrace();
                callback.onFailure(e);
            }
        });
    }

    public static void getFailure(String apiUrl, ICallBack<List<Crops>> callback) {
        //sensör olaylarını getirdiğimiz metod yine üstteki metodla benzer çalışıyor
        executorService.submit(() -> {
            List<Crops> cropsList = new ArrayList<>();
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        content.append(line);
                    }
                    JSONArray jsonArray = new JSONArray(content.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject cropsObject = jsonArray.getJSONObject(i);
                        String event=cropsObject.getString("fail_type");
                        String date=cropsObject.getString("fail_date");
                        Crops crops = new Crops(event,date);//constructor overloading yaptık.
                        cropsList.add(crops);
                    }
                }
                callback.onSuccess(cropsList);
            } catch (Exception e) {
                e.printStackTrace();
                callback.onFailure(e);
            }
        });
    }



    @Override
    public void onSuccess(Object result) {
    }

    @Override
    public void onFailure(Exception e) {

    }
}
