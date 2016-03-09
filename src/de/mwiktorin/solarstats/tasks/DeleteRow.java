package de.mwiktorin.solarstats.tasks;

import android.content.Context;
import android.os.AsyncTask;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.model.DataRow;
import de.mwiktorin.solarstats.storage.MyContentProvider;
import de.mwiktorin.solarstats.storage.MySQLiteHelper;

public class DeleteRow extends AsyncTask<DataRow, Integer, Boolean> {

	private Context context;

	public DeleteRow(Context context) {
		this.context = context;
	}

	@Override
	protected Boolean doInBackground(DataRow... params) {
		context.getContentResolver().delete(MyContentProvider.DATA_URI, MySQLiteHelper.DATA_COLUMN_ID + " = " + params[0].getId(), null);
		EventBus.getData().remove(new DataRow(params[0].getDate(), 0, -1));
		return true;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		EventBus.getInstance().fireDataChanged();
	}

}
