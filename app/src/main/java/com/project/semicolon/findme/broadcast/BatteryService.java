package com.project.semicolon.findme.broadcast;

import android.app.IntentService;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import androidx.annotation.Nullable;

public class BatteryService extends IntentService {
    private BatteryLevelReceiver receiver;
    private static final String TAG = "BatteryService";
    private WakeLock wakeLock;


    public BatteryService() {
        super("BatteryLevelService");
        setIntentRedelivery(true);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: service starting...");
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FindMe:WakeLock");
            wakeLock.acquire(10*60*1000L /*10 minutes*/);
        }

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        startBatteryLevelBroadCast();

    }


    private void startBatteryLevelBroadCast() {
        receiver = new BatteryLevelReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        unregisterReceiver(receiver);
    }
}
