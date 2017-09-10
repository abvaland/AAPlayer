package com.example.ajay.aaplayer;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Ajay on 05-03-2017.
 */

class MySongAdapter extends RecyclerView.Adapter<MySongAdapter.ViewHolder> {
    private static final String TAG = "MySongAdapter";
    Context context;
    ArrayList<SongModel> alSongs;
    MyInterface myListner;
    public MySongAdapter(Context context, ArrayList<SongModel> alSongs,MyInterface myListner) {
        this.context=context;
        this.alSongs=alSongs;
        this.myListner=myListner;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.single_song_list,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final SongModel songModel=alSongs.get(position);
        holder.txtSongName.setText(songModel.displayName);
        if(songModel.isplaying)
        {
            holder.imgSong.setImageResource(R.drawable.ic_play_arrow_green_a400_36dp);
        }
        else {
            holder.imgSong.setImageBitmap(songModel.getBitmap());
        }

        setDuration(songModel.duration,holder.txtDuration);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myListner.ItemClick(holder.getAdapterPosition());
            }
        });

        holder.imgOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu popupMenu=new PopupMenu(context,holder.imgOption);
                popupMenu.inflate(R.menu.song_option_menu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())
                        {
                            case R.id.menu_share:
                                Log.e(TAG,"Data : "+songModel.getData());
                                final Uri uri=Uri.fromFile(new File(songModel.data));
                                Log.e(TAG,"Uri : "+uri.toString());

                                Intent intent=new Intent();
                                intent.setAction(Intent.ACTION_SEND);
                                String extension= MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                                extension=extension.toLowerCase();
                                String type=MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                                Log.e("Extension=",extension);
                                Log.e("Mime Type=",""+type);
                                intent.putExtra(Intent.EXTRA_STREAM,uri);
                                intent.setType(type);



                                PackageManager packageManager=context.getPackageManager();
                                List<ResolveInfo> activities=packageManager.queryIntentActivities(intent,0);
                                boolean isIntentSafe=activities.size()>0;

                                if(isIntentSafe)
                                {
                                    context.startActivity(Intent.createChooser(intent,"Share audio to.."));
                                }
                                else
                                {
                                    Toast.makeText(context,"Can't find app to share audio",Toast.LENGTH_SHORT).show();
                                }

                                return true;

                            case R.id.menu_delete:
                                int permissionCheck= ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                if(permissionCheck==PackageManager.PERMISSION_DENIED)
                                {
                                    ActivityCompat.requestPermissions((MusicActivity)context, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

                                }
                                final Dialog dialog=new Dialog(context);
                                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                dialog.setContentView(R.layout.alert_dialog_ok_cancel);
                                ((TextView)dialog.findViewById(R.id.tvSongName)).setText(songModel.getDisplayName());

                                dialog.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        Log.e(TAG,"Data : "+songModel.getData());
                                        Uri uri2=Uri.fromFile(new File(songModel.data));
                                        Log.e(TAG,"Uri : "+uri2.toString());

                                        File file=new File(uri2.getPath());
                                        if(file.exists())
                                        {
                                            if(file.delete())
                                            {
                                                alSongs.remove(holder.getAdapterPosition());
                                                notifyDataSetChanged();
                                                Toast.makeText(context, "Deleting Successfully.", Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();

                                                ((MusicActivity)context).deletedFile();

                                            }
                                            else {
                                                Toast.makeText(context, "Deleting Error.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else {
                                            Log.e(TAG,"File Not Exist.");
                                        }
                                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,uri2));
                                        //delete(context,context.getContentResolver(),uri2,null,null);

                                    }
                                });
                                dialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });

                                dialog.show();
                                return true;

                            case R.id.menu_details:
                                Log.e(TAG,"Data : "+songModel.getData());
                                Uri uri2=Uri.fromFile(new File(songModel.data));
                                Log.e(TAG,"Uri : "+uri2.toString());

                                File file=new File(uri2.getPath());
                                if(file.exists())
                                {
                                        showProperyDialog(file,songModel);
                                }
                                return true;

                            default:
                                return false;

                        }
                    }
                });

                popupMenu.show();
            }
        });

    }

    private void showProperyDialog(File file, SongModel songModel) {
        final Dialog prty_dialog=new Dialog(context);
        prty_dialog.setContentView(R.layout.property_layout);

        TextView txt_prty_fileName,txt_prty_path,txt_prty_size;

        txt_prty_fileName= (TextView) prty_dialog.findViewById(R.id.txt_prty_fileName);
        txt_prty_size= (TextView) prty_dialog.findViewById(R.id.txt_prty_size);
        txt_prty_path= (TextView) prty_dialog.findViewById(R.id.txt_prty_path);

        txt_prty_fileName.setText(songModel.getDisplayName());
        txt_prty_path.setText(file.getAbsolutePath());
        txt_prty_size.setText((file.length())/(1024*1024)+" MB");

        prty_dialog.findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prty_dialog.dismiss();
            }
        });

        prty_dialog.show();

    }

    public static int delete(Context context, ContentResolver resolver, Uri uri,
                             String where, String[] selectionArgs) {
        try {
            return resolver.delete(uri, where, selectionArgs);
        } catch (Exception e) {
            Log.e(TAG, "Catch a SQLiteException when delete: ", e);
            return -1;
        }
    }


    private void setDuration(int duration, TextView txtDuration) {
        long secound= TimeUnit.MILLISECONDS.toSeconds(duration);
        long minutes=secound/60;
        long sec=secound%60;
        String mm = String.format("%02d", minutes);
        String ss=String.format("%02d",sec);
        txtDuration.setText(mm+":"+ss);
        //txtSong_name.setText(ms[last_selected_position].getDisplay_Name());

    }

    @Override
    public int getItemCount() {
        return alSongs.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgSong,imgOption;
        TextView txtSongName;
        TextView txtDuration;
        public ViewHolder(View itemView) {
            super(itemView);
            imgOption= (ImageView) itemView.findViewById(R.id.imgOption);
            imgSong= (ImageView) itemView.findViewById(R.id.imgSong);
            txtSongName= (TextView) itemView.findViewById(R.id.txtSongName);
            txtDuration= (TextView) itemView.findViewById(R.id.txtDuration);
        }
    }
}
