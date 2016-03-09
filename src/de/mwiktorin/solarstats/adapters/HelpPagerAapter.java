package de.mwiktorin.solarstats.adapters;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import de.mwiktorin.solarstats.fragments.HelpTextFragment;


public class HelpPagerAapter extends FragmentPagerAdapter {

	private String[] titles;
	
	public HelpPagerAapter(FragmentManager fm, String[] titles) {
		super(fm);
		this.titles = titles;
	}

	@Override
	public android.support.v4.app.Fragment getItem(int pos) {
		Bundle b = new Bundle();
		b.putInt(HelpTextFragment.BUNDLE_POS_KEY, pos);
		HelpTextFragment fragment = new HelpTextFragment();
		fragment.setArguments(b);
		return fragment;
	}

	@Override
	public int getCount() {
		return titles.length;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
	    return titles[position];
	}

}