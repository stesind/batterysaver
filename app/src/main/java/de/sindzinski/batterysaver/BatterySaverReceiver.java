package de.sindzinski.batterysaver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.util.Log;
import android.R;
/**
 * Created by steffen on 03.01.16.
 */
public class BatterySaverReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String message =null;
        String action = intent.getAction();

        switch (action) {
            case "android.intent.ACTION_BATTERY_CHANGED":
                Log.i("BatterySaverReceiver", "BatteryChanging");
            case "android.intent.action.BATTERY_LOW":
                Log.i("BatterySaverReceiver", "BatteryLow");
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);

                    message = "BatteryLow - Wifi turned off";
                    Intent i = new Intent(context, MainActivity.class);
                    // use System.currentTimeMillis() to have a unique ID for the pending intent
                    PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), i, 0);

                    // build notification
                    // the addAction re-use the same intent to keep the example short
                    Notification n  = new Notification.Builder(context)
                            .setContentTitle("Battery Saver")
                            .setContentText(message)
                            .setSmallIcon(R.drawable.ic_lock_idle_charging)
                            .setContentIntent(pIntent)
                            .setAutoCancel(true)
                            .build();

                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                    notificationManager.notify(0, n);
                }

                break;
            case "android.intent.action.BATTERY_OKAY":
                Log.i("BatterySaverReceiver", "BatteryOK");
            case "android.intent.action.ACTION_DOCK_EVENT":
                Log.i("BatterySaverReceiver", "Dock connected");
            case "android.intent.action.ACTION_POWER_CONNECTED":
                Log.i("BatterySaverReceiver", "Power Connected");
                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                    message = "Power Connected - Wifi turned on";
                    Intent i = new Intent(context, MainActivity.class);
                    // use System.currentTimeMillis() to have a unique ID for the pending intent
                    PendingIntent pIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), i, 0);

                    // build notification
                    // the addAction re-use the same intent to keep the example short
                    Notification n  = new Notification.Builder(context)
                            .setContentTitle("Battery Saver")
                            .setContentText(message)
                            .setSmallIcon(R.drawable.ic_lock_idle_charging)
                            .setContentIntent(pIntent)
                            .setAutoCancel(true)
                            .build();

                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

                    notificationManager.notify(0, n);
                }
                break;
            case "Intent.ACTION_BOOT_COMPLETED":
                Log.i("BatterySaverReceiver", "Device booted - install service");

                break;
        }

    }
}
