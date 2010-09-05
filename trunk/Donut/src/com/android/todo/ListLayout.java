package com.android.todo;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class ListLayout extends LinearLayout {
  private Runnable mRunnable = null;
  private int mSelected = -1;

  public ListLayout(Context context) {
    super(context);
    this.setOrientation(LinearLayout.VERTICAL);
  }

  public final int getSelected() {
    return mSelected;
  }

  public final void select(final int index) {
    select(this.getChildAt(index));
  }

  private final void select(final View v) {
    v.setBackgroundColor(Color.GRAY);
    final LinearLayout ll = (LinearLayout) v.getParent();
    for (int i = 0; i < ll.getChildCount(); i++) {
      if (ll.getChildAt(i).equals(v)) {
        mSelected = i;
      } else {
        ll.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
      }
    }
    if (mRunnable != null) {
      mRunnable.run();
    }
  }

  public final void setAdapter(final ArrayAdapter<CharSequence> aa) {
    this.removeAllViews();
    for (int i = 0; i < aa.getCount(); i++) {
      final TextView tv = new TextView(this.getContext());
      tv.setTextAppearance(this.getContext(),
          android.R.style.TextAppearance_Medium);
      tv.setText(aa.getItem(i));
      tv.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          select(v);
        }
      });
      this.addView(tv);
    }
  }

  public final void setOnClickRunnable(final Runnable r) {
    mRunnable = r;
  }
}
