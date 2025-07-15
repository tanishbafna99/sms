package com.example.easy_sms_receiver;

import io.flutter.plugin.common.EventChannel;

public class EventSinkHelper {
    public static EventChannel.EventSink sink;

    public static void setSink(EventChannel.EventSink eventSink) {
        sink = eventSink;
    }

    public static void send(String message) {
        if (sink != null) sink.success(message);
    }

    public static void endStream() {
        if (sink != null) sink.endOfStream();
    }

    public static void error(String code, String msg, Object details) {
        if (sink != null) sink.error(code, msg, details);
    }
}
