package com.mdkefir.filmlibrary.models;

public class Movie {
    private String title;
    private String year;
    private String rating;
    private String imagePath;

    public Movie(String title, String year, String rating, String imagePath) {
        this.title = title;
        this.year = year;
        this.rating = rating;
        this.imagePath = imagePath;
    }

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public String getRating() {
        return rating;
    }

    public String getImagePath() {
        return imagePath;
    }
}
