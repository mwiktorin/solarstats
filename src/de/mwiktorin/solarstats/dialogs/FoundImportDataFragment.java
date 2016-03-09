package de.mwiktorin.solarstats.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;

public class FoundImportDataFragment extends DialogFragment {

	private View.OnClickListener listener;
	private Spinner spinner;
	private int newCount;
	private String startDate;
	private int startValue;
	private String endDate;
	private int endValue;
	private String valueSize;
	private String messageSecond;
	private TextView message;

	public void setValues(int newCount, String startDate, int startValue, String endDate, int endValue, String valueSize, String messageSecond,
	        View.OnClickListener listener) {
		this.listener = listener;
		this.newCount = newCount;
		this.startDate = startDate;
		this.startValue = startValue;
		this.endDate = endDate;
		this.endValue = endValue;
		this.valueSize = valueSize;
		this.messageSecond = messageSecond;
	}

	public int getSelectedItem() {
		return spinner.getSelectedItemPosition();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		View view = View.inflate(getActivity(), R.layout.dialog_found_import_data, null);

		if (valueSize == null) {
			valueSize = Utils.getUnit(getActivity());
		}
		message = (TextView) view.findViewById(R.id.message);
		updateMessage();
		if (messageSecond != null) {
			TextView secondMessage = (TextView) view.findViewById(R.id.second_message);
			secondMessage.setVisibility(View.VISIBLE);
			secondMessage.setText(messageSecond);
		}

		spinner = (Spinner) view.findViewById(R.id.found_import_data_spinner);
		final String[] sizes = getActivity().getResources().getStringArray(R.array.valueSizes);
		for (int i = 0; i < sizes.length; i++) {
			if (sizes[i].equals(valueSize)) {
				spinner.setSelection(i);
				break;
			}
		}
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				valueSize = sizes[pos];
				updateMessage();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		((Button) view.findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		((Button) view.findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				listener.onClick(v);
			}
		});
		dialog.setView(view);
		return dialog.show();
	}

	private void updateMessage() {
		String messageFirst = String.format(getActivity().getResources().getString(R.string.data_fragment_import_excel_dialog_message), newCount,
		        startDate, startValue + " " + valueSize, endDate, endValue + " " + valueSize);
		message.setText(messageFirst);
	}
}
