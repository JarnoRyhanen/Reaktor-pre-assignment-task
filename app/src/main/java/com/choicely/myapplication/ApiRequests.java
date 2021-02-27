package com.choicely.myapplication;

import android.util.Log;

import com.choicely.myapplication.dp.RealmHelper;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ApiRequests {

    private final static String TAG = "ApiRequests";
    private OkHttpClient client = new OkHttpClient();

    public String getData(String item) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES);
        client = builder.build();

        String url = "https://bad-api-assignment.reaktor.com/v2/products/" + item;
        Log.d(TAG, "url: " + url);
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "Failed");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    parseData(response);
                } else {
                    Log.d(TAG, "Response Failed " + response.code());
                    if (apiRequestsFailureListener != null) {
                        apiRequestsFailureListener.onFailure(response.code());
                    }
                }
            }
        });
        return null;
    }

    private ApiRequestsFailureListener apiRequestsFailureListener;

    public interface ApiRequestsFailureListener {
        void onFailure(int errorCode);
    }

    public void setApiRequestsFailureListener(ApiRequestsFailureListener listener) {
        this.apiRequestsFailureListener = listener;
    }

    private void parseData(@NotNull Response response) {
        Log.d(TAG, "Response was successful");

        Runnable runAfterTransaction = () -> {
            if (allItemsDownLoadedListener != null) {
                allItemsDownLoadedListener.onItemDownLoaded();
            }
        };
        RealmHelper.runAsyncRealmTransaction(realm -> {
            try {
                String myResponse = response.body().string();
                Log.d(TAG, "onResponse: " + myResponse);

                JSONArray dataArray = new JSONArray(myResponse);
                Log.d(TAG, "parseData: " + dataArray.length());

                for (int i = 0; i < dataArray.length(); i++) {
                    ItemData itemData = new ItemData();

                    JSONObject obj = dataArray.getJSONObject(i);
                    String name = obj.getString("name");
                    String category = obj.getString("type");
                    String id = obj.getString("id");
                    String manufacturer = obj.getString("manufacturer");

                    itemData.setId(id);
                    itemData.setItemName(name);
                    itemData.setItemCategory(category);
                    itemData.setItemManufacturer(manufacturer);
                    addDataToRealm(realm, itemData);

                    if (oneItemLoadedListener != null) {
                        oneItemLoadedListener.onOneItemLoaded();
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
        }, runAfterTransaction);
    }

    private void addDataToRealm(Realm realm, ItemData itemData) {
        realm.insertOrUpdate(itemData);
    }

    private oneItemLoadedListener oneItemLoadedListener;

    public interface oneItemLoadedListener {
        void onOneItemLoaded();
    }

    public void setOneItemLoadedListener(oneItemLoadedListener listener) {
        this.oneItemLoadedListener = listener;
    }

    private allItemsDownLoadedListener allItemsDownLoadedListener;

    public interface allItemsDownLoadedListener {
        void onItemDownLoaded();
    }

    public void setListener(allItemsDownLoadedListener listener) {
        this.allItemsDownLoadedListener = listener;
    }
}
//                int price = Integer.parseInt(obj.getString("color"));
