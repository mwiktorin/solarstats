package de.mwiktorin.solarstats.views;

import java.util.HashMap;
import java.util.List;

import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

public class PercentBarView extends View {

	public static final String KEY_VALUE = "value";
	public static final String KEY_NAME = "name";
	public static final String KEY_COLOR = "color";

	private Paint barPaint;
	private Paint textPaint;
	private Rect bounds = new Rect();
	private List<HashMap<String, Object>> data;

	public PercentBarView(Context context) {
		super(context);
		init();
	}

	public PercentBarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public PercentBarView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	private void init() {
		barPaint = new Paint();
		barPaint.setStrokeWidth(1);
		barPaint.setAntiAlias(true);
		barPaint.setSubpixelText(true);

		textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
		textPaint.setStyle(Style.FILL);
		textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics()));
		textPaint.setAntiAlias(true);
		textPaint.setSubpixelText(true);
	}

	public void setData(List<HashMap<String, Object>> data) {
		this.data = data;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (data == null) {
			String text = getContext().getString(R.string.solarsystems_fragment_no_data);
			textPaint.getTextBounds(text, 0,
			        text.length(), bounds);
			canvas.drawText(text, getWidth() / 2 - bounds.right / 2, getHeight() / 2 - bounds.bottom / 2, textPaint);
			return;
		}
		long startX = 0;
		for (HashMap<String, Object> map : data) {
			long endX = Math.round(startX + ((Double) map.get(KEY_VALUE) * getWidth()));
			barPaint.setColor((Integer) map.get(KEY_COLOR));
			canvas.drawRect(startX, 0, endX, getHeight(), barPaint);
			String text = (Utils.getIntString((Double) map.get(KEY_VALUE) * 100)) + " %";
			drawText(canvas, startX, endX, 0, getHeight() / 2,  text);
			text = (String) map.get(KEY_NAME);
			drawText(canvas, startX, endX, getHeight() / 2, getHeight(), text);
			textPaint.setTextScaleX(1);
			startX = endX;
		}
	}

	private void drawText(Canvas canvas, long startX, long endX, long startY, long endY, String text) {
	    textPaint.getTextBounds(text, 0, text.length(), bounds);
	    while (bounds.right > endX - startX) {
	    	textPaint.setTextScaleX(textPaint.getTextScaleX() - 0.01f);
	    	textPaint.getTextBounds(text, 0, text.length(), bounds);
	    }
	    canvas.drawText(text, startX + (endX - startX) / 2 - bounds.right / 2, startY + (endY - startY) / 2 - bounds.top / 2, textPaint);
    }
}
