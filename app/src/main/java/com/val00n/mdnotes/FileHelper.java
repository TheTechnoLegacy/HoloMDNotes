package com.val00n.mdnotes;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FileHelper {
    Context context;

    public FileHelper(Context context) {
        this.context = context;
    }

    public ArrayList<FileMeta> getAvailableNotesList() {
        File folder = new File(getVaultDirectoryPath());
        if (!folder.exists()) {
            return null;
        }

        File[] files = folder.listFiles();
        if (files == null) {
            return null;
        }

        ArrayList<FileMeta> fileMetas = new ArrayList<FileMeta>();
        FileMeta fileMeta;
        File file;
        String fileName;
        for (int i = 0; i < files.length; i++) {
            fileName = files[i].getName();
            if (fileName.endsWith(".md") || fileName.endsWith(".txt") || fileName.endsWith(".html")) {
                file = new File(folder, fileName);
                fileMeta = new FileMeta(files[i].getName(), file.lastModified());
                fileMetas.add(fileMeta);
            }
        }

        return fileMetas;
    }

    public boolean saveFile(Note note, String extension) {
        File folder = new File(getVaultDirectoryPath());
        File file = new File(folder, note.title + extension);
        String fullFileText = note.content;

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        OutputStreamWriter outputStreamWriter = null;
        try {
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            outputStreamWriter.write(fullFileText);
            outputStreamWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (outputStreamWriter != null) {
                try { outputStreamWriter.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }

        return true;
    }

    public Uri getFileUri(Note note, String extension) {
        File folder = new File(getVaultDirectoryPath());
        File file = new File(folder, note.title + extension);
        return Uri.fromFile(file);
    }

    public boolean isFileExist(String fileName) {
        File file = new File(getVaultDirectoryPath(), fileName);
        if (file.exists()) {
            return true;
        }
        return false;
    }

    public Note loadFile(String fileName) {
        File file = new File(getVaultDirectoryPath(), fileName);
        String fullFileText = null;
        long timestamp = file.lastModified();

        FileInputStream input = null;
        BufferedReader reader = null;
        InputStreamReader inputStreamReader = null;
        try {
            input = new FileInputStream(file);
            inputStreamReader = new InputStreamReader(input, "UTF-8");

            reader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }

            fullFileText = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (reader != null) {
                try { reader.close(); } catch (IOException e) { e.printStackTrace(); }
            }
        }

        String title = fileName;
        if (fileName.endsWith(".md")) {
            title = fileName.substring(0, fileName.length() - 3);
        } else if (fileName.endsWith(".txt")) {
            title = fileName.substring(0, fileName.length() - 4);
        } else if (fileName.endsWith(".html")) {
            title = fileName.substring(0, fileName.length() - 5);
        }

        Note note = new Note(title, fullFileText, timestamp);
        return note;
    }

    public boolean deleteFile(String fileName) {
        File file = new File(getVaultDirectoryPath(), fileName);
        if (file.exists()) {
            return file.delete();
        }

        return false;
    }

    private String getVaultDirectoryPath() {
        String baseDir;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            baseDir = Environment.getExternalStorageDirectory().getPath();
        } else {
            baseDir = context.getFilesDir().getPath();
        }

        return baseDir + "/MDNotes/";
    }
}