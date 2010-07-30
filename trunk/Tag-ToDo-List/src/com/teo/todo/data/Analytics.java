package com.teo.todo.data;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * Container class for Analytics parameters
 * 
 * @author Teo
 * 
 */
public final class Analytics {
  /**
   * Represents the Analytics UA code for http://todo.android.com (fictional
   * site).
   */
  public static final String UA_CODE = "UA-298650-15";
  /**
   * Represents the last synchronized month. It's compared with the current
   * month to determine if a sync is necessary.
   */
  public static final String LAST_SYNCHRONIZED_MONTH = "lastSyncedMonth";
  /**
   * Represents the main to-do list view, as a false web page which will
   * register a pageview in Analytics
   */
  public static final String VIEW_MAIN = "/mainView";

  /**
   * Represents a press action.
   */
  public static final String PRESS = "PRESS";
  /**
   * Represents an attribute which is used to described various actions (e.g. we
   * know the action is triggered by a finger and not from code).
   */
  public static final String INTERFACE = "INTERFACE";
  /**
   * Represents the occurence of an exception.
   */
  public static final String EXCEPTION = "EXCEPTION";
  
  public static GoogleAnalyticsTracker sTracker = null;
}
