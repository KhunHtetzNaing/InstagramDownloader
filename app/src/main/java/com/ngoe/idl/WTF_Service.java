package com.ngoe.idl;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class WTF_Service extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (clipboardManager.getText().toString().startsWith("https://www.instagram.com")) {
                    Intent intent = new Intent(getApplicationContext(), myDialog.class);
                    intent.putExtra("url",clipboardManager.getText().toString());
                    startActivity(intent);
                }
            }
        });
        return START_STICKY;
    }
}
