package com.project.mosis.buymeadrink;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;

import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class UsersRankActivity extends AppCompatActivity {

    final String REQUSET_TAG = "UsersRankActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_rank);

        final Button t = (Button) findViewById(R.id.testbtn);
        assert t!=null;
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserHandler.getAllUsers(UsersRankActivity.this,REQUSET_TAG,new GetAllUsersListener(UsersRankActivity.this));
            }
        });
    }


    /**
     *This function will do some job after request is successful.
     * */
    public void onLogIn(JSONObject result){
        Toast.makeText(UsersRankActivity.this, result.toString(),Toast.LENGTH_SHORT).show();
    }
    /**
     *This function will do some job if request is unsuccessful.
     * */
    public void onLogInFailure(String error){
        Toast.makeText(UsersRankActivity.this, "Error occur:\n"+ error.toString(),Toast.LENGTH_SHORT).show();
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
            mActivity = new WeakReference<UsersRankActivity>(usersRankActivity);
        }
        @Override
        public void onSuccess(JSONObject result) {
            UsersRankActivity usersRankActivity = mActivity.get();
            if(usersRankActivity!=null)//If activity still exist then do some job, if not just return;
                usersRankActivity.onLogIn(result);
        }

        @Override
        public void onFailed(String error) {
            UsersRankActivity usersRankActivity = mActivity.get();
            if(usersRankActivity!=null)//If activity still exist then do some job, if not just return;
                usersRankActivity.onLogInFailure(error);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        UserHandler.CancelAllRequestWithTagStatic(this,REQUSET_TAG);
    }
}
