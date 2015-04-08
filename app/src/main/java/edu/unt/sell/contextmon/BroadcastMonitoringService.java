package edu.unt.sell.contextmon;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.os.IBinder;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

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
        addAllKnownActions(intentFilter);
        registerReceiver(mAnyBroadcastReceiver, intentFilter);
    }

    private void registerBroadcastReceiverForActionsWithDataType() throws MalformedMimeTypeException {
        IntentFilter intentFilter = new IntentFilter();

        // This is needed for broadcasts like new picture, which is data type: "image/*"
        intentFilter.addDataType("*/*");

        addAllKnownActions(intentFilter);
        registerReceiver(mAnyBroadcastReceiver, intentFilter);
    }

    private void registerBroadcastReceiverForActionsWithSchemes() throws MalformedMimeTypeException {
        IntentFilter intentFilter = new IntentFilter();

        // needed for uninstalls
        intentFilter.addDataScheme("package");

        // needed for file system mounts
        intentFilter.addDataScheme("file");

        // other schemes
        intentFilter.addDataScheme("geo");
        intentFilter.addDataScheme("market");
        intentFilter.addDataScheme("http");
        intentFilter.addDataScheme("tel");
        intentFilter.addDataScheme("mailto");
        intentFilter.addDataScheme("about");
        intentFilter.addDataScheme("https");
        intentFilter.addDataScheme("ftps");
        intentFilter.addDataScheme("ftp");
        intentFilter.addDataScheme("javascript");

        addAllKnownActions(intentFilter);
        registerReceiver(mAnyBroadcastReceiver, intentFilter);
    }

    private void addAllKnownActions(IntentFilter pIntentFilter) {
        // System broadcasts
        List<String> sysBroadcasts = Arrays.asList(getResources().getStringArray(R.array.system_broadcast));
        for (String sysBroadcast : sysBroadcasts) {
            pIntentFilter.addAction(sysBroadcast);
        }
    }

    // no need for notifications
}
