//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * This is another activity (basically an audio screen). It will allow the user
 * to do the following: - record an audio note - replay the previously recorded
 * audio note
 */
public final class AudioScreen extends Activity {

	private static final int MAX_RECORDING_DURATION = 50; // in seconds

	private Button mAudioExitButton;
	private Button mAudioRecordButton;
	private Button mReplayButton = null;
	private static String mEntry = null;
	private static boolean mStatus = false;
	private static boolean RECORDING_STATE;
	private static MediaRecorder mRecorder;
	private static MediaPlayer mPlayer = null;
	private static Timer mSeekBarTimer = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.audio_note_title);
		setContentView(R.layout.audio);

		// checking if the necessary folders exist on the sdcard
		File f = new File("/sdcard/Tag-ToDo_data/audio/");
		if (f.exists() == false) {
			try {
				f.mkdirs();
			} catch (Exception e) {
				Utils.showDialog(R.string.notification, R.string.audio_recording_impossible, this);
			}
		}

		mEntry = savedInstanceState != null ? savedInstanceState
				.getString(ToDoListDB.KEY_NAME) : null;

		Bundle extras = getIntent().getExtras();
		if (mEntry == null) {
			mEntry = extras != null ? extras.getString(ToDoListDB.KEY_NAME)
					: null;
		}

		mStatus = extras != null ? extras.getBoolean(ToDoListDB.KEY_STATUS)
				: false;

		final LinearLayout ll = (LinearLayout) findViewById(R.id.audioNoteLayout);

		// creating a TextView which will contain the elapsed time in both
		// recording and playback modes
		final TextView positionTextView = new TextView(this);
		positionTextView.setGravity(Gravity.CENTER_HORIZONTAL);
		positionTextView.setPadding(0, 32, 0, 0);
		positionTextView.setTextColor(Color.WHITE);
		positionTextView.setTextSize(30);
		ll.addView(positionTextView);

		final SeekBar sb = new SeekBar(this);
		sb.setPadding(0, 32, 0, 32);
		sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromTouch) {
				positionTextView.setText(progress + " s");
				if (RECORDING_STATE) {
					if (progress == MAX_RECORDING_DURATION - 10) {
						positionTextView.setText(R.string.warning);
					} else if (progress == MAX_RECORDING_DURATION - 9) {
						positionTextView
								.setText(R.string.audio_recording_limit);
						positionTextView.append(" " + MAX_RECORDING_DURATION
								+ " s");
					} else if (progress == MAX_RECORDING_DURATION) {
						mAudioRecordButton.performClick();
					}
				}
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				if (mPlayer != null) {
					mPlayer.seekTo(sb.getProgress() * 1000);
					mPlayer.start();
				}
			}
		});
		ll.addView(sb);
		mPlayer = new MediaPlayer();

		mAudioExitButton = (Button) findViewById(R.id.audioExitButton);
		mAudioRecordButton = (Button) findViewById(R.id.audioRecordButton);

		mAudioExitButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				setResult(RESULT_OK);
				finish();
			}
		});

		/*
		 * A TimerTask that will be given to a timer. It's responsible with
		 * refreshing the seekbar.
		 */
		final class SeekBarTask extends TimerTask {

			public SeekBarTask(int duration, boolean recording) {
				if (recording) {
					sb.setVisibility(4); // invisible
					sb.setMax(MAX_RECORDING_DURATION);
				} else {
					sb.setMax(duration / 1000 + 1);
				}
				sb.setProgress(0);
			}

			public void run() {
				sb.incrementProgressBy(1);
			}
		}

		mAudioRecordButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				RECORDING_STATE = !(RECORDING_STATE);
				((Button) view)
						.setText(RECORDING_STATE ? R.string.audio_stop_recording
								: R.string.audio_record);
				((Button) view).setTextColor(RECORDING_STATE ? Color.RED
						: Color.argb(255, 33, 150, 0));
				if (RECORDING_STATE) {
					if (mReplayButton != null) {
						ll.removeView(mReplayButton);
					}
					if (mSeekBarTimer != null) {
						mSeekBarTimer.cancel();
					}

					(mSeekBarTimer = new Timer()).schedule(new SeekBarTask(
							mPlayer.getDuration(), true), 0, 1000);

					mPlayer.stop();
					mRecorder = new MediaRecorder();
					mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					mRecorder
							.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
					mRecorder
							.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
					mRecorder.setOutputFile(Utils.getAudioName(mEntry));
					try {
						mRecorder.prepare();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
					}
					try {
						mRecorder.start();
					} catch (Exception e) {
						// mSeekBarTimer.cancel();
						((Button) view).performClick();
						Utils.showDialog(-1,
								R.string.audio_recording_impossible,
								AudioScreen.this);
					}
				} else {
					mSeekBarTimer.cancel();
					try {
						mRecorder.stop();
						mRecorder.release();
					} catch (Exception e) {
						return;
					}
					Intent i = new Intent(AudioScreen.this, AudioScreen.class);
					i.putExtra(ToDoListDB.KEY_NAME, mEntry);
					i.putExtra(ToDoListDB.KEY_STATUS, false);
					startActivity(i);
					finish();
				}
			}
		});

		try {
			mPlayer.setDataSource(Utils.getAudioName(mEntry));
			mPlayer.prepare();
			if (!(mStatus)) {
				mReplayButton = new Button(this);
				mReplayButton.setText(R.string.audio_replay);
				mReplayButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						ll.removeView(v);
						if (mSeekBarTimer != null) {
							mSeekBarTimer.cancel();
						}
						(mSeekBarTimer = new Timer()).schedule(new SeekBarTask(
								mPlayer.getDuration(), false), 0, 1000);

						mPlayer
								.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
									public void onCompletion(MediaPlayer mp) {
										sb.setProgress(sb.getMax());
									}
								});
						mPlayer.start();
					}
				});
				ll.addView(mReplayButton);
				throw new Exception();
			}
		} catch (Exception e) {
			if (mStatus) {
				sb.setVisibility(4);
			}
			return;
		}

		// if there previously was an exception it means that execution will not
		// reach this point;
		// if we are here, it means that an audio file has been found AND it
		// needs to be played

		mPlayer.start();

		(mSeekBarTimer = new Timer()).schedule(new SeekBarTask(mPlayer
				.getDuration(), false), 0, 1000);

		mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				sb.setProgress(sb.getMax());
			}
		});
	}

	/**
	 * Creates all the UI elements
	 */
	private void populateFields() {
		// initializing some stuff
		RECORDING_STATE = false;
		mAudioRecordButton.setTextColor(Color.argb(255, 33, 150, 0));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ToDoListDB.KEY_NAME, mEntry);
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

	/**
	 * Saves the state on pause
	 */
	private void saveState() {
		if (RECORDING_STATE) {
			mAudioRecordButton.performClick();
		}
		mPlayer.stop();
		if (mSeekBarTimer != null) {
			mSeekBarTimer.cancel();
		}
	}

	public boolean onKeyDown(int keyCode, KeyEvent msg) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_ENTER:
			mAudioRecordButton.performClick();
			break;
		case KeyEvent.KEYCODE_DEL:
			mAudioExitButton.performClick();
			break;
		case KeyEvent.KEYCODE_BACK:
			finish();
			break;
		case KeyEvent.KEYCODE_R:
			if (!(RECORDING_STATE)) {
				mAudioRecordButton.performClick();
			}
			break;
		case KeyEvent.KEYCODE_S:
			if (RECORDING_STATE) {
				mAudioRecordButton.performClick();
			}
			break;
		}
		return false;
	}
}
