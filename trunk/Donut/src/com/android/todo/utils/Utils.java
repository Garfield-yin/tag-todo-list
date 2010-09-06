//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Vector;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.todo.Audio;
import com.android.todo.Graphics;
import com.android.todo.NotificationActivity;
import com.android.todo.R;
import com.android.todo.data.ToDoDB;
import com.android.todo.receivers.AlarmReceiver;

/**
 * A class that contains some helper functions
 */
public final class Utils {

  /**
   * Adds a seekbar with UI stuff attached to it, also tied to app preferences.
   * 
   * @param ll
   *          Parent which will host the new stuff
   * @param sp
   *          SharedPreferences
   * @param key
   *          Preferences key
   * @param defaultValue
   * @param maxValue
   * @param titleStringId
   * @param descriptionStringId
   */
  public final static LinearLayout addSeekBar(final LinearLayout ll,
      final SharedPreferences sp, final String key, final int defaultValue,
      final int maxValue, final int titleStringId, final int descriptionStringId) {
    final Context c = ll.getContext();
    final TextView tv = new TextView(c);
    tv.setTextSize(16);
    tv.setText(c.getString(titleStringId).concat(": "));
    final LinearLayout localLayout = new LinearLayout(c); // reusing
    localLayout.setOrientation(LinearLayout.VERTICAL);
    final LinearLayout textLayout = new LinearLayout(c); // also
    // reusing
    textLayout.setOrientation(LinearLayout.HORIZONTAL);
    textLayout.addView(tv);
    final TextView limitTv = new TextView(c);
    limitTv.setMinimumWidth(50);
    localLayout.addView(textLayout);
    final SeekBar sLimit = new SeekBar(c);
    sLimit.setMax(maxValue);
    final int l = sp.getInt(key, defaultValue);
    sLimit.setProgress(l <= sLimit.getMax() ? l : sLimit.getMax());
    sLimit.setPadding(0, 0, 5, 0);
    limitTv.setTextSize(17);
    limitTv.setText(Integer.toString(sLimit.getProgress()));
    textLayout.addView(limitTv);
    sLimit.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
      public void onProgressChanged(SeekBar seekBar, int progress,
          boolean fromUser) {
        limitTv.setText(Integer.toString(progress));
      }

      public void onStartTrackingTouch(SeekBar seekBar) {
      }

      public void onStopTrackingTouch(SeekBar seekBar) {
        sp.edit().putInt(key, seekBar.getProgress()).commit();
      }
    });
    localLayout.addView(sLimit);
    final TextView localDescription = new TextView(c);
    localDescription.setText(descriptionStringId);
    localLayout.addView(localDescription);
    localLayout.setPadding(10, 5, 15, 5);
    ll.addView(localLayout);
    return localLayout;
  }

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
   * @param task
   * @return the image file name
   */
  public final static String getImageName(final String task,
      final boolean newWay) {
    // remove this last parameter when Android2.2 is not supported anymore
    if (!newWay) {
      return "note_" + task.hashCode() + ".png";
    }
    return Environment.getExternalStorageDirectory().getPath() + Graphics.PATH
        + task.hashCode() + ".png";
  }

  /**
   * This function is used to get an audio file name (with prefix, hash code and
   * extension) from an original string, for example a task which wants an audio
   * note. It only works on sdcard?!
   * 
   * @param task
   * @return the audio file name
   */
  public final static String getAudioName(final String task) {
    return Environment.getExternalStorageDirectory().getPath() + Audio.PATH
        + task.hashCode() + ".3gpp";
  }

  /**
   * Returns an alarm intent which will be passed to the AlarmReceiver
   * 
   * @param i
   *          initial intent
   * @param task
   *          the name of the task as a String
   * @return the new intent (actually the same, just updated)
   */
  public final static Intent getAlarmIntent(final Intent i, final String task) {
    i.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
    i.putExtra(ToDoDB.KEY_NAME, task);
    // the following notification extras don't really need to be here from a
    // design point of a view, but thinking about a possible future feature of
    // individual alarms
    i.putExtra(AlarmReceiver.RINGTONE,
        Uri.parse("android.resource://com.android.todo/" + R.raw.beep));
    i.putExtra(AlarmReceiver.VIBRATION, new long[] { 200, 300 });
    return i;
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

    Notification n = new Notification(R.drawable.small_icon, text,
        System.currentTimeMillis());
    n.setLatestEventInfo(c, c.getString(R.string.notification),
        c.getString(R.string.notification_view), contentIntent);

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

  /**
   * Used to filter for CSV files (or any other type of files for that matter)
   * 
   * @param directory
   * @param filter
   * @param recurse
   * @return
   */
  public final static File[] listFilesAsArray(File directory,
      FilenameFilter filter, boolean recurse) {
    Collection<File> files = listFiles(directory, filter, recurse);
    File[] arr = new File[files.size()];
    return files.toArray(arr);
  }

  private final static Collection<File> listFiles(File directory,
      FilenameFilter filter, boolean recurse) {
    // List of files / directories
    Vector<File> files = new Vector<File>();

    // Get files / directories in the directory
    File[] entries = directory.listFiles();

    // Go over entries
    for (File entry : entries) {
      ;

      // If there is no filter or the filter accepts the
      // file / directory, add it to the list
      if (filter == null || filter.accept(directory, entry.getName())) {
        files.add(entry);
      }

      // If the file is a directory and the recurse flag
      // is set, recurse into the directory
      if (recurse && entry.isDirectory()) {
        files.addAll(listFiles(entry, filter, recurse));
      }
    }

    // Return collection of files
    return files;
  }

}