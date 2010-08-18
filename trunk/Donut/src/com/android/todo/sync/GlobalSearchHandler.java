package com.android.todo.sync;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.android.todo.Edit;
import com.android.todo.ToDo;
import com.android.todo.data.ToDoDB;

public final class GlobalSearchHandler extends Activity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    startActivity(new Intent(this, Edit.class)
        .putExtra(Edit.EXTERNAL_INVOKER, true)
        .setAction(ToDo.ACTIVITY_EDIT_ENTRY + "")
        .putExtra(ToDoDB.KEY_NAME, getIntent().getData().getLastPathSegment())
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    finish();
  }

}
