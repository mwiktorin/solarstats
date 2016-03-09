package de.mwiktorin.solarstats.dialogs;

import java.util.Calendar;
import java.util.Date;

import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;

public class DatePickerFragment extends DialogFragment {

	private OnDateSetListener listener;
	private int year;
	private int month;
	private int day;
	private long maxDate = -1;
	private String title = "";
	private DatePicker picker;

	public DatePickerFragment() {
		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);
	}

	public void setOnDateSetListener(OnDateSetListener listener) {
		this.listener = listener;
	}

	public void setStartDate(int day, int month, int year) {
		this.day = day;
		this.month = month;
		this.year = year;
	}

	public void setStartDate(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		day = cal.get(Calendar.DATE);
		month = cal.get(Calendar.MONTH);
		year = cal.get(Calendar.YEAR);
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setMaxDate(Date maxDate) {
		this.maxDate = maxDate.getTime();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		View view = View.inflate(getActivity(), R.layout.dialog_date_picker, null);
		picker = (DatePicker) view.findViewById(R.id.datePicker);
		picker.updateDate(year, month, day);
		if (maxDate != -1) {
			picker.setMaxDate(maxDate);
		}
		((TextView) view.findViewById(R.id.alertTitle)).setText(title);
		((Button) view.findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				listener.onDateSet(picker, picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
			}
		});
		dialog.setView(view);
		return dialog.show();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		if (listener != null && picker != null)
			listener.onDateSet(picker, picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
		super.onDismiss(dialog);
	}
}