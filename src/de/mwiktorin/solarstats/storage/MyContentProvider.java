package de.mwiktorin.solarstats.storage;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

public class MyContentProvider extends ContentProvider {

	static final String AUTHORITY = "de.mwiktorin.solarstats.MyContentProvider";
	public static final Uri DATA_URI = Uri.parse("content://" + AUTHORITY + "/data");
	public static final Uri SYSTEM_URI = Uri.parse("content://" + AUTHORITY + "/systems");
	private MySQLiteHelper dbHelper;
	private SQLiteDatabase database;

	private static final int DATA_ID = 1;
	private static final int DATA = 2;
	private static final int SYSTEMS_ID = 3;
	private static final int SYSTEMS = 4;

	private static UriMatcher sUriMatcher;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "data", DATA);
		sUriMatcher.addURI(AUTHORITY, "data/#", DATA_ID);
		sUriMatcher.addURI(AUTHORITY, "systems", SYSTEMS);
		sUriMatcher.addURI(AUTHORITY, "systems/#", SYSTEMS_ID);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return database.delete(getTable(uri), createSelection(uri, selection), selectionArgs);
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long insertId = database.insert(getTable(uri), null, values);
		database.query(getTable(uri), null, getIdColumn(uri) + " = " + insertId, null, null, null, null);
		return Uri.withAppendedPath(uri, insertId + "");
	}

	@Override
	public boolean onCreate() {
		dbHelper = new MySQLiteHelper(getContext());
		database = dbHelper.getWritableDatabase();
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return database.query(getTable(uri), projection, createSelection(uri, selection), selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return database.update(getTable(uri), values, createSelection(uri, selection), selectionArgs);
	}

	private String getIdColumn(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case DATA_ID:
		case DATA:
			return MySQLiteHelper.DATA_COLUMN_ID;
		case SYSTEMS_ID:
		case SYSTEMS:
			return MySQLiteHelper.SYSTEMS_COLUMN_ID;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	private String getTable(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case DATA_ID:
		case DATA:
			return MySQLiteHelper.TABLE_DATA;
		case SYSTEMS_ID:
		case SYSTEMS:
			return MySQLiteHelper.TABLE_SYSTEMS;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	private String createSelection(Uri uri, String selection) {
		switch (sUriMatcher.match(uri)) {
		case DATA:
		case SYSTEMS:
			return selection;
		case DATA_ID:
			return MySQLiteHelper.DATA_COLUMN_ID + "=" + uri.getLastPathSegment() + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
		case SYSTEMS_ID:
			return MySQLiteHelper.SYSTEMS_COLUMN_ID + "=" + uri.getLastPathSegment() + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

}
