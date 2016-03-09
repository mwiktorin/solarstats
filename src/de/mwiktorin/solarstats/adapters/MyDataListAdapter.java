package de.mwiktorin.solarstats.adapters;

import java.util.Date;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.mwiktorin.solarstats.EventBus;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.model.DataRow;

public class MyDataListAdapter extends BaseAdapter {

	private Context context;
	private String valueSize;

	public MyDataListAdapter(Context context) {
		this.context = context;
		updateValueSize();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			v = inflater.inflate(R.layout.data_row, parent, false);
		}

		TextView dateView = (TextView) v.findViewById(R.id.row_date_textView);
		TextView valueView = (TextView) v.findViewById(R.id.row_value_textView);
		TextView producedView = (TextView) v.findViewById(R.id.row_produced_textView);

		dateView.setText(getItem(position).getStringDate());
		valueView.setText(getItem(position).getValue() + " " + valueSize);
		if (position == 0) {
			Date startDate = EventBus.getSystems().get(Utils.getCurrentSystemPosition(context)).getDate();
			if(startDate.equals(getItem(0).getDate())){
				producedView.setText(valueView.getText());
			} else {
				int dayDiff = Utils.getDayDiff(startDate, getItem(0).getDate()) + 1;
				int produced = (int) Math.round((double) getItem(0).getValue() / dayDiff);
				String tilde = dayDiff > 1 ? "~ " : "";
				producedView.setText(tilde + produced + " " + valueSize);
			}
		} else {
			int valueDiff = getItem(position).getValue() - getItem(position - 1).getValue();
			int dayDiff = Utils.getDayDiff(getItem(position - 1).getDate(), getItem(position).getDate());
			int produced = (int) Math.round((double) valueDiff / dayDiff);
			String tilde = dayDiff > 1 ? "~ " : "";
			producedView.setText(tilde + produced + " " + valueSize);
		}

		return v;
	}

	@Override
	public int getCount() {
		return EventBus.getData().size();
	}

	@Override
	public DataRow getItem(int position) {
		return EventBus.getData().get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	public void updateValueSize() {
		valueSize = Utils.getUnit(context);
	}	
}
