package com.project.mosis.buymeadrink.Service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
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

    public final static String UPDATE_MAP = "UPDATE_MAP_ACTION";
    public final static String FRIENDS_LOCATIONS = "FRIENDS_LOCATION";
    private Handler mHandlerSendCurrentLocation;
    private SendCurrentLocationRunnable sendCurrentLocationRunnable = null;
    private String user_id = "";
    private ArrayList<String> friends;
    private RequestQueue mRequestQueue;
    private int counter =0;

    private final IBinder mBinder = new MyBinder();
    private boolean isBind = false;
    @Override
    public void onCreate() {

        super.onCreate();
        Toast.makeText(this,"Service onCreate.",Toast.LENGTH_SHORT).show();
        User user = SaveSharedPreference.GetUser(this);

        mHandlerSendCurrentLocation = new Handler();
        sendCurrentLocationRunnable = new SendCurrentLocationRunnable();


        //this shouldn’t ever happen, because mainActivity start this service and if mainActivity is started user must be set
        //but if this happen for some strange reason this will prevent (ANR)
        if(user!=null){
            friends = user.getFriends();
            user_id = user.getId();
            //Start thread for sending location to the server
            mHandlerSendCurrentLocation.post(sendCurrentLocationRunnable);
        }
        else {
            Toast.makeText(this,"Service error:User not set!",Toast.LENGTH_SHORT).show();
            stopSelf();
        }

    }
    public void setCounter(int val){
        this.counter = val;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Toast.makeText(this,"Service onBind.",Toast.LENGTH_SHORT).show();
        isBind = true;
        return mBinder;
//        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public boolean onUnbind(Intent intent) {
        Toast.makeText(this,"Service onUnbind.",Toast.LENGTH_LONG).show();
        isBind = false;
        return super.onUnbind(intent);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Toast.makeText(this,"Service onStartCommand.",Toast.LENGTH_LONG).show();
        //int flag =super.onStartCommand(intent, flags, startId);
        int process = android.os.Process.myPid();
        Toast.makeText(this,"Service onStartCommand.\nProcess id:"+process,Toast.LENGTH_LONG).show();


        //Restart service if it gets terminated.
        return Service.START_STICKY;
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
            if(isBind) {
                //Toast.makeText(LocationService.this,"User id is:" + LocationService.this.user_id,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(UPDATE_MAP);
                intent.putExtra(FRIENDS_LOCATIONS, "Counter value:" + counter);
                sendBroadcast(intent);
                counter++;
            }else{
                Toast.makeText(LocationService.this,"Counter value:" + counter,Toast.LENGTH_SHORT).show();
            }
            mHandlerSendCurrentLocation.postDelayed(this,5000);
        }
    }
    public class MyBinder extends Binder{
        public LocationService getService(){
            return LocationService.this;
        }
    }
}

