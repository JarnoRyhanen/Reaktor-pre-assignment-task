package com.choicely.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.choicely.myapplication.dp.RealmHelper;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class WareHouseActivity extends AppCompatActivity {

    private final static String TAG = "WareHouseActivity";

    private final XmlParser xmlParser = new XmlParser();
    private final ApiRequests apiRequests = new ApiRequests();
    private final Realm realm = RealmHelper.getInstance().getRealm();

    private final ApiRequests.allItemsDownLoadedListener allItemsDownLoadedListener = new ApiRequests.allItemsDownLoadedListener() {
        @Override
        public void onItemDownLoaded() {
            runOnUiThread(() -> {
                dataCounter += 1;
                if (dataCounter == 2) {
                    progressBar.setVisibility(View.GONE);
                    progressBarTextView.setVisibility(View.GONE);
                    itemDownLoadedCounter.setVisibility(View.GONE);
                    itemsLoadedTextView.setVisibility(View.GONE);
                    addItemCategoriesToList();
                    performItemFiltering();
                    getXmlData();
                    Log.d(TAG, "onItemDownLoaded: ALL ITEMS LOADED");
                }
            });
        }
    };

    private int itemsLoadedCounter = 0;
    private final ApiRequests.oneItemLoadedListener oneItemLoadedListener = () -> {
        runOnUiThread(() -> {
            updateFoundItemCount(itemsLoadedCounter);
            itemsLoadedCounter += 1;
        });
    };
    private final XmlParser.OnSuccessListener onSuccessListener = () -> runOnUiThread(this::performItemFiltering);
    private final ApiRequests.ApiRequestsFailureListener apiRequestsFailureListener = new ApiRequests.ApiRequestsFailureListener() {
        @Override
        public void onFailure(int errorCode) {
            runOnUiThread(() -> makeErrorMessage(errorCode, menu));
        }
    };
    private final XmlParser.XmlParserFailureListener xmlParserFailureListener = new XmlParser.XmlParserFailureListener() {
        @Override
        public void onFailure(int errorCode) {
            runOnUiThread(() -> makeErrorMessage(errorCode, menu));
        }
    };
    private final XmlParser.JsonExceptionListener jsonExceptionListener = manufacturer -> {
        Log.d(TAG, "onJsonFailed: RUNNING AGAIN WITH MANUFACTURER:" + manufacturer);
        xmlParser.getManufacturerXmlData(manufacturer);
    };

    private boolean firstResume;

    private SearchView searchView;
    private ProgressBar progressBar;
    private TextView progressBarTextView;
    private TextView itemDownLoadedCounter;
    private TextView itemsLoadedTextView;

    private Spinner spinner;
    private String itemCategory;
    private RecyclerView recyclerView;
    private WareHouseRecyclerViewAdapter adapter;

    private final List<String> itemFilters = new ArrayList<>();
    private final List<String> itemManufacturers = new ArrayList<>();

    private int dataCounter = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ware_house_activity);

        progressBar = findViewById(R.id.ware_house_activity_progress_bar);
        progressBarTextView = findViewById(R.id.ware_house_activity_progress_bar_text);
        spinner = findViewById(R.id.ware_house_activity_spinner);

        itemDownLoadedCounter = findViewById(R.id.ware_house_activity_progress_items_found_count);
        itemsLoadedTextView = findViewById(R.id.ware_house_activity_progress_items_found_text_view);

        recyclerView = findViewById(R.id.ware_house_activity_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WareHouseRecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);

        firstResume = true;
        addItemCategoriesToList();

        if (realm.isEmpty()) {
            downloadItems();
        }
        if (!realm.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            progressBarTextView.setVisibility(View.GONE);
            itemDownLoadedCounter.setVisibility(View.GONE);
            itemsLoadedTextView.setVisibility(View.GONE);
        }
        setAllListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (firstResume) {
            addItemsToSpinner();
            firstResume = false;
        } else {
            addItemCategoriesToList();
            performItemFiltering();
        }
    }

    private void setAllListeners() {
        apiRequests.setOneItemLoadedListener(oneItemLoadedListener);
        xmlParser.setOnSuccessListener(onSuccessListener);
        apiRequests.setListener(allItemsDownLoadedListener);

        apiRequests.setApiRequestsFailureListener(apiRequestsFailureListener);
        xmlParser.setXmlParserFailureListener(xmlParserFailureListener);
        xmlParser.setJsonExceptionListener(jsonExceptionListener);
    }

    private void getXmlData() {
        for (int i = 0; i < itemManufacturers.size(); i++) {
            Log.d(TAG, "getXmldata: manufacturer: " + itemManufacturers.get(i));
            xmlParser.getManufacturerXmlData(itemManufacturers.get(i));
        }
    }

    private void downloadItems() {
        apiRequests.getData("beanies");
        apiRequests.getData("facemasks");
        apiRequests.getData("gloves");
    }

    private Menu menu;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.ware_house_activity_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search_libraries);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search for items");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!itemCategory.equals("All items")) {
                    updateContent(realm.where(ItemData.class)
                            .beginsWith("itemName", searchView.getQuery().toString().toUpperCase())
                            .contains("itemCategory", itemCategory)
                            .findAll()
                            .sort("itemName", Sort.ASCENDING));
                } else {
                    updateContent(realm.where(ItemData.class)
                            .beginsWith("itemName", searchView.getQuery().toString().toUpperCase())
                            .findAll()
                            .sort("itemName", Sort.ASCENDING));
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void makeErrorMessage(int errorCode, Menu menu) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        String errorMessage = "Error: " + String.valueOf(errorCode);
        SpannableString redErrorMessage = new SpannableString(errorMessage);

        if (menu.findItem(R.id.ware_house_activity_menu_error_message_field) != null) {
            MenuItem errorMessageItem = menu.findItem(R.id.ware_house_activity_menu_error_message_field);
            redErrorMessage.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0, redErrorMessage.length(), 0);
            builder.append(redErrorMessage);
            errorMessageItem.setVisible(true);
            errorMessageItem.setTitle(redErrorMessage);
        }
    }

    private void addItemCategoriesToList() {
        itemFilters.clear();
        RealmResults<ItemData> categories = realm.where(ItemData.class).distinct("itemCategory").findAll();

        itemFilters.add("All items");
        for (ItemData item : categories) {
            Log.d(TAG, "addItemCategoriesToList: " + item.getItemCategory());
            itemFilters.add(item.getItemCategory());
        }
        getAllItemManufacturers();
        getAllItemAvailabilityValues();
    }

    private void getAllItemManufacturers() {
        RealmResults<ItemData> manufacturers = realm.where(ItemData.class).distinct("itemManufacturer").findAll();

        for (ItemData item : manufacturers) {
            if (item.getItemManufacturer() != null) {
                Log.d(TAG, "getAllItemManufacturers: " + item.getItemManufacturer());
                itemFilters.add(item.getItemManufacturer());
                itemManufacturers.add(item.getItemManufacturer());
            }
        }
    }

    private void getAllItemAvailabilityValues() {
        RealmResults<ItemData> availabilityValues = realm.where(ItemData.class).distinct("itemAvailabilityValue").findAll();

        for (ItemData item : availabilityValues) {
            if (item.getAvailabilityValue() != null) {
                Log.d(TAG, "getAllItemManufacturers: " + item.getAvailabilityValue());
                itemFilters.add(item.getAvailabilityValue());
            }
        }
    }

    private void addItemsToSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, itemFilters);
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
            updateContent(realm.where(ItemData.class).findAll().sort("itemName", Sort.ASCENDING));
        } else {
            Log.d(TAG, "performItemFiltering: item category selected: " + itemCategory);
            updateContent(realm.where(ItemData.class).contains("itemCategory", itemCategory).findAll().sort("itemName", Sort.ASCENDING));
        }
    }

    private void updateContent(RealmResults realmResults) {
        adapter.clear();

        for (ItemData item : (RealmResults<ItemData>) realmResults) {
            adapter.add(item);
        }
        adapter.notifyDataSetChanged();
        Log.d(TAG, "updateContent: item count: " + adapter.getItemCount());
    }

    public void onClick(View view) {
        Intent intent = new Intent(this, EditItemActivity.class);
        startActivity(intent);
    }

    private void updateFoundItemCount(int itemsFoundCount) {
        itemDownLoadedCounter.setText(String.valueOf(itemsFoundCount));
    }
}