package dev.dex.common;

import javafx.beans.property.*;

public class Album {
    private int _id;
    private SimpleStringProperty name = new SimpleStringProperty();
    private int artist;

    public Album(int _id, String name, int artist) {
        this._id = _id;
        this.name.set(name);
        this.artist = artist;
    }

    public int get_id() {
        return _id;
    }

    public String getName() {
        return name.get();
    }

    public int getArtist() {
        return artist;
    }
}
