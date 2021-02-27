package com.choicely.myapplication.dp;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.choicely.myapplication.BuildConfig;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmHelper {

    private static final String TAG = "RealmHelper";
    private RealmConfiguration config;

    private static final String REALM_NAME = "WareHouse.realm";
    private static final int REALM_VERSION = RealmHistory.VERSION_1;

    private Realm realm;
    private ExecutorService thread;
    private Realm threadRealm;

    private static class RealmHistory {
        static final int VERSION_1 = 1;
    }

    private static RealmHelper instance;

    private RealmHelper() {
    }

    public static RealmHelper getInstance() {
        if (instance == null) {
            throw new IllegalStateException(TAG + " is not initialized!");
        }
        return instance;
    }

    public static void init(Context context) {
        if (instance != null) {
            throw new IllegalStateException(TAG + " is already initialized!");
        }

        instance = new RealmHelper();

        // Initialize Realm (just once per application)
        Realm.init(context);

        // The RealmConfiguration is created using the builder pattern.
        // The Realm file will be located in Context.getFilesDir() with name "myrealm.realm"
        instance.config = new RealmConfiguration.Builder()
                .name(REALM_NAME)
                .schemaVersion(REALM_VERSION)
                .allowWritesOnUiThread(true)
                .deleteRealmIfMigrationNeeded()
                .build();
        // Use the config
        instance.realm = Realm.getInstance(instance.config);

        instance.thread = Executors.newSingleThreadExecutor();
        instance.thread.execute(() -> instance.threadRealm = Realm.getInstance(instance.config));
    }

    public static void runAsyncRealmTransaction(Realm.Transaction transaction, @Nullable Runnable runAfterTransaction) {
        instance.thread.execute(() -> {
            instance.threadRealm.executeTransaction(transaction);
            if (runAfterTransaction != null) {
                runAfterTransaction.run();
            }
        });
    }

    public Realm getCurrentThreadRealm() {
        return Realm.getInstance(instance.config);
    }

    public Realm getRealm() {
        return realm;
    }
}
