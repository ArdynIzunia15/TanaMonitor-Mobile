package com.example.tanamonitor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class MonitoringActivity extends AppCompatActivity {
    RecyclerView recyclerHistory;
    ArrayList<History> arrHistory = new ArrayList<>();
    HistoryRecyclerViewAdapter adapter;
    TextView txtMoistureValue;
    Slider sliderThreshold;
    MaterialButton btnManualOn, btnApplyThreshold, btnCancelApplyThreshold;
    Integer sliderValue;
    Boolean isOnline;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private void writeHistory(History history){
        String tempPath = "history/" + String.valueOf(history.getActionType() + "_" + String.valueOf(history.getDateTime()));
        FirebaseDatabase tempDatabase = FirebaseDatabase.getInstance();
        DatabaseReference tempReference = tempDatabase.getReference(tempPath);

        tempReference.setValue(history);
    }

    private long countDuration(History history){
        LocalDateTime currentTime = LocalDateTime.now();
        LocalDateTime pastTime = LocalDateTime.of(history.getDateTime().getYear(), history.getDateTime().getMonth(), history.getDateTime().getDayOfMonth(), history.getDateTime().getHour(), history.getDateTime().getMinute());
        Duration duration = Duration.between(pastTime, currentTime);
        long minutes = duration.toMinutes();
        return minutes;
    }

    private void turnOnPump(){
        // Get reference
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference ref = db.getReference();
        ref.child("settings").child("pumpIsOn").setValue(true);
    }

    private void setActivityEnabled(Boolean isOnline, Slider sliderThreshold, TextView txtMoistureValue, MaterialButton btnManualOn, MaterialButton btnApplyThreshold, MaterialButton btnCancelApplyThreshold) {
        if (!isOnline) {
            sliderThreshold.setEnabled(false);
            txtMoistureValue.setText("--");
            btnManualOn.setEnabled(false);
            btnApplyThreshold.setEnabled(false);
            btnCancelApplyThreshold.setEnabled(false);
        } else {
            sliderThreshold.setEnabled(true);
            btnManualOn.setEnabled(true);
            btnApplyThreshold.setEnabled(true);
            btnCancelApplyThreshold.setEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring);

        // Init components
        sliderThreshold = findViewById(R.id.sliderThreshold);
        txtMoistureValue = findViewById(R.id.txtMoistureValue);
        btnManualOn = findViewById(R.id.btnManualOn);
        btnApplyThreshold = findViewById(R.id.btnThresholdApply);
        btnCancelApplyThreshold = findViewById(R.id.btnThresholdCancel);

        // Check if esp32 is online
        mDatabase.child("settings").child("isOnline").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!Boolean.getBoolean(String.valueOf(snapshot.getValue()))){
                    isOnline = false;
                    setActivityEnabled(false, sliderThreshold, txtMoistureValue, btnManualOn, btnApplyThreshold, btnCancelApplyThreshold);
                }
                else{
                    isOnline = true;
                    setActivityEnabled(true, sliderThreshold, txtMoistureValue, btnManualOn, btnApplyThreshold, btnCancelApplyThreshold);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Database Value Change Listener
        mDatabase.child("moistureData").child("value").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(isOnline){
                    txtMoistureValue.setText(String.valueOf(snapshot.getValue()) + " %");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // Manual Turn On Button
        btnManualOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnOnPump();
            }
        });

        // Apply Threshold Button
        btnApplyThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sliderValue = Math.round(sliderThreshold.getValue());
                mDatabase.child("settings").child("threshold").setValue(sliderValue);
            }
        });

        // Cancel Threshold Button
        btnCancelApplyThreshold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sliderThreshold.setValue(sliderValue);
            }
        });

        // Get latest threshold data
        mDatabase.child("settings").child("threshold").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!task.isSuccessful()){
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else{
                    sliderValue = ((Long) task.getResult().getValue()).intValue();
                    sliderThreshold.setValue(sliderValue);
                }
            }
        });

        // RecyclerView things
        adapter = new HistoryRecyclerViewAdapter(arrHistory);
        recyclerHistory = findViewById(R.id.recyclerHistory);
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this.getApplicationContext()));
        recyclerHistory.setAdapter(adapter);

        // Fetch history data from database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("history");

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Get action type
                String actionType = snapshot.child("actionType").getValue(String.class);
                // Get timestamp
                HashMap<String, Object> dateTimeMap = snapshot.child("dateTime").getValue(new GenericTypeIndicator<HashMap<String, Object>>() {});
                Long year = (Long) dateTimeMap.get("year");
                int yearValue = year.intValue();
                Long month = (Long) dateTimeMap.get("monthValue");
                int monthValue = month.intValue();
                Long day = (Long) dateTimeMap.get("dayOfMonth");
                int dayValue = day.intValue();
                Long hour = (Long) dateTimeMap.get("hour");
                int hourValue = hour.intValue();
                Long minute = (Long) dateTimeMap.get("minute");
                int minuteValue = minute.intValue();
                LocalDateTime dateTime = LocalDateTime.of(yearValue, monthValue, dayValue, hourValue, minuteValue);
                // Create a new 'History' object with the retrieved data
                History history = new History(actionType, dateTime);
                // Add object 'history' to arrayList
                arrHistory.add(history);
                adapter.updateArray(arrHistory);
                // Ascending order based on latest duration
                Collections.sort(arrHistory, new HistoryComparator());
                // Refresh RecyclerView
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}