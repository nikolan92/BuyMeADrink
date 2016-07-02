package com.project.mosis.buymeadrink.DataLayer;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.project.mosis.buymeadrink.DataLayer.DataObject.Question;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.Utils.Constants;
import com.project.mosis.buymeadrink.Utils.VolleyHelperSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class QuestionHandler {

    private final String LOG_TAG = "QuestionHandler";
    private VolleyHelperSingleton mVolleyHelper;

    public QuestionHandler(Context context){
        mVolleyHelper = VolleyHelperSingleton.getInstance(context);
    }

    //Return all question from database if exist
    public void getQuestion(String questionID, String tag, final VolleyCallBack volleyCallBack){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.QUESTION_URL+"/"+questionID, null, new Response.Listener<JSONObject>() {
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
        });
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }
    public void addQuestion(Question question,String tag, final VolleyCallBack volleyCallBack){
        JSONObject jsonData = null;
        try {
            jsonData = new JSONObject(new Gson().toJson(question));
        }catch (JSONException exception){
            Log.e(LOG_TAG,exception.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.QUESTION_URL, jsonData, new Response.Listener<JSONObject>() {
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
        });
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }

    public void getAllQuestions(String tag,final VolleyCallBack volleyCallBack){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.QUESTION_URL, null, new Response.Listener<JSONObject>() {
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
        });
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }

    public void getAllAvailableQuestionsFromServer(int range){
        // range je neki odredjeni krug npr 100m
    }
    public void cancelAllRequestWithTag(String tag){
        mVolleyHelper.cancelPendingRequests(tag);
        //TODO: When activity call onStop() this function must be called because if you not call this fun Volley will call your handler and app will crash
    }
}
