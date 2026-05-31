package com.techno.holomdnotes;

import java.util.Comparator;

public class LastModifiedComparator implements Comparator<FileMeta> {
    @Override
    public int compare(FileMeta a, FileMeta b) {
        if (a.timestamp > b.timestamp) return -1;
        if (b.timestamp < a.timestamp) return 1;
        return 0;
    }
}
