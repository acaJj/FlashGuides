package com.wew.azizchr.guidezprototype;


/**
 * Both Chris and Jeffrey have worked on this
 *
 * Chris was responsible for: Starting the Result model
 * Jeff was responsible for: Adding the needed variables to make his part work
 */

public class Result {

    public String title;
    public String name;
    public String date;
    public String id;
    public String userId;//id of the user who created the guide, used only for searches
    public String key;
    public String destination;//where the result will take you(eg. Edit for createGuide, View for ViewGuide)

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

    public void setUserId(String id) {
        this.userId = id;
    }

    public String getUserId() {
        return userId;
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

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}
