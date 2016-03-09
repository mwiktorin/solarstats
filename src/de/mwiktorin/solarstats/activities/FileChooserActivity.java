package de.mwiktorin.solarstats.activities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.adapters.MyFileChooserListAdapter;

public class FileChooserActivity extends ListActivity {

	public static final String EXTRA_RESULT_PATH = "path";

	private TextView pathTextView;
	private String path;
	private List<File> files;
	private MyFileChooserListAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_chooser);
		
		getActionBar().setHomeButtonEnabled(true);

		path = Environment.getExternalStorageDirectory().getAbsolutePath();
		pathTextView = (TextView) findViewById(R.id.file_chooser_text);
		pathTextView.setText(path);
		loadFiles();
		adapter = new MyFileChooserListAdapter(this, files);
		getListView().setAdapter(adapter);
		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				if (pos == 0 && !path.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
					back();
					return;
				}
				if (!files.get(pos).isFile()) {
					addToPath(files.get(pos).getName());
				} else {
					Intent intent = new Intent();
					intent.putExtra(EXTRA_RESULT_PATH, path + File.separator + files.get(pos).getName());
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		});
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
	    case android.R.id.home:
	    	finish();
	        break;
	    }
	    return true;
	}
	
	@Override
	public void onBackPressed() {
	    if(path.equals(Environment.getExternalStorageDirectory().getAbsolutePath())){
	    	super.onBackPressed();
	    } else {
	    	back();
	    }
	}
	
	public String getPath() {
	    return path;
    }

	private void addToPath(String path) {
		this.path += File.separator + path;
		loadFiles();
		adapter.setList(files);
		pathTextView.setText(this.path);
	}

	private void back() {
		path = new File(path).getParent();
		loadFiles();
		adapter.setList(files);
		pathTextView.setText(path);
	}

	private void loadFiles() {
		File dir = new File(path);
		File[] fileArray = dir.listFiles();
		files = new ArrayList<File>();
		if (!path.equals(Environment.getExternalStorageDirectory().getAbsolutePath())) {
			files.add(new File(dir, getString(R.string.file_chooser_back)));
		}
		if (fileArray != null) {
			for (int i = 0; i < fileArray.length; i++) {
				if (!fileArray[i].isHidden()) {
					if (!fileArray[i].isDirectory()) {
						if (!Utils.getFileEnding(fileArray[i]).equals(".xls")) {
							continue;
						}
					}
					files.add(fileArray[i]);
				}
			}
		}
	}
}
