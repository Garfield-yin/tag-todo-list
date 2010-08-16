// class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.android.todo.R;
import com.android.todo.ToDo;
import com.android.todo.data.ToDoDB;

/**
 * Class that takes care of the Widget behavior
 */
public final class TagToDoWidget extends AppWidgetProvider {

  @Override
  public void onUpdate(Context c, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {
    TagToDoWidget.onUpdate(c.getApplicationContext(), appWidgetManager);
  }

  public final static void onUpdate(Context c, AppWidgetManager appWidgetManager) {
    RemoteViews rv = new RemoteViews(c.getPackageName(), R.layout.widget);
    rv.setOnClickPendingIntent(R.id.widgetLogo, PendingIntent.getActivity(c, 0,
        new Intent(c, ToDo.class), Intent.FLAG_ACTIVITY_NEW_TASK));
    rv.setOnClickPendingIntent(R.id.nextTagButton, PendingIntent.getBroadcast(
        c, 1, new Intent(c, WidgetChange.class).putExtra(ToDoDB.KEY_NAME,
            R.id.nextTagButton), 0));
    rv.setOnClickPendingIntent(R.id.nextTaskButton, PendingIntent.getBroadcast(
        c, 2, new Intent(c, WidgetChange.class).putExtra(ToDoDB.KEY_NAME,
            R.id.nextTaskButton), 0));
    rv.setOnClickPendingIntent(R.id.addTaskButton, PendingIntent.getBroadcast(
        c, 3, new Intent(c, WidgetChange.class).putExtra(ToDoDB.KEY_NAME,
            R.id.addTaskButton), 0));
    rv.setOnClickPendingIntent(R.id.checkButton, PendingIntent.getBroadcast(c,
        4, new Intent(c, WidgetChange.class).putExtra(ToDoDB.KEY_NAME,
            R.id.checkButton), 0));
    rv.setOnClickPendingIntent(R.id.cicleButton, PendingIntent.getBroadcast(c,
        5, new Intent(c, WidgetChange.class).putExtra(ToDoDB.KEY_NAME,
            R.id.cicleButton), 0));
    rv.setOnClickPendingIntent(R.id.widgetItem, PendingIntent.getBroadcast(c,
        6, new Intent(c, WidgetChange.class).putExtra(ToDoDB.KEY_NAME,
            R.id.widgetItem), 0));
    WidgetChange.refresh(rv, c);
    appWidgetManager.updateAppWidget(new ComponentName(c, TagToDoWidget.class),
        rv);

  }
}
