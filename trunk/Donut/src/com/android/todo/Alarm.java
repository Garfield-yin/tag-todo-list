//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.android.todo.data.ToDoDB;
import com.android.todo.olympus.Apollo;

/**
 * This is an Alarm screen (of the Snooze/Dismiss type).
 */
public final class Alarm extends Activity {
  private static int SNOOZE_TIME = 20000; // 5 minutes
  private static String sTask;
  private static Timer sTimer1 = null, sTimer2 = null;
  private static SharedPreferences sPref;
  private static int sSnoozeCount;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    sSnoozeCount = 0;
    if (savedInstanceState != null) {
      sTask = savedInstanceState.getString(ToDoDB.KEY_NAME);
    } else {
      sTask = null;
    }
    if (sTask == null) {
      final Bundle extras = getIntent().getExtras();
      if (extras != null) {
        sTask = extras.getString(ToDoDB.KEY_NAME);
      } else {
        sTask = null;
      }
    }
    final LinearLayout ll = new LinearLayout(this);
    ll.setOrientation(LinearLayout.VERTICAL);

    final TextView tv = new TextView(this);
    tv.setText(sTask);
    tv.setPadding(0, 10, 0, 10);
    tv.setGravity(Gravity.CENTER_HORIZONTAL);
    ll.addView(tv);

    final Button b = new Button(this);
    b.setText(R.string.alarm_dismiss);
    b.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (sTimer1 != null) {
          sTimer1.cancel();
          sTimer1 = null;
        }
        if (sTimer2 != null) {
          sTimer2.cancel();
          sTimer1 = null;
        }
        finish();
      }
    });
    ll.addView(b);
    final Button snoozeButton = new Button(this);
    snoozeButton.setText(R.string.alarm_snooze);
    final LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT);
    lp.weight = 1;
    snoozeButton.setLayoutParams(lp);
    snoozeButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Apollo.shutUp();
        sSnoozeCount++;
        ll.removeView(snoozeButton);
      }
    });
    ll.addView(snoozeButton);
    setContentView(ll);
    unlockScreen();

    if (sTimer1 != null) {
      sTimer1.cancel();
    }
    (sTimer1 = new Timer()).schedule(new TimerTask() {
      public void run() {
        unlockScreen();
        if (sPref == null) {
          sPref = getSharedPreferences(TagToDoList.PREFS_NAME,
              Context.MODE_PRIVATE);
        }
        Alarm.soundAlarm(Alarm.this, sPref,
            Uri.parse("android.resource://com.android.todo/" + R.raw.beep));

        // setting second and last alarm (you can only snooze 2 times!)
        (sTimer2 = new Timer()).schedule(new TimerTask() {
          public void run() {
            unlockScreen();
            Alarm.soundAlarm(Alarm.this, sPref,
                Uri.parse("android.resource://com.android.todo/" + R.raw.beep));
            TextView tv = new TextView(Alarm.this);
            tv.setText(R.string.alarm_last_message);
            ll.addView(tv);
          }
        }, SNOOZE_TIME);
      }
    }, SNOOZE_TIME);
  }

  /**
   * Sounds the alarm. (Which one? Depends on user preferences)
   * 
   * @param c
   * @param settings
   * @param ringtone
   *          Uri of the default ring
   */
  public final static void soundAlarm(final Context c,
      final SharedPreferences settings, final Uri ringtone) {
    if (settings.getBoolean(Config.CUSTOM_ALARM, false)) {
      final String uriString = settings.getString(Config.ALARM_URI, null);
      if (uriString != null) {
        Apollo.play(c, Uri.parse(uriString),
            settings.getInt(Config.ALARM_DURATION, 20));
      } else {
        Apollo.play(28, c, ringtone);
      }
    } else {
      Apollo.play(28, c, ringtone);
    }
  }

  /**
   * Unlocks the screen
   */
  private final void unlockScreen() {
    ((KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE))
        .newKeyguardLock(KEYGUARD_SERVICE).disableKeyguard();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ToDoDB.KEY_NAME, sTask);
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }
}
