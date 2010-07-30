package com.teo.todo.olympus;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.teo.todo.R;

import android.app.AlarmManager;
import android.app.PendingIntent;


/**
 * This helper class deals with times, dates and stuff like that.
 */
public final class Chronos {

  public static final int[] DAYS = { R.string.monday, R.string.tuesday,
      R.string.wednesday, R.string.thursday, R.string.friday,
      R.string.saturday, R.string.sunday };

  public static class Time {
    private int mHour;
    private int mMinute;
    private Integer mDayOfWeek;
    private int mEncodedTime;

    public Time(final int encodedTime, final int dayOfWeek) {
      mHour = encodedTime / 60;
      mMinute = encodedTime % 60;
      mDayOfWeek = dayOfWeek;
      mEncodedTime = encodedTime;
    }

    public Time(final int hour, final int minute, final int dayOfWeek) {
      mHour = hour;
      mMinute = minute;
      mDayOfWeek = dayOfWeek;
      mEncodedTime = 60 * hour + minute;
    }

    public final int getHour() {
      return mHour;
    }

    public final int getMinute() {
      return mMinute;
    }

    public final int getDayOfWeek() {
      return mDayOfWeek;
    }

    public final int getEncodedTime() {
      return mEncodedTime;
    }

    public final boolean isWeekly() {
      return mDayOfWeek != null && mDayOfWeek > -1;
    }
  }

  public static class Date {
    private int mDayOfMonth;
    private int mMonth;
    private int mYear;
    private int mEncodedDate;

    public Date(final int encodedDate) {
      mDayOfMonth = encodedDate % 31;
      mMonth = encodedDate / 31 % 12;
      mYear = encodedDate / 372;
      mEncodedDate = encodedDate;
    }

    public final int getDay() {
      return mDayOfMonth;
    }

    public final int getMonth() {
      return mMonth;
    }

    public final int getYear() {
      return mYear;
    }

    public final int getEncodedDate() {
      return mEncodedDate;
    }
  }

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
   * Returns the time millis based on the defined date and time
   * 
   * @param encodedTime
   * @param encodedDate
   *          if negative, the function will return the first time that fits
   *          (today or tomorrow), relative to current time
   * @return the millis, or -1 if the alarm trigger time is in the past
   */
  public final static long getTimeMillis(final Time t, final Date d) {
    final int encodedTime = t.getEncodedTime();
    Calendar c = Calendar.getInstance();
    final int encodedTimeDif = encodedTime - c.get(Calendar.HOUR_OF_DAY) * 60
        - c.get(Calendar.MINUTE);
    if (d != null) {
      final int encodedDate = d.getEncodedDate();
      final int encodedDateDif = encodedDate - 372 * c.get(Calendar.YEAR) - 31
          * c.get(Calendar.MONTH) - c.get(Calendar.DAY_OF_MONTH);
      if (encodedDateDif < 0 || (encodedDateDif == 0 && encodedTimeDif < -1)) {
        return -1;
      }
      c.set(d.getYear(), d.getMonth(), d.getDay(), t.getHour(), t.getMinute());
      return c.getTimeInMillis();
    } else {
      if (t.getDayOfWeek() == -1) {
        if (encodedTimeDif > 0) {// today
          return System.currentTimeMillis() + encodedTimeDif * 60000;
        } else {// tomorrow
          return System.currentTimeMillis() + 86400000 + encodedTimeDif * 60000;
        }
      } else {
        int da = c.get(Calendar.DAY_OF_WEEK) - 2;
        if (da < 0) {
          da += 7;
        }
        while (da != t.getDayOfWeek()) {
          c.setTimeInMillis(c.getTimeInMillis() + 86400000);
          da = c.get(Calendar.DAY_OF_WEEK) - 2;
          if (da < 0) {
            da += 7;
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

  public final static void setSingularAlarm(final AlarmManager am,
      final PendingIntent pi, final Time t, final Date d) {
    am.set(AlarmManager.RTC_WAKEUP, Chronos.getTimeMillis(t, d), pi);
  }

  public final static void setRepeatingAlarm(final AlarmManager am,
      final PendingIntent pi, final Time t, final Date d) {
    am.setRepeating(AlarmManager.RTC_WAKEUP, Chronos.getTimeMillis(t, d),
        86400000L * (t.isWeekly() ? 7 : 1), pi);
  }

}
