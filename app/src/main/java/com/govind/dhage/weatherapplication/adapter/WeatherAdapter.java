package com.govind.dhage.weatherapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.govind.dhage.weatherapplication.R;
import com.govind.dhage.weatherapplication.model.WeatherModel;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.MyViewHolder> {
    Context context;
    private ArrayList<WeatherModel> weatherModelArrayList;



    public WeatherAdapter(Context context,ArrayList<WeatherModel> weatherModelArrayList) {
        this.context = context;
        this.weatherModelArrayList = weatherModelArrayList;

    }

    @Override
    public WeatherAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_background_layout, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WeatherAdapter.MyViewHolder holder, int position) {
        WeatherModel weatherModel = weatherModelArrayList.get(position);
        Picasso.get().load("http:".concat(weatherModel.getIcon())).into(holder.imgCondtion);
        holder.tvWindSpeed.setText(weatherModel.getWindSpeed() + "Km/h");
        holder.tvTemperature.setText(weatherModel.getTemperature() + "°c");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        SimpleDateFormat sdf1 = new SimpleDateFormat("hh:mm");
        try {
            Date d = sdf.parse(weatherModel.getTime());
            holder.tvTime.setText(sdf1.format(d));
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return weatherModelArrayList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemperature, tvWindSpeed;
        ImageView imgCondtion;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvTemperature = itemView.findViewById(R.id.tvTemperature);
            tvWindSpeed = itemView.findViewById(R.id.tvWindSpeed);
            imgCondtion = itemView.findViewById(R.id.ivCondtion);
        }
    }
}
