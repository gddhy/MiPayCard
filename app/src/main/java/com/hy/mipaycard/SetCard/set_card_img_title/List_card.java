package com.hy.mipaycard.SetCard.set_card_img_title;

import java.io.File;

public class List_card {
    private String name;
    private File imageFile;

    public List_card(String name,File imageFile){
        this.name = name;
        this.imageFile = imageFile;
    }

    public String getName() {
        return name;
    }

    public File getImageFile() {
        return imageFile;
    }
}
