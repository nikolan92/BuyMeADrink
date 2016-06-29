package com.project.mosis.buymeadrink.DataLayer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.google.gson.Gson;
import com.project.mosis.buymeadrink.DataLayer.DataObject.User;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBack;
import com.project.mosis.buymeadrink.DataLayer.EventListeners.VolleyCallBackBitmap;
import com.project.mosis.buymeadrink.R;
import com.project.mosis.buymeadrink.Utils.Constants;
import com.project.mosis.buymeadrink.Utils.VolleyHelperSingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
public class UserHandler {

    private VolleyHelperSingleton mVolleyHelper;
    private Handler mHandler = null;

    public UserHandler(Context context){

        this.mVolleyHelper = VolleyHelperSingleton.getInstance(context);
    }

    public static void logIn(Context context, String email , String password, String tag, final VolleyCallBack volleyCallBack){

        VolleyHelperSingleton mVolleyHelper = VolleyHelperSingleton.getInstance(context);
        JSONObject jsonData = new JSONObject();
        try{
            jsonData.put("email",email);
            jsonData.put("password",password);
        }catch (JSONException exception){
            Log.e("UserHandler",exception.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.LOG_IN_URL, jsonData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(volleyCallBack!=null)
                    volleyCallBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(volleyCallBack!=null)
                    volleyCallBack.onFailed(error.toString());
            }
        }
        );
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }
    public static void register(Context context, User user, String tag, final VolleyCallBack  volleyCallBack){

        VolleyHelperSingleton mVolleyHelper = VolleyHelperSingleton.getInstance(context);
        JSONObject jsonData= null;
        try {
            jsonData = new JSONObject(new Gson().toJson(user));
        }catch (JSONException exception){
            Log.e("UserHandler",exception.toString());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.USER_URL, jsonData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(volleyCallBack!=null)
                    volleyCallBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(volleyCallBack!=null)
                    volleyCallBack.onFailed(error.toString());
            }
        }
        );
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }
    public void updateUserInfo(User newUser, String tag, final VolleyCallBack  volleyCallBack){
        JSONObject jsonData= null;
        try {
            jsonData = new JSONObject(new Gson().toJson(newUser));
        }catch (JSONException exception){
            Log.e("UserHandler",exception.toString());
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, Constants.USER_URL, jsonData, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(volleyCallBack!=null)
                    volleyCallBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(volleyCallBack!=null)
                    volleyCallBack.onFailed(error.toString());
            }
        }
        );
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }
    public void updateUserInfoAndPicture(User newUser, String tag, final Bitmap newUserImage, final VolleyCallBack  volleyCallBack){
        BitmapToBase64 bitmapToBase64 = new BitmapToBase64(newUser,tag,newUserImage,volleyCallBack);
        mHandler = new Handler();
        mHandler.post(bitmapToBase64);
    }

    public void getUserImage(String userID, final NetworkImageView imageView){

        ImageLoader mImageLoader =  mVolleyHelper.getImageLoader();
        mImageLoader.get(Constants.USER_IMAGE_URL + userID + ".jpg",
                ImageLoader.getImageListener(imageView, R.mipmap.ic_default_user_image,R.mipmap.ic_default_user_image));

        imageView.setImageUrl(Constants.USER_IMAGE_URL + userID +".jpg",mImageLoader);
    }
    public void getUserImageInBitmap(String userID,String tag, final VolleyCallBackBitmap volleyCallBackBitmap){
        ImageRequest imageRequest = new ImageRequest(Constants.USER_IMAGE_URL + userID + ".jpg", new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                volleyCallBackBitmap.onSuccess(response);
            }
        }, 100, 100, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565 , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyCallBackBitmap.onFailed(error.toString());
            }
        });
        imageRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(imageRequest);
    }
    //Return all friends of user with given id.
    public void getUserFriends(String tag, String userID, final VolleyCallBack volleyCallBack){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_FRIENDS_URL+userID, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(volleyCallBack!=null)
                    volleyCallBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(volleyCallBack!=null)
                    volleyCallBack.onFailed(error.toString());
            }
        });
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }

    public static void getUserStatic(Context context,String userID,String tag,final VolleyCallBack volleyCallBack){
        VolleyHelperSingleton mVolleyHelper = VolleyHelperSingleton.getInstance(context);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_URL+"/"+userID, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(volleyCallBack!=null)
                    volleyCallBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(volleyCallBack!=null)
                    volleyCallBack.onFailed(error.toString());
            }
        });
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }
    public static void getUserBitmapStatic(Context context,String userID,String tag,final VolleyCallBackBitmap volleyCallBackBitmap){
        VolleyHelperSingleton mVolleyHelper = VolleyHelperSingleton.getInstance(context);
        ImageRequest imageRequest = new ImageRequest(Constants.USER_IMAGE_URL + userID + ".jpg", new Response.Listener<Bitmap>() {
            @Override
            public void onResponse(Bitmap response) {
                volleyCallBackBitmap.onSuccess(response);
            }
        }, 200, 200, ImageView.ScaleType.FIT_XY, Bitmap.Config.RGB_565 , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                volleyCallBackBitmap.onFailed(error.toString());
            }
        });
        imageRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(imageRequest);
    }

    public void  getAllUsers(String tag,final VolleyCallBack  volleyCallBack){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, Constants.USER_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if(volleyCallBack!=null)
                    volleyCallBack.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(volleyCallBack!=null)
                    volleyCallBack.onFailed(error.toString());
            }
        });
        jsonObjectRequest.setTag(tag);
        mVolleyHelper.addToRequestQueue(jsonObjectRequest);
    }
    public void cancelAllRequestWithTag(String tag){
        mVolleyHelper.cancelPendingRequests(tag);
        if(mHandler!=null){
            //mHandler.removeCallbacks(null);
        }
        //TODO: When activity call onStop() this function must be caled because if you not call this fun Volley will call your handler and app will crash
    }
    public static void CancelAllRequestWithTagStatic(Context context,String tag){
        VolleyHelperSingleton mVolleyHelper = VolleyHelperSingleton.getInstance(context);
        mVolleyHelper.cancelPendingRequests(tag);
    }
    /**
     *<p>This thread convert bitmap in Base64 string, after that make volley request.</p>
     * */
    private class BitmapToBase64 implements Runnable{
        User user;
        VolleyCallBack volleyCallBack;
        Bitmap newUserImage;
        String tag;
        public BitmapToBase64(User user, String tag, Bitmap newUserImage, VolleyCallBack  volleyCallBack){
            this.user = user;
            this.tag = tag;
            this.newUserImage = newUserImage;
            this.volleyCallBack = volleyCallBack;
        }
        @Override
        public void run() {
            //Making base64 string-----------------------
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            newUserImage.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
            String base64 = Base64.encodeToString(byteArrayOutputStream.toByteArray(),Base64.DEFAULT);
            //End of Making base64 string----------------
            JSONObject jsonData= null;
            try {
                jsonData = new JSONObject(new Gson().toJson(user));
                jsonData.put("image_base64",base64);
            }catch (JSONException exception){
                Log.e("UserHandler",exception.toString());
            }
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, Constants.USER_URL, jsonData, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    if(volleyCallBack!=null) {
                        volleyCallBack.onSuccess(response);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if(volleyCallBack!=null)
                        volleyCallBack.onFailed(error.toString());
                }
            });
            jsonObjectRequest.setTag(tag);
            mVolleyHelper.addToRequestQueue(jsonObjectRequest);
        }
    }
}
