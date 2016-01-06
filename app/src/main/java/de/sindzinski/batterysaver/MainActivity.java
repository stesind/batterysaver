package de.sindzinski.batterysaver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity  {
    private static final long REPEAT_TIME = 1000 * 30;
    private static final String TAG = "BatterySaver";
    private static final int DEFAULTCRITICALBATTERYLEVEL = 30;
    final String CRITICALBATTERYLEVEL = "criticalbatterylevel";
    int criticalBatteryLevel;
    TextView textViewCriticalBatteryLevel;
    SeekBar seekBar;

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

        Switch switchWifi = (Switch) findViewById(R.id.switchWifi);
        Switch switchReceiver = (Switch) findViewById(R.id.switchReceiver);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        switchWifi.setChecked(mWifi.isConnected());
        switchWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The switch is enabled
                    turnWifiOn();
                } else {
                    // The switch is disabled
                    turnWifiOff();
                }
            }
        });

        //set the switch button to the actual registration state of broadcast receiver
        switchReceiver.setChecked(checkReceiver());

        switchReceiver.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
        Switch switchService = (Switch) findViewById(R.id.switchService);

        //set the switch button to the actual registration state of broadcast receiver
        switchService.setChecked(checkReceiver());

        switchService.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    startBatterySaverService();
                } else {
                    // The toggle is disabled
                    stopBatterySaverService();
                }
            }
        });

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textViewCriticalBatteryLevel = (TextView) findViewById(R.id.textViewCriticalBatteryLevel);

        seekBar.setMax(100);
        seekBar.setProgress(DEFAULTCRITICALBATTERYLEVEL);
        textViewCriticalBatteryLevel.setText(String.valueOf(DEFAULTCRITICALBATTERYLEVEL));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                criticalBatteryLevel = progress;
                textViewCriticalBatteryLevel.setText(String.valueOf(criticalBatteryLevel));
            }
        });

    }

    public boolean checkReceiver() {
        ComponentName receiver = new ComponentName(this, BatterySaverReceiver.class);
        PackageManager pm = getPackageManager();
        int componentEnabledSetting = pm.getComponentEnabledSetting(receiver);
        if (componentEnabledSetting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            return true;
        } else {
            return false;
        }
    }

    public void registerReceiver() {
        ComponentName receiver = new ComponentName(this, BatterySaverReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                //COMPONENT_ENABLED_STATE_DEFAULT	Sets the state to the manifest file value
                PackageManager.DONT_KILL_APP);
        String message = "BatterySaverReceiver registered";
        setMessage(message);
    }

    public void unRegisterReceiver() {
        ComponentName receiver = new ComponentName(this, BatterySaverReceiver.class);
        PackageManager pm = getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        String message = "BatterySaverReceiver unregistered";
        setMessage(message);
    }

    public void startBatterySaverService() {
        // use this to start and trigger a service

        Calendar cal = Calendar.getInstance();

        Intent intent = new Intent(this, BatterySaverService.class);
        intent.putExtra(CRITICALBATTERYLEVEL, criticalBatteryLevel);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        // schedule 30 seconds after boot
        //cal.add(Calendar.SECOND, 30);
        //
        // Fetch every 30 seconds
        // InexactRepeating allows Android to optimize the energy consumption
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(), REPEAT_TIME, pendingIntent);
    }

    public void stopBatterySaverService() {
        final String CRITICALBATTERYLEVEL = "criticalbatterylevel";

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, BatterySaverService.class);
        intent.putExtra(CRITICALBATTERYLEVEL, 30.0F);
/*        intent.putExtra("ALERT_TIME", alert.date);
        intent.putExtra("ID_ALERT", alert.idAlert);
        intent.putExtra("TITLE", alert.title);
        intent.putExtra("GEO_LOC", alert.isGeoLoc);*/
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        alarmManager.cancel(pendingIntent);
        Log.i(TAG,"REMOVED BatteryServerService");
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

    public void turnWifiOff() {
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        Log.i("BatterySaverReceiver", "Power Connected");
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
