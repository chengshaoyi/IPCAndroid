package com.numericcal.serviceprovider;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.widget.Toast;


import java.io.IOException;

public class SharedMemoryService extends Service {
    static {
        System.loadLibrary("native-lib");
    }

    public SharedMemoryService() {
    }

    public native int nativeGetFD(String name, int numPages);

    public native int nativeGetData(int ind);



    private final ShmemInterface.Stub smBinder = new ShmemInterface.Stub() {
        public ParcelFileDescriptor getFD(String name, int numPages) throws RemoteException {
            try {
                int fd = nativeGetFD(name, numPages); // call to native function
                ParcelFileDescriptor ret= ParcelFileDescriptor.fromFd(fd);
                return ret;
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
        public int informMessageSend(int ind) throws  RemoteException {
            // just to check if an index is written properly
            return nativeGetData(ind);
        }

    };







    @Override
    public IBinder onBind(Intent intent) {

        return smBinder;

    }
}
