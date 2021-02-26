package com.choicely.myapplication;

import android.content.Intent;
import android.os.Bundle;
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

    private final ApiRequests apiRequests = new ApiRequests();
    private final AlarmHelper alarmHelper = new AlarmHelper(this);

    private final Realm realm = RealmHelper.getInstance().getRealm();

    private final ApiRequests.allItemsDownLoadedListener allItemsDownLoadedListener = new ApiRequests.allItemsDownLoadedListener() {
        @Override
        public void onItemDownLoaded() {
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                progressBarTextView.setVisibility(View.GONE);
                performItemFiltering();
            });
        }
    };

    private boolean firstResume;

    private SearchView searchView;
    private ProgressBar progressBar;
    private TextView progressBarTextView;

    private Spinner spinner;
    private String itemCategory;
    private RecyclerView recyclerView;
    private WareHouseRecyclerViewAdapter adapter;

    private final List<String> itemCategories = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ware_house_activity);

        progressBar = findViewById(R.id.ware_house_activity_progress_bar);
        progressBarTextView = findViewById(R.id.ware_house_activity_progress_bar_text);
        spinner = findViewById(R.id.ware_house_activity_spinner);

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
        }
        startAlarmHelper();
        apiRequests.setListener(allItemsDownLoadedListener);
    }

    public void downloadItems() {
        apiRequests.getData("beanies");
        apiRequests.getData("facemasks");
        apiRequests.getData("gloves");
    }

    private void startAlarmHelper() {
        alarmHelper.updateEveryOneHour();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
                updateContent(realm.where(ItemData.class)
                        .beginsWith("itemName", searchView.getQuery().toString().toUpperCase())
                        .contains("itemCategory", itemCategory)
                        .findAll()
                        .sort("itemName", Sort.ASCENDING));
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void addItemCategoriesToList() {
        itemCategories.clear();
        RealmResults<ItemData> categories = realm.where(ItemData.class).distinct("itemCategory").findAll();

        itemCategories.add("All items");
        for (ItemData item : (RealmResults<ItemData>) categories) {
            Log.d(TAG, "addItemCategoriesToList: " + item.getItemCategory());
            itemCategories.add(item.getItemCategory());
        }
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

    private void addItemsToSpinner() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, itemCategories);
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
}
