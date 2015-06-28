package com.inqubu.doa.doaservice;

import android.bluetooth.*;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile.ServiceListener;
import android.app.Activity;
import android.content.*;
import android.os.SystemClock;
import android.widget.ArrayAdapter;
import android.util.Log;

import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;

import java.util.List;
import android.os.AsyncTask;
import android.os.Handler;
import java.util.ArrayList;
import android.os.Parcelable;
import 	java.util.UUID;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Random;
import java.text.*;
import java.util.Date;
import android.telephony.TelephonyManager;

/**
 * Created by VORHECHR on 07.04.2015.
 */
public class BluetoothHandler {

    private static Activity activity;
    private static Context c;
    private static BroadcastReceiver mReceiver = null;
    private static final int REQUEST_ENABLE_BT = 0;
    private static boolean locked = false;
    private static boolean firstrun;
    private static List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
    private static List<Thread> connectionThreads = new ArrayList<Thread>();
    private static List<BluetoothSocket> connectionSockets = new ArrayList<BluetoothSocket>();
    static byte[] buffer;
    private static Thread listenThread;
    static BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket bs;
    private static int runnablecounter = 0;

    public BluetoothHandler(Activity activity, BluetoothAdapter bta) {
        this.activity = activity;
        this.mBluetoothAdapter = bta;
        
        c = this.activity.getApplication().getApplicationContext();
        this.firstrun = true;
    }

