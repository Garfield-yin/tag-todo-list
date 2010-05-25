//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.android.todo.data.ToDoDB;

/**
 * This is an activity used to process status bar notification activations
 */
public final class AlarmReceiver extends BroadcastReceiver {
  private static MediaPlayer sPlayer;
  private static int sCounter;

  @Override
  public void onReceive(Context context, Intent intent) {
    final NotificationManager manager = (NotificationManager) context
        .getSystemService(Context.NOTIFICATION_SERVICE);
    final String task = intent.getStringExtra(ToDoDB.KEY_NAME);
    final Notification notification = new Notification(R.drawable.small_icon,
        task, System.currentTimeMillis());
    final PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
        new Intent(context, TagToDoList.class), Intent.FLAG_ACTIVITY_NEW_TASK);
    notification.setLatestEventInfo(context, context.getString(R.string.alarm),
        task, contentIntent);
    notification.flags = Notification.FLAG_AUTO_CANCEL;

    /**
     * Makes the alarm sound separately. We only want it to ring a few times, so
     * that it doesn't drain the user's battery in case he doesn't have the
     * phone.
     */
    final int ringerMode = ((AudioManager) context
        .getSystemService(Context.AUDIO_SERVICE)).getRingerMode();
    if (ringerMode == AudioManager.RINGER_MODE_NORMAL) {
      sCounter = 0;
      sPlayer = new MediaPlayer();
      try {
        sPlayer.setDataSource(context, (Uri) intent
            .getParcelableExtra("Ringtone"));
        sPlayer.prepare();
      } catch (Exception e) {
        // if we can't play sound, no point in doing anything else since the
        // user
        // already has a notification onscreen
        return;
      }
      sPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
        public void onCompletion(MediaPlayer mp) {
          if (sCounter++ < 28) {
            mp.start();
          } else {
            mp.release();
          }
        }
      });
      sPlayer.start();
    }

    if (ringerMode != AudioManager.RINGER_MODE_SILENT) {
      notification.vibrate = (long[]) intent.getExtras().get("vibrationPatern");
    }

    manager.notify(2, notification);
  }
}
