//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo.receivers;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.net.Uri;

import com.android.todo.Config;
import com.android.todo.R;
import com.android.todo.TagToDoList;
import com.android.todo.Utils;
import com.android.todo.data.ToDoDB;
import com.android.todo.olympus.Apollo;
import com.android.todo.olympus.Chronos;
import com.android.todo.olympus.Chronos.Date;
import com.android.todo.olympus.Chronos.Time;

/**
 * This is an activity used to process status bar notification activations
 */
public final class AlarmReceiver extends BroadcastReceiver {
  public final static String RINGTONE = "Ringtone";
  public final static String VIBRATION = "vibrationPatern";

  @Override
  public void onReceive(Context c, Intent intent) {
    final NotificationManager manager = (NotificationManager) c
        .getSystemService(Context.NOTIFICATION_SERVICE);
    final String task = intent.getStringExtra(ToDoDB.KEY_NAME);

    // checking if this alarm needs to be set again (e.g. monthly)
    final BootDB dbHelper = new BootDB(c.getApplicationContext()).open();
    if (!dbHelper.isTask(task)) {
      dbHelper.close();
      return;
    }
    final Date d = new Date(dbHelper.getDueDate(task));
    if (d.isMonthly()) {
      final Context ctx = c.getApplicationContext();
      final PendingIntent pi = PendingIntent.getBroadcast(ctx, task.hashCode(),
          Utils.getAlarmIntent(new Intent(ctx, AlarmReceiver.class), task), 0);
      final AlarmManager am = (AlarmManager) ctx
          .getSystemService(Context.ALARM_SERVICE);
      Chronos.setRepeatingAlarm(am, pi,
          new Time(dbHelper.getDueTime(task), -1), d);
    }
    dbHelper.close();

    final Notification notification = new Notification(R.drawable.small_icon,
        task, System.currentTimeMillis());
    final PendingIntent contentIntent = PendingIntent.getActivity(c, 0,
        new Intent(c, TagToDoList.class), Intent.FLAG_ACTIVITY_NEW_TASK);
    notification.setLatestEventInfo(c, c.getString(R.string.alarm), task,
        contentIntent);
    notification.flags = Notification.FLAG_AUTO_CANCEL;

    /**
     * Makes the alarm sound separately. We only want it to ring a few times, so
     * that it doesn't drain the user's battery in case he doesn't have the
     * phone.
     */
    final int ringerMode = ((AudioManager) c
        .getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
    final SharedPreferences settings = c.getSharedPreferences(
        TagToDoList.PREFS_NAME, Context.MODE_PRIVATE);
    if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
      final boolean b = settings.getBoolean(Config.CUSTOM_ALARM, false);
      if (b) {
        final String uriString = settings.getString(Config.ALARM_URI,
            null);
        if (uriString != null) {
          Apollo.play(c, Uri.parse(uriString));
        } else {
          Apollo.play(28, c, (Uri) intent.getParcelableExtra(RINGTONE));
        }
      } else {
        Apollo.play(28, c, (Uri) intent.getParcelableExtra(RINGTONE));
      }
    }

    if (ringerMode != AudioManager.RINGER_MODE_SILENT
        && settings.getBoolean(Config.ALARM_VIBRATION, true)) {
      notification.vibrate = (long[]) intent.getExtras().get(VIBRATION);
    }

    manager.notify(2, notification);
  }
}
