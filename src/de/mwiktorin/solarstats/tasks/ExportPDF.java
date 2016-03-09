package de.mwiktorin.solarstats.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.ViewGroup.LayoutParams;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.activities.MainActivity;
import de.mwiktorin.solarstats.apwlibrary.PDFWriter;
import de.mwiktorin.solarstats.apwlibrary.PaperSize;
import de.mwiktorin.solarstats.dialogs.MyProgressDialog;
import de.mwiktorin.solarstats.fragments.DataTableFragment;
import de.mwiktorin.solarstats.model.DataRow;
import de.mwiktorin.solarstats.views.DiagramView;

public class ExportPDF extends AsyncTask<Boolean, Integer, File> {

	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault());
	private final static SimpleDateFormat DATE_TITLE_FORMAT = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
	private List<DataRow> list;
	private DataTableFragment fragment;
	private MyProgressDialog progressDialog;
	private String valueSize;

	public ExportPDF(DataTableFragment fragment, List<DataRow> list) {
		this.list = list;
		this.fragment = fragment;
		valueSize = Utils.getUnit(fragment.getActivity());
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
	protected File doInBackground(Boolean... params) {
		String fileName = String.format(fragment.getActivity().getString(R.string.data_fragment_export_pdf_filename), DATE_FORMAT.format(new Date()));
		try {
			String folder = Environment.getExternalStorageDirectory() + File.separator + fragment.getActivity().getString(R.string.export_foldername)
			        + File.separator;
			new File(folder).mkdirs();

			PDFWriter writer = new PDFWriter();

			String currentMonth = DATE_TITLE_FORMAT.format(list.get(1).getDate());
			drawTitle(writer, currentMonth);
			int j = 0;
			List<DataRow> monthList = new ArrayList<DataRow>();
			for (int i = 1; i < list.size(); i++) {
				if (!DATE_TITLE_FORMAT.format(list.get(i).getDate()).equals(currentMonth)) {
					drawMonthImage(writer, monthList, !(boolean) params[0]);
					writer.newPage();
					monthList.clear();
					currentMonth = DATE_TITLE_FORMAT.format(list.get(i).getDate());
					drawTitle(writer, currentMonth);
					j = 0;
				}
				writer.addText(50, PaperSize.A4_HEIGHT - 190 - j * 20, 12, list.get(i).getStringDate());
				writer.addText(120, PaperSize.A4_HEIGHT - 190 - j * 20, 12, list.get(i).getValue() + " " + valueSize);
				monthList.add(list.get(i));
				j++;
				publishProgress(i + 1);
			}
			drawMonthImage(writer, monthList, !(boolean) params[0]);

			int pageCount = writer.getPageCount();
			for (int i = 0; i < pageCount; i++) {
				writer.setCurrentPage(i);
				writer.addText(PaperSize.A4_WIDTH - 30, 20, 8, (i + 1) + " / " + pageCount);
				writer.addText(20, 20, 8, fragment.getActivity().getString(R.string.data_fragment_export_pdf_apwcopyright));
			}

			String pdfString = writer.asString();
			File file = new File(folder + fileName);
			file.createNewFile();
			FileOutputStream pdfFile = new FileOutputStream(file);
			pdfFile.write(pdfString.getBytes("ISO-8859-1"));
			pdfFile.close();
			return file;

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void drawMonthImage(PDFWriter writer, List<DataRow> monthList, boolean onlyMonthValue) {
		int beforeIndex = list.indexOf(monthList.get(0)) - 1;
		if (beforeIndex < 0) {
			monthList.add(0, null);
		} else {
			monthList.add(0, list.get(beforeIndex));
		}
		String monthValue = "";
		if (monthList.get(0) == null) {
			monthValue = monthList.get(monthList.size() - 1).getValue() + " " + valueSize;
		} else {
			monthValue = (monthList.get(monthList.size() - 1).getValue() - monthList.get(0).getValue()) + " " + valueSize;
		}
		writer.addText(250, PaperSize.A4_HEIGHT - 230, 30, monthValue);
		if (onlyMonthValue) {
			return;
		}
		if (monthList.size() > 4) {
			DiagramView view = new DiagramView(fragment.getActivity());
			view.setLayoutParams(new LayoutParams(500, 500));
			view.setNormalData(DiagramView.TYPE_NORMAL, new int[] { fragment.getActivity().getResources().getColor(R.color.dark_orange) }, monthList);
			Bitmap viewBitmap = Bitmap.createBitmap(view.getLayoutParams().width, view.getLayoutParams().height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(viewBitmap);
			view.layout(0, 0, view.getLayoutParams().width, view.getLayoutParams().height);
			canvas.drawColor(Color.WHITE);
			view.draw(canvas);
			writer.addImageKeepRatio(250, 296, 250, 250, viewBitmap);
			viewBitmap.recycle();
		}
	}

	private void drawTitle(PDFWriter writer, String currentMonth) {
		writer.addText(50, PaperSize.A4_HEIGHT - 100, 30,
		        String.format(fragment.getActivity().getString(R.string.data_fragment_export_pdf_title), currentMonth));
		writer.addText(50, PaperSize.A4_HEIGHT - 130, 15, fragment.getActivity().getString(R.string.data_fragment_export_pdf_subtitle));
		writer.addText(50, PaperSize.A4_HEIGHT - 145, 10, fragment.getActivity().getString(R.string.data_fragment_export_pdf_homepage));
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
