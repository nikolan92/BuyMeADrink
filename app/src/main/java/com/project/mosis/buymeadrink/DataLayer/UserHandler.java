package com.project.mosis.buymeadrink.DataLayer;

import com.project.mosis.buymeadrink.DataLayer.DataObject.User;

public class UserHandler {

    private User user = null;

    public UserHandler(User user){
        this.user = user;
    }
    public boolean LogIn(){

        //TODO: Add code for log in (Requset to server with Volley)
        return true;
    }
    public boolean LogOut(){

        //TODO: Add code for log out (Requset to server with Volley)
        return true;
    }
    public String Register(){

        //TODO: Add code for Register (Requset to server with Volley)
        return "Some response from server";
    }
    public boolean UpdateInfo(){
        //TODO: Add code for UpdateInfo (Requset to server with Volley)
        return true;
    }
    public boolean ReiseRank(){
        //TODO: Add code for RaiseRank (Requset to server with Volley)
        return true;
    }
    public boolean getFriends(){
        //TODO: Add code for getFriends (Requset to server with Volley)
        return true;
    }

    public User getUser(){
        return this.user;
    }
}
