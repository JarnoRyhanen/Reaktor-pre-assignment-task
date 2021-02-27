package com.choicely.myapplication;

import io.realm.RealmObject;

public class LastTimeLoaded extends RealmObject {

    private long previousTimeLoaded;

    public long getPreviousTimeLoaded() {
        return previousTimeLoaded;
    }

    public void setPreviousTimeLoaded(long previousTimeLoaded) {
        this.previousTimeLoaded = previousTimeLoaded;
    }
}
