package de.sindzinski.batterysaver;

import android.app.Fragment;
import android.os.BatteryManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by steffen on 04.01.16.
 */
public class BatteryFragment extends Fragment {
    // pass data to the fragment using factory pattern
    // later get date in onCreate with getArguments
    public static BatteryFragment newInstance(Boolean isCharging, Boolean isFull, Boolean usbCharge, Boolean acCharge) {
        BatteryFragment testFragment = new BatteryFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putBoolean("isCharging", isCharging);
        args.putBoolean("isFull", isFull);
        args.putBoolean("usbCharge", usbCharge);
        args.putBoolean("acCharge", acCharge);
        testFragment.setArguments(args);

        return testFragment;
    }

    public BatteryFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.battery_fragment, container, false);

        //get Arguments from bundle
        boolean isCharging = getArguments().getBoolean("isCharging", false);
        boolean isFull = getArguments().getBoolean("isFull", false);
        boolean usbCharge = getArguments().getBoolean("usbCharge", false);
        boolean acCharge = getArguments().getBoolean("acCharge", false);

        TextView textView = (TextView) view.findViewById(R.id.textView);
        textView.setText("Current status:" +
                "\nisCharging: " + isCharging
                + " \nisFull: " + isFull
                + " \nusbCharge: " + usbCharge
                + " \nacCharge: " + acCharge);

        return view;
    }
}
