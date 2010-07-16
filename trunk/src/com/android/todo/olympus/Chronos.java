package com.android.todo.olympus;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.android.todo.R;

/**
 * This helper class deals with times, dates and stuff like that.
 */
public final class Chronos {
  public static final int[] DAYS = { R.string.monday, R.string.tuesday,
    R.string.wednesday, R.string.thursday, R.string.friday,
    R.string.saturday, R.string.sunday };
  
  private static GregorianCalendar mCal;

  /**
   * Refreshes the calendar.
   */
  public final static void refresh() {
    mCal = new GregorianCalendar();
  }

  /**
   * Gets the date.
   * 
   * @return an int (1-31)
   */
  public final static int getDate() {
    return mCal.get(GregorianCalendar.DATE);
  }

  /**
   * Gets the month.
   * 
   * @return an int (0-11)
   */
  public final static int getMonth() {
    return mCal.get(GregorianCalendar.MONTH);
  }

  /**
   * Gets the year.
   * 
   * @return an int (the year)
   */
  public final static int getYear() {
    return mCal.get(GregorianCalendar.YEAR);
  }

  /**
   * Decodes an encoded date.
   * 
   * @param encodedDate
   *          a date encoded as an int, like this: (year*12+month)*31+day
   * @param months
   *          a list of months, as a String, in the user's language, separated
   *          by space
   * @return the decoded date as a String
   */
  public final static String decodeDate(final int encodedDate,
      final String months) {
    return months.split(" ")[encodedDate / 31 % 12] + " " + (encodedDate % 31)
        + ", " + (encodedDate / 372);
  }

  /**
   * Decodes an encoded time.
   * 
   * @param encodedTime
   *          time encoded like this: 60*hours+minutes
   * @return the decoded time as a String
   */
  public final static String decodeTime(final int encodedTime) {
    final int hour = encodedTime / 60;
    final int minute = encodedTime % 60;
    return (hour < 10 ? "0" : "") + hour + ":" + (minute < 10 ? "0" : "")
        + minute;
  }

  /**
   * Returns the time millis based on the define date and time
   * 
   * @param encodedTime
   * @param encodedDate
   *          if negative, the function will return the first time that fits
   *          (today or tomorrow), relative to current time
   * @return the millis, or -1 if the alarm trigger time is in the past
   */
  public final static long getTimeMillis(int encodedTime, int encodedDate,
      int dayOfWeek) {
    Calendar c = Calendar.getInstance();
    final int encodedTimeDif = encodedTime - c.get(Calendar.HOUR_OF_DAY) * 60
        - c.get(Calendar.MINUTE);
    if (encodedDate > 0) {
      final int encodedDateDif = encodedDate - 372 * c.get(Calendar.YEAR) - 31
          * c.get(Calendar.MONTH) - c.get(Calendar.DAY_OF_MONTH);
      if (encodedDateDif < 0 || (encodedDateDif == 0 && encodedTimeDif < -1)) {
        return -1;
      }
      c.set(encodedDate / 372, encodedDate / 31 % 12, encodedDate % 31,
          encodedTime / 60, encodedTime % 60);
      return c.getTimeInMillis();
    } else {
      if (dayOfWeek == -1) {
        if (encodedTimeDif > 0) {// today
          return System.currentTimeMillis() + encodedTimeDif * 60000;
        } else {// tomorrow
          return System.currentTimeMillis() + 86400000 + encodedTimeDif * 60000;
        }
      } else {
        int d = c.get(Calendar.DAY_OF_WEEK) - 2;
        if (d < 0) {
          d += 7;
        }
        while (d != dayOfWeek) {
          c.setTimeInMillis(c.getTimeInMillis() + 86400000);
          d = c.get(Calendar.DAY_OF_WEEK) - 2;
          if (d < 0) {
            d += 7;
          }
        }
        if (encodedTimeDif > 0) {// in the past
          return c.getTimeInMillis() + 60000L * encodedTimeDif;
        } else {// next week
          return 604800000L + c.getTimeInMillis() + 60000L * encodedTimeDif;
        }
      }
    }
  }

}
