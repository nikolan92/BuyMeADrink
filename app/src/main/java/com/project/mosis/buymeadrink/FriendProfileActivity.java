package com.project.mosis.buymeadrink;

import android.app.ProgressDialog;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import com.google.gson.Gson;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.android.volley.toolbox.NetworkImageView;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;
import android.widget.TextView;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class FriendProfileActivity extends AppCompatActivity {

    private final String REQUEST_TAG = "FriendProfileActivity";
    private final String LOG_TAG = "FriendProfileActivity";
    private UserHandler userHandler;
    //Layout
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private CoordinatorLayout coordinatorLayout;
    private TextView emailInput,rationInput;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.friend_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.friend_profile_toolbar_layout);
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.friend_profile_coordinator_layout);

        emailInput = (TextView)findViewById(R.id.friend_profile_email);
        rationInput = (TextView)findViewById(R.id.friend_profile_rating);

        NetworkImageView friendImage = (NetworkImageView) findViewById(R.id.friend_profile_imageView);

        collapsingToolbarLayout.setTitle("No data");

        Bundle bundle = getIntent().getExtras();

        if(bundle!=null) {
            String friendID = bundle.getString("friendID");

            userHandler = new UserHandler(this);
            userHandler.getUserImage(friendID, friendImage);
            userHandler.getUser(friendID,REQUEST_TAG,new OnFriendDataReady(this));
            progressDialog = ProgressDialog.show(this,"Please wait","Waiting for data from the server...",true,true);
        }
    }
    private void onFriendDataReady(JSONObject friendInJson){
        progressDialog.dismiss();
        try {
            if (friendInJson.getBoolean("Success")) {
                User friend = new Gson().fromJson(friendInJson.getString("Data"),User.class);
                emailInput.setText(friend.getEmail());
                rationInput.setText(String.valueOf(friend.getRating()));
                collapsingToolbarLayout.setTitle(friend.getName());
            }else{
                Log.e(LOG_TAG,friendInJson.getString("Error"));
                Snackbar.make(coordinatorLayout,"Something goes wrong try again later.",Snackbar.LENGTH_LONG).show();
            }
        }catch (JSONException exception){
            Log.e(LOG_TAG,exception.toString());
            Snackbar.make(coordinatorLayout,"Something goes wrong try again later.",Snackbar.LENGTH_LONG).show();
        }
    }

    private static class OnFriendDataReady implements VolleyCallBack {

        private final WeakReference<FriendProfileActivity> mActivity;
        OnFriendDataReady(FriendProfileActivity friendProfileActivity){
            mActivity = new WeakReference<>(friendProfileActivity);
        }
        @Override
        public void onSuccess(JSONObject result) {
            FriendProfileActivity friendProfileActivity = mActivity.get();
            if(friendProfileActivity!=null)//If activity still exist then do some job, if not just return;
                friendProfileActivity.onFriendDataReady(result);
        }

        @Override
        public void onFailed(String error) {
            FriendProfileActivity friendProfileActivity = mActivity.get();
            if (friendProfileActivity != null)//If activity still exist then do some job, if not just return;
            {
                friendProfileActivity.progressDialog.dismiss();
                Log.e(friendProfileActivity.LOG_TAG, error);
            }
        }
    }

}
