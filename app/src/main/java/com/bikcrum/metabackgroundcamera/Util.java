package com.bikcrum.metabackgroundcamera;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by Rakesh Pandit on 11/23/2017.
 */

public class Util {
    public static String START_SERVICE = "start_service";
    public static String STOP_SERVICE = "stop_service";
    public static String START_INDEX = "start_index";
    public static String END_INDEX = "stop_index";
    public static String PREFERENCE_NAME = "preference";

    public static void startService(Context context) {
        if (isMyServiceRunning(context, MyService.class)) {
            return;
        }

        context.startService(new Intent(context, MyService.class));

        Toast.makeText(context, "Service started", Toast.LENGTH_SHORT).show();
        NewMessageNotification.notify(context, "Service started", 0);
    }

    public static void stopService(Context context) {
        if (!isMyServiceRunning(context, MyService.class)) {
            return;
        }

        context.stopService(new Intent(context, MyService.class));

        Toast.makeText(context, "Destroyed", Toast.LENGTH_SHORT).show();
        NewMessageNotification.notify(context, "Destroyed", 1);
    }


    public static boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            //noinspection deprecation
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(service.service.getClassName())) {
                    Log.i("biky", "service already running");
                    return true;
                }
            }
        }
        return false;
    }
}
