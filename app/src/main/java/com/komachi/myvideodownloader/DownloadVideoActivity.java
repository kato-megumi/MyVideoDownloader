package com.komachi.myvideodownloader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


public class DownloadVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String ffmpeg =getFilesDir().getAbsolutePath();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            String link = intent.getStringExtra(Intent.EXTRA_TEXT);
            Intent i=new Intent(this, DirectDl.class);
            i.putExtra("a",link);
            i.putExtra("type","video");
            i.putExtra("dirs",ffmpeg);
            startService(i);
        };
        finish();
    }


}