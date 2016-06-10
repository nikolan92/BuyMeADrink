package com.project.mosis.buymeadrink.DataLayer;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.OnErrorListener;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.OnResponseListener;
import com.project.mosis.buymeadrink.Utils.VolleyHelperSingleton;

import org.json.JSONObject;

public class UserHandler {

    private User user = null;
    private VolleyHelperSingleton mVolleyHelper;
    //private OnResponseListener logInListener;


    public UserHandler(Context context, User user){

        this.user = user;
        this.mVolleyHelper = VolleyHelperSingleton.getInstance(context);
    }
    /**
     *login function will try to log in user with given username and password.
     *<p>
     * If login is successful then onResponseLisener will run otherwise onErrorListener will be triggered.
     *<p>
     * */
    public void logIn(String username , String password, String Tag, final OnResponseListener onResponseListener, final OnErrorListener onErrorListener){
        String url = "http://api.androidhive.info/volley/person_object.json";

        //this.onResponseListener = onResponseListener;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                onResponseListener.OnResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onErrorListener.OnError();
            }
        }
        );

        mVolleyHelper.addToRequestQueue(jsonObjectRequest);

        //TODO: Add code for log in (Requset to server with Volley)
    }
    public void logOut(String Tag){
        //Ovo nam nece ni treba sobzirom da status da li je korisnik ulogovan ili ne pamtimo u shared preferencis
        //TODO: Add code for log out (Requset to server with Volley)
    }
    public String register(String Tag){

        //TODO: Add code for Register (Requset to server with Volley)
        return "Some response from server";
    }
    public boolean updateInfo(String Tag){
        //TODO: Add code for UpdateInfo (Requset to server with Volley)
        return true;
    }
    public boolean reiseRank(String Tag){
        //TODO: Add code for RaiseRank (Requset to server with Volley)
        return true;
    }
    public boolean getFriends(String Tag){
        //TODO: Add code for getFriends (Requset to server with Volley)
        return true;
    }
    public void showMyFriends(){

    }
    public void sendMyLocation(LatLng currentLocation){

    }

    public User GetUser(){
        return this.user;
    }

    public void CancelAllRequestWithTag(String Tag){
        mVolleyHelper.cancelPendingRequests(tag);
        //TODO: When activity call onStop() this function must be caled because if you not call this fun Volley will call your handler and app will crash
    }
}
