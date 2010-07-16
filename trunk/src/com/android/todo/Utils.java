//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.android.todo.data.ToDoDB;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * A class that contains some helper functions
 */
public final class Utils {

  /**
   * This function iterates the received value unless the iterated value is
   * equal to maxValue, in which case 0 is returned. This is the behavior when
   * increment equals 1.
   * 
   * When it's -1, the behavior will be the exact opposite.
   * 
   * If maxValue is 0, meaning the array has no elements, -1 will be returned.
   * 
   * @param currentValue
   * @param maxValue
   * @return the new value
   */
  public final static int iterate(int currentValue, int maxValue, int increment) {
    int newValue = currentValue + increment;
    if (newValue * increment > (maxValue - 1) * (increment + 1) / 2) {
      newValue = (1 - increment) / 2 * (maxValue - 1);
    }
    return newValue;
  }

  /**
   * This function is used to get a file name (with prefix, hash code and
   * extension) from an original string, for example a task which wants a
   * graphical note.
   * 
   * @param original
   * @return the image file name
   */
  public final static String getImageName(String original) {
    return "note_" + original.hashCode() + ".png";
  }

  /**
   * This function is used to get an audio file name (with prefix, hash code and
   * extension) from an original string, for example a task which wants an audio
   * note. It only works on sdcard?!
   * 
   * @param original
   * @return the audio file name
   */
  public final static String getAudioName(final String original) {
    return "/sdcard/Tag-ToDo_data/audio/" + original.hashCode() + ".3gpp";
  }

  /**
   * Returns an alarm intent which will be passed to the AlarmReceiver
   * 
   * @param intent
   *          initial intent
   * @param task
   *          the name of the task as a String
   * @return the new intent (actually the same, just updated)
   */
  public final static Intent getAlarmIntent(Intent intent, String task) {
    intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
    intent.putExtra(ToDoDB.KEY_NAME, task);
    // the following notification extras don't really need to be here from a
    // design point of a view, but thinking about a possible future feature of
    // individual alarms
    intent.putExtra(AlarmReceiver.RINGTONE, Uri
        .parse("android.resource://com.android.todo/" + R.raw.beep));
    intent.putExtra(AlarmReceiver.VIBRATION, new long[] { 200, 300 });
    return intent;
  }

  /**
   * A general usage function, usable from any activity. It shows a dialog with
   * a text message, and is compatible with both landscape and portrait mode
   * because of the contained ScrollView.
   * 
   * @param tId
   *          resource id of the title string
   * @param mId
   *          resource id of the message string
   * @param c
   *          context passed to the UI constructors
   */
  public final static void showDialog(final int tId, final int mId,
      final Context c) {
    final Dialog d = new Dialog(c);
    final ScrollView sv = new ScrollView(c);
    final TextView tv = new TextView(c);
    sv.addView(tv);
    final LinearLayout ll = new LinearLayout(c);
    ll.setOrientation(1);
    tv.setTextSize(18);
    tv.setText(mId);
    final Button b = new Button(c);
    b.setText(R.string.go_back);
    b.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        d.dismiss();
      }
    });
    ll.setPadding(10, 10, 10, 0);
    ll.addView(b);
    ll.addView(sv);
    if (tId > -1) {
      d.setTitle(tId);
    } else {
      d.setTitle("");
    }
    d.setContentView(ll);
    d.setOnKeyListener(new Dialog.OnKeyListener() {
      public boolean onKey(DialogInterface di, int keyCode, KeyEvent msg) {
        if (keyCode == KeyEvent.KEYCODE_ENTER
            || keyCode == KeyEvent.KEYCODE_DEL
            || keyCode == KeyEvent.KEYCODE_SPACE) {
          b.performClick();
        }
        return true;
      }
    });
    d.show();
  }

  /**
   * A general usage function, usable from any activity.
   * 
   * @param title
   * @param message
   * @param c
   *          context passed to the UI constructors
   */
  public final static void showDueTasksNotification(String text, final Context c) {

    PendingIntent contentIntent = PendingIntent.getActivity(c, 0, new Intent(c,
        NotificationActivity.class), 0);

    Notification n = new Notification(R.drawable.small_icon, text, System
        .currentTimeMillis());
    n.setLatestEventInfo(c, c.getString(R.string.notification), c
        .getString(R.string.notification_view), contentIntent);

    NotificationManager nm = (NotificationManager) c
        .getSystemService(Context.NOTIFICATION_SERVICE);
    nm.notify(1, n);
  }

  /**
   * Copies a File to another File. They may also be folders.
   * 
   * @param source
   * @param target
   * @throws Exception
   */
  public static final void copy(File source, File target) throws Exception {

    if (source.isDirectory()) {
      if (!target.exists()) {
        target.mkdir();
      }
      String[] children = source.list();
      for (int i = 0; i < children.length; i++) {
        copy(new File(source, children[i]), new File(target, children[i]));
      }
    } else {
      InputStream in = new FileInputStream(source);
      OutputStream out = new FileOutputStream(target);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = in.read(buffer)) > 0) {
        out.write(buffer, 0, length);
      }
      in.close();
      out.close();
    }
  }

}