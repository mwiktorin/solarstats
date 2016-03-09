package de.mwiktorin.solarstats.tasks;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.activities.MainActivity;
import de.mwiktorin.solarstats.dialogs.FoundImportDataFragment;
import de.mwiktorin.solarstats.dialogs.MyAlertDialog;
import de.mwiktorin.solarstats.dialogs.MyProgressDialog;
import de.mwiktorin.solarstats.fragments.DataTableFragment;
import de.mwiktorin.solarstats.model.DataRow;

public class FindImportData extends AsyncTask<String, Integer, Void> {

	private SimpleDateFormat DATE_FORMAT_STRING_EXCEL;
	private List<DataRow> list;
	private DataTableFragment fragment;
	private MyProgressDialog progressDialog;
	private SaveImportData saveTask;
	private boolean dataInRange;
	private boolean deleteOldData;
	private List<DataRow> importList;
	private boolean foundDate;
	private DataRow firstRow;
	private DataRow lastRow;
	private String valueSize;

	public FindImportData(List<DataRow> list, DataTableFragment fragment) {
		this.list = list;
		this.fragment = fragment;
		
		if(Locale.getDefault().equals(Locale.US)){
			DATE_FORMAT_STRING_EXCEL = new SimpleDateFormat("M/dd/yy", Locale.getDefault());
		} else {
			if(Locale.getDefault().equals(Locale.GERMANY) || Locale.getDefault().equals(Locale.GERMAN)){
				DATE_FORMAT_STRING_EXCEL = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
			} else {
				DATE_FORMAT_STRING_EXCEL = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
			}
		}
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new MyProgressDialog(fragment.getActivity());
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setTitle(R.string.data_fragment_import_excel_dialog_title);
		progressDialog.setIcon(R.drawable.ic_menu_search);
		progressDialog.setMessage(fragment.getActivity().getResources().getString(R.string.data_fragment_import_excel_progress1));
		progressDialog.show();
	}

	@Override
	protected Void doInBackground(String... params) {
		importList = new ArrayList<DataRow>();
		File inputWorkbook = new File(params[0]);
		try {
			Sheet sheet = Workbook.getWorkbook(inputWorkbook).getSheet(0);
			int row = -1;
			int column = 0;
			foundDate = false;
			while (row < sheet.getRows() - 1 && !foundDate) {
				row++;
				column = 0;
				while (column < sheet.getColumns() && !foundDate) {
					try {
						DATE_FORMAT_STRING_EXCEL.parse(sheet.getCell(column, row).getContents());
						foundDate = true;
					} catch (ParseException e) {
						column++;
					}
				}
			}
			if (foundDate) {
				String[] sizes = fragment.getActivity().getResources().getStringArray(R.array.valueSizes);
				for (int i = row; i < sheet.getRows(); i++) {
					try {
						Date date = DATE_FORMAT_STRING_EXCEL.parse(sheet.getCell(column, i).getContents());
						String cellContent = sheet.getCell(column + 1, i).getContents();
						if (valueSize == null) {
							for (int j = 0; j < sizes.length; j++) {
								if (cellContent.contains(sizes[j])) {
									valueSize = sizes[j];
								}
							}
						}
						int value = Integer.parseInt(cellContent.replaceAll("[\\D]", ""));
						if (importList.size() > 0
						        && (importList.get(importList.size() - 1).getValue() > value || !importList.get(importList.size() - 1).getDate()
						                .before(date))) {
							break;
						}
						importList.add(new DataRow(date, value, -1));
					} catch (Exception e) {
						// break;
					}
				}
				deleteOldData = false;

				firstRow = importList.get(0);
				lastRow = importList.get(importList.size() - 1);
				if (list.size() > 0) {
					int beforeFirstIndex = list.indexOf(firstRow) == -1 ? Utils.getBeforeDateIndex(firstRow.getDate(), list)
					        : list.indexOf(firstRow) - 1;
					if (beforeFirstIndex > 0 && list.get(beforeFirstIndex).getValue() > firstRow.getValue()) {
						deleteOldData = true;
					}
					int afterLastIndex = list.indexOf(lastRow) == -1 ? Utils.getBeforeDateIndex(lastRow.getDate(), list) + 1 : list.indexOf(lastRow);
					if (afterLastIndex < list.size() && afterLastIndex >= 0 && list.get(afterLastIndex).getValue() < lastRow.getValue()) {
						deleteOldData = true;
					}
				}

				dataInRange = false;
				if (deleteOldData) {
					dataInRange = true;
				} else {
					for (DataRow dataRow : list) {
						if (!dataRow.getDate().after(lastRow.getDate()) && !dataRow.getDate().before(firstRow.getDate())) {
							dataInRange = true;
							break;
						}
					}
				}
			}
		} catch (BiffException e) {
			Toast.makeText(fragment.getActivity(), R.string.data_fragment_toast_couldntreadfile, Toast.LENGTH_LONG).show();
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	protected void onCancelled() {
		if (saveTask != null) {
			saveTask.cancel(true);
		}
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(Void result) {
		progressDialog.dismiss();
		if (foundDate) {
			String messageSecond = deleteOldData ? fragment.getActivity().getResources().getString(R.string.data_fragment_import_excel_dialog_message2)
			        : fragment.getActivity().getResources().getString(R.string.data_fragment_import_excel_dialog_message1);
			if (!dataInRange) {
				messageSecond = null;
			}
			final FoundImportDataFragment dialogFragment = new FoundImportDataFragment();
			dialogFragment.setValues(importList.size(), firstRow.getStringDate(), firstRow.getValue(), lastRow.getStringDate(), lastRow.getValue(),
			        valueSize, messageSecond, new OnClickListener() {
				        @Override
				        public void onClick(View v) {
					        saveTask = new SaveImportData(list, importList, fragment, deleteOldData, dialogFragment.getSelectedItem());
					        saveTask.execute();
				        }
			        });
			dialogFragment.show(fragment.getActivity().getFragmentManager(), "foundImportDataDialog");
		} else {
			MyAlertDialog builder = new MyAlertDialog(fragment.getActivity());
			builder.setTitle(R.string.data_fragment_import_excel_nodata_dialog_title);
			builder.setMessage(R.string.data_fragment_import_excel_nodata_dialog_message);
			builder.setIcon(R.drawable.ic_menu_info_details);
			builder.setMyNegativeButton(R.string.data_fragment_import_excel_nodata_dialog_negative, null);
			builder.setMyPositiveButton(R.string.data_fragment_import_excel_nodata_dialog_positive, new OnClickListener() {
				@Override
				public void onClick(View v) {
					((MainActivity) fragment.getActivity()).selectItem(4);
				}
			});
			builder.show();
		}
	}

}
