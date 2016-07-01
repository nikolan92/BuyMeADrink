package com.project.mosis.buymeadrink;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.project.mosis.buymeadrink.Application.MyApplication;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_main);

        ImageView imageView = (ImageView) findViewById(R.id.logo_splash_screen);
        assert imageView != null;

        final Handler handler = new Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                User user = SaveSharedPreference.GetUser(SplashScreenActivity.this);
                if(user == null)
                {
                    startActivity(new Intent(SplashScreenActivity.this,LogInActivity.class));
                    //TODO: set sharedPreference in logIn activity if is login successful
                    //TODO: after success login set user var with ((MyApplication) this.getApplication()).setSomeVariable("foo");

                    finish();
                }else{
                    //restore service settings, and set as global var
                    boolean serviceSettings = SaveSharedPreference.getServiceSettings(SplashScreenActivity.this);
                    ((MyApplication) SplashScreenActivity.this.getApplication()).setServiceSetings(serviceSettings);

                    //set user as global var
                    ((MyApplication) SplashScreenActivity.this.getApplication()).setUser(user);

                    startActivity(new Intent(SplashScreenActivity.this,MainActivity.class));
                    finish();
                }
            }
        });
    }
}
