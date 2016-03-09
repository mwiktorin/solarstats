package de.mwiktorin.solarstats.tasks;

import java.util.List;

import android.os.AsyncTask;
import de.mwiktorin.solarstats.Preferences;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.dialogs.MyProgressDialog;
import de.mwiktorin.solarstats.fragments.DataTableFragment;
import de.mwiktorin.solarstats.model.DataRow;
import de.mwiktorin.solarstats.storage.MyContentProvider;
import de.mwiktorin.solarstats.storage.MySQLiteHelper;

public class SaveImportData extends AsyncTask<String, Integer, Boolean> {

	private List<DataRow> list;
	private List<DataRow> importList;
	private DataTableFragment fragment;
	private boolean deleteOldData;
	private int valueSizePos;
	private MyProgressDialog progressDialog;

	public SaveImportData(List<DataRow> list, List<DataRow> importList, DataTableFragment fragment, boolean deleteOldData, int valueSizePos) {
		this.list = list;
		this.importList = importList;
		this.fragment = fragment;
		this.deleteOldData = deleteOldData;
		this.valueSizePos = valueSizePos;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new MyProgressDialog(fragment.getActivity());
		progressDialog.setMax(importList.size());
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setTitle(R.string.data_fragment_import_excel_dialog_title);
		progressDialog.setIcon(R.drawable.ic_menu_save);
		progressDialog.setMessage(fragment.getActivity().getResources().getString(R.string.data_fragment_import_excel_progress2));
		progressDialog.show();
	}

	@Override
	protected Boolean doInBackground(String... params) {
		String[] sizes = fragment.getActivity().getResources().getStringArray(R.array.valueSizes);
		if (deleteOldData || list.size() == 0) {
			Preferences.getInstance(fragment.getActivity()).putString(fragment.getActivity().getString(R.string.prefkey_valueSize),
			        sizes[valueSizePos]);
		}
		if (deleteOldData) {
			fragment.getActivity()
			        .getContentResolver()
			        .delete(MyContentProvider.DATA_URI,
			                MySQLiteHelper.DATA_COLUMN_SYSTEM + " = '"
			                        + Preferences.getInstance(fragment.getActivity()).getLong(Preferences.CURRENT_SYSTEM_ID, 1) + "'", null);
			list.clear();
		} else {
			for (int i = 0; i < list.size(); i++) {
				if (!list.get(i).getDate().after(importList.get(importList.size() - 1).getDate())
				        && !list.get(i).getDate().before(importList.get(0).getDate())) {
					list.remove(list.get(i));
					i--;
				}
			}
			fragment.getActivity()
			        .getContentResolver()
			        .delete(MyContentProvider.DATA_URI,
			                MySQLiteHelper.DATA_COLUMN_DATE + " >= '" + importList.get(0).getDatabaseDate() + "' AND "
			                        + MySQLiteHelper.DATA_COLUMN_DATE + " <= '" + importList.get(importList.size() - 1).getDatabaseDate() + "' AND "
			                        + MySQLiteHelper.DATA_COLUMN_SYSTEM + " = '"
			                        + Preferences.getInstance(fragment.getActivity()).getLong(Preferences.CURRENT_SYSTEM_ID, 1) + "'", null);
		}
		String newSize = Utils.getUnit(fragment.getActivity());
		int newSizeId = 0;
		for (int i = 0; i < sizes.length; i++) {
			if (sizes[i].equals(newSize)) {
				newSizeId = i;
			}
		}
		double factor = Math.pow(1000, valueSizePos - newSizeId);
		for (int i = 0; i < importList.size(); i++) {
			if (fragment.getActivity() == null) {
				return false;
			}
			importList.get(i).setValue((int) Math.round(importList.get(i).getValue() * factor));
			Utils.saveData(fragment.getActivity().getApplicationContext(), importList.get(i), list, false);
			publishProgress(i + 1);
		}
		return true;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		progressDialog.setProgress(values[0]);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		fragment.updateUI();
		progressDialog.dismiss();
	}

}
