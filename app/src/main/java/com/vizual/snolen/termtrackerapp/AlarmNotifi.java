package com.vizual.snolen.termtrackerapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

  // Alarm Notification
public class AlarmNotifi extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent){

        createNotification(context, "Term Tracker", "Check your courses & assessments!", "Alert");
    }


      //  Notification
    public void createNotification(Context context, String msg, String msgText, String msgAlert) {

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, MainActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.vhlogo)
                .setContentTitle(msg)
                .setTicker(msgAlert)
                .setContentText(msgText);

        // Erase & Notification Sound
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, mBuilder.build());
    }
}
