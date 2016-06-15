package com.project.mosis.buymeadrink;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;

import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.arlib.floatingsearchview.FloatingSearchView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.project.mosis.buymeadrink.Application.MyAplication;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;
import com.project.mosis.buymeadrink.SearchResultData.SearchResult;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private FloatingSearchView mSearchView;
    private DrawerLayout drawer;
    private GoogleMap mMap;
    UserHandler userHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mSearchView = (FloatingSearchView)findViewById(R.id.floating_search_view);

        //TODO:Get user from global var and make userHandler with that user
        User user = ((MyAplication) MainActivity.this.getApplication()).getUser();
        userHandler = new UserHandler(this,user);

        setupFloatingSearch();
        setupDrawer();

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(-34, 151);
        MarkerOptions markerOptions = new MarkerOptions().position(sydney).title("Marker in Sydney");
        Marker m = mMap.addMarker(markerOptions);

        m.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher));//moze kasnije dodavanje ikone

        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        //markerOptions.

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
        final TextView name = (TextView) hView.findViewById(R.id.nav_user_name);
        final TextView email = (TextView) hView.findViewById(R.id.nav_user_email);

        assert name != null;
        name.setText(userHandler.GetUser().getName());
        assert email != null;
        email.setText(userHandler.GetUser().getEmail());

        assert drawer != null;
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                mSearchView.setLeftMenuOpen(false);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
//            if(isSearchMenuOpen) {
//                persistentSearch.closeMenu(true);
//                isSearchMenuOpen = false;
//            }
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

        } else if (id == R.id.nav_my_profile) {
            Intent i = new Intent(this, UserProfileActivity.class);
            startActivity(new Intent(this,UserProfileActivity.class));

        } else if (id == R.id.nav_add_friend) {

        } else if (id == R.id.nav_my_friends) {

        } else if (id == R.id.nav_setings) {

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
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onDestroy() {
        //Toast.makeText(this,"MainActivity Destroyed.",Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

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
    private static class OnLogInListener implements VolleyCallBack{
        private final WeakReference<MainActivity> mActivity;
        OnLogInListener(MainActivity mainActivity){
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
