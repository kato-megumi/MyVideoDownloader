package com.komachi.myvideodownloader;

public class DownObj {
    public String ext;
    public int filesize;
    public String format;
    public  String url;
    public String name;
    DownObj(String ext,int filesize, String format, String url, String name){
        this.ext =ext;
        this.filesize=filesize;
        this.format=format;
        this.url = url;
        this.name = name+"."+ext;
    }

}
