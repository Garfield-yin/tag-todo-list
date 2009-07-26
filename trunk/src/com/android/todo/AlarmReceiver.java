//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * This is an activity used to process status bar notification activations
 */
public final class AlarmReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    NotificationManager manager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    String task = intent.getStringExtra(ToDoListDB.KEY_NAME);
    Notification notification = new Notification(R.drawable.small_icon, task,
        System.currentTimeMillis());
    PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
        new Intent(context, TagToDoList.class), 0);
    notification.setLatestEventInfo(context, context.getString(R.string.alarm),
        task, contentIntent);
    notification.flags = Notification.FLAG_INSISTENT;
    notification.sound = (Uri) intent.getParcelableExtra("Ringtone");
    notification.vibrate = (long[]) intent.getExtras().get("vibrationPatern");
    manager.notify(2, notification);
  }
}
