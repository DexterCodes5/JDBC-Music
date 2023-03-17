package dev.dex.common;

import javafx.beans.property.*;

public class Artist {
    private int _id;
    private SimpleStringProperty name = new SimpleStringProperty();

    public Artist(int _id, String name) {
        this._id = _id;
        this.name.setValue(name);
    }

    public int get_id() {
        return _id;
    }

    public String getName() {
        return name.get();
    }

}
