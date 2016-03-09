package de.mwiktorin.solarstats.adapters;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;

public class MyExportedListAdapter extends BaseAdapter {

	private final static SimpleDateFormat DATE_FORMAT_STRING = new SimpleDateFormat("dd.MM.yyyy", Locale.US);

	private Context context;
	private List<File> list;

	public MyExportedListAdapter(Context context, List<File> list) {
		this.context = context;
		this.list = list;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			v = inflater.inflate(R.layout.file_row, parent, false);
		}

		TextView name = (TextView) v.findViewById(R.id.file_row_name);
		TextView date = (TextView) v.findViewById(R.id.file_row_date);
		TextView size = (TextView) v.findViewById(R.id.file_row_size);

		name.setText(getItem(position).getName());
		date.setText(DATE_FORMAT_STRING.format(new Date(getItem(position).lastModified())));
		size.setText(readableFileSize(getItem(position).length()));
		String ending = Utils.getFileEnding(getItem(position));
		if (ending.equals(".png") || ending.equals(".PNG") || ending.equals(".jpeg") || ending.equals(".JPEG") || ending.equals(".jpg")
		        || ending.equals(".JPG")) {
			name.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.imgicon), null, null, null);
		} else {
			if (ending.equals(".xls") || ending.equals(".XLS")) {
				name.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.xlsicon), null, null, null);
			} else {
				if (ending.equals(".pdf") || ending.equals(".PDF")) {
					name.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.pdficon), null, null, null);
				} else {
					name.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(R.drawable.unknownicon), null, null, null);
				}
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

	private String readableFileSize(long size) {
		if (size <= 0)
			return "0";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
}
