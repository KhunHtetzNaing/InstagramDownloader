package com.ngoe.idl;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.ngoe.idl.UI.Main2Activity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import static android.content.Context.DOWNLOAD_SERVICE;

public class CheckUpdate {
    Activity activity;
    boolean silence;
    String checkUrl = "https://raw.githubusercontent.com/KhunHtetzNaing/myServer/master/instagram-downloader";
    ProgressDialog progressDialog;
    String downloadPath;
    private DownloadManager mDownloadManager;
    private long mDownloadedFileID;
    private DownloadManager.Request mRequest;
    boolean uninstall = false;
    InterstitialAd interstitialAd;
    AdRequest adRequest;

    public CheckUpdate(Activity activity, boolean silence) {
        this.activity = activity;
        this.silence = silence;
        progressDialog = new ProgressDialog(activity);
        mDownloadManager = (DownloadManager) activity.getSystemService(DOWNLOAD_SERVICE);
        initAds();
    }
    public void check(){
        new checkNow().execute();
    }

    class checkNow extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (silence==true){
                progressDialog.setCancelable(false);
                progressDialog.setTitle("Please Wait!");
                progressDialog.setMessage("Checking...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            URLConnection connection = null;
            try {
                connection = (new URL(checkUrl)).openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                connection.connect();
                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder html = new StringBuilder();
                for (String line; (line = reader.readLine()) != null; ) {
                    html.append(line);
                }
                in.close();
                String result = html.toString();
                return result;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progressDialog.dismiss();
            if (s!=null){
                try {
                    JSONObject jsonObject = new JSONObject(s);
                    final String title = jsonObject.getString("title");
                    final String message = jsonObject.getString("message");
                    final String version = jsonObject.getString("version");
                    final String direct = jsonObject.getString("direct");
                    final String playstore = jsonObject.getString("playstore");
                    uninstall = jsonObject.getBoolean("uninstall");

                    PackageManager manager = activity.getPackageManager();
                    PackageInfo info;
                    String currentVersion = "1.0";
                    try {
                        info = manager.getPackageInfo(activity.getPackageName(), 0);
                        currentVersion = info.versionName;
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }

                    double cVersion = Double.parseDouble(currentVersion);
                    double nVersion = Double.parseDouble(version);

                    if (nVersion==cVersion || nVersion<cVersion){
                        if (silence==true){
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    AlertDialog.Builder b = new AlertDialog.Builder(activity)
                                            .setTitle("Congratulations!")
                                            .setCancelable(false)
                                            .setMessage("You are on latest version!")
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    showADS();
                                                }
                                            });
                                    AlertDialog d = b.create();
                                    d.show();
                                }
                            });
                        }
                    }else{
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                                        .setTitle(title)
                                        .setCancelable(false)
                                        .setMessage(message)
                                        .setPositiveButton("Direct", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dlFile(direct,"InstagramDownloader_"+version+".apk");
                                                showADS();
                                            }
                                        })
                                        .setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                showADS();
                                                activity.finish();
                                            }
                                        });
                                if (playstore!=null){
                                    builder.setNegativeButton("Play Store", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            showADS();
                                            if (uninstall==true){
                                                uninstall(activity.getPackageName());
                                            };
                                            try {
                                                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + playstore)));
                                            } catch (android.content.ActivityNotFoundException anfe) {
                                                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + playstore)));
                                            }
                                        }
                                    });
                                }
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void dlFile(String url,String fileName){
        try {
            downloadPath = Environment.getExternalStorageDirectory()+"/Download/";
            String mBaseFolderPath = downloadPath;
            if (!new File(mBaseFolderPath).exists()) {
                new File(mBaseFolderPath).mkdir();
            }
            String mFilePath = "file://" + mBaseFolderPath + fileName;
            Uri downloadUri = Uri.parse(url);
            mRequest = new DownloadManager.Request(downloadUri);
            mRequest.setDestinationUri(Uri.parse(mFilePath));
            mRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            mDownloadedFileID = mDownloadManager.enqueue(mRequest);
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            activity.registerReceiver(downloadReceiver, filter);
            Toast.makeText(activity, "Starting Download : "+fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            activity.startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
        }
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //check if the broadcast message is for our enqueued download
            final Uri uri = mDownloadManager.getUriForDownloadedFile(mDownloadedFileID);
            final String apk = getRealPathFromURI(uri);
            Toast.makeText(context, "Downloaded : "+new File(apk).getName(), Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                    .setTitle("Download Completed!")
                    .setCancelable(false)
                    .setMessage("Please install this latest apk! ")
                    .setPositiveButton("Install Now", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (uninstall==true){
                                uninstall(activity.getPackageName());
                            };
                            openFile(apk);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    private void uninstall(String mPackage){
        Intent intent = new Intent(Intent.ACTION_DELETE);
        intent.setData(Uri.parse("package:"+mPackage));
        activity.startActivity(intent);
    }

    public void openFile(String apk){
        File file = new File(apk);
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = file.getName().substring(file.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkURI = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file);
            install.setDataAndType(apkURI, type);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }else{
            install.setDataAndType(Uri.fromFile(file), type);
        }
        activity.startActivity(install);
    }
    public String getRealPathFromURI (Uri contentUri) {
        String path = null;
        String[] proj = { MediaStore.MediaColumns.DATA };
        Cursor cursor = activity.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    public void initAds(){
        adRequest = new AdRequest.Builder().build();
        interstitialAd = new InterstitialAd(activity);
        interstitialAd.setAdUnitId("ca-app-pub-1325188641119577/2090294760");
        interstitialAd.loadAd(adRequest);
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                loadADS();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                loadADS();
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                loadADS();
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                loadADS();
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                loadADS();
            }
        });
        showADS();
    }

    public void loadADS(){
        if (!interstitialAd.isLoaded()){
            interstitialAd.loadAd(adRequest);
        }
    }

    public void showADS(){
        if (!interstitialAd.isLoaded()){
            interstitialAd.loadAd(adRequest);
        }else{
            interstitialAd.show();
        }
    }
}
