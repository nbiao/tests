package com.hb.plantrecognition;

import android.app.Application;

/**
 * Created by HB on 2017/5/26.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RetrofitFactory.init(this);
    }
}
