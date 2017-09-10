package com.example.ajay.aaplayer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;



/**
 * Created by frenzin05 on 6/20/2017.
 */

public class DialogManager {

    private static ProgressDialog progressDialog;
    private static Dialog progressBar=null;



    public static void showWaitingDialog(Context context)
    {
                    if(progressBar==null)
                    {
                        progressBar = new Dialog(context);
                        progressBar.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        progressBar.setContentView(R.layout.custom_dialog);
                        progressBar.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        progressBar.setCancelable(false);
                        progressBar.show();
                    }

    }
    public static void releaseDialog() {

       if(progressBar!=null)
       {
           progressBar.dismiss();
           progressBar=null;

       }
    }//end of releaseDialog()

    /**********************************************************************************************************
     * Function to display simple Alert Dialog with click event
     *
     * @param context
     * @param message
     * @param clickListener
     */
    public static void showAlertDialog(Context context, String title,String message, final ClickListener clickListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();

        // Setting Dialog Title
        alertDialog.setTitle(title);

        // Setting Dialog Message
        alertDialog.setMessage(message);

        // Setting OK Button
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                clickListener.okClick();
            }
        });

        alertDialog.setCancelable(false);
        // Showing Alert Message
        alertDialog.show();
    }//end of showAlertDialog


    public static abstract class ClickListener {
        public void okClick() {
        }

        public void noClick() {
        }

        public void cancelClick() {

        }
    }

}
