package com.example.liaobo.myapplication;

public class Review {
    String name;
    int rating;
    String date;
    String content;
    String img;
    String url;

    Review(String name, String img, String url, int rating, String date, String content) {
        this.name = name;
        this.img = img;
        this.url = url;
        this.rating = rating;
        this.date = date;
        this.content = content;
    }
}
