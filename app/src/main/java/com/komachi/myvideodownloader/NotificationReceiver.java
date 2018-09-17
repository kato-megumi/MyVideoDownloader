package com.komachi.myvideodownloader;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (context instanceof IntentServiceDownload){
            ((IntentServiceDownload) context).stop();
            int notify = intent.getIntExtra("id",0);
            ((NotificationManager)context.getSystemService(NOTIFICATION_SERVICE)).
                    cancel(notify);
        }
    }
}
