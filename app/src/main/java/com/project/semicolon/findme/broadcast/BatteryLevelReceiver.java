package com.project.semicolon.findme.broadcast;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.telephony.SmsManager;

import com.project.semicolon.findme.AppExactors;
import com.project.semicolon.findme.SharedHelper;
import com.project.semicolon.findme.database.AppDatabase;

import java.util.List;

public class BatteryLevelReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
        if (batteryLevel == 100) {
            sendSmsMessage(context);
        }
    }

    private void sendSmsMessage(final Context context) {
        final AppDatabase database = AppDatabase.getInstance(context);
        final SmsManager smsManager = SmsManager.getDefault();

        AppExactors.getInstance().getDiskIo().execute(new Runnable() {
            @Override
            public void run() {

                List<String> phoneNumbers = database.dao().getAllPhoneNumbers();
                for (String phoneNumber : phoneNumbers) {

                    String address = SharedHelper.getLocation(context, "address");
                    String message = "My phone's battery dies. I'm in " + address + ". Don't worry!";
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context,
                            BatteryLevelReceiver.class), 0);
                    smsManager.sendTextMessage(phoneNumber,
                            null,
                            message,
                            pendingIntent,
                            null);


                }

                abortBroadcast();


            }


        });

    }

}
