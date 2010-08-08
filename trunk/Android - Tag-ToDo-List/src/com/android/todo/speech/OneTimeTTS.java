package com.android.todo.speech;

import android.content.Context;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

/**
 * Text-To-Speech wrapper class, which is designed for one time use, then self
 * destructs
 */
public final class OneTimeTTS extends TTS implements
    OnUtteranceCompletedListener {

  public OneTimeTTS(final Context c, final String s) {
    super(c, s);
  }

  /**
   * An event handler, which also shuts down the object
   */
  public void onUtteranceCompleted(String s) {
    mTts.shutdown();
  }
}
