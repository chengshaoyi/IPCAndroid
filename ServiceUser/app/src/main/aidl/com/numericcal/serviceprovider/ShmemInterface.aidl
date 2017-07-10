// ShmemInterface.aidl
package com.numericcal.serviceprovider;

// Declare any non-default types here with import statements
interface ShmemInterface {
    ParcelFileDescriptor getFD(String name, int numPages);
    int informMessageSend(int ind);
}
