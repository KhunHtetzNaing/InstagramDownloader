package com.ngoe.idl.UI;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.ngoe.idl.Ads.AdsActivity;
import com.ngoe.idl.CheckInternet;
import com.ngoe.idl.CheckUpdate;
import com.ngoe.idl.Downloader.DL;
import com.ngoe.idl.Gallery.GalleryActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.ngoe.idl.R;
import com.ngoe.idl.WTF_Service;
import com.ngoe.idl.myWebBrowser.MyBrowser;

public class Main2Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, RewardedVideoAdListener {
    String mUrl = "https://www.instagram.com/p/BjunwbxDE4h/?utm_source=ig_share_sheet&igshid=1bzwyyeoscahs";
    ProgressDialog progressDialog;
    String downloadPath;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    InterstitialAd interstitialAd;
    AdRequest adRequest;
    int showAds_code = 1001;
    private RewardedVideoAd mRewardedVideoAd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Downloader");
        setSupportActionBar(toolbar);

        FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
        new AdsActivity(Main2Activity.this,frameLayout);

        initAds();
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                share();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        new CheckUpdate(Main2Activity.this,false).check();
        startService(new Intent(this,WTF_Service.class).setAction("startforeground"));
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
        downloadPath = Environment.getExternalStorageDirectory()+"/InstagramDownload/";

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setTitle("Please Wait!");
        progressDialog.setMessage("Fetching...");
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkPermissions();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("Attention!")
                    .setCancelable(false)
                    .setMessage("Do you want to exit ?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showADS();
                            finish();
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            showADS();
                        }
                    })
                    .setNeutralButton("Rate", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            rate();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.instagram:
                String insta = "com.instagram.android";
                try{
                    Intent intent = getPackageManager().getLaunchIntentForPackage(insta);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }catch (Exception e){
                    try {
                        startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + insta)),showAds_code);
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + insta)),showAds_code);
                    }
                }
                break;
            case R.id.show_downloaded:
                startActivityForResult(new Intent(Main2Activity.this, GalleryActivity.class),showAds_code);
                showADS();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        switch (item.getItemId()){
            case R.id.help:
                Intent intent = new Intent(Main2Activity.this, MyBrowser.class);
                intent.putExtra("url","https://mynewapp2.blogspot.com/p/help.html");
                intent.putExtra("title","Help");
                startActivityForResult(intent,showAds_code);
                showADS();
                break;
            case R.id.more:
                intent = new Intent(Main2Activity.this, MyBrowser.class);
                intent.putExtra("url","https://mynewapp2.blogspot.com/p/more-apps.html");
                intent.putExtra("title","More Apps");
                startActivityForResult(intent,showAds_code);
                showADS();
                break;
            case R.id.update:
                new CheckUpdate(Main2Activity.this,true).check();
                break;
            case R.id.about:
                showAbout();
                break;
            case R.id.rate:
                rate();
                break;
            case R.id.feedback:
                startActivityForResult(new Intent(Main2Activity.this,Feedback.class),showAds_code);
                showADS();
                break;
            case R.id.share:
                share();
                break;
            case R.id.ad:if (mRewardedVideoAd.isLoaded()) {
                mRewardedVideoAd.show();
            }else{
                Toast.makeText(this, "Please try again later!", Toast.LENGTH_SHORT).show();
            }
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void downloadNow(View view) {
        EditText editText = findViewById(R.id.edText);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboardManager.hasText() == true){
            String text = clipboardManager.getText().toString();
            if (text.contains("instagram.com")==true){
                editText.setText(text);
                mUrl = text;
                if (checkInternet()==true){
                    new download().execute(text);
                    progressDialog.show();
                }
            }else{
                Toast.makeText(this, "Please copy instagram link only!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Please copy link!", Toast.LENGTH_SHORT).show();
        }
    }

    public void rate_me(View view) {
        rate();
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
                        progressDialog.dismiss();
                        if (sharedPreferences.getBoolean("check",false)==true){
                            new DL(Main2Activity.this,strings,true);
                        }else{
                            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                            View view = inflater.inflate(R.layout.dialog,null);
                            final CheckBox checkBox = view.findViewById(R.id.checkBox);
                            AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this)
                                    .setCancelable(false)
                                    .setView(view)
                                    .setTitle("Attention!")
                                    .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            editor.putBoolean("check",checkBox.isChecked());
                                            editor.commit();
                                            editor.apply();
                                            new DL(Main2Activity.this,strings,true);
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
                    }
                });

            }else{
                progressDialog.dismiss();
                Toast.makeText(Main2Activity.this, "Something was wrong!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean checkPermissions() {
        int storage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        final List<String> listPermissionsNeeded = new ArrayList<>();
        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(Main2Activity.this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), 5217);
            return false;
        }
        File n = new File(downloadPath);
        if (!n.exists()){
            n.mkdirs();
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 5217: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    File n = new File(downloadPath);
                    if (!n.exists()){
                        n.mkdirs();
                    }
                } else {
                    checkPermissions();
                    Toast.makeText(this, "You need to Allow Write Storage Permission!", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    public void showAbout(){
        String version = "1.0";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        View view = getLayoutInflater().inflate(R.layout.about,null);
        view.setPadding(30,0,30,0);
        TextView web = view.findViewById(R.id.tvWebDev);
        TextView title = view.findViewById(R.id.tvName);
        title.setText("App Name : "+getString(R.string.app_name));
        TextView tvVersion = view.findViewById(R.id.tvVersion);
        tvVersion.setText("Version : "+version);
        web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("fb://profile/100013433347720"));
                    startActivityForResult(intent,showAds_code);
                }catch (Exception e){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://m.facebook.com/GSKhai"));
                    startActivityForResult(intent,showAds_code);
                }
            }
        });

        TextView dev = view.findViewById(R.id.AndroidDev);
        dev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("fb://profile/100011339710114"));
                    startActivityForResult(intent,showAds_code);
                }catch (Exception e){
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://m.facebook.com/KHtetzNaing"));
                    startActivityForResult(intent,showAds_code);
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("About App")
                .setCancelable(false)
                .setView(view)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showADS();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==showAds_code){
            showADS();
        }
    }

    public void rate(){
        View view = getLayoutInflater().inflate(R.layout.image_view,null);
        TextView tv = view.findViewById(R.id.tv);
        tv.setText("ဒီေဆာ့ဝဲကို ၾကယ္ငါးလုံးေပးၿပီး\n" +
                "အဆင္ေျပမႈရွိ/မရွိ အႀကံေပးၾကပါေနာ္။");
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)),showAds_code);
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)),showAds_code);
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("ံHelp Us")
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Rate Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)),showAds_code);
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)),showAds_code);
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showADS();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean checkInternet(){
        boolean what = false;
        CheckInternet checkNet = new CheckInternet(this);
        if (checkNet.isInternetOn()){
            what = true;
        }else{
            what = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this)
                    .setTitle("Error!")
                    .setCancelable(false)
                    .setMessage("No internet connection!")
                    .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (checkInternet()){
                                progressDialog.show();
                                new download().execute(mUrl);
                            }
                        }
                    })
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        return what;
    }

    public void initAds(){
        adRequest = new AdRequest.Builder().build();
        interstitialAd = new InterstitialAd(Main2Activity.this);
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

    private void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,"All in One Photos & Videos Downloader For Instagram!\n\nDownload at Google Play Store : play.google.com/store/apps/details?id="+getPackageName()+"\n\nDirect Download : http://bit.ly/2LW400T\n#DownloaderForInstagram #InstagramDownloader");
        startActivityForResult(Intent.createChooser(intent,"Share App..."),showAds_code);
    }

    @Override
    public void onRewarded(RewardItem reward) {

    }

    @Override
    public void onRewardedVideoAdLeftApplication() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
    }

    @Override
    public void onRewardedVideoCompleted() {
        loadRewardedVideoAd();
        Toast.makeText(this, "Thanks for you support us!", Toast.LENGTH_SHORT).show();
    }

    public void loadRewardedVideoAd(){
        if (!mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.loadAd("ca-app-pub-1325188641119577/9777213094",
                    new AdRequest.Builder().build());
        }
    }
}
