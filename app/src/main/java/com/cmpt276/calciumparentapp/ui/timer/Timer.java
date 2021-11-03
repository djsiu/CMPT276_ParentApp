package com.cmpt276.calciumparentapp.ui.timer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.cmpt276.calciumparentapp.R;
import com.cmpt276.calciumparentapp.model.timer.TimerLogic;

import java.util.Objects;

public class Timer extends AppCompatActivity {
    private TextView countdownText;
    public static final String CHANNEL_ID = "timerServiceChannel";
    private final TimerLogic timerLogic = TimerLogic.getInstance();
    private long timeRemaining;
    private BroadcastReceiver broadcastReceiver;
    private BroadcastReceiver timerRunningBroadcastReciever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timer);
        createNotificationChannel();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        countdownText = findViewById(R.id.countdown_text);

        setup();
    }

    @Override
    protected void onResume() {
        setupBroadcastReceiver();
        broadcastTimeRequest();
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterBroadcastReceiver();
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Top left back arrow
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        // If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }


    private void setup(){
        if(timerLogic.isTimerServiceRunning(this)){
            broadcastTimerRunningRequest();
            broadcastTimeRequest();
        }
        else{
            countdownText.setText(timerLogic.getTimerText(timerLogic.getTimerLength()));
            setupButtons(false, false);
        }

    }

    private void setupButtons(boolean timerServiceRunning, boolean timerRunning){
        Button btn = (Button) findViewById(R.id.btnStartPause);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStartPauseButtonClick((Button) v);
            }
        });

        if(timerServiceRunning && timerRunning){
            btn.setText(R.string.btnPause);
        }

        if(timerServiceRunning && !timerRunning) {
            btn.setText(R.string.btnResume);
        }
    }


    private void onStartPauseButtonClick(Button btn) {
        // Check what the button is supposed to do
        if(btn.getText().equals(getString(R.string.btnStart))){
            btn.setText(R.string.btnPause);
            startTimer();
        }

        else if(btn.getText().equals(getString(R.string.btnPause))){
            broadcastPauseIntent();
            btn.setText(R.string.btnResume);
        }

        else if(btn.getText().equals(getString(R.string.btnResume))){
            broadcastResumeIntent();
            btn.setText(R.string.btnPause);
        }
    }

    private void startTimer() {
        Button btn = (Button) findViewById(R.id.btnStartPause);
        btn.setText(R.string.btnPause);
        startTimerService();
    }


    // TIMER SERVICE

    // Creates the notification channel for the required versions of android
    // May need to be called only once for the whole application in which case this needs to be
    // moved to the main activity
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Timer Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        serviceChannel.enableVibration(false);
        serviceChannel.enableLights(false);
        serviceChannel.setSound(null, null);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    private void startTimerService() {
        Intent serviceIntent = new Intent(this, TimerService.class);
        serviceIntent.putExtra(getString(R.string.timer_length_extra), timerLogic.getTimerLength());
        startService(serviceIntent);
    }

    private void setupBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                timeRemaining = intent.getLongExtra(TimerService.TIME_REMAINING_BROADCAST, -1);
                countdownText.setText(timerLogic.getTimerText(timeRemaining));
            }
        };

        timerRunningBroadcastReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setupButtons(true, intent.getBooleanExtra(TimerService.TIMER_RUNNING_BROADCAST, false));
            }
        };

        IntentFilter filter = new IntentFilter(TimerService.TIME_BROADCAST_FILTER);
        this.registerReceiver(broadcastReceiver, filter);

        IntentFilter timerRunningFilter = new IntentFilter(TimerService.TIMER_RUNNING_BROADCAST_FILTER);
        this.registerReceiver(timerRunningBroadcastReciever, timerRunningFilter);
    }

    private void unregisterBroadcastReceiver() {
        unregisterReceiver(broadcastReceiver);
    }

    private void broadcastPauseIntent() {
        Log.e("Pause", "Broadcasting pause intent");
        Intent i = new Intent();
        i.putExtra(TimerService.PAUSE_TIMER_INTENT, true);
        i.setAction(TimerService.TIMER_SERVICE_REQUEST_FILTER);
        sendBroadcast(i);
    }

    private void broadcastResumeIntent() {
        Log.e("Resume", "Broadcasting resume intent");
        Intent i = new Intent();
        i.putExtra(TimerService.RESUME_TIMER_INTENT, true);
        i.setAction(TimerService.TIMER_SERVICE_REQUEST_FILTER);
        sendBroadcast(i);
    }

    private void broadcastTimeRequest() {
        Intent i = new Intent();
        i.putExtra(TimerService.REFRESH_TIME_INTENT, true);
        i.setAction(TimerService.TIMER_SERVICE_REQUEST_FILTER);
        sendBroadcast(i);
    }

    private void broadcastTimerRunningRequest() {
        Intent i = new Intent();
        i.putExtra(TimerService.TIMER_RUNNING_REQUEST_INTENT, true);
        i.setAction(TimerService.TIMER_SERVICE_REQUEST_FILTER);
        sendBroadcast(i);
    }

}