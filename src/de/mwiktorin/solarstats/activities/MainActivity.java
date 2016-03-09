package de.mwiktorin.solarstats.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.EventBus.EventListener;
import de.mwiktorin.solarstats.Preferences;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.fragments.AboutFragment;
import de.mwiktorin.solarstats.fragments.DataTableFragment;
import de.mwiktorin.solarstats.fragments.DiagramFragment;
import de.mwiktorin.solarstats.fragments.ExportedFragment;
import de.mwiktorin.solarstats.fragments.HelpFragment;
import de.mwiktorin.solarstats.fragments.SolarsystemsFragment;
import de.mwiktorin.solarstats.tasks.LoadData;

public class MainActivity extends FragmentActivity implements EventListener {

	public static final String OUTSTATE_SUBTITLE = "subtitle";
	public static final String OUTSTATE_TITLE = "title";
	public static final String OUTSTATE_POSITION = "position";

	private static final int[] ICONS = new int[] { R.drawable.ic_menu_sort_by_size, R.drawable.ic_menu_gallery,
	        R.drawable.ic_menu_home, R.drawable.ic_menu_archive, R.drawable.ic_menu_help, R.drawable.ic_menu_info_details };

	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private int currentPosition;
	private View mLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private Fragment fragment;
	private FragmentManager fragmentManager;
	private String[] menuItems;
	private ArrayAdapter<String> arrayAdapter;
	private long lastBackPressed;
	private Spinner spinner;
	private ArrayAdapter<String> spinnerAdapter;

	private OnItemSelectedListener systemSpinnerListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
			if (EventBus.getSystems().get(pos).getId() != Preferences.getInstance(MainActivity.this).getLong(Preferences.CURRENT_SYSTEM_ID, 1)) {
				Preferences.getInstance(MainActivity.this).putLong(Preferences.CURRENT_SYSTEM_ID, EventBus.getSystems().get(pos).getId());
				LoadData loadTask = new LoadData(MainActivity.this, true);
				loadTask.execute();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		EventBus.getInstance().register(this);

		Button settingButton = (Button) findViewById(R.id.drawer_settings_button);
		settingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, SettingsActivity.class));
				mDrawerLayout.closeDrawer(mLayout);
			}
		});

		fragmentManager = getSupportFragmentManager();
		menuItems = getResources().getStringArray(R.array.sideMenu);

		arrayAdapter = new ArrayAdapter<String>(this, R.layout.drawer_list_item, menuItems) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				TextView textView = (TextView) view.findViewById(android.R.id.text1);
				textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(ICONS[position]), null, null, null);
				textView.setTextColor(Color.BLACK);

				view.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

				return view;
			}
		};

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
			@Override
			public void onDrawerOpened(View drawerView) {
				Utils.hideKeyboard(MainActivity.this);
				super.onDrawerOpened(drawerView);
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
		mLayout = findViewById(R.id.left_drawer);

		spinner = (Spinner) findViewById(R.id.left_spinner);
		initSpinnerAdapter();
		spinner.setOnItemSelectedListener(systemSpinnerListener);

		mDrawerList.setAdapter(arrayAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
		if (savedInstanceState != null) {
			getActionBar().setTitle(savedInstanceState.getCharSequence(OUTSTATE_TITLE));
			getActionBar().setSubtitle(savedInstanceState.getCharSequence(OUTSTATE_SUBTITLE));
			getActionBar().setIcon(ICONS[savedInstanceState.getInt(OUTSTATE_POSITION)]);
		} else {
			selectItem(0);
		}

	}

	private void initSpinnerAdapter() {
		String[] array = getSystemsArray();
		spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, array);
		spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerAdapter);
		int index = Utils.getCurrentSystemPosition(this);
		if (index != -1) {
			spinner.setSelection(index);
		}
	}

	private String[] getSystemsArray() {
		String[] array = new String[EventBus.getSystems().size()];
		for (int i = 0; i < array.length; i++) {
			array[i] = EventBus.getSystems().get(i).getName();
		}
		return array;
	}

	@Override
	protected void onDestroy() {
		EventBus.getInstance().unregister(this);
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		if (lastBackPressed == 0 || System.currentTimeMillis() - lastBackPressed > 2000) {
			Toast.makeText(this, R.string.toast_finish_app, Toast.LENGTH_SHORT).show();
		} else {
			super.onBackPressed();
		}
		lastBackPressed = System.currentTimeMillis();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putCharSequence(OUTSTATE_TITLE, getActionBar().getTitle());
		outState.putCharSequence(OUTSTATE_SUBTITLE, getActionBar().getSubtitle());
		outState.putInt(OUTSTATE_POSITION, currentPosition);
		super.onSaveInstanceState(outState);
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	public void selectItem(int position) {
		currentPosition = position;
		getActionBar().setSubtitle(null);
		switch (position) {
		case 0:
			fragment = new DataTableFragment();
			getActionBar().setSubtitle(Utils.getCurrentSystemTitle(this));
			break;
		case 1:
			fragment = new DiagramFragment();
			getActionBar().setSubtitle(Utils.getCurrentSystemTitle(this));
			break;
		case 2:
			fragment = new SolarsystemsFragment();
			break;
		case 3:
			fragment = new ExportedFragment();
			break;
		case 4:
			fragment = new HelpFragment();
			break;
		case 5:
			fragment = new AboutFragment();
			break;
		}
		getActionBar().setIcon(ICONS[position]);
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

		mDrawerList.setItemChecked(position, true);
		getActionBar().setTitle(menuItems[position]);
		mDrawerLayout.closeDrawer(mLayout);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	// TODO DEBUG

	@Override
	protected void onStart() {
		super.onStart();
//		 EasyTracker.getInstance().activityStart(this);
		//Tracker in Utils !!!!!!!
	}

	@Override
	protected void onStop() {
		super.onStop();
//		 EasyTracker.getInstance().activityStop(this);
	}

	@Override
	public void onDataChanged() {
	}

	@Override
	public void onDataLoaded() {
		if (mDrawerList.getCheckedItemPosition() == 0 || mDrawerList.getCheckedItemPosition() == 1) {
			getActionBar().setSubtitle(Utils.getCurrentSystemTitle(this));
		}
	}

	@Override
	public void onSystemUpdate() {
		initSpinnerAdapter();
	}
}
