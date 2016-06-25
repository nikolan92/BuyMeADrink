package com.project.mosis.buymeadrink.DataLayer;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.Utils.Constants;
import com.project.mosis.buymeadrink.Utils.VolleyHelperSingleton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class LocationHelper {
    private final String REPORT_LOCATION_REQUEST_TAG = "LocationHelper";
    private User user;
    private VolleyHelperSingleton mVolleyHelper;

    public LocationHelper(Context context, User user){
        this.user = user;
        this.mVolleyHelper = VolleyHelperSingleton.getInstance(context);
    }

    public void sendCurrentLocationAndReceiveNearbyPlaces(double lat, double lng,int range, final VolleyCallBack volleyCallBack){
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try{
            jsonObject.put("user_id",user.getId());
            jsonObject.put("lat",lat);
            jsonObject.put("lng",lng);
            jsonObject.put("range",range);

            for(int i=0;i<user.getFriends().size();i++)
                jsonArray.put(user.getFriends().get(i));

            jsonObject.put("friends",jsonArray);

        }catch (JSONException exception){
            Log.e("LocationHelper",exception.toString());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.UPDATE_LOCATION, jsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                volleyCallBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyCallBack.onFailed(error.toString());
            }
        });
        jsonObjectRequest.setTag(REPORT_LOCATION_REQUEST_TAG);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }

    public void cancelAllRequestWithTag(String tag){
        mVolleyHelper.cancelPendingRequests(REPORT_LOCATION_REQUEST_TAG);
        //TODO: When activity call onStop() this function must be caled because if you not call this fun Volley will call your handler and app will crash
    }
}
