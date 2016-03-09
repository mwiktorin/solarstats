package de.mwiktorin.solarstats.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;

public class ImageSizePickerFragment extends DialogFragment {

	private View.OnClickListener listener;
	private SeekBar widthBar;
	private SeekBar heightBar;
	private int min;
	private int max;
	
	public ImageSizePickerFragment() {
    }
	
	public void setListener (View.OnClickListener listener){
		this.listener = listener;
	}
	
	public int getWidth() {
		return (widthBar.getProgress() + min) * 100;
	}
	
	public int getHeight() {
		return (heightBar.getProgress() + min) * 100;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		View view = View.inflate(getActivity(), R.layout.dialog_image_size, null);
		final TextView widthText = (TextView) view.findViewById(R.id.dialog_image_size_width_text);
		final TextView heightText = (TextView) view.findViewById(R.id.dialog_image_size_height_text);
		widthBar = (SeekBar) view.findViewById(R.id.dialog_image_size_widthbar);
		heightBar = (SeekBar) view.findViewById(R.id.dialog_image_size_heightbar);
		min = Integer.parseInt(getActivity().getString(R.string.diagramm_fragment_export_dialog_size_minvalue));
		max = Integer.parseInt(getActivity().getString(R.string.diagramm_fragment_export_dialog_size_maxvalue));
		widthBar.setMax(max - min);
		heightBar.setMax(max - min);
		OnSeekBarChangeListener onSeekBarChangeListener = new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				int realProgress = progress + min;
				realProgress *= 100;
				if (seekBar.getId() == R.id.dialog_image_size_widthbar) {
					widthText.setText(String.format(getActivity().getString(R.string.diagramm_fragment_export_dialog_size_value), realProgress));
				} else {
					heightText.setText(String.format(getActivity().getString(R.string.diagramm_fragment_export_dialog_size_value), realProgress));
				}
			}
		};
		widthBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		heightBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
		widthBar.setProgress(widthBar.getMax() / 2);
		heightBar.setProgress(heightBar.getMax() / 2);
		((Button) view.findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
				listener.onClick(v);
			}
		});
		((Button) view.findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		dialog.setView(view);
		return dialog.show();
	}
}