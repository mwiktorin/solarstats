package de.mwiktorin.solarstats.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import de.mwiktorin.solarstats.Preferences;
import de.mwiktorin.solarstats.R;

public class FirstActivity extends FragmentActivity {

	private ViewPager pager;

	public static class Fragment1 extends Fragment {
		private int id;

		public void setId(int id) {
			this.id = id;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			int res = getResources().getIdentifier("fragment_first_" + id, "layout", getActivity().getPackageName());
			View view = inflater.inflate(res, container, false);

			if (id != ((FirstActivity) getActivity()).getCount() - 1) {
				TextView forward = (TextView) view.findViewById(R.id.first_forward);
				forward.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						((FirstActivity) getActivity()).pageForward();
					}
				});
			}

			if (id != 1) {
				TextView backward = (TextView) view.findViewById(R.id.first_back);
				backward.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						((FirstActivity) getActivity()).pageBackward();
					}
				});
			}
			if(id == 6) {
				TextView forward = (TextView) view.findViewById(R.id.first_forward);
				forward.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Preferences.getInstance(getActivity()).putBoolean(Preferences.FIRST_START, false);
						Intent intent = new Intent(getActivity(), StartActivity.class);
						startActivity(intent);
						getActivity().finish();
					}
				});
			}

			return view;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_first);

		pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
			@Override
			public int getCount() {
				return 6;
			}

			@Override
			public Fragment getItem(int pos) {
				Fragment1 f = new Fragment1();
				f.setId(pos + 1);
				return f;
			}
		});
	}

	public void pageForward() {
		pager.setCurrentItem(pager.getCurrentItem() + 1, true);
	}

	public void pageBackward() {
		pager.setCurrentItem(pager.getCurrentItem() - 1, true);
	}
	
	public int getCount() {
		return pager.getChildCount();
	}

}
