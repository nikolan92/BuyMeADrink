package com.project.mosis.buymeadrink.Application;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;

public class SaveSharedPreference {
    static final String PREF_USER_OBJECT = "user_data";
    static final String PREF_SERVICE_SETTINGS = "service_settings";

    static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    public static void SetUser(Context context, User user){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_USER_OBJECT,new Gson().toJson(user));//convert user object in JSON
        editor.apply();
    }
    public static void clearUser(Context context){
        getSharedPreferences(context).edit().remove(PREF_USER_OBJECT).commit();
    }
    public static User GetUser(Context context){
        String userInJSON = getSharedPreferences(context).getString(PREF_USER_OBJECT,null);
        if(userInJSON != null)
            return new Gson().fromJson(userInJSON, User.class);//make user object from JSON
        return null;
    }

    public static void SetServiceSettings(Context context,boolean serviceSettings){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(PREF_SERVICE_SETTINGS,serviceSettings).apply();
    }
    /**
     * Return true if settings have not yet been set, which is actually default value.
     * */
    public static boolean getServiceSettings(Context context){
        return getSharedPreferences(context).getBoolean(PREF_SERVICE_SETTINGS,true);
    }
}
