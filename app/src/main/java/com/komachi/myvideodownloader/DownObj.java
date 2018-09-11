package com.komachi.myvideodownloader;

public class DownObj {
    public String ext;
    public int filesize;
    public String format;
    public  String url;
    DownObj(String ext,int filesize, String format, String url){
        this.ext =ext;
        this.filesize=filesize;
        this.format=format;
        this.url = url;
    }
}
