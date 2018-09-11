package com.komachi.myvideodownloader;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Download extends AppCompatActivity {
    ArrayList<DownObj> dos = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        String link =  getIntent().getStringExtra("link");

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python python = Python.getInstance();


        PyObject ydl = python.getModule("info");
        PyObject dict = ydl.get("fetch_data").call(link);
        PyObject list = dict.callAttr("get","formats");


//        ArrayList<DownObj> dos = new ArrayList<>();
        for (int i=0; i< list.callAttr("__len__").toJava(int.class);i++){
            PyObject down = list.callAttr("pop");
            dos.add(new DownObj(down.callAttr("get","ext").toString(),
                    Integer.parseInt(down.callAttr("get","filesize").toString()),
                    down.callAttr("get","format").toString(),
                    down.callAttr("get","url").toString()));
        }


        RecyclerView recyclerView = findViewById(R.id.recycle_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this,1));
        DownObjAdapter adapter = new DownObjAdapter(this,dos);
        recyclerView.setAdapter(adapter);
        ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener(){
            @Override
            public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                down(dos.get(position).url);
            }
        });



    }
    void down(String link){
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
        request.setDescription("Some descrition");
        request.setTitle("Some title");
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "name-of-the-file.ext");

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }
}
