package com.example.nomorescrolling;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class UsageMonitorService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
