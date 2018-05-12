package com.evolvexie.popularmovies.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.evolvexie.popularmovies.MainActivity;
import com.evolvexie.popularmovies.R;
import com.evolvexie.popularmovies.data.CommonPreferences;

public class NotificationUtils {

    public static final int FIRST_POPULAR_CHANGE_NOTIFY_INTENT_ID = 913;
    public static final int FIRST_RATED_CHANGE_NOTIFY_INTENT_ID = 914;

    public static PendingIntent getChangeContentIntent(Context context,int intentId){
        Intent startIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context,
                intentId,
                startIntent,
                PendingIntent.FLAG_IMMUTABLE);
        return pendingIntent;
    }

    public static void createAndSendFirstChangeNotification(Context context,String content,int intentId){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setColor(ContextCompat.getColor(context,R.color.colorPrimary))
                .setLargeIcon(largeIcon(context))
                .setSmallIcon(R.mipmap.app_icon)
                .setContentTitle(context.getResources().getString(R.string.notification_title_first_change))
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(getChangeContentIntent(context,intentId))
                .setAutoCancel(true);
        // 当sdk版本大于等于JELLY_BEAN时，可以设置通知优先级，并且设置后通知将会浮动式弹出通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            builder.setPriority(Notification.PRIORITY_HIGH);
        }
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(intentId,builder.build());
    }

    /**
     * 判断是否启用了通知
     * @param context
     * @return
     */
    public static boolean isNotify(Context context){
        boolean isNotify = CommonPreferences.getDefaultSharedPreferenceValue(context,
                context.getResources().getString(R.string.pref_key_notification),
                true);
        return isNotify;
    }

    public static Bitmap largeIcon(Context context){
        Resources resources = context.getResources();
        return BitmapFactory.decodeResource(resources, R.mipmap.app_icon);
    }
}
