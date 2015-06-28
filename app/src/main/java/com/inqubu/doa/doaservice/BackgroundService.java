package com.inqubu.doa.doaservice;
import android.app.*;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.Toast;
import android.os.IBinder;
import android.content.Intent;

import java.io.FileDescriptor;

/**
 * Created by VORHECHR on 13.04.2015.
 */
public class BackgroundService extends Service  {

    private NotificationManager mNM;
    private int NOTIFICATION = 0;

    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(NOTIFICATION);

        // Tell the user we stopped.
        Toast.makeText(this, 0, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new IBinder() {
            @Override
            public String getInterfaceDescriptor() throws RemoteException {
                return null;
            }

            @Override
            public boolean pingBinder() {
                return false;
            }

            @Override
            public boolean isBinderAlive() {
                return false;
            }

            @Override
            public IInterface queryLocalInterface(String s) {
                return null;
            }

            @Override
            public void dump(FileDescriptor fileDescriptor, String[] strings) throws RemoteException {

            }

            @Override
            public void dumpAsync(FileDescriptor fileDescriptor, String[] strings) throws RemoteException {

            }

            @Override
            public boolean transact(int i, Parcel parcel, Parcel parcel2, int i2) throws RemoteException {
                return false;
            }

            @Override
            public void linkToDeath(DeathRecipient deathRecipient, int i) throws RemoteException {

            }

            @Override
            public boolean unlinkToDeath(DeathRecipient deathRecipient, int i) {
                return false;
            }
        };
    }
}
