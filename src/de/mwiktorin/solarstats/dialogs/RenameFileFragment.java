package de.mwiktorin.solarstats.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;

public class RenameFileFragment extends DialogFragment {

	private String filename;
	private String fileending;
	private View.OnClickListener listener;
	private EditText input;

	public void setValues(String filename, String fileending, View.OnClickListener listener) {
		this.filename = filename;
		this.fileending = fileending;
		this.listener = listener;
	}

	public String getEditString() {
		return input.getText().toString();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		View view = View.inflate(getActivity(), R.layout.dialog_rename_file, null);
		input = (EditText) view.findViewById(R.id.rename_edit_text);
		Utils.showKeyboard(getActivity());
		input.setText(filename);
		input.setSelection(0, filename.length());
		input.requestFocus();
		TextView text = (TextView) view.findViewById(R.id.rename_fileending);
		text.setText(fileending);
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

	@Override
	public void onDismiss(DialogInterface dialog) {
		Utils.hideKeyboard(getActivity());
		super.onDismiss(dialog);
	}

}
