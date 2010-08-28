package com.android.todo.olympus;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * This helper class deals with music, media and stuff like that.
 */
public final class Apollo {
  private static MediaPlayer sPlayer;
  private static int sCounter;
  private static Timer sAudioTimer = null;

  /**
   * Plays the audio file at the given URI for several times.
   * 
   * @param times
   * @param c
   * @param uri
   */
  public final static void play(final int times, final Context c, final Uri uri) {
    sCounter = 0;
    sPlayer = new MediaPlayer();
    try {
      sPlayer.setDataSource(c, uri);
      sPlayer.prepare();
    } catch (Exception e) {
      // if we can't play sound, no point in doing anything else since the
      // user already has a notification onscreen
      return;
    }
    sPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
      public void onCompletion(MediaPlayer mp) {
        if (sCounter++ < times) {
          mp.start();
        } else {
          mp.release();
        }
      }
    });
    sPlayer.start();
  }

  /**
   * Plays the audio file at the given URI only one time.
   * 
   * @param c
   * @param uri
   * @param seconds
   *          Maximum play time
   */
  public final static void play(final Context c, final Uri uri,
      final int seconds) {
    sPlayer = new MediaPlayer();
    try {
      sPlayer.setDataSource(c, uri);
      sPlayer.prepare();
    } catch (Exception e) {
      // if we can't play sound, no point in doing anything else since the
      // user already has a notification onscreen
      return;
    }
    if (seconds > 0) {
      sPlayer.start();
      if (sAudioTimer != null) {
        sAudioTimer.cancel();
      }
      (sAudioTimer = new Timer()).schedule(new TimerTask() {
        public void run() {
          try {
            sPlayer.stop();
            sPlayer.release();
          } catch (Exception e) {
          }
        }
      }, seconds * 1000);
    }
  }
  
  /**
   * Shuts everything up
   */
  public final static void shutUp(){
    try {
      sPlayer.stop();
      sPlayer.release();
    } catch (Exception e) {
    }
  }

}
