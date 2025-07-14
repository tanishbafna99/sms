package com.example.easy_sms_receiver;

import android.content.Context;
import android.content.IntentFilter;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.EventChannel;

public class EasySmsReceiverPlugin implements FlutterPlugin, MethodChannel.MethodCallHandler {

  public static MethodChannel channel;
  public static final EventSinkHelper eventSinkHelper = new EventSinkHelper();

  private Context mContext;
  private MsgReceiver msgReceiver = new MsgReceiver();

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    // Set up the method channel for SMS control
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), Constants.easySmsReceiverChannel);
    channel.setMethodCallHandler(this);

    // Set up the event channel for incoming call events
    new EventChannel(
            flutterPluginBinding.getBinaryMessenger(),
            "easy_sms_receiver/call_event"
    ).setStreamHandler(new EventChannel.StreamHandler() {
      @Override
      public void onListen(Object arguments, EventChannel.EventSink events) {
        eventSinkHelper.setSink(events);
      }

      @Override
      public void onCancel(Object arguments) {
        eventSinkHelper.setSink(null);
      }
    });

    // Save application context
    mContext = flutterPluginBinding.getApplicationContext();
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
    switch (call.method) {
      case Constants.startReceiverMethod:
        startReceiver();
        result.success(null);
        break;

      case Constants.stopReceiverMethod:
        stopReceiver();
        result.success(null);
        break;

      default:
        result.notImplemented();
        break;
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  public void startReceiver() {
    IntentFilter intent = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
    mContext.registerReceiver(msgReceiver, intent);
    System.out.println("::::::EasySmsReceiver - start receiver: ");
  }

  public void stopReceiver() {
    try {
      mContext.unregisterReceiver(msgReceiver);
      System.out.println("::::::EasySmsReceiver - stop receiver: ");
    } catch (Exception e) {
      System.out.println(":::::::EasySmsReceiver Exception: " + e.toString());
    }
  }
}
