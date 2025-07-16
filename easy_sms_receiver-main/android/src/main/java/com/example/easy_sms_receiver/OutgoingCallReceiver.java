package com.example.easy_sms_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class OutgoingCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

        Log.d("OutgoingCallReceiver", "📤 Outgoing call to: " + number);

        // Send to Flutter
        EventSinkHelper.send("📤 Outgoing call to: " + number);

        // Optional: Toast or Notification
        Toast.makeText(context, "Outgoing call to: " + number, Toast.LENGTH_SHORT).show();
    }
}
