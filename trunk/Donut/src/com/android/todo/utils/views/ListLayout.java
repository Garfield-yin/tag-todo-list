package com.android.todo.utils.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ListLayout extends LinearLayout {

  private Runnable mRunnable = null;
  private int mSelected = -1;

  public ListLayout(Context context) {
    super(context);
    this.setOrientation(LinearLayout.VERTICAL);
  }

  public ListLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    this.setOrientation(LinearLayout.VERTICAL);
  }

  /**
   * @return The selected index
   */
  public final int getSelected() {
    return mSelected;
  }

  /**
   * Select a view by its index.
   * 
   * @param index
   */
  public final void select(final int index) {
    select(this.getChildAt(index));
  }

  /**
   * Selects one of the views.
   * 
   * @param v
   */
  private final void select(final View v) {
    if (v == null) {
      return;
    }
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

  /**
   * Sets an adapter and creates the corresponding views.
   * 
   * @param aa
   */
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

  /**
   * Sets the Runnable to be run when one of the views is clicked.
   * 
   * @param r
   */
  public final void setOnClickRunnable(final Runnable r) {
    mRunnable = r;
  }
}
