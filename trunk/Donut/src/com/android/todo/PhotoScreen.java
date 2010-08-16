//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.io.FileNotFoundException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.android.todo.data.ToDoDB;

/**
 * This is another activity (basically an audio screen). It will allow the user
 * to do the following: - record an audio note - replay the previously recorded
 * audio note
 */
public final class PhotoScreen extends Activity {
  private static String sEntry = null;
  private static Button sCamButton, sDelButton, sCloseButton;
  // private static ImageView sImageView;
  private static WebView sWebView;
  private static Uri sUri;
  private static ToDoDB sDbHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    ToDo.setTheme(this, getSharedPreferences(ToDo.PREFS_NAME,
        Context.MODE_PRIVATE));
    super.onCreate(savedInstanceState);
    setTitle(R.string.entry_photo_note);
    setContentView(R.layout.photo);
    final Bundle extras = getIntent().getExtras();
    if (sEntry == null) {
      sEntry = extras != null ? extras.getString(ToDoDB.KEY_NAME) : null;
    }
    sDbHelper = ToDoDB.getInstance(getApplicationContext());
    sUri = Uri
        .parse(sDbHelper.getStringFlag(sEntry, ToDoDB.KEY_PHOTO_NOTE_URI));
    sCamButton = (Button) findViewById(R.id.camButton);
    sCamButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        final Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        i.putExtra(MediaStore.EXTRA_OUTPUT, sUri);
        i.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        startActivityForResult(i, ToDo.TASK_PHOTO_ID);
      }
    });
    sDelButton = (Button) findViewById(R.id.photoDelButton);
    sDelButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        sDbHelper.setFlag(sEntry, ToDoDB.KEY_NOTE_IS_PHOTO, 0);
        finish();
      }
    });
    sCloseButton = (Button) findViewById(R.id.photoCloseButton);
    sCloseButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        finish();
      }
    });
    // sImageView = (ImageView) findViewById(R.id.imageView);
    // sImageView.setDrawingCacheEnabled(false);
    // sImageView.setWillNotCacheDrawing(false);
    sWebView = (WebView) findViewById(R.id.webView);
    sWebView.getSettings().setBuiltInZoomControls(true);
    sWebView.setBackgroundColor(Color.BLACK);
  }

  /**
   * Creates all the UI elements
   * 
   * @throws FileNotFoundException
   */
  private final void populateFields() throws FileNotFoundException {
    /*
     * final BitmapFactory.Options options = new BitmapFactory.Options();
     * options.inSampleSize = 1; Bitmap photoBitmap =
     * BitmapFactory.decodeStream(getContentResolver() .openInputStream(sUri),
     * null, options); int h = photoBitmap.getHeight(); int w =
     * photoBitmap.getWidth(); if ((w > h) && (w > 256)) { double ratio = 256d /
     * w; w = 256; h = (int) (ratio * h); } else if ((h > w) && (h > 256)) {
     * double ratio = 256d / h; h = 256; w = (int) (ratio * w); } final Bitmap
     * scaled = Bitmap.createScaledBitmap(photoBitmap, w, h, true);
     * photoBitmap.recycle(); sImageView.setImageBitmap(scaled);
     */
    sWebView.loadData(
        "<meta name='viewport' content='width=device-width' /><img src='"
            + sUri.toString() + "'/>", "text/html", "UTF-8");
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == ToDo.TASK_PHOTO_ID) {
      if (resultCode == RESULT_OK) {
        try {
          populateFields();
        } catch (FileNotFoundException e) {
        }
      } else {
        Toast
            .makeText(this, getString(R.string.error_photo), Toast.LENGTH_LONG)
            .show();
      }
    }
  }

  @Override
  protected void onDestroy() {
    sEntry = null;
    // sImageView.destroyDrawingCache();
    // sImageView = null;
    sWebView.destroy();
    sWebView = null;
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ToDoDB.KEY_NAME, sEntry);
  }

  @Override
  protected void onResume() {
    super.onResume();
    try {
      populateFields();
    } catch (FileNotFoundException e) {
    }
  }
}
