package com.project.mosis.buymeadrink.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;

import java.util.ArrayList;

public class LocationService extends Service {
    public LocationService() {
    }

    private Handler mHandlerSendCurrentLocation;
    private SendCurrentLocationRunnable sendCurrentLocationRunnable = null;
    private String user_id = "";
    private ArrayList<String> friends;
    private RequestQueue mRequestQueue;
    private boolean isBinded = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this,"Service onCreate.",Toast.LENGTH_SHORT).show();
        User user = SaveSharedPreference.GetUser(this);

        mHandlerSendCurrentLocation = new Handler();
        sendCurrentLocationRunnable = new SendCurrentLocationRunnable();


        //this shouldn’t ever happen, because mainActivity start this service and if mainActivity is started user must be seted
        //but if this happen for some strange reason this will prevent (ANR)
        if(user!=null){
            friends = user.getFriends();
            user_id = user.getId();
        }
        else {
            Toast.makeText(this,"Service error:User not set!",Toast.LENGTH_SHORT).show();
            stopSelf();
        }
        //Start thread for sending location to the server
        mHandlerSendCurrentLocation.post(sendCurrentLocationRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Toast.makeText(this,"Service onBind.",Toast.LENGTH_SHORT).show();
        isBinded = true;
        return null;
//        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        isBinded=false;
        Toast.makeText(this,"Service onUnbind.",Toast.LENGTH_LONG).show();

        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this,"Service onStartComand.",Toast.LENGTH_LONG).show();
        return super.onStartCommand(intent, flags, startId);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(sendCurrentLocationRunnable!=null)//this also shouldn’t ever happen, but i add this for testing...
            mHandlerSendCurrentLocation.removeCallbacks(sendCurrentLocationRunnable);
        else
            Toast.makeText(this,"Service sendCurrentLocationRunnable is null.",Toast.LENGTH_LONG).show();

        Toast.makeText(this,"Service destroyed.",Toast.LENGTH_SHORT).show();
    }

    //Inner Runnable classes
    /**
     *<p>This class implements Runnable and run in background periodically on every one minute for example.</p>
     *<p>Task: Send current userLocation and receive all friends location and will receive nearby places if exist.</p>
    **/
    private class SendCurrentLocationRunnable implements Runnable{

        @Override
        public void run() {
            Toast.makeText(LocationService.this,"User id is:" + LocationService.this.user_id,Toast.LENGTH_SHORT).show();
            mHandlerSendCurrentLocation.postDelayed(this,10000);
        }
    }
}

