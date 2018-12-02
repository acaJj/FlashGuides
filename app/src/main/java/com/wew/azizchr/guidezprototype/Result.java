package com.wew.azizchr.guidezprototype;


/**
 * Both Chris and Jeffrey have worked on this
 *
 * Chris was responsible for: Starting the Result model
 * Jeff was responsible for: Adding the needed variables to make his part work
 *
 * NOTE: DO NOT PASS IN NULL VARIABLES, EVERY SETTER HAS A CHECK FOR IT
 */

public class Result {

    //validate the setDestination method
    private final static String EDIT_DEST = "Edit";
    private final static String VIEW_DEST = "View";

    private String title;
    private String name;
    private String date;
    private String id;
    private String userId;//id of the user who created the guide, used only for searches
    private String key;
    private String destination;//where the result will take you(eg. Edit for createGuide, View for ViewGuide)

    public Result() {
        title = "unknown";
        name = "unknown";
        date = "unknown";
    }


    public Result(String title, String name, String date) {
        if(title == null || name == null || date == null) return;//do not pass in null variables

        if (title.equals("")){
            this.title = "unknown";
        }else{
            this.title = title;
        }

        if (name.equals("")){
            this.name = "unknown";
        }else{
            this.name = name;
        }

        if (date.equals("")){
            this.date = "unknown";
        }else{
            this.date = date;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null) return;

        if (!title.equals("")){
            this.title = title;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) return;

        if (!name.equals("")){
            this.name = name;
        }
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        if (date != null && !date.equals("")){
            this.date = date;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null) return;

        if (!id.equals("")){
            this.id = id;
        }
    }

    public void setUserId(String id) {
        if (id == null)return;

        if (!id.equals("")){
            this.userId = id;
        }
    }

    public String getUserId() {
        return userId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        if (key == null)return;

        if (!key.equals("")){
            this.key = key;
        }
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        if (destination == null)return;

        if (destination.equals(EDIT_DEST) || destination.equals(VIEW_DEST)){
            this.destination = destination;
        }
    }
}
