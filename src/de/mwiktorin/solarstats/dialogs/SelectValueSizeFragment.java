package de.mwiktorin.solarstats.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;

public class SelectValueSizeFragment extends DialogFragment {

	private View.OnClickListener listener;
	private RadioGroup group;
	private CheckBox check;

	public void setListener(View.OnClickListener listener) {
		this.listener = listener;
	}

	public int getCheckedRadioButtonId() {
		return group.getCheckedRadioButtonId();
	}

	public boolean isChecked() {
		return check.isChecked();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		View view = View.inflate(getActivity(), R.layout.dialog_select_value_size, null);
		group = (RadioGroup) view.findViewById(R.id.values_radioGroup);
		check = (CheckBox) view.findViewById(R.id.values_checkbox);
		String[] sizes = getActivity().getResources().getStringArray(R.array.valueSizes);
		String valueSize = Utils.getUnit(getActivity());
		for (int i = 0; i < sizes.length; i++) {
			RadioButton button = new RadioButton(getActivity());
			button.setText(sizes[i]);
			button.setId(i);
			button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			button.setChecked(sizes[i].equals(valueSize));
			group.addView(button);
		}
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
}
