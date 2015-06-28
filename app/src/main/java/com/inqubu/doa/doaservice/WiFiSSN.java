package com.inqubu.doa.doaservice;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import java.util.ArrayList;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.WpsInfo;
import java.net.Socket;

/**
 * Created by VORHECHR on 16.04.2015.
 */
public class WiFiSSN {

    private static Context c;
    private static Channel mChannel;
    private static WifiP2pManager mManager;
    private static boolean firstrun;
    private Activity app_activity;
    private static Looper looper;
    private static BroadcastReceiver mReceiver = null;
    private static List<WifiP2pDevice> devices = new ArrayList<>();
    private static WifiP2pDnsSdServiceRequest serviceRequest;
    private static int runnablecounter = 0;

    public WiFiSSN(Activity activity, Looper looper) {
        app_activity = activity;
        this.looper = looper;
        c = activity.getApplication().getApplicationContext();
        this.firstrun = true;
    }

    public static void makeDeviceVisible() {
        Map<String, String> record = new HashMap<>();
        record.put("listenport", "4545");
        record.put("available", "visible");
        mManager = (WifiP2pManager) c.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(c,looper,null);

        scanForDevices();

    }

    public static void scanForDevices() {

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                    int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        System.out.println("WIFI ENABLED");
                    } else {
                        System.out.println("WIFI NOT ENABLED");
                    }
                } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

                    mManager.requestPeers(mChannel, new PeerListListener() {
                        @Override
                        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
                            ArrayList<WifiP2pDevice> co = new ArrayList<WifiP2pDevice>(wifiP2pDeviceList.getDeviceList());
                            for(int i=0;i<co.size();i++) {
                                WifiP2pDevice dv = co.get(i);
                                String address = dv.deviceAddress;
                                String name = dv.deviceName;
                                boolean inflag = true;
                                for(int j=0;j<devices.size();j++) {
                                    if(address.equals(devices.get(j).deviceAddress)) {
                                        inflag = false;
                                        break;
                                    }
                                }
                                if(inflag) {
                                    devices.add(dv);
                                }

                                sendData(dv);
                                System.out.println("Device: "+name+", "+address);
                            }
                        }
                    });

                } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                    mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
                        @Override
                        public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {

                            if(wifiP2pInfo.isGroupOwner) {

                                Thread bglt = new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            ServerSocket serversocket = new ServerSocket(4545);
                                            System.out.println("Listening on "+serversocket.getInetAddress()+", "+serversocket.getLocalPort());
                                            Socket socket = serversocket.accept();
                                            InputStream is = socket.getInputStream();
                                            String ot = readItem(is);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                bglt.start();

                            } else if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner == false) {

                                Thread bglt = new Thread() {
                                    @Override
                                    public void run() {
                                        try {
                                            Socket socket = new Socket();
                                            socket.connect(new InetSocketAddress(wifiP2pInfo.groupOwnerAddress, 4545));
                                            OutputStream tmpOut = socket.getOutputStream();
                                            writeItem(tmpOut, "");
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                bglt.start();
                            }

                        }
                    });

                } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                    System.out.println("WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
                }
            }
        };
        c.registerReceiver(mReceiver, filter);

        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(int reasonCode) {
                System.out.println(String.valueOf(reasonCode));
            }
        });

        final Handler sendhandler = new Handler();
        Runnable sendrunnable = new Runnable() {
            @Override
            public void run() {


                if(DOAMain.serviceStarted == true) {
                    sendhandler.postDelayed(this, 5000);
                }
                if(devices.size() > 0) {
                    runnablecounter = 0;
                    for(int i=0;i<devices.size();i++) {
                        System.out.println("Device "+devices.get(i).deviceAddress);
                    }
                    System.out.println("--------");
                }

            }
        };
        sendhandler.postDelayed(sendrunnable, 5000);

    }

    public static void writeItem(OutputStream out, String s) throws IOException {
        byte[] bs = s.getBytes();
        out.write(bs.length);
        out.write(bs.length>>>8);
        out.write(bs.length>>>16);
        out.write(bs.length>>>24);
        out.write(bs);
        out.flush();
    }

    public static String readItem(InputStream in) throws IOException {
        int len = in.read();
        if (len<0) throw new IOException("end of stream");
        for(int i=1;i<4;i++) {
            int n = in.read();
            if (n<0) throw new IOException("partial data");
            len |= n << (i<<3);
        }
        byte[] bs = new byte[len];
        int ofs = 0;
        while (len>0) {
            int n = in.read(bs, ofs, len);
            if (n<0) throw new IOException("partial data");
            ofs += n;
            len -= n;
        }
        return new String(bs);
    }

    private static void sendData(final WifiP2pDevice device) {

        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new ActionListener() {
            @Override
            public void onSuccess() {
                System.out.println("connection to "+config.deviceAddress);
            }
            @Override
            public void onFailure(int reason) {
            }
        });
    }

    public static void destroyReceiver() {
        c.unregisterReceiver(mReceiver);
    }

}


