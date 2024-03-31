package com.mdkefir.filmlibrary.models;

public class Series {
    public String title;
    public String imagePath;

    public Series(String title, String imagePath) {
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
