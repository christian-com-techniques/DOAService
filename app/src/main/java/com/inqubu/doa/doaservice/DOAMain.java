package com.inqubu.doa.doaservice;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.location.LocationManager;
import java.io.*;
import android.util.Log;
import android.widget.*;
import android.view.View.OnClickListener;
import android.view.*;
import android.os.Handler;
import android.bluetooth.*;
import android.bluetooth.BluetoothDevice;

import java.util.ArrayList;
import java.util.UUID;


public class DOAMain extends Activity {

    public enum WirelessTechnology {
        BLUETOOTH,
        WIFI
    }

    BluetoothHandler bh = null;
    WiFiSSN wh = null;
    WiFiSSN wf = null;
    static boolean serviceStarted = false;
    static TextView tv = null;
    BluetoothAdapter mBluetoothAdapter;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            boolean isDiscoverable = resultCode > 0;
            if (isDiscoverable) {
                System.out.println("Discoverability enabled");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doamain);
        tv = (TextView) findViewById(R.id.sensorOutput);

        //Change the wireless technology here
        final WirelessTechnology wt = WirelessTechnology.BLUETOOTH;


        ImageView img = (ImageView) findViewById(R.id.powerbutton);
        img.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TextView tv = (TextView) findViewById(R.id.sensorOutput);
                if(serviceStarted == false) {
                    serviceStarted = true;
                    tv.setText("Started!");
                    if(wt == WirelessTechnology.BLUETOOTH) {
                        bh.makeDeviceVisible();
                    } else {
                        wh.makeDeviceVisible();
                    }
                } else {
                    serviceStarted = false;
                    if(wt == WirelessTechnology.BLUETOOTH) {
                        bh.destroyReceiver();
                    } else {

                    }
                    tv.setText("Stopped!");
                }
            }
        });

        if(wt == WirelessTechnology.BLUETOOTH) {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                System.out.println("No device found");
                return;
            }

            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }

            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3000);
            startActivityForResult(discoverableIntent,1);
            bh = new BluetoothHandler(this, mBluetoothAdapter);
        } else {

            wh = new WiFiSSN(this, getMainLooper());
        }

    }

    public static void setConsoleOutput(String out) {
        if(out == null) {
            return;
        }
        tv.setText(out);
    }

    public static void setConsoleOutputAdd(String out) {
        if(out == null) {
            return;
        }
        tv.append(out);
    }

    @Override
    public void onBackPressed() {
        System.out.println("Paused");
        if(serviceStarted == true) {
            serviceStarted = false;
            DOAAccessHandler.resetPublicChunks();
            bh.destroyReceiver();
        }
        finish();
    }

    public static void setStarted(boolean started) {
        serviceStarted = started;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_doamain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
