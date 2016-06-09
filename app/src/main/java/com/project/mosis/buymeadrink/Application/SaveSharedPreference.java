package com.project.mosis.buymeadrink.Application;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SaveSharedPreference {
    static final String PREF_USER_ID = "id";


    static SharedPreferences getSharedPreferences(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
    public static void SetUserId(Context context,String id){
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(PREF_USER_ID,id);
        editor.commit();
    }
    public static String GetUserId(Context context){
        return getSharedPreferences(context).getString(PREF_USER_ID,"");
    }
}
