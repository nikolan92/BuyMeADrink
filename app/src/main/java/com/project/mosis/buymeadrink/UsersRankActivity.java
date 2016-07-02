package com.project.mosis.buymeadrink;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.project.mosis.buymeadrink.Adapters.UsersRankArrayAdapter;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class UsersRankActivity extends AppCompatActivity {

    final String REQUEST_TAG = "UsersRankActivity";
    private UserHandler userHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_rank);
        Toolbar toolbar = (Toolbar) findViewById(R.id.user_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userHandler = new UserHandler(this);
        userHandler.getAllUsers(REQUEST_TAG,new GetAllUsersListener(UsersRankActivity.this));
    }


    /**
     * This function is called when server return all users*/
    private void usersAreReady(ArrayList<User> users){
        UsersRankArrayAdapter adapter = new UsersRankArrayAdapter(this, users);
        ListView lv =(ListView) findViewById(R.id.listView2);
        lv.setAdapter(adapter);


        for(int i=0;i<users.size();i++) {
            Log.d("USERS", users.get(i).getName());
        }
    }

    //Request to the server, and handle result...
    /**
     *This function will do some job after request is successful.
     * */
    public void onSuccess(JSONObject result) {
        ArrayList<User> users = new ArrayList<>();
        try {
            if(result.getBoolean("Success"))
            {
                JSONArray usersInJSON = result.getJSONArray("Data");
                for(int i=0;i<usersInJSON.length();i++){
                    users.add(new Gson().fromJson(usersInJSON.getJSONObject(i).toString(),User.class));
                }
            }else {
                //{"Success":false,"Error":"No users in data base."}
            }
        }catch (JSONException exception){
            Log.e("UserRankActivity",exception.toString());
        }
        //Toast.makeText(this, result.toString(),Toast.LENGTH_SHORT).show();
        usersAreReady(users);

    }
    /**
     *This function will do some job if request is unsuccessful.
     * */
    public void onFailure(String error){
        Toast.makeText(this, "Error occur:\n"+ error.toString(),Toast.LENGTH_SHORT).show();
    }
    /**
     * Static inner classes do not hold an implicit reference to their outher clases, so activity will not be leaked.
     * Also because i need to access to an activity method i need to hold a reference to it. But i keep weakReference,
     * so GC will not be prevented from deleting it. Because of that i need to check whether activity still exist.
     * */
    private static class GetAllUsersListener implements VolleyCallBack {
        //TODO:Change MainActivity to activity
        private final WeakReference<UsersRankActivity> mActivity;
        GetAllUsersListener(UsersRankActivity usersRankActivity){
            mActivity = new WeakReference<>(usersRankActivity);
        }
        @Override
        public void onSuccess(JSONObject result) {
            UsersRankActivity usersRankActivity = mActivity.get();
            if(usersRankActivity!=null)//If activity still exist then do some job, if not just return;
                usersRankActivity.onSuccess(result);
        }

        @Override
        public void onFailed(String error) {
            UsersRankActivity usersRankActivity = mActivity.get();
            if(usersRankActivity!=null)//If activity still exist then do some job, if not just return;
                usersRankActivity.onFailure(error);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        userHandler.cancelAllRequestWithTag(REQUEST_TAG);
    }
}
