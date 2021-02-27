package com.choicely.myapplication;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class LastTimeLoaded extends RealmObject {

    @PrimaryKey
    private String id;
    private long previousTimeLoaded;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getPreviousTimeLoaded() {
        return previousTimeLoaded;
    }

    public void setPreviousTimeLoaded(long previousTimeLoaded) {
        this.previousTimeLoaded = previousTimeLoaded;
    }
}
