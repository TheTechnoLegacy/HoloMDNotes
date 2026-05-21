package com.val00n.mdnotes;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Note {
    public String title;
    public String content;
    public long lastModified;

    public Note(String title, String content, long lastModified) {
        this.content = content;
        this.title = sanitizeString(title);
        this.lastModified = lastModified;
    }

    public Note(String title, String content) {
        this(title, content, 0);
    }

    public String getWholeNote() {
        return title + "\n" + content;
    }

    private static String sanitizeString(String input) {
        if (input == null) return "";

        String sanitized = input
                .replace('/', '_')
                .replace('\\', '_')
                .replace(':', '_')
                .replace('*', '_')
                .replace('?', '_')
                .replace('"', '_')
                .replace('\'', '_')
                .replace('<', '_')
                .replace('>', '_')
                .replace('|', '_')
                .replace('\n', '_')
                .replace('\r', '_')
                .replace('#', '_')
                .replace('$', '_')
                .replace('{', '_')
                .replace('}', '_');

        if (sanitized.length() > 128) {
            sanitized = sanitized.substring(0, 128);
        }

        return sanitized.trim();
    }
}
