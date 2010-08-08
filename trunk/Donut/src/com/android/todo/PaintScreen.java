//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.android.todo.data.ToDoDB;

/**
 * This is an activity which allows us to draw a graphical note for a specific
 * ToDo list entry
 */
public final class PaintScreen extends GraphicsActivity {
  public final static String PATH="/sdcard/Tag-ToDo_data/graphics/";
  
  private Paint mPaint;
  private MyView mView;
  private LinearLayout mLL;
  private static String mEntry;
  private static Button sClearButton, sExitButton, sDeleteButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle(R.string.note_title);
    setContentView(R.layout.note);

    // checking if the necessary folders exist on the sdcard
    final File f = new File(PATH);
    if (f.exists() == false) {
      try {
        f.mkdirs();
      } catch (Exception e) {
        Utils.showDialog(R.string.notification,
            R.string.audio_recording_impossible, this);
      }
    }

    mEntry = savedInstanceState != null ? savedInstanceState
        .getString(ToDoDB.KEY_NAME) : null;

    if (mEntry == null) {
      Bundle extras = getIntent().getExtras();
      mEntry = extras != null ? extras.getString(ToDoDB.KEY_NAME) : null;
    }

    mLL = (LinearLayout) findViewById(R.id.ll);
    mView = new MyView(this, true);
    mLL.addView(mView);

    sClearButton = (Button) findViewById(R.id.clearButton);
    sClearButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        mLL.removeView(mView);
        mView = new MyView(v.getContext(), false);
        mLL.addView(mView);
      }
    });

    sExitButton = (Button) findViewById(R.id.exitButton);
    sExitButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        mView.save(Utils.getImageName(mEntry, (Boolean) mView.getTag()));
        finish();
      }
    });

    sDeleteButton = (Button) findViewById(R.id.deleteButton);
    sDeleteButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        ToDoDB.getInstance(getApplicationContext()).setFlag(mEntry,
            ToDoDB.KEY_NOTE_IS_GRAPHICAL, 0);
        // the next line should be removed when Android 2.2 is no longer
        // supported
        new File(Utils.getImageName(mEntry, false)).delete();

        new File(Utils.getImageName(mEntry, true)).delete();
        finish();
      }
    });

    mPaint = new Paint();
    mPaint.setAntiAlias(true);
    mPaint.setDither(true);
    mPaint.setColor(0xFFFFFF00);
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth(12);
  }

  final class MyView extends View {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Path mPath;
    private Paint mBitmapPaint;

    /**
     * Constructor for the MyView class
     * 
     * @param c
     *          is the context
     * @param fromScratch
     *          is a parameter which shows if we're rebuilding everything or
     *          just some parts of the UI
     */
    protected MyView(final Context c, final boolean fromScratch) {
      super(c);
      InputStream fIn = null;
      boolean found = true;
      final File f = new File(Utils.getImageName(mEntry, true));
      try {
        if (!f.exists()) {
          throw new Exception();
        } else {
          fIn = new FileInputStream(f.getCanonicalPath());
          this.setTag(new Boolean(true));
        }
      } catch (Exception e) {
        try {
          // the next line should be removed when Android 2.2 is no longer
          // supported
          fIn = getContext().openFileInput(Utils.getImageName(mEntry, false));
          this.setTag(new Boolean(false));
        } catch (Exception e2) {
          found = false;
          try {
            f.createNewFile();
            fIn = new FileInputStream(f.getCanonicalPath());
          } catch (IOException e1) {
          }
          this.setTag(new Boolean(true));
        }
      }

      found = found && fromScratch;

      final DisplayMetrics dm = new DisplayMetrics();
      getWindowManager().getDefaultDisplay().getMetrics(dm);
      mBitmap = Bitmap.createBitmap(dm.widthPixels, dm.heightPixels - 100,
          Bitmap.Config.ARGB_8888);

      mCanvas = new Canvas(mBitmap);
      mPath = new Path();
      mBitmapPaint = new Paint(Paint.DITHER_FLAG);

      if (found) {
        mCanvas.drawBitmap(BitmapFactory.decodeStream(fIn), 0, 0, mPaint);
        try {
          fIn.close();
        } catch (IOException e) {
        }
      }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
      super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
      canvas.drawColor(0xFF000000);
      canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
      canvas.drawPath(mPath, mPaint);
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
      mPath.reset();
      mPath.moveTo(x, y);
      mX = x;
      mY = y;
    }

    private void touch_move(float x, float y) {
      float dx = Math.abs(x - mX);
      float dy = Math.abs(y - mY);
      if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
        mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
        mX = x;
        mY = y;
      }
    }

    private void touch_up() {
      mPath.lineTo(mX, mY);

      // commit the path to off screen
      mCanvas.drawPath(mPath, mPaint);

      // kill this to avoid double draw
      mPath.reset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
      float x = event.getX();
      float y = event.getY();

      switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
          touch_start(x, y);
          invalidate();
          break;
        case MotionEvent.ACTION_MOVE:
          touch_move(x, y);
          invalidate();
          break;
        case MotionEvent.ACTION_UP:
          touch_up();
          invalidate();
          break;
      }
      return true;
    }

    /**
     * Saves the bitmap to a PNG file. This is not mandatory, but in calls of
     * this function the path should include a hash code of the corresponding
     * task's string.
     * 
     * @param path
     *          file location
     */
    public void save(String path) {
      OutputStream fOut = null;
      try {
        try {
          fOut = getContext().openFileOutput(path, Context.MODE_PRIVATE);
        } catch (Exception e1) {
          fOut = new FileOutputStream(path);
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        fOut.flush();
        fOut.close();
        ToDoDB.getInstance(getApplicationContext()).setFlag(mEntry,
            ToDoDB.KEY_NOTE_IS_GRAPHICAL, 1);
      } catch (Exception e) {
      }
    }
  }

  public boolean onKeyDown(int keyCode, KeyEvent msg) {
    switch (keyCode) {
      case KeyEvent.KEYCODE_ENTER:
        sExitButton.performClick();
        break;
      case KeyEvent.KEYCODE_DEL:
        sClearButton.performClick();
        break;
      case KeyEvent.KEYCODE_BACK:
        finish();
        break;
    }
    return false;
  }
}