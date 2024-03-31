package com.mdkefir.filmlibrary.models;

public class Movie {
    private String title;
    private String imagePath;

    public Movie(String title, String imagePath) {
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
