package com.wew.azizchr.guidezprototype;

/**
 * Created by Jeffrey on 2018-05-27.
 * Models the user info
 */

public class User {
    private String id;
    private String userName;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private int numGuides;

    //in settings activity, we instantiate an empty object before filling values
    public User(){}

    //used in homepage and signupActivity
    public User(String id){
        if (id == null)return;

        if (!id.equals("")){
            this.id = id;
        }
    }

    /*not used anywhere, may use it later
    public User(String id,String userName, String firstName, String lastName, String email, String password, int numGuides) {
        this.id = id;
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.numGuides = numGuides;
    }*/

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (id == null)return;

        if (!id.equals("")){
            this.id = id;
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if (userName == null)return;

        if (!userName.equals("")){
            this.userName = userName;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null)return;

        if (!firstName.equals("")){
            this.firstName = firstName;
        }

    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null)return;

        if (!lastName.equals("")){
            this.lastName = lastName;
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null)return;

        if (!email.equals("")){
            this.email = email;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null)return;

        if (!password.equals("")){
            this.password = password;
        }
    }

    public int getNumGuides() {
        return numGuides;
    }

    public void setNumGuides(int numGuides) {
        if (numGuides >= 0){
            this.numGuides = numGuides;
        }
    }
}
