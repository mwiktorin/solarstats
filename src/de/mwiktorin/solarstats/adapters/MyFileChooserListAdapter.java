package de.mwiktorin.solarstats.adapters;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.activities.FileChooserActivity;

public class MyFileChooserListAdapter extends BaseAdapter {

	private final static SimpleDateFormat DATE_FORMAT_STRING = new SimpleDateFormat("dd.MM.yyyy", Locale.US);

	private FileChooserActivity activity;
	private List<File> list;

	public MyFileChooserListAdapter(FileChooserActivity activity, List<File> list) {
		this.activity = activity;
		this.list = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = LayoutInflater.from(activity);
			v = inflater.inflate(R.layout.file_row, parent, false);
		}

		TextView name = (TextView) v.findViewById(R.id.file_row_name);
		TextView date = (TextView) v.findViewById(R.id.file_row_date);
		TextView size = (TextView) v.findViewById(R.id.file_row_size);

		name.setText(getItem(position).getName());
		name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		date.setText("");
		size.setText("");
		if(position == 0 && !activity.getPath().equals(Environment.getExternalStorageDirectory().getAbsolutePath())){
			name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_back, 0, 0, 0);
		} else {
			if (!getItem(position).isDirectory()) {
				date.setText(DATE_FORMAT_STRING.format(new Date(getItem(position).lastModified())));
				size.setText(readableFileSize(getItem(position).length()));
			} else {
				name.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_archive, 0, 0, 0);
			}
		}
		
		return v;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public File getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setList(List<File> newList) {
		list = newList;
		notifyDataSetChanged();
	}

	private String readableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
