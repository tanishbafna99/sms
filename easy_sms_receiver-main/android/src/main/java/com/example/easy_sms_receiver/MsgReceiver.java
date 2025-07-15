package com.example.easy_sms_receiver;

import static android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class MsgReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SMS_RECEIVED_ACTION.equals(intent.getAction())) {
            Log.d("MsgReceiver", "📩 SMS_RECEIVED_ACTION triggered");
            SmsMessage[] smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            handleMessage(smsMessages);
        }
    }

    private void handleMessage(SmsMessage[] smsList) {
        Map<String, StringBuilder> messagesGroupedByAddress = new HashMap<>();

        for (SmsMessage sms : smsList) {
            String address = sms.getOriginatingAddress();
            String body = sms.getMessageBody();

            if (!messagesGroupedByAddress.containsKey(address)) {
                messagesGroupedByAddress.put(address, new StringBuilder());
            }
            messagesGroupedByAddress.get(address).append(body);
        }

        for (Map.Entry<String, StringBuilder> entry : messagesGroupedByAddress.entrySet()) {
            String sender = entry.getKey();
            String fullMessage = entry.getValue().toString();

            Log.d("MsgReceiver", "📨 From: " + sender + " | Message: " + fullMessage);
            passMessageToFlutter(sender, fullMessage);
        }
    }

    private void passMessageToFlutter(String address, String body) {
        if (EasySmsReceiverPlugin.channel != null) {
            Map<String, String> data = new HashMap<>();
            data.put("address", address);
            data.put("body", body);

            EasySmsReceiverPlugin.channel.invokeMethod(Constants.onMessage, data);
        } else {
            Log.w("MsgReceiver", "⚠️ Flutter channel is null. Can't send SMS data.");
        }
    }
}
