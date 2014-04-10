package com.justdoit.samplecontentprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class TodoContentProvider extends ContentProvider {

	public static final Uri CONTENT_URI = Uri
			.parse("content://com.justdoit.samplecontentprovider/todoitems");
	private static final int ALLROWS = 1;
	private static final int SINGLE_ROW = 2;
	public static final String KEY_ID = "_id";
	public static final String KEY_TASK = "task";
	public static final String KEY_CREATION_DATE = "creation_date";
	private MySQLiteOpenHelper openHelper;

	private static final UriMatcher uriMatcher;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI("com.justdoit.samplecontentprovider", "todoitems",
				ALLROWS);
		uriMatcher.addURI("com.justdoit.samplecontentprovider", "todoitems/#",
				SINGLE_ROW);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		SQLiteDatabase db = openHelper.getWritableDatabase();

		switch (uriMatcher.match(uri)) {
		case SINGLE_ROW:
			String row_id = uri.getPathSegments().get(1);
			selection = MySQLiteOpenHelper.KEY_ID
					+ " = "
					+ row_id
					+ (!TextUtils.isEmpty(selection) ? "AND " + "(" + selection
							+ ")" : "");
			break;
		default:
			break;
		}

		if (null == selection) {
			selection = "1";
		}

		int deleteCount = db.delete(MySQLiteOpenHelper.DATABASE_TABLE,
				selection, selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);
		return deleteCount;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		case ALLROWS:
			return "vnd.android.cursor.dir/vnd.justdoit.todos";
		case SINGLE_ROW:
			return "vnd.android.cursor.item/vnd.justdoit.todos";
		default:
			throw new IllegalArgumentException("Unsupported URI : " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		SQLiteDatabase db = openHelper.getWritableDatabase();

		String nullColumnHack = null;

		long id = db.insert(MySQLiteOpenHelper.DATABASE_TABLE, nullColumnHack,
				values);

		if (id > -1) {
			Uri InsertedId = ContentUris.withAppendedId(CONTENT_URI, id);
			getContext().getContentResolver().notifyChange(InsertedId, null);
		} else {
			return null;
		}
		return null;
	}

	@Override
	public boolean onCreate() {
		openHelper = new MySQLiteOpenHelper(getContext(),
				MySQLiteOpenHelper.DATABASE_NAME, null,
				MySQLiteOpenHelper.DATABASE_VERSION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteDatabase db = openHelper.getReadableDatabase();

		String groupBy = null;
		String having = null;

		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(MySQLiteOpenHelper.DATABASE_TABLE);

		switch (uriMatcher.match(uri)) {
		case SINGLE_ROW:
			String Row_Id = uri.getPathSegments().get(1);
			builder.appendWhere(MySQLiteOpenHelper.KEY_ID + "=" + Row_Id);
		default:
			break;
		}

		Cursor cursor = builder.query(db, projection, selection, selectionArgs,
				groupBy, having, sortOrder);
		return cursor;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SQLiteDatabase db = openHelper.getReadableDatabase();

		switch (uriMatcher.match(uri)) {
		case SINGLE_ROW:
			String row_id = uri.getPathSegments().get(1);
			selection = MySQLiteOpenHelper.KEY_ID
					+ "="
					+ row_id
					+ (!TextUtils.isEmpty(selection) ? " AND " + "("
							+ selection + ")" : "");
			break;
		default:
			break;
		}

		int updateCount = db.update(MySQLiteOpenHelper.DATABASE_TABLE, values,
				selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return updateCount;
	}

	private static class MySQLiteOpenHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "todoDatabase.db";
		private static final int DATABASE_VERSION = 1;
		private static final String DATABASE_TABLE = "todoItemTable";
		public static final String KEY_ID = "_id";
		public static final String KEY_TASK = "task";
		public static final String KEY_CREATION_DATE = "creation_date";

		private static final String DATABASE_CREATE = "create table "
				+ DATABASE_TABLE + " (" + KEY_ID
				+ " integer primary key autoincrement, " + KEY_TASK + " text not null,"
				+ KEY_CREATION_DATE + "long);";

		public MySQLiteOpenHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		// Called when no database exists in disk and the helper class needs
		// to create a new one.
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		// Called when there is a database version mismatch meaning that the
		// version
		// of the database on disk needs to be upgraded to the current version.
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF IT EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}
}
