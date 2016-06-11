package com.project.mosis.buymeadrink;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.project.mosis.buymeadrink.Application.MyAplication;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final Button signUp = (Button) findViewById(R.id.register_act_sign_up_btn);

        assert signUp != null;
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:Make request to server and if register is ok set user variable as global variable and set shared preference


                User user = new User("id","Nikola","Nikolic","nikolan92@hotmail.com","id");

                //set just loged user as gloabal variable (this var live togeder with app)
                ((MyAplication) RegisterActivity.this.getApplication()).setUser(user);

                //store user in local storage with sharedPreference
                SaveSharedPreference.SetUser(RegisterActivity.this,user);

                //start mainActivity and clear back stack
                startActivity(new Intent(RegisterActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
            }
        });
    }
}
