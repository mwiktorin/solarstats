package de.mwiktorin.solarstats.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.EventBus.EventListener;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.dialogs.MyAlertDialog;
import de.mwiktorin.solarstats.dialogs.SolarsystemFragment;
import de.mwiktorin.solarstats.model.System;
import de.mwiktorin.solarstats.tasks.DeleteSystem;
import de.mwiktorin.solarstats.tasks.SaveSystem;
import de.mwiktorin.solarstats.tasks.SolarsystemLoadData;
import de.mwiktorin.solarstats.views.PercentBarView;

public class SolarsystemsFragment extends Fragment implements EventListener {

	private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
	private LinearLayout layout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		EventBus.getInstance().register(this);
		setHasOptionsMenu(true);
		View view = inflater.inflate(R.layout.fragment_solarsystems, container, false);

		layout = (LinearLayout) view.findViewById(R.id.solarsystems_layout);

		startLoadingTask();

		return view;
	}

	private void startLoadingTask() {
		new SolarsystemLoadData(this).execute();
	}

	@Override
	public void onDestroy() {
		EventBus.getInstance().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.solarsystems, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.solarsystems_menu_add:
			final SolarsystemFragment dialogFragment = new SolarsystemFragment();
			dialogFragment.setListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SaveSystem saveTask = new SaveSystem(getActivity());
					saveTask.execute(new System(-1, dialogFragment.getName(), dialogFragment.getDate(), dialogFragment.getPeak(), dialogFragment
					        .getPayment(), dialogFragment.getColor()));
				}
			});
			dialogFragment.show(getFragmentManager(), "newSolarsystemDialog");
			break;
		}
		return true;
	}

	public void onSystemsLoaded(List<HashMap<String, Object>> data) {

		if (data.size() > 1) {
			LinearLayout totalLayout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.solar_system_total, layout, false);

			int totalReadings = 0;
			int totalValue = 0;
			double totalEfficiency = 0;
			double totalEarnings = 0;
			for (HashMap<String, Object> map : data) {
				totalReadings += (Integer) map.get(SolarsystemLoadData.KEY_COUNT);
				if (map.get(SolarsystemLoadData.KEY_LASTVALUE) != null) {
					totalValue += (Integer) map.get(SolarsystemLoadData.KEY_LASTVALUE);
				}
				if (map.get(SolarsystemLoadData.KEY_EFFICIENCY) != null) {
					totalEfficiency += (Double) map.get(SolarsystemLoadData.KEY_EFFICIENCY);
				}
				if (map.get(SolarsystemLoadData.KEY_PROFIT) != null) {
					totalEarnings += (Double) map.get(SolarsystemLoadData.KEY_PROFIT);
				}
			}
			totalEfficiency = totalEfficiency / data.size();

			((TextView) totalLayout.findViewById(R.id.solar_system_meter_readings)).setText("" + totalReadings);
			if (totalValue != 0) {
				((TextView) totalLayout.findViewById(R.id.solar_system_total_value)).setText((Integer) totalValue + " "
				        + Utils.getUnit(getActivity()));
			}
			if (totalEfficiency != 0) {
				((TextView) totalLayout.findViewById(R.id.solar_system_efficiency)).setText(Utils.getIntString(totalEfficiency * 100) + " %");
			}
			if (totalEarnings != 0) {
				((TextView) totalLayout.findViewById(R.id.solar_system_earnings)).setText(Utils.getDoubleString(totalEarnings)
				        + Currency.getInstance(Locale.getDefault()).getSymbol());
			}

			if (totalValue != 0) {
				PercentBarView percentView = (PercentBarView) totalLayout.findViewById(R.id.percentBarView);
				List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
				for (int i = 0; i < data.size(); i++) {
					if (data.get(i).get(SolarsystemLoadData.KEY_LASTVALUE) == null) {
						continue;
					}
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put(PercentBarView.KEY_VALUE, (double) (Integer) data.get(i).get(SolarsystemLoadData.KEY_LASTVALUE) / totalValue);
					map.put(PercentBarView.KEY_COLOR, ((System) data.get(i).get(SolarsystemLoadData.KEY_SYSTEM)).getColor());
					map.put(PercentBarView.KEY_NAME, ((System) data.get(i).get(SolarsystemLoadData.KEY_SYSTEM)).getName());
					list.add(map);
				}
				percentView.setData(list);
			}
			layout.addView(totalLayout);
		}

		LinearLayout systemLayout = null;
		for (int i = 0; i < data.size(); i++) {
			final HashMap<String, Object> map = data.get(i);
			final System system = (System) map.get(SolarsystemLoadData.KEY_SYSTEM);
			systemLayout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.solar_system, layout, false);
			((TextView) systemLayout.findViewById(R.id.solar_system_name)).setText(system.getName());
			if (system.getDate() != null) {
				((TextView) systemLayout.findViewById(R.id.solar_system_date)).setText(DATE_FORMAT.format(system.getDate()));
			}
			systemLayout.findViewById(R.id.solar_system_background).setBackgroundColor(system.getColor());
			systemLayout.findViewById(R.id.solar_system_divider).setBackgroundColor(system.getColor());

			((ImageButton) systemLayout.findViewById(R.id.solar_system_edit)).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final SolarsystemFragment dialogFragment = new SolarsystemFragment();
					dialogFragment.setSystem(system);
					if (map.get(SolarsystemLoadData.KEY_FIRSTDATE) != null) {
						dialogFragment.setMaxDate((Date) map.get(SolarsystemLoadData.KEY_FIRSTDATE));
					}
					dialogFragment.setListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							SaveSystem saveTask = new SaveSystem(getActivity());
							saveTask.execute(new System(system.getId(), dialogFragment.getName(), dialogFragment.getDate(), dialogFragment.getPeak(),
							        dialogFragment.getPayment(), dialogFragment.getColor()));
						}
					});
					dialogFragment.show(getFragmentManager(), "newSolarsystemDialog");
				}
			});

			if (EventBus.getSystems().size() > 1) {
				((ImageButton) systemLayout.findViewById(R.id.solar_system_delete)).setVisibility(View.VISIBLE);
				((ImageButton) systemLayout.findViewById(R.id.solar_system_delete)).setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						MyAlertDialog dialog = new MyAlertDialog(getActivity());
						dialog.setIcon(R.drawable.ic_menu_delete);
						dialog.setTitle(R.string.solarsystems_fragment_dialog_delete_title);
						dialog.setMessage(R.string.solarsystems_fragment_dialog_delete_message);
						dialog.setMyNegativeButton(R.string.solarsystems_fragment_dialog_delte_negative, null);
						dialog.setMyPositiveButton(R.string.solarsystems_fragment_dialog_delete_positive, new OnClickListener() {
							@Override
							public void onClick(View v) {
								DeleteSystem deleteTask = new DeleteSystem(getActivity());
								deleteTask.execute(system);
							}
						});
						dialog.show();
					}
				});
			}

			int peak = system.getPeak();
			if (peak == 0) {
				((TextView) systemLayout.findViewById(R.id.solar_system_peak)).setText(R.string.solarsystems_fragment_notset);
			} else {
				((TextView) systemLayout.findViewById(R.id.solar_system_peak)).setText(peak + " " + Utils.getPeakUnit(getActivity()));
			}
			((TextView) systemLayout.findViewById(R.id.solar_system_payment_left)).setText(String.format(
			        getActivity().getString(R.string.solarsystems_fragment_dialog_new_payment), Utils.getUnit(getActivity())));
			double payment = system.getPayment();
			if (payment == 0) {
				((TextView) systemLayout.findViewById(R.id.solar_system_payment_right)).setText(R.string.solarsystems_fragment_notset);
			} else {
				((TextView) systemLayout.findViewById(R.id.solar_system_payment_right)).setText(Utils.getDoubleString(payment) + " "
				        + getActivity().getString(R.string.solarsystems_fragment_dialog_new_cent));
			}

			((TextView) systemLayout.findViewById(R.id.solar_system_meter_readings)).setText("" + (Integer) map.get(SolarsystemLoadData.KEY_COUNT));
			if (map.get(SolarsystemLoadData.KEY_LASTVALUE) != null) {
				((TextView) systemLayout.findViewById(R.id.solar_system_total_value)).setText((Integer) map.get(SolarsystemLoadData.KEY_LASTVALUE)
				        + " " + Utils.getUnit(getActivity()));
			}
			if (map.get(SolarsystemLoadData.KEY_EFFICIENCY) != null) {
				((TextView) systemLayout.findViewById(R.id.solar_system_efficiency)).setText(Utils.getIntString((Double) map
				        .get(SolarsystemLoadData.KEY_EFFICIENCY) * 100) + " %");
			}
			if (map.get(SolarsystemLoadData.KEY_PROFIT) != null) {
				((TextView) systemLayout.findViewById(R.id.solar_system_earnings)).setText(Utils.getDoubleString((Double) map
				        .get(SolarsystemLoadData.KEY_PROFIT)) + Currency.getInstance(Locale.getDefault()).getSymbol());
			}
			layout.addView(systemLayout);
		}
		if (systemLayout != null) {
			((LinearLayout.LayoutParams) systemLayout.getLayoutParams()).bottomMargin = 0;
		}
	}

	@Override
	public void onDataLoaded() {
	}

	@Override
	public void onDataChanged() {
	}

	@Override
	public void onSystemUpdate() {
		layout.removeAllViews();
		startLoadingTask();
	}

}