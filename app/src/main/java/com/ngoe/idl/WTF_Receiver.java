package com.ngoe.idl;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class WTF_Receiver extends BroadcastReceiver {
    Context context;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        if (isMyServiceRunning(WTF_Service.class)){

        }else{
            context.startService(new Intent(context,WTF_Service.class));
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }
}
