package com.bikcrum.metabackgroundcamera;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.bikcrum.circularrangeslider.CircularRangeSlider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Handler handler = new Handler();
    private Runnable runnable;
    private Intent intent;

    private CircularRangeSlider mCircularRangeSlider;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        intent = new Intent(this, MyService.class);

        preferences = getSharedPreferences(Util.PREFERENCE_NAME, MODE_PRIVATE);

        mCircularRangeSlider = findViewById(R.id.circular_range_slider);
        mCircularRangeSlider.setStartIndex(preferences.getInt(Util.START_INDEX, 0));
        mCircularRangeSlider.setEndIndex(preferences.getInt(Util.END_INDEX, 1));

        mCircularRangeSlider.setOnRangeChangeListener(new CircularRangeSlider.OnRangeChangeListener() {
            @Override
            public void onRangePress(int i, int i1) {

            }

            @Override
            public void onRangeChange(int i, int i1) {

            }

            @Override
            public void onRangeRelease(int startIndex, int endIndex) {
                Calendar calendar = Calendar.getInstance();
                long now = System.currentTimeMillis();

                calendar.set(Calendar.HOUR_OF_DAY, endIndex);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                long startTime = calendar.getTimeInMillis();

                calendar.set(Calendar.HOUR_OF_DAY, startIndex);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                long endTime = calendar.getTimeInMillis();

                if (now > startTime) {
                    //schedule for tomorrow
                    startTime += AlarmManager.INTERVAL_DAY;
                }

                if (now > endTime) {
                    //schedule for tomorrow
                    endTime += AlarmManager.INTERVAL_DAY;
                }

                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt(Util.START_INDEX, startIndex);
                editor.putInt(Util.END_INDEX, endIndex);
                editor.apply();

                String nowS = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.ENGLISH).format(new Date(now));
                String startTimeS = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.ENGLISH).format(new Date(startTime));
                String endTimeS = new SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.ENGLISH).format(new Date(endTime));

                Log.i("biky", "now = " + nowS + ", start time = " + startTimeS + ", end time = " + endTimeS);

                PendingIntent startService = PendingIntent.getService(MainActivity.this, 0,
                        intent.setAction(Util.START_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT);

                PendingIntent stopService = PendingIntent.getService(MainActivity.this, 1,
                        intent.setAction(Util.STOP_SERVICE), PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                am.cancel(startService);
                am.cancel(stopService);

                am.setRepeating(AlarmManager.RTC_WAKEUP, startTime,
                        AlarmManager.INTERVAL_DAY, startService);

                am.setRepeating(AlarmManager.RTC_WAKEUP, endTime,
                        AlarmManager.INTERVAL_DAY, stopService);

                if (mCircularRangeSlider.isProgressInsideRange()) {
                    Util.stopService(MainActivity.this);
                    unbindService();
                } else {

                    Util.startService(MainActivity.this);
                    bindService();

                }
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                Calendar calendar = Calendar.getInstance();
                float progress = calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) / 60f;
                mCircularRangeSlider.setProgress(progress);

                if (mCircularRangeSlider.isProgressInsideRange()) {
                    Util.stopService(MainActivity.this);
                    unbindService();

                } else {
                    Util.startService(MainActivity.this);
                    bindService();
                }
                handler.postDelayed(this, 60000);
            }
        };
    }


    @Override
    protected void onResume() {
        handler.post(runnable);
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService();
    }

    private void bindService() {
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        unbindService();
        handler.removeCallbacks(runnable);
        super.onDestroy();
    }

    private boolean isBound;
    private MyService myService;
    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.MyBinder myBinder = (MyService.MyBinder) service;
            myService = myBinder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

}
