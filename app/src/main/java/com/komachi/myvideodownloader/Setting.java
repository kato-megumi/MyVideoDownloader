package com.komachi.myvideodownloader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;

public class Setting extends AppCompatActivity {
    String ffStore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        String store = getSharedPreferences("settings", 0).getString("store","");
        ((TextView)findViewById(R.id.tvStore)).setText(store);
        ffStore = getExternalFilesDir("").getAbsolutePath()+"/Download/";
        File f1 = new File(ffStore +"ffmpeg");
        File f2 = new File(ffStore+"ffprobe");
        boolean g1 = f1.canExecute();
        boolean g2 = f2.canExecute();
        if (!g1){ g1 = f1.setExecutable(true,false); }
        if (!g2){ g2 = f2.setExecutable(true,false); }
        if (!g1 | !g2){
            f1.delete();f1.delete();
        } else {
            Button v = findViewById(R.id.ffmpeg);
            v.setEnabled(false);
            v.setClickable(false);
        }
    }
    public  void ffmpeg(View v){
        v.setEnabled(false);
        v.setClickable(false);
        IntentServiceFFmeg.start(this,ffStore);

    }
    public void select(View v){
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case 42:
                if (resultCode != RESULT_OK)
                    return;
                Uri treeUri = data.getData();
                grantUriPermission(getPackageName(), treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                String path = FileUtil.getFullPathFromTreeUri(treeUri,this);
                TextView textView = findViewById(R.id.tvStore);
                textView.setText(path);
                getSharedPreferences("settings", 0).edit()
                        .putString("uri",treeUri.toString())
                        .putString("store",path+"/").apply();

        }
    }
}
