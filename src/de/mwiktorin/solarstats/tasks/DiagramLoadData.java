package de.mwiktorin.solarstats.tasks;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import android.database.Cursor;
import android.os.AsyncTask;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.fragments.DiagramFragment;
import de.mwiktorin.solarstats.model.DataRow;
import de.mwiktorin.solarstats.model.DateCount;
import de.mwiktorin.solarstats.storage.MyContentProvider;
import de.mwiktorin.solarstats.storage.MySQLiteHelper;

public class DiagramLoadData extends AsyncTask<String, Integer, Boolean> {

	private DiagramFragment fragment;
	private List<DataRow>[] list;
	private List<DateCount> monthList;
	private List<DateCount> yearList;

	public DiagramLoadData(DiagramFragment fragement) {
		this.fragment = fragement;
	}

	@Override
	protected Boolean doInBackground(String... params) {
		list = new List[EventBus.getSystems().size()];

		for (int i = 0; i < EventBus.getSystems().size(); i++) {
			List<DataRow> data = new ArrayList<DataRow>();
			Cursor cursor = fragment
			        .getActivity()
			        .getContentResolver()
			        .query(MyContentProvider.DATA_URI, new String[] { MySQLiteHelper.DATA_COLUMN_DATE, MySQLiteHelper.DATA_COLUMN_VALUE },
			                MySQLiteHelper.DATA_COLUMN_SYSTEM + " = " + EventBus.getSystems().get(i).getId(), null, MySQLiteHelper.DATA_COLUMN_DATE);
			if (cursor.moveToFirst()) {
				do {
					data.add(new DataRow(cursor.getString(0), cursor.getInt(1), -1));
				} while (cursor.moveToNext());
			}
			cursor.close();
			list[i] = data;
		}

		monthList = new ArrayList<DateCount>();
		Calendar calender = Calendar.getInstance();
		calender.set(Calendar.DAY_OF_MONTH, 1);
		for (DataRow row : list[Utils.getCurrentSystemPosition(fragment.getActivity())]) {
			Calendar rowCal = new GregorianCalendar();
			rowCal.setTime(row.getDate());
			calender.set(Calendar.MONTH, rowCal.get(Calendar.MONTH));
			calender.set(Calendar.YEAR, rowCal.get(Calendar.YEAR));
			DateCount dateCount = new DateCount(calender.getTime());
			if (!monthList.contains(dateCount)) {
				monthList.add(dateCount);
			} else {
				monthList.get(monthList.indexOf(dateCount)).incrementCount();
			}
		}
		for (int i = 0; i < monthList.size(); i++) {
			if (monthList.get(i).getCount() < 2) {
				monthList.remove(i);
				i--;
			}
		}

		yearList = new ArrayList<DateCount>();
		calender.set(Calendar.MONTH, 0);
		for (DataRow row : list[Utils.getCurrentSystemPosition(fragment.getActivity())]) {
			Calendar rowCal = new GregorianCalendar();
			rowCal.setTime(row.getDate());
			calender.set(Calendar.YEAR, rowCal.get(Calendar.YEAR));
			DateCount dateCount = new DateCount(calender.getTime());
			if (!yearList.contains(dateCount)) {
				yearList.add(dateCount);
			} else {
				yearList.get(yearList.indexOf(dateCount)).incrementCount();
			}
		}
		for (int i = 0; i < yearList.size(); i++) {
			if (yearList.get(i).getCount() < 2) {
				yearList.remove(i);
				i--;
			}
		}
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		fragment.onDataPrepared(list, monthList, yearList);
	}

}
