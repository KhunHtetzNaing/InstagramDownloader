package com.ngoe.idl;

import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Feedback extends AppCompatActivity {
    WebView webView;
    boolean ready = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        TextView textView = findViewById(R.id.contact_form_title);
        textView.setText("<Contact Developer/>");
        webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.setVisibility(View.GONE);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                ready = true;
            }
        });
        webView.loadUrl("https://contact-form777.blogspot.com/p/edit.html");
    }

    public final boolean isInternetOn() {

        // get Connectivity Manager object to check connection
        ConnectivityManager connec =
                (ConnectivityManager) getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {

            // if connected with internet
            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    public void sendNow(View view) {
        EditText contant_name = findViewById(R.id.contant_name);
        EditText contant_email = findViewById(R.id.contant_email);
        EditText contant_message = findViewById(R.id.contant_message);

        String name = contant_name.getText().toString();
        String email = contant_email.getText().toString();
        if (email.length()>0){
        }else{
            email = "emptyemail@gmail.com";
        }
        String message = contant_message.getText().toString();

        if (name.length()>0){
            if (email.length()>0) {
               if (isValidEmail(email)==true){
                   if (message.length() > 0) {
                       if (isInternetOn() == true) {
                           if (ready == true) {
                               webView.loadUrl("javascript:document.getElementById(\"ContactForm1_contact-form-name\").value = \"" + name + "\";\n" +
                                       "document.getElementById(\"ContactForm1_contact-form-email\").value = \"" + email + "\";\n" +
                                       "document.getElementById(\"ContactForm1_contact-form-email-message\").value = \"" + "[#"+getString(R.string.app_name)+"]\n"+ message + "\";\n" +
                                       "document.getElementById(\"ContactForm1_contact-form-submit\").click();");
                               AlertDialog.Builder builder = new AlertDialog.Builder(this)
                                       .setTitle("Completed!")
                                       .setMessage("Your message has been sent.")
                                       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                           @Override
                                           public void onClick(DialogInterface dialogInterface, int i) {
                                               finish();
                                           }
                                       });
                               AlertDialog dialog = builder.create();
                               dialog.show();
                           }
                       }else{
                           Toast.makeText(this, "Need internet connection!", Toast.LENGTH_SHORT).show();
                       }
                   } else {
                       Toast.makeText(this, "Please write your message!", Toast.LENGTH_SHORT).show();
                   }
               }else{
                   Toast.makeText(this, "Please fill valid email address!", Toast.LENGTH_SHORT).show();
               }
            }else{
                Toast.makeText(this, "Please fill your email!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(this, "Please fill your name!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
