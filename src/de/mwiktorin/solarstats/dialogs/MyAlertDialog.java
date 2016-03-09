package de.mwiktorin.solarstats.dialogs;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;

public class MyAlertDialog extends AlertDialog implements DialogInterface {

	private View mDialogView;

	private TextView mTitle;
	private TextView mMessage;

	public MyAlertDialog(Context context) {
		super(context);

		mDialogView = View.inflate(context, R.layout.dialog, null);
		setView(mDialogView);

		mTitle = (TextView) mDialogView.findViewById(R.id.alertTitle);
		mMessage = (TextView) mDialogView.findViewById(R.id.message);
	}

	@Override
	public void setTitle(CharSequence text) {
		mTitle.setText(text);
	}

	@Override
	public void setTitle(int titleId) {
		mTitle.setText(getContext().getString(titleId));
	}

	public void setTitleColor(String colorString) {
		mTitle.setTextColor(Color.parseColor(colorString));
	}

	public void setMessage(int textResId) {
		mMessage.setText(textResId);
	}

	@Override
	public void setMessage(CharSequence text) {
		mMessage.setText(text);
	}

	@Override
	public void setIcon(int drawableResId) {
		mTitle.setCompoundDrawablesWithIntrinsicBounds(getContext().getResources().getDrawable(drawableResId), null, null, null);
	}

	@Override
	public void setIcon(Drawable icon) {
		mTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
	}

	public void setMyNegativeButton(int textId, final View.OnClickListener listener) {
		Button b = (Button) mDialogView.findViewById(R.id.button1);
		b.setVisibility(View.VISIBLE);
		b.setText(textId);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null)
					listener.onClick(v);
				MyAlertDialog.this.dismiss();
			}
		});
	}

	public void setMyNeutralButton(int textId, final View.OnClickListener listener) {
		Button b = (Button) mDialogView.findViewById(R.id.button3);
		b.setVisibility(View.VISIBLE);
		b.setText(textId);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null)
					listener.onClick(v);
				MyAlertDialog.this.dismiss();
			}
		});
	}

	public void setMyPositiveButton(int textId, final View.OnClickListener listener) {
		Button b = (Button) mDialogView.findViewById(R.id.button2);
		b.setVisibility(View.VISIBLE);
		b.setText(textId);
		b.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (listener != null)
					listener.onClick(v);
				MyAlertDialog.this.dismiss();
			}
		});
	}

	public void setItems(int itemsId, final OnItemClickListener listener) {
		setItems(getContext().getResources().getStringArray(itemsId), listener);
	}

	public void setItems(List<String> items, final OnItemClickListener listener) {
		String[] array = new String[items.size()];
		for (int i = 0; i < items.size(); i++) {
			array[i] = items.get(i);
		}
		setItems(array, listener);
	}

	public void setItems(String[] items, final OnItemClickListener listener) {
		ListView view = new ListView(getContext());
		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		view.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, items));
		view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				listener.onItemClick(parent, view, pos, id);
				MyAlertDialog.this.dismiss();
			}
		});
		((LinearLayout) mDialogView.findViewById(R.id.customPanel)).addView(view);
		mDialogView.findViewById(R.id.message).setVisibility(View.GONE);
	}

	public void setCostumView(int resId, Context context) {
		LinearLayout frameLayout = (LinearLayout) mDialogView.findViewById(R.id.customPanel);
		View customView = View.inflate(context, resId, frameLayout);
		frameLayout.addView(customView);
	}

	public void setCostumView(View view) {
		LinearLayout frameLayout = (LinearLayout) mDialogView.findViewById(R.id.customPanel);
		frameLayout.addView(view);
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