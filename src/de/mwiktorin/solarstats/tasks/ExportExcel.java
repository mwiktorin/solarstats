package de.mwiktorin.solarstats.tasks;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import android.os.AsyncTask;
import android.os.Environment;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.activities.MainActivity;
import de.mwiktorin.solarstats.dialogs.MyProgressDialog;
import de.mwiktorin.solarstats.fragments.DataTableFragment;
import de.mwiktorin.solarstats.model.DataRow;

public class ExportExcel extends AsyncTask<Void, Integer, File> {

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US);
	private List<DataRow> list;
	private DataTableFragment fragment;
	private MyProgressDialog progressDialog;

	public ExportExcel(DataTableFragment fragment, List<DataRow> list) {
		this.list = list;
		this.fragment = fragment;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new MyProgressDialog(fragment.getActivity());
		progressDialog.setMax(list.size());
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setTitle(R.string.data_fragment_export_progress_title);
		progressDialog.setIcon(R.drawable.ic_menu_save);
		progressDialog.setMessage(fragment.getActivity().getResources().getString(R.string.data_fragment_export_progress_message));
		progressDialog.show();
	}

	@Override
	protected File doInBackground(Void... params) {
		String fileName = String.format(fragment.getActivity().getString(R.string.data_fragment_export_excel_filename), DATE_FORMAT.format(new Date()));
		try {
			String folder = Environment.getExternalStorageDirectory() + File.separator + fragment.getActivity().getString(R.string.export_foldername)
			        + File.separator;
			new File(folder).mkdirs();
			File file = new File(folder + fileName);
			file.createNewFile();
			WritableWorkbook workbook = Workbook.createWorkbook(file);
			WritableSheet sheet = workbook.createSheet(fragment.getActivity().getString(R.string.data_fragment_export_excel_sheetname), 0);
			Label label = new Label(0, 0, fragment.getActivity().getString(R.string.data_fragment_export_excel_head1));
			sheet.addCell(label);
			label = new Label(1, 0, fragment.getActivity().getString(R.string.data_fragment_export_excel_head2));
			sheet.addCell(label);
			for (int i = 0; i < list.size(); i++) {
				label = new Label(0, i + 1, list.get(i).getStringDate());
				sheet.addCell(label);
				label = new Label(1, i + 1, list.get(i).getValue()
				        + " "
				        + Utils.getUnit(fragment.getActivity()));
				sheet.addCell(label);
				publishProgress(i);
			}
			workbook.write();
			workbook.close();
			return file;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		progressDialog.setProgress(values[0]);
	}

	@Override
	protected void onPostExecute(File result) {
		progressDialog.dismiss();
		((MainActivity) fragment.getActivity()).selectItem(3);
		Utils.openFile(result, fragment.getActivity());
	}

}
