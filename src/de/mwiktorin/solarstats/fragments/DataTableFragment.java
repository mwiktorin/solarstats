package de.mwiktorin.solarstats.fragments;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.EventBus.EventListener;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.activities.FileChooserActivity;
import de.mwiktorin.solarstats.activities.MainActivity;
import de.mwiktorin.solarstats.adapters.MyDataListAdapter;
import de.mwiktorin.solarstats.dialogs.DataExportFragment;
import de.mwiktorin.solarstats.dialogs.DatePickerFragment;
import de.mwiktorin.solarstats.dialogs.MyAlertDialog;
import de.mwiktorin.solarstats.dialogs.MyProgressDialog;
import de.mwiktorin.solarstats.model.DataRow;
import de.mwiktorin.solarstats.receivers.AlarmReceiver;
import de.mwiktorin.solarstats.tasks.DeleteRow;
import de.mwiktorin.solarstats.tasks.ExportExcel;
import de.mwiktorin.solarstats.tasks.ExportPDF;
import de.mwiktorin.solarstats.tasks.FindImportData;
import de.mwiktorin.solarstats.threads.ConnectBluetoothThread;

public class DataTableFragment extends Fragment implements EventListener {

	public static final int FILE_RESQUEST_CODE = 1;
	private static final int BLUTOOTH_TURN_ON_REQUEST_CODE = 2;
	private static final SimpleDateFormat DATE_FORMAT_STRING = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

	private Date selectedDate;
	private MyDataListAdapter adapter;
	private ListView listView;
	private TextView noDataText;
	private LinearLayout tableHead;
	private Button saveButton;
	private EditText input;
	private ArrayAdapter<CharSequence> spinnerAdapter;
	private Spinner dateSpinner;
	private CharSequence[] datePickerItems;
	private FindImportData findTask;
	private TextView sizeText;
	private Camera camera;
	private List<BluetoothDevice> btDeviceList;
	private MyProgressDialog bluetoothScanDialog;

	private int currentMinValue, currentMaxValue;

