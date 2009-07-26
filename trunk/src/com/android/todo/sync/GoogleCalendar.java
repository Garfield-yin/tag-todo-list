//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo.sync;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * This class is in charge of synchronizing the events (with due dates) with
 * Google Calendar
 */
public final class GoogleCalendar {
  private static String mAuthToken = null;
  private static String mUsername = "", mPassword = "";
  private static long mLastActivity = 0;
  private final static HostnameVerifier HOSTNAME_VERIFIER = new HostnameVerifier() {
    public boolean verify(String hostname, SSLSession session) {
      return "www.google.com".equals(hostname);
    }
  };

  /**
   * The login has to be set by someone, either in a configuration window, or in
   * the main view, on creation, etc.
   * 
   * @param username
   * @param password
   */
  public final static void setLogin(String username, String password) {
    mUsername = username;
    mPassword = password;
  }

  /**
   * Initiates the proper communications with Google to add an event in the
   * user's main calendar
   * 
   * @param title
   *          Name of the task and future event
   * @param year
   * @param month
   * @param day
   * @param hour
   *          (if hour is -1, the event will last all day)
   * @param minute
   * @return true if the event was created
   * @throws Exception
   */
  public final static boolean createEvent(String title, int year, int month,
      int day, int hour, int minute) throws Exception {

    authenticate(false);
    boolean redirect = false;
    month++; // our months are from 0 to 11
    String sessionUrl = "http://www.google.com/calendar/feeds/default/private/full";
    do {
      HttpURLConnection uc = (HttpURLConnection) new URL(sessionUrl)
          .openConnection();
      uc.setDoOutput(true);
      uc.setUseCaches(false);
      uc.setRequestMethod("POST");
      uc.setRequestProperty("Content-Type", "application/atom+xml");
      uc.setRequestProperty("Authorization", "GoogleLogin auth=" + mAuthToken);
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(uc
          .getOutputStream()));
      String s = year + "-" + (month / 10 < 1 ? "0" + month : month) + "-"
          + (day / 10 < 1 ? "0" + day : day);
      bw
          .write("<entry xmlns='http://www.w3.org/2005/Atom' xmlns:gd='http://schemas.google.com/g/2005'>\n<category scheme='http://schemas.google.com/g/2005#kind' term='http://schemas.google.com/g/2005#event'></category>\n<title type='text'>"
              + title.replace('\'', '"')
              + "</title>\n<content type='text'>event created through Tag-ToDo-List, on an Android phone</content>\n<gd:transparency value='http://schemas.google.com/g/2005#event.opaque'></gd:transparency>\n<gd:eventStatus value='http://schemas.google.com/g/2005#event.confirmed'></gd:eventStatus>\n<gd:when startTime='"
              + s
              + (hour != -1 ? "T" + (hour / 10 < 1 ? "0" + hour : hour) + ":"
                  + (minute / 10 < 1 ? "0" + minute : minute)
                  + ":00.000Z' endTime='" + s + "T"
                  + (hour / 10 < 1 ? "0" + hour : hour) + ":"
                  + (minute / 10 < 1 ? "0" + minute : minute) + ":00.000Z" : "")
              + "'></gd:when>\n</entry>");
      bw.flush();
      bw.close();
      if (!(redirect) && uc.getResponseCode() == 302) { // REDIRECT
        redirect = true;
        sessionUrl = uc.getHeaderField("Location");
      } else {
        redirect = false;
        if (uc.getResponseCode() == HttpURLConnection.HTTP_CREATED) {
          return true;
        }
      }
    } while (redirect);
    return false;
  }

  /**
   * Authentication in the Google Calendar service through HTTPS
   * 
   * @param force
   *          - if true, it forces a re-authentication, even if the present
   *          session isn't timeout
   * @return true if authentication succeeds
   * @throws Exception
   */
  public final static boolean authenticate(boolean force) throws Exception {

    long millis = System.currentTimeMillis();
    if (!(force) && millis - mLastActivity < 1800000) {
      mLastActivity = millis;
      return true;
    } else {
      mLastActivity = millis;
    }

    HttpsURLConnection uc = (HttpsURLConnection) new URL(
        "https://www.google.com/accounts/ClientLogin").openConnection();
    uc.setHostnameVerifier(HOSTNAME_VERIFIER);
    uc.setDoOutput(true);
    uc.setRequestMethod("POST");
    uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    uc.setUseCaches(false);
    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(uc
        .getOutputStream()));
    bw.write(URLEncoder.encode("Email", "UTF-8") + "="
        + URLEncoder.encode(mUsername, "UTF-8") + "&"
        + URLEncoder.encode("Passwd", "UTF-8") + "="
        + URLEncoder.encode(mPassword, "UTF-8") + "&"
        + URLEncoder.encode("source", "UTF-8") + "="
        + URLEncoder.encode("Tag-ToDo-List", "UTF-8") + "&"
        + URLEncoder.encode("service", "UTF-8") + "="
        + URLEncoder.encode("cl", "UTF-8"));
    bw.flush();
    bw.close();
    BufferedReader in = new BufferedReader(new InputStreamReader(uc
        .getInputStream()));
    if (uc.getResponseCode() == HttpsURLConnection.HTTP_FORBIDDEN) {
      return false;
    }
    // only the 3rd parameter (Auth) is of interest
    in.readLine();
    in.readLine();
    mAuthToken = in.readLine().substring(5);
    in.close();
    return true;
  }
}