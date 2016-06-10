package com.project.mosis.buymeadrink.DataLayer;

import android.content.Context;

import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.Utils.VolleyHelperSingleton;

public class UserHandler {

    private User user = null;
    private VolleyHelperSingleton mVolleyHelper;
    public UserHandler(Context context, User user){

        this.user = user;
        this.mVolleyHelper = VolleyHelperSingleton.getInstance(context);
    }
    public boolean LogIn(String Tag){

        //TODO: Add code for log in (Requset to server with Volley)

        return true;
    }
    public boolean LogOut(String Tag){

        //TODO: Add code for log out (Requset to server with Volley)
        return true;
    }
    public String Register(String Tag){

        //TODO: Add code for Register (Requset to server with Volley)
        return "Some response from server";
    }
    public boolean UpdateInfo(String Tag){
        //TODO: Add code for UpdateInfo (Requset to server with Volley)
        return true;
    }
    public boolean ReiseRank(String Tag){
        //TODO: Add code for RaiseRank (Requset to server with Volley)
        return true;
    }
    public boolean GetFriends(String Tag){
        //TODO: Add code for getFriends (Requset to server with Volley)
        return true;
    }

    public User GetUser(){
        return this.user;
    }

    public void CancelAllRequest(String Tag){
        //TODO: When activity call onStop() this function must be caled because if you not call this fun Volley will call your handler and app will crash
    }
}
