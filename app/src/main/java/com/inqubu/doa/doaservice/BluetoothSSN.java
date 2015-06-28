package com.inqubu.doa.doaservice;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by VORHECHR on 16.04.2015.
 */
public class BluetoothSSN {

    private Activity activity;
    private static Context context;
    private BluetoothAdapter mBluetoothAdapter;

    public BluetoothSSN(Activity activity) {
        this.activity = activity;
        this.context = this.activity.getApplication().getApplicationContext();
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void start() {
        if (mBluetoothAdapter == null) {
            System.out.println("No device found");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        scanForDevices();

    }

    private void scanForDevices() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.EXTRA_UUID);
        filter.addAction(BluetoothDevice.ACTION_UUID);

        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                }

                if(BluetoothDevice.ACTION_UUID.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                }

                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                }

            }

        };

        context.registerReceiver(mReceiver, filter);
        System.out.println("Scan started");

        Timer timer = new Timer ();
        TimerTask hourlyTask = new TimerTask () {
            @Override
            public void run () {
                mBluetoothAdapter.startDiscovery();
            }
        };
        timer.schedule(hourlyTask, 0l, 20000);
    }

    public void stop() {

    }


}
