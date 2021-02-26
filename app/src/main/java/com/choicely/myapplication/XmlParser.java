package com.choicely.myapplication;

import android.util.Log;
import android.util.Pair;
import android.util.Xml;

import androidx.annotation.LongDef;

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
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;
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
    private final List<Pair<String, String>> entries = new ArrayList();

    public void getXml(String manufacturer) {

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
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    parseJsonData(response);
                } else {
                    Log.d(TAG, "Response Failed " + response.code());
                }
            }
        });
    }

    private void parseJsonData(Response response) {

        Log.d(TAG, "Response was successful");
        try {
            Log.d(TAG, "parseJsonData: " + response.code());
            String myResponse = response.body().string();

            JSONObject jsonObject = new JSONObject(myResponse);
            JSONArray dataArray = jsonObject.getJSONArray("response");
            Log.d(TAG, "parseData: " + dataArray.length());

            for (int i = 0; i < dataArray.length(); i++) {
                Realm realm = RealmHelper.getInstance().getCurrentThreadRealm();

                Log.d(TAG, "\t parseJsonData: NEW OBJECT STARTS HERE");
                JSONObject obj = dataArray.getJSONObject(i);

                String itemID = obj.getString("id");

                ItemData item = realm.where(ItemData.class).equalTo("id", itemID.toLowerCase()).findFirst();
                String xmlPayLoad = obj.getString("DATAPAYLOAD");
                Log.d(TAG, "parseJsonData: " + xmlPayLoad);

                InputStream xmlPayLoadInputStream = new ByteArrayInputStream(xmlPayLoad.getBytes(StandardCharsets.UTF_8));
                parse(xmlPayLoadInputStream);

                if (entries.get(i).first != null && entries.get(i).second != null && item != null) {
                    realm.beginTransaction();
                    item.setHttpStatusCode(entries.get(i).first);
                    item.setAvailability(entries.get(i).second);
                    realm.copyToRealmOrUpdate(item);
                    realm.commitTransaction();
                    Log.d(TAG, "OBJECT ENDS HERE");
                }
                if(listener != null){
                    Log.d(TAG, "parseJsonData: listener heard");
                    listener.onStatusCodeAndAvailabilityLoaded();
                }
            }
        } catch (JSONException | IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

//    public void findCorrectItem(String itemID, int index) {
//
//
//    }

//    private void addDataToRealm(ItemData itemData) {
//        Realm realm = RealmHelper.getInstance().getCurrentThreadRealm();
//        realm.executeTransaction(realm1 -> realm1.copyToRealmOrUpdate(itemData));
//        realm.close();
//    }

    private statusCodeAndAvailabilityAddedListener listener;

    public interface statusCodeAndAvailabilityAddedListener{
        void onStatusCodeAndAvailabilityLoaded();
    }

    public void setListener(statusCodeAndAvailabilityAddedListener listener) {
        this.listener = listener;
    }
    
    public void parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            readXml(parser);
        } finally {
            in.close();
        }
    }

    private void readXml(XmlPullParser parser) throws IOException, XmlPullParserException {

        parser.require(XmlPullParser.START_TAG, ns, "AVAILABILITY");
        String httpCode = null;
        String availability = null;

        int i = 0;

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("CODE")) {
                httpCode = readHttpCode(parser);
            } else if (name.equals("INSTOCKVALUE")) {

                availability = readAvailability(parser);
                Log.d(TAG, "readXml: httpCode: " + httpCode + ", stockValue: " + availability);
                entries.add(new Pair<>(httpCode, availability));
                i++;
            }
        }
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
