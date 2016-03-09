package de.mwiktorin.solarstats.dialogs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.Toast;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.model.DataRow;

public class DataExportFragment extends DialogFragment {

	private final static SimpleDateFormat DATE_TITLE_FORMAT = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

	private List<DataRow> list;
	private View.OnClickListener listener;
	private RadioGroup group;
	private Spinner start;
	private Spinner end;
	private Spinner month;
	private List<Date> monthList;
	private ArrayAdapter<CharSequence> adapter;

	public void setValues(List<DataRow> list, View.OnClickListener listener) {
		this.listener = listener;
		this.list = list;
		monthList = getMonthList();
	}

	public int getCheckedRadioButtonId() {
		return group.getCheckedRadioButtonId();
	}

	public List<DataRow> getList() {
		try {
			List<DataRow> returnList = new ArrayList<DataRow>();
			if(getCheckedRadioButtonId() == 2){
				String month = (String) this.month.getSelectedItem();
				for (DataRow row : list) {
		        if (DATE_TITLE_FORMAT.format(row.getDate()).equals(month)) {
		        		returnList.add(row);
		        	}
		        }
				return returnList;
			}
			Calendar lastDate = Calendar.getInstance();
			lastDate.setTime(DATE_TITLE_FORMAT.parse((String) end.getSelectedItem()));
			lastDate.set(Calendar.DAY_OF_MONTH, lastDate.getActualMaximum(Calendar.DAY_OF_MONTH));
			lastDate.set(Calendar.HOUR_OF_DAY, lastDate.getActualMaximum(Calendar.HOUR_OF_DAY));
			Calendar firstDate = Calendar.getInstance();
	        firstDate.setTime(DATE_TITLE_FORMAT.parse((String) start.getSelectedItem()));
	        firstDate.set(Calendar.HOUR_OF_DAY, 0);
	        firstDate.set(Calendar.MINUTE, 0);
	        firstDate.set(Calendar.SECOND, 0);
	        firstDate.set(Calendar.MILLISECOND, 0);
	        for (DataRow row : list) {
	        	if (!row.getDate().after(lastDate.getTime()) && !row.getDate().before(firstDate.getTime())) {
	        		returnList.add(row);
	        	}
	        }
	        return returnList;
        } catch (ParseException e) {
	        e.printStackTrace();
        }
		return null;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		final View view = View.inflate(getActivity(), R.layout.dialog_data_export, null);
		group = (RadioGroup) view.findViewById(R.id.exportTypes_radioGroup);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				((Button) view.findViewById(R.id.button2)).setEnabled(true);
				view.findViewById(R.id.dialog_data_export_ll1).setVisibility(View.VISIBLE);
				view.findViewById(R.id.dialog_data_export_ll2).setVisibility(View.VISIBLE);
				switch (checkedId) {
				case 0:
				case 1:
					view.findViewById(R.id.dialog_data_export_ll1).setVisibility(View.VISIBLE);
					view.findViewById(R.id.dialog_data_export_ll2).setVisibility(View.VISIBLE);
					view.findViewById(R.id.dialog_data_export_ll3).setVisibility(View.GONE);
					end.setAdapter(adapter);
					end.setSelection(end.getCount() - 1);
					start.setOnItemSelectedListener(null);
					break;
				case 2:
					view.findViewById(R.id.dialog_data_export_ll1).setVisibility(View.GONE);
					view.findViewById(R.id.dialog_data_export_ll2).setVisibility(View.GONE);
					view.findViewById(R.id.dialog_data_export_ll3).setVisibility(View.VISIBLE);
					break;
				}
			}
		});
		String[] exportTypes = getActivity().getResources().getStringArray(R.array.data_export_types);
		for (int i = 0; i < exportTypes.length; i++) {
			RadioButton button = new RadioButton(getActivity());
			button.setText(exportTypes[i]);
			button.setId(i);
			button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			group.addView(button);
		}

		start = (Spinner) view.findViewById(R.id.export_start_spinner);
		end = (Spinner) view.findViewById(R.id.export_end_spinner);
		month = (Spinner) view.findViewById(R.id.export_month_spinner);
		adapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, getMonthsArray(monthList));
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		start.setAdapter(adapter);
		end.setAdapter(adapter);
		month.setAdapter(adapter);
		end.setSelection(end.getCount() - 1);
		((Button) view.findViewById(R.id.button1)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		((Button) view.findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
	                if (DATE_TITLE_FORMAT.parse((String) start.getSelectedItem()).after(DATE_TITLE_FORMAT.parse((String) end.getSelectedItem()))){
                		Toast.makeText(getActivity(), R.string.data_fragment_export_dialog_toast, Toast.LENGTH_LONG).show();
	                } else {
	                	dismiss();
	                	listener.onClick(v);
	                }
                } catch (NotFoundException e) {
	                e.printStackTrace();
                } catch (ParseException e) {
	                e.printStackTrace();
                }
			}
		});
		dialog.setView(view);
		return dialog.show();
	}

	private List<Date> getMonthList() {
		List<Date> monthList = new ArrayList<Date>();
		Calendar calender = Calendar.getInstance();
		calender.set(Calendar.DAY_OF_MONTH, 1);
		for (DataRow row : list) {
			Calendar rowCal = new GregorianCalendar();
			rowCal.setTime(row.getDate());
			calender.set(Calendar.MONTH, rowCal.get(Calendar.MONTH));
			calender.set(Calendar.YEAR, rowCal.get(Calendar.YEAR));
			if (!monthList.contains(calender.getTime())) {
				monthList.add(calender.getTime());
			}
		}
		return monthList;
	}

	private String[] getMonthsArray(List<Date> monthList) {
		String[] array = new String[monthList.size()];
		for (int i = 0; i < monthList.size(); i++) {
			array[i] = DATE_TITLE_FORMAT.format(monthList.get(i));
		}
		return array;
	}
}
