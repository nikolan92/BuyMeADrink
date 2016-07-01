package com.project.mosis.buymeadrink;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.project.mosis.buymeadrink.Application.MyApplication;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class LogInActivity extends AppCompatActivity {
    private EditText inputEmail,inputPassword;
    private TextInputLayout inputLayoutEmail,inputLayoutPassword;
    private final String REQUEST_TAG = "LogInActivity";

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        final Button signUp = (Button)findViewById(R.id.login_btn_sign_up);
        final Button logIn = (Button) findViewById(R.id.login_btn_log_in);

        inputEmail = (EditText) findViewById(R.id.login_input_email);
        inputPassword = (EditText) findViewById(R.id.login_input_password);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.login_input_layout_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.login_input_layout_password);

        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));

        assert signUp != null;
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LogInActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });
        assert logIn != null;
        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }

    /**
     * Static inner classes do not hold an implicit reference to their outher clases, so activity will not be leaked.
     * Also because i need to access to an activity method i need to hold a reference to it. But i keep weakReference,
     * so GC will not be prevented from deleting it. Because of that i need to check whether activity still exist.
     * */
    private static class OnLogInListener implements VolleyCallBack {
        private final WeakReference<LogInActivity> mActivity;
        OnLogInListener(LogInActivity logInActivity){
            mActivity = new WeakReference<>(logInActivity);
        }
        @Override
        public void onSuccess(JSONObject result) {
            LogInActivity logInActivity = mActivity.get();
            if(logInActivity!=null)//If activity still exist then do some job, if not just return;
                logInActivity.onLogIn(result);
        }

        @Override
        public void onFailed(String error) {
            LogInActivity logInActivity = mActivity.get();
            if(logInActivity!=null)//If activity still exist then do some job, if not just return;
                logInActivity.onLogInFailure(error);
        }
    }

    private void onLogIn(JSONObject result) {
        progressDialog.dismiss();
        try{
            if(result.getBoolean("Success"))
            {
                User user = new Gson().fromJson(result.getString("Data"),User.class);


                //set just logged user as global variable (this var live together with app)
                ((MyApplication) LogInActivity.this.getApplication()).setUser(user);

                //store user in local storage with sharedPreference
                SaveSharedPreference.SetUser(LogInActivity.this,user);

                //start mainActivity and clear back stack
                startActivity(new Intent(LogInActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();

            }else{
                Toast.makeText(this,result.getString("Error"),Toast.LENGTH_LONG).show();
            }
        }catch (JSONException exception){
            Log.e("LogInActivity",exception.toString());
        }
    }
    private void onLogInFailure(String error) {
        progressDialog.dismiss();
        Toast.makeText(this,"Volley error during logIn:"+ error.toString(),Toast.LENGTH_LONG).show();
    }

    /**
     *Validation form
     */
    private void submitForm() {
        if (!validateEmail()) {
            return;
        }

        if (!validatePassword()) {
            return;
        }
        progressDialog = ProgressDialog.show(this,"Please wait","LogIn in progress...",false,false);
        //TODO:Make request to server and if log in is ok set user variable as global variable and set shared preference
        UserHandler.logIn(this,inputEmail.getText().toString(),inputPassword.getText().toString(), REQUEST_TAG,new OnLogInListener(LogInActivity.this));
    }
    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
//            String status = inputLayoutEmail.isErrorEnabled()?"true":"false";
//            Toast.makeText(this,status+" "+inputLayoutEmail.getError(),Toast.LENGTH_LONG).show();
//            inputLayoutEmail.setErrorEnabled(true);
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
            return true;
        }
    }
    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
//            inputLayoutPassword.setErrorEnabled(true);
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
            return true;
        }
    }
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.login_input_email:
                    validateEmail();
                    break;
                case R.id.login_input_password:
                    validatePassword();
                    break;
            }
        }
    }
    /**
     *End of validation form
     */

}
