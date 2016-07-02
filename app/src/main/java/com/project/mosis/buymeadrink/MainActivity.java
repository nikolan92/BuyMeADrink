package com.project.mosis.buymeadrink;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.toolbox.NetworkImageView;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.project.mosis.buymeadrink.Application.MyApplication;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.ObjectLocation;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBackBitmap;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;
import com.project.mosis.buymeadrink.SearchResultData.SearchResult;
import com.project.mosis.buymeadrink.Service.LocationService;
import com.project.mosis.buymeadrink.Utils.LatLngInterpolator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    //code for Log report
    private final String LOG_TAG = "MainActivity";
    //location permission codes
    private final int LOCATION_PERMISSION_CODE = 0;
    //Intent request codes
    private final int USER_PROFILE_ACTIVITY_REQUEST_CODE = 0;
    private final int ADD_QUESTION_ACTIVITY_REQUEST_CODE = 1;
    private final int ADD_FRIEND_ACTIVITY_REQUEST_CODE = 2;

    private final int SETTINGS_ACTIVITY_REQUEST_CODE = 3;
    //UserHandler Volley request code
    private final String REQUEST_TAG = "MainActivity";
    //Layout var
    private FloatingSearchView mSearchView;
    private DrawerLayout drawer;
    private TextView nameInput;
    private TextView emailInput;
    private NetworkImageView userImage;
    //User
    private UserHandler userHandler;
    private User user;
    //Map
    private GoogleMap mMap;
    private HashMap<String,Marker> markers;
    private HashMap<String,String> friends_names;
    private HashMap<Marker,String> markerOnClick;
    private Marker currentLocation;
    private LatLng globalCurrentLocation;
    //Service vars
    private UpdateMapReceiver updateMapReceiver;
    private LocationService locationService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.MyBinder myBinder = (LocationService.MyBinder) service;
            locationService = myBinder.getService();
            //Toast.makeText(MainActivity.this,"Connected to service!",Toast.LENGTH_SHORT).show();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService = null;
