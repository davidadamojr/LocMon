package edu.unt.sell.contextmon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BroadcastMonitoringService extends Service {

    private static final String TAG = BroadcastMonitoringService.class.getSimpleName();
    private static final int NOTIFICATION = 1;
    private AnyBroadcastReceiver mAnyBroadcastReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mAnyBroadcastReceiver = new AnyBroadcastReceiver();
    }

    public BroadcastMonitoringService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent pIntent, int pFlags, int pStartId) {
        Log.d(TAG, "onStartCommand");
        registerAnyBroadcastReceiver();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        try {
            unregisterReceiver(mAnyBroadcastReceiver);
        } catch (Exception e) {
            Log.d(TAG, "Skipping Exception which might be raised when the receiver is not yet registered");
        }

        // hideNotification(getApplicationContext());
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    private void registerAnyBroadcastReceiver() {
        try {
            registerBroadcastReceiverForActions();
            registerBroadcastReceiverForActionsWithDataType();
            registerBroadcastReceiverForActionsWithSchemes();
            Log.d(TAG, "Registered receivers.");
        } catch (Exception e) {
            Log.d(TAG, "Exception while registering: " + e.getMessage());
        }
    }

    private void registerBroadcastReceiverForActions() {
        IntentFilter intentFilter = new IntentFilter();
        addAllKnownActions
    }
}
