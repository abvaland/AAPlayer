package com.example.ajay.aaplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.IntDef;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;

public class MyMusicService extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener {
    private MediaPlayer player;
    private ArrayList<SongModel> songs;
    private int songPos;
    private final IBinder musicBind=new MusicBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        songPos=0;
        player=new MediaPlayer();
        initMusicPlayer();
    }
    public void setList(ArrayList<SongModel> theSongs)
    {
        songs=theSongs;
    }
    public class MusicBinder extends Binder
    {
        MyMusicService getService()
        {
            return MyMusicService.this;
        }

    }
    public void initMusicPlayer()
    {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        player.setOnCompletionListener(this);
    }
    public int getSongPos()
    {
        return songPos;
    }
    public MyMusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
       return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public int getPosn()
    {
        return player.getCurrentPosition();
    }
    public int getDur()
    {
        return player.getDuration();
    }
    public boolean isPng()
    {
        return player.isPlaying();
    }
    public void seek(int pos)
    {
        player.seekTo(pos);
    }
    public void go()
    {
        if(player.getDuration()>0)
        player.start();
        else {
            playSong();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0)
        {
            player.reset();
            plaNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        player.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        player.start();

        Intent notIntent=new Intent(this,MusicActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent=PendingIntent.getActivity(this,0,notIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder=new Notification.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_play_arrow_green_a400_36dp)
                .setTicker(songs.get(songPos).getDisplayName())
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songs.get(songPos).getDisplayName());

        Notification notification=builder.build();

        startForeground(1,notification);
      //showNotification();
    }

    public void setRemoteViews(RemoteViews remoteViews)
    {
        Intent openIntent=new Intent(this,MusicActivity.class);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Intent closeIntent=new Intent("close_notification");
        Intent prevIntent=new Intent("play_prev");
        Intent nextIntent=new Intent("play_next");
        Intent pauseIntent=new Intent("play_pause");
        //Intent playIntent=new Intent("play_resume");


        PendingIntent pendingClose=PendingIntent.getBroadcast(this,0,closeIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingPrev=PendingIntent.getBroadcast(this,0,prevIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingNext= PendingIntent.getBroadcast(this,0,nextIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingPause= PendingIntent.getBroadcast(this,0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent pendingPlay=new PendingIntent.getBroadcast(this,0,playIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingOpen=PendingIntent.getBroadcast(this,0,openIntent,PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.imgClose,pendingClose);
        remoteViews.setOnClickPendingIntent(R.id.imgPrev,pendingPrev);
        remoteViews.setOnClickPendingIntent(R.id.imgNext,pendingNext);
        remoteViews.setOnClickPendingIntent(R.id.imgPause,pendingPause);
        //remoteViews.setOnClickPendingIntent(R.id.imgPLay,pendingPlay);
        remoteViews.setOnClickPendingIntent(R.id.customNotification,pendingOpen);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        registerReceiver(mCloseReciver,new IntentFilter("close_notification"));
        registerReceiver(mPauseReciver,new IntentFilter("play_pause"));
        registerReceiver(mPrevReciver,new IntentFilter("play_prev"));
        registerReceiver(mNextReciver,new IntentFilter("play_next"));
    }

    private BroadcastReceiver mCloseReciver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NotificationManager notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(100);
            player.stop();
        }
    };
    private BroadcastReceiver mPrevReciver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playPrev();
        }
    };

    private BroadcastReceiver mNextReciver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            plaNext();
        }
    };

    private BroadcastReceiver mPauseReciver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pausePlayer();
        }
    };

    private void showNotification()
    {
        RemoteViews remoteViews=new RemoteViews(getApplicationContext().getPackageName(),R.layout.custom_notification);
        Intent intent=new Intent(getApplicationContext(),MusicActivity.class);

        PendingIntent pendingIntent=PendingIntent.getActivity(getApplicationContext(),0,intent,0);

        NotificationCompat.Builder builder=new NotificationCompat.Builder(getApplicationContext());
        Notification notifiPlayer = builder.setSmallIcon(R.drawable.ic_play_arrow_green_a400_36dp)
                .setContentIntent(pendingIntent).setOngoing(true).build();
        notifiPlayer.bigContentView=remoteViews;
        setRemoteViews(remoteViews);

        NotificationManager notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(100,notifiPlayer);
    }

    public void setSong(int index)
    {
        songPos=index;
    }

    public void playSong()
    {
        player.reset();
        try {
            player.setDataSource(songs.get(songPos).getData());
            player.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playPrev()
    {
        songPos--;
        if(songPos<0)
        {
            songPos=songs.size()-1;
        }
        playSong();
    }
    public void plaNext()
    {
        songPos++;

        if(songPos>=songs.size())
        {
            songPos=0;
        }
        playSong();
    }

    public void pausePlayer()
    {
        if(player.isPlaying())
        {
            player.pause();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
    }
}
