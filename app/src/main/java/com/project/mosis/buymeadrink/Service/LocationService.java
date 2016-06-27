package com.project.mosis.buymeadrink.Service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.Gson;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.ObjectLocation;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.LocationHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LocationService extends Service {
    public LocationService() {
    }

    public final static String ACTION_UPDATE_MY_LOCATION = "ACTION_UPDATE_MY_LOCATION";
    public final static String ACTION_UPDATE_MAP = "ACTION_UPDATE_MAP";
    public final static String FRIENDS_LOCATIONS = "FRIENDS_LOCATION";
    public final static String MY_LOCATION = "MY_LOCATION";
    private final String LOG_TAG = "LocationService";

    private final IBinder mBinder = new MyBinder();
    private boolean isBind = false;

    private LocationHelper mLocationHelper;

    private int range = 200;//200m
    private Handler mHandler;
    private ServiceMainThread serviceMainThread;
    private LocationManager mLocationManager;
    private MyLocationListener locationListener;

    //last location
    private double lat=0,lng=0;

    private int refreshRate = 5000;//5s
    @Override
    public void onCreate() {

        super.onCreate();
        Log.i(LOG_TAG,"onCreate");

            User user = SaveSharedPreference.GetUser(this);
            //This should not ever happen

            mLocationHelper = new LocationHelper(this,user);

            mHandler = new Handler();
            serviceMainThread = new ServiceMainThread();


            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,10,locationListener);
            locationListener = new MyLocationListener();

            try {
                //One single location update with NETWORK_PROVIDER to speedup first load.
                mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }catch (SecurityException exception){
                Log.e(LOG_TAG,exception.toString());
            }
    }

    private void backUpService() {
        //TODO:Save LRU cache in SharedPreference
    }

    private void restoreService() {
        //TODO:Restore LRU cache from SharedPreference
    }

    public void setRange(int range) {
        this.range = range;
    }

    /**
     * <p>Set refresh rate for ServiceMainThread</p>
     * @param refreshRate In milliseconds.
     * */
    public void setRefreshRate(int refreshRate) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG,"onBind");
        isBind = true;
        mHandler.removeCallbacks(serviceMainThread);
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, refreshRate, range, locationListener);
        }catch (SecurityException exception) {
            Log.e(LOG_TAG,exception.toString());
        }
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(LOG_TAG,"onRebind");
        isBind = true;
        mHandler.removeCallbacks(serviceMainThread);
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, refreshRate, range, locationListener);
        }catch (SecurityException exception) {
            Log.e(LOG_TAG,exception.toString());
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(LOG_TAG,"onUnbind");
        isBind = false;
        try {
            mLocationManager.removeUpdates(locationListener);
        }catch (SecurityException exception) {
            Log.e(LOG_TAG,exception.toString());
        }
        mHandler.postDelayed(serviceMainThread,5000);

        //Return true, allow rebind function
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null) {
            //Toast.makeText(this, "Service onStartCommand.\nIntent is null.", Toast.LENGTH_LONG).show();
            Log.i(LOG_TAG,"onStartCommand, \nstart_id:"+startId+"\nIntent:Is NULL");
            mHandler.post(serviceMainThread);//If service is killed or app removed from recent apps intent will be null, in that case it is logical
            // that activity is no longer used so now a can create battery friendly thread ServiceMainThread for reporting locations.
        }
        //Restart service if it gets terminated.
        Log.i(LOG_TAG,"onStartCommand, \nstart_id:"+startId+"\nIntent:Not NULL");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(serviceMainThread!=null)
            mHandler.removeCallbacks(serviceMainThread);
        Log.i(LOG_TAG,"Service destroyed.");
        //Toast.makeText(this, "Service destroyed.", Toast.LENGTH_SHORT).show();
    }


    private void sendLocationAndReceiveFriendsLocation(double lat, double lng) {

        mLocationHelper.sendCurrentLocationAndReceiveNearbyPlaces(lat,lng,range,new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                //Toast.makeText(LocationService.this,result.toString(),Toast.LENGTH_LONG).show();
                Log.i(LOG_TAG,result.toString());
                ArrayList<ObjectLocation> friendsLocations = new ArrayList<>();
//                myAndFriendsLocations.add(new ObjectLocation("",LocationService.this.lat,LocationService.this.lng));
                try {
                    if (result.getBoolean("Success")){
                        JSONObject data = result.getJSONObject("Data");

                        JSONArray jsonArray = data.getJSONArray("friends_location");

                        for(int i=0;i<jsonArray.length();i++){
                            friendsLocations.add(new Gson().fromJson(jsonArray.get(i).toString(),ObjectLocation.class));
                        }
                    }
                }catch (JSONException exception){
                    Log.e(LOG_TAG,exception.toString());
                }catch (Exception exception){
                    Log.e(LOG_TAG,exception.toString());
                }

                if(friendsLocations.size()!=0){
//                    for(int i=0;friendsLocations.size()>i;i++)
//                        Log.i(LOG_TAG, friendsLocations.get(i).toString());
                    //"for" is test///

                    //if is bind then send data to activity with broadCast
                    if(isBind) {
                        Intent intent = new Intent();
                        intent.setAction(ACTION_UPDATE_MAP);
                        intent.putParcelableArrayListExtra(FRIENDS_LOCATIONS, friendsLocations);
                        sendBroadcast(intent);
                    }
                }
                //TODO: make notification if condition are met.
            }
            @Override
            public void onFailed(String error) {
                //Do nothing here.
                //Toast.makeText(LocationService.this,error,Toast.LENGTH_LONG).show();
                Log.e(LOG_TAG,error);
            }
        });
    }
    private void makeNotification(){

    }
    /**
     * <p>This class periodically call singleLocationRequest, refresh rate depends:</p>
     * <p>- if user using app, refresh rate is higher or if user don't use app then refresh rate is 5m.</p>
     * */
    private class ServiceMainThread implements Runnable {
        @Override
        public void run() {
            Log.i(LOG_TAG,"ServiceMainThread triggered. Is bind:"+String.valueOf(isBind));
            try{
                mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }catch (SecurityException exception){
                Log.e(LOG_TAG,exception.toString());
            }
            mHandler.postDelayed(this,300000);//Callback on every 5m, this is when user don't use application.
        }
    }
    private class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            Log.i(LOG_TAG,"onLocationChanged");
            //LocationService.this.lat = location.getLatitude();
            //LocationService.this.lng = location.getLongitude();
            sendLocationAndReceiveFriendsLocation(location.getLatitude(),location.getLongitude());
            if(isBind){
                Intent intent = new Intent();
                intent.setAction(ACTION_UPDATE_MY_LOCATION);
                intent.putExtra(MY_LOCATION,new ObjectLocation("",location.getLatitude(),location.getLongitude()));
                sendBroadcast(intent);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.i(LOG_TAG,"onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(LOG_TAG,"onProviderEnabled");
            //run callback thread again
            if(!isBind){
                mHandler.post(serviceMainThread);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(LOG_TAG,"onProviderDisabled");
            mHandler.removeCallbacks(serviceMainThread);
        }
    }
    public class MyBinder extends Binder{
        public LocationService getService(){
            return LocationService.this;
        }
    }
}

