package com.choicely.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WareHouseRecyclerViewAdapter extends RecyclerView.Adapter<WareHouseRecyclerViewAdapter.WareHouseViewHolder> {

    private static final String TAG = "WareHouseAdapter";
    private List<ItemData> itemDataList = new ArrayList<>();

    private final Context context;

    public WareHouseRecyclerViewAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public WareHouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new WareHouseViewHolder(LayoutInflater.from(context).inflate(R.layout.ware_house_list_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull WareHouseViewHolder holder, int position) {

        ItemData item = itemDataList.get(position);

        holder.itemID = item.getId();
        holder.itemName.setText(item.getItemName());
        holder.itemCategory.setText(item.getItemCategory());
    }

    @Override
    public int getItemCount() {
        return itemDataList.size();
    }

    public void add(ItemData itemData) {
        itemDataList.add(itemData);
    }

    public void clear() {
        itemDataList.clear();
    }

    public static class WareHouseViewHolder extends RecyclerView.ViewHolder {

        public String itemID;
        public TextView itemName;
        public TextView itemCategory;

        public WareHouseViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(onRowClick);
            itemName = itemView.findViewById(R.id.ware_house_list_row_item_name);
            itemCategory = itemView.findViewById(R.id.ware_house_list_row_item_category);
        }

        private final View.OnClickListener onRowClick = view -> {
            Context ctx = itemView.getContext();
            Intent intent = new Intent(ctx, EditItemActivity.class);
            intent.putExtra(IntentKeys.ITEM_ID, itemID);
            Log.d(TAG, "itemId: " + itemID);
            ctx.startActivity(intent);
        };

    }
}
