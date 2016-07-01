package com.project.mosis.buymeadrink.Application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.project.mosis.buymeadrink.DataLayer.DataObject.User;

//ova klasa dodaje user-a kao globalnu promenjivu kojoj moze da se pristupa iz bilo kog activitja u aplikaciji
//naravno vazi sve dok sistem ne ubije samu aplikaciju
//mislim da ce da nam treba da bi iz razlicitih aktivitija pristupali ulogovanom user-u
public class MyApplication extends Application{

    private User user=null;
    private boolean serviceSettings = true;

    public boolean getServiceSettings(){
        return serviceSettings;
    }
    public void setServiceSetings(boolean serviceSettings){
        this.serviceSettings = serviceSettings;
    }

    public User getUser(){
        return this.user;
    }
    public void setUser(User user){
        this.user = user;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
