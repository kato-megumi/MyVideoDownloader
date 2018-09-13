package com.komachi.myvideodownloader;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;

public class Download extends AppCompatActivity {
    ArrayList<DownObj> dos = new ArrayList<>();
    String link;
    PyObject dict;
    String title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        link = getIntent().getStringExtra("link");
        new Thread() {
            @Override
            public void run() { bg(); }
        }.start();
        findViewById(R.id.wait_data).setVisibility(View.INVISIBLE);
    }

    void down(String link, String name) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link));
        request.setDescription(name);
        request.setTitle("Downloading " + name);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        String store = getSharedPreferences("settings", 0)
                .getString("store", "");
        request.setDestinationInExternalPublicDir(store, name);

        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    void bg() {

        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        Python python = Python.getInstance();
        PyObject info = python.getModule("info");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.python).setVisibility(View.INVISIBLE);
                findViewById(R.id.wait_data).setVisibility(View.VISIBLE);
            }
        });
        dict = info.get("fetch_data").call(link);
        switch (dict.callAttr("get", "dltype").toJava(int.class)) {
            case 1:
                PyObject list = dict.callAttr("get", "formats");
                title = dict.callAttr("get", "title").repr();
                for (int i = 0; i < list.callAttr("__len__").toJava(int.class); i++) {
                    PyObject down = list.callAttr("pop");
                    String ext = down.callAttr("get", "ext").toString();
                    String format = down.callAttr("get", "format_note").toString();
                    String url = down.callAttr("get", "url").toString();
                    int filesize;
                    try {
                        filesize = down.callAttr("get", "filesize").toJava(int.class);
                    } catch (Exception e) {
                        filesize = 0;
                    }
                    dos.add(new DownObj(ext, filesize, format, url, title));
                }
                break;
            case 2:
                break;
            default:
                break;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dict.callAttr("get", "dltype").toJava(int.class) != 0) {


                    RecyclerView recyclerView = findViewById(R.id.recycle_view);
                    recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1));
                    DownObjAdapter adapter = new DownObjAdapter(getApplicationContext(), dos);
                    recyclerView.setAdapter(adapter);
                    ItemClickSupport.addTo(recyclerView).setOnItemClickListener(new ItemClickSupport.OnItemClickListener() {
                        @Override
                        public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                            DownObj downobj = dos.get(position);
                            down(downobj.url, downobj.name);
                        }
                    });
                }
                findViewById(R.id.wait).setVisibility(View.INVISIBLE);

            }
        });
    }
}
