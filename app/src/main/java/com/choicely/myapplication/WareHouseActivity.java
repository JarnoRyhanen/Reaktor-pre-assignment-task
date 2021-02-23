package com.choicely.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.choicely.myapplication.dp.RealmHelper;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class WareHouseActivity extends AppCompatActivity {

    private final static String TAG = "WareHouseActivity";
    private boolean firstResume;

    private Spinner spinner;
    private String itemCategory;
    private RecyclerView recyclerView;
    private WareHouseRecyclerViewAdapter adapter;

    private final Realm realm = RealmHelper.getInstance().getRealm();
    List<String> itemCategories = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ware_house_activity);

        spinner = findViewById(R.id.ware_house_activity_spinner);
        recyclerView = findViewById(R.id.ware_house_activity_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WareHouseRecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);

        firstResume = true;
        addItemCategoriesToList();
    }

    private void addItemCategoriesToList() {
        itemCategories.add("All items");
        itemCategories.add("Beanies");
        itemCategories.add("Face masks");
        itemCategories.add("Gloves");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(firstResume){
            addItemsToSpinner();
            firstResume = false;
        }else {
            Log.d(TAG, "performItemFiltering: item category selected: " + itemCategory);
            performItemFiltering();
        }
    }

    private void addItemsToSpinner() {
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, R.layout.spinner_item, itemCategories);
        spinner.setAdapter(arrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                itemCategory = parent.getItemAtPosition(position).toString();
                Log.d(TAG, "onItemSelected: " + itemCategory);

                performItemFiltering();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "Nothing selected");
            }
        });

    }

    private void performItemFiltering() {
        if (itemCategory == null || itemCategory.equals("All items")) {
            updateContent();
        } else {
            Log.d(TAG, "performItemFiltering: item category selected: " + itemCategory);
            filterImages();
        }
    }

    private void filterImages() {
        adapter.clear();

        RealmResults<ItemData> items = realm.where(ItemData.class).contains("itemCategory", itemCategory).findAll();
        for (ItemData item : items) {
            adapter.add(item);
        }
        adapter.notifyDataSetChanged();
    }

    private void updateContent() {
        adapter.clear();

        RealmResults<ItemData> items = realm.where(ItemData.class).findAll();
        for (ItemData item : items) {
            adapter.add(item);
        }
        adapter.notifyDataSetChanged();
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, EditItemActivity.class);
        startActivity(intent);
    }
}
