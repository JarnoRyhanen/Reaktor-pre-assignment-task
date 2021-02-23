package com.choicely.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.choicely.myapplication.dp.RealmHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;

public class EditItemActivity extends AppCompatActivity {

    private final static String TAG = "EditItemActivity";

    private EditText itemNameEditText;
    private EditText itemCategoryEditText;
    private String itemID;

    private ListPopupWindow listPopupWindow;
    private final List<String> sampleCategoryList = new ArrayList<>();

    private final Realm realm = RealmHelper.getInstance().getRealm();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_item_activity);

        itemNameEditText = findViewById(R.id.edit_item_activity_item_name_edit_text);
        itemCategoryEditText = findViewById(R.id.edit_item_activity_category_name_edit_text);

        listPopupWindow = new ListPopupWindow(this);
        listPopupWindow.setAdapter(new ArrayAdapter<>(this,
                R.layout.category_list_window, R.id.category_text_view,
                new String[]{"Beanies", "Face masks", "Gloves"}));

        listPopupWindow.setAnchorView(itemCategoryEditText);
        listPopupWindow.setWidth(ListPopupWindow.MATCH_PARENT);
        listPopupWindow.setHeight(ListPopupWindow.WRAP_CONTENT);

        sampleCategoryList.add("Beanies");
        sampleCategoryList.add("Face masks");
        sampleCategoryList.add("Gloves");

        itemCategoryEditText.setOnFocusChangeListener(onFocusChangeListener);
        listPopupWindow.setOnItemClickListener(categoryPopupItemClickListener);

        itemID = getIntent().getStringExtra(IntentKeys.ITEM_ID);

        if (itemID == null) {
            itemID = UUID.randomUUID().toString();
        } else {
            loadItem();
        }
    }

    @Override
    public void onBackPressed() {
        if (!checkIfEditTextFieldsAreEmpty()) {
            super.onBackPressed();
            saveItem();
        }
    }

    private boolean checkIfEditTextFieldsAreEmpty() {
        if (itemNameEditText.getText().toString().isEmpty() || itemCategoryEditText.getText().toString().isEmpty()) {
            Toast.makeText(this, "Both text fields must be filled", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void loadItem() {
        ItemData item = realm.where(ItemData.class).equalTo("id", itemID).findFirst();

        itemNameEditText.setText(item.getItemName());
        itemCategoryEditText.setText(item.getItemCategory());
        Log.d(TAG, "Item loaded with id: " + itemID + ", named " + itemNameEditText.getText());
    }

    private void saveItem() {
        realm.executeTransaction(realm1 -> {
            ItemData itemData = new ItemData();
            itemData.setId(itemID);
            itemData.setItemName(itemNameEditText.getText().toString());
            itemData.setItemCategory(itemCategoryEditText.getText().toString());

            realm.copyToRealmOrUpdate(itemData);
        });
    }

    private final AdapterView.OnItemClickListener categoryPopupItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            itemCategoryEditText.setText(sampleCategoryList.get(position));
            listPopupWindow.dismiss();
        }
    };
    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus && !listPopupWindow.isShowing()) {
                listPopupWindow.show();
            }
        }
    };

}
