package com.example.easy_sms_receiver;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class CallSmsForegroundService extends Service {

    public static final String CHANNEL_ID = "CallSmsServiceChannel";

    private CallReceiver callReceiver;
    private MsgReceiver msgReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize and register receivers
        callReceiver = new CallReceiver();
        msgReceiver = new MsgReceiver();

        IntentFilter callFilter = new IntentFilter("android.intent.action.PHONE_STATE");
        registerReceiver(callReceiver, callFilter);

        IntentFilter smsFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        smsFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(msgReceiver, smsFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("📲 Call & SMS Monitor")
                .setContentText("Running in background to detect calls & SMS")
                .setSmallIcon(android.R.drawable.sym_call_incoming)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE);

        // Android 12+: Optional behavioral flag
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE);
        }

        try {
            startForeground(1, builder.build());
        } catch (SecurityException e) {
            e.printStackTrace();
            stopSelf(); // Prevent crash loop
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (callReceiver != null) unregisterReceiver(callReceiver);
        if (msgReceiver != null) unregisterReceiver(msgReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Call & SMS Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
}
