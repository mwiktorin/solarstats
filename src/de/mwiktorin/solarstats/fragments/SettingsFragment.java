package de.mwiktorin.solarstats.fragments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TimePicker;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.Preferences;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.dialogs.MyAlertDialog;
import de.mwiktorin.solarstats.dialogs.SelectValueSizeFragment;
import de.mwiktorin.solarstats.dialogs.TimePickerFragment;
import de.mwiktorin.solarstats.tasks.DeleteAllData;
import de.mwiktorin.solarstats.tasks.RecalculateData;

public class SettingsFragment extends PreferenceFragment {

	private static final SimpleDateFormat DATE_NEXT_ALARM = new SimpleDateFormat("cccc, dd.MM.yyyy, HH:mm", Locale.getDefault());

	private Preference deleteAll;
	private Preference valueSize;
	private CheckBoxPreference enableReminder;
	private Preference reminderInterval;
	private Preference reminderTime;
	private Preference reminderDay;
	private Preference reminderNextAlarm;

	private OnPreferenceClickListener valueSizeListener = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			final SelectValueSizeFragment dialogFragment = new SelectValueSizeFragment();
			dialogFragment.setListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String oldSize = Utils.getUnit(getActivity());
					String newSize = getActivity().getResources().getStringArray(R.array.valueSizes)[dialogFragment.getCheckedRadioButtonId()];
					if (oldSize.equals(newSize)) {
						return;
					}
					if (dialogFragment.isChecked()) {
						RecalculateData recalcualteTask = new RecalculateData(EventBus.getData(), SettingsFragment.this, oldSize, newSize);
						recalcualteTask.execute();
					}
					Preferences.getInstance(getActivity()).putString(getActivity().getString(R.string.prefkey_valueSize), newSize);
					valueSize.setSummary(newSize);
					EventBus.getInstance().fireDataChanged();
				}
			});
			dialogFragment.show(getActivity().getFragmentManager(), "valueSizeDialog");
			return true;
		}
	};

	private OnPreferenceClickListener deleteListener = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			MyAlertDialog dialog = new MyAlertDialog(getActivity());
			dialog.setTitle(R.string.settings_title_deleteAll);
			dialog.setIcon(R.drawable.ic_menu_delete);
			dialog.setMessage(R.string.settings_summary_deleteAll);
			dialog.setMyNegativeButton(R.string.settings_deleteDilaog_negative, null);
			dialog.setMyPositiveButton(R.string.settings_deleteDilaog_positive, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DeleteAllData deleteTask = new DeleteAllData(SettingsFragment.this);
					deleteTask.execute();
				}
			});
			dialog.show();
			return true;
		}
	};

	private OnPreferenceClickListener reminderTimeListener = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			String timeString = Preferences.getInstance(getActivity()).getString(getActivity().getString(R.string.prefkey_reminderTime),
			        getActivity().getString(R.string.settings_reminderTime_default));
			TimePickerFragment dialogFragment = new TimePickerFragment();
			dialogFragment.setSartTime(Integer.parseInt(timeString.split(":")[0]), Integer.parseInt(timeString.split(":")[1]));
			dialogFragment.setTitle(getActivity().getString(R.string.settings_timeDialog_title));
			dialogFragment.setOnTimeSetListener(new OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					String time = (hourOfDay < 10 ? "0" + hourOfDay : hourOfDay) + ":" + (minute < 10 ? "0" + minute : minute);
					Preferences.getInstance(getActivity()).putString(getActivity().getString(R.string.prefkey_reminderTime), time);
					reminderTime.setSummary(time);
					alarmChanged();
				}
			});
			dialogFragment.show(getFragmentManager(), "timeDialog");
			return true;
		}
	};

	private OnPreferenceClickListener reminderIntervalListener = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			MyAlertDialog dialog = new MyAlertDialog(getActivity());
			dialog.setTitle(R.string.settings_intervalDialog_title);
			dialog.setIcon(R.drawable.ic_menu_today);
			dialog.setItems(R.array.reminderIntervals, new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
					Preferences.getInstance(getActivity()).putInt(getActivity().getString(R.string.prefkey_reminderInterval), pos);
					reminderInterval.setSummary(getActivity().getResources().getStringArray(R.array.reminderIntervals)[pos]);
					alarmChanged();
					updateDayVisibility();
				}
			});
			dialog.show();
			return true;
		}
	};

	private OnPreferenceClickListener reminderDayListener = new OnPreferenceClickListener() {
		public boolean onPreferenceClick(Preference preference) {
			MyAlertDialog dialog = new MyAlertDialog(getActivity());
			dialog.setTitle(R.string.settings_dayDialog_title);
			dialog.setIcon(R.drawable.ic_menu_day);
			dialog.setItems(R.array.daysOfWeek, new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
					Preferences.getInstance(getActivity()).putInt(getActivity().getString(R.string.prefkey_reminderDay), pos);
					reminderDay.setSummary(getActivity().getResources().getStringArray(R.array.daysOfWeek)[pos]);
					alarmChanged();
				}
			});
			dialog.show();
			return true;
		}
	};

	private OnPreferenceChangeListener enableReminderListener = new OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			boolean value = newValue.toString().trim().equals("true");
			Preferences.getInstance(getActivity()).putBoolean(getActivity().getString(R.string.prefkey_enableReminder), value);
			if (value) {
				Utils.updateNextAlarm(getActivity());
			} else {
				Preferences.getInstance(getActivity()).putLong(getActivity().getString(R.string.prefkey_reminderNextAlarm), 0);
			}
			updateNextAlarm();
			return true;
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.fragment_settings);

		valueSize = findPreference(getActivity().getString(R.string.prefkey_valueSize));
		valueSize.setSummary(Utils.getUnit(getActivity()));
		valueSize.setOnPreferenceClickListener(valueSizeListener);

		deleteAll = findPreference(getActivity().getString(R.string.prefkey_deleteAllData));
		deleteAll.setOnPreferenceClickListener(deleteListener);

		enableReminder = (CheckBoxPreference) findPreference(getActivity().getString(R.string.prefkey_enableReminder));
		enableReminder.setOnPreferenceChangeListener(enableReminderListener);

		reminderInterval = (Preference) findPreference(getActivity().getString(R.string.prefkey_reminderInterval));
		reminderInterval.setSummary(getActivity().getResources().getStringArray(R.array.reminderIntervals)[Preferences.getInstance(getActivity())
		        .getInt(getActivity().getString(R.string.prefkey_reminderInterval), 0)]);
		reminderInterval.setOnPreferenceClickListener(reminderIntervalListener);

		reminderDay = (Preference) findPreference(getActivity().getString(R.string.prefkey_reminderDay));
		updateDayVisibility();
		reminderDay.setOnPreferenceClickListener(reminderDayListener);

		reminderNextAlarm = (Preference) findPreference(getActivity().getString(R.string.prefkey_reminderNextAlarm));
		updateNextAlarm();

		reminderTime = findPreference(getActivity().getString(R.string.prefkey_reminderTime));
		reminderTime.setSummary(Preferences.getInstance(getActivity()).getString(getActivity().getString(R.string.prefkey_reminderTime),
		        getActivity().getString(R.string.settings_reminderTime_default)));
		reminderTime.setOnPreferenceClickListener(reminderTimeListener);
	}
	
	private void alarmChanged(){
		Preferences.getInstance(getActivity()).putLong(Preferences.LAST_ALARM, 0);
		Utils.updateNextAlarm(getActivity());
		updateNextAlarm();
	}

	private void updateDayVisibility() {
		boolean enable = Preferences.getInstance(getActivity()).getInt(getActivity().getString(R.string.prefkey_reminderInterval), 0) == 3;
		reminderDay.setEnabled(enable);
		if (enable) {
			reminderDay.setSummary(getActivity().getResources().getStringArray(R.array.daysOfWeek)[Preferences.getInstance(getActivity()).getInt(
			        getActivity().getString(R.string.prefkey_reminderDay), 0)]);
		} else {
			reminderDay.setSummary("");
		}
	}

	private void updateNextAlarm() {
		long nextAlarm = Preferences.getInstance(getActivity()).getLong(getActivity().getString(R.string.prefkey_reminderNextAlarm), 0);
		if (nextAlarm != 0) {
			reminderNextAlarm.setSummary(DATE_NEXT_ALARM.format(new Date(nextAlarm)));
		} else {
			reminderNextAlarm.setSummary("");
		}
	}
}
