package de.mwiktorin.solarstats.activities;

import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.EventBus.EventListener;
import de.mwiktorin.solarstats.Preferences;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.receivers.AlarmReceiver;
import de.mwiktorin.solarstats.tasks.LoadData;

public class StartActivity extends FragmentActivity implements EventListener {

	private boolean loaded = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		EventBus.getInstance().register(this);

		int versionCode = 0;
		TextView version = (TextView) findViewById(R.id.start_activity_version);
		try {
			version.setText(String.format(getString(R.string.start_version), getPackageManager().getPackageInfo(getPackageName(), 0).versionName));
			versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
		}

		Preferences.getInstance(this).putInt(Preferences.LAST_VERSION, versionCode);
		
		if (Preferences.getInstance(this).getBoolean(Preferences.FIRST_START, true)) {
			startActivity(new Intent(this, FirstActivity.class));
			finish();
		} else {
			LoadData loadTask = new LoadData(this, false);
			loadTask.execute();

			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (loaded) {
						showMain();
					} else {
						loaded = true;
						findViewById(R.id.start_activity_loading).setVisibility(View.VISIBLE);
					}
				}

			}, 1500);
		}
	}

	@Override
	protected void onDestroy() {
		EventBus.getInstance().unregister(this);
		super.onDestroy();
	}

	@Override
	public void finish() {
		EventBus.getInstance().unregister(this);
		super.finish();
	}

	@Override
	public void onBackPressed() {
		// Do nothing
	}

	private void showMain() {
		Bundle b = new Bundle();
		b.putBoolean(AlarmReceiver.SHOW_KEYBOARD, getIntent().getBooleanExtra(AlarmReceiver.SHOW_KEYBOARD, false));
		Intent intent = new Intent(StartActivity.this, MainActivity.class);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}

	@Override
	public void onDataLoaded() {
		if (loaded) {
			showMain();
		} else {
			loaded = true;
		}
	}

	@Override
	public void onDataChanged() {
	}

	@Override
	public void onSystemUpdate() {
		showMain();
	}
}
