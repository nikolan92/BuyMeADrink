package com.project.mosis.buymeadrink.DataLayer.EventListeners;


import org.json.JSONObject;

public interface VolleyCallBack {
    void onSuccess(JSONObject result);

    void onFailed(String error);
}