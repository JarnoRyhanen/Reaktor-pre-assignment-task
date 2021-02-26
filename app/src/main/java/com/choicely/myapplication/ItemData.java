package com.choicely.myapplication;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ItemData extends RealmObject {

    @PrimaryKey
    private String id;
    private String itemCategory;
    private String itemName;
    private String itemManufacturer;
    private String httpStatusCode;
    private String stockValue;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemManufacturer() {
        return itemManufacturer;
    }

    public void setItemManufacturer(String itemManufacturer) {
        this.itemManufacturer = itemManufacturer;
    }

    public String getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(String httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getAvailabilityValue() {
        return stockValue;
    }

    public void setAvailability(String stockValue) {
        this.stockValue = stockValue;
    }
}
