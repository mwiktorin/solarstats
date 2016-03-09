package de.mwiktorin.solarstats.tasks;

import java.util.ArrayList;

import android.os.AsyncTask;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.Preferences;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.dialogs.MyProgressDialog;
import de.mwiktorin.solarstats.fragments.SettingsFragment;
import de.mwiktorin.solarstats.model.DataRow;
import de.mwiktorin.solarstats.storage.MyContentProvider;

public class DeleteAllData extends AsyncTask<DataRow, Integer, Boolean> {

	private SettingsFragment fragment;
	private MyProgressDialog progressDialog;

	public DeleteAllData(SettingsFragment fragment) {
		this.fragment = fragment;
	}
	
	@Override
	protected void onPreExecute() {
	    progressDialog = new MyProgressDialog(fragment.getActivity());
	    progressDialog.setCancelable(false);
	    progressDialog.setCanceledOnTouchOutside(false);
	    progressDialog.setMessage(fragment.getString(R.string.settings_deleteDilaog_progress));
	    progressDialog.show();
	}

	@Override
	protected Boolean doInBackground(DataRow... params) {
		fragment.getActivity().getContentResolver().delete(MyContentProvider.DATA_URI, null, null);
		Preferences.getInstance(fragment.getActivity()).deleteAllDiagrammSettings();
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		EventBus.getInstance().setData(new ArrayList<DataRow>());
		EventBus.getInstance().fireDataChanged();
		progressDialog.dismiss();
	}

}
