package de.mwiktorin.solarstats.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.Toast;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.EventBus.EventListener;
import de.mwiktorin.solarstats.Preferences;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.dialogs.DatePickerFragment;
import de.mwiktorin.solarstats.dialogs.ImageSizePickerFragment;
import de.mwiktorin.solarstats.dialogs.MoreYearsPickerFragment;
import de.mwiktorin.solarstats.dialogs.MyAlertDialog;
import de.mwiktorin.solarstats.dialogs.YearComparisonPickerFragment;
import de.mwiktorin.solarstats.model.DataRow;
import de.mwiktorin.solarstats.model.DateCount;
import de.mwiktorin.solarstats.model.System;
import de.mwiktorin.solarstats.tasks.DiagramLoadData;
import de.mwiktorin.solarstats.tasks.ExportImage;
import de.mwiktorin.solarstats.views.DiagramView;

public class DiagramFragment extends Fragment implements EventListener {

	private static final SimpleDateFormat MONTH_DATE_FORMAT_STRING = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
	private static final SimpleDateFormat YEAR_DATE_FORMAT_STRING = new SimpleDateFormat("yyyy", Locale.getDefault());
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
	private final static SimpleDateFormat DATE_FORMAT_STRING = new SimpleDateFormat("dd.MM.yyyy", Locale.US);

	private Menu menu;
	private List<MenuItem> menuItems = new ArrayList<MenuItem>();
	private List<DataRow>[] list;
	private int[] showIndizes;
	private DiagramView diagram;
	private List<DateCount> monthList;
	private List<DateCount> yearList;
	private Spinner timeSpinner;
	private Spinner categorySpinner;
	private String[] categoryPickerItems;
	private ArrayAdapter<CharSequence> categorySpinnerAdapter;
	private ArrayAdapter<CharSequence> timeSpinnerAdapter;
	private boolean startDatePickerStarted = false;
	private boolean endDatePickerStarted = false;
	private Date selectedStartDate;
	private YearComparisonPickerFragment yearComparisonPickerFragment;
	private MoreYearsPickerFragment moreYearsPickerFragment;
	private List<DataRow>[] showList;
	private String currentTitle;

	private OnDateSetListener startDateListener = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			if (!startDatePickerStarted)
				return;

			Calendar calendar = Calendar.getInstance();
			calendar.set(year, monthOfYear, dayOfMonth);
			selectedStartDate = calendar.getTime();

