package com.project.mosis.buymeadrink.Service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.LocationHelper;

import org.json.JSONObject;

public class LocationService extends Service {
    public LocationService() {
    }

    public final static String ACTION_UPDATE_MAP = "ACTION_UPDATE_MAP";
    public final static String FRIENDS_LOCATIONS = "FRIENDS_LOCATION";

    private final IBinder mBinder = new MyBinder();
    private boolean isBind = false;

    private LocationHelper mLocationHelper;

    private int range = 200;//200m
    private Handler mHandler;
    private ServiceMainThread serviceMainThread;
    private LocationManager mLocationManager;
    private MyLocationListener locationListener;

    private int refreshRate = 5000;
    @Override
    public void onCreate() {

        super.onCreate();
        //Toast.makeText(this, "Service onCreate.", Toast.LENGTH_SHORT).show();

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
                Log.e("LocationService",exception.toString());
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
        //oast.makeText(this, "Service onBind.", Toast.LENGTH_SHORT).show();
        isBind = true;
        mHandler.removeCallbacks(serviceMainThread);
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, refreshRate, range, locationListener);
        }catch (SecurityException exception) {
            Log.e("LocationService",exception.toString());
        }
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        //Toast.makeText(this, "Service onRebind.", Toast.LENGTH_SHORT).show();
        isBind = true;
        mHandler.removeCallbacks(serviceMainThread);
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, refreshRate, range, locationListener);
        }catch (SecurityException exception) {
            Log.e("LocationService",exception.toString());
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Toast.makeText(this, "Service onUnbind.", Toast.LENGTH_LONG).show();
        isBind = false;
        try {
            mLocationManager.removeUpdates(locationListener);
        }catch (SecurityException exception) {
            Log.e("LocationService",exception.toString());
        }
        mHandler.postDelayed(serviceMainThread,5000);

        //Return true, allow rebind function
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null) {
            //Toast.makeText(this, "Service onStartCommand.\nIntent is null.", Toast.LENGTH_LONG).show();
            mHandler.post(serviceMainThread);//If service is killed or app removed from recent apps intent will be null, in that case it is logical
            // that activity is no longer used so now a can create battery friendly thread ServiceMainThread for reporting locations.
        }
        //Restart service if it gets terminated.
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(serviceMainThread!=null)
            mHandler.removeCallbacks(serviceMainThread);
        Toast.makeText(this, "Service destroyed.", Toast.LENGTH_SHORT).show();
    }


    private void sendLocationAndReceiveFriendsLocation(double lat, double lng) {

        mLocationHelper.sendCurrentLocationAndReceiveNearbyPlaces(lat,lng,range,new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                Toast.makeText(LocationService.this,result.toString(),Toast.LENGTH_LONG).show();
                if(isBind){
                    //            Intent intent = new Intent();
                    //            intent.setAction(ACTION_UPDATE_MAP);
                    //            intent.putExtra(FRIENDS_LOCATIONS, "Counter value:");
                    //            sendBroadcast(intent);
                }else{
                    //TODO: make notification if condition are met.
                }
            }
            @Override
            public void onFailed(String error) {
                //Do nothing here.
                Toast.makeText(LocationService.this,error,Toast.LENGTH_LONG).show();
                Log.e("LocationService",error);
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
            //Toast.makeText(LocationService.this,"CallBackTriggered!\nIsBind:"+String.valueOf(isBind),Toast.LENGTH_SHORT).show();
            try{
                mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }catch (SecurityException exception){
                Log.e("LocationService",exception.toString());
            }
            mHandler.postDelayed(this,300000);//Callback on every 5m, this is when user don't use application.
        }
    }
    private class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            //Toast.makeText(LocationService.this,"onLocationChanged. LAT:"+location.getLatitude(),Toast.LENGTH_SHORT).show();
            sendLocationAndReceiveFriendsLocation(location.getLatitude(),location.getLongitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Toast.makeText(LocationService.this,"onStatusChanged.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(LocationService.this,"onProviderEnabled.",Toast.LENGTH_SHORT).show();
            //run callback thread again
            if(!isBind){
                mHandler.post(serviceMainThread);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(LocationService.this,"onProviderDisabled.",Toast.LENGTH_SHORT).show();
            mHandler.removeCallbacks(serviceMainThread);


        }
    }
    public class MyBinder extends Binder{
        public LocationService getService(){
            return LocationService.this;
        }
    }
}

