//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.android.todo.data.ADB;
import com.android.todo.data.BootDB;
import com.android.todo.olympus.Chronos;
import com.android.todo.olympus.Chronos.Date;
import com.android.todo.olympus.Chronos.Time;
import com.android.todo.utils.Utils;

/**
 * This is an activity used to process status bar notification activations
 */
public final class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      final Context c = context.getApplicationContext();
      final BootDB dbHelper = new BootDB(c).open();
      BootReceiver.setOldAlarms(c, dbHelper);
      dbHelper.close();
    } catch (Exception e) {
    }
  }

  public final static synchronized void setOldAlarms(final Context context,
      final ADB dbHelper) {
    final Cursor c = dbHelper.getUncheckedEntries();
    if (c.getCount() <= 0) {
      c.close();
      return;
    }
    final int name = c.getColumnIndex(BootDB.KEY_NAME);
    c.moveToFirst();
    do {
      final String task = c.getString(name);
      if (dbHelper.isDueTimeSet(task)) {
        final PendingIntent pi = PendingIntent.getBroadcast(context, task
            .hashCode(), Utils.getAlarmIntent(new Intent(context,
            AlarmReceiver.class), task), 0);
        final AlarmManager am = (AlarmManager) context
            .getSystemService(Context.ALARM_SERVICE);
        final Time t = new Time(dbHelper.getDueTime(task),
            dbHelper.getDueDayOfWeek(task));
        final Date d = new Date(dbHelper.getDueDate(task));
        if (dbHelper.isDueDateSet(task)) {
          // single occurence
          Chronos.setSingularAlarm(am, pi, t, d);
        } else {// daily or weekly
          Chronos.setRepeatingAlarm(am, pi, t, d);
        }
      }
    } while (c.moveToNext());
    c.close();
  }
}