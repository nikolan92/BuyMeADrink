package com.project.mosis.buymeadrink.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleyHelperSingleton {
    private static VolleyHelperSingleton mInstance;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static Context mCtx;

    private VolleyHelperSingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        mImageLoader = new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {

            private final LruCache<String, Bitmap>
                    cache = new LruCache<String, Bitmap>(20);//number of entries 20 a ako overridujem sizeOf onda bi bila max velicina u bytima

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }
    public static synchronized VolleyHelperSingleton getInstance(Context context){
        if(mInstance == null){
            mInstance = new VolleyHelperSingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue(){
        if(mRequestQueue == null){
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }
    public <T> void addToRequsetQueueWithTag(Request<T> request,  String tag){
        request.setTag(tag);
        getRequestQueue().add(request);
    }
    public <T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);
    }
    public ImageLoader getImageLoader(){
        return mImageLoader;
    }
    //Tag moze da bude bilo koja klasa tj Object class ali ogranicio sam je na string zato sto nema potrebe da bude Object
    public void cancelPendingRequests(String tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
//exemple of use
//    // Get a RequestQueue
//    RequestQueue queue = MySingleton.getInstance(this.getApplicationContext()).
//            getRequestQueue();
//
//// ...
//
//// Add a request (in this example, called stringRequest) to your RequestQueue.
//MySingleton.getInstance(this).addToRequestQueue(stringRequest);