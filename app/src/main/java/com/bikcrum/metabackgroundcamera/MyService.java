package com.bikcrum.metabackgroundcamera;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.lang.invoke.ConstantCallSite;

public class MyService extends Service {
    private MediaPlayer mediaPlayer;
    private IBinder binder = new MyBinder();
    private boolean isBound;

    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && Util.STOP_SERVICE.equals(intent.getAction())) {
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();
        NewMessageNotification.notify(this, "Service started", 0);

        mediaPlayer = MediaPlayer.create(this, R.raw.silence01s);

        mediaPlayer.start();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("biky", "on bind");
        isBound = true;
        return binder;
    }

    public class MyBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

    @Override
    public void onRebind(Intent intent) {
        isBound = true;
        Log.d("biky", "on rebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("biky", "on unbind");
        isBound = false;
        return true;
    }

    @Override
    public void onDestroy() {

        Toast.makeText(this, "Destroyed", Toast.LENGTH_SHORT).show();
        NewMessageNotification.notify(this, "Destroyed", 1);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        super.onDestroy();
    }
}
