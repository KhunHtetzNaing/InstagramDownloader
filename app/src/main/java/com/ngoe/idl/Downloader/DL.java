package com.ngoe.idl.Downloader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ngoe.idl.Gallery.GalleryActivity;
import com.ngoe.idl.R;

import java.io.File;
import java.util.ArrayList;

import static android.content.Context.DOWNLOAD_SERVICE;

public class DL {
    private DownloadManager mDownloadManager;
    private DownloadManager.Request mRequest;
    Activity activity;
    ArrayList<String> url;
    boolean showDialog;
    int all_count;
    int last_count;
    boolean toast_showed;
    public DL(Activity activity,ArrayList<String> url,boolean showDialog){
        this.activity = activity;
        this.url = url;
        this.showDialog = showDialog;
        last_count = 0;
        all_count = url.size();
        toast_showed  = false;
        mDownloadManager = (DownloadManager) activity.getSystemService(DOWNLOAD_SERVICE);
        for (int i=0;i<url.size();i++){
            String realUrl = url.get(i);
            String type = realUrl.substring(realUrl.lastIndexOf("."));
            String name = String.valueOf(System.currentTimeMillis())+type;
            dlFile(realUrl, name);
        }
    }

    public void dlFile(String url, String fileName){
        try {
            String mBaseFolderPath = Environment.getExternalStorageDirectory()+"/InstagramDownload/";
            if (!new File(mBaseFolderPath).exists()) {
                new File(mBaseFolderPath).mkdir();
            }
            String mFilePath = "file://" + mBaseFolderPath  + fileName;
            Uri downloadUri = Uri.parse(url);
            mRequest = new DownloadManager.Request(downloadUri);
            mRequest.setDestinationUri(Uri.parse(mFilePath));
            mRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            mDownloadManager.enqueue(mRequest);
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            activity.registerReceiver(downloadReceiver, filter);
            if (!toast_showed){
                toast_showed=true;
                Toast.makeText(activity, "Starting Download : " + fileName, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
        }
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            last_count++;
            if (all_count==last_count){
                Toast.makeText(context, "Downloaded!", Toast.LENGTH_SHORT).show();
                sendNotification(context);

                if (showDialog) {
                    AlertDialog.Builder b = new AlertDialog.Builder(context)
                            .setTitle("Completed!")
                            .setCancelable(false)
                            .setMessage("Open your downloaded ?")
                            .setPositiveButton("Open", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    activity.startActivity(new Intent(activity, GalleryActivity.class));
                                }
                            })
                            .setNegativeButton("Rate", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    rate();
                                }
                            })
                            .setNeutralButton("Help Us", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    rate();
                                }
                            });
                    AlertDialog dialog = b.create();
                    dialog.show();
                }
            }
        }
    };

    public void rate(){
        View view = activity.getLayoutInflater().inflate(R.layout.image_view,null);
        TextView tv = view.findViewById(R.id.tv);
        tv.setText("ဒီေဆာ့ဝဲကို ၾကယ္ငါးလုံးေပးၿပီး\n" +
                "အဆင္ေျပမႈရွိ/မရွိ အႀကံေပးၾကပါေနာ္။");
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String appPackageName = activity.getPackageName(); // getPackageName() from Context or Activity object
                try {
                    activity.startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)),1001);
                } catch (android.content.ActivityNotFoundException anfe) {
                    activity.startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)),1001);
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle("ံHelp Us")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Rate Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String appPackageName = activity.getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            activity.startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)),1001);
                        } catch (android.content.ActivityNotFoundException anfe) {
                            activity.startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)),1001);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void sendNotification(Context context) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Intent intent = new Intent(context,GalleryActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(false);
        mBuilder.setSmallIcon(R.drawable.icon);
        mBuilder.setContentTitle("Download Completed");
        mBuilder.setContentText("Click here to open!");
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(001, mBuilder.build());
    }
}
