package de.mwiktorin.solarstats.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.model.DataRow;

public class DiagramView extends View implements OnGestureListener {

	public static final int TYPE_NORMAL = 1;
	public static final int TYPE_ONE_YEAR = 2;
	public static final int TYPE_MORE_YEARS = 3;
	public static final int TYPE_YEAR_COMPARISON = 4;

	private static final int LINE_STROKE_WIDTH = 7;
	private static final int HINT_LINE_STROKE_WIDTH = 3;
	private static final int INDICATOR_STROKE_WIDTH = 3;
	private static final int INDICATOR_LENGTH = 20;
	private static final int DATE_MARGIN = 20;
	private static final int MIN_COLUMN_MARGIN = 20;
	private static final String GRAPH_COLOR = "#FF8800";
	private static final String GRAPH_YEAR1_COLOR = "#CC0000";
	private static final String GRAPH_YEAR2_COLOR = "#669900";
	private static final int COLUMN_WIDTH = 100; // in dp
	private static final int PADDING = 20; // in dp
	private static final int DESCRIPTION_MARGIN = 20; // in dp
	private static final int AXES_TEXT_SIZE = 12; // in sp
	private static final int NO_DATA_TEXT_SIZE = 20; // in sp

	private static final SimpleDateFormat DATE_FORMAT_STRING = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
	private static final SimpleDateFormat DATE_FORMAT_SHORTMONTH = new SimpleDateFormat("MMM", Locale.getDefault());
	private static final SimpleDateFormat DATE_FORMAT_YEAR = new SimpleDateFormat("yyyy", Locale.getDefault());

	private int type;
	private int paddingPX;
	private int descriptionMarginPX;
	private int axesTextSizePX;
	private int noDataTextSizePX;
	private int columnWidthPX;
	private float touchX;
	private List<DataRow>[] list;
	private List<DataRow>[] dataList;
	private int[] colors;
	private List<DataRow> year1List;
	private List<DataRow> year2List;
	private List<DataRow> moreYearsList;
	private List<Integer>[] diffList;
	private List<Integer> monthValues;
	private List<Integer> year1Values;
	private List<Integer> year2Values;
	private List<Integer> moreYearsValues;
	private Paint linePaint = new Paint();
	private Paint hintLinePaint = new Paint();
	private Paint graphPaint = new Paint();
	private Paint indicatorPaint = new Paint();
	private Paint textPaint = new Paint();
	private Paint bigTextPaint = new Paint();
	private int height;
	private int width;
	private int dayDiff;
	private int maxValue;
	private double dayWidth;
	private double columnSpace;
	private Rect bounds = new Rect();
	private int yAxeHeight;

	public DiagramView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public DiagramView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public DiagramView(Context context) {
		super(context);
		init();
	}

