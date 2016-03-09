package de.mwiktorin.solarstats.tasks;

import java.util.List;

import android.content.Context;
import android.os.AsyncTask;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.model.DataRow;

public class SaveData extends AsyncTask<DataRow, Integer, Boolean> {

	private Context context;
	private List<DataRow> list;

	public SaveData(Context context, List<DataRow> list) {
		this.context = context;
		this.list = list;
	}
	
	@Override
	protected Boolean doInBackground(DataRow... params) {
		Utils.saveData(context, params[0], list, false);
		return true;
	}
	
	@Override
	protected void onPostExecute(Boolean result) {
		EventBus.getInstance().fireDataChanged();
	}

}
