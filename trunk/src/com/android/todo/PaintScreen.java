//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.io.FileNotFoundException;
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

/**
 * This is an activity which allows us to draw a graphical note for a specific
 * ToDo list entry
 */
public final class PaintScreen extends GraphicsActivity {
  private Paint mPaint;
  private MyView mView;
  private LinearLayout mLL;
  private static String mEntry;
  private Button mClearButton, mExitButton;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle(R.string.note_title);
    setContentView(R.layout.note);

    mEntry = savedInstanceState != null ? savedInstanceState
        .getString(ToDoDB.KEY_NAME) : null;

    if (mEntry == null) {
      Bundle extras = getIntent().getExtras();
      mEntry = extras != null ? extras.getString(ToDoDB.KEY_NAME) : null;
    }

    mLL = (LinearLayout) findViewById(R.id.ll);
    mView = new MyView(this, true);
    mLL.addView(mView);

    mClearButton = (Button) findViewById(R.id.clearButton);
    mClearButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        mLL.removeView(mView);
        mView = new MyView(v.getContext(), false);
        mLL.addView(mView);
        TagToDoList.getDbHelper().setFlag(mEntry, ToDoDB.KEY_NOTE_IS_GRAPHICAL,
            0);
      }
    });

    mExitButton = (Button) findViewById(R.id.exitButton);
    mExitButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        mView.save(Utils.getImageName(mEntry));
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
      try {
        fIn = getContext().openFileInput(Utils.getImageName(mEntry));
      } catch (FileNotFoundException e) {
        found = false;
      }

      found = found && fromScratch;

      DisplayMetrics dm = new DisplayMetrics();
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
        fOut = getContext().openFileOutput(path, Context.MODE_PRIVATE);
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        fOut.flush();
        fOut.close();
        TagToDoList.getDbHelper().setFlag(mEntry, ToDoDB.KEY_NOTE_IS_GRAPHICAL,
            1);
      } catch (Exception e) {
      }
    }
  }

  public boolean onKeyDown(int keyCode, KeyEvent msg) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_ENTER:
      mExitButton.performClick();
      break;
    case KeyEvent.KEYCODE_DEL:
      mClearButton.performClick();
      break;
    case KeyEvent.KEYCODE_BACK:
      finish();
      break;
    }
    return false;
  }
}