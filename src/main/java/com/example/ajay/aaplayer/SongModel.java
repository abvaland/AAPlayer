package com.example.ajay.aaplayer;

import android.graphics.Bitmap;

/**
 * Created by Ajay on 05-03-2017.
 */

public class SongModel {
    String id;
    String data;
    String displayName;
    int duration;
    String Album;
    String albumId;
    long aId;
    boolean isplaying=false;

    public boolean isplaying() {
        return isplaying;
    }

    public void setIsplaying(boolean isplaying) {
        this.isplaying = isplaying;
    }

    Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public long getaId() {
        return aId;
    }

    public void setaId(long aId) {
        this.aId = aId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public String getAlbum() {
        return Album;
    }

    public SongModel setAlbum(String album) {
        Album = album;
        return this;
    }

    public String getData() {
        return data;
    }

    public SongModel setData(String data) {
        this.data = data;
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SongModel setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    public int getDuration() {
        return duration;
    }

    public SongModel setDuration(int duration) {
        this.duration = duration;
        return this;
    }
}
