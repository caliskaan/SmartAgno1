package com.example.agnoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

// Bu sınıf, bir liste verisini RecyclerView içinde görüntülemek için kullanılır.
public class SensorAttemptsAdapter extends RecyclerView.Adapter<SensorAttemptsAdapter.SensorViewHolder> {
    // Adapter içinde kullanılacak değişkenler.
    String sensorEvent;
    String date;
    int id;

    // Crops sınıfını tutan bir ArrayList.
    private ArrayList<Crops> crops;

    // Constructor: Adapter oluşturulurken veri listesini alır.
    public SensorAttemptsAdapter(ArrayList<Crops> cropsList) {
        this.crops = cropsList; // crops değişkeni, parametre olarak gelen listeye atanır.
    }

    @NonNull
    @Override
    // RecyclerView her bir öğe için görünümü oluşturur.
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_list_sensor_attempts layout dosyasını görünüm olarak bağlar.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_sensor_attempts, parent, false);
        return new SensorViewHolder(view);// SensorViewHolder nesnesi döndürülür.
    }

    @Override
    // Veriler, belirli bir pozisyondaki görünüm bileşenlerine atanır.
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        // crops listesinden pozisyona karşılık gelen crop (ürün) alınır.
        Crops crop = crops.get(position);
        sensorEvent=crop.sensorEvent;// crop nesnesindeki sensorEvent değeri alınır.
        date=crop.date;// crop nesnesindeki tarih bilgisi alınır.

        // TextView bileşenlerine veriler atanır.
        holder.tvEventName.setText(sensorEvent);
        holder.tvEventDate.setText(date);
    }

    @Override
    // RecyclerView içindeki öğe sayısını döndürür.
    public int getItemCount() {
        return crops.size();
    }

    // ViewHolder sınıfı: RecyclerView öğelerini tanımlar ve bağlar.
    public static class SensorViewHolder extends RecyclerView.ViewHolder {
        // TextView bileşenlerini tanımlıyoruz.
            TextView tvEventDate;
            TextView tvEventName;

        public SensorViewHolder(@NonNull View itemView) {
            super(itemView); // itemView, her bir öğeyi temsil eder.
            // XML'deki bileşenler Java koduyla bağlanır.
            tvEventDate=itemView.findViewById(R.id.tvEventDate);
            tvEventName=itemView.findViewById(R.id.tvEventName);
        }
    }
}
