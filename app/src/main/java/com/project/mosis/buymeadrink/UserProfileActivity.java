package com.project.mosis.buymeadrink;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.project.mosis.buymeadrink.Application.MyApplication;
import com.project.mosis.buymeadrink.Application.SaveSharedPreference;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;


public class UserProfileActivity extends AppCompatActivity {
    private NetworkImageView userImage;
    private Uri imageUri;
    private User user,updatedUser;
    private boolean isImageChanged = false;

    private EditText inputName,inputEmail,inputNewPassword,inputOldPassword;
    private TextInputLayout inputLayoutName,inputLayoutNewPassword,inputLayoutOldPassword;

    private final String REQUEST_TAG = "UserProfileActivity";
    private final String LOG_TAG = "UserProfileActivity";
    private UserHandler userHandler;

    private ProgressDialog progressDialog;
    private CoordinatorLayout coordinatorLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.user_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.user_profile_coordinator_layout);

        user = ((MyApplication) this.getApplication()).getUser();
        //I making new user because i need copy of user, not a reference. Real user object is stored in MyApplication class and its global for all activity
        //so i can't change that object until everything is success
        updatedUser = new User(user.getId(),user.getName(),user.getEmail(),user.getPassword(),user.getFriends(),user.getRating());
        userHandler = new UserHandler(this);

        setupInput();
        loadUserInfo(user);
    }
    /**
     *Layout Setup
     *=================================================================================================
     **/
    private void setupInput(){
        inputLayoutName = (TextInputLayout) findViewById(R.id.user_profile_input_layout_name);
        inputLayoutOldPassword = (TextInputLayout) findViewById(R.id.user_profile_input_layout_old_password);
        inputLayoutNewPassword = (TextInputLayout) findViewById(R.id.user_profile_input_layout_new_password);
        inputName = (EditText) findViewById(R.id.user_profile_input_name);
        inputEmail = (EditText) findViewById(R.id.user_profile_input_email);
        inputOldPassword = (EditText) findViewById(R.id.user_profile_input_old_password);
        inputNewPassword = (EditText) findViewById(R.id.user_profile_input_new_password);

        //key changed listener
        inputName.addTextChangedListener(new MyTextWatcher(inputName));
        inputNewPassword.addTextChangedListener(new MyTextWatcher(inputNewPassword));
        inputOldPassword.addTextChangedListener(new MyTextWatcher(inputOldPassword));

        userImage = (NetworkImageView) findViewById(R.id.user_profile_imageView);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startImagePicker();
            }
        });

        final Button saveBtn = (Button)findViewById(R.id.user_profile_save_btn);
        assert saveBtn!=null;
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
    }
    private void loadUserInfo(User user){
        //load image
        userHandler.getUserImage(user.getId(),userImage);
        inputName.setText(user.getName());
        inputEmail.setText(user.getEmail());
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void startImagePicker(){
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            CropImage.startPickImageActivity(this);
        }
    }
    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage,
            // but we don't know if we need to for the URI so the simplest is to try open the stream and see if we get error.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {

                // request permissions and handle the result in onRequestPermissionsResult()
                this.imageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {

                    CropImage.activity(imageUri)
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setFixAspectRatio(true)
                            .start(this);

            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                try {
                    //scale image to 500x500
                    Bitmap croppedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imageUri);
                    Bitmap scaledImage = Bitmap.createScaledBitmap(croppedImage,500,500,false);
                    userImage.setImageBitmap(scaledImage);
                } catch (IOException e) {
                    Log.e(LOG_TAG,e.toString());
                }
                //userImage.setImageURI(imageUri);
                isImageChanged= true;
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("UserProfileActivity",error.toString());
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (userImage != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //if we get permission for read external storage then start crop image after that we show image
                CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setFixAspectRatio(true)
                        .start(this);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        userHandler.cancelAllRequestWithTag(REQUEST_TAG);
    }

    private User collectUserInfo(){
        updatedUser.setName(inputName.getText().toString());
        if(!inputOldPassword.getText().toString().isEmpty())
            updatedUser.setPassword(inputNewPassword.getText().toString());
        return updatedUser;
    }
    /**
     * Validating form
     */
    private void submitForm() {
        if (!validateName()) {
            return;
        }
        if(!inputOldPassword.getText().toString().isEmpty()){
            if (!validateOldPassword()) {
                return;
            }
            if (!validatePassword()) {
                return;
            }
        }
        inputLayoutNewPassword.setError("");
        inputLayoutOldPassword.setError("");
        if(isImageChanged){
            //TODO:Set progress bar
            progressDialog = ProgressDialog.show(UserProfileActivity.this,"Please wait","Updating user info...",true,false);
            Bitmap userImageBitmap= ((BitmapDrawable)userImage.getDrawable()).getBitmap();
            userHandler.updateUserInfoAndPicture(collectUserInfo(), REQUEST_TAG,userImageBitmap,new OnUserUpdateListener(this));
        }else{
            //TODO:Set progress bar
            progressDialog = ProgressDialog.show(UserProfileActivity.this,"Please wait","Updating user info...",true,false);
            userHandler.updateUserInfo(collectUserInfo(), REQUEST_TAG,new OnUserUpdateListener(this));
        }
        //TODO:make request to server if everything ok then set shared preference and set global var just like in logIn activity
    }

    private boolean validateOldPassword() {
        if (!inputOldPassword.getText().toString().equals(user.getPassword())) {
            inputLayoutOldPassword.setError(getString(R.string.err_msg_old_password));
            requestFocus(inputOldPassword);
            return false;
        } else {
            inputLayoutOldPassword.setError("");
            return true;
        }
    }
    private boolean validatePassword() {
        if (inputNewPassword.getText().toString().trim().isEmpty()) {
            inputLayoutNewPassword.setError(getString(R.string.err_msg_password));
            requestFocus(inputNewPassword);
            return false;
        } else {
            inputLayoutNewPassword.setError("");
            return true;
        }
    }
    private boolean validateName() {
        if (inputName.getText().toString().trim().isEmpty()) {
            inputLayoutName.setError(getString(R.string.err_msg_name));
            requestFocus(inputName);
            return false;
        } else {
            inputLayoutName.setError("");
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
                case R.id.user_profile_input_name:
                    validateName();
                    break;
                case R.id.user_profile_input_old_password:
                    validateOldPassword();
                    break;
                case R.id.user_profile_input_new_password:
                    validatePassword();
                    break;
            }
        }
    }
    /**
     * End of validating form
     */

    /**
     * Static inner classes do not hold an implicit reference to their outher clases, so activity will not be leaked.
     * Also because i need to access to an activity method i need to hold a reference to it. But i keep weakReference,
     * so GC will not be prevented from deleting it. Because of that i need to check whether activity still exist.
     * */
    private static class OnUserUpdateListener implements VolleyCallBack {
        private final WeakReference<UserProfileActivity> mActivity;
        OnUserUpdateListener(UserProfileActivity userProfileActivity){
            mActivity = new WeakReference<>(userProfileActivity);
        }
        @Override
        public void onSuccess(JSONObject result) {
            UserProfileActivity userProfileActivity = mActivity.get();
            if(userProfileActivity!=null)//If activity still exist then do some job, if not just return;
                userProfileActivity.onUserUpdate(result);
        }

        @Override
        public void onFailed(String error) {
            UserProfileActivity userProfileActivity = mActivity.get();
            if(userProfileActivity!=null)//If activity still exist then do some job, if not just return;
            {
                userProfileActivity.onUserUpdateFailure(error);
                userProfileActivity.progressDialog.dismiss();
            }
        }
    }

    private void onUserUpdate(JSONObject result) {
        try {
            if (result.getBoolean("Success")) {
                //Set new user as global
                ((MyApplication)this.getApplication()).setUser(updatedUser);
                //Saving new User on storage for future use
                SaveSharedPreference.SetUser(this,updatedUser);
                progressDialog.dismiss();
                //TODO:return in main activity with RESULT_OK
                setResult(RESULT_OK);
                finish();
            }else{
                Snackbar.make(coordinatorLayout,"Something goes wrong, try again later.",Snackbar.LENGTH_LONG).show();
                //Toast.makeText(this,"Something goes wrong. Try again later.",Toast.LENGTH_LONG).show();
            }
        }catch (JSONException exception){
            Toast.makeText(this,exception.toString(),Toast.LENGTH_LONG).show();
            Log.e("UserProfileActivity",exception.toString());
        }

    }
    private void onUserUpdateFailure(String error) {
        Snackbar.make(coordinatorLayout,"Failed to update user, try again later.",Snackbar.LENGTH_LONG).show();
        Log.e("UserProfileActivity",error);
    }
}