	private void init() {
		paddingPX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING, getResources().getDisplayMetrics());
		descriptionMarginPX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DESCRIPTION_MARGIN, getResources().getDisplayMetrics());
		axesTextSizePX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, AXES_TEXT_SIZE, getResources().getDisplayMetrics());
		noDataTextSizePX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, NO_DATA_TEXT_SIZE, getResources().getDisplayMetrics());
		columnWidthPX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, COLUMN_WIDTH, getResources().getDisplayMetrics());

		linePaint.setColor(Color.BLACK);
		linePaint.setStrokeWidth(LINE_STROKE_WIDTH);
		linePaint.setStrokeCap(Paint.Cap.ROUND);

		hintLinePaint.setColor(Color.GRAY);
		hintLinePaint.setStrokeWidth(HINT_LINE_STROKE_WIDTH);
		hintLinePaint.setAlpha(60);
		hintLinePaint.setStrokeCap(Paint.Cap.ROUND);

		graphPaint.setColor(Color.parseColor(GRAPH_COLOR));
		graphPaint.setStrokeWidth(LINE_STROKE_WIDTH);
		graphPaint.setStrokeCap(Paint.Cap.ROUND);
		graphPaint.setAntiAlias(true);

		indicatorPaint.setColor(Color.BLACK);
		indicatorPaint.setStrokeWidth(INDICATOR_STROKE_WIDTH);
		indicatorPaint.setStrokeCap(Paint.Cap.ROUND);

		textPaint.setColor(Color.BLACK);
		textPaint.setStyle(Style.FILL);
		textPaint.setTextSize(axesTextSizePX);
		textPaint.setAntiAlias(true);
		textPaint.setSubpixelText(true);

		bigTextPaint.setColor(Color.BLACK);
		bigTextPaint.setStyle(Style.FILL);
		bigTextPaint.setTextSize(noDataTextSizePX);
		bigTextPaint.setAntiAlias(true);
		bigTextPaint.setSubpixelText(true);

		try {
			final GestureDetector mDetector = new GestureDetector(getContext(), this);

			this.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					return mDetector.onTouchEvent(event);
				}
			});
		} catch (RuntimeException e) {
		}
	}

	/**
	 * 
	 * @param list
	 *            Data to show. The first item will not be shown, may be null
	 * @param type
	 *            NORMAL, ONE_YEAR, MORE_YEARS
	 */
	public void setNormalData(int type, int[] colors, List<DataRow>... dataList) {
		this.type = type;
		this.dataList = dataList;
		this.colors = colors;
		if (dataList[0].size() < 2) {
			list = new List[1];
			list[0] = new ArrayList<DataRow>();
			invalidate();
			return;
		}
		
		list = new List[dataList.length];
		diffList = new List[dataList.length];
		
		for (int j = 0; j < dataList.length; j++) {
			if (dataList[j].size() < 2) {
				list[j] = new ArrayList<DataRow>();
				diffList[j] = new ArrayList<Integer>();
			} else {
				list[j] = getFullList(dataList[j]);
				diffList[j] = getDiffList(dataList[j].get(0), list[j]);
			}
		}
		switch (type) {
		case TYPE_NORMAL:
			dayDiff = Utils.getDayDiff(list[0].get(0).getDate(), list[0].get(list[0].size() - 1).getDate());
			maxValue = getMaxValue(diffList);
			break;
		case TYPE_ONE_YEAR:
			monthValues = new ArrayList<Integer>();
			for (int i = 0; i < 12; i++) {
				monthValues.add(0);
			}
			Calendar calendar = Calendar.getInstance();
			for (int i = 0; i < diffList[0].size(); i++) {
				calendar.setTime(list[0].get(i).getDate());
				monthValues.set(calendar.get(Calendar.MONTH), monthValues.get(calendar.get(Calendar.MONTH)) + diffList[0].get(i));
			}
			maxValue = getMaxValue(monthValues);
			break;
		case TYPE_MORE_YEARS:
			moreYearsList = new ArrayList<DataRow>();
			moreYearsList.add(list[0].get(0));
			moreYearsValues = new ArrayList<Integer>();
			moreYearsValues.add(diffList[0].get(0));
			for (int i = 1; i < list[0].size(); i++) {
				if (DATE_FORMAT_YEAR.format(list[0].get(i).getDate()).equals(DATE_FORMAT_YEAR.format(list[0].get(i - 1).getDate()))) {
					moreYearsValues.set(moreYearsValues.size() - 1, moreYearsValues.get(moreYearsValues.size() - 1) + diffList[0].get(i));
				} else {
					moreYearsList.add(list[0].get(i));
					moreYearsValues.add(diffList[0].get(i));
				}
			}
			maxValue = getMaxValue(moreYearsValues);
			break;
		}
		invalidate();
	}

	public void setYearComparisonData(List<DataRow> year1dataList, List<DataRow> year2dataList) {
		type = TYPE_YEAR_COMPARISON;
		if (year1dataList.size() < 2 || year2dataList.size() < 2) {
			list = new List[1];
			list[0] = new ArrayList<DataRow>();
			invalidate();
			return;
		}
		year1List = getFullList(year1dataList);
		year2List = getFullList(year2dataList);
		List<Integer> diffListYear1 = getDiffList(year1dataList.get(0), year1List);
		List<Integer> diffListYear2 = getDiffList(year2dataList.get(0), year2List);
		year1Values = new ArrayList<Integer>();
		for (int i = 0; i < 12; i++) {
			year1Values.add(0);
		}
		year2Values = new ArrayList<Integer>();
		for (int i = 0; i < 12; i++) {
			year2Values.add(0);
		}
		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < diffListYear1.size(); i++) {
			cal.setTime(year1List.get(i).getDate());
			year1Values.set(cal.get(Calendar.MONTH), year1Values.get(cal.get(Calendar.MONTH)) + diffListYear1.get(i));
		}
		for (int i = 0; i < diffListYear2.size(); i++) {
			cal.setTime(year2List.get(i).getDate());
			year2Values.set(cal.get(Calendar.MONTH), year2Values.get(cal.get(Calendar.MONTH)) + diffListYear2.get(i));
		}
		List<Integer> helpList = new ArrayList<Integer>();
		helpList.addAll(year1Values);
		helpList.addAll(year2Values);
		maxValue = getMaxValue(helpList);
		list = new List[1];
		list[0] = new ArrayList<DataRow>();
		for (int i = 1; i < year1List.size(); i++) {
			list[0].add(year1List.get(i));
		}
		for (int i = 1; i < year2List.size(); i++) {
			list[0].add(year2List.get(i));
		}
		invalidate();
	}

	/**
	 * @param dataList
	 *            Doesn't insert the first DataRow in dataList
	 * @return the dataList without spaces between days
	 */
	private List<DataRow> getFullList(List<DataRow> dataList) {
		List<DataRow> list = new ArrayList<DataRow>();
		list.add(dataList.get(1));
		for (int i = 2; i < dataList.size(); i++) {
			int dayDiff = Utils.getDayDiff(dataList.get(i - 1).getDate(), dataList.get(i).getDate());
			if (dayDiff > 1) {
				double value = (double) (dataList.get(i).getValue() - dataList.get(i - 1).getValue()) / dayDiff;
				Date newDate = Utils.addDays(dataList.get(i - 1).getDate(), 1);
				int j = 1;
				while (newDate.before(dataList.get(i).getDate())) {
					list.add(new DataRow(newDate, (int) (dataList.get(i - 1).getValue() + Math.round(j * value)), -1));
					newDate = Utils.addDays(newDate, 1);
					j++;
				}
			}
			list.add(dataList.get(i));
		}
		return list;
	}

	private List<Integer> getDiffList(DataRow firstValue, List<DataRow> list) {
		List<Integer> diffList = new ArrayList<Integer>();
		for (int i = 0; i < list.size(); i++) {
			if (i == 0) {
				if (firstValue != null) {
					int dayDiff = Utils.getDayDiff(firstValue.getDate(), list.get(0).getDate());
					diffList.add((int) Math.round((double) (list.get(0).getValue() - firstValue.getValue()) / dayDiff));
				} else {
					diffList.add(list.get(0).getValue());
				}
			} else {
				diffList.add(list.get(i).getValue() - list.get(i - 1).getValue());
			}
		}
		return diffList;
	}

	public int getType() {
		return type;
	}

	public List<DataRow>[] getList() {
		return dataList;
	}
	
	public int[] getColors() {
	    return colors;
    }

	public List<DataRow> getYear1List() {
		return year1List;
	}

	public List<DataRow> getYear2List() {
		return year2List;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		width = canvas.getWidth();
		height = canvas.getHeight();

		if (list == null) {
			String text = getContext().getResources().getString(R.string.diagramm_fragment_view_text_loading);
			bigTextPaint.getTextBounds(text, 0, text.length(), bounds);
			canvas.drawText(text, width / 2 - bounds.right / 2, height / 2 - bounds.top, bigTextPaint);
			return;
		}

		if (list[0].size() < 2) {
			String text = getContext().getResources().getString(R.string.diagramm_fragment_view_text_no_data);
			bigTextPaint.getTextBounds(text, 0, text.length(), bounds);
			canvas.drawText(text, width / 2 - bounds.right / 2, height / 2 - bounds.top, bigTextPaint);
			return;
		}

		yAxeHeight = height - 2 * paddingPX - descriptionMarginPX;

		switch (type) {
		case TYPE_NORMAL:
			dayWidth = (double) (width - 2 * paddingPX - descriptionMarginPX - descriptionMarginPX / 2) / dayDiff;
			drawNormalAxes(canvas);
			drawGraph(canvas);
			if (touchXIndiagramm()) {
				drawNormalTouch(canvas);
			}
			break;
		case TYPE_ONE_YEAR:
			calculateColumns(12);
			drawOneYearAxes(canvas);
			drawOneYearColumns(canvas);
			break;
		case TYPE_YEAR_COMPARISON:
			calculateColumns(12);
			drawOneYearAxes(canvas);
			drawYearComparisonColumns(canvas);
			drawYearColorIndicators(canvas);
			break;
		case TYPE_MORE_YEARS:
			calculateColumns(moreYearsValues.size());
			drawMoreYearsAxes(canvas);
			drawMoreYearsColumns(canvas);
			break;
		}
	}

	private void drawNormalTouch(Canvas canvas) {
		float findX = touchX - paddingPX - descriptionMarginPX;
		long i = Math.round(findX / dayWidth);
		float x = (float) (paddingPX + descriptionMarginPX + i * dayWidth);
		hintLinePaint.setColor(Color.RED);
		canvas.drawLine(x, paddingPX, x, height - paddingPX - descriptionMarginPX, hintLinePaint);

		Date date = Utils.addDays(list[0].get(0).getDate(), (int) i);
		String dateString = DATE_FORMAT_STRING.format(date);
		drawDate(dateString, x, canvas);

		int value = diffList[0].get(list[0].indexOf(new DataRow(date, 0, -1)));
		int y = height - paddingPX - descriptionMarginPX - (int) (((double) value / maxValue) * yAxeHeight);
		canvas.drawLine(paddingPX + descriptionMarginPX, y, width - paddingPX - descriptionMarginPX / 2, y, hintLinePaint);
		drawValue(y, canvas, value + "");
		hintLinePaint.setColor(Color.GRAY);
		hintLinePaint.setAlpha(60);
	}

	private void calculateColumns(int columns) {
		columnWidthPX = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, COLUMN_WIDTH, getResources().getDisplayMetrics());
		do {
			columnSpace = (double) ((width - 2 * paddingPX - descriptionMarginPX - descriptionMarginPX / 2) - columns * columnWidthPX)
			        / (columns + 1);
			columnWidthPX--;
		} while (columnSpace < MIN_COLUMN_MARGIN && columnWidthPX > 0);
		columnWidthPX++;
	}

	private void drawOneYearColumns(Canvas canvas) {
		float bottom = height - paddingPX - descriptionMarginPX - LINE_STROKE_WIDTH / 2;
		for (int i = 0; i < 12; i++) {
			int value = monthValues.get(i);
			float left = (float) (paddingPX + descriptionMarginPX + columnSpace + i * (columnSpace + columnWidthPX));
			float right = left + columnWidthPX;
			float top = (float) (height - paddingPX - descriptionMarginPX - ((double) value / maxValue) * yAxeHeight);
			canvas.drawRect(left, top, right, bottom, graphPaint);
			drawColumnText(canvas, value, left, top);
		}
	}

	private void drawColumnText(Canvas canvas, int value, float left, float top) {
		textPaint.getTextBounds(value + "", 0, (value + "").length(), bounds);
		while (bounds.right > columnWidthPX + 2 * columnSpace) {
			textPaint.setTextScaleX(textPaint.getTextScaleX() - 0.1f);
		}
		canvas.drawText(value + "", left + columnWidthPX / 2 - bounds.right / 2, top - 10, textPaint);
		textPaint.setTextScaleX(1);
	}

	private void drawYearComparisonColumns(Canvas canvas) {
		float bottom = height - paddingPX - descriptionMarginPX - LINE_STROKE_WIDTH / 2;
		for (int i = 0; i < 12; i++) {
			float left = (float) (paddingPX + descriptionMarginPX + columnSpace + i * (columnSpace + columnWidthPX));
			float right = left + columnWidthPX / 2;
			float top = (float) (height - paddingPX - descriptionMarginPX - ((double) year1Values.get(i) / maxValue) * yAxeHeight);
			graphPaint.setColor(Color.parseColor(GRAPH_YEAR1_COLOR));
			canvas.drawRect(left, top, right, bottom, graphPaint);
			left += columnWidthPX / 2;
			right = left + columnWidthPX / 2;
			top = (float) (height - paddingPX - descriptionMarginPX - ((double) year2Values.get(i) / maxValue) * yAxeHeight);
			graphPaint.setColor(Color.parseColor(GRAPH_YEAR2_COLOR));
			canvas.drawRect(left, top, right, bottom, graphPaint);
			graphPaint.setColor(Color.parseColor(GRAPH_COLOR));
		}
	}

	private void drawYearColorIndicators(Canvas canvas) {
		int year1CompleteValue = 0;
		for (int x : year1Values) {
			year1CompleteValue += x;
		}
		int year2CompleteValue = 0;
		for (int x : year2Values) {
			year2CompleteValue += x;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(list[0].get(0).getDate());
		String year1 = calendar.get(Calendar.YEAR)
		        + ": "
		        + year1CompleteValue
		        + " "
		        + Utils.getUnit(getContext());
		calendar.setTime(list[0].get(list[0].size() - 1).getDate());
		String year2 = calendar.get(Calendar.YEAR)
		        + ": "
		        + year2CompleteValue
		        + " "
		        + Utils.getUnit(getContext());
		textPaint.getTextBounds(year1, 0, year1.length(), bounds);
		float x = width - (paddingPX + descriptionMarginPX / 2) / 2 + bounds.top / 2;
		float y = height / 3 - bounds.right / 2;
		textPaint.setColor(Color.parseColor(GRAPH_YEAR1_COLOR));
		canvas.rotate(90);
		canvas.drawText(year1, y, -x, textPaint);
		textPaint.getTextBounds(year2, 0, year2.length(), bounds);
		x = width - (paddingPX + descriptionMarginPX / 2) / 2 + bounds.top / 2;
		y = (height / 3) * 2 - bounds.right / 2;
		textPaint.setColor(Color.parseColor(GRAPH_YEAR2_COLOR));
		canvas.drawText(year2, y, -x, textPaint);
		canvas.rotate(-90);
		textPaint.setColor(Color.BLACK);
	}

	private void drawMoreYearsColumns(Canvas canvas) {
		float bottom = height - paddingPX - descriptionMarginPX - LINE_STROKE_WIDTH / 2;
		for (int i = 0; i < moreYearsValues.size(); i++) {
			float left = (float) (paddingPX + descriptionMarginPX + columnSpace + i * (columnSpace + columnWidthPX));
			float right = left + columnWidthPX;
			float top = (float) (height - paddingPX - descriptionMarginPX - ((double) moreYearsValues.get(i) / maxValue) * yAxeHeight);
			canvas.drawRect(left, top, right, bottom, graphPaint);
			drawColumnText(canvas, moreYearsValues.get(i), left, top);
		}
	}

	private void drawGraph(Canvas canvas) {
		Date leftDate = list[0].get(0).getDate();
		for (int j = 0; j < list.length; j++) {
			graphPaint.setColor(colors[j]);
			for (int i = 0; i < list[j].size() - 1; i++) {
				Date startDate = list[j].get(i).getDate();
				Date endDate = list[j].get(i + 1).getDate();

				int startDays = Utils.getDayDiff(leftDate, startDate);
				int endDays = Utils.getDayDiff(leftDate, endDate);

				float startX = paddingPX + descriptionMarginPX + (float) (startDays * dayWidth);
				float startY = height - paddingPX - descriptionMarginPX - (float) ((double) diffList[j].get(i) / maxValue) * yAxeHeight;
				float stopX = paddingPX + descriptionMarginPX + (float) (endDays * dayWidth);
				float stopY = height - paddingPX - descriptionMarginPX - (float) ((double) diffList[j].get(i + 1) / maxValue) * yAxeHeight;
				canvas.drawLine(startX, startY, stopX, stopY, graphPaint);
			}
		}
		graphPaint.setColor(Color.parseColor(GRAPH_COLOR));
	}

	private void drawMoreYearsAxes(Canvas canvas) {
		drawAxesLines(canvas);
		drawYindicators(canvas);

		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < moreYearsList.size(); i++) {
			calendar.setTime(moreYearsList.get(i).getDate());
			String year = DATE_FORMAT_YEAR.format(calendar.getTime());
			textPaint.getTextBounds(year, 0, year.length(), bounds);
			float x = (float) (paddingPX + descriptionMarginPX + columnSpace + i * (columnWidthPX + columnSpace) + columnWidthPX / 2 - bounds.right / 2);
			int y = height - paddingPX;
			canvas.drawText(year, x, y, textPaint);
		}
	}

	private void drawOneYearAxes(Canvas canvas) {
		drawAxesLines(canvas);
		drawYindicators(canvas);

		Calendar calendar = Calendar.getInstance();
		for (int i = 0; i < 12; i++) {
			calendar.set(Calendar.MONTH, i);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			String month = DATE_FORMAT_SHORTMONTH.format(calendar.getTime());
			textPaint.getTextBounds(month, 0, month.length(), bounds);
			float x = (float) (paddingPX + descriptionMarginPX + columnSpace + i * (columnWidthPX + columnSpace) + columnWidthPX / 2 - bounds.right / 2);
			int y = height - paddingPX;
			canvas.drawText(month, x, y, textPaint);
		}
	}

	private void drawNormalAxes(Canvas canvas) {
		drawAxesLines(canvas);
		drawYindicators(canvas);

		textPaint.getTextBounds("00.00.0000", 0, 10, bounds);
		int showWhichDates = (int) ((bounds.right + DATE_MARGIN / 2) / dayWidth);
		Date showDate;
		for (int i = 0; i <= dayDiff; i++) {
			if (i % (showWhichDates + 1) == 0) {
				showDate = Utils.addDays(list[0].get(0).getDate(), i);
				String dateString = DATE_FORMAT_STRING.format(showDate);
				drawDateIndicaor(paddingPX + descriptionMarginPX + i * dayWidth, dateString, canvas);
			}
		}
	}

	private void drawYindicators(Canvas canvas) {
		for (int i = 0; i < 5; i++) {
			drawValueIndicator(height - paddingPX - descriptionMarginPX - (int) (((double) i / 4) * yAxeHeight), (double) i * maxValue / 4, canvas);
		}
	}

	private void drawAxesLines(Canvas canvas) {
		canvas.drawLine(paddingPX + descriptionMarginPX, height - paddingPX - descriptionMarginPX, width - paddingPX - descriptionMarginPX / 2,
		        height - paddingPX - descriptionMarginPX, linePaint);
		canvas.drawLine(paddingPX + descriptionMarginPX, paddingPX, paddingPX + descriptionMarginPX, height - paddingPX - descriptionMarginPX,
		        linePaint);

		String text =Utils.getUnit(getContext());
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		float x = paddingPX + descriptionMarginPX - bounds.right / 2;
		float y = (float) paddingPX / 2 - bounds.top / 2;
		canvas.drawText(text, x, y, textPaint);
	}

	private int getMaxValue(List<Integer> list) {
		int maxValue = 0;
		for (int value : list) {
			if (value > maxValue)
				maxValue = value;
		}

		if (maxValue < 100) {
			while (maxValue % 10 != 0)
				maxValue++;
		} else {
			if (maxValue < 1000) {
				while (maxValue % 50 != 0)
					maxValue++;
			} else {
				while (maxValue % 100 != 0)
					maxValue++;
			}
		}
		return maxValue;
	}

	private int getMaxValue(List<Integer>[] list) {
		int max = 0;
		for (int i = 0; i < list.length; i++) {
			int tmp = getMaxValue(list[i]);
			if (tmp > max) {
				max = tmp;
			}
		}
		return max;
	}

	private void drawDateIndicaor(double x, String text, Canvas canvas) {
		if (touchXIndiagramm()) {
			return;
		}
		int startY = height - paddingPX - descriptionMarginPX - INDICATOR_LENGTH / 2;
		int endY = height - paddingPX - descriptionMarginPX + INDICATOR_LENGTH / 2;
		drawDate(text, x, canvas);
		canvas.drawLine((float) x, startY, (float) x, endY, indicatorPaint);
	}

	private void drawDate(String date, double x, Canvas canvas) {
		float y = height - paddingPX - descriptionMarginPX + INDICATOR_LENGTH / 2 + (paddingPX + descriptionMarginPX + INDICATOR_LENGTH / 2) / 2
		        + bounds.bottom / 2;
		do {
			textPaint.getTextBounds(date, 0, date.length(), bounds);
			x = x - bounds.right / 2;
			textPaint.setTextScaleX(textPaint.getTextScaleX() - 0.1f);
		} while (x < 0 || bounds.right > width);
		textPaint.setTextScaleX(1);

		canvas.drawText(date, (float) x, y, textPaint);
	}

	private void drawValueIndicator(int y, double value, Canvas canvas) {
		if (touchXIndiagramm() && type == TYPE_NORMAL) {
			return;
		}
		String text = value + "";
		text = text.replace(".0", "");
		text = text.replace(".", ",");
		canvas.drawLine(paddingPX + descriptionMarginPX - INDICATOR_LENGTH / 2, y, paddingPX + descriptionMarginPX + INDICATOR_LENGTH / 2, y,
		        indicatorPaint);

		canvas.drawLine(paddingPX + descriptionMarginPX, y, width - paddingPX - descriptionMarginPX / 2, y, hintLinePaint);

		drawValue(y, canvas, text);
	}

	private void drawValue(int y, Canvas canvas, String text) {
		int x;
		do {
			textPaint.getTextBounds(text, 0, text.length(), bounds);
			x = (paddingPX + descriptionMarginPX - INDICATOR_LENGTH / 2) / 2 - bounds.right / 2;
			textPaint.setTextScaleX(textPaint.getTextScaleX() - 0.1f);
		} while (x < 0);
		canvas.drawText(text, x, y - bounds.top / 2, textPaint);
		textPaint.setTextScaleX(1);
	}

	private boolean touchXIndiagramm() {
		return touchX > paddingPX + descriptionMarginPX && touchX < width - paddingPX - descriptionMarginPX / 2;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (type != TYPE_NORMAL)
			return false;
		if (event.getAction() == MotionEvent.ACTION_UP) {
			touchX = 0;
		} else {
			touchX = event.getX();
		}
		invalidate();
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

}