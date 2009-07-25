//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Contacts;

/**
 * This is a class which actually performs the action found in a text
 */
public final class Action {
	private ArrayList<String> CALLS = new ArrayList<String>();
	private ArrayList<String> MOVES = new ArrayList<String>();
	private ArrayList<String> BUYS = new ArrayList<String>();
	private Performer mPerformer;

	/**
	 * Inner class that represents a performer and is supposed to be inherited
	 * by specific performers. The specific performers handle the text that
	 * represents the target of the action however they want.
	 */
	private abstract class Performer {
		protected String[] mWords;
		protected int mCurrentIndex;
		protected String mTarget = null;

		public Performer(String[] words, int currentIndex) {
			mWords = words;
			mCurrentIndex = currentIndex;

			final String[] endMarks = { ".", "!", ";" };

			StringBuilder sb = new StringBuilder();
			for (; currentIndex < words.length; currentIndex++) {
				sb.append(" ");
				sb.append(words[currentIndex]);
			}
			if (sb.length() < 1) {
				return;
			}
			mTarget = sb.toString().substring(1);
			int possibleEnd;
			for (int i = 0; i < endMarks.length; i++) {
				possibleEnd = mTarget.indexOf(endMarks[i]);
				if (possibleEnd > 0) {
					mTarget = mTarget.substring(0, possibleEnd);
				}
			}

			final String[] ignorables = { ToDoListDB.res
					.getString(R.string.move_ignorable) };
			for (int i = 0; i < ignorables.length; i++) {
				if (mTarget.startsWith(ignorables[i])) {
					mTarget = mTarget.substring(ignorables[i].length() + 1);
				}
			}
		}

		public String toString() {
			return "! ";
		}

		public abstract void perform(Context c);
	}

	/**
	 * A special kind of performer, which can make calls
	 */
	private class CallPerformer extends Performer {

		public CallPerformer(String[] words, int currentIndex) {
			super(words, currentIndex);
		}

		public void perform(Context c) {
			if (Character.isDigit(mTarget.charAt(0))
					&& Character.isDigit(mTarget.charAt(1))) {
				Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
						+ mTarget));
				c.startActivity(i);
			} else {
				Intent i = new Intent(
						Contacts.Intents.UI.FILTER_CONTACTS_ACTION);
				i.putExtra(Contacts.Intents.UI.FILTER_TEXT_EXTRA_KEY, mTarget);
				c.startActivity(i);
			}
		}

		public String toString() {
			return super.toString()
					+ ToDoListDB.res.getString(R.string.call_making) + " "
					+ mTarget;
		}
	}

	/**
	 * A special kind of performer, which deals with movement (maps, etc.)
	 */
	private class MovePerformer extends Performer {

		public MovePerformer(String[] words, int currentIndex) {
			super(words, currentIndex);
		}

		public void perform(Context c) {
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="
					+ mTarget));
			c.startActivity(i);
		}

		public String toString() {
			return super.toString()
					+ (mTarget.length() < 9 ? mTarget : ToDoListDB.res
							.getString(R.string.find)) + " "
					+ ToDoListDB.res.getString(R.string.google_maps);
		}
	}

	/**
	 * A special kind of performer, which deals with buying stuff (Amazon, etc.)
	 */
	private class BuyPerformer extends Performer {

		public BuyPerformer(String[] words, int currentIndex) {
			super(words, currentIndex);
		}

		public void perform(Context c) {
			Intent i;
			try {
				i = new Intent(
						Intent.ACTION_VIEW,
						Uri
								.parse("http://www.amazon.com/gp/search?ie=UTF8&keywords="+URLEncoder.encode(mTarget,"UTF-8")+"&tag=tagtodo-20&index=blended&linkCode=ur2&camp=1789&creative=9325"));
				c.startActivity(i);
			} catch (UnsupportedEncodingException e) {
			}
		}

		public String toString() {
			return super.toString()
					+ (mTarget.length() < 14 ? mTarget : ToDoListDB.res
							.getString(R.string.buying)) + " "
					+ ToDoListDB.res.getString(R.string.amazon);
		}
	}

	/**
	 * Constructor for the action, also initializes the so called possible
	 * symbols, depending on the language
	 * 
	 * @param text
	 */
	public Action() {
		CALLS.add(ToDoListDB.res.getString(R.string.call1));
		CALLS.add(ToDoListDB.res.getString(R.string.call2));
		MOVES.add(ToDoListDB.res.getString(R.string.move1));
		MOVES.add(ToDoListDB.res.getString(R.string.move2));
		MOVES.add(ToDoListDB.res.getString(R.string.move3));
		BUYS.add(ToDoListDB.res.getString(R.string.buy1));
		BUYS.add(ToDoListDB.res.getString(R.string.buy2));
	}

	public void perform(Context c) {
		mPerformer.perform(c);
	}

	public String setAndExtractAction(String text) {
		String[] words = text.toUpperCase().split(" ");
		for (int i = 0; i < words.length; i++) {
			if (MOVES.contains(words[i])) {
				mPerformer = new MovePerformer(words, i + 1);
				return mPerformer.toString();
			} else if (BUYS.contains(words[i])) {
				mPerformer = new BuyPerformer(words, i + 1);
				return mPerformer.toString();
			} else if (CALLS.contains(words[i])) {
				mPerformer = new CallPerformer(words, i + 1);
				return mPerformer.toString();
			}
		}
		return "";
	}
}