package com.example.ajay.aaplayer;

import android.app.Application;

import java.util.ArrayList;

/**
 * Created by Ajay on 05-03-2017.
 */

public class GlobalStorage extends Application {
    ArrayList<SongModel> alSongList;

    @Override
    public void onCreate() {
        super.onCreate();
        alSongList=new ArrayList<SongModel>();
    }

    public ArrayList<SongModel> getAlSongList() {
        return alSongList;
    }

    public void setAlSongList(ArrayList<SongModel> alSongList) {
        this.alSongList = alSongList;
    }
}
