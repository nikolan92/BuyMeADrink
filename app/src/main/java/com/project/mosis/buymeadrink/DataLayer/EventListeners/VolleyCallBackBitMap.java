package com.project.mosis.buymeadrink.DataLayer.EventListeners;

import android.graphics.Bitmap;

public interface VolleyCallBackBitmap {

    void onSuccess(Bitmap result);

    void onFailed(String error);
}
