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
import com.android.todo.TagToDoList;
import com.android.todo.data.ToDoDB;

/**
 * Class that takes care of the Widget behavior
 */
public final class TagToDoWidget extends AppWidgetProvider {

  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {
    TagToDoWidget.onUpdate(context, appWidgetManager);

  }

  public final static void onUpdate(Context context,
      AppWidgetManager appWidgetManager) {
    RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget);
    rv.setOnClickPendingIntent(R.id.widgetLogo, PendingIntent.getActivity(
        context, 0, new Intent(context, TagToDoList.class),
        Intent.FLAG_ACTIVITY_NEW_TASK));
    rv.setOnClickPendingIntent(R.id.nextTagButton, PendingIntent.getBroadcast(
        context, 1, new Intent(context, WidgetChange.class).putExtra(
            ToDoDB.KEY_NAME, R.id.nextTagButton), 0));
    rv.setOnClickPendingIntent(R.id.nextTaskButton, PendingIntent.getBroadcast(
        context, 2, new Intent(context, WidgetChange.class).putExtra(
            ToDoDB.KEY_NAME, R.id.nextTaskButton), 0));
    rv.setOnClickPendingIntent(R.id.addTaskButton, PendingIntent.getBroadcast(
        context, 3, new Intent(context, WidgetChange.class).putExtra(
            ToDoDB.KEY_NAME, R.id.addTaskButton), 0));
    rv.setOnClickPendingIntent(R.id.checkButton, PendingIntent.getBroadcast(
        context, 4, new Intent(context, WidgetChange.class).putExtra(
            ToDoDB.KEY_NAME, R.id.checkButton), 0));
    rv.setOnClickPendingIntent(R.id.cicleButton, PendingIntent.getBroadcast(
        context, 5, new Intent(context, WidgetChange.class).putExtra(
            ToDoDB.KEY_NAME, R.id.cicleButton), 0));
    WidgetChange.refresh(rv, context);
    appWidgetManager.updateAppWidget(new ComponentName(context,
        TagToDoWidget.class), rv);

  }
}
