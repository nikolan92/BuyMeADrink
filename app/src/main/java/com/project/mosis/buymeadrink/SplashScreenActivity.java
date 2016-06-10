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
        imageView.setAnimation(animation);


        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(SaveSharedPreference.GetUserId(SplashScreenActivity.this).length() == 0)
                {
                    startActivity(new Intent(SplashScreenActivity.this,LogInActivity.class));
                    //TODO: set sharedPreference in logIn activity if is login succesful
                    //TODO: after success login set user var with ((MyApplication) this.getApplication()).setSomeVariable("foo");
                    //example
                    SaveSharedPreference.SetUserId(SplashScreenActivity.this,"someId");
                    finish();
                }else{

                    //TODO: send request to server or SqlLite database to get user info, then call ((MyApplication) this.getApplication()).setSomeVariable("foo");
                    ((MyAplication) SplashScreenActivity.this.getApplication()).setUser(new User("43243243","Nikola","Nikolic","92nikolan@gmail.com","someUrl"));
                    //dummydata
                    startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
                    finish();
                }
            }
        }, 300);
    }
}
