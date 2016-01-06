package de.sindzinski.batterysaver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by steffen on 05.01.16.
 */
public class BatterySaverService extends Service {
    private static final String TAG = "BatterySaverService";

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

        float batteryPct = level / (float)scale;
        Log.i(TAG, "Batterylevel: " + batteryPct);
        if (batteryPct < ((float) criticalBatteryLevel/100)) {
            Log.i(TAG, "criticalbatterylevel reached: " + criticalBatteryLevel);
            turnWifiOff();
        } else if (batteryPct > criticalBatteryLevel+10) {
            Log.i(TAG, "safebatterylevel reached " + criticalBatteryLevel);
            turnWifiOn();
        }

        //stopSelf();
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO for communication return IBinder implementation
        return null;
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
        }
        String message = "Wifi Off";
        setMessage(message);
    }
    public void turnWifiOn() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        Log.i("BatterySaverReceiver", "BatteryLow");
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        String message = "Wifi On";
        setMessage(message);
    }

    public void setMessage(String message) {
        Intent i = new Intent(this, MainActivity.class);
        // use System.currentTimeMillis() to have a unique ID for the pending intent
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), i, 0);

        // build notification
        // the addAction re-use the same intent to keep the example short
        Notification n  = new Notification.Builder(this)
                .setContentTitle("Battery Saver")
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .build();

        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(this.NOTIFICATION_SERVICE);

        notificationManager.notify(0, n);
    }
}

