package com.project.mosis.buymeadrink.Application;

import android.app.Application;

import com.project.mosis.buymeadrink.DataLayer.DataObject.User;

//ova klasa dodaje user-a kao globalnu promenjivu kojoj moze da se pristupa iz bilo kog activitja u aplikaciji
//naravno vazi sve dok sistem ne ubije samu aplikaciju
//mislim da ce da nam treba da bi iz razlicitih aktivitija pristupali ulogovanom user-u
public class MyAplication extends Application{

    private User user=null;

    public User GetUser(){
        return this.user;
    }
    public void setUser(User user){
        this.user = user;
    }
}
