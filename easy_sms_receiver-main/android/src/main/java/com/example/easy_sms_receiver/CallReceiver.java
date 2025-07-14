package com.example.easy_sms_receiver;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.app.Notification;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.easy_sms_receiver.EventSinkHelper;

public class CallReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "incoming_call_channel";

    // Access the singleton EventSinkHelper from plugin
    public static EventSinkHelper eventSinkHelper = new EventSinkHelper();

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            Log.d("CallReceiver", "📞 Incoming call from: " + number);

            // 1. Show a notification popup
            showNotification(context, number);

            // 2. Send the call number to Flutter
            if (eventSinkHelper != null) {
                eventSinkHelper.send("Incoming call from: " + number);
            }
        }
    }

    private void showNotification(Context context, String phoneNumber) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Incoming Call Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Shows notifications on incoming calls");
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("📞 Incoming Call")
                .setContentText("From: " + (phoneNumber != null ? phoneNumber : "Unknown"))
                .setSmallIcon(android.R.drawable.sym_call_incoming)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(101, notification);
    }
}
