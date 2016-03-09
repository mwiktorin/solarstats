package de.mwiktorin.solarstats.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import de.mwiktorin.solarstats.R;

public class BluetoothImportFragment extends DialogFragment {

	private View.OnClickListener listener;

	public void setValues(View.OnClickListener listener) {
		this.listener = listener;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		View view = View.inflate(getActivity(), R.layout.dialog_found_import_data, null);


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