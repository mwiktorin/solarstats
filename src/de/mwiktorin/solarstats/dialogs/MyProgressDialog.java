package de.mwiktorin.solarstats.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;

public class MyProgressDialog extends AlertDialog implements DialogInterface {

	private View mDialogView;
	private TextView mTitle;
	private TextView mMessage;
	private ProgressBar progressBar;
	private TextView percent;
	private TextView absolute;
	private int max;

	public MyProgressDialog(Context context) {
		super(context);

		mDialogView = View.inflate(context, R.layout.dialog_progress, null);
		setView(mDialogView);

		mTitle = (TextView) mDialogView.findViewById(R.id.alertTitle);
		mMessage = (TextView) mDialogView.findViewById(R.id.message);
		progressBar = (ProgressBar) mDialogView.findViewById(R.id.progressBar);

		percent = (TextView) mDialogView.findViewById(R.id.progressPercent);
		absolute = (TextView) mDialogView.findViewById(R.id.progressAbsolute);
	}

	@Override
	public void setTitle(CharSequence title) {
		mDialogView.findViewById(R.id.titleBar).setVisibility(View.VISIBLE);
		mTitle.setText(title);
	}

	@Override
	public void setTitle(int titleId) {
		setTitle(getContext().getString(titleId));
	}

	@Override
	public void setMessage(CharSequence message) {
		mMessage.setText(message);
	}

	@Override
	public void setIcon(Drawable icon) {
		mTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
	}

	@Override
	public void setIcon(int resId) {
		setIcon(getContext().getResources().getDrawable(resId));
	}

	public void setMax(int max) {
		this.max = max;
		mDialogView.findViewById(R.id.progressCircle).setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);
		mDialogView.findViewById(R.id.progressBarText).setVisibility(View.VISIBLE);
		progressBar.setMax(max);
		absolute.setText("0 / " + max);
		percent.setText("0 %");
	}

	public void setProgress(int value) {
		progressBar.setProgress(value);
		absolute.setText(value + " / " + max);
		percent.setText(Math.round((double) 100 * value / max) + " %");
	}

	@Override
	public void show() {
		super.show();
	}

	@Override
	public void cancel() {
		super.cancel();
	}

	@Override
	public void dismiss() {
		super.dismiss();
	}

}
