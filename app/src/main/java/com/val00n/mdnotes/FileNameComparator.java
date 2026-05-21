package com.val00n.mdnotes;

import java.util.Comparator;

public class FileNameComparator implements Comparator<FileMeta> {
    @Override
    public int compare(FileMeta a, FileMeta b) {
        return a.fileName.compareTo(b.fileName);
    }
}

