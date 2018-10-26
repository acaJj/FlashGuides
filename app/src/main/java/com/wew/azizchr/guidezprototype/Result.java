package com.wew.azizchr.guidezprototype;

public class Result {

    public String title;
    public String name;
    public String date;

    public Result() {
        title = "unknown";
        name = "unknown";
        date = "unknown";
    }


    public Result(String title, String name, String date) {
        this.title = title;
        this.name = name;
        this.date = date;
    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
