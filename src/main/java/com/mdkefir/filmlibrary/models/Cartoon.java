package com.mdkefir.filmlibrary.models;

public class Cartoon {
    public String title;
    public String imagePath;

    public Cartoon(String title, String imagePath) {
        this.title = title;
        this.imagePath = imagePath;
    }

    public String getTitle() {
        return title;
    }

    public String getImagePath() {
        return imagePath;
    }
}