    public static void makeDeviceVisible() {

        if (mBluetoothAdapter == null) {
            System.out.println("No device found");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothAdapter.enable();
        }

        UUID uuid = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
        String serverName = "BTserver";
        try {
            final BluetoothServerSocket bluetoothServer = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(serverName, uuid);
            listenThread = new Thread(new Runnable() {
                public void run() {
                    while(DOAMain.serviceStarted) {
                        try {
                            System.out.println("LIISTENIING");
                            BluetoothSocket serverSocket = bluetoothServer.accept();
                            System.out.println("CONNECTEED");
                            InputStream is = serverSocket.getInputStream();
                            String ot = readItem(is);
                            System.out.println(ot);
                            final String otm = ot;

                            DataModel dcm = DOAAccessHandler.convertJsonToChunk(otm);
                            System.out.println("RECEIVED: "+otm);
                            if(dcm instanceof DataGroup) {
                                ArrayList<DataChunk> ar = ((DataGroup) dcm).getValue();
                                for(int i=0;i<ar.size();i++) {
                                    //System.out.println(ar.get(i).getKey());
                                    DataChunk dd = ar.get(i);
                                    dd.incrementCounter();
                                    // If entry has passed more than 5 nodes, don't add it -> avoid flooding
                                    if(dd.getTTL() == 1) {
                                        dd.setOldflag(false);
                                    }
                                    if(dd.getTTL() <= 5) {
                                        DOAAccessHandler.addValue(dd);
                                    }

                                }
                            } else {
                                ((DataChunk)dcm).incrementCounter();
                                //When datachunk comes direct from the source (ttl==1), it is not old for sure
                                if(((DataChunk)dcm).getTTL() == 1) {
                                    ((DataChunk)dcm).setOldflag(false);
                                }
                                if(((DataChunk)dcm).getTTL() <= 5) {
                                    DOAAccessHandler.addValue(dcm);
                                }
                            }

                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    DOAMain.setConsoleOutput("");
                                    DOAMain.setConsoleOutput(DOAAccessHandler.printValues());
                                }
                            });

                        } catch (IOException e) {

                            Log.d("BLUETOOTH", e.getMessage());
                        }
                    }
                    try{
                        bluetoothServer.close();
                    } catch(Exception e) { e.printStackTrace(); }
                }
            });
            listenThread.start();
            scanForDevices(mBluetoothAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }

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

    private static void sendData(final BluetoothDevice device) {

            Thread bglt = new Thread() {
                @Override
                public void run() {
                    BluetoothSocket bs;
                    try {
                        //Method reflectMethod = device.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
                        //bs = (BluetoothSocket)reflectMethod.invoke(device, Integer.valueOf(10));

                        UUID uuid = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

                        bs = device.createInsecureRfcommSocketToServiceRecord(uuid);
                        connectionSockets.add(bs);
                        System.out.println("Connection to " + device.getName() + "...");
                        bs.connect();
                        OutputStream tmpOut = bs.getOutputStream();
                        System.out.println("SENDING...");

                        String mac_address = mBluetoothAdapter.getAddress().toString();
                        String devicename = mBluetoothAdapter.getName();

                        long uptime = SystemClock.uptimeMillis()/1000/60;

                        if(batterylevel == 0) {
                            batterylevel = 100;
                        }

                        TelephonyManager mngr = (TelephonyManager)c.getSystemService(c.TELEPHONY_SERVICE);
                        String deviceid = mngr.getDeviceId();

                        String value = "name="+devicename+";uptime="+String.valueOf(uptime)+";battery="+String.valueOf(batterylevel)+";deviceid="+deviceid;

                        DataChunk dc = new DataChunk(mac_address, value);
                        DOAAccessHandler.addValue(dc);
                        DataGroup dagro = DOAAccessHandler.getPublicData();

                        String json = DOAAccessHandler.convertChunkToJson(dagro);

                        writeItem(tmpOut, json);
                        bs.close();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }  /*catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }*/
                }
            };
            bglt.start();
            connectionThreads.add(bglt);


    }


    public static void destroyReceiver() {
        try {
            //bluetoothServer.close();
            DOAAccessHandler.resetPublicChunks();
            listenThread.interrupt();
            c.unregisterReceiver(mReceiver);
        } catch(Exception e) {
            System.out.println("Could not stop service");
            DOAMain.setConsoleOutput("Error: Could not stop service");
        }
    }

    public static int batterylevel = 0;

    public static void scanForDevices(final BluetoothAdapter mBluetoothAdapter) {

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.EXTRA_UUID);
        filter.addAction(BluetoothDevice.ACTION_UUID);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    device.fetchUuidsWithSdp();
                }

                if(Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                    batterylevel = intent.getIntExtra("level", 0);
                    System.out.println("battery changed");
                    try {
                        FileInputStream fis = c.openFileInput("battery.csv");
                    } catch(Exception e) {

                    }
                }

                if(BluetoothDevice.ACTION_UUID.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Parcelable[] uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                    if(uuidExtra != null) {
                        for (int i = 0; i < uuidExtra.length; i++) {
                            if(uuidExtra[i].toString().equals("fa87c0d0-afac-11de-8a39-0800200c9a66")) {
                                //DOAMain.setConsoleOutput("Sending data to "+device.getName()+"...\n");
                                devices.add(device);
                            }
                        }
                    }
                }

                if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    //DOAMain.setConsoleOutput("");
                    Log.d("TAG", "New round started. Found: " + devices.size());
                    for(int j=0;j<connectionSockets.size();j++) {
                        try {
                            connectionSockets.get(j).close();
                        } catch(Exception e) { e.printStackTrace(); }
                    }
                    connectionSockets.clear();
                    for(int j=0;j<connectionThreads.size();j++) {
                        connectionThreads.get(j).interrupt();
                    }
                    connectionThreads.clear();
                    for (int i = 0; i < devices.size(); i++) {
                        //sendData(devices.get(i));
                        //System.out.println(devices.get(i).getName() + " - " + devices.get(i).getAddress() + " - ");
                        //DOAMain.setConsoleOutputAdd(devices.get(i).getName() + " - " + devices.get(i).getAddress() + " - " + devices.get(i).fetchUuidsWithSdp() + "\n");
                    }

                    /*
                    if (devices.size() > 0) {
                        try {
                            Method method = devices.get(0).getClass().getMethod("createBond", (Class[]) null);
                            method.invoke(devices.get(0), (Object[]) null);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    */

                    //mBluetoothAdapter.startDiscovery();
                }

            }

            public void onDestroy(BroadcastReceiver mReceiver) {
                c.unregisterReceiver(mReceiver);
            }
        };


        c.registerReceiver(mReceiver, filter);
        //mBluetoothAdapter.startDiscovery();
        DOAMain.setStarted(true);

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                Random randomGenerator = new Random();
                float randombool = randomGenerator.nextFloat();
                if(DOAMain.serviceStarted == true) {
                    handler.postDelayed(this, 60000);
                }
                //if(firstrun == true || randombool > 0.6) {
                    firstrun = false;
                    devices.clear();
                    System.out.println("Discovery started");
                    //DOAMain.setConsoleOutput("Discovering...");
                    mBluetoothAdapter.startDiscovery();
                //} else {
                    //DOAMain.setConsoleOutput("Waiting for connections...");
                //}
            }
        };
        handler.postDelayed(runnable, 10);


        final Handler sendhandler = new Handler();
        Runnable sendrunnable = new Runnable() {
            @Override
            public void run() {


                if(DOAMain.serviceStarted == true) {
                    sendhandler.postDelayed(this, 15000);
                }
                if(devices.size() > 0) {
                    runnablecounter = 0;
                    for(int i=0;i<devices.size();i++) {
                        final Handler sendInnerhandler = new Handler();
                        Runnable sendInnerrunnable = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    sendData(devices.get(runnablecounter));
                                } catch(Exception e) {
                                    e.printStackTrace();
                                }
                                runnablecounter++;
                            }
                        };
                        sendInnerhandler.postDelayed(sendInnerrunnable, 2000);
                    }
                }

            }
        };
        sendhandler.postDelayed(sendrunnable, 15000);

        final DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        final long ONE_MINUTE_IN_MILLIS=60000;

        final Handler garbagehandler = new Handler();
        Runnable garbagerunnable = new Runnable() {
            @Override
            public void run() {
                if(DOAMain.serviceStarted == true) {
                    garbagehandler.postDelayed(this, 60000);

                    String mac_address = mBluetoothAdapter.getAddress().toString();
                    String devicename = mBluetoothAdapter.getName();

                    long uptime = SystemClock.uptimeMillis()/1000/60;

                    if(batterylevel == 0) {
                        batterylevel = 100;
                    }

                    TelephonyManager mngr = (TelephonyManager)c.getSystemService(c.TELEPHONY_SERVICE);
                    String deviceid = mngr.getDeviceId();

                    String value = "name="+devicename+";uptime="+String.valueOf(uptime)+";battery="+String.valueOf(batterylevel)+";deviceid="+deviceid;

                    DataChunk dc = new DataChunk(mac_address, value);
                    DOAAccessHandler.addValue(dc);

                    List<DataModel> al = DOAAccessHandler.getPublicDataChunks();

                    for(int i=0;i<al.size();i++) {
                        if(al.get(i) instanceof DataChunk) {
                            String createdAt = ((DataChunk)al.get(i)).getCreatedAt();
                            try {
                                Date com_createdAt = sdf.parse(createdAt);
                                Date currentDate = new Date();

                                Calendar gc = new GregorianCalendar();
                                gc.setTime(com_createdAt);
                                gc.add(Calendar.MINUTE, 1);
                                Date chunkDate = gc.getTime();

                                if(chunkDate.before(currentDate)) {
                                    if(((DataChunk) al.get(i)).getOldFlag() == false) {
                                        ((DataChunk)al.get(i)).setOldflag(true);
                                    } else {
                                        DOAAccessHandler.removeValue(al.get(i));
                                        System.out.println("removed");
                                    }
                                }

                            } catch(ParseException e) { e.printStackTrace(); }
                        }
                    }

                    if(HttpsGetRequest.needupdate == true) {
                        DataGroup dagro = DOAAccessHandler.getPublicData();
                        String json = DOAAccessHandler.convertChunkToJson(dagro);
                        new HttpsGetRequest().execute(json);
                    }

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            DOAMain.setConsoleOutput("");
                            DOAMain.setConsoleOutput(DOAAccessHandler.printValues());
                        }
                    });
                }
            }
        };
        garbagehandler.postDelayed(garbagerunnable, 10000);


    }

}
