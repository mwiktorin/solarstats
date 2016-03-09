package de.mwiktorin.solarstats.tasks;

import java.util.List;

import android.content.ContentValues;
import android.os.AsyncTask;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.dialogs.MyProgressDialog;
import de.mwiktorin.solarstats.fragments.SettingsFragment;
import de.mwiktorin.solarstats.model.DataRow;
import de.mwiktorin.solarstats.storage.MyContentProvider;
import de.mwiktorin.solarstats.storage.MySQLiteHelper;

public class RecalculateData extends AsyncTask<Void, Integer, Void> {

	private List<DataRow> list;
	private SettingsFragment fragment;
	private MyProgressDialog progressDialog;
	private String oldSize;
	private String newSize;

	public RecalculateData(List<DataRow> list, SettingsFragment fragment, String oldSize, String newSize) {
		this.list = list;
		this.fragment = fragment;
		this.oldSize = oldSize;
		this.newSize = newSize;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new MyProgressDialog(fragment.getActivity());
		progressDialog.setMax(list.size());
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setTitle(R.string.settings_valueSizeProgressDialog_title);
		progressDialog.setIcon(R.drawable.ic_menu_save);
		progressDialog.setMessage(fragment.getActivity().getString(R.string.settings_valueSizeProgressDialog_message));
		progressDialog.show();
	}

	@Override
	protected Void doInBackground(Void... params) {
		int oldSizeId = 0;
		int newSizeId = 0;
		String[] sizes = fragment.getActivity().getResources().getStringArray(R.array.valueSizes);
		for (int i = 0; i < sizes.length; i++) {
			if (sizes[i].equals(oldSize)) {
				oldSizeId = i;
			}
			if (sizes[i].equals(newSize)) {
				newSizeId = i;
			}
		}
		double factor = Math.pow(1000, oldSizeId - newSizeId);
		for (int i = 0; i < list.size(); i++) {
			int newValue = (int) Math.round(factor * list.get(i).getValue());
			list.get(i).setValue(newValue);
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.DATA_COLUMN_VALUE, newValue);
			fragment.getActivity().getContentResolver()
			        .update(MyContentProvider.DATA_URI, values, MySQLiteHelper.DATA_COLUMN_ID + " = " + list.get(i).getId(), null);
			publishProgress(i + 1);
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		progressDialog.setProgress(values[0]);
	}

	@Override
	protected void onPostExecute(Void result) {
		EventBus.getInstance().fireDataChanged();
		progressDialog.dismiss();
	}

}
