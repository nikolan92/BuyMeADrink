package com.project.mosis.buymeadrink;


import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.toolbox.NetworkImageView;
import com.project.mosis.buymeadrink.Application.MyAplication;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.UserHandler;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


public class UserProfileActivity extends AppCompatActivity {
    private NetworkImageView userImage;
    Uri imageUri;

    private EditText inputName,inputEmail,inputNewPassword,inputOldPassword;
    private TextInputLayout inputLayoutName,inputLayoutNewPassword,inputLayoutOldPassword;

    final String REQUSET_TAG = "UserProfileActivity";
    private UserHandler userHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.user_profile_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        User user = ((MyAplication) this.getApplication()).getUser();
        userHandler = new UserHandler(this,user);

        setupInput();
        loadUserInfo(user);
    }
    private void setupInput(){
        inputLayoutName = (TextInputLayout) findViewById(R.id.user_profile_input_layout_name);
        inputLayoutOldPassword = (TextInputLayout) findViewById(R.id.user_profile_input_layout_old_password);
        inputLayoutNewPassword = (TextInputLayout) findViewById(R.id.user_profile_input_layout_new_password);
        inputName = (EditText) findViewById(R.id.user_profile_input_name);
        inputEmail = (EditText) findViewById(R.id.user_profile_input_email);
        inputOldPassword = (EditText) findViewById(R.id.user_profile_input_old_password);
        inputNewPassword = (EditText) findViewById(R.id.user_profile_input_new_password);

        userImage = (NetworkImageView) findViewById(R.id.user_profile_imageView);
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startImagePicker();
            }
        });
    }
    private void loadUserInfo(User user){
        //load image
        userHandler.getUserImage(userImage,REQUSET_TAG);
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
                userImage.setImageURI(imageUri);
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
        userHandler.CancelAllRequestWithTag(REQUSET_TAG);
    }
}
