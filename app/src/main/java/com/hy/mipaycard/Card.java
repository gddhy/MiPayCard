package com.hy.mipaycard;

import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;

public class Card {
    private String name;
    private Uri uri;
    private File imageFile;
    private boolean isFile;

    public Card(String name, File imageFile) {
        this.name = name;
        this.imageFile = imageFile;
        this.isFile = true;
    }

    public Card(String name,Uri uri){
        this.name = name;
        this.uri = uri;
        this.isFile =false;
    }

    public Card(DocumentFile documentFile){
        String fileName = documentFile.getName();
        assert fileName != null;
        if(fileName.contains(".")){
            fileName = fileName.substring(0,fileName.lastIndexOf("."));
        }
        this.name = fileName;
        this.uri = documentFile.getUri();
        this.isFile =false;
    }

    public Card(File imageFile) {
        String fileName = imageFile.getName();
        if(fileName.contains(".")){
            fileName = fileName.substring(0,fileName.lastIndexOf("."));
        }
        this.name = fileName;
        this.imageFile = imageFile;
        this.isFile = true;
    }

    public String getName() {
        return name;
    }

    public File getImageFile() {
        return imageFile;
    }

    public Uri getUri() {
        return uri;
    }

    public boolean isFile() {
        return isFile;
    }
}
