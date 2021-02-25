package com.choicely.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateItems extends BroadcastReceiver {
    private static final String TAG = "UpdateItems";
    private final WareHouseActivity wareHouseActivity = new WareHouseActivity();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "All data has been reloaded");
        wareHouseActivity.downloadItems();
    }
}