//            Toast.makeText(MainActivity.this,"Disconnected from service!",Toast.LENGTH_SHORT).show();
        }
    };
    private boolean locationPermission = false;
    private boolean leaveServiceOnAfterDestroy = true;
    //End of Service vars

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        markers = new HashMap<>();
        friends_names = new HashMap<>();
        markerOnClick = new HashMap<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mSearchView = (FloatingSearchView)findViewById(R.id.floating_search_view);

        //because of line below app can crash because in some situations maybe application don't start splash screen activity or (login or register)
        //SplashScreenActivity setting up user var and if is not called then user will not be set and will be null, that's a problem!
        user = ((MyApplication) MainActivity.this.getApplication()).getUser();
        //in that case we need to load user from shared preferences
        if(user == null){
            user = SaveSharedPreference.GetUser(this);
        }

        globalCurrentLocation = ((MyApplication) MainActivity.this.getApplication()).getCurrentLocation();
        //restore serviceSettings
        leaveServiceOnAfterDestroy = ((MyApplication) MainActivity.this.getApplication()).getServiceSettings();

        userHandler = new UserHandler(this);

        //load friends info
        userHandler.getUserFriends(user.getId(),REQUEST_TAG,new GetFriendsInfoListener(this));

        setupFloatingSearch();
        setupDrawer();//Drawer will setup NavigationView header for userInfo
        loadUser();

        //Request permission
        locationPermission();

        //TEST_TAG
        //if(locationPermission)
        //bindService(new Intent(this,LocationService.class),mConnection,BIND_AUTO_CREATE);

    }

    @Override
    protected void onStart() {
        super.onStart();
        if(locationPermission){
            updateMapReceiver = new UpdateMapReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(LocationService.ACTION_UPDATE_FRIENDS_LOCATIONS);
            intentFilter.addAction(LocationService.ACTION_UPDATE_MY_LOCATION);
            registerReceiver(updateMapReceiver,intentFilter);
            //TEST_TAG
            bindService(new Intent(this,LocationService.class),mConnection,BIND_AUTO_CREATE);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        userHandler.cancelAllRequestWithTag(REQUEST_TAG);
        if(locationPermission) {
            unregisterReceiver(updateMapReceiver);
            //TEST_TAG
            unbindService(mConnection);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Stop service if user say so
        if(!leaveServiceOnAfterDestroy)
            stopService(new Intent(this, LocationService.class));
        //TEST TAG
//        if(locationPermission)
//            unbindService(mConnection);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if(!mSearchView.setSearchFocused(false)){
            super.onBackPressed();
        }
    }

    private void setupFloatingSearch(){
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                }else{
                    //mSearchView.showProgress();
                    ArrayList<SearchResult> list = new ArrayList<SearchResult>();
                    list.add(new SearchResult("Test"));
                    list.add(new SearchResult("Test2"));

                    mSearchView.swapSuggestions(list);
                }

            }
        });
        mSearchView.setOnLeftMenuClickListener(new FloatingSearchView.OnLeftMenuClickListener() {
            @Override
            public void onMenuOpened() {
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
            }

            @Override
            public void onMenuClosed() {

            }
        });
    }
    private void setupDrawer(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        View hView = navigationView.getHeaderView(0);

        this.nameInput = (TextView) hView.findViewById(R.id.nav_user_name);
        this.emailInput = (TextView) hView.findViewById(R.id.nav_user_email);

        this.userImage = (NetworkImageView) hView.findViewById(R.id.nav_user_image);

        assert drawer != null;
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                mSearchView.setLeftMenuOpen(false);
            }
        });

        View switchView = navigationView.getMenu().findItem(R.id.nav_service).getActionView();
        SwitchCompat mSwitch = (SwitchCompat) switchView.findViewById(R.id.service_switcher);

        mSwitch.setChecked(leaveServiceOnAfterDestroy);
        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SwitchCompat mSwitch = (SwitchCompat) v;
                if(mSwitch.isChecked()){
                    //turn service on after destroy without question
                    serviceSwitcher(true);
                }else{
                    //if user want to turn service off then show dialog
                    serviceSettingsDialog(v);
                }
            }
        });
    }

    private void loadUser(){
        userHandler.getUserImage(user.getId(),userImage);
        assert nameInput != null;
        nameInput.setText(user.getName());
        assert emailInput != null;
        emailInput.setText(user.getEmail());
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add_question) {
            if(currentLocation==null){
                Snackbar.make(drawer,"Please wait until GPS find your location.",Snackbar.LENGTH_LONG).show();
            }else {
                Intent addQuestionIntent = new Intent(this, AddQuestionActivity.class);
                addQuestionIntent.putExtra("ownerID", user.getId());
                addQuestionIntent.putExtra("lat", currentLocation.getPosition().latitude);
                addQuestionIntent.putExtra("lng", currentLocation.getPosition().longitude);
                startActivityForResult(addQuestionIntent, ADD_QUESTION_ACTIVITY_REQUEST_CODE);
            }
        } else if (id == R.id.nav_my_profile) {
            startActivityForResult(new Intent(this,UserProfileActivity.class),USER_PROFILE_ACTIVITY_REQUEST_CODE);
        } else if (id == R.id.nav_add_friend) {
            startActivityForResult(new Intent(this, AddFriendActivity.class),ADD_FRIEND_ACTIVITY_REQUEST_CODE);
        } else if (id == R.id.nav_my_friends) {
            Intent myFriendsListIntent = new Intent(this,FriendsListActivity.class);
            myFriendsListIntent.putExtra("userID",user.getId());
            startActivity(myFriendsListIntent);
        } else if (id == R.id.nav_log_out) {
            //Clear Shared Preference and start LogIn again
            SaveSharedPreference.clearUser(this.getApplicationContext());
            //start Log in activity and clear back stack
            startActivity(new Intent(MainActivity.this,LogInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            if(locationPermission) {
                stopService(new Intent(this, LocationService.class));
            }
            finish();
        } else if(id == R.id.nav_rank_list){
            startActivity(new Intent(this,UsersRankActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /**
     *Service Settings
     *=================================================================================================
     * */
    private void serviceSettingsDialog(final View serviceSwitcher){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("If your turn off this service your friends won't be able to see your exact location on the map, during the inactivity of the application.");

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                //turn service off after destroy
                serviceSwitcher(false);
            }
        });

        alertDialogBuilder.setNegativeButton("No",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SwitchCompat mSwitch = (SwitchCompat) serviceSwitcher;
                //recheck service switcher on again
                mSwitch.setChecked(true);
            }
        });
        alertDialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                SwitchCompat mSwitch = (SwitchCompat) serviceSwitcher;
                //recheck service switcher on again
                mSwitch.setChecked(true);
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void serviceSwitcher(boolean isChecked){
        if(isChecked){
            //TODO:Save shared preferences (service running normally)
            SaveSharedPreference.SetServiceSettings(this,true);
            leaveServiceOnAfterDestroy = true;
        }else{
            //TODO:Save shared preferences (stop service after activity destroy)
            SaveSharedPreference.SetServiceSettings(this,false);
            leaveServiceOnAfterDestroy = false;
        }
        Toast.makeText(MainActivity.this, isChecked?"Service turned on.":"Service turned off.", Toast.LENGTH_SHORT).show();

    }

    /**
     *On Activity result
     *=================================================================================================
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == USER_PROFILE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            user = ((MyApplication)getApplication()).getUser();
            loadUser();
            Snackbar.make(drawer,"Profile is successfully updated.",Snackbar.LENGTH_LONG).show();
        }else if(requestCode == SETTINGS_ACTIVITY_REQUEST_CODE)
        {
            locationPermission();
        }else if(requestCode == ADD_QUESTION_ACTIVITY_REQUEST_CODE){
            //TODO:Add new question on the map
        }else if(requestCode == ADD_FRIEND_ACTIVITY_REQUEST_CODE){
            //TODO:Add new friend on map
        }
    }
    /**
     *Permission for location, Android Marshmallow
     *=================================================================================================
     * */
    private void locationPermission() {
        if(Build.VERSION.SDK_INT<23){
            locationPermission = true;
            startService(new Intent(this, LocationService.class));
        }else{
            if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationPermission = true;
                startService(new Intent(this, LocationService.class));
                return;
            }

            if(shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) && shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)){
                Snackbar.make(drawer,"Location access is required to show your friend and questions on map.",Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(View v) {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSION_CODE);
                            }
                        })
                        .show();
            }else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},LOCATION_PERMISSION_CODE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(drawer,"Location access is available, you will see your friends and question on map in few second.",Snackbar.LENGTH_LONG).show();

            locationPermission =true;
            startService(new Intent(this, LocationService.class));
            updateMapReceiver = new UpdateMapReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(LocationService.ACTION_UPDATE_FRIENDS_LOCATIONS);
            registerReceiver(updateMapReceiver,intentFilter);

            bindService(new Intent(this,LocationService.class),mConnection,BIND_AUTO_CREATE);

        } else {
            Snackbar.make(drawer,"Location access is critical for this app, please allow location access!",Snackbar.LENGTH_INDEFINITE)
                    .setAction("Settings", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", getPackageName(), null)), SETTINGS_ACTIVITY_REQUEST_CODE);
                        }
                    }).show();
        }
    }
    /**
     *BroadCastReceiver inner class
     *=================================================================================================
     **/
    private class UpdateMapReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG,intent.getAction());
            if(intent.getAction().equals(LocationService.ACTION_UPDATE_FRIENDS_LOCATIONS)) {
                ArrayList<ObjectLocation> friends_location = intent.getParcelableArrayListExtra(LocationService.FRIENDS_LOCATIONS);
                updateFriendsLocation(friends_location);

            }else if(intent.getAction().equals(LocationService.ACTION_UPDATE_MY_LOCATION)){
                updateMyLocation((ObjectLocation) intent.getParcelableExtra(LocationService.MY_LOCATION));
            }
        }
    }
    /**
     *Work with map and markers
     *=================================================================================================
     **/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //setOnInfoWindowClick listener
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MainActivity.this,FriendProfileActivity.class);
                intent.putExtra("friendID",markerOnClick.get(marker));
                startActivity(intent);
            }
        });
        restoreCurrentLocation();
    }
    private void animateMarker(final Marker marker, final LatLng newLocation,final LatLngInterpolator latLngInterpolator){

        final LatLng startPosition = marker.getPosition();
        final long start = SystemClock.uptimeMillis();

        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final float durationInMs = 1500;

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            long elapsed;
            float t;
            float v;
            @Override
            public void run() {
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed / durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v, startPosition, newLocation));

                // Repeat till progress is complete.
                if (t < 1) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    private void updateMyLocation(ObjectLocation location){
        //if for some reason map is not available return
        if(mMap==null)
            return;

        if(currentLocation==null){
            LatLng latLng = new LatLng(location.getLat(),location.getLng());
            Bitmap icon = ((BitmapDrawable)userImage.getDrawable()).getBitmap();
            Bitmap smallIcon = Bitmap.createScaledBitmap(icon,100,100,false);//Sony xperia mini 40x40
            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Me").icon(BitmapDescriptorFactory.fromBitmap(smallIcon));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));

            currentLocation = mMap.addMarker(markerOptions);
            markerOnClick.put(currentLocation,user.getId());
        }else{
            animateMarker(currentLocation,new LatLng(location.getLat(),location.getLng()),new LatLngInterpolator.Linear());
        }
    }
    //speed up showing user location after user return from some activity because MainActivity lost all markers when
    // user goes in some other activity actually MainActivity again go through onCreate method
    private void restoreCurrentLocation(){
        if(globalCurrentLocation==null){
            return;
        }
        if(currentLocation==null){
            Bitmap icon = ((BitmapDrawable)userImage.getDrawable()).getBitmap();
            Bitmap smallIcon = Bitmap.createScaledBitmap(icon,100,100,false);//Sony xperia mini 40x40
            MarkerOptions markerOptions = new MarkerOptions().position(globalCurrentLocation).title("Me").icon(BitmapDescriptorFactory.fromBitmap(smallIcon));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(globalCurrentLocation,18));

            currentLocation = mMap.addMarker(markerOptions);
            markerOnClick.put(currentLocation,user.getId());
        }
    }

    private void updateFriendsLocation(ArrayList<ObjectLocation> friends_location){
        //if map is not ready yet which should not ever happen then return
        if(mMap==null)
            return;
        //if friends names is not ready yet which should not ever happen then return
        if(friends_names.isEmpty())
            return;

        //this do only first time, if markers is set then just update markers location (else branch)
        if(markers.isEmpty()) {
            for (int i = 0; i < friends_location.size(); i++) {
                LatLng latLng = new LatLng(friends_location.get(i).getLat(), friends_location.get(i).getLng());
                String friendID = friends_location.get(i).getObjectId();

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(friends_names.get(friendID))//set friend name in title
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_default_friend));

                //this is necessary for onInfoWindowClick
                Marker marker = mMap.addMarker(markerOptions);
                markerOnClick.put(marker,friends_location.get(i).getObjectId());
                //this is necessary for later update -friend icon(once) and his location on every change
                markers.put(friendID, marker);

                //make request for image
                userHandler.getUserImageInBitmap(friends_location.get(i).getObjectId(), REQUEST_TAG, new GetFriendsBitmapListener(this,friendID));
            }
            Log.i(LOG_TAG,"Friends placed on the map, for the first time.");
        }else{
            for (int i = 0; i < friends_location.size(); i++) {
                String friendID = friends_location.get(i).getObjectId();
                animateMarker(markers.get(friendID),new LatLng(friends_location.get(i).getLat(),friends_location.get(i).getLng()),new LatLngInterpolator.Linear());
            }
        }
    }
    /**
     *
     *EXAMPLE:For UserHandler use
     *=================================================================================================
     * */
    /**
     *This function will do some job after request was successful.
     * */
    public void onFriendsInfoReady(JSONObject result){

        try{
            if(result.getBoolean("Success")){
                JSONArray friendsInJson = result.getJSONArray("Data");

                for(int i=0;i<friendsInJson.length();i++)
                {
                    JSONObject friendInJson = (JSONObject) friendsInJson.get(i);
                    //fill hash map with friends name
                    friends_names.put(friendInJson.getString("_id"),friendInJson.getString("name"));
                    //Log.i(LOG_TAG,friendInJson.toString());
                }
            }else{
                Log.e(LOG_TAG,result.getString("Error"));
            }

        }catch (JSONException exception)
        {
            Log.e(LOG_TAG,exception.toString());
        }
        //TODO: parse result and set friends hash map,result is array of user
        //Toast.makeText(MainActivity.this, result.toString(),Toast.LENGTH_SHORT).show();
    }
    public void onFriendsBitmapReady(Bitmap image,String friendID){
        Marker marker = markers.get(friendID);
        marker.setIcon(BitmapDescriptorFactory.fromBitmap(image));
    }

    /**
     * Static inner classes do not hold an implicit reference to their outer classes, so activity will not be leaked.
     * Also because i need to access to an activity method i need to hold a reference to it. But i keep weakReference,
     * so GC will not be prevented from deleting it. Because of that i need to check whether activity still exist.
     * */
    private static class GetFriendsInfoListener implements VolleyCallBack{

        private final WeakReference<MainActivity> mActivity;
        GetFriendsInfoListener(MainActivity mainActivity){
            mActivity = new WeakReference<>(mainActivity);
        }
        @Override
        public void onSuccess(JSONObject result) {
            MainActivity mainActivity = mActivity.get();
            if(mainActivity!=null)//If activity still exist then do some job, if not just return;
                mainActivity.onFriendsInfoReady(result);
        }

        @Override
        public void onFailed(String error) {
            MainActivity mainActivity = mActivity.get();
            if(mainActivity!=null)//If activity still exist then do some job, if not just return;
                Log.e(mainActivity.LOG_TAG,error);
        }
    }
    private static class GetFriendsBitmapListener implements VolleyCallBackBitmap {
        private final WeakReference<MainActivity> mActivity;
        private String objectID;
        GetFriendsBitmapListener(MainActivity mainActivity,String objectID){
            mActivity = new WeakReference<>(mainActivity);
            this.objectID= objectID;
        }
        @Override
        public void onSuccess(Bitmap result) {
            MainActivity mainActivity = mActivity.get();
            if(mainActivity!=null){
                mainActivity.onFriendsBitmapReady(result,this.objectID);
            }
        }

        @Override
        public void onFailed(String error) {
            MainActivity mainActivity = mActivity.get();
            if(mainActivity!=null)//If activity still exist then do some job, if not just return;
                Log.e(mainActivity.LOG_TAG,error);
        }
    }
    private class MyMarker {
        private String _id;
        private boolean isItFriend;

        MyMarker(String _id,boolean isItFriend){
            this._id = _id;
            this.isItFriend = isItFriend;
        }
        public String getID(){
            return _id;
        }
        public boolean isItFriend(){
            return isItFriend;
        }
    }
}