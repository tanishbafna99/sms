package com.example.easy_sms_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionInfo;
import android.app.NotificationManager;
import android.app.NotificationChannel;
import android.app.Notification;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.List;

public class CallReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "incoming_call_channel";
    private static final int NOTIFICATION_ID = 101;

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (!TelephonyManager.EXTRA_STATE_RINGING.equals(state)) return;

        String simLabel = getSimInfoBySlot(context);

        Log.d("CallReceiver", "📞 Incoming call from: " + incomingNumber + " on " + simLabel);

        showNotification(context, incomingNumber, simLabel);

        EventSinkHelper.send("Incoming call from: " + incomingNumber + " on " + simLabel);
    }

    private void showNotification(Context context, String phoneNumber, String simLabel) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Incoming Call Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Shows incoming call notifications");
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("📞 Incoming Call")
                .setContentText("From: " + (phoneNumber != null ? phoneNumber : "Unknown") + "\nVia: " + simLabel)
                .setSmallIcon(android.R.drawable.sym_call_incoming)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    // ✅ Fallback: Show all SIM slots even if number isn't available
    private String getSimInfoBySlot(Context context) {
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (subscriptionManager != null) {
            List<SubscriptionInfo> subscriptionList = subscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptionList != null && !subscriptionList.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (SubscriptionInfo info : subscriptionList) {
                    if (info == null) continue;
                    int slot = info.getSimSlotIndex();
                    String label = info.getCarrierName() != null ? info.getCarrierName().toString() : "SIM " + (slot + 1);
                    builder.append("SIM ").append(slot + 1).append(" (").append(label).append("), ");
                }
                return builder.toString().replaceAll(", $", "");
            }
        }
        return "Unknown SIM";
    }
}
