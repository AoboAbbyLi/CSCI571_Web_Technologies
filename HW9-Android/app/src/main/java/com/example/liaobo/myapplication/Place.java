package com.example.liaobo.myapplication;

public class Place {
    String id;
    String icon;
    String name;
    String address;
    boolean favorite;

    Place (String id, String icon, String name, String address, boolean favorite) {
        this.id = id;
        this.icon = icon;
        this.name = name;
        this.address = address;
        this.favorite = favorite;
    }
    public String getId() {
        return id;
    }
    public String getIcon() {
        return icon;
    }
    public String getName() {
        return name;
    }
    public String getAddress() {
        return address;
    }
    public String getFavorite() {
        return String.valueOf(favorite);
    }
}
