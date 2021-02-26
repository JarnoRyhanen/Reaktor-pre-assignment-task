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
    private int itemCategoryCount = 0;

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
                }
            }
        });
        return null;
    }


    private void parseData(@NotNull Response response) {
        Log.d(TAG, "Response was successful");
        try {
            String myResponse = response.body().string();
            Log.d(TAG, "onResponse: " + myResponse);
            JSONArray dataArray = new JSONArray(myResponse);
            Log.d(TAG, "parseData: " + dataArray.length());
            Log.d(TAG, "parseData: STARTING TO PARSE DATA BRRRRRRRRRRR");

            for (int i = 0; i < dataArray.length(); i++) {
                ItemData itemData = new ItemData();
                itemCategoryCount++;
                Log.d(TAG, "parseData: " + itemCategoryCount);

                JSONObject obj = dataArray.getJSONObject(i);
                String name = obj.getString("name");
                String category = obj.getString("type");
                String id = obj.getString("id");
                String manufacturer = obj.getString("manufacturer");

                itemData.setId(id);
                itemData.setItemName(name);
                itemData.setItemCategory(category);
                itemData.setItemManufacturer(manufacturer);

                addDataToRealm(itemData);
            }
            if (listener != null) {
                listener.onItemDownLoaded();
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void addDataToRealm(ItemData itemData) {
        Realm realm = RealmHelper.getInstance().getCurrentThreadRealm();

        realm.executeTransaction(realm1 -> realm.copyToRealmOrUpdate(itemData));
        realm.close();
    }


    private allItemsDownLoadedListener listener;

    public interface allItemsDownLoadedListener {
        void onItemDownLoaded();
    }

    public void setListener(allItemsDownLoadedListener listener) {
        this.listener = listener;
    }
}
//                int price = Integer.parseInt(obj.getString("color"));
