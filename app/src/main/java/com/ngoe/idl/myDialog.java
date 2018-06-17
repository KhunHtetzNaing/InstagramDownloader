package com.ngoe.idl;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.formats.MediaView;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.NativeAdOptions;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.ngoe.idl.Ads.AdsActivity;
import com.ngoe.idl.Downloader.DL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class myDialog extends AppCompatActivity {
    String url = null;
    private static final String ADMOB_AD_UNIT_ID = "ca-app-pub-1325188641119577/5876408792";
    private static final String ADMOB_APP_ID = "ca-app-pub-1325188641119577~6608124273";
    FrameLayout adLayout;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_dialog);

        url = getIntent().getStringExtra("url");
        if (url!=null && url.startsWith("https://www.instagram.com")){
            init();
        }else{
            finish();
        }
    }

    private boolean checkPermissions() {
        int storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        final List<String> listPermissionsNeeded = new ArrayList<>();
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(myDialog.this, listPermissionsNeeded.toArray
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
                    goWork();
                } else {
                    checkPermissions();
                    Toast.makeText(this, "You need to Allow Write Storage Permission!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void init(){
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View v = layoutInflater.inflate(R.layout.fuck_dialog,null);
        Button button = v.findViewById(R.id.btnDownload);
        adLayout = v.findViewById(R.id.fl_adplaceholder);
        imageView =v.findViewById(R.id.iv);

        MobileAds.initialize(this, ADMOB_APP_ID);
        refreshAd();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions()) {
                    if (checkInternet()) {
                        goWork();
                    }
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setView(v);
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();
            }
        });
        dialog.show();
    }

    public void goWork(){
            new download().execute(url);
            finish();
    }

    class download extends AsyncTask<String,Void,ArrayList<String>>{
        @Override
        protected ArrayList<String> doInBackground(String... strings) {
            Log.d("mUrl","doInBackground");
            URLConnection connection = null;
            ArrayList<String> lol = new ArrayList<>();
            try {
                connection = (new URL("http://app.htetznaing.com/insta-dl/index.php?url="+strings[0])).openConnection();
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
                JSONObject jObject;
                try {
                    jObject = new JSONObject(result);
                    JSONArray cast = jObject.getJSONArray("url");
                    for (int i=0; i<cast.length(); i++) {
                        String name = cast.getString(i);
                        Log.d("mUrl",name);
                        lol.add(name);
                    }
                    return lol;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            if (strings!=null){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new DL(myDialog.this,strings,false);
                    }
                });

            }else{
                Toast.makeText(myDialog.this, "Something was wrong!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean checkInternet(){
        boolean what = false;
        CheckInternet checkNet = new CheckInternet(this);
        if (checkNet.isInternetOn()){
            what = true;
        }else{
            what = false;
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(myDialog.this)
                    .setTitle("Error!")
                    .setCancelable(false)
                    .setMessage("No internet connection!")
                    .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (checkInternet()){
                                new download().execute(url);
                            }
                        }
                    })
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            android.app.AlertDialog dialog = builder.create();
            dialog.show();
        }
        return what;
    }

    private void populateUnifiedNativeAdView(UnifiedNativeAd nativeAd, UnifiedNativeAdView adView) {
        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        //        // have a video asset.
        VideoController vc = nativeAd.getVideoController();

        // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
        // VideoController will call methods on this object when events occur in the video
        // lifecycle.
        vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
            public void onVideoEnd() {
                // Publishers should allow native ads to complete video playback before refreshing
                // or replacing them with another ad in the same UI location.
                super.onVideoEnd();
            }
        });

        MediaView mediaView = adView.findViewById(R.id.ad_media);
        ImageView mainImageView = adView.findViewById(R.id.ad_image);

        // Apps can check the VideoController's hasVideoContent property to determine if the
        // NativeAppInstallAd has a video asset.
        if (vc.hasVideoContent()) {
            adView.setMediaView(mediaView);
            mainImageView.setVisibility(View.GONE);
        } else {
            adView.setImageView(mainImageView);
            mediaView.setVisibility(View.GONE);

            // At least one image is guaranteed.
            List<NativeAd.Image> images = nativeAd.getImages();
            mainImageView.setImageDrawable(images.get(0).getDrawable());
        }

        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // Some assets are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }
        adView.setNativeAd(nativeAd);
    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     *
     */
    private void refreshAd() {
        AdLoader.Builder builder = new AdLoader.Builder(this, ADMOB_AD_UNIT_ID);
        builder.forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
            // OnUnifiedNativeAdLoadedListener implementation.
            @Override
            public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                FrameLayout frameLayout = adLayout;
                UnifiedNativeAdView adView = (UnifiedNativeAdView) getLayoutInflater().inflate(R.layout.ad_unified, null);
                populateUnifiedNativeAdView(unifiedNativeAd, adView);
                frameLayout.removeAllViews();
                frameLayout.addView(adView);
            }

        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(false)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                imageView.setVisibility(View.VISIBLE);
                adLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                imageView.setVisibility(View.GONE);
                adLayout.setVisibility(View.VISIBLE);
            }
        }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }
}