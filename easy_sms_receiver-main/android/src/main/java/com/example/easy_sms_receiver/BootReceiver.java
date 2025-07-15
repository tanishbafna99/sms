package com.example.easy_sms_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;

/**
 * Receives the BOOT_COMPLETED broadcast and starts the persistent CallSmsForegroundService.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "📱 Device rebooted — scheduling CallSmsForegroundService...");

            // For Android 12+ (API 31+), use JobScheduler instead of directly starting foreground service
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ComponentName componentName = new ComponentName(context, BootServiceJob.class);
                JobInfo jobInfo = new JobInfo.Builder(1234, componentName)
                        .setMinimumLatency(1) // Run as soon as possible
                        .setOverrideDeadline(3000) // Max delay
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                        .setPersisted(true) // Persist after reboot
                        .build();

                JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                if (scheduler != null) {
                    scheduler.schedule(jobInfo);
                    Log.d(TAG, "✅ Job scheduled successfully for foreground service.");
                }
            } else {
                // For older Android versions
                Intent serviceIntent = new Intent(context, CallSmsForegroundService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
                Log.d(TAG, "✅ Foreground service started directly.");
            }
        }
    }
}
