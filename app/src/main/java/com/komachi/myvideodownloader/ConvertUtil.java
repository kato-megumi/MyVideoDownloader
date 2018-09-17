package com.komachi.myvideodownloader;

import android.util.Log;

public class ConvertUtil {
    public static boolean addSubtitle(String vPath, String sPath, String fPath) {
        Runtime rt = Runtime.getRuntime();
        int a = vPath.lastIndexOf('.');
        String newPath = vPath.substring(0,a)+"_"+vPath.substring(a,vPath.length());
        String[] cmd = {fPath, "-i", vPath, "-i", sPath, "-map", "0:v",
                "-map", "0:a", "-map", "1:s", "-c", "copy", "-y",newPath};
        try {
            rt.exec(cmd);
        } catch (Exception e) {
            Log.e("e", e.toString());
        }
        return false;
    }
    public static boolean webmToMp3(String aPath, String fPath){
        return true;
    }
}
