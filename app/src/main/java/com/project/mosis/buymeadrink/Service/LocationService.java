package com.project.mosis.buymeadrink.Service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.ObjectLocation;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.LocationHelper;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;
import com.project.mosis.buymeadrink.MainActivity;
import com.project.mosis.buymeadrink.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LocationService extends Service {
    public LocationService() {
    }

    public final static String ACTION_UPDATE_MY_LOCATION = "ACTION_UPDATE_MY_LOCATION";
    public final static String ACTION_UPDATE_FRIENDS_LOCATIONS = "ACTION_UPDATE_FRIENDS_LOCATIONS";
    public final static String FRIENDS_LOCATIONS = "FRIENDS_LOCATIONS";
    public final static String MY_LOCATION = "MY_LOCATION";
    private final String LOG_TAG = "LocationService";
    private final String REQUEST_TAG = "LocationService";
    private final IBinder mBinder = new MyBinder();
    private boolean isBind = false;

    //class for network support
    private LocationHelper mLocationHelper;
    //Thread var and settings
    private Handler backgroundHandler,foregroundHandler;
    private ServiceBackgroundThread serviceBackgroundThread;
    private ServiceForegroundThread serviceForegroundThread;
    private final int backgroundThreadRefreshRate= 300000;//5m
    private final int foregroundThreadRefreshRate= 6000;//6s
    //Location manager and lister
    private LocationManager mLocationManager;
    private MyLocationListener locationListener;
    //Range for notification messages
    private int range = 100;//this is range for notification
    //GPS parameters
    private float minDistance = (float) 1.5;//1.5m
    private int minTime = 15000;//15s
    //last updated location location
    private double lat=0,lng=0;
    private boolean coordinatesIsReady = false;
    @Override
    public void onCreate() {

        super.onCreate();
        Log.i(LOG_TAG,"onCreate");

        User user = SaveSharedPreference.GetUser(this);

        mLocationHelper = new LocationHelper(this,user);

        backgroundHandler = new Handler();
        foregroundHandler = new Handler();

        serviceBackgroundThread = new ServiceBackgroundThread();
        serviceForegroundThread = new ServiceForegroundThread();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

//            try {
//                //singe request for location with(NETWORK_PROVIDER) will speed up first time loading, GPS is slow
//                mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
//            }catch (SecurityException exception){
//                Log.e(LOG_TAG,exception.toString());
//            }
    }

    public void setRange(int range) {
        this.range = range;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(LOG_TAG,"onBind");
        isBind = true;
        //start foreground thread
        foregroundHandler.post(serviceForegroundThread);
        //stop background thread
        backgroundHandler.removeCallbacks(serviceBackgroundThread);
        try {
            // singe request for location with(NETWORK_PROVIDER) will speed up first time loading, GPS is slow
            mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
        }catch (SecurityException exception) {
            Log.e(LOG_TAG,exception.toString());
        }
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(LOG_TAG,"onRebind");
        isBind = true;
        //start foreground thread
        foregroundHandler.post(serviceForegroundThread);
        //stop background thread
        backgroundHandler.removeCallbacks(serviceBackgroundThread);
        try {
            // singe request for location with(NETWORK_PROVIDER) will speed up first time loading, GPS is slow
            mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
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

        //stop foreground thread
        foregroundHandler.removeCallbacks(serviceForegroundThread);
        //start background thread to report location in background after 5s
        // (maybe user just go in another activity for short time, this should be 1m for example)
        // but now for testing is 5s
        backgroundHandler.postDelayed(serviceBackgroundThread,backgroundThreadRefreshRate);

        //Return true, allow rebind function
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent==null) {
            Log.i(LOG_TAG,"onStartCommand, \nstart_id:"+startId+"\nIntent:Is NULL");
            backgroundHandler.postDelayed(serviceBackgroundThread,backgroundThreadRefreshRate);//If service is killed or app removed from recent apps intent will be null, in that case it is logical
            // that activity is no longer used, so now a can create battery friendly thread ServiceBackgroundThread for reporting locations.
            // This thread using locating from NETWORK_PROVIDE in a way that it uses less battery.
            //Note:Background thread will start after some time (1h for example) because is stupid to make notification just after user leave app.
        }
        //Restart service if it gets terminated.
        Log.i(LOG_TAG,"onStartCommand, \nstart_id:"+startId+"\nIntent:Not NULL");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //on destroy remove all running thread if running
        backgroundHandler.removeCallbacks(serviceBackgroundThread);
        backgroundHandler.removeCallbacks(serviceForegroundThread);
        //cancel all request to the server
        mLocationHelper.cancelAllRequest();
        UserHandler.CancelAllRequestWithTagStatic(this,REQUEST_TAG);
        Log.i(LOG_TAG,"Service destroyed.");
    }


    private void sendLocationAndReceiveFriendsLocation(double lat, double lng) {

        mLocationHelper.sendCurrentLocationAndReceiveNearbyPlaces(lat,lng,range,new VolleyCallBack() {
            @Override
            public void onSuccess(JSONObject result) {
                //is is not main activity is not in use then parse friends locations data
                //Otherwise is there is some object in nearby show notification
                if(isBind){

                    Log.i(LOG_TAG, "Function sendLocationAndReceiveFriendsLocation\nLocations received, Data:" + result.toString());
                    ArrayList<ObjectLocation> friendsLocations = new ArrayList<>();
                    try {
                        if (result.getBoolean("Success")) {
                            JSONObject data = result.getJSONObject("Data");

                            JSONArray jsonArray = data.getJSONArray("friends_location");

                            for (int i = 0; i < jsonArray.length(); i++) {
                                friendsLocations.add(new Gson().fromJson(jsonArray.get(i).toString(), ObjectLocation.class));
                            }
                        }
                    } catch (JSONException exception) {
                        Log.e(LOG_TAG, exception.toString());
                    } catch (Exception exception) {
                        Log.e(LOG_TAG, exception.toString());
                    }

                    if (friendsLocations.size() != 0) {
                        Intent intent = new Intent();
                        intent.setAction(ACTION_UPDATE_FRIENDS_LOCATIONS);
                        intent.putParcelableArrayListExtra(FRIENDS_LOCATIONS, friendsLocations);
                        sendBroadcast(intent);
                    }
                }else{
                    try{
                        if(result.getBoolean("Success")){
                            JSONObject data = result.getJSONObject("Data");

                            JSONArray jsonArray = data.getJSONArray("questions_in_nearby");

                            //if questions and friends arrives as object in nearby question have
                            //bigger priority than friends in nearby. so question will be shown in notification.
                            //Otherwise friends will be in notification
                            if(jsonArray.length()>0){
                                //parse questions_in_nearby
                                makeNotification(null,jsonArray.length(),false);
                                //TODO:make notificationb

                            }else{
                                //parse friends_in_nearby
                                jsonArray = data.getJSONArray("friends_in_nearby");
                                if(jsonArray.length()>0) {
                                    String friendID = jsonArray.get(0).toString();
                                    //Make request for user info and picture
                                    final int friends_num = jsonArray.length();
                                    UserHandler.getUserStatic(LocationService.this, friendID, REQUEST_TAG, new VolleyCallBack() {
                                        private int friends_in_nearby_num = friends_num;

                                        @Override
                                        public void onSuccess(JSONObject result) {
                                            makeNotification(result, friends_in_nearby_num, true);
                                        }

                                        @Override
                                        public void onFailed(String error) {
                                            Log.e(LOG_TAG, error);
                                        }
                                    });
                                }
                                //UserHandler.getUserBitmapStatic(LocationService.this,friendID,REQUEST_TAG,);
                            }

                        }else{
                            Log.i(LOG_TAG,result.getString("Error"));
                        }

                    } catch (JSONException exception) {
                        Log.e(LOG_TAG, exception.toString());
                    } catch (Exception exception) {
                        Log.e(LOG_TAG, exception.toString());
                    }
                }//end of isBind = false
            }
            @Override
            public void onFailed(String error) {
                //Do nothing here.
                Log.e(LOG_TAG,"Error in function send current location and receive friends locations.\n" + error);
            }
        });
    }
    private void makeNotification(JSONObject result,int num_of_objects_in_nearby,boolean isFriend){
        if(isFriend) {
            User friend=null;
            try {
                friend = new Gson().fromJson(result.getString("Data"), User.class);
            } catch (JSONException exception) {
                Log.e(LOG_TAG, exception.toString());
            } catch (Exception exception) {
                Log.e(LOG_TAG, exception.toString());
            }
            if(friend==null)
                return;
            int FRIEND_NOTIFICATION_ID = 0;
            String tmp = num_of_objects_in_nearby==1?
                    "Your friend " + friend.getName()+" is nearby.":
                    "Your friend " + friend.getName()+" is nearby and "+(num_of_objects_in_nearby-1)+" more.";
            NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_default_friend)
                    .setContentTitle("Friends in nearby")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(tmp))
                    .setContentText(tmp)
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setLights(Color.CYAN,1000,500);

                        Intent intent = new Intent(this, MainActivity.class);
            PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(mainActivityPendingIntent);
            NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mManager.notify(FRIEND_NOTIFICATION_ID,mBuilder.build());
        }else{
            int QUESTION_NOTIFICATION_ID = 1;
            String tmp = num_of_objects_in_nearby==1?
                    "Your have one question nearby.":
                    "Your have "+ num_of_objects_in_nearby +" questions nearby.";
            NotificationCompat.Builder  mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_question_mark_big)
                    .setContentTitle("Question in nearby")
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(tmp))
                    .setContentText(tmp)
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setLights(Color.CYAN,1000,500);

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent mainActivityPendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(mainActivityPendingIntent);
            NotificationManager mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mManager.notify(QUESTION_NOTIFICATION_ID,mBuilder.build());
        }
    }
    /**
     * <p>This class running in background and when mainActivity (app) is not in use, and report location to the server,
     * </p>
     * */
    private class ServiceBackgroundThread implements Runnable {
        @Override
        public void run() {
            Log.i(LOG_TAG,"ServiceBackgroundThread triggered. Is bind:"+String.valueOf(isBind));
            try{
                mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }catch (SecurityException exception){
                Log.e(LOG_TAG,exception.toString());
            }
            backgroundHandler.postDelayed(this,backgroundThreadRefreshRate);
        }
    }
    private class ServiceForegroundThread implements Runnable{

        @Override
        public void run() {
            Log.i(LOG_TAG,"ServiceForegroundThread triggered. Is bind:"+String.valueOf(isBind));
            //if coordinates is ready, send last know coordinates
            if(coordinatesIsReady)
                sendLocationAndReceiveFriendsLocation(LocationService.this.lat,LocationService.this.lng);
            //update friends location on every 2s, this fresh rate is high later may be on 5s for example
            foregroundHandler.postDelayed(this,foregroundThreadRefreshRate);
        }
    }

    private class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            Log.i(LOG_TAG,"onLocationChanged");
            LocationService.this.lat = location.getLatitude();
            LocationService.this.lng = location.getLongitude();

            //this var "coordinatesIsReady" is needed only for the first time when service is started
            //and preventing foreground thread from sending bad location on the server.
            LocationService.this.coordinatesIsReady = true;
            //if app is in use:
            // 1.Update user location in mainActivity.
            // 2.Foreground thread will call function sendLocationAndReceiveFriendsLocation(...) and this function after data receive from server
            // will call mainActivity to update friends locations on map.
            // Otherwise if app is not in use just report location on the server.
            if(isBind){
                Intent intent = new Intent();
                intent.setAction(ACTION_UPDATE_MY_LOCATION);
                intent.putExtra(MY_LOCATION,new ObjectLocation("",location.getLatitude(),location.getLongitude()));
                sendBroadcast(intent);
            }else{
                sendLocationAndReceiveFriendsLocation(location.getLatitude(),location.getLongitude());
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Log.i(LOG_TAG,"onStatusChanged");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(LOG_TAG,"onProviderEnabled");
            //If app is in use and provider is on again, start foreground thread.
            //Otherwise if app is not in use start background thread
            if(isBind){
                foregroundHandler.post(serviceForegroundThread);
            }else{
                backgroundHandler.post(serviceBackgroundThread);
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.i(LOG_TAG,"onProviderDisabled");
            //if provider is disabled stop all threads, this should not crash if thread not running.
            backgroundHandler.removeCallbacks(serviceBackgroundThread);
            foregroundHandler.removeCallbacks(serviceForegroundThread);
        }
    }

    public class MyBinder extends Binder{
        public LocationService getService(){
            return LocationService.this;
        }
    }

}

