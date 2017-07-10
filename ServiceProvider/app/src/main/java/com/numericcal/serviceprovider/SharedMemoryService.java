package com.numericcal.serviceprovider;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

import java.io.IOException;

public class SharedMemoryService extends Service {
    static {
        System.loadLibrary("native-lib");
    }

    public SharedMemoryService() {
    }

    public native int nativeGetFD(String name, int numPages);




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

    };



    @Override
    public IBinder onBind(Intent intent) {

        return smBinder;
    }
}
