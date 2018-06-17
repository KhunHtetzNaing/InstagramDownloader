package com.ngoe.idl.myWebBrowser;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.DownloadListener;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.ngoe.idl.BuildConfig;
import com.ngoe.idl.CheckInternet;
import com.ngoe.idl.R;
import com.ngoe.idl.UI.Main2Activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static java.security.AccessController.getContext;

public class MyBrowser extends AppCompatActivity {
    WebView webView;
    InterstitialAd interstitialAd;
    AdRequest adRequest;
    String url,title;
    private DownloadManager mDownloadManager;
    private long mDownloadedFileID;
    private DownloadManager.Request mRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_browser);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");
        setTitle(title);

        initAds();
        mDownloadManager = (DownloadManager) getSystemService(getApplicationContext().DOWNLOAD_SERVICE);
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d("myURL",url);
                if (url.startsWith("https://www.facebook.com/419786931807272")){
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("fb://profile/419786931807272"));
                        startActivity(intent);
                    }catch (Exception e){
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://m.facebook.com/419786931807272"));
                        startActivity(intent);
                    }
                    return true;
                }else if (url.startsWith("https://play.google.com/store/apps")) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + url.replace("https://play.google.com/store/apps/details?id=",""))));
                    }
                    return true;
                }else{
                    if (checkInternet()){
                        return false;
                    }else{
                        return true;
                    }
                }
            }
        });
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String s, String s1, String s2, String s3, long l) {
                String fileName= URLUtil.guessFileName(s,s2,s3);
                Log.d("FileName",fileName);
                if(checkPermissions()==true){
                    dlFile(s,fileName);
                }
            }
        });
        if (checkInternet()) {
            webView.loadUrl(url);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()){
            webView.goBack();
        }else{
            super.onBackPressed();
        }
    }

    public void initAds(){
        adRequest = new AdRequest.Builder().build();
        interstitialAd = new InterstitialAd(MyBrowser.this);
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

    public void dlFile(String url,String fileName){
        try {
            String mBaseFolderPath = android.os.Environment.getExternalStorageDirectory()+ File.separator+ "Download" + File.separator;
            if (!new File(mBaseFolderPath).exists()) {
                new File(mBaseFolderPath).mkdir();
            }
            String mFilePath = "file://" + mBaseFolderPath + "/" + fileName;
            Uri downloadUri = Uri.parse(url);
            mRequest = new DownloadManager.Request(downloadUri);
            mRequest.setDestinationUri(Uri.parse(mFilePath));
            mRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            mDownloadedFileID = mDownloadManager.enqueue(mRequest);
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            registerReceiver(downloadReceiver, filter);
            Toast.makeText(this, "Starting Download : "+fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));
        }
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //check if the broadcast message is for our enqueued download
            final Uri uri = mDownloadManager.getUriForDownloadedFile(mDownloadedFileID);
            String apk = getRealPathFromURI(uri);
            Toast.makeText(context, "Downloaded : "+new File(apk).getName(), Toast.LENGTH_SHORT).show();
            if (apk.endsWith(".apk")) {
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Intent intent1 = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                                        intent1.setData(uri);
                                        intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        startActivity(intent1);
                                    } else {
                                        Intent intent2 = new Intent(Intent.ACTION_VIEW);
                                        intent2.setDataAndType(uri, "application/vnd.android.package-archive");
                                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent2);
                                    }
                                } catch (Exception e) {

                                }
            }
        }
    };

    private boolean checkPermissions() {
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        final List<String> listPermissionsNeeded = new ArrayList<>();
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(MyBrowser.this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), 5217);
            Log.d("TAG","Permission"+"\n"+String.valueOf(false));
            return false;
        }
        Log.d("Permission","Permission"+"\n"+String.valueOf(true));
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 5217: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Please download again :)", Toast.LENGTH_SHORT).show();
                } else {
                    checkPermissions();
                    Toast.makeText(this, "You need to Allow Write Storage Permission!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public String getRealPathFromURI (Uri contentUri) {
        String path = null;
        String[] proj = { MediaStore.MediaColumns.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            path = cursor.getString(column_index);
        }
        cursor.close();
        return path;
    }

    public boolean checkInternet(){
        boolean what = false;
        CheckInternet checkNet = new CheckInternet(this);
        if (checkNet.isInternetOn()){
            what = true;
        }else{
            what = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(MyBrowser.this)
                    .setTitle("Error!")
                    .setCancelable(false)
                    .setMessage("No internet connection!")
                    .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (checkInternet()){
                                showADS();
                                webView.loadUrl(webView.getUrl());
                            }
                        }
                    })
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showADS();
                            finish();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return what;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