			String savedEndDate = Preferences.getInstance(getActivity()).getString(Preferences.DIAGRAMM_SELECTED_TIME_ENDDATE, "");
			if (savedEndDate.equals("")) {
				calendar.setTime(list[showIndizes[0]].get(list[showIndizes[0]].size() - 1).getDate());
			} else {
				try {
					calendar.setTime(DATE_FORMAT.parse(savedEndDate));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			DatePickerFragment datePickerFragment = new DatePickerFragment();
			datePickerFragment.setStartDate(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
			datePickerFragment.setOnDateSetListener(endDateListener);
			datePickerFragment.setTitle(getActivity().getResources().getString(R.string.diagramm_fragment_dialog_end_title));
			if (!endDatePickerStarted) {
				endDatePickerStarted = true;
				datePickerFragment.show(getActivity().getFragmentManager(), "datePicker");
			}
			startDatePickerStarted = false;
		}
	};

	private OnDateSetListener endDateListener = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			if (!endDatePickerStarted)
				return;

			Calendar calendar = Calendar.getInstance();
			calendar.set(year, monthOfYear, dayOfMonth);
			if (selectedStartDate.before(calendar.getTime())) {
				Preferences.getInstance(getActivity()).putString(Preferences.DIAGRAMM_SELECTED_TIME_STARTDATE, DATE_FORMAT.format(selectedStartDate));
				Preferences.getInstance(getActivity()).putString(Preferences.DIAGRAMM_SELECTED_TIME_ENDDATE, DATE_FORMAT.format(calendar.getTime()));
				updateDiagram(true);
			} else {
				Toast.makeText(
				        getActivity(),
				        String.format(getActivity().getResources().getString(R.string.diagramm_fragment_wrong_time_selected_toast),
				                DATE_FORMAT_STRING.format(selectedStartDate), DATE_FORMAT_STRING.format(calendar.getTime())), Toast.LENGTH_LONG)
				        .show();
			}
			endDatePickerStarted = false;
		}
	};

	private View.OnClickListener yearComparisonListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (yearComparisonPickerFragment == null)
				return;

			int year1 = yearComparisonPickerFragment.getSmallerValue();
			int year2 = yearComparisonPickerFragment.getBiggerValue();

			if (year1 != year2) {
				Preferences.getInstance(getActivity()).putString(Preferences.DIAGRAMM_SELECTED_TIME_YEAR1, year1 + "");
				Preferences.getInstance(getActivity()).putString(Preferences.DIAGRAMM_SELECTED_TIME_YEAR2, year2 + "");
			} else {
				Toast.makeText(getActivity(), R.string.diagramm_fragment_dialog_pickyears_pickedsame_toast, Toast.LENGTH_LONG).show();
			}
			updateDiagram(true);
			yearComparisonPickerFragment = null;
		}
	};

	private View.OnClickListener moreYearsListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (moreYearsPickerFragment == null)
				return;

			int year1 = moreYearsPickerFragment.getSmallerValue();
			int year2 = moreYearsPickerFragment.getBiggerValue();

			if (year1 != year2) {
				Preferences.getInstance(getActivity()).putString(Preferences.DIAGRAMM_SELECTED_TIME_STARTYEAR, year1 + "");
				Preferences.getInstance(getActivity()).putString(Preferences.DIAGRAMM_SELECTED_TIME_ENDYEAR, year2 + "");
			} else {
				Toast.makeText(getActivity(), R.string.diagramm_fragment_dialog_pickyears_pickedsame_toast, Toast.LENGTH_LONG).show();
			}
			updateDiagram(true);
			moreYearsPickerFragment = null;
		}
	};

	private OnItemSelectedListener categorySpinnerListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			if (list == null) {
				return;
			}

			Preferences.getInstance(getActivity()).putInt(Preferences.DIAGRAMM_SELECTED_CATEGRORY, pos);
			showIndizes = new int[] { Utils.getCurrentSystemPosition(getActivity()) };
			showList = new List[1];
			switch (pos) {
			case 0:
				addOptionItems();
				break;
			case 1:
				addOptionItems();
				break;
			case 2:
				getActivity().invalidateOptionsMenu();
				break;
			case 3:
				getActivity().invalidateOptionsMenu();
				break;
			case 4:
				getActivity().invalidateOptionsMenu();
				break;
			case 5:
				addOptionItems();
				break;
			}
			updateDiagram(true);
			categorySpinner.setVisibility(View.VISIBLE);
			timeSpinner.setVisibility(View.VISIBLE);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	private OnItemSelectedListener timeMonthSpinnerListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			Preferences.getInstance(getActivity()).putInt(Preferences.DIAGRAMM_SELECTED_TIME_MONTH, pos);
			updateDiagram(false);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	private OnItemSelectedListener timeYearSpinnerListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			Preferences.getInstance(getActivity()).putInt(Preferences.DIAGRAMM_SELECTED_TIME_YEAR, pos);
			updateDiagram(false);
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		EventBus.getInstance().register(this);
		setHasOptionsMenu(true);

		showList = new List[1];
		showIndizes = new int[] { Utils.getCurrentSystemPosition(getActivity()) };

		startLoading();

		View view = inflater.inflate(R.layout.fragment_diagramm, container, false);

		diagram = (DiagramView) view.findViewById(R.id.diagrammView);
		categorySpinner = (Spinner) view.findViewById(R.id.diagrammSpinnerCategory);
		timeSpinner = (Spinner) view.findViewById(R.id.diagrammSpinnerTime);

		categoryPickerItems = getActivity().getResources().getStringArray(R.array.diagramm_categorySpinner);
		categorySpinnerAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, categoryPickerItems);
		categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		categorySpinner.setAdapter(categorySpinnerAdapter);
		categorySpinner.setOnItemSelectedListener(categorySpinnerListener);

		categorySpinner.setVisibility(View.INVISIBLE);
		timeSpinner.setVisibility(View.INVISIBLE);

		return view;
	}

	@Override
	public void onDestroy() {
		EventBus.getInstance().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		this.menu = menu;
		inflater.inflate(R.menu.diagram, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.diagramm_menu_export:
			if (showList != null && showList[0] != null && showList[0].size() > 1) {
				MyAlertDialog builder = new MyAlertDialog(getActivity());
				builder.setTitle(R.string.diagramm_fragment_export_dialog_title);
				builder.setIcon(R.drawable.ic_menu_export);
				builder.setItems(R.array.diagramm_export_types, new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
						if (pos == 2) {
							final ImageSizePickerFragment dialogFragment = new ImageSizePickerFragment();
							dialogFragment.setListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									ExportImage exportTask = new ExportImage(DiagramFragment.this, dialogFragment.getWidth(), dialogFragment
									        .getHeight(), diagram, currentTitle);
									exportTask.execute();
								}
							});
							dialogFragment.show(getActivity().getFragmentManager(), "imageSizePicker");
						} else {
							ExportImage exportTask = new ExportImage(DiagramFragment.this, getActivity().getResources().getIntArray(
							        R.array.diagramm_image_export_width_px)[pos], getActivity().getResources().getIntArray(
							        R.array.diagramm_image_export_height_px)[pos], diagram, currentTitle);
							exportTask.execute();
						}
					}
				});
				builder.show();
			} else {
				Toast.makeText(getActivity(), R.string.data_fragment_toast_noexport, Toast.LENGTH_LONG).show();
			}
			break;
		}
		return false;
	}

	private void startLoading() {
		DiagramLoadData loadTask = new DiagramLoadData(this);
		loadTask.execute();
	}

	private void addOptionItems() {
		for (MenuItem item : menuItems) {
			menu.removeItem(item.getItemId());
		}
		menuItems.clear();

		for (final System system : EventBus.getSystems()) {
			if (system.getId() == EventBus.getSystems().get(Utils.getCurrentSystemPosition(getActivity())).getId()) {
				continue;
			}
			final MenuItem item = menu.add(system.getName());
			SpannableString s = new SpannableString(system.getName());
			s.setSpan(new ForegroundColorSpan(system.getColor()), 0, s.length(), 0);
			item.setTitle(s);

			item.setCheckable(true);
			item.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					int[] oldShowIndizes = new int[showIndizes.length];
					for (int i = 0; i < oldShowIndizes.length; i++) {
						oldShowIndizes[i] = showIndizes[i];
					}
					if (!item.isChecked()) {
						showList = new List[showList.length + 1];
						showIndizes = new int[showIndizes.length + 1];
						for (int i = 0; i < showIndizes.length - 1; i++) {
							showIndizes[i] = oldShowIndizes[i];
						}
						showIndizes[showIndizes.length - 1] = EventBus.getSystems().indexOf(system);
					} else {
						showList = new List[showList.length - 1];
						showIndizes = new int[showIndizes.length - 1];
						int smallerIndex = 0;
						for (int i = 0; i < oldShowIndizes.length; i++) {
							if (!EventBus.getSystems().get(oldShowIndizes[i]).equals(system)) {
								showIndizes[smallerIndex] = oldShowIndizes[i];
								smallerIndex++;
							}
						}
					}
					item.setChecked(!item.isChecked());
					updateDiagram(false);
					return false;
				}
			});
			menuItems.add(item);
		}
	}

	private void startDatePicker() {
		timeSpinner.setEnabled(false);
		String savedStartDate = Preferences.getInstance(getActivity()).getString(Preferences.DIAGRAMM_SELECTED_TIME_STARTDATE, "");
		Calendar calendar = Calendar.getInstance();
		if (savedStartDate.equals("")) {
			calendar.setTime(list[showIndizes[0]].get(0).getDate());
		} else {
			try {
				calendar.setTime(DATE_FORMAT.parse(savedStartDate));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		DatePickerFragment datePickerFragment = new DatePickerFragment();
		datePickerFragment.setStartDate(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
		datePickerFragment.setOnDateSetListener(startDateListener);
		datePickerFragment.setTitle(getActivity().getResources().getString(R.string.diagramm_fragment_dialog_start_title));
		if (!startDatePickerStarted) {
			startDatePickerStarted = true;
			datePickerFragment.show(getActivity().getFragmentManager(), "datePicker");
		}

	}

	private void startYearComparisonPicker() {
		if (yearComparisonPickerFragment != null)
			return;
		timeSpinner.setEnabled(false);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(yearList.get(0).getDate());
		int startYear = calendar.get(Calendar.YEAR);
		calendar.setTime(yearList.get(yearList.size() - 1).getDate());
		int endYear = calendar.get(Calendar.YEAR);

		String savedYear1 = Preferences.getInstance(getActivity()).getString(Preferences.DIAGRAMM_SELECTED_TIME_YEAR1, "");
		String savedYear2 = Preferences.getInstance(getActivity()).getString(Preferences.DIAGRAMM_SELECTED_TIME_YEAR2, "");

		yearComparisonPickerFragment = new YearComparisonPickerFragment();
		if (savedYear1.equals("") || savedYear2.equals("")) {
			yearComparisonPickerFragment.setValues(startYear, endYear, startYear, endYear);
		} else {
			yearComparisonPickerFragment.setValues(startYear, endYear, Integer.parseInt(savedYear1), Integer.parseInt(savedYear2));
		}
		yearComparisonPickerFragment.setListener(yearComparisonListener);
		yearComparisonPickerFragment.show(getActivity().getFragmentManager(), "yearPicker");
	}

	private void startMoreYearsPicker() {
		if (moreYearsPickerFragment != null)
			return;
		timeSpinner.setEnabled(false);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(yearList.get(0).getDate());
		int startYear = calendar.get(Calendar.YEAR);
		calendar.setTime(yearList.get(yearList.size() - 1).getDate());
		int endYear = calendar.get(Calendar.YEAR);

		String savedStartYear = Preferences.getInstance(getActivity()).getString(Preferences.DIAGRAMM_SELECTED_TIME_STARTYEAR, "");
		String savedEndYear = Preferences.getInstance(getActivity()).getString(Preferences.DIAGRAMM_SELECTED_TIME_ENDYEAR, "");

		moreYearsPickerFragment = new MoreYearsPickerFragment();
		if (savedStartYear.equals("") || savedEndYear.equals("")) {
			moreYearsPickerFragment.setValues(startYear, endYear, startYear, endYear);
		} else {
			moreYearsPickerFragment.setValues(startYear, endYear, Integer.parseInt(savedStartYear), Integer.parseInt(savedEndYear));
		}
		moreYearsPickerFragment.setListener(moreYearsListener);
		moreYearsPickerFragment.show(getActivity().getFragmentManager(), "yearPicker");
	}

	private void updateDiagram(boolean updateTimeSpinner) {
		boolean lowData = false;
		boolean enableTimeSpinner = true;
		int type = 0;
		String[] timeSpinnerData = null;
		String timeSpinnerText = null;
		OnTouchListener timeSpinnerTouchListener = null;
		OnItemSelectedListener timeSpinnerSelectListener = null;
		int timeSpinnerPosition = 0;

		List<DataRow> year1List = null;
		List<DataRow> year2List = null;

		if (list[showIndizes[0]].size() < 2) {
			lowData = true;
		} else {

			switch (categorySpinner.getSelectedItemPosition()) {
			case 0: // whole time
				type = DiagramView.TYPE_NORMAL;
				showList[0] = new ArrayList<DataRow>();
				Utils.addPreviousToList(getActivity(), list[showIndizes[0]], showList[0], 0);
				showList[0].addAll(list[showIndizes[0]]);

				addOtherSystems();

				enableTimeSpinner = false;
				timeSpinnerText = list[showIndizes[0]].get(0).getStringDate() + " - "
				        + list[showIndizes[0]].get(list[showIndizes[0]].size() - 1).getStringDate();
				break;
			case 1: // month
				type = DiagramView.TYPE_NORMAL;
				if (monthList.size() > 0) {
					int pos = Preferences.getInstance(getActivity()).getInt(Preferences.DIAGRAMM_SELECTED_TIME_MONTH, 0);
					if (pos >= monthList.size()) {
						pos = 0;
					}

					showList[0] = new ArrayList<DataRow>();
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(monthList.get(pos).getDate());
					for (int i = 0; i < list[showIndizes[0]].size(); i++) {
						Calendar cal = new GregorianCalendar();
						cal.setTime(list[showIndizes[0]].get(i).getDate());
						if (calendar.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
							if (showList[0].size() == 0) {
								Utils.addPreviousToList(getActivity(), list[showIndizes[0]], showList[0], i);
							}
							showList[0].add(list[showIndizes[0]].get(i));
						}
					}

					addOtherSystems();

					timeSpinnerData = new String[monthList.size()];
					for (int i = 0; i < timeSpinnerData.length; i++) {
						timeSpinnerData[i] = MONTH_DATE_FORMAT_STRING.format(monthList.get(i).getDate());
					}
					timeSpinnerSelectListener = timeMonthSpinnerListener;
					timeSpinnerPosition = Preferences.getInstance(getActivity()).getInt(Preferences.DIAGRAMM_SELECTED_TIME_MONTH, 0);
					timeSpinnerText = MONTH_DATE_FORMAT_STRING.format(monthList.get(pos).getDate());
				} else {
					lowData = true;
				}

				break;
			case 2: // one year
				type = DiagramView.TYPE_ONE_YEAR;
				if (yearList.size() > 0) {
					showList[0] = new ArrayList<DataRow>();
					int pos = Preferences.getInstance(getActivity()).getInt(Preferences.DIAGRAMM_SELECTED_TIME_YEAR, 0);
					if (pos >= yearList.size()) {
						pos = 0;
					}
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(yearList.get(pos).getDate());
					for (int i = 0; i < list[showIndizes[0]].size(); i++) {
						Calendar cal = new GregorianCalendar();
						cal.setTime(list[showIndizes[0]].get(i).getDate());
						if (calendar.get(Calendar.YEAR) == cal.get(Calendar.YEAR)) {
							if (showList[0].size() == 0) {
								Utils.addPreviousToList(getActivity(), list[showIndizes[0]], showList[0], i);
							}
							showList[0].add(list[showIndizes[0]].get(i));
						}
					}

					timeSpinnerData = new String[yearList.size()];
					for (int i = 0; i < timeSpinnerData.length; i++) {
						timeSpinnerData[i] = YEAR_DATE_FORMAT_STRING.format(yearList.get(i).getDate());
					}
					timeSpinnerSelectListener = timeYearSpinnerListener;
					timeSpinnerPosition = Preferences.getInstance(getActivity()).getInt(Preferences.DIAGRAMM_SELECTED_TIME_YEAR, 0);
					timeSpinnerText = YEAR_DATE_FORMAT_STRING.format(yearList.get(pos).getDate());
				} else {
					lowData = true;
				}
				break;

			case 3: // more years
				type = DiagramView.TYPE_MORE_YEARS;
				if (yearList.size() > 1) {
					String savedStartYear = Preferences.getInstance(getActivity()).getString(Preferences.DIAGRAMM_SELECTED_TIME_STARTYEAR, "");
					String savedEndYear = Preferences.getInstance(getActivity()).getString(Preferences.DIAGRAMM_SELECTED_TIME_ENDYEAR, "");
					if (savedStartYear.equals("") || savedEndYear.equals("")) {
						startMoreYearsPicker();
						lowData = true;
					} else {
						Calendar startCal = Calendar.getInstance();
						startCal.set(Integer.parseInt(savedStartYear), 0, 1, 0, 0, 0);
						Calendar endCal = Calendar.getInstance();
						endCal.set(Integer.parseInt(savedEndYear), 11, 31, 23, 59, 59);

						showList[0] = new ArrayList<DataRow>();
						for (int i = 0; i < list[showIndizes[0]].size(); i++) {
							if (!list[showIndizes[0]].get(i).getDate().after(endCal.getTime())
							        && !list[showIndizes[0]].get(i).getDate().before(startCal.getTime())) {
								if (showList[0].size() == 0) {
									Utils.addPreviousToList(getActivity(), list[showIndizes[0]], showList[0], i);
								}
								showList[0].add(list[showIndizes[0]].get(i));
							}
						}
						timeSpinnerText = savedStartYear + " - " + savedEndYear;
						timeSpinnerTouchListener = new OnTouchListener() {
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								startMoreYearsPicker();
								return false;
							}
						};
					}
				} else {
					lowData = true;
				}
				break;
			case 4: // year comparison
				type = DiagramView.TYPE_YEAR_COMPARISON;
				if (yearList.size() > 1) {
					String savedYear1 = Preferences.getInstance(getActivity()).getString(Preferences.DIAGRAMM_SELECTED_TIME_YEAR1, "");
					String savedYear2 = Preferences.getInstance(getActivity()).getString(Preferences.DIAGRAMM_SELECTED_TIME_YEAR2, "");
					if (savedYear1.equals("") || savedYear2.equals("")) {
						startYearComparisonPicker();
						lowData = true;
					} else {
						Calendar calendar = Calendar.getInstance();
						year1List = new ArrayList<DataRow>();
						for (int i = 0; i < list[showIndizes[0]].size(); i++) {
							calendar.setTime(list[showIndizes[0]].get(i).getDate());
							if (savedYear1.equals(calendar.get(Calendar.YEAR) + "")) {
								if (year1List.size() == 0) {
									Utils.addPreviousToList(getActivity(), list[showIndizes[0]], year1List, i);
								}
								year1List.add(list[showIndizes[0]].get(i));
							}
						}
						year2List = new ArrayList<DataRow>();
						for (int i = 0; i < list[showIndizes[0]].size(); i++) {
							calendar.setTime(list[showIndizes[0]].get(i).getDate());
							if (savedYear2.equals(calendar.get(Calendar.YEAR) + "")) {
								if (year2List.size() == 0) {
									Utils.addPreviousToList(getActivity(), list[showIndizes[0]], year2List, i);
								}
								year2List.add(list[showIndizes[0]].get(i));
							}
						}
						showList[0] = year1List;
						timeSpinnerText = savedYear1 + " | " + savedYear2;
						timeSpinnerTouchListener = new OnTouchListener() {
							@Override
							public boolean onTouch(View v, MotionEvent event) {
								startYearComparisonPicker();
								return false;
							}
						};
					}
				} else {
					lowData = true;
				}
				break;
			case 5: // custom period
				try {
					type = DiagramView.TYPE_NORMAL;
					Date savedStartDate = DATE_FORMAT.parse(Preferences.getInstance(getActivity()).getString(
					        Preferences.DIAGRAMM_SELECTED_TIME_STARTDATE, ""));
					Date savedEndDate = DATE_FORMAT.parse(Preferences.getInstance(getActivity()).getString(
					        Preferences.DIAGRAMM_SELECTED_TIME_ENDDATE, ""));

					showList[0] = new ArrayList<DataRow>();
					for (int i = 0; i < list[showIndizes[0]].size(); i++) {
						if (!list[showIndizes[0]].get(i).getDate().before(savedStartDate)
						        && !list[showIndizes[0]].get(i).getDate().after(savedEndDate)) {
							if (showList[0].size() == 0) {
								Utils.addPreviousToList(getActivity(), list[showIndizes[0]], showList[0], i);
							}
							showList[0].add(list[showIndizes[0]].get(i));
						}
					}
					addOtherSystems();

					timeSpinnerText = DATE_FORMAT_STRING.format(savedStartDate) + " - " + DATE_FORMAT_STRING.format(savedEndDate);
					timeSpinnerTouchListener = new OnTouchListener() {
						@Override
						public boolean onTouch(View arg0, MotionEvent arg1) {
							startDatePicker();
							return false;
						}
					};
				} catch (ParseException e) {
					startDatePicker();
					lowData = true;
				}
				break;
			}
		}

		if (lowData) {
			enableTimeSpinner = false;
			timeSpinnerText = getActivity().getResources().getString(R.string.diagramm_fragment_view_text_no_data);
			showList[0] = new ArrayList<DataRow>();
			year1List = new ArrayList<DataRow>();
			year2List = new ArrayList<DataRow>();
		}
		if (type == DiagramView.TYPE_YEAR_COMPARISON) {
			diagram.setYearComparisonData(year1List, year2List);
		} else {
			int[] colors = new int[showList.length];
			for (int i = 0; i < colors.length; i++) {
				colors[i] = EventBus.getSystems().get(showIndizes[i]).getColor();
			}
			diagram.setNormalData(type, colors, showList);
		}
		if (updateTimeSpinner) {
			timeSpinner.setOnTouchListener(timeSpinnerTouchListener);
			timeSpinner.setOnItemSelectedListener(timeSpinnerSelectListener);
			if (timeSpinnerText != null && timeSpinnerData == null) {
				timeSpinnerData = new String[] { timeSpinnerText };
			}
			timeSpinnerAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, timeSpinnerData);
			timeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			timeSpinner.setAdapter(timeSpinnerAdapter);
			timeSpinner.setEnabled(enableTimeSpinner);
			if (timeSpinnerPosition < timeSpinnerData.length) {
				timeSpinner.setSelection(timeSpinnerPosition);
			}
		}
		getActivity().getActionBar().setTitle(timeSpinnerText);
		currentTitle = timeSpinnerText;
	}

	private void addOtherSystems() {
		Date firstDate = showList[0].get(1).getDate();
		Date lastDate = showList[0].get(showList[0].size() - 1).getDate();
		for (int j = 1; j < showIndizes.length; j++) {
			showList[j] = new ArrayList<DataRow>();
			for (int i = 0; i < list[showIndizes[j]].size(); i++) {
				if (!list[showIndizes[j]].get(i).getDate().before(firstDate) && !list[showIndizes[j]].get(i).getDate().after(lastDate)) {
					if (showList[j].size() == 0) {
						Utils.addPreviousToList(getActivity(), list[showIndizes[j]], showList[j], i);
					}
					showList[j].add(list[showIndizes[j]].get(i));
				}
			}
		}
	}

	public void onDataPrepared(List<DataRow>[] list, List<DateCount> monthList, List<DateCount> yearList) {
		this.list = list;
		this.monthList = monthList;
		this.yearList = yearList;

		int selectedPos = Preferences.getInstance(getActivity()).getInt(Preferences.DIAGRAMM_SELECTED_CATEGRORY, 0);
		if (selectedPos == categorySpinner.getSelectedItemPosition()) {
			categorySpinnerListener.onItemSelected(null, null, selectedPos, 0);
		} else {
			categorySpinner.setSelection(selectedPos);
		}
	}

	@Override
	public void onDataLoaded() {
		startLoading();
	}

	@Override
	public void onDataChanged() {
	}

	@Override
	public void onSystemUpdate() {
	}

}
