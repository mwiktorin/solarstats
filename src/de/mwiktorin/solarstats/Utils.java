package de.mwiktorin.solarstats;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.inputmethod.InputMethodManager;

import com.google.analytics.tracking.android.EasyTracker;

import de.mwiktorin.solarstats.model.DataRow;
import de.mwiktorin.solarstats.model.System;
import de.mwiktorin.solarstats.receivers.AlarmReceiver;
import de.mwiktorin.solarstats.storage.MyContentProvider;
import de.mwiktorin.solarstats.storage.MySQLiteHelper;
import de.mwiktorin.solarstats.tasks.SaveData;

public class Utils {

	public static Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days); // minus number would decrement the days
		return new Date(cal.getTimeInMillis());
	}

	/**
	 * @param d1
	 *            early date
	 * @param d2
	 *            later date
	 * @return The day difference between the given dates
	 */
	public static int getDayDiff(Date d1, Date d2) {
		return (int) Math.round(((double) d2.getTime() - d1.getTime()) / (86400 * 1000));
	}

	public static void openFile(File file, Activity activity) {
		try {
			Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW);
			String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
			String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			myIntent.setDataAndType(Uri.fromFile(file), mimetype);
			activity.startActivity(myIntent);
		} catch (Exception e) {
		}
	}

	public static void hideKeyboard(Activity activity) {
		if (activity != null && activity.getCurrentFocus() != null && activity.getCurrentFocus().getWindowToken() != null) {
			((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(activity.getCurrentFocus()
			        .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	public static void showKeyboard(Activity activity) {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	/**
	 * @param file
	 * @return The ending if exists (e.g. ".pdf")
	 */
	public static String getFileEnding(File file) {
		int index = file.getName().lastIndexOf(".");
		if (index == -1) {
			return "";
		}
		return file.getName().substring(index);
	}

	public static void updateNextAlarm(Context context) {
		boolean reminderEnabled = Preferences.getInstance(context).getBoolean(context.getString(R.string.prefkey_enableReminder), false);
		if (!reminderEnabled) {
			return;
		}
		String savedTime = Preferences.getInstance(context).getString(context.getString(R.string.prefkey_reminderTime),
		        context.getString(R.string.settings_reminderTime_default));
		int savedInterval = Preferences.getInstance(context).getInt(context.getString(R.string.prefkey_reminderInterval), 0);
		int savedDay = Preferences.getInstance(context).getInt(context.getString(R.string.prefkey_reminderDay), 0);

		GregorianCalendar cal = new GregorianCalendar();
		cal.set(GregorianCalendar.HOUR_OF_DAY, Integer.parseInt(savedTime.split(":")[0]));
		cal.set(GregorianCalendar.MINUTE, Integer.parseInt(savedTime.split(":")[1]));
		cal.set(GregorianCalendar.SECOND, 0);

		if (!cal.after(Calendar.getInstance())) {
			cal.add(Calendar.DATE, 1);
		}

		if (savedInterval == 3) {
			while (cal.get(Calendar.DAY_OF_WEEK) != savedDay + 1) {
				cal.add(Calendar.DATE, 1);
			}
		} else if (savedInterval == 1 || savedInterval == 2) {
			Calendar tmp = Calendar.getInstance();
			tmp.setTimeInMillis(Preferences.getInstance(context).getLong(Preferences.LAST_ALARM, 0));
			if (tmp.getTimeInMillis() != 0) {
				switch (savedInterval) {
				case 1: // Every second day
					tmp.add(Calendar.DATE, 2);
					break;
				case 2: // Every fourth day
					tmp.add(Calendar.DATE, 4);
					break;
				}
				cal.set(Calendar.DATE, tmp.get(Calendar.DATE));
				cal.set(Calendar.MONTH, tmp.get(Calendar.MONTH));
				cal.set(Calendar.YEAR, tmp.get(Calendar.YEAR));
			}
		}

		Preferences.getInstance(context).putLong(context.getString(R.string.prefkey_reminderNextAlarm), cal.getTimeInMillis());
		PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class),
		        PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager am = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
		// am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(),
		// interval, alarmIntent);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmIntent);
	}

	public static void saveData(Context context, DataRow row, List<DataRow> list, boolean doInBackground) {
		if (doInBackground) {
			SaveData saveTask = new SaveData(context, list);
			saveTask.execute(row);
		} else {
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.DATA_COLUMN_DATE, row.getDatabaseDate());
			values.put(MySQLiteHelper.DATA_COLUMN_VALUE, row.getValue());
			values.put(MySQLiteHelper.DATA_COLUMN_SYSTEM, Preferences.getInstance(context).getLong(Preferences.CURRENT_SYSTEM_ID, 1));
			if (row.getId() != -1) {
				context.getContentResolver().update(MyContentProvider.DATA_URI, values, MySQLiteHelper.DATA_COLUMN_ID + " = " + row.getId(), null);
				list.set(list.indexOf(row), row);
			} else {
				long id = Long.parseLong(context.getContentResolver().insert(MyContentProvider.DATA_URI, values).getLastPathSegment());
				row.setId(id);
				list.add(getBeforeDateIndex(row.getDate(), list) + 1, row);
			}
			System system = EventBus.getSystems().get(getCurrentSystemPosition(context));
			if (row.getDate().before(system.getDate())) {
				values = new ContentValues();
				values.put(MySQLiteHelper.SYSTEMS_COLUMN_DATE, row.getDatabaseDate());
				context.getContentResolver().update(MyContentProvider.SYSTEM_URI, values, MySQLiteHelper.SYSTEMS_COLUMN_ID + " = " + system.getId(),
				        null);
				system.setDate(new Date(row.getDate().getTime()));
			}
			EasyTracker.getInstance().setContext(context);
//			TODO
//			EasyTracker.getTracker().sendEvent("Data", "Save data", "Save data", 0l);
		}
	}

	public static int getBeforeDateIndex(Date date, List<DataRow> list) {
		if (list.size() == 0) {
			return -1;
		}
		int nearDateIndex = findNearDate(date, list);
		while (nearDateIndex < list.size() && list.get(nearDateIndex).getDate().before(date)) {
			nearDateIndex++;
		}
		nearDateIndex--;
		while (nearDateIndex >= 0 && list.get(nearDateIndex).getDate().after(date)) {
			nearDateIndex--;
		}
		return nearDateIndex;
	}

	/**
	 * @param date
	 *            the date to find the nearest
	 * @return -1, if the date was found; position of the nearest date
	 */
	public static int findNearDate(Date date, List<DataRow> list) {
		return findNearDate(new DataRow(date, 0, -1), 0, list.size() - 1, list);
	}

	public static int findNearDate(DataRow date, int start, int end, List<DataRow> list) {
		if (start == end) {
			if (list.get(end).equals(date)) {
				return -1;
			} else {
				return end;
			}
		}
		DataRow centerDate = list.get((start + end) / 2);
		if (centerDate.equals(date)) {
			return (start + end) / 2;
		}
		return date.isBefore(centerDate) ? findNearDate(date, start, (start + end) / 2, list) : findNearDate(date, (start + end) / 2 + 1, end, list);

	}

	public static String getDoubleString(double x) {
		return String.format(Locale.getDefault(), "%1$,.2f", x);
	}

	public static String getIntString(double x) {
		return String.format(Locale.getDefault(), "%d", Math.round(x));
	}

	public static String getCurrentSystemTitle(Context context) {
		int i = getCurrentSystemPosition(context);
		if (i == -1) {
			return null;
		}
		return EventBus.getSystems().get(i).getName();
	}

	public static int getCurrentSystemPosition(Context context) {
		return EventBus.getSystems().indexOf(new System(Preferences.getInstance(context).getLong(Preferences.CURRENT_SYSTEM_ID, 1), "", "", 0, 0, 0));
	}

	public static void addPreviousToList(Context context, List<DataRow> fromList, List<DataRow> addList, int i) {
		if (i < 1) {
			Date date = EventBus.getSystems().get(getCurrentSystemPosition(context)).getDate();
			addList.add(0, new DataRow(addDays(date, -1), 0, -1));
		} else {
			addList.add(0, fromList.get(i - 1));
		}
	}

	public static String getUnit(Context context) {
		return Preferences.getInstance(context).getString(context.getString(R.string.prefkey_valueSize),
		        context.getString(R.string.settings_default_valueSize));
	}

	public static String getPeakUnit(Context context) {
		String unit = getUnit(context);
		unit = unit.substring(0, unit.length() - 1) + context.getString(R.string.solarsystems_fragment_dialog_new_peak_unit_ending);
		return unit;
	}
}