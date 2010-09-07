package com.android.todo.olympus;

import android.os.Handler;
import android.view.View;

import com.android.todo.TagToDoList;

/**
 * Zeus, the god of thunder. Instead of bugging users with explicit messages
 * about app features, we can remind them in a subtle way that they're there.
 * For example, a very short moment of invisibility might attract the user's
 * attention and determine him to take action on the respective UI widget.
 */
public final class Zeus {
  /**
   * The time the app will wait before reminding the user
   */
  private final static long INITIAL_DELAY_TIME = 5000;

  /**
   * The duration of the view's invisibility
   */
  private final static long THUNDER_TIME = 200;

  public final static void remind(final View v, final String key) {
    if (TagToDoList.sPref.getInt(key, 0) < 1) {
      final Handler h = new Handler();
      h.postDelayed(new Runnable() {
        public void run() {
          try {
            v.setVisibility(View.INVISIBLE);
            h.postDelayed(new Runnable() {
              public void run() {
                try {
                  v.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                }
              }
            }, THUNDER_TIME);
          } catch (Exception e) {
          }
        }
      }, INITIAL_DELAY_TIME);
    }
  }
}
