package de.mwiktorin.solarstats.tasks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.database.Cursor;
import android.os.AsyncTask;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.fragments.SolarsystemsFragment;
import de.mwiktorin.solarstats.model.System;
import de.mwiktorin.solarstats.storage.MyContentProvider;
import de.mwiktorin.solarstats.storage.MySQLiteHelper;

public class SolarsystemLoadData extends AsyncTask<Void, Void, List<HashMap<String, Object>>> {

	private static final SimpleDateFormat DATE_DATABASE = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

	public static final String KEY_SYSTEM = "system";
	public static final String KEY_COUNT = "count";
	public static final String KEY_EFFICIENCY = "efficiency";
	public static final String KEY_PROFIT = "profit";
	public static final String KEY_LASTVALUE = "lastvalue";
	public static final String KEY_FIRSTDATE = "firstdate";

	private SolarsystemsFragment fragment;

	public SolarsystemLoadData(SolarsystemsFragment fragment) {
		this.fragment = fragment;
	}

	@Override
	protected List<HashMap<String, Object>> doInBackground(Void... params) {

		List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();

		for (System system : EventBus.getSystems()) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put(KEY_SYSTEM, system);
			Cursor cursor = fragment
			        .getActivity()
			        .getContentResolver()
			        .query(MyContentProvider.DATA_URI, new String[] { MySQLiteHelper.DATA_COLUMN_DATE, MySQLiteHelper.DATA_COLUMN_VALUE },
			                MySQLiteHelper.DATA_COLUMN_SYSTEM + " = " + system.getId(), null, MySQLiteHelper.DATA_COLUMN_DATE + " DESC");

			map.put(KEY_COUNT, cursor.getCount());

			if (cursor.moveToFirst()) {
				try {
					map.put(KEY_LASTVALUE, cursor.getInt(1));
					if (system.getPeak() != 0) {
						Date lastDate = DATE_DATABASE.parse(cursor.getString(0));
						int dayDiff = Utils.getDayDiff(system.getDate(), lastDate) + 1;
						double efficiency = (double) cursor.getInt(1) / (((double) system.getPeak() / 365) * dayDiff);
						map.put(KEY_EFFICIENCY, efficiency);
					}
					if (system.getPayment() != 0) {
						int lastValue = cursor.getInt(1);
						map.put(KEY_PROFIT, (double) Math.round(lastValue * system.getPayment()) / 100);
					}
					
					cursor.moveToLast();
					map.put(KEY_FIRSTDATE, DATE_DATABASE.parse(cursor.getString(0)));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			cursor.close();
			result.add(map);
		}

		return result;
	}

	@Override
	protected void onPostExecute(List<HashMap<String, Object>> result) {
		fragment.onSystemsLoaded(result);
	}

}
