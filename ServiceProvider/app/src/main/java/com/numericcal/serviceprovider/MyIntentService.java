package com.numericcal.serviceprovider;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyIntentService extends IntentService {

    private final String pixelKey= "pixels";
    private final String rrKey= "resultReceiver";
    public MyIntentService() {
        super("MyIntentService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            //Bundle b = intent.getExtras();
            //ClassLoader cl = b.getClassLoader();





            ResultReceiver replyReceiver = intent.getParcelableExtra(rrKey);
            int[] msgData = intent.getIntArrayExtra(pixelKey);

            Bundle reply = new Bundle();
            reply.putIntArray("pixels",msgData);
            replyReceiver.send(0,reply);

        }
    }

}
