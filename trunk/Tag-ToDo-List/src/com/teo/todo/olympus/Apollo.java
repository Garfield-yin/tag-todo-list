package com.teo.todo.olympus;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;

/**
 * This helper class deals with music, media and stuff like that.
 */
public final class Apollo {
  private static MediaPlayer sPlayer;
  private static int sCounter;
  
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
   */
  public final static void play(final Context c, final Uri uri) {
    sPlayer = new MediaPlayer();
    try {
      sPlayer.setDataSource(c, uri);
      sPlayer.prepare();
    } catch (Exception e) {
      // if we can't play sound, no point in doing anything else since the
      // user already has a notification onscreen
      return;
    }
    sPlayer.start();
  }

}
