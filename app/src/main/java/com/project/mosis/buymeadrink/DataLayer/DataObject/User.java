package com.project.mosis.buymeadrink.DataLayer.DataObject;


import java.util.ArrayList;
import java.util.List;

public class User {

    private String _id;
    private String name;
    private String email;
    private String password;
    private List<String> friends = null;

    public User(String id, String name, String email, String password,ArrayList<String> friends) {
        this._id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.friends = friends;
    }
    public User(String id, String name, String email, String password) {
        this._id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.friends = new ArrayList<>();
    }
    //this constructor is used when user going to login or register
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.friends = new ArrayList<>();
    }

    public String getId(){
        return this._id;
    }
    public String getName() {
        return this.name;
    }
    public String getEmail(){
        return this.email;
    }
    public String getPassword(){
        return this.password;
    }
    public ArrayList<String> getFriends(){return (ArrayList<String>) this.friends;}
    public void setId(String id){
        this._id = id;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setEmail(String email){
        this.email = email;
    }
    public void setPassword(String password){
        this.password = password;
    }
    public void addFriend(String id){
        if(this.friends!=null)
            this.friends.add(id);
        else {
            this.friends = new ArrayList<>();
            this.friends.add(id);
        }
    }

}
