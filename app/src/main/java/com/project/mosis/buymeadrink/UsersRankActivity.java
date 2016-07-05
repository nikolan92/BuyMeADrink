package com.project.mosis.buymeadrink;

import android.app.ProgressDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
    final String LOG_TAG = "UsersRankActivity";
    private UserHandler userHandler;
    private CoordinatorLayout coordinatorLayout;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_rank);
        Toolbar toolbar = (Toolbar) findViewById(R.id.user_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.user_rank_coordinator_layout);

        userHandler = new UserHandler(this);
        userHandler.getAllUsers(REQUEST_TAG,new GetAllUsersListener(UsersRankActivity.this));
        progressDialog = ProgressDialog.show(this,"Please wait","Waiting for data from the server...",true,true);
    }


    /**
     * This function is called when server return all users*/
    private void usersAreReady(ArrayList<User> users){
        UsersRankArrayAdapter adapter = new UsersRankArrayAdapter(this, users);
        ListView lv =(ListView) findViewById(R.id.users_rank_list_view);
        lv.setAdapter(adapter);
    }

    //Request to the server, and handle result...
    /**
     *This function will do some job after request is successful.
     * */
    public void onSuccess(JSONObject result) {
        progressDialog.dismiss();
        ArrayList<User> users = new ArrayList<>();
        try {
            if(result.getBoolean("Success"))
            {
                JSONArray usersInJSON = result.getJSONArray("Data");
                for(int i=0;i<usersInJSON.length();i++){
                    users.add(new Gson().fromJson(usersInJSON.getJSONObject(i).toString(),User.class));
                }
            }else {
                Snackbar.make(coordinatorLayout,result.getString("Error"),Snackbar.LENGTH_LONG).show();
                Log.e(LOG_TAG,result.getString("Error"));
            }
        }catch (JSONException exception){
            Log.e(LOG_TAG,exception.toString());
        }
        //Toast.makeText(this, result.toString(),Toast.LENGTH_SHORT).show();
        usersAreReady(users);
    }
    /**
     *This function will do some job if request is unsuccessful.
     * */
    public void onFailure(String error){
        progressDialog.dismiss();
        Snackbar.make(coordinatorLayout,"Something goes wrong, try again later.",Snackbar.LENGTH_LONG).show();
    }
    /**
     * Static inner classes do not hold an implicit reference to their outer classes, so activity will not be leaked.
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
