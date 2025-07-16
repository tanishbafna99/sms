package com.example.easy_sms_receiver;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import android.os.Build;
import android.util.Log;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Notification;
import android.database.Cursor;
import android.provider.CallLog;
import android.content.pm.PackageManager;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CallReceiver extends BroadcastReceiver {

    private static final String TAG = "CallReceiver";
    private static String lastState = TelephonyManager.EXTRA_STATE_IDLE;
    private static long callStartTime = 0;
    private static boolean isIncoming = false;
    private static String savedNumber = "Unknown";

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        String outgoingNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        String number = (incomingNumber != null) ? incomingNumber : outgoingNumber;

        if (number != null && !number.isEmpty()) {
            savedNumber = number;
        }

        if (state == null && intent.getAction() != null &&
                intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            EventSinkHelper.send("📤 Outgoing call started to: " + savedNumber);
            return;
        }

        if (state == null || state.equals(lastState)) return;

        switch (state) {
            case "RINGING":
                isIncoming = true;
                callStartTime = System.currentTimeMillis();
                EventSinkHelper.send("📞 Incoming call from: " + savedNumber);
                break;

            case "OFFHOOK":
                isIncoming = lastState.equals("RINGING");
                callStartTime = System.currentTimeMillis();
                EventSinkHelper.send((isIncoming ? "✅ Call answered from: " : "📤 Outgoing call connected to: ")
                        + savedNumber);
                break;

            case "IDLE":
                if (lastState.equals("RINGING")) {
                    String simUsed = getLastCallSimLabel(context);
                    String callTime = getLastCallTime(context);
                    EventSinkHelper.send("❌ Missed call from: " + savedNumber + " on " + simUsed + " at " + callTime);
                    showMissedCallNotification(context, savedNumber, simUsed, callTime);
                } else if (lastState.equals("OFFHOOK")) {
                    String simUsed = getLastCallSimLabel(context);
                    String callTime = getLastCallTime(context);
                    String duration = getLastCallDuration(context);
                    EventSinkHelper.send((isIncoming ? "📴 Incoming" : "📴 Outgoing")
                            + " call ended with: " + savedNumber + " on " + simUsed
                            + " at " + callTime + " (⏱ " + duration + "s)");
                }
                break;
        }

        lastState = state;
    }

    private String getLastCallSimLabel(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {

                Cursor cursor = context.getContentResolver().query(
                        CallLog.Calls.CONTENT_URI,
                        null,
                        null,
                        null,
                        CallLog.Calls.DATE + " DESC"
                );

                if (cursor != null && cursor.moveToFirst()) {
                    int subId = cursor.getInt(cursor.getColumnIndexOrThrow("subscription_id"));

                    SubscriptionManager sm = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
                    List<SubscriptionInfo> list = sm.getActiveSubscriptionInfoList();
                    for (SubscriptionInfo info : list) {
                        if (info.getSubscriptionId() == subId) {
                            cursor.close();
                            return "SIM " + (info.getSimSlotIndex() + 1) + " (" + info.getDisplayName() + ")";
                        }
                    }
                    cursor.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "SIM detection error: " + e.getMessage());
        }
        return "Unknown SIM";
    }

    private String getLastCallTime(Context context) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                Cursor cursor = context.getContentResolver().query(
                        CallLog.Calls.CONTENT_URI,
                        null,
                        null,
                        null,
                        CallLog.Calls.DATE + " DESC"
                );

                if (cursor != null && cursor.moveToFirst()) {
                    long dateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    cursor.close();

                    SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    return formatter.format(new Date(dateMillis));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Call time error: " + e.getMessage());
        }
        return "Unknown Time";
    }

    private String getLastCallDuration(Context context) {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                Cursor cursor = context.getContentResolver().query(
                        CallLog.Calls.CONTENT_URI,
                        null,
                        null,
                        null,
                        CallLog.Calls.DATE + " DESC"
                );

                if (cursor != null && cursor.moveToFirst()) {
                    int duration = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION));
                    cursor.close();
                    return String.valueOf(duration);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Call duration error: " + e.getMessage());
        }
        return "0";
    }

    private void showMissedCallNotification(Context context, String number, String simLabel, String time) {
        String CHANNEL_ID = "missed_call_channel";
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Missed Call Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Shows missed call notifications");
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("❌ Missed Call")
                .setContentText("From: " + number + "\nVia: " + simLabel + "\nAt: " + time)
                .setSmallIcon(android.R.drawable.stat_notify_missed_call)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        manager.notify(102, notification);
    }
}
