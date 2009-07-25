//class made by Teo ( www.teodorfilimon.com ). More about the app in readme.txt

package com.android.todo.sync;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import com.android.todo.TagToDoList;

public final class Provider extends ContentProvider {
	private static final UriMatcher sUriMatcher = new UriMatcher(0);
	private static final String AUTHORITY = "com.android.todo.sync.provider";
	private static final int URI_TAGS = 1;
	private static final int URI_TASKS = 2;
	private static final int URI_SPECIFIC_TAG = 3;
	private static final int URI_TASKS_DUE = 4;
	private static final int URI_TASKS_DUE_TODAY = 5;

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		sUriMatcher.addURI(AUTHORITY, "tags", URI_TAGS);
		sUriMatcher.addURI(AUTHORITY, "tasks", URI_TASKS);
		sUriMatcher.addURI(AUTHORITY, "tags/*", URI_SPECIFIC_TAG);
		sUriMatcher.addURI(AUTHORITY, "tasks/due", URI_TASKS_DUE);
		sUriMatcher.addURI(AUTHORITY, "tasks/due/present", URI_TASKS_DUE_TODAY);
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
		case URI_TASKS:
			return TagToDoList.getDbHelper().getEntries(null);
		case URI_TAGS:
			return TagToDoList.getDbHelper().getAllTags();
		case URI_SPECIFIC_TAG:
			return TagToDoList.getDbHelper().getEntries(
					uri.getLastPathSegment());
		case URI_TASKS_DUE:
			return TagToDoList.getDbHelper().getAllDueEntries();
		case URI_TASKS_DUE_TODAY:
			return TagToDoList.getDbHelper().getDueEntries();
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
}
