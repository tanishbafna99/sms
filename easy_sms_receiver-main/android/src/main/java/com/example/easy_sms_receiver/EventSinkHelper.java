package com.example.easy_sms_receiver;

import io.flutter.plugin.common.EventChannel;

public class EventSinkHelper {
    private EventChannel.EventSink sink;

    public void setSink(EventChannel.EventSink sink) {
        this.sink = sink;
    }

    public void send(String message) {
        if (sink != null) sink.success(message);
    }

    public void endStream() {
        if (sink != null) sink.endOfStream();
    }

    public void error(String code, String msg, Object details) {
        if (sink != null) sink.error(code, msg, details);
    }
}
