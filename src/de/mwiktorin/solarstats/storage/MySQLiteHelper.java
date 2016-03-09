package de.mwiktorin.solarstats.storage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import de.mwiktorin.solarstats.R;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_DATA = "datatable";
	public static final String DATA_COLUMN_ID = "_id";
	public static final String DATA_COLUMN_VALUE = "value";
	public static final String DATA_COLUMN_DATE = "date";
	public static final String DATA_COLUMN_SYSTEM = "system";

	public static final String TABLE_SYSTEMS = "systems";
	public static final String SYSTEMS_COLUMN_ID = "_id";
	public static final String SYSTEMS_COLUMN_NAME = "name";
	public static final String SYSTEMS_COLUMN_DATE = "date";
	public static final String SYSTEMS_COLUMN_PAYMENT = "payment";
	public static final String SYSTEMS_COLUMN_PEAK = "peak";
	public static final String SYSTEMS_COLUMN_COLOR = "color";

	private static final SimpleDateFormat DATE_DATABASE = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private static final String DATABASE_NAME = "solarstats.db";
	private static final int DATABASE_VERSION = 2;

	private static final String CREATE_DATA_TABLE = "create table " + TABLE_DATA + "(" + DATA_COLUMN_ID + " integer primary key autoincrement, "
	        + DATA_COLUMN_VALUE + " integer, " + DATA_COLUMN_DATE + " timestamp, " + DATA_COLUMN_SYSTEM + " integer" + ");";
	private static final String CREATE_SYSTEMS_TABLE = "create table " + TABLE_SYSTEMS + "(" + SYSTEMS_COLUMN_ID
	        + " integer primary key autoincrement, " + SYSTEMS_COLUMN_NAME + " varchar(50), " + SYSTEMS_COLUMN_DATE + " timestamp, "
	        + SYSTEMS_COLUMN_PAYMENT + " real, " + SYSTEMS_COLUMN_PEAK + " integer, " + SYSTEMS_COLUMN_COLOR + " integer" + ");";

	private Context context;

	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_DATA_TABLE);
		database.execSQL(CREATE_SYSTEMS_TABLE);
		String firstDate = DATE_DATABASE.format(new Date());
		int color = context.getResources().getColor(R.color.dark_orange);
		database.execSQL("INSERT INTO " + TABLE_SYSTEMS + " (" + SYSTEMS_COLUMN_ID + ", " + SYSTEMS_COLUMN_NAME + ", " + SYSTEMS_COLUMN_DATE + ", "
		        + SYSTEMS_COLUMN_COLOR + ") VALUES ('1', '" + context.getString(R.string.update_system_name) + "', '" + firstDate + "', '" + color
		        + "')");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			db.execSQL("ALTER TABLE " + TABLE_DATA + " ADD " + DATA_COLUMN_SYSTEM + " integer");
			db.execSQL(CREATE_SYSTEMS_TABLE);
			Cursor cursor = db.query(TABLE_DATA, new String[] { DATA_COLUMN_DATE }, null, null, null, null, DATA_COLUMN_DATE + " ASC", "1");
			String firstDate = DATE_DATABASE.format(new Date());
			if (cursor.moveToFirst()) {
				firstDate = cursor.getString(0);
			}
			cursor.close();
			int color = context.getResources().getColor(R.color.dark_orange);
			db.execSQL("INSERT INTO " + TABLE_SYSTEMS + " (" + SYSTEMS_COLUMN_ID + ", " + SYSTEMS_COLUMN_NAME + ", " + SYSTEMS_COLUMN_DATE + ", "
			        + SYSTEMS_COLUMN_COLOR + ") VALUES ('1', '" + context.getString(R.string.update_system_name) + "', '" + firstDate + "', '"
			        + color + "')");
			db.execSQL("UPDATE " + TABLE_DATA + " SET " + DATA_COLUMN_SYSTEM + "='1'");
		}
	}

}
