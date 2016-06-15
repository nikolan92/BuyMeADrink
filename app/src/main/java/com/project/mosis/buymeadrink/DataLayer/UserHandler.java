package com.project.mosis.buymeadrink.DataLayer;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.Utils.Constants;
import com.project.mosis.buymeadrink.Utils.VolleyHelperSingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class UserHandler {

    private User user = null;
    private VolleyHelperSingleton mVolleyHelper;

    public UserHandler(Context context, User user){

        this.user = user;
        this.mVolleyHelper = VolleyHelperSingleton.getInstance(context);
    }
    public UserHandler(Context context){

        this.mVolleyHelper = VolleyHelperSingleton.getInstance(context);
    }
    /**
     *login function will try to log in user with given username and password.
     *<p>
     * If login is successful then onResponseLisener will run otherwise onErrorListener will be triggered.
     *<p>
     * */
    public static void logIn(Context context, String email , String password, String tag, final VolleyCallBack volleyCallBack){

        VolleyHelperSingleton mVolleyHelper = VolleyHelperSingleton.getInstance(context);
        JSONObject jsonData = new JSONObject();
        try{
            jsonData.put("email",email);
            jsonData.put("password",password);
        }catch (JSONException exception){
            Log.e("UserHandler",exception.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.LOG_IN_URL, jsonData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(volleyCallBack!=null)
                    volleyCallBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(volleyCallBack!=null)
                    volleyCallBack.onFailed(error.toString());
            }
        }
        );
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }
    public void logOut(String Tag){
        //Ovo nam nece ni treba sobzirom da status da li je korisnik ulogovan ili ne pamtimo u shared preferencis
        //TODO: Add code for log out (Requset to server with Volley)
    }
    public static void register(Context context, User user, String tag, final VolleyCallBack  volleyCallBack){

        VolleyHelperSingleton mVolleyHelper = VolleyHelperSingleton.getInstance(context);
        JSONObject jsonData= null;
        try {
            jsonData = new JSONObject(new Gson().toJson(user));
        }catch (JSONException exception){
            Log.e("UserHandler",exception.toString());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.USER_URL, jsonData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(volleyCallBack!=null)
                    volleyCallBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(volleyCallBack!=null)
                    volleyCallBack.onFailed(error.toString());
            }
        }
        );
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }
    public static void updateUserInfo(Context context, User user, String tag, final VolleyCallBack  volleyCallBack){
        VolleyHelperSingleton mVolleyHelper = VolleyHelperSingleton.getInstance(context);
        JSONObject jsonData= null;
        try {
            jsonData = new JSONObject(new Gson().toJson(user));
        }catch (JSONException exception){
            Log.e("UserHandler",exception.toString());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, Constants.USER_URL, jsonData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(volleyCallBack!=null)
                    volleyCallBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(volleyCallBack!=null)
                    volleyCallBack.onFailed(error.toString());
            }
        }
        );
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
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
    public void SetUser(User user){
         this.user = user;
    }
    public User GetUser(){
        return this.user;
    }

    public void CancelAllRequestWithTag(String tag){
        mVolleyHelper.cancelPendingRequests(tag);
        //TODO: When activity call onStop() this function must be caled because if you not call this fun Volley will call your handler and app will crash
    }
}
