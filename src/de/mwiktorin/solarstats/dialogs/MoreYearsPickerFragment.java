package de.mwiktorin.solarstats.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;

public class MoreYearsPickerFragment extends DialogFragment {

	private int minValue;
	private int maxValue;
	private int year1;
	private int year2;
	private View.OnClickListener listener;
	private NumberPicker picker1;
	private NumberPicker picker2;

	public void setValues(int minValue, int maxValue, int year1, int year2) {
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.year1 = year1;
		this.year2 = year2;
	}

	public void setListener(View.OnClickListener listener) {
		this.listener = listener;
	}
	
	public int getSmallerValue(){
		return Math.min(picker1.getValue(), picker2.getValue());
	}
	
	public int getBiggerValue(){
		return Math.max(picker1.getValue(), picker2.getValue());
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		View view = View.inflate(getActivity(), R.layout.dialog_years_picker, null);
		picker1 = (NumberPicker) view.findViewById(R.id.numberPicker1);
		picker2 = (NumberPicker) view.findViewById(R.id.numberPicker2);
		picker1.setMinValue(minValue);
		picker1.setMaxValue(maxValue);
		picker1.setValue(year1);
		picker2.setMinValue(minValue);
		picker2.setMaxValue(maxValue);
		picker2.setValue(year2);
		TextView textView = (TextView) view.findViewById(R.id.alertTitle);
		textView.setText(R.string.diagramm_fragment_dialog_pickyears_title);
		((TextView) view.findViewById(R.id.two_years_picker_text)).setText(R.string.diagramm_fragment_dialog_pickyears_text_moreyears);
		textView.setCompoundDrawablesWithIntrinsicBounds(getActivity().getResources().getDrawable(R.drawable.ic_menu_today), null, null, null);
		((Button) view.findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
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
	    if(listener != null)
	    	listener.onClick(null);
	    super.onDismiss(dialog);
	}

}
