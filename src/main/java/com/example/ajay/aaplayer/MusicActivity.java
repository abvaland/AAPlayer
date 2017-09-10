package com.example.ajay.aaplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.Inflater;

import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SeekBar;
import android.widget.TextView;

public class MusicActivity extends AppCompatActivity implements MyInterface,MediaPlayerControl, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private boolean paused=false,playbackPaused=false;
    private Toolbar toolbar;
    private RecyclerView rvSongs;
    //GlobalStorage globalStorage;
    static ArrayList<SongModel> alSongs;
    //private MusicController controller;

    private MyMusicService musicService;
    private Intent playIntent;
    private boolean musicBound=false;
    private LinearLayout llController;
    private ImageView imgPrev,imgPlay,imgpause,imgNext;
    private TextView txtCurrDuration,txtDuration;
    private SeekBar seekBar;
    final Handler mhandler=new Handler();
    private MySongAdapter mySongAdapter;
    private BroadcastReceiver mReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_PHONE_STATE},1);

        initObjects();
        initToolbar();
        initUIControls();
        registerForListeneres();
        new GetAudioListAsyncTask().execute();

        MyThread t=new MyThread();
        t.run();

       // setController();
        IntentFilter intentFilter = new IntentFilter("android.intent.action.PHONE_STATE");

        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                Log.e("InchooTutorial", "call recieve");
                TelephonyManager telephonyManager= (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                Log.e("Call State",telephonyManager.getCallState()+"");
                switch (telephonyManager.getCallState())
                {
                    case TelephonyManager.CALL_STATE_RINGING:
                        pause();
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        pause();
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        start();
                        break;

                }

            }
        };
        //registering our receiver
        this.registerReceiver(mReceiver, intentFilter);

    }

    private void registerForListeneres() {
        imgPrev.setOnClickListener(this);
        imgPlay.setOnClickListener(this);
        imgpause.setOnClickListener(this);
        imgNext.setOnClickListener(this);
        seekBar.setOnSeekBarChangeListener(this);
    }

    //connect to service
    private ServiceConnection musicConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyMusicService.MusicBinder binder= (MyMusicService.MusicBinder) service;
            musicService=binder.getService();
            musicService.setList(alSongs);
            musicBound=true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound=false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        if(playIntent==null)
        {
            playIntent=new Intent(this,MyMusicService.class);
            bindService(playIntent,musicConnection, Context.BIND_AUTO_CREATE);
            startService(playIntent);
        }

        if(musicService!=null)
        {
            if(musicService.isPng())
            {
                llController.setVisibility(View.VISIBLE);
            }
        }
    }
    public  void deletedFile()
    {
        playingFalse();
        musicService.playSong();
        alSongs.get(musicService.getSongPos()).setIsplaying(true);
        mySongAdapter.notifyDataSetChanged();
    }

    public void playingFalse()
    {
        for (SongModel song :
                alSongs) {
            song.setIsplaying(false);
        }
    }

    /*private void setController()
    {
        controller=new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        },
         new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                playPrev();
             }
         });

        controller.setMediaPlayer(this);
        controller.setAnchorView(rvSongs);
        controller.setEnabled(true);
    }*/
    private void playNext()
    {
        playingFalse();
        musicService.plaNext();

        if(playbackPaused)
        {
           // setController();
            playbackPaused=false;
        }
        alSongs.get(musicService.getSongPos()).setIsplaying(true);
        mySongAdapter.notifyDataSetChanged();

        //controller.show(0);
    }
    private void playPrev()
    {
        playingFalse();
        musicService.playPrev();
        if(playbackPaused)
        {
           // setController();
            playbackPaused=false;
        }
       // controller.show(0);

        alSongs.get(musicService.getSongPos()).setIsplaying(true);
        mySongAdapter.notifyDataSetChanged();

    }

    private void initObjects() {
       // globalStorage = (GlobalStorage) getApplicationContext();
        alSongs=new ArrayList<SongModel>();
    }

    private void initUIData() {
        String[] columns={MediaStore.Audio.Media.DISPLAY_NAME,MediaStore.Audio.Media.DATA,
                            MediaStore.Audio.Media.DURATION,MediaStore.Audio.Media.ALBUM_ID,
                            MediaStore.Audio.Media._ID,MediaStore.Audio.Media.ALBUM
                        };
        @SuppressLint("Recycle") Cursor cursor=getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,columns,null,null,null);
        @SuppressLint("Recycle") Cursor cursor1=getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,new String[]{MediaStore.Audio.Albums.ALBUM_ART},null,null,null);

        while (cursor.moveToNext())
        {
            SongModel songModel=new SongModel();
            songModel.setId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            songModel.setData(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)));
            songModel.setDisplayName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)));
            songModel.setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)));
            songModel.setAlbumId(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)));
            songModel.setaId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)));
            songModel.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));


            Uri sArtworkUri=Uri.parse("content://media/external/audio/albumart");
            Uri albumArt= ContentUris.withAppendedId(sArtworkUri,songModel.getaId());
            Log.d("MusicActivity","AlbumArt URI :: "+albumArt.toString());

            Bitmap bitmap=null;

            try
            {
                bitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),albumArt);
                //bitmap=Bitmap.createScaledBitmap(bitmap,30,30,true);
            } catch (IOException e) {
                e.printStackTrace();
                bitmap= BitmapFactory.decodeResource(getResources(),R.drawable.ic_music_note_black_36dp);
            }

            songModel.setBitmap(bitmap);

            alSongs.add(songModel);
        }

       // globalStorage.setAlSongList(alSongs);

    }

    private void initUIControls() {
        rvSongs= (RecyclerView) findViewById(R.id.rvSongs);
        imgNext= (ImageView) findViewById(R.id.imgNext);
        imgpause= (ImageView) findViewById(R.id.imgPause);
        imgPlay= (ImageView) findViewById(R.id.imgPLay);
        imgPrev= (ImageView) findViewById(R.id.imgPrev);
        txtCurrDuration= (TextView) findViewById(R.id.txtCurrDuration);
        txtDuration= (TextView) findViewById(R.id.txtDuration);
        seekBar= (SeekBar) findViewById(R.id.seekBar);
    }

    private void initToolbar() {
        toolbar= (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("AAPlayer");
        toolbar.setTitleTextColor(Color.WHITE);
        llController= (LinearLayout) findViewById(R.id.controlLayout);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==1)
        {
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                new GetAudioListAsyncTask().execute();
            }
            else {
                finish();
            }
        }
    }

    @Override
    public void ItemClick(int position) {
        playingFalse();
        musicService.setSong(position);
        musicService.playSong();

        imgpause.setVisibility(View.VISIBLE);
        imgPlay.setVisibility(View.GONE);
        if(playbackPaused)
        {
           // setController();
            playbackPaused=false;
        }
       // controller.show(0);
        llController.setVisibility(View.VISIBLE);

//        txtDuration.setText(musicService.getDur()+"");
//        seekBar.setMax(musicService.getDur());

      /*  int duration=musicService.getDur();
        setDuration(duration);*/

        alSongs.get(musicService.getSongPos()).setIsplaying(true);
        mySongAdapter.notifyDataSetChanged();
    }
    private void setDuration(int duration) {

        long secound= TimeUnit.MILLISECONDS.toSeconds(duration);
        long minutes=secound/60;
        long sec=secound%60;
        String mm = String.format("%02d", minutes);
        String ss=String.format("%02d",sec);
        seekBar.setMax((int) secound);
        txtDuration.setText(mm+":"+ss);
        //txtSong_name.setText(ms[last_selected_position].getDisplay_Name());

    }


    @Override
    public void start() {
        alSongs.get(musicService.getSongPos()).isplaying=false;
        musicService.go();
        alSongs.get(musicService.getSongPos()).isplaying=true;
        mySongAdapter.notifyDataSetChanged();
    }

    @Override
    public void pause() {
        playbackPaused=true;
        musicService.pausePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused=true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(paused)
        {
           // setController();
            paused=false;
        }
    }

    @Override
    protected void onStop() {
        //controller.hide();
        super.onStop();
    }

    @Override
    public int getDuration() {
        if(musicService!=null && musicBound && musicService.isPng())
        {
            return musicService.getDur();
        }else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {

        if (musicService!=null && musicBound && musicService.isPng())
        {
            return musicService.getPosn();
        }
        else {
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicService.seek(pos);
    }

    @Override
    public boolean isPlaying() {
        if(musicService!=null && musicBound)
        {
            return musicService.isPng();
        }
        else {
            return false;
        }
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }


    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicService=null;

        if(mReceiver!=null)
        {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.imgNext:
                playNext();
                break;

            case R.id.imgPause:
                if(isPlaying())
                {
                    musicService.pausePlayer();

                    imgpause.setVisibility(View.GONE);
                    imgPlay.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.imgPLay:
                start();

                imgpause.setVisibility(View.VISIBLE);
                imgPlay.setVisibility(View.GONE);
                break;

            case R.id.imgPrev:
                playPrev();
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
       // seekTo(progress);
        if(fromUser && musicService!=null)
        {
            int cp=musicService.getDur();
            int ft=1000*seekBar.getProgress();
            if((ft)<musicService.getDur())
            {
                seekTo(ft);
            }
            else
            {
                seekTo(cp/100);
            }

        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    private class GetAudioListAsyncTask extends AsyncTask<Void,Void,Boolean>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            DialogManager.showWaitingDialog(MusicActivity.this);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                initUIData();
                return true;
            }catch (Exception e)
            {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            DialogManager.releaseDialog();
            rvSongs.setLayoutManager(new LinearLayoutManager(MusicActivity.this));
            mySongAdapter=new MySongAdapter(MusicActivity.this,alSongs,MusicActivity.this);
            rvSongs.setAdapter(mySongAdapter);

        }
    }
    class MyThread extends Thread implements Runnable
    {
        @Override
        public void run() {
            if(musicService!=null)
            {
                if(musicService.isPng())
                {
                    int duration=musicService.getDur();
                    setDuration(duration);
                    int mcurrentPosition=musicService.getPosn();
                    long currentSec= TimeUnit.MILLISECONDS.toSeconds(mcurrentPosition);
                    seekBar.setProgress((int)currentSec);
                    Log.i("sec:",""+currentSec);
                    long txtmint=currentSec/60;
                    long txtsec=currentSec%60;
                    String mm1=String.format("%02d",txtmint);
                    String ss1=String.format("%02d",txtsec);
                    txtCurrDuration.setText(mm1+":"+ss1);


                    if(seekBar.getProgress()==seekBar.getMax())
                    {
                        playNext();
                    }
                }
            }

            mhandler.postDelayed(this,1000);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }
}
