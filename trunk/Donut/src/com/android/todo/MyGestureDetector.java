package com.android.todo;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public final class MyGestureDetector extends SimpleOnGestureListener {
  
  /**
   * If +1 the direction is 'right', if -1 it's 'left'
   */
  private static int sDirection;
  
  private static final int SWIPE_MIN_DISTANCE = 120;
  private static final int SWIPE_THRESHOLD_VELOCITY = 210;
  
  /**
   * Gets the direction of the screen movement. @see sDirection
   * @return +1 or -1
   */
  public final static int getDirection(){
    return sDirection;
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
      float velocityY) {
    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
      sDirection=1;
      return true;
    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
      sDirection=-1;
      return true;
    }
    return false;
  }
}
