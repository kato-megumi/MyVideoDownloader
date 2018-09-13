package com.komachi.myvideodownloader;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Random;

public class DirectDl extends IntentService {
    private int NOTIFY_ID;
    private int FOREGROUND_ID;
    public NotificationCompat.Builder builder;
    String filename = "";

    public DirectDl() {
        super("DirectDl");
        Random r = new Random();
        NOTIFY_ID = r.nextInt(32768);
        FOREGROUND_ID = r.nextInt(32768);
    }

    @Override
    public void onHandleIntent(Intent i) {
        String link = i.getStringExtra("a");
        String type = i.getStringExtra("type");
        String ffmpeg = getApplicationContext().getFilesDir().getAbsolutePath();

        SharedPreferences settings = getSharedPreferences("settings", 0);
        String path = settings.getString("store", "");

        File files[] = getExternalFilesDirs("");
        String store = files[files.length - 1].getAbsolutePath();

        startForeground(FOREGROUND_ID, buildForegroundNotification(link));
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python python = Python.getInstance();
        PyObject ydl = python.getModule("direct_dl");
        if (link != null) {
            try {
                try {
                    ydl.get("download").call(this, link, store, type, ffmpeg);
                } catch (Exception e) {
                    doneDownload(false);
                }
                File flstore = new File(store + "/" + filename);
                File flpath = new File(path + "/" + filename);
                flstore.delete();
                FileUtils.copyFile(flstore, flpath);
            } catch (Exception e) {
                Log.d("", "onHandleIntent: ");
            }
        }
    }

    public void updateStatus(int a, String b) {
        builder.setContentTitle("Downloading").setContentText(b).
                setStyle(new NotificationCompat.BigTextStyle().bigText(b));

        if (a == -1) {
            builder.setProgress(100, a, true);
        }
        builder.setProgress(100, a, false);

        NotificationManager nm = (NotificationManager) (getSystemService(Context.NOTIFICATION_SERVICE));
        nm.notify(FOREGROUND_ID, builder.build());
    }

    public void doneDownload(boolean a) {
        if (a) {
            raiseNotification("Download complete");
        } else {
            raiseNotification("Download fail");
        }
        stopForeground(true);
    }

    private void raiseNotification(String s) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this, "a");
        b.setAutoCancel(true)
                //.setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(s)
                .setContentText("")
                .setSmallIcon(android.R.drawable.stat_notify_more)
                .setTicker("");
        NotificationManager mgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mgr.notify(NOTIFY_ID, b.build());
    }

    private Notification buildForegroundNotification(String link) {
        builder = new NotificationCompat.Builder(this, "ad");
        builder.setOngoing(true)
                .setContentTitle("Preparing...")
                .setContentText(link)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setTicker("aaaa")
                .setProgress(1000, 0, false)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(link));

        return (builder.build());
    }

}