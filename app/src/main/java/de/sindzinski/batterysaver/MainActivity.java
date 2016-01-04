package de.sindzinski.batterysaver;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sharedPrefs;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Register for the battery changed event
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        // Intent is sticky so using null as receiver works fine
        // return value contains the status
        Intent batteryStatus = this.registerReceiver(null, filter);
        // Are we charging / charged?
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                || status == BatteryManager.BATTERY_STATUS_FULL;

        boolean isFull = status == BatteryManager.BATTERY_STATUS_FULL;

        // How are we charging?
        int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
        boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText("Current status:" +
                "\nisCharging: " + isCharging
                + " \nisFull: " + isFull
                + " \nusbCharge: " + usbCharge
                + " \nacCharge: " + acCharge);

        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.toggleButton);

        //set the toogle button to the actual registration state of broadcast receiver
        toggleButton.setChecked(checkReceiver());
        //toggleButton.setChecked(false);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    registerReceiver();
                } else {
                    // The toggle is disabled
                    unRegisterReceiver();
                }
            }
        });
    }

    public boolean checkReceiver() {
        ComponentName receiver = new ComponentName(this, BatteryReceiver.class);
        PackageManager pm = getPackageManager();
        int componentEnabledSetting = pm.getComponentEnabledSetting(receiver);
        if (componentEnabledSetting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            return true;
        } else {
            return false;
        }
    }

    public void registerReceiver() {
        ComponentName receiver = new ComponentName(this, BatteryReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                //COMPONENT_ENABLED_STATE_DEFAULT	Sets the state to the manifest file value
                PackageManager.DONT_KILL_APP);
        String message = "BatteryReceiver registered";
        setMessage(message);
    }

    public void unRegisterReceiver() {
        ComponentName receiver = new ComponentName(this, BatteryReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        String message = "BatteryReceiver unregistered";
        setMessage(message);
    }

    public void wifiOn(View view) {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        Log.i("BatteryReceiver", "BatteryLow");
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        String message = "Wifi On";
        setMessage(message);
    }

    public void wifiOff(View view) {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        Log.i("BatteryReceiver", "Power Connected");
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
        String message = "Wifi Off";
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
