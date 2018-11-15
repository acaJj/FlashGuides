package com.wew.azizchr.guidezprototype;

public class Result {

    public String title;
    public String name;
    public String date;
    public String id;
    public String key;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
