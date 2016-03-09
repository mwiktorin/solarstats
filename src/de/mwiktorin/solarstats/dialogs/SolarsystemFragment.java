package de.mwiktorin.solarstats.dialogs;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.model.System;

public class SolarsystemFragment extends DialogFragment {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

	private View.OnClickListener listener;
	private EditText inputName;
	private EditText inputPeak;
	private EditText inputPayment;
	private Spinner spinnerColor;
	private Date date;
	private Button dateButton;
	private View.OnClickListener cancelListener;
	private System system;
	private Date maxDate;

	public void setListener(View.OnClickListener listener) {
		this.listener = listener;
	}

	public void setCancelListener(View.OnClickListener listener) {
		cancelListener = listener;
	}

	public void setSystem(System system) {
		this.system = system;
	}

	public void setMaxDate(Date maxDate) {
		this.maxDate = maxDate;
	}

	public int getPeak() {
		if (inputPeak.getText().toString().equals("")) {
			return 0;
		}
		return Integer.parseInt(inputPeak.getText().toString());
	}

	public double getPayment() {
		if (inputPayment.getText().toString().equals("")) {
			return 0;
		}
		return Double.parseDouble(inputPayment.getText().toString().replace(",", "."));
	}

	public String getName() {
		return inputName.getText().toString();
	}

	public Date getDate() {
		return date;
	}

	public int getColor() {
		return Color.parseColor(getResources().getStringArray(R.array.systemColorsHEX)[spinnerColor.getSelectedItemPosition()]);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		final View view = View.inflate(getActivity(), R.layout.dialog_solarsystem, null);

		inputName = (EditText) view.findViewById(R.id.new_solarsystem_name);
		inputPeak = (EditText) view.findViewById(R.id.new_solarsystem_peak);
		inputPayment = (EditText) view.findViewById(R.id.new_solarsystem_payment_edit);
		spinnerColor = (Spinner) view.findViewById(R.id.new_solarsystem_color);
		dateButton = (Button) view.findViewById(R.id.new_solarsystem_date_button);
		final Button positiveButton = (Button) view.findViewById(R.id.button2);

		((TextView) view.findViewById(R.id.alertTitle)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_add, 0, 0, 0);
		((TextView) view.findViewById(R.id.alertTitle)).setText(R.string.solarsystems_fragment_dialog_new_title);
		((TextView) view.findViewById(R.id.new_solarsystem_payment_text)).setText(String.format(
		        getActivity().getString(R.string.solarsystems_fragment_dialog_new_payment), Utils.getUnit(getActivity())));
		((TextView) view.findViewById(R.id.solarsystem_fragment_dialog_peak_unit)).setText(Utils.getPeakUnit(getActivity()));

		if (system != null) {
			((TextView) view.findViewById(R.id.alertTitle)).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_edit, 0, 0, 0);
			((TextView) view.findViewById(R.id.alertTitle)).setText(R.string.solarsystems_fragment_dialog_edit_title);
			inputName.setText(system.getName());
			String color = "#" + Integer.toHexString(system.getColor()).substring(2).toUpperCase(Locale.US);
			int foundIndex = 0;
			String[] colorArray = getActivity().getResources().getStringArray(R.array.systemColorsHEX);
			for (int i = 0; i < colorArray.length; i++) {
				if (color.equals(colorArray[i])) {
					foundIndex = i;
					break;
				}
			}
			spinnerColor.setSelection(foundIndex);
			dateButton.setText(DATE_FORMAT.format(system.getDate()));
			positiveButton.setEnabled(true);
			date = system.getDate();
			inputPeak.setText(system.getPeak() == 0 ? "" : system.getPeak() + "");
			inputPayment.setText(system.getPayment() == 0 ? "" : (system.getPayment() + "").replace(".", ","));
		}

		inputName.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				positiveButton.setEnabled(count == 0 ? false : date != null);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		dateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerFragment dateDialog = new DatePickerFragment();
				dateDialog.setTitle(getActivity().getString(R.string.solarsystems_fragment_dialog_new_date_dialog_title));
				if (system != null) {
					dateDialog.setStartDate(system.getDate());
				}
				if (maxDate != null) {
					dateDialog.setMaxDate(maxDate);
				} else {
					dateDialog.setMaxDate(new Date());
				}
				dateDialog.setOnDateSetListener(new OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						Calendar cal = Calendar.getInstance();
						cal.set(year, monthOfYear, dayOfMonth, 0, 0, 0);
						date = cal.getTime();
						positiveButton.setEnabled(!inputName.getText().toString().equals(""));
						dateButton.setText(DATE_FORMAT.format(date));
					}
				});
				dateDialog.show(getActivity().getFragmentManager(), "datePickerDialog");
			}
		});

		((Button) view.findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				if (cancelListener != null) {
					cancelListener.onClick(v);
				}
			}
		});
		positiveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				listener.onClick(v);
			}
		});
		dialog.setView(view);
		return dialog.show();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (cancelListener != null) {
			cancelListener.onClick(null);
		}
		super.onCancel(dialog);
	}

}
