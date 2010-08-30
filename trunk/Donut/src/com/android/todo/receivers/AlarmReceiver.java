//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo.receivers;

import java.util.Calendar;

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
import android.os.Vibrator;

import com.android.todo.Alarm;
import com.android.todo.Config;
import com.android.todo.R;
import com.android.todo.TagToDoList;
import com.android.todo.Utils;
import com.android.todo.data.ToDoDB;
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

    final int ringerMode = ((AudioManager) c
        .getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
    final SharedPreferences settings = c.getSharedPreferences(
        TagToDoList.PREFS_NAME, Context.MODE_PRIVATE);

    if (settings.getBoolean(Config.ALARM_SCREEN, false)) {
      if (ringerMode != AudioManager.RINGER_MODE_SILENT
          && settings.getBoolean(Config.ALARM_VIBRATION, true)) {
        ((Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE))
            .vibrate(new long[] { 500, 500, 500, 500, 500, 500, 500, 500, 500,
                500 }, -1);
      }
      final int alarmPhase = intent.getIntExtra(Alarm.ALARM_PHASE, 0);

      if (alarmPhase < 2) {
        final PendingIntent pi = PendingIntent.getBroadcast(c,
            task.hashCode() - 1 - alarmPhase, Utils.getAlarmIntent(new Intent(c,
                AlarmReceiver.class)
                .putExtra(Alarm.ALARM_PHASE, alarmPhase + 1), task), 0);
        final AlarmManager am = (AlarmManager) c
            .getSystemService(Context.ALARM_SERVICE);
        final Calendar cal = Calendar.getInstance();
        cal.roll(Calendar.MINUTE, Alarm.SNOOZE_TIME);
        Chronos.setSingularAlarm(
            am,
            pi,
            new Time(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE),
                -1),
            new Date(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                .get(Calendar.DAY_OF_MONTH)));
      }

      c.startActivity(new Intent(c, Alarm.class)
          .setFlags(
              Intent.FLAG_ACTIVITY_NEW_TASK
                  | Intent.FLAG_ACTIVITY_NO_USER_ACTION)
          .putExtra(ToDoDB.KEY_NAME, task)
          .putExtra(Alarm.ALARM_PHASE, alarmPhase));
    } else {
      if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
        Alarm
            .soundAlarm(c, settings, (Uri) intent.getParcelableExtra(RINGTONE));
      }
      final Notification notification = new Notification(R.drawable.small_icon,
          task, System.currentTimeMillis());
      final PendingIntent contentIntent = PendingIntent.getActivity(c, 0,
          new Intent(c, TagToDoList.class), Intent.FLAG_ACTIVITY_NEW_TASK);
      notification.setLatestEventInfo(c, c.getString(R.string.alarm), task,
          contentIntent);
      notification.flags = Notification.FLAG_AUTO_CANCEL;

      if (ringerMode != AudioManager.RINGER_MODE_SILENT
          && settings.getBoolean(Config.ALARM_VIBRATION, true)) {
        notification.vibrate = (long[]) intent.getExtras().get(VIBRATION);
      }

      ((NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE))
          .notify(2, notification);
    }
  }
}
