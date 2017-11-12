package it.tfobz.jj_zp.vokabeltrainer;


import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

/**
 * Created by julian on 12/11/17.
 */

public class MyNotificationManager extends BroadcastReceiver{

    private final static String title = "Vergiss nicht zu lernen!";
    private final static String text = "Es gibt Vokabeln die heute gelernt werden sollen";

    private final static int alarmId = 1;

    public static void checkDailyAt(Context context, int hour, int minutes) {
        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);

        Intent intent = new Intent(context, MyNotificationManager.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                alarmId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        showNotifications(context);
    }

    public static void showNotifications(Context context){
        VokabeltrainerDB db = VokabeltrainerDB.getInstance(context);
        if(!db.getLernkarteienErinnerung().isEmpty()){
            showNotification(context);
        }
    }

    public static void showNotification(Context context){
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent
                .getActivity(context, 0, notificationIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.mipmap.small_icon)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setVibrate(new long[]{0, 300, 0, 300})
                .setLights(Color.WHITE, 1000, 5000)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(16, builder.build());
    }

}
