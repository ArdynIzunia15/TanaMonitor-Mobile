package com.example.tanamonitor;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tanamonitor.History;
import com.example.tanamonitor.R;
import com.google.android.material.card.MaterialCardView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {
    ArrayList<History> arrayListHistory;

    public HistoryRecyclerViewAdapter(ArrayList<History> arrayListHistory){
        this.arrayListHistory = new ArrayList<>(arrayListHistory);
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        // Ambil data dari setiap index di arraylist
        final History history = arrayListHistory.get(position);

        // Set tiap elemen
        holder.txtActionType.setText(history.getActionType());
        holder.txtDate.setText(String.valueOf(history.getDateTime().getDayOfMonth()) + " " + String.valueOf(history.getDateTime().getMonth()) + " " + String.valueOf(history.getDateTime().getYear()));
        holder.txtTime.setText(String.valueOf(history.getDateTime().getHour()) + ":" + String.valueOf(history.getDateTime().getMinute()));
        // Count Duration
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime pastTime = LocalDateTime.of(history.getDateTime().getYear(), history.getDateTime().getMonth(), history.getDateTime().getDayOfMonth(), history.getDateTime().getHour(), history.getDateTime().getMinute());
        Duration duration = Duration.between(pastTime, currentTime);
        long minutes = duration.toMinutes();
        String durationStr;
        if (minutes < 60) {
            durationStr = minutes + " minutes ago";
        } else {
            long hours = duration.toHours();
            if (hours < 24) {
                durationStr = hours + " hours ago";
            } else {
                long days = duration.toDays();
                durationStr = days + " days ago";
            }
        }
        holder.txtDuration.setText(durationStr);

        // Set icon
        if(history.getActionType().equals("AUTOMATIC")){
            holder.iconHistory.setImageResource(R.drawable.baseline_circle_blue_24);
        }
        else{
            holder.iconHistory.setImageResource(R.drawable.baseline_circle_orange_24);
        }
    }

    @Override
    public int getItemCount() {
        return arrayListHistory.size();
    }

    public void updateArray(ArrayList<History> tempArrHistory){
        arrayListHistory = tempArrHistory;
    }
    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        // Deklarasi layout history
        TextView txtActionType, txtDate, txtTime, txtDuration;
        ImageView iconHistory;
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            txtActionType = itemView.findViewById(R.id.txtActionType);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtTime = itemView.findViewById(R.id.txtTime);
            txtDuration = itemView.findViewById(R.id.txtDuration);
            iconHistory = itemView.findViewById(R.id.iconHistory);
        }
    }
}
