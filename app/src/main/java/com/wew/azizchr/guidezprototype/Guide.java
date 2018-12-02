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
    private boolean published;
    private String description;
    private String dateCreated;//should be stored as timestamp

    //Used when creating a new guide, empty except for its id
    public Guide(String id){
        if (id == null)return;

        if (!id.equals("")){
            this.id = id;
        }
    }

    //Used when loading up a guide for editing; we pass the id, key, and title from the UserCollectionActivity
    public Guide(String id, String key, String title){
        if (id == null || key == null || title == null)return;

        if (!id.equals("") && !key.equals("") && !title.equals("")){
            this.id = id;
            this.key = key;
            this.title = title;
        }
    }

    //another constructor, might be unnecessary,
    public Guide(String id, String title, String author, String description, String dateCreated) {
        if (id == null || title == null || author == null || description == null || dateCreated == null)return;

        if (!id.equals("") && !title.equals("") && !author.equals("") && !description.equals("") && !dateCreated.equals("")){
            this.id = id;
            this.title = title;
            this.author = author;
            this.description = description;
            this.dateCreated = dateCreated;
        }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null)return;

        if (!id.equals("")){
            this.id = id;
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == null)return;

        if (!title.equals("")){
            this.title = title;
        }
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        if (author == null)return;

        if (!author.equals("")){
            this.author = author;
        }

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null)return;

        if (!description.equals("")){
            this.description = description;
        }
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        if (dateCreated == null)return;

        this.dateCreated = dateCreated;
    }

    public boolean getPublishedStatus(){
        return published;
    }

    public void setPublishedStatus(boolean published){
        this.published = published;
    }
}
