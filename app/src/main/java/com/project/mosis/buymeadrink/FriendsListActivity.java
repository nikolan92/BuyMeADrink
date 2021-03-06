package com.project.mosis.buymeadrink;

import android.app.ProgressDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ListView;

import com.google.gson.Gson;
import com.project.mosis.buymeadrink.Adapters.FriendsListArrayAdapter;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class FriendsListActivity extends AppCompatActivity {
    final String REQUEST_TAG = "FriendsListActivity";
    final String LOG_TAG = "FriendsListActivity";
    private UserHandler userHandler;
    private CoordinatorLayout coordinatorLayout;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.friend_list_coordinator_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.friend_list_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        if(bundle!=null) {
            String userId = bundle.getString("userID");
            userHandler = new UserHandler(this);
            userHandler.getUserFriends(userId,REQUEST_TAG, new GetAllUsersListener(FriendsListActivity.this));
            progressDialog = ProgressDialog.show(this,"Please wait","Waiting for data from the server...",true,true);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        userHandler.cancelAllRequestWithTag(REQUEST_TAG);
    }
    private void friendsAreReady(ArrayList<User> users) {
        FriendsListArrayAdapter adapter = new FriendsListArrayAdapter(this, users);
        ListView lv = (ListView) findViewById(R.id.friend_list_list_view);
        lv.setAdapter(adapter);
    }
    public void onSuccess(JSONObject result) {
        progressDialog.dismiss();
        ArrayList<User> friends = new ArrayList<>();
        try {
            if(result.getBoolean("Success")) {
                JSONArray usersInJSON = result.getJSONArray("Data");
                for(int i=0; i<usersInJSON.length(); i++) {
                    friends.add(new Gson().fromJson(usersInJSON.getJSONObject(i).toString(), User.class));
                }
            } else {
                Snackbar.make(coordinatorLayout,"Something goes wrong,try again later.",Snackbar.LENGTH_LONG).show();
                Log.e(LOG_TAG,result.getString("Error"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.toString());
        }
        friendsAreReady(friends);
    }

    public void onFailure(String error) {
        progressDialog.dismiss();
        Snackbar.make(coordinatorLayout,"Something goes wrong,try again later.",Snackbar.LENGTH_LONG).show();
        Log.e(LOG_TAG,error);
    }
    public static class GetAllUsersListener implements VolleyCallBack {

        private final WeakReference<FriendsListActivity> mActivity;
        GetAllUsersListener(FriendsListActivity friendsListActivity) {
            mActivity = new WeakReference<>(friendsListActivity);
        }
        @Override
        public void onSuccess(JSONObject result) {
            FriendsListActivity friendsListActivity = mActivity.get();
            if(friendsListActivity != null){
                friendsListActivity.onSuccess(result);
            }
        }
        @Override
        public void onFailed(String error) {
            FriendsListActivity friendsListActivity = mActivity.get();
            if(friendsListActivity != null) {
                friendsListActivity.onFailure(error);
            }
        }
    }

}
