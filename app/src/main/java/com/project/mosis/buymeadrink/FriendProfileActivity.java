package com.project.mosis.buymeadrink;

import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.android.volley.toolbox.NetworkImageView;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;

public class FriendProfileActivity extends AppCompatActivity {

    private UserHandler userHandler;
    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_profile);
        toolbar = (Toolbar) findViewById(R.id.friend_profile_toolbar);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.friend_profile_toolbar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        NetworkImageView friendImage = (NetworkImageView) findViewById(R.id.friend_profile_imageView);

        collapsingToolbarLayout.setTitle("Nikola Nikolic");

        userHandler = new UserHandler(this);
        userHandler.getUserImage("5773114c34721ecc0a000029",friendImage);
    }
}
