package com.project.semicolon.findme.broadcast;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class BatteryService extends Service {
    private BatteryLevelReceiver receiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startBatteryLevelBroadCast();
        return START_STICKY;

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
        startBatteryLevelBroadCast();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        startBatteryLevelBroadCast();
        super.onTaskRemoved(rootIntent);
    }
}
