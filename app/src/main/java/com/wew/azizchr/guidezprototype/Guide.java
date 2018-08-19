package com.wew.azizchr.guidezprototype;

/**
 * Created by Jeffrey on 2018-06-28.
 * This class holds data about the guide itself such as title, description, author, etc
 */

public class Guide {
    private String key;
    private String id;
    private String title;
    private String author;
    private String description;
    private String dateCreated;

    public Guide(){}

    public Guide(String title){
        this.title = title;
    }

    public Guide(String id, String title, String author, String description, String dateCreated) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.dateCreated = dateCreated;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }
}
