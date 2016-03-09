package de.mwiktorin.solarstats.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.adapters.HelpPagerAapter;

public class HelpFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_help, container, false);

		ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		HelpPagerAapter adapter = new HelpPagerAapter(getChildFragmentManager(), getActivity().getResources()
		        .getStringArray(R.array.helpTabs));
		pager.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		return view;
	}

}