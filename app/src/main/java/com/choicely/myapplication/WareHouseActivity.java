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

import java.util.ArrayList;
import java.util.List;

public class WareHouseActivity extends AppCompatActivity {

    private final static String TAG = "WareHouseActivity";

    private Spinner spinner;
    private String itemCategory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ware_house_activity);

        spinner = findViewById(R.id.ware_house_activity_spinner);

        addItemsToSpinner();

    }

    private void addItemsToSpinner() {
        List<String> itemCategories = new ArrayList<>();
        itemCategories.add("All items");
        itemCategories.add("Beanies");
        itemCategories.add("Facemasks");
        itemCategories.add("gloves");

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
            filterImages();
        }
    }

    private void filterImages() {
    }

    private void updateContent() {
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, EditItemActivity.class);
        startActivity(intent);
    }
}
