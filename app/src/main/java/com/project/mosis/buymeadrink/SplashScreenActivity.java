package com.project.mosis.buymeadrink;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.project.mosis.buymeadrink.Application.MyAplication;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_main);

        Animation animation = AnimationUtils.loadAnimation(this,R.anim.move_up);
        ImageView imageView = (ImageView) findViewById(R.id.logo_splash_screen);
        assert imageView != null;
        imageView.setAnimation(animation);


        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                User user = SaveSharedPreference.GetUser(SplashScreenActivity.this);
                if(user == null)
                {
                    startActivity(new Intent(SplashScreenActivity.this,LogInActivity.class));
                    //TODO: set sharedPreference in logIn activity if is login succesful
                    //TODO: after success login set user var with ((MyApplication) this.getApplication()).setSomeVariable("foo");
                    //example
//                    SaveSharedPreference.SetUser(SplashScreenActivity.this,"someId");
                    finish();
                }else{

                    //TODO: send request to server or SqlLite database to get user info, then call ((MyApplication) this.getApplication()).setSomeVariable("foo");
                    ((MyAplication) SplashScreenActivity.this.getApplication()).setUser(user);
                    //dummydata
                    startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
                    finish();
                }
            }
        }, 400);
    }
}
