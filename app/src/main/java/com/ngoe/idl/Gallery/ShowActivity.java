package com.ngoe.idl.Gallery;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.ngoe.idl.R;
import com.ngoe.idl.UI.Main2Activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShowActivity extends AppCompatActivity {
    VideoView videoPlayer;
    MediaController mediaC;
    ImageView mImageView;
    String filePath = null;
    InterstitialAd interstitialAd;
    AdRequest adRequest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initAds();

        mImageView = (ImageView) findViewById(R.id.image);
        videoPlayer = (VideoView) findViewById(R.id.videoView);
        mediaC = new MediaController(this);
        mediaC.setAnchorView(videoPlayer);
        videoPlayer.setMediaController(mediaC);

        filePath = getIntent().getStringExtra("data");

        if (filePath.endsWith(".mp4")){
            mImageView.setVisibility(View.GONE);
            mImageView.setEnabled(false);
            showVideo(filePath);
        }else {
            videoPlayer.setVisibility(View.GONE);
            videoPlayer.setEnabled(false);
            showPhoto(filePath);
        }

    }

    public void showPhoto(String photo){
        Glide.with(this)
                .load(photo)
                .asBitmap()
                .error(R.drawable.empty_image)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .into(mImageView);
    }

    public void showVideo(String video){
        videoPlayer.setVideoPath(video);
        videoPlayer.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void initAds(){
        adRequest = new AdRequest.Builder().build();
        interstitialAd = new InterstitialAd(ShowActivity.this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.delete:
                deleteFilez(filePath);
                break;
            case R.id.info:
                showDetails(filePath);
                break;
            case R.id.share:
                shareFile(filePath);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if (videoPlayer!=null) {
            savedInstanceState.putInt("CurrentPosition", videoPlayer.getCurrentPosition());
            videoPlayer.pause();
        }
    }

    @Override
    public void onBackPressed() {
        showADS();
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (videoPlayer!=null){
            if (!videoPlayer.isPlaying()){
                videoPlayer.resume();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (videoPlayer!=null){
            if (!videoPlayer.isPlaying()){
                videoPlayer.resume();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (videoPlayer!=null){
            if (!videoPlayer.isPlaying()){
                videoPlayer.resume();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoPlayer!=null){
            if (videoPlayer.isPlaying()){
                videoPlayer.pause();
            }
        }
    }

    // After rotating the phone. This method is called.
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (videoPlayer!=null) {
            int position = savedInstanceState.getInt("CurrentPosition");
            videoPlayer.seekTo(position);
        }
    }

    private void showDetails(String apk){
        File file = new File(apk);
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String ext = file.getName().substring(file.getName().indexOf(".") + 1);
        String type = mime.getMimeTypeFromExtension(ext);
        String name = new File(apk).getName();
        String path = apk;
        String create = getFileDate(new File(apk));
        String size = getFileSize(new File(apk));
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Detail info")
                .setMessage("Name: "+name+"\nType: "+type+"\nDate: "+create+"\nSize: "+size+"\nPath: "+path)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showADS();
                    }
                });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    public String getFileSize(File file){
        float sizeInBytes = file.length();
        float sizeInMb = sizeInBytes / (1024 * 1024);

        String size = String.valueOf(sizeInMb);
        size = size.substring(0,4)+" MB";
        Log.d("FileSize",size);
        return size;
    }

    public String getFileDate(File file){
        Date lastModified = new Date(file.lastModified());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDateString = formatter.format(lastModified);
        return formattedDateString;
    }


    public void shareFile(final String file){
        final File fl = new File(file);
        Intent mmsIntent = new Intent(Intent.ACTION_SEND);
        mmsIntent.setType("*/*");
        mmsIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(fl));
        try {
            startActivityForResult(Intent.createChooser(mmsIntent,"Share APK Via..."),10);
        }catch (Exception e){
            Toast.makeText(ShowActivity.this, "Error! \nPlease try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==10){
            showADS();
        }
    }

    private void deleteFilez(final String path){
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("Attention!")
                .setMessage("Do you want to delete "+new File(path).getName()+" ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (checkPermissions()) {
                            showADS();
                            boolean b = delFile(path);
                            if (b == true) {
                                Toast.makeText(ShowActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(ShowActivity.this, "Something was wrong!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showADS();
                    }
                });
        android.support.v7.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean delFile(String path){
        boolean b = false;
        File file = new File(path);
        if (file.exists()) {
            b = file.delete();
        }else{
            b = true;
        }
        return b;
    }

    private boolean checkPermissions() {
        int storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        final List<String> listPermissionsNeeded = new ArrayList<>();
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(ShowActivity.this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), 5217);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 5217: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean b = delFile(filePath);
                    if (b == true) {
                        Toast.makeText(ShowActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(ShowActivity.this, "Something was wrong!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    checkPermissions();
                    Toast.makeText(this, "You need to Allow Write Storage Permission!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }
}
