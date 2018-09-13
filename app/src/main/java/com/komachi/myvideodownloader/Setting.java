package com.komachi.myvideodownloader;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class Setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        String store = getSharedPreferences("settings", 0).getString("store","");
        ((TextView)findViewById(R.id.tvStore)).setText(store);

        File f1 = new File(getExternalFilesDir("").getAbsolutePath()+"/Download/ffmpeg");
        File f2 = new File(getExternalFilesDir("").getAbsolutePath()+"/Download/ffprobe");
        boolean g1 = f1.canExecute();
        boolean g2 = f2.canExecute();
        if (!g1){ g1 = f1.setExecutable(true,false); }
        if (!g2){ g2 = f2.setExecutable(true,false); }
        if (!g1 | !g2){
            f1.delete();f1.delete();
            Button v = findViewById(R.id.ffmpeg);
            v.setEnabled(false);
            v.setClickable(false);
        }
    }
    public  void ffmpeg(View v){
        v.setEnabled(false);
        v.setClickable(false);
        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        down("http://whhh.000webhostapp.com/source/ffmpeg","ffmpeg");
        down("http://whhh.000webhostapp.com/source/ffprobe","ffprobe");
    }
    void down(String link, String name){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
        request.setDescription(name);
        request.setTitle("Downloading "+name);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(this,Environment.DIRECTORY_DOWNLOADS, name);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        long a = manager.enqueue(request);
        getSharedPreferences("tmp", 0).edit().putLong(name,a).apply();
    }
    public void Select(View v){
        // get sdcard permission
        // https://stackoverflow.com/questions/34331956/trying-to-takepersistableuripermission-fails-for-custom-documentsprovider-via
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, 42);

    }
    BroadcastReceiver onComplete=new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {
            String done;
            long id = intent.getLongExtra("extra_download_id",0);
            long fid = getSharedPreferences("tmp", 0).getLong("ffmpeg",0);
            if (id ==fid){
                done = "ffmpeg";
            } else {
                done = "ffprobe";
            }
            File f = new File(getExternalFilesDir("").getAbsolutePath()+"/Download/"+done);
            boolean q = f.setExecutable(true,false);
        }
    };
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 42:
                if (resultCode != RESULT_OK)
                    return;
                Uri treeUri = data.getData();
                String newpath = FileUtil.getFullPathFromTreeUri(treeUri,this);
                grantUriPermission(getPackageName(), treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                TextView textView = findViewById(R.id.tvStore);
                String path = FileUtil.getFullPathFromTreeUri(treeUri,this);
                textView.setText(path);
                getSharedPreferences("settings", 0).edit()
                        .putString("store",path).apply();

        }
    }
}
