//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

/**
 * Handles speech
 */
public final class TTS implements OnInitListener {
  private TextToSpeech mTts;
  private String mText;

  /**
   * The initial string to be spoken can't be null!! A check is not performed
   * due to performance reasons.
   * 
   * @param c
   * @param s
   */
  public TTS(Context c, String s) {
    mTts = new TextToSpeech(c, this);
    mText = s;
  }

  /**
   * An event handler, which also speaks the initial string
   */
  public void onInit(int status) {
    mTts.speak(mText, 0, null);
  }

  /**
   * Speaks any given string (also, for performance reasons, it won't check if
   * the TTS object is shutdown or not)
   * 
   * @param s
   */
  public void speak(String s) {
    mTts.speak(s, 0, null);
  }

  /**
   * Shuts down the TTS object
   */
  public void shutdown() {
    mTts.shutdown();
  }
}
