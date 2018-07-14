package com.wew.azizchr.guidezprototype;

/**
 * Created by Jeffrey on 2018-05-27.
 */

public class User {
    private String id;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private int numGuides;

    public User(){}

    public User(String id){
        this.id = id;
    }

    public User(String id,String userName, String firstName, String lastName, String email, String password, int numGuides) {
        this.id = id;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.numGuides = numGuides;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getNumGuides() {
        return numGuides;
    }

    public void setNumGuides(int numGuides) {
        this.numGuides = numGuides;
    }
}
