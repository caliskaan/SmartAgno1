package com.example.agnoapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
/*
* Bu sınıf bir CustomAdapter sınıfı bu sınıf sayesinde kendi oluşturduğumu layoutlarda  verileri düzgün
* bir şekilde göstermekteyiz.(item_sensor_attempts)
*
*
* */
public class GetSensorInfoAdapter extends RecyclerView.Adapter<GetSensorInfoAdapter.SensorViewHolder> {
    String soilMoisture;
    String airTemp;
    String airHumidity;
    String co2;
    String date;
    int id;

    private ArrayList<Crops> crops;

    public GetSensorInfoAdapter(ArrayList<Crops> cropsList) {
        this.crops = cropsList;
    }

    @NonNull
    @Override
    public SensorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sensor_attempts, parent, false);
        return new SensorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SensorViewHolder holder, int position) {
        Crops crop = crops.get(position);
        soilMoisture=crop.soilMoisture;
        airHumidity=crop.airHumidity;
        airTemp=crop.airTemp;
        id=crop.eventId;
        co2=crop.co2;
        date=crop.date;
        holder.tvSensorDate.setText("ID:"+id+"                             Tarih:"+date);
        String firstData="Sıcaklık:"+crop.airTemp+"C°              Nem:"+airHumidity;
        String secondData="Toprak Nemi:"+soilMoisture+"        Co2 Derişimi(ppm):"+co2;
        holder.tvFirstRow.setText(firstData);
        holder.tvSecondRow.setText(secondData);
    }

    @Override
    public int getItemCount() {
        return crops.size();
    }

    public static class SensorViewHolder extends RecyclerView.ViewHolder {
        TextView tvSensorDate;
        TextView tvFirstRow;
        TextView tvSecondRow;
        public SensorViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSensorDate=itemView.findViewById(R.id.tvSensorDate);
            tvFirstRow=itemView.findViewById(R.id.tvFirstRow);
            tvSecondRow=itemView.findViewById(R.id.tvSecondRow);
        }
    }
}
