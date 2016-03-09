package de.mwiktorin.solarstats.tasks;

import android.content.Context;
import android.os.AsyncTask;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.Preferences;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.dialogs.MyProgressDialog;
import de.mwiktorin.solarstats.model.System;
import de.mwiktorin.solarstats.storage.MyContentProvider;
import de.mwiktorin.solarstats.storage.MySQLiteHelper;

public class DeleteSystem extends AsyncTask<System, Integer, Boolean> {

	private Context context;
	private MyProgressDialog progressDialog;

	public DeleteSystem(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new MyProgressDialog(context);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setMessage(context.getString(R.string.solarsystems_fragment_progressdialog_deleting));
		progressDialog.show();
	}
	
	@Override
	protected Boolean doInBackground(System... params) {
		context.getContentResolver().delete(MyContentProvider.SYSTEM_URI, MySQLiteHelper.SYSTEMS_COLUMN_ID + " = " + params[0].getId(), null);
		context.getContentResolver().delete(MyContentProvider.DATA_URI, MySQLiteHelper.DATA_COLUMN_SYSTEM + " = " + params[0].getId(), null);
		if(Preferences.getInstance(context).getLong(Preferences.CURRENT_SYSTEM_ID, 1) == params[0].getId()){
			EventBus.getData().clear();
			Preferences.getInstance(context).putLong(Preferences.CURRENT_SYSTEM_ID, EventBus.getSystems().get(0).getId());
		}
		EventBus.getSystems().remove(params[0]);
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		progressDialog.dismiss();
		EventBus.getInstance().fireDataChanged();
		EventBus.getInstance().fireSystemUpdate();
	}

}
