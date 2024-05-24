package com.days_counter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final int UPDATE_DURATION = 1000;
    private TextView show_days;
    private Button start_btn;

    private SharedPreferences sp;

    private Handler handler;
    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateDisplay();
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler = new Handler();
        sp = (SharedPreferences) getSharedPreferences("database", Context.MODE_PRIVATE);

        show_days = (TextView) findViewById(R.id.show_days);
        start_btn = (Button) findViewById(R.id.start_btn);

        start_btn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDisplay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.start_btn) {

            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);

            if (sp.getString("store_date", null) != null) {
                // For clear date and time from database to stop count-down
                dialog.setTitle("Clear Time:");
                dialog.setMessage("Do you want to stop time count down?");
                dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sp.edit().putString("store_date", null).apply();
                        updateDisplay();
                    }
                });
                dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.create();
            } else {
                // For set date and time to start count-down
                dialog.setTitle("Set Time:");
                dialog.setMessage("Start days count down! \n\n Current Date: " + getCurrentDateTime());
                dialog.setPositiveButton("SET", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sp.edit().putString("store_date", getCurrentDateTime()).apply();
                        updateDisplay();
                    }
                });
                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialog.create();
            }
            dialog.show();
        }
    }

    private void updateDisplay(){
        if(sp.getString("store_date", null) != null) {
            show_days.setText(calculateDaysAndHoursDifference(sp.getString("store_date", getCurrentDateTime())));
            start_btn.setText("Stop");
            handler.postDelayed(runnable, UPDATE_DURATION);
        } else {
            show_days.setText("Please click 'Start' count down");
            start_btn.setText("Start");
            handler.removeCallbacks(runnable);
        }
    }

    private String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date currentDate = new Date();
        return formatter.format(currentDate);
    }

    private String calculateDaysAndHoursDifference(String previousDateTimeStr) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date previousDateTime = formatter.parse(previousDateTimeStr);
            Date currentDateTime = new Date();
            long differenceInMillis = currentDateTime.getTime() - previousDateTime.getTime();
            long days = differenceInMillis / (24 * 60 * 60 * 1000);
            long hours = (differenceInMillis % (24 * 60 * 60 * 1000)) / (60 * 60 * 1000);
            long minutes = (differenceInMillis % (60 * 60 * 1000)) / (60 * 1000);
            long seconds = (differenceInMillis % (60 * 1000)) / 1000;

            return daysFormat(days + 1) + " (" + preFormat(hours) + ":" + preFormat(minutes) + ":" + preFormat(seconds) + "), ago";
        } catch (Exception e){
            return "Error date calculation!";
        }
    }

    private String daysFormat(long days){
        return days < 2 ? days + " Day" : days + " Days";
    }

    private String preFormat(long n){
        return n < 10 ? "0" + n : String.valueOf(n);
    }
}