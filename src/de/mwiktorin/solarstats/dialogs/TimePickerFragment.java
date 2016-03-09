package de.mwiktorin.solarstats.dialogs;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import de.mwiktorin.solarstats.R;

public class TimePickerFragment extends DialogFragment {

	private OnTimeSetListener listener;
	private int hour;
	private int minute;
	private String title = "";
	private TimePicker picker;

	public TimePickerFragment() {
		final Calendar c = Calendar.getInstance();
		hour = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
	}

	public void setOnTimeSetListener(OnTimeSetListener listener) {
		this.listener = listener;
	}

	public void setSartTime(int hour, int minute) {
		this.hour = hour;
		this.minute = minute;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		View view = View.inflate(getActivity(), R.layout.dialog_time_picker, null);
		picker = (TimePicker) view.findViewById(R.id.timePicker);
		picker.setCurrentHour(hour);
		picker.setCurrentMinute(minute);
		picker.setIs24HourView(true);
		((TextView) view.findViewById(R.id.alertTitle)).setText(title);
		((Button) view.findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				listener.onTimeSet(picker, picker.getCurrentHour(), picker.getCurrentMinute());
			}
		});
		dialog.setView(view);
		return dialog.show();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (listener != null && picker != null)
			listener.onTimeSet(picker, picker.getCurrentHour(), picker.getCurrentMinute());
		super.onDismiss(dialog);
	}
}