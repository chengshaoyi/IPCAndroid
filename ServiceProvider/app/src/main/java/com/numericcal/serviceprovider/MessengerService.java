package com.numericcal.serviceprovider;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

public class MessengerService extends Service {
    public MessengerService() {
    }
    Object receivedData;
    class MessageHandler extends Handler{
        @Override
        public void handleMessage(Message msg)
        {
            receivedData = msg.obj;
            int[] actualData = ((Bundle)receivedData).getIntArray("pixels");
            // compose a new message and send back
            Message replyMsg = Message.obtain(null, 1,0,0,receivedData);
            try {
                msg.replyTo.send(replyMsg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
    final Messenger myMessenger = new Messenger(new MessageHandler());
    @Override
    public IBinder onBind(Intent intent) {
        return myMessenger.getBinder();
    }
}
