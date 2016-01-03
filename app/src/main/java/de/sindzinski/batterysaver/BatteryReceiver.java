package de.sindzinski.batterysaver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.R;
/**
 * Created by steffen on 03.01.16.
 */
public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        // assumes WordService is a registered service
        //Intent intent = new Intent(context, WordService.class);
        //context.startService(intent);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        String message =null;
        String action = intent.getAction();
        switch (action) {
            case "android.intent.action.BATTERY_LOW":
                Log.i("BatteryReceiver", "BatteryLow");
                if (wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(false);
                }
                message = "Wifi turned off";
                break;
            case "android.intent.action.BATTERY_OKAY":
                Log.i("BatteryReceiver", "BatteryOK");
                break;
            case "android.intent.action.ACTION_POWER_CONNECTED":
                Log.i("BatteryReceiver", "Power Connected");
                if (!wifiManager.isWifiEnabled()) {
                    wifiManager.setWifiEnabled(true);
                }
                message = "Wifi turned off";
                break;
        }
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
}
