package com.komachi.myvideodownloader;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;


public class IntentServiceDownload extends IntentService {

    String name, url, uri;
    DocumentFile df,newFile;
    public int NOTIFY;
    NotificationCompat.Builder builder;
    volatile boolean stop = false;

    public IntentServiceDownload() {
        super("IntentServiceDownload");
    }

    public static void start(Context context, String url, String name, ArrayList<String> a) {
        Intent intent = new Intent(context, IntentServiceDownload.class);
        for (String x : a) {
            intent.putExtra(x, true);
        }
        intent.putExtra("url", url);
        intent.putExtra("name", name);
        context.startService(intent);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        name = intent.getStringExtra("name");
        url = intent.getStringExtra("url");
        uri = getApplicationContext().getSharedPreferences("settings", 0)
                .getString("uri", "");
        df = DocumentFile.fromTreeUri(this,Uri.parse(uri));
        download(url, name);
    }

    private void download(String path, String name) {
        Random r = new Random();
        NOTIFY = r.nextInt(32768) + 10000;

        builder = new NotificationCompat.Builder(this, "add");
        Intent i = new Intent(this, NotificationReceiver.class);
        i.putExtra("id",NOTIFY);
        PendingIntent pIntent = PendingIntent.
                getBroadcast(this, 1337, i, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setOngoing(true)
                .setContentTitle("Download ")
                .setSubText(name)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setProgress(1000, 0, false)
                .setAutoCancel(true)
                .addAction(R.attr.closeIcon, "Cancel", pIntent);

        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFY, builder.build());
        try {
            URL url = new URL(path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.e("e", "Server returned HTTP " + connection.getResponseCode() + " "
                        + connection.getResponseMessage());
            }
            int fileLength = connection.getContentLength();
            InputStream input = connection.getInputStream();
            newFile = df.createFile("*/*",name);
            OutputStream output =  getContentResolver().openOutputStream(newFile.getUri());
            byte data[] = new byte[8192];
            long total = 0;
            int count, progress;
            while ((count = input.read(data)) != -1) {
                if (stop) break;
                total += count;
                progress = fileLength > 0 ? (int) (total * 1000 / fileLength) : 0;
                updateProgress(progress, total);
                output.write(data, 0, count);
            }
            finish();

        } catch (Exception e) {
            Log.e("e", e.toString());
        }

    }

    private void updateProgress(int progress, long total) {
        builder.setProgress(1000, progress, false)
                .setContentText("Downloaded: " + (double) total / 1000 + "kb");
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFY, builder.build());
    }

    public void stop() {
        stop = true;
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFY);
        File f = new File(uri + name);
        f.delete();
    }
    void finish(){
        builder = new NotificationCompat.Builder(this, "add");
        Intent i = new Intent(Intent.ACTION_VIEW) ;
        i.setData(newFile.getUri());
        PendingIntent p = PendingIntent.getActivity(this,1234,i,PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setAutoCancel(true).setContentIntent(p)
                .setContentText(name)
                .setContentTitle("Download Finished")
        .setSmallIcon(android.R.drawable.stat_sys_download_done);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(NOTIFY, builder.build());
    }
}
