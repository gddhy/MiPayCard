package com.hy.mipaycard;

import java.io.File;

public class Card {
    private String name;

    private File imageFile;

    public Card(String name, File imageFile) {
        this.name = name;
        this.imageFile = imageFile;
    }

    public Card(File imageFile) {
        String fileName = imageFile.getName();
        if(fileName.contains(".")){
            fileName = fileName.substring(0,fileName.lastIndexOf("."));
        }
        this.name = fileName;
        this.imageFile = imageFile;
    }

    public String getName() {
        return name;
    }

    public File getImageFile() {
        return imageFile;
    }
}
