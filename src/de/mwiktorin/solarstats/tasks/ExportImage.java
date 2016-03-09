package de.mwiktorin.solarstats.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.ViewGroup.LayoutParams;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.activities.MainActivity;
import de.mwiktorin.solarstats.dialogs.MyProgressDialog;
import de.mwiktorin.solarstats.fragments.DiagramFragment;
import de.mwiktorin.solarstats.views.DiagramView;

public class ExportImage extends AsyncTask<Void, Integer, File> {

	private static final int TOP_HEIGHT_PX = 100;
	private static final int BOTTOM_HEIGHT_PX = 50;
	private static final int TOP_TEXTSIZE = 70;
	private static final int BOTTOM_TEXTSIZE = 20;

	private DiagramFragment fragment;
	private DiagramView diagram;
	private MyProgressDialog progressDialog;
	private int width;
	private int height;
	private String title;

	public ExportImage(DiagramFragment fragment, int width, int height, DiagramView diagramm, String title) {
		this.fragment = fragment;
		this.width = width;
		this.height = height;
		this.diagram = diagramm;
		this.title = title;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new MyProgressDialog(fragment.getActivity());
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setTitle(R.string.diagramm_fragment_export_progress_title);
		progressDialog.setIcon(R.drawable.ic_menu_save);
		progressDialog.setMessage(fragment.getActivity().getResources().getString(R.string.diagramm_fragment_export_progress_message));
		progressDialog.show();
	}

	@Override
	protected File doInBackground(Void... params) {
		String titleFile = title.replace(" ", "_").replace('.', '-').replace("_|_", "|").replace("_-_", "_");
		String fileName = String.format(fragment.getActivity().getString(R.string.diagramm_fragment_export_filename), titleFile);
		DiagramView view = new DiagramView(fragment.getActivity());
		view.setLayoutParams(new LayoutParams(width, height - TOP_HEIGHT_PX - BOTTOM_HEIGHT_PX));
		switch (diagram.getType()) {
		case DiagramView.TYPE_NORMAL:
			view.setNormalData(DiagramView.TYPE_NORMAL, diagram.getColors(), diagram.getList());
			break;
		case DiagramView.TYPE_ONE_YEAR:
			view.setNormalData(DiagramView.TYPE_ONE_YEAR, diagram.getColors(), diagram.getList());
			break;
		case DiagramView.TYPE_MORE_YEARS:
			view.setNormalData(DiagramView.TYPE_MORE_YEARS, diagram.getColors(), diagram.getList());
			break;
		case DiagramView.TYPE_YEAR_COMPARISON:
			view.setYearComparisonData(diagram.getYear1List(), diagram.getYear2List());
			break;
		}
		try {
			String folder = Environment.getExternalStorageDirectory() + File.separator + fragment.getActivity().getString(R.string.export_foldername)
			        + File.separator;
			new File(folder).mkdirs();
			File file = new File(folder + fileName);
			int i = 1;
			while (file.exists()) {
				fileName = String.format(fragment.getActivity().getString(R.string.diagramm_fragment_export_filename), titleFile + "_" + i);
				file = new File(folder + fileName);
				i++;
			}
			file.createNewFile();

			Bitmap viewBitmap = Bitmap.createBitmap(view.getLayoutParams().width, view.getLayoutParams().height, Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(viewBitmap);
			view.layout(0, 0, view.getLayoutParams().width, view.getLayoutParams().height);
			canvas.drawColor(Color.WHITE);
			view.draw(canvas);

			Paint textPaint = new Paint();
			textPaint.setColor(Color.BLACK);
			textPaint.setStyle(Style.FILL);
			textPaint.setTextSize(TOP_TEXTSIZE);
			textPaint.setAntiAlias(true);
			textPaint.setSubpixelText(true);

			Bitmap topBitmap = Bitmap.createBitmap(view.getLayoutParams().width, TOP_HEIGHT_PX, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(topBitmap);
			canvas.drawColor(Color.WHITE);
			Rect bounds = new Rect();
			textPaint.getTextBounds(title, 0, title.length(), bounds);
			canvas.drawText(title, topBitmap.getWidth() / 2 - bounds.right / 2, TOP_HEIGHT_PX / 2 - bounds.top / 2, textPaint);

			textPaint.setTextSize(BOTTOM_TEXTSIZE);
			Bitmap bottomBitmap = Bitmap.createBitmap(view.getLayoutParams().width, BOTTOM_HEIGHT_PX, Bitmap.Config.ARGB_8888);
			canvas = new Canvas(bottomBitmap);
			canvas.drawColor(Color.WHITE);
			bounds = new Rect();
			String left = fragment.getActivity().getString(R.string.diagramm_fragment_export_text_left);
			String right = fragment.getActivity().getString(R.string.diagramm_fragment_export_text_right);
			textPaint.getTextBounds(left, 0, left.length(), bounds);
			canvas.drawText(left, 10, BOTTOM_HEIGHT_PX / 2 - bounds.top / 2, textPaint);
			textPaint.getTextBounds(right, 0, right.length(), bounds);
			canvas.drawText(right, bottomBitmap.getWidth() - 10 - bounds.right, BOTTOM_HEIGHT_PX / 2 - bounds.top / 2, textPaint);

			Bitmap finalBitmap = Bitmap.createBitmap(topBitmap.getWidth(), viewBitmap.getHeight() + topBitmap.getHeight() + bottomBitmap.getHeight(),
			        Bitmap.Config.ARGB_8888);
			canvas = new Canvas(finalBitmap);
			canvas.drawBitmap(topBitmap, 0, 0, new Paint());
			canvas.drawBitmap(viewBitmap, 0, TOP_HEIGHT_PX, new Paint());
			canvas.drawBitmap(bottomBitmap, 0, TOP_HEIGHT_PX + viewBitmap.getHeight(), new Paint());

			finalBitmap.compress(CompressFormat.PNG, 100, new FileOutputStream(file));
			
			return file;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(File result) {
		progressDialog.dismiss();
		((MainActivity) fragment.getActivity()).selectItem(3);
		Utils.openFile(result, fragment.getActivity());
	}

}
