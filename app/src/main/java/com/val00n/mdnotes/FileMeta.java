package com.val00n.mdnotes;

public class FileMeta {
    public String fileName; // should be without extension
    public long timestamp;
    public String extension;

    public FileMeta(String fileName, long timestamp) {
        this.timestamp = timestamp;
        extension = fileName.substring(fileName.lastIndexOf('.'));
        this.fileName = fileName.substring(0, fileName.lastIndexOf('.'));
    }
}
