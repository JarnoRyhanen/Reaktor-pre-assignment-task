package com.choicely.myapplication.app;

import android.app.Application;
import android.util.Log;

public class WareHouseApp extends Application {

    private static final String TAG = "WareHouseApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: App started");

    }
}
