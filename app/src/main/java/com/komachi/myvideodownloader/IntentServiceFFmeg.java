package com.komachi.myvideodownloader;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class IntentServiceFFmeg extends IntentService {
    String f1 = "http://whhh.000webhostapp.com/source/";
    String f2 = "http://whhh.000webhostapp.com/source/";
    String store;
    int NOTIFY =1337;
    NotificationCompat.Builder builder;

    public IntentServiceFFmeg() {
        super("IntentServiceFFmeg");
    }

    public static void start(Context context, String param1) {
        Intent intent = new Intent(context, IntentServiceFFmeg.class);
        intent.putExtra("store",param1);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        store = intent.getStringExtra("store");
        download("ffprove");
        download("ffmpeg");
    }

    private void download(String f) {
        builder = new NotificationCompat.Builder(this, "add");
        builder.setOngoing(true)
                .setContentTitle("Download "+f)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setTicker("aaaa")
                .setProgress(1000, 0, false)
                .setAutoCancel(true);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFY, builder.build());
        try {
            URL url = new URL(f1+f);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e("e", "Server returned HTTP " + connection.getResponseCode() + " "
                        + connection.getResponseMessage());
            }
            int fileLength = connection.getContentLength();
            InputStream input = connection.getInputStream();
            OutputStream output  = new FileOutputStream(store+f);
            byte data[] = new byte[4096];
            long total = 0;
            int count,progress;
            while ((count = input.read(data)) != -1) {
                total += count;
                progress = fileLength > 0 ? (int)(total * 1000 / fileLength) : 0;
                updateProgress(progress,total);
                output.write(data, 0, count);}

        }
        catch (Exception e){ Log.e("e",e.toString());}
        File file = new File(store+f);
        file.setExecutable(true,false);

    }

    private  void updateProgress(int progress, long total ){
        builder.setProgress(1000,progress,false)
            .setContentText("Downloaded: "+(double)total/1000 + "kb");
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFY, builder.build());
    }
}
