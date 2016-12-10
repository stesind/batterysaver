package de.sindzinski.batterysaver;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

/**
 * Created by steffen on 05.01.16.
 */
public class BatterySaverService extends Service {
    private static final String TAG = "BatterySaverService";
    final int mNotificationId = 0;
    final static String GROUP_KEY_BATTERY_SAVER = "group_key_battery_saver";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //TODO do something useful
        Log.i(TAG, "BatteryServerService called");

        final String CRITICALBATTERYLEVEL = "criticalbatterylevel";
        int criticalBatteryLevel = intent.getIntExtra(CRITICALBATTERYLEVEL, 39);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

// How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        int batteryPct = (int) ((level / (float)scale) * 100);
        Log.i(TAG, "Battery level: " + batteryPct + "% Critical battery level: " + criticalBatteryLevel + "%");
        if (batteryPct < (criticalBatteryLevel)) {
            Log.i(TAG, "criticalbatterylevel reached: " );
            setNotification("Critical Battery level: " + batteryPct + "% Critical battery level: " + criticalBatteryLevel + "%");
            turnWifiOff();
        } else if (batteryPct > criticalBatteryLevel+10) {
            Log.i(TAG, "safebatterylevel reached" );
            //setNotification("safebatterylevel reached");
        }

        stopSelf();
        return Service.START_NOT_STICKY;
    }

    //Bind is needed for communication between activity and service
    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;    // returns null because we do not want to communicate with service
    }

    //starts a broadcast receiver
    private void publishResults(String outputPath, int result) {
        final String STATUS = "status";
        final String NOTIFICATION = "de.sindzinski.batterysaver.BatterySaverReceiver";
        String status ="";

        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(STATUS, status);
        sendBroadcast(intent);
    }

    public void turnWifiOff() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        Log.i("BatterySaverReceiver", "Power Connected");
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
            String message = "Wifi turned off by battery saver service";
            setNotification(message);
        }


    }
    public void turnWifiOn() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        Log.i("BatterySaverReceiver", "BatteryLow");
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        String message = "Wifi On";
        setNotification(message);
    }

    public void setNotification(String message) {
        Intent i = new Intent(this, MainActivity.class);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        //PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), i, 0);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);


        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n  = new NotificationCompat.Builder(this)
                .setContentTitle("Battery Saver")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
                .setContentIntent(pIntent)
                .setGroup(GROUP_KEY_BATTERY_SAVER)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(mNotificationId, n);

//        NotificationCompat.Builder mBuilder =
//                (NotificationCompat.Builder) new NotificationCompat.Builder(this)
//                        .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
//                        .setContentTitle("Battery Saver Service")
//                        .setAutoCancel(false)
//                        .setGroup(GROUP_KEY_BATTERY_SAVER)
//                        .setGroupSummary(false)
//                        .setContentText(message);
//        // Creates an explicit intent for an Activity in your app
//        Intent resultIntent = new Intent(this, MainActivity.class);
//
//        // The stack builder object will contain an artificial back stack for the
//        // started Activity.
//        // This ensures that navigating backward from the Activity leads out of
//        // your application to the Home screen.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        // Adds the back stack for the Intent (but not the Intent itself)
//        stackBuilder.addParentStack(MainActivity.class);
//        // Adds the Intent that starts the Activity to the top of the stack
//        stackBuilder.addNextIntent(resultIntent);
//        PendingIntent resultPendingIntent =
//                stackBuilder.getPendingIntent(
//                        0,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        mBuilder.setContentIntent(resultPendingIntent);
//        NotificationManager mNotificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        // mId allows you to update the notification later on.
//        // using mNotificationId changes the old notification instead of creating a new one
//        //mNotificationManager.notify(mNotificationId, mBuilder.build());
//        mNotificationManager.notify(mNotificationId, mBuilder.build());
    }
}

