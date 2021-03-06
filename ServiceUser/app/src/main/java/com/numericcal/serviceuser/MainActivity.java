package com.numericcal.serviceuser;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.numericcal.serviceprovider.ShmemInterface;

// extends service to receive response
public class MainActivity extends AppCompatActivity {
    private final String pixelKey= "pixels";
    private final String rrKey= "resultReceiver";
    private boolean mBound=false;
    private Messenger serviceMessenger = null;
    long msgTimeLog;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            serviceMessenger = new Messenger(service);
            mBound = true;
            Toast.makeText(getApplicationContext(),"user connected", Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceMessenger = null;
            mBound = false;
            Toast.makeText(getApplicationContext(),"user disconnected", Toast.LENGTH_SHORT).show();
        }
    };
    class ActivityMessengerHandler extends Handler {
        @Override
        public void handleMessage(Message msg)
        {
            msgTimeLog = System.currentTimeMillis() - msgTimeLog;
            View timeView = findViewById(R.id.msgTimeText);
            ((TextView)timeView).setText(""+(msgTimeLog /1.0f)+" ms");
            Toast.makeText(getApplicationContext(),"data received from message service", Toast.LENGTH_SHORT).show();
        }
    }

    private Messenger activityMessenger = new Messenger(new ActivityMessengerHandler());
    public void connectMsgButtonClicked(View view)
    {
        // bind to the service
        Intent bindIntent = new Intent();
        bindIntent.setComponent(new ComponentName("com.numericcal.serviceprovider","com.numericcal.serviceprovider.MessengerService"));
        bindService(bindIntent,mConnection, Context.BIND_AUTO_CREATE);
    }
    public void sendMsgButtonClicked(View view)
    {
        if(!mBound)
        {
            Toast.makeText(getApplicationContext(),"no connected service", Toast.LENGTH_SHORT).show();
            return;
        }
        // send stuff to the service
        Bundle msgData = new Bundle();
        msgData.putIntArray(pixelKey,dataToSend);
        Message msg = Message.obtain(null, 1,0,0, msgData);
        msg.replyTo = activityMessenger;
        msgTimeLog = System.currentTimeMillis();

        try {
            serviceMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch(Exception e)
        {
            Toast.makeText(getApplicationContext(),"send data fail", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    //----------------- end of message based ----------------------------//
    long intServTimeLog;

    public class MyResultReceiver extends ResultReceiver{

        public MyResultReceiver(Handler handler)
        {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            intServTimeLog = System.currentTimeMillis()-intServTimeLog;
            Toast.makeText(getApplicationContext(),"data from IntentService", Toast.LENGTH_SHORT).show();
            View timeView = findViewById(R.id.intentTimeText);
            ((TextView)timeView).setText(""+(intServTimeLog /1.0f)+" ms");
        }

    }
    public MyResultReceiver activityResultReceiver = new MyResultReceiver(new Handler());


    public static ResultReceiver receiverForSending(ResultReceiver actualReceiver) {
        Parcel parcel = Parcel.obtain();
        actualReceiver.writeToParcel(parcel,0);
        parcel.setDataPosition(0);
        ResultReceiver receiverForSending = ResultReceiver.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return receiverForSending;
    }

    public void sendIntentButtonClicked(View view)
    {
        Intent getIntentService = new Intent();
        getIntentService.setComponent(new ComponentName("com.numericcal.serviceprovider","com.numericcal.serviceprovider.MyIntentService"));

        intServTimeLog = System.currentTimeMillis();

        getIntentService.putExtra(pixelKey,dataToSend);
        getIntentService.putExtra(rrKey,receiverForSending(activityResultReceiver));
        startService(getIntentService);
    }


    //-------------- end of intentService based -----------------------//

    ParcelFileDescriptor pfd;
    long shmemTimeLog;
    static {
        System.loadLibrary("native-lib");
    }
    public native void shmemInit(int fd, int numPages);
    public native void shmemSend(int[] data);
    public native int getPageSize();




    public void sendShmemButtonClicked(View view)
    {
        shmemTimeLog = System.currentTimeMillis();
        shmemSend(dataToSend);
        int rtVal = -1;
        try {
            // check the data item seen on the other end
            // just to make sure it is written
            rtVal = remoteShmem.informMessageSend(0);
        }catch (RemoteException e) {
            e.printStackTrace();
        }
        shmemTimeLog = System.currentTimeMillis() - shmemTimeLog;
        View timeView = findViewById(R.id.shmemTimeText);
        ((TextView)timeView).setText(""+(shmemTimeLog /1.0f)+" ms "+"and check result is "+rtVal);

    }

    public void setupShmemButtonClicked(View view)
    {
        Intent bindIntent = new Intent();
        bindIntent.setComponent(new ComponentName("com.numericcal.serviceprovider","com.numericcal.serviceprovider.SharedMemoryService"));
        bindService(bindIntent,shmemConnection, Context.BIND_AUTO_CREATE);

    }
    ShmemInterface remoteShmem;
    private ServiceConnection shmemConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteShmem = ShmemInterface.Stub.asInterface(service);
            int pageSize = getPageSize();
            try {
                int numPages = numWord*4/pageSize+1;

                pfd = remoteShmem.getFD("abc",numPages);
                shmemInit(pfd.getFd(),numPages);
                Toast.makeText(getApplicationContext(),"shmem connected "+numPages, Toast.LENGTH_SHORT).show();


            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            remoteShmem = null;
            Toast.makeText(getApplicationContext(),"shmem disconnected", Toast.LENGTH_SHORT).show();
        }
    };









    //------------- end of shared mem -------------------------------//
    private int[] dataToSend;
    private final int numWord = 160*120;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dataToSend = new int[numWord];
        for(int i=0; i<numWord;i++)
        {
            dataToSend[i] = i;
        }
        // shared memory stuff

    }
    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(mBound)
            unbindService(mConnection);
    }



}