	private OnClickListener saveButtonClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (isInputValueCorrect()) {
				final DataRow row = new DataRow(selectedDate, Integer.parseInt(input.getText().toString()), -1);
				if (EventBus.getData().contains(row)) {
					DataRow existingDataRow = EventBus.getData().get(EventBus.getData().indexOf(row));
					row.setId(existingDataRow.getId());
					MyAlertDialog dialogBuilder = new MyAlertDialog(getActivity());
					dialogBuilder.setTitle(R.string.data_fragment_update_dialog_title);
					dialogBuilder.setIcon(R.drawable.ic_menu_save);
					String unit = Utils.getUnit(getActivity());
					dialogBuilder.setMessage(String.format(getActivity().getResources().getString(R.string.data_fragment_update_dialog_message),
					        row.getStringDate(), existingDataRow.getValue() + " " + unit, row.getValue())
					        + " " + unit);
					dialogBuilder.setMyNegativeButton(R.string.data_fragment_update_dialog_negative, null);
					dialogBuilder.setMyPositiveButton(R.string.data_fragment_update_dialog_positiv, new OnClickListener() {
						@Override
						public void onClick(View v) {
							Utils.saveData(getActivity().getApplicationContext(), row, EventBus.getData(), true);
						}
					});
					dialogBuilder.show();
				} else {
					Utils.saveData(getActivity().getApplicationContext(), row, EventBus.getData(), true);
				}
			} else {
				Toast.makeText(getActivity(), R.string.data_fragment_toast_incorrect_value, Toast.LENGTH_SHORT).show();
			}
		}
	};

	private TextWatcher inputTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			saveButton.setEnabled(isInputValueCorrect());
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};

	private OnEditorActionListener inputActionListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			saveButtonClickListener.onClick(null);
			return true;
		}
	};

	private OnItemSelectedListener spinnerListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			if (EventBus.getData() == null) {
				return;
			}
			switch (pos) {
			case 0:
				datePickerItems[1] = getActivity().getResources().getStringArray(R.array.data_date_spinner)[1];
				spinnerAdapter.notifyDataSetChanged();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(selectedDate);
				DatePickerFragment datePickerFragment = new DatePickerFragment();
				datePickerFragment.setStartDate(calendar.get(Calendar.DATE), calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
				datePickerFragment.setOnDateSetListener(dateSetListener);
				datePickerFragment.setTitle(getActivity().getResources().getStringArray(R.array.data_date_spinner)[0]);
				datePickerFragment.show(getActivity().getFragmentManager(), "datePicker");
				break;
			case 1:
				if (datePickerItems[1].equals(getActivity().getResources().getStringArray(R.array.data_date_spinner)[1])) {
					Date yesterday = new Date(getToday().getTime() - 86400 * 1000);
					selectedDate = yesterday;
				}
				break;
			case 2:
				datePickerItems[1] = getActivity().getResources().getStringArray(R.array.data_date_spinner)[1];
				spinnerAdapter.notifyDataSetChanged();
				selectedDate = getToday();
				break;
			}
			updateMinMaxValue();
			saveButton.setEnabled(isInputValueCorrect());
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};

	private OnDateSetListener dateSetListener = new OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			GregorianCalendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
			if (calendar.getTime().before(getToday())) {
				datePickerItems[1] = DATE_FORMAT_STRING.format(calendar.getTime());
				spinnerAdapter.notifyDataSetChanged();
				dateSpinner.setSelection(1);
				selectedDate = calendar.getTime();
				if (EventBus.getData().contains(new DataRow(selectedDate, 0, -1))) {
					listView.setSelection(EventBus.getData().indexOf(new DataRow(selectedDate, 0, -1)));
					input.setText(EventBus.getData().get(EventBus.getData().indexOf(new DataRow(selectedDate, 0, -1))).getValue() + "");
				} else {
					int beforeDateIndex = Utils.getBeforeDateIndex(selectedDate, EventBus.getData());
					if (beforeDateIndex != -1) {
						listView.setSelection(beforeDateIndex);
					} else {
						listView.setSelection(0);
					}
				}
			} else {
				dateSpinner.setSelection(2);
				listView.setSelection(EventBus.getData().size() - 1);
			}
		}
	};

	private OnItemClickListener listClickedListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
			selectedDate = EventBus.getData().get(pos).getDate();
			if (selectedDate.equals(getToday())) {
				dateSpinner.setSelection(2);
			} else {
				datePickerItems[1] = DATE_FORMAT_STRING.format(selectedDate);
				spinnerAdapter.notifyDataSetChanged();
				dateSpinner.setSelection(1);
			}
			input.setText(EventBus.getData().get(pos).getValue() + "");
			updateMinMaxValue();
			saveButton.setEnabled(isInputValueCorrect());
		}
	};

	private OnItemLongClickListener listLongClickListener = new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, final int pos, long id) {
			MyAlertDialog builder = new MyAlertDialog(getActivity());
			builder.setTitle(R.string.data_fragment_delete_dialog_title);
			builder.setIcon(R.drawable.ic_menu_delete);
			builder.setMessage(String.format(getActivity().getString(R.string.data_fragment_delete_dialog_message), EventBus.getData().get(pos)
			        .getStringDate()));
			builder.setMyNegativeButton(R.string.data_fragment_delete_dialog_negative, null);
			builder.setMyPositiveButton(R.string.data_fragment_delete_dialog_positive, new OnClickListener() {
				@Override
				public void onClick(View v) {
					DeleteRow deleteTask = new DeleteRow(getActivity());
					deleteTask.execute(EventBus.getData().get(pos));
				}
			});
			builder.show();
			return false;
		}
	};

	private BroadcastReceiver btReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (!device.getAddress().equals(BluetoothAdapter.getDefaultAdapter().getAddress()) && btDeviceList != null
				        && !btDeviceList.contains(device)) {
					btDeviceList.add(device);
				}
			} else {
				if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					if (bluetoothScanDialog != null) {
						bluetoothScanDialog.dismiss();
					}
					getActivity().unregisterReceiver(this);
					for (BluetoothDevice device : btDeviceList) {
						System.out.println(device.getAddress() + " " + device.getName());
					}
					if (btDeviceList.size() > 0) {
						MyAlertDialog dialog = new MyAlertDialog(getActivity());
						dialog.setTitle(R.string.data_fragment_import_bluetooth_list_dialog_title);
						String[] names = new String[btDeviceList.size()];
						for (int i = 0; i < btDeviceList.size(); i++) {
							names[i] = btDeviceList.get(i).getName();
						}
						dialog.setItems(names, new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
								new ConnectBluetoothThread(btDeviceList.get(pos)).run();
							}
						});
						dialog.show();

					} else {
						MyAlertDialog dialog = new MyAlertDialog(getActivity());
						dialog.setTitle(R.string.data_fragment_import_bluetooth_nodevices_dialog_title);
						dialog.setMessage(R.string.data_fragment_import_bluetooth_nodevices_dialog_message);
						dialog.setMyNegativeButton(R.string.data_fragment_import_bluetooth_nodevices_dialog_negative, null);
						dialog.setMyPositiveButton(R.string.data_fragment_import_bluetooth_nodevices_dialog_positive, new OnClickListener() {
							@Override
							public void onClick(View v) {
								startBluetooth();
							}
						});
						dialog.show();
					}
				}
			}
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		EventBus.getInstance().register(this);
		setHasOptionsMenu(true);

		View view = inflater.inflate(R.layout.fragment_data, container, false);

		((NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(AlarmReceiver.NOTIFICATION_ID);

		noDataText = (TextView) view.findViewById(R.id.no_data_text);
		tableHead = (LinearLayout) view.findViewById(R.id.dataTable_head);

		listView = (ListView) view.findViewById(R.id.data_listView);
		adapter = new MyDataListAdapter(getActivity());
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(listClickedListener);
		listView.setOnItemLongClickListener(listLongClickListener);

		showListViewIfNecessery();

		selectedDate = getToday();
		currentMinValue = 0;
		currentMaxValue = -1;

		saveButton = (Button) view.findViewById(R.id.input_button);
		saveButton.setOnClickListener(saveButtonClickListener);
		input = (EditText) view.findViewById(R.id.input_editText);
		input.addTextChangedListener(inputTextWatcher);
		input.setOnEditorActionListener(inputActionListener);
		if (((MainActivity) getActivity()).getIntent().getBooleanExtra(AlarmReceiver.SHOW_KEYBOARD, false)) {
			input.requestFocus();
		}
		sizeText = (TextView) view.findViewById(R.id.data_size_text);
		updateSizeTextView();

		dateSpinner = (Spinner) view.findViewById(R.id.data_date_spinner);
		datePickerItems = getActivity().getResources().getStringArray(R.array.data_date_spinner);
		spinnerAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, datePickerItems);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		dateSpinner.setAdapter(spinnerAdapter);
		dateSpinner.setSelection(2);
		dateSpinner.setOnItemSelectedListener(spinnerListener);

		return view;
	}

	@Override
	public void onDestroy() {
		EventBus.getInstance().unregister(this);
		if (camera != null) {
			camera.release();
		}
		try {
			getActivity().unregisterReceiver(btReceiver);
		} catch (Exception e) {

		}
		super.onDestroy();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.data, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.data_menu_import:
//			MyAlertDialog dialog = new MyAlertDialog(getActivity());
//			dialog.setIcon(R.drawable.ic_menu_import);
//			dialog.setTitle(R.string.data_fragment_import_dialog_title);
//			dialog.setItems(R.array.data_import_types, new OnItemClickListener() {
//				@Override
//				public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
//					switch (pos) {
//					case 0:
//						Intent intent = new Intent(getActivity(), FileChooserActivity.class);
//						startActivityForResult(intent, FILE_RESQUEST_CODE);
//						break;
//					case 1:
//						startBluetooth();
//						break;
//					}
//				}
//			});
//			dialog.show();
			Intent intent = new Intent(getActivity(), FileChooserActivity.class);
			startActivityForResult(intent, FILE_RESQUEST_CODE);
			break;
		case R.id.data_menu_export:
			if (EventBus.getData().size() == 0) {
				Toast.makeText(getActivity(), R.string.data_fragment_toast_noexport, Toast.LENGTH_LONG).show();
			} else {
				final DataExportFragment dialogFragment = new DataExportFragment();
				dialogFragment.setValues(EventBus.getData(), new OnClickListener() {
					@Override
					public void onClick(View v) {
						List<DataRow> showList = dialogFragment.getList();
						switch (dialogFragment.getCheckedRadioButtonId()) {
						case 0:
							ExportExcel exportExcelTask = new ExportExcel(DataTableFragment.this, showList);
							exportExcelTask.execute();
							break;
						case 1:
							Utils.addPreviousToList(getActivity(), EventBus.getData(), showList, EventBus.getData().indexOf(showList.get(0)));
							ExportPDF exportPDFTask = new ExportPDF(DataTableFragment.this, showList);
							exportPDFTask.execute(false);
							break;
						case 2:
							Utils.addPreviousToList(getActivity(), EventBus.getData(), showList, EventBus.getData().indexOf(showList.get(0)));
							ExportPDF exportPDFImageTask = new ExportPDF(DataTableFragment.this, showList);
							exportPDFImageTask.execute(true);
							break;
						}
					}
				});
				dialogFragment.show(getFragmentManager(), "exportDialog");
			}
			break;
		/* removed because i have no privacy policy
		case R.id.data_menu_led:
			try {
				if (camera == null) {
					camera = Camera.open();
				}
				camera.reconnect();
				if (camera.getParameters().getFlashMode() == null) {
					Toast.makeText(getActivity(), R.string.data_fragment_toast_noled, Toast.LENGTH_LONG).show();
					break;
				}
				Parameters params = camera.getParameters();
				if (params.getFlashMode().equals(Parameters.FLASH_MODE_TORCH)) {
					params.setFlashMode(Parameters.FLASH_MODE_OFF);
				} else {
					if (camera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_OFF)) {
						params.setFlashMode(Parameters.FLASH_MODE_TORCH);
					}
				}
				camera.setParameters(params);
				camera.startPreview();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
			break;
		*/
		}
		return false;
	}

	private void startBluetooth() {
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		if (btAdapter == null) {
			Toast.makeText(getActivity(), R.string.data_fragment_toast_bluetooth_notavailible, Toast.LENGTH_LONG).show();
			return;
		}

		if (!btAdapter.isEnabled()) {
			Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(turnOn, BLUTOOTH_TURN_ON_REQUEST_CODE);
		} else {
			onBluetoothActive();
		}
	}

	private void onBluetoothActive() {
		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		getActivity().registerReceiver(btReceiver, filter);
		btDeviceList = new ArrayList<BluetoothDevice>();
		btAdapter.startDiscovery();
		bluetoothScanDialog = new MyProgressDialog(getActivity());
		bluetoothScanDialog.setMessage(getActivity().getString(R.string.data_fragment_import_bluetooth_progressdialog_message));
		bluetoothScanDialog.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		switch (requestCode) {
		case FILE_RESQUEST_CODE:
			if (!data.getExtras().getString(FileChooserActivity.EXTRA_RESULT_PATH).endsWith(".xls")) {
				Toast.makeText(getActivity(), R.string.data_fragment_toast_onlyxls, Toast.LENGTH_LONG).show();
				break;
			}
			findTask = new FindImportData(EventBus.getData(), this);
			findTask.execute(data.getExtras().getString(FileChooserActivity.EXTRA_RESULT_PATH));
			break;
		case BLUTOOTH_TURN_ON_REQUEST_CODE:
			onBluetoothActive();
			break;
		}
	}

	@Override
	public void onStop() {
		if (findTask != null) {
			findTask.cancel(true);
		}
		super.onStop();
	}

	public void updateUI() {
		notifyDataSetChanged();
		input.setText("");
		if (!selectedDate.equals(getToday())) {
			selectedDate = new Date(selectedDate.getTime() + 86400 * 1000);
			if (selectedDate.equals(getToday())) {
				dateSpinner.setSelection(2);
			} else {
				datePickerItems[1] = DATE_FORMAT_STRING.format(selectedDate);
				spinnerAdapter.notifyDataSetChanged();
				dateSpinner.setSelection(1);
			}
		}
		updateSizeTextView();
		updateMinMaxValue();
	}

	public void updateMinMaxValue() {
		if (EventBus.getData().size() == 0) {
			currentMinValue = 0;
			currentMaxValue = -1;
			return;
		}

		int index = EventBus.getData().indexOf(new DataRow(selectedDate, 0, 0));
		if (index == -1) {
			int nearDateIndex = Utils.getBeforeDateIndex(selectedDate, EventBus.getData());
			currentMinValue = nearDateIndex == -1 ? 0 : EventBus.getData().get(nearDateIndex).getValue();
			currentMaxValue = nearDateIndex + 1 < EventBus.getData().size() ? EventBus.getData().get(nearDateIndex + 1).getValue() : -1;
		} else {
			currentMinValue = index - 1 >= 0 ? EventBus.getData().get(index - 1).getValue() : 0;
			currentMaxValue = index + 1 < EventBus.getData().size() ? EventBus.getData().get(index + 1).getValue() : -1;
		}
	}

	private void updateSizeTextView() {
		sizeText.setText(Utils.getUnit(getActivity()));
	}

	private void showListViewIfNecessery() {
		if (EventBus.getData().size() > 0) {
			listView.setVisibility(View.VISIBLE);
			tableHead.setVisibility(View.VISIBLE);
			noDataText.setVisibility(View.GONE);
		} else {
			listView.setVisibility(View.GONE);
			tableHead.setVisibility(View.GONE);
			noDataText.setVisibility(View.VISIBLE);
		}
	}

	private boolean isInputValueCorrect() {
		if (input.getText().toString().equals("")) {
			return false;
		}
		int currentValue = Integer.parseInt(input.getText().toString());
		if (currentMaxValue == -1) {
			return currentValue >= currentMinValue;
		} else {
			return currentValue >= currentMinValue && currentValue <= currentMaxValue;
		}
	}

	private static Date getToday() {
		return new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance()
		        .get(Calendar.DATE)).getTime();
	}

	public void notifyDataSetChanged() {
		showListViewIfNecessery();
		adapter.updateValueSize();
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onDataChanged() {
		updateUI();
	}

	@Override
	public void onDataLoaded() {
		updateUI();
	}

	@Override
	public void onSystemUpdate() {
	}
}