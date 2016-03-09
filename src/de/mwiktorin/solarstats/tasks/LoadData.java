package de.mwiktorin.solarstats.tasks;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.Preferences;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.dialogs.MyProgressDialog;
import de.mwiktorin.solarstats.model.DataRow;
import de.mwiktorin.solarstats.model.System;
import de.mwiktorin.solarstats.storage.MyContentProvider;
import de.mwiktorin.solarstats.storage.MySQLiteHelper;

public class LoadData extends AsyncTask<String, Integer, List<DataRow>> {

	private Activity activity;
	private boolean showDialog;
	private MyProgressDialog progressDialog;

	public LoadData(Activity activity, boolean showDialog) {
		this.activity = activity;
		this.showDialog = showDialog;
	}

	@Override
	protected void onPreExecute() {
		if (!showDialog) {
			return;
		}
		progressDialog = new MyProgressDialog(activity);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setMessage(activity.getString(R.string.load_data_dialog_text));
		progressDialog.show();
	}

	@Override
	protected List<DataRow> doInBackground(String... params) {
		List<DataRow> list = new ArrayList<DataRow>();

		Cursor cursor = activity.getContentResolver().query(MyContentProvider.DATA_URI,
		        new String[] { MySQLiteHelper.DATA_COLUMN_DATE, MySQLiteHelper.DATA_COLUMN_VALUE, MySQLiteHelper.DATA_COLUMN_ID },
		        MySQLiteHelper.DATA_COLUMN_SYSTEM + " = " + Preferences.getInstance(activity).getLong(Preferences.CURRENT_SYSTEM_ID, 1), null,
		        MySQLiteHelper.DATA_COLUMN_DATE);
		if (cursor.moveToFirst()) {
			do {
				list.add(new DataRow(cursor.getString(0), cursor.getInt(1), cursor.getLong(2)));
			} while (cursor.moveToNext());
		}
		cursor.close();

		if (EventBus.getSystems() == null) {
			List<System> systemList = new ArrayList<System>();
			Cursor systemCursor = activity.getContentResolver().query(
			        MyContentProvider.SYSTEM_URI,
			        new String[] { MySQLiteHelper.SYSTEMS_COLUMN_ID, MySQLiteHelper.SYSTEMS_COLUMN_NAME, MySQLiteHelper.SYSTEMS_COLUMN_DATE,
			                MySQLiteHelper.SYSTEMS_COLUMN_PEAK, MySQLiteHelper.SYSTEMS_COLUMN_PAYMENT, MySQLiteHelper.SYSTEMS_COLUMN_COLOR }, null, null, null);
			if (systemCursor.moveToFirst()) {
				do {
					systemList.add(new System(systemCursor.getLong(0), systemCursor.getString(1), systemCursor.getString(2), systemCursor.getInt(3),
					        systemCursor.getDouble(4), systemCursor.getInt(5)));
				} while (systemCursor.moveToNext());
			}
			systemCursor.close();
			EventBus.getInstance().setSystems(systemList);
		}

		// try {
		// Thread.sleep(4000);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }

		return list;
	}

	@Override
	protected void onPostExecute(List<DataRow> result) {
		EventBus.getInstance().setData(result);
		EventBus.getInstance().fireDataLoaded();
		if (progressDialog != null) {
			progressDialog.dismiss();
		}
	}

}
