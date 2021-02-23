package com.choicely.myapplication.app;

import android.app.Application;
import android.util.Log;

import com.choicely.myapplication.dp.RealmHelper;

public class WareHouseApp extends Application {

    private static final String TAG = "WareHouseApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: App started");
        RealmHelper.init(this);
    }
}
