package de.mwiktorin.solarstats.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import de.mwiktorin.solarstats.R;

public class HelpTextFragment extends Fragment{
	
	public static final String BUNDLE_POS_KEY = "position";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		int pos = getArguments().getInt(BUNDLE_POS_KEY);
		switch(pos){
		case 0:
			return inflater.inflate(R.layout.fragment_help_data, container, false);
		case 1:
			return inflater.inflate(R.layout.fragment_help_diagram, container, false);
		case 2:
			return inflater.inflate(R.layout.fragment_help_systems, container, false);
		case 3:
			return inflater.inflate(R.layout.fragment_help_exported, container, false);
		}
		return null;
	}

}
