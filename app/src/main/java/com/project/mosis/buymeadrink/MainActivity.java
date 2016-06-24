package com.project.mosis.buymeadrink;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.project.mosis.buymeadrink.Application.MyAplication;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;
import com.project.mosis.buymeadrink.SearchResultData.SearchResult;
import com.project.mosis.buymeadrink.Service.LocationService;
import com.project.mosis.buymeadrink.Utils.LatLngInterpolator;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private final int USER_PROFILE_ACTIVITY_REQUEST_CODE = 0;
    private final int ADD_QUESTION_ACTIVITY_REQUEST_CODE = 1;

    private FloatingSearchView mSearchView;
    private DrawerLayout drawer;
    private GoogleMap mMap;
    private UserHandler userHandler;
    private User user;
    private final String REQUSET_TAG = "MainActivity";

    //Service
    private UpdateMapReceiver updateMapReceiver;
    private LocationService locationService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //locationService = ((LocationService.MyBinder)service).getService();
            Toast.makeText(MainActivity.this,"Connected to service!",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationService = null;
            Toast.makeText(MainActivity.this,"Disconnected from service!",Toast.LENGTH_SHORT).show();
        }
    };
    //End of Service

    private TextView nameInput;
    private TextView emailInput;
    private NetworkImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mSearchView = (FloatingSearchView)findViewById(R.id.floating_search_view);

        //TODO:Get user from global var and make userHandler with that user
        user = ((MyAplication) MainActivity.this.getApplication()).getUser();
        userHandler = new UserHandler(this);

        setupFloatingSearch();
        setupDrawer();//Drawer will setup NavigationView header for userInfo
        loadUser();

//        int process = android.os.Process.myPid();
//        Toast.makeText(this,"Activity process id:"+process,Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        updateMapReceiver = new UpdateMapReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(LocationService.UPDATE_MAP);
        registerReceiver(updateMapReceiver,intentFilter);

        super.onStart();
    }

    @Override
    protected void onStop() {
        userHandler.CancelAllRequestWithTag(REQUSET_TAG);
        unregisterReceiver(updateMapReceiver);

        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(44.0, 23);
        MarkerOptions markerOptions = new MarkerOptions().position(sydney).title("Marker in Sydney");
        Marker m = mMap.addMarker(markerOptions);

        //m.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_default_user_image));//moze kasnije dodavanje ikone

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(44.5,23.5),9));
        //markerOptions.
        animateMarker(m,new LatLng(44.5,23.5),new LatLngInterpolator.Linear());


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
    }
    private void loadUser(){
        userHandler.getUserImage(user.getId(),userImage);
        assert nameInput != null;
        nameInput.setText(user.getName());
        assert emailInput != null;
        emailInput.setText(user.getEmail());
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add_question) {
            //bindService(new Intent(this,LocationService.class),mConnection,BIND_AUTO_CREATE);

        } else if (id == R.id.nav_my_profile) {
            startActivityForResult(new Intent(this,UserProfileActivity.class),USER_PROFILE_ACTIVITY_REQUEST_CODE);
        } else if (id == R.id.nav_add_friend) {

            //startActivity(new Intent(this, BluetoothActivity.class));

        } else if (id == R.id.nav_my_friends) {
            //unbindService(mConnection);

        } else if (id == R.id.nav_setings) {

            //Starting service testing
            //startService(new Intent(this, LocationService.class));
            /**
             * Now here all we need to do is to make variable to our static class and make new one, then pass to the userHanler
            * */
            //final OnLogInListener listener = new OnLogInListener(this);

            //userHandler.logIn("s", "s", "Tag", listener);

        } else if (id == R.id.nav_log_out) {
            //Clear Shared Preference and start LogIn again
            SaveSharedPreference.clearUser(this.getApplicationContext());
            //start Log in activity and clear back stack
            startActivity(new Intent(MainActivity.this,LogInActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            stopService(new Intent(this, LocationService.class));
            finish();
        } else if(id == R.id.nav_rank_list){
            startActivity(new Intent(this,UsersRankActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == USER_PROFILE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            user = ((MyAplication)getApplication()).getUser();
            loadUser();
        }
    }
    /**
     *BroadCastReceiver
     *=================================================================================================
     * */
    private class UpdateMapReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String dataFromService = intent.getStringExtra(LocationService.FRIENDS_LOCATIONS);
            Toast.makeText(MainActivity.this,"Service trigger this event. Data:" + dataFromService,Toast.LENGTH_SHORT).show();
        }
    }
    /**
    *EXAMPLE:For UserHandler use
    *=================================================================================================
    * */
    /**
    *This function will do some job after logIn was successful.
    * */
    public void onLogIn(JSONObject result){
        Toast.makeText(MainActivity.this, result.toString(),Toast.LENGTH_SHORT).show();
    }
    /**
     *This function will do some job if logIn was unsuccessful.
     * */
    public void onLogInFailure(String error){
        Toast.makeText(MainActivity.this, "Error occur:\n"+ error.toString(),Toast.LENGTH_SHORT).show();
    }

    /**
     * Static inner classes do not hold an implicit reference to their outher clases, so activity will not be leaked.
     * Also because i need to access to an activity method i need to hold a reference to it. But i keep weakReference,
     * so GC will not be prevented from deleting it. Because of that i need to check whether activity still exist.
     * */
    private static class GetAllUsersListener implements VolleyCallBack{
        //TODO:Change MainActivity to activity
        private final WeakReference<MainActivity> mActivity;
        GetAllUsersListener(MainActivity mainActivity){
            mActivity = new WeakReference<MainActivity>(mainActivity);
        }
        @Override
        public void onSuccess(JSONObject result) {
            MainActivity mainActivity = mActivity.get();
            if(mainActivity!=null)//If activity still exist then do some job, if not just return;
                mainActivity.onLogIn(result);
        }

        @Override
        public void onFailed(String error) {
            MainActivity mainActivity = mActivity.get();
            if(mainActivity!=null)//If activity still exist then do some job, if not just return;
                mainActivity.onLogInFailure(error);
        }
    }
}
