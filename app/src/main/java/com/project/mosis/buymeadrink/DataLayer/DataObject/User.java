package com.project.mosis.buymeadrink.DataLayer.DataObject;

import java.util.List;


public class User {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String imgUrl;
    private List<User> friends;

    //this constructor is used when user going to login or register
    public User(String id, String firstName, String lastName, String email, String password, String imgUrl, List<User> friends) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.imgUrl = imgUrl;
        this.friends = friends;
    }

    //this constructor is used when user sent request to the server for his friends
    public User(String id, String firstName, String lastName, String email, String imgUrl) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.imgUrl = imgUrl;
    }

    public String getName() {
        return this.firstName + " " + this.lastName;
    }
    public String getId(){
        return this.id;
    }
    public String getFirstName() {
        return this.firstName;
    }
    public String getEmail(){
        return this.email;
    }
    public String getImgUrl(){
        return this.imgUrl;
    }
    public String getPassword(){
        return this.password;
    }
    public void setId(String id){
        this.id = id;
    }
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }
    public void setLastName(String lastName){
        this.lastName = lastName;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public void setPassword(String password){
        this.password = password;
    }

}
