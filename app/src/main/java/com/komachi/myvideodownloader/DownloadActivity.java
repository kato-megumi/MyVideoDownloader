package com.komachi.myvideodownloader;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;
import java.util.List;

public class DownloadActivity extends AppCompatActivity {
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
            public void run() {
                bg();
            }
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
                int len = list.callAttr("__len__").toJava(int.class);
                for (int i = 0; i < len; i++) {
                    PyObject down = list.callAttr("pop");
                    String ext = down.callAttr("get", "ext").toString();
                    String format = down.callAttr("get", "format").toString();
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
                            if (downobj.format.contains("audio")){
                                createAudioDialog(downobj.url, downobj.name);
                            } else {
                            createVideoDialog(downobj.url, downobj.name);}
                        }
                    });
                }
                findViewById(R.id.wait).setVisibility(View.INVISIBLE);

            }
        });
    }
    void createAudioDialog(final String url, final String name){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.audio_dialog);
        dialog.setTitle("Choose video option...");
        final CheckBox cbConvert = dialog.findViewById(R.id.cbConvert3);
        dialog.findViewById(R.id.btAudioOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> para = new ArrayList<String>();
                if (cbConvert.isChecked()) para.add("mp3");
                IntentServiceDownload.start(getApplicationContext(),url,name,para);
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btAudioCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    void createVideoDialog(final String url, final String name) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.video_dialog);
        dialog.setTitle("Choose video option...");
        final CheckBox cbConvert = dialog.findViewById(R.id.cbConvert);
        final CheckBox cbSub = dialog.findViewById(R.id.cbSubtitle);
        final CheckBox cbJA = dialog.findViewById(R.id.cbSubJA);
        final CheckBox cbVI = dialog.findViewById(R.id.cbSubVI);
        final CheckBox cbEN = dialog.findViewById(R.id.cbSubEN);
        final RadioGroup rg = dialog.findViewById(R.id.rgVideo);
        final LinearLayout llSub = dialog.findViewById(R.id.llSub);
        rg.setVisibility(View.INVISIBLE);
        llSub.setVisibility(View.INVISIBLE);
        cbConvert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbConvert.isChecked()) rg.setVisibility(View.VISIBLE);
                else rg.setVisibility(View.INVISIBLE);
            }
        });
        cbSub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cbSub.isChecked()) llSub.setVisibility(View.VISIBLE);
                else llSub.setVisibility(View.INVISIBLE);
            }
        });
        dialog.findViewById(R.id.btVideoOK).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), IntentServiceFFmeg.class);
                ArrayList<String> para = new ArrayList<String>();
                if (cbConvert.isChecked()) {
                    if (cbEN.isChecked()) para.add("en");
                    if (cbJA.isChecked()) para.add("ja");
                    if (cbVI.isChecked()) para.add("vi");
                }
                if (cbSub.isChecked()) {
                    switch (rg.getCheckedRadioButtonId()) {
                        case R.id.rbMp4:
                            para.add("mp4");
                            break;
                        case R.id.rbMkv:
                            para.add("mkv");
                            break;
                    }
                }
                IntentServiceDownload.start(getBaseContext(),url,name,para);
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btVideoCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
