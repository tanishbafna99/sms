package com.example.easy_sms_receiver;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootServiceJob extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d("BootServiceJob", "⏰ BootServiceJob triggered — starting CallSmsForegroundService");

        Intent serviceIntent = new Intent(this, CallSmsForegroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        jobFinished(params, false);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true; // Retry if job is stopped
    }
}
