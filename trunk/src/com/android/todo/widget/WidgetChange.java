package com.android.todo.widget;

import java.util.Timer;
import java.util.TimerTask;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.android.todo.EditScreen;
import com.android.todo.R;
import com.android.todo.TagToDoList;
import com.android.todo.ToDoDB;
import com.android.todo.Utils;

/**
 * This is an activity used process user actions on the widget
 */
public final class WidgetChange extends BroadcastReceiver {
  private static int sTag = 0, sTask = 0;
  private static Cursor sTagCursor;
  private static Cursor sTaskCursor;
  private static ToDoDB sDbHelper;
  private static Timer sTimer;
  private static SharedPreferences sSettings;

  private final static String CICLE_ON = "widgetCicle";
  public final static String WIDGET_INITIATED = "widgetInitiated";

  public final static void refresh(final RemoteViews rv, final Context c) {
    // initializing logic
    // sTag = 0;
    sTask = 0;
    if (sDbHelper != null) {
      sDbHelper.close();
    }
    sDbHelper = new ToDoDB(c).open();
    sTagCursor = sDbHelper.getAllTags();
    if (sTimer != null) {
      sTimer.cancel();
      sTimer = null;
    }

    // initializing UI
    sTagCursor.moveToPosition(sTag);
    final String tag = sTagCursor.getString(sTagCursor
        .getColumnIndex(ToDoDB.KEY_NAME));
    rv.setTextViewText(R.id.tagItem, tag);
    sTaskCursor = sDbHelper.getUncheckedEntries(tag);
    if (sTaskCursor.getCount() > 0) {
      sTaskCursor.moveToPosition(sTask);
      rv.setTextViewText(R.id.widgetItem, sTaskCursor.getString(sTaskCursor
          .getColumnIndex(ToDoDB.KEY_NAME)));
    } else {
      rv.setTextViewText(R.id.widgetItem, c.getString(R.string.no_tasks));
    }
    sSettings = c.getSharedPreferences(TagToDoList.PREFS_NAME, 0);
    if (sSettings.getBoolean(CICLE_ON, false)) {
      WidgetChange.cicle(c);
    }
  }

  @Override
  public void onReceive(final Context c, final Intent intent) {
    final RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget);
    switch (intent.getExtras().getInt(ToDoDB.KEY_NAME)) {
    case R.id.nextTaskButton:
      if (sTaskCursor.getCount() > 0) {
        sTaskCursor.moveToPosition(sTask = Utils.iterate(sTask, sTaskCursor
            .getCount(), 1));
        rv.setTextViewText(R.id.widgetItem, sTaskCursor.getString(sTaskCursor
            .getColumnIndex(ToDoDB.KEY_NAME)));
      }
      break;
    case R.id.nextTagButton:
      sTagCursor.moveToPosition(sTag = Utils.iterate(sTag, sTagCursor
          .getCount(), 1));
      final String tag = sTagCursor.getString(sTagCursor
          .getColumnIndex(ToDoDB.KEY_NAME));
      rv.setTextViewText(R.id.tagItem, tag);
      sTaskCursor = sDbHelper.getUncheckedEntries(tag);
      if (sTaskCursor.getCount() > 0) {
        sTaskCursor.moveToFirst();
        rv.setTextViewText(R.id.widgetItem, sTaskCursor.getString(sTaskCursor
            .getColumnIndex(ToDoDB.KEY_NAME)));
      } else {
        rv.setTextViewText(R.id.widgetItem, c.getString(R.string.no_tasks));
      }
      sTask = 0;
      break;
    case R.id.cicleButton:
      WidgetChange.cicle(c);
      return;
    case R.id.addTaskButton:
      c.startActivity(new Intent(c, EditScreen.class).putExtra(
          WIDGET_INITIATED, true).setAction(
          TagToDoList.ACTIVITY_CREATE_ENTRY + "").putExtra(ToDoDB.KEY_NAME,
          sTagCursor.getString(sTagCursor.getColumnIndex(ToDoDB.KEY_NAME)))
          .putExtra(ToDoDB.KEY_SUPERTASK, "").addFlags(
              Intent.FLAG_ACTIVITY_NEW_TASK));
      break;
    case R.id.checkButton:
      if (sTaskCursor.getCount() > 0) {
        sDbHelper.updateEntry(sTaskCursor.getString(sTaskCursor
            .getColumnIndex(ToDoDB.KEY_NAME)), true);
        WidgetChange.refresh(rv, c);
      }
      break;
    }
    AppWidgetManager.getInstance(c).updateAppWidget(
        new ComponentName(c, TagToDoWidget.class), rv);
    if (sTimer != null) {
      sTimer.cancel();
      sTimer = null;
    }
  }

  /**
   * Cicles through tasks and tags automatically
   * 
   * @param c
   *          = context
   */
  private final static void cicle(final Context c) {
    final SharedPreferences.Editor e = sSettings.edit();
    if (sTimer != null) {
      sTimer.cancel();
      sTimer = null;
      e.putBoolean(CICLE_ON, false);
      e.commit();
      return;
    }

    e.putBoolean(CICLE_ON, true);
    e.commit();

    /*
     * A TimerTask that will be given to a timer. It's responsible with
     * refreshing the seekbar.
     */
    final class CicleTask extends TimerTask {
      // private Context mContext;

      public CicleTask() {
        // mContext=c;
      }

      public void run() {
        final RemoteViews rv = new RemoteViews(c.getPackageName(),
            R.layout.widget);
        sTask = Utils.iterate(sTask, sTaskCursor.getCount(), 1);
        if (sTask > 0) {
          sTaskCursor.moveToPosition(sTask);
        } else {
          sTagCursor.moveToPosition(sTag = Utils.iterate(sTag, sTagCursor
              .getCount(), 1));
          final String tag = sTagCursor.getString(sTagCursor
              .getColumnIndex(ToDoDB.KEY_NAME));
          rv.setTextViewText(R.id.tagItem, tag);
          sTaskCursor = sDbHelper.getUncheckedEntries(tag);
          if (sTaskCursor.getCount() > 0) {
            sTaskCursor.moveToFirst();
            rv.setTextViewText(R.id.widgetItem, sTaskCursor
                .getString(sTaskCursor.getColumnIndex(ToDoDB.KEY_NAME)));
          } else {
            rv.setTextViewText(R.id.widgetItem, c.getString(R.string.no_tasks));
          }
          sTask = 0;
        }
        rv.setTextViewText(R.id.widgetItem, sTaskCursor.getString(sTaskCursor
            .getColumnIndex(ToDoDB.KEY_NAME)));
        AppWidgetManager.getInstance(c).updateAppWidget(
            new ComponentName(c, TagToDoWidget.class), rv);
      }
    }
    (sTimer = new Timer()).schedule(new CicleTask(), 2000, 5000);
  }
}
