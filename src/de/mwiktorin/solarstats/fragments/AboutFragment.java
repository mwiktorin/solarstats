package de.mwiktorin.solarstats.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.dialogs.MyAlertDialog;

public class AboutFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_about, container, false);
		TextView infoTextView = (TextView) view.findViewById(R.id.about_info);
		infoTextView.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView imprintTextView = (TextView) view.findViewById(R.id.about_imprint);
		imprintTextView.setMovementMethod(LinkMovementMethod.getInstance());
		
		TextView copyRightTextView = (TextView) view.findViewById(R.id.about_apwcopyright);
		copyRightTextView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MyAlertDialog dialog = new MyAlertDialog(getActivity());
				dialog.setTitle(R.string.about_fragment_apwdialog_title);
				dialog.setMessage(R.string.about_fragment_apwdialog_message);
				dialog.setMyPositiveButton(R.string.about_fragment_apwdialog_positive, null);
				dialog.show();
			}
		});

		Button rateButton = (Button) view.findViewById(R.id.about_rate_button);
		rateButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String url = getActivity().getString(R.string.about_fragment_rate_url);
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
		});

		Button shareButton = (Button) view.findViewById(R.id.about_share_button);
		shareButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String shareBody = getActivity().getString(R.string.about_fragment_share_text) + getActivity().getString(R.string.about_fragment_rate_url);
				Intent sharingIntent = new Intent(Intent.ACTION_SEND);
				sharingIntent.setType("text/plain");
				sharingIntent.putExtra(Intent.EXTRA_SUBJECT, getActivity().getString(R.string.about_fragment_share_subject));
				sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
				startActivity(Intent.createChooser(sharingIntent, getActivity().getString(R.string.about_fragment_share_chooser)));
			}
		});

		return view;
	}
}
