package de.mwiktorin.solarstats.tasks;

import java.util.ArrayList;

import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.storage.MyContentProvider;
import de.mwiktorin.solarstats.storage.MySQLiteHelper;
import de.mwiktorin.solarstats.model.System;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;

public class SaveSystem extends AsyncTask<System, Void, Void> {

	private Context context;

	public SaveSystem(Context context) {
		this.context = context;
	}

	@Override
	protected Void doInBackground(System... params) {
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.SYSTEMS_COLUMN_NAME, params[0].getName());
		values.put(MySQLiteHelper.SYSTEMS_COLUMN_DATE, params[0].getDatabaseDate());
		values.put(MySQLiteHelper.SYSTEMS_COLUMN_PAYMENT, params[0].getPayment());
		values.put(MySQLiteHelper.SYSTEMS_COLUMN_PEAK, params[0].getPeak());
		values.put(MySQLiteHelper.SYSTEMS_COLUMN_COLOR, params[0].getColor());
		if (params[0].getId() == -1) {
			long insertId = Long.parseLong(context.getContentResolver().insert(MyContentProvider.SYSTEM_URI, values).getLastPathSegment());
			params[0].setId(insertId);
			if (EventBus.getSystems() == null) {
				EventBus.getInstance().setSystems(new ArrayList<System>());
			}
			EventBus.getSystems().add(params[0]);
		} else {
			context.getContentResolver().update(MyContentProvider.SYSTEM_URI, values, MySQLiteHelper.SYSTEMS_COLUMN_ID + " = " + params[0].getId(), null);
			EventBus.getSystems().set(EventBus.getSystems().indexOf(params[0]), params[0]);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		EventBus.getInstance().fireSystemUpdate();
	}

}
