package com.choicely.myapplication;

import android.util.Log;
import android.util.Pair;
import android.util.Xml;

import com.choicely.myapplication.dp.RealmHelper;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class XmlParser {

    private final static String TAG = "XmlParser";
    private static final String ns = null;

    private OkHttpClient client = new OkHttpClient();

    public void getManufacturerXmlData(String manufacturer) {

        String url = "https://bad-api-assignment.reaktor.com/v2/availability/" + manufacturer;
        Log.d(TAG, "url: " + url);

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.MINUTES)
                .writeTimeout(5, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES);
        client = builder.build();

        Request requestXml = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(requestXml).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.d(TAG, "Failed");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                    parseJsonData(response, manufacturer);
                } else {
                    Log.d(TAG, "Response Failed " + response.code());
                    if (xmlParserFailureListener != null) {
                        xmlParserFailureListener.onFailure(response.code());
                        Log.d(TAG, "onResponse: failure listener activated");
                    }
                }
            }
        });
    }

    private XmlParserFailureListener xmlParserFailureListener;

    public interface XmlParserFailureListener {
        void onFailure(int errorCode);
    }

    public void setXmlParserFailureListener(XmlParserFailureListener listener) {
        this.xmlParserFailureListener = listener;
    }

    private void parseJsonData(Response response, String manufacturer) {

        Log.d(TAG, "Response was successful");
        Runnable runAfterTransaction = () -> {
            if (onSuccessListener != null) {
                Log.d(TAG, "parseJsonData: listener heard");
                onSuccessListener.onSuccess();
            }
        };
        RealmHelper.runAsyncRealmTransaction(realm -> {
            try {
                Log.d(TAG, "parseJsonData: " + response.code());
                String myResponse = response.body().string();

                if (myResponse != null) {

                    JSONObject jsonObject = new JSONObject(myResponse);
                    JSONArray dataArray = jsonObject.getJSONArray("response");
                    Log.d(TAG, "parseData: " + dataArray.length());

                    for (int i = 0; i < dataArray.length(); i++) {

                        JSONObject obj = dataArray.getJSONObject(i);
                        String itemID = obj.getString("id").toLowerCase();

                        String xmlPayLoad = obj.getString("DATAPAYLOAD");

                        InputStream xmlPayLoadInputStream = new ByteArrayInputStream(xmlPayLoad.getBytes(StandardCharsets.UTF_8));
                        Pair<String, String> pair = parse(xmlPayLoadInputStream);
                        addDataToRealm(realm, itemID, pair);
                    }
                }
            } catch (JSONException | XmlPullParserException | IOException e) {
                e.printStackTrace();
                if (jsonExceptionListener != null) {
                    jsonExceptionListener.onJsonFailed(manufacturer);
                }
            }
        }, runAfterTransaction);
    }

    private JsonExceptionListener jsonExceptionListener;

    public interface JsonExceptionListener {
        void onJsonFailed(String manufacturer);
    }

    public void setJsonExceptionListener(JsonExceptionListener listener) {
        this.jsonExceptionListener = listener;
    }

    private void addDataToRealm(Realm realm, String itemID, Pair<String, String> pair) {
        ItemData item = realm.where(ItemData.class).equalTo("id", itemID).findFirst();

        if (item != null) {
            item.setHttpStatusCode(pair.first);
            item.setAvailability(pair.second);
            realm.insertOrUpdate(item);
        }
    }

    private OnSuccessListener onSuccessListener;

    public interface OnSuccessListener {
        void onSuccess();
    }

    public void setOnSuccessListener(OnSuccessListener listener) {
        this.onSuccessListener = listener;
    }

    public Pair<String, String> parse(InputStream in) throws XmlPullParserException, IOException {
        Pair<String, String> pair = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            pair = readXml(parser);
        } finally {
            in.close();
        }
        return pair;
    }

    private Pair<String, String> readXml(XmlPullParser parser) throws IOException, XmlPullParserException {

        parser.require(XmlPullParser.START_TAG, ns, "AVAILABILITY");
        String httpCode = null;
        String availability = null;


        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("CODE")) {
                httpCode = readHttpCode(parser);
            } else if (name.equals("INSTOCKVALUE")) {
                availability = readAvailability(parser);
            }
        }
        return new Pair<>(httpCode, availability);
    }


    private String readHttpCode(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "CODE");
        String httpCode = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "CODE");
        return httpCode;
    }

    private String readAvailability(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "INSTOCKVALUE");
        String availability = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "INSTOCKVALUE");
        return availability;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }
}
