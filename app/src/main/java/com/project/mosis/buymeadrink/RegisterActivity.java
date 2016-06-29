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
import com.project.mosis.buymeadrink.Application.MyAplication;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

public class RegisterActivity extends AppCompatActivity {

    private Button signUp;
    private EditText inputName,inputEmail,inputPassword;
    private TextInputLayout inputLayoutName,inputLayoutEmail,inputLayoutPassword;
    private final String REQUEST_TAG = "RegisterActivity";
    private ProgressDialog progressDialog;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signUp = (Button) findViewById(R.id.register_btn_sign_up);
        inputLayoutName = (TextInputLayout) findViewById(R.id.register_input_layout_name);
        inputLayoutEmail = (TextInputLayout) findViewById(R.id.register_input_layout_email);
        inputLayoutPassword = (TextInputLayout) findViewById(R.id.register_input_layout_password);
        inputEmail = (EditText) findViewById(R.id.register_input_email);
        inputPassword = (EditText) findViewById(R.id.register_input_password);
        inputName = (EditText) findViewById(R.id.register_input_name);

        inputName.addTextChangedListener(new MyTextWatcher(inputName));
        inputEmail.addTextChangedListener(new MyTextWatcher(inputEmail));
        inputPassword.addTextChangedListener(new MyTextWatcher(inputPassword));

        assert signUp != null;
        signUp.setOnClickListener(new View.OnClickListener() {
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
    private static class OnRegisterListener implements VolleyCallBack {
        private final WeakReference<RegisterActivity> mActivity;
        OnRegisterListener(RegisterActivity registerActivity){
            mActivity = new WeakReference<>(registerActivity);
        }
        @Override
        public void onSuccess(JSONObject result) {
            RegisterActivity registerActivity = mActivity.get();
            if(registerActivity!=null)//If activity still exist then do some job, if not just return;
                registerActivity.onRegister(result);
        }

        @Override
        public void onFailed(String error) {
            RegisterActivity registerActivity = mActivity.get();
            if(registerActivity!=null)//If activity still exist then do some job, if not just return;
                registerActivity.onRegisterFailure(error);
        }
    }
    private void onRegister(JSONObject result){
        progressDialog.dismiss();
        try{
            if(result.getBoolean("Success"))
            {
                User user = new Gson().fromJson(result.getString("Data"),User.class);

                String name = user.getName().split(" ")[0];
                name = (name.equals(""))?user.getName():name;//case when user don't enter fullName

                //set just logged user as global variable (this var live together with app)
                ((MyAplication) RegisterActivity.this.getApplication()).setUser(user);

                //store user in local storage with sharedPreference
                SaveSharedPreference.SetUser(RegisterActivity.this,user);

                //start mainActivity and clear back stack
                startActivity(new Intent(RegisterActivity.this,MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                Toast.makeText(this, "Welcome to our application "+ name, Toast.LENGTH_SHORT).show();
                finish();

            }else{
                Toast.makeText(this,result.getString("Error"),Toast.LENGTH_LONG).show();
            }
        }catch (JSONException exception){
            Log.e("LogInActivity",exception.toString());
        }
    }
    private void onRegisterFailure(String error){
        progressDialog.dismiss();
        Toast.makeText(this,"Volley error during register:"+ error.toString(),Toast.LENGTH_LONG).show();
    }
    //TODO:See logIn Activity Note:When data arive from server use Gson libraty to make user object from json
    /**
     * Validating form
     */
    private void submitForm() {
        if (!validateName()) {
            return;
        }

        if (!validateEmail()) {
            return;
        }

        if (!validatePassword()) {
            return;
        }
        progressDialog = ProgressDialog.show(this,"Please wait","Registation in progress",false,false);
        //TODO:make request to server if everything ok then set shared preference and set global var just like in logIn activity
        UserHandler.register(this,collectUserInfo(), REQUEST_TAG,new OnRegisterListener(this));
    }
    private User collectUserInfo(){
        return new User(inputName.getText().toString(),inputEmail.getText().toString(),inputPassword.getText().toString());
    }

    private boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validateEmail() {
        String email = inputEmail.getText().toString().trim();

        if (email.isEmpty() || !isValidEmail(email)) {
            inputLayoutEmail.setError(getString(R.string.err_msg_email));
            requestFocus(inputEmail);
            return false;
        } else {
            inputLayoutEmail.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.err_msg_password));
            requestFocus(inputPassword);
            return false;
        } else {
            inputLayoutPassword.setErrorEnabled(false);
            return true;
        }
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
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
                case R.id.register_input_name:
                    validateName();
                    break;
                case R.id.register_input_email:
                    validateEmail();
                    break;
                case R.id.register_input_password:
                    validatePassword();
                    break;
            }
        }
    }
}
