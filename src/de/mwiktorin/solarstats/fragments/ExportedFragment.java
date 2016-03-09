package de.mwiktorin.solarstats.fragments;

import java.io.File;
import java.util.ArrayList;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import de.mwiktorin.solarstats.R;
import de.mwiktorin.solarstats.Utils;
import de.mwiktorin.solarstats.adapters.MyExportedListAdapter;
import de.mwiktorin.solarstats.dialogs.MyAlertDialog;
import de.mwiktorin.solarstats.dialogs.RenameFileFragment;

public class ExportedFragment extends Fragment {

	private ArrayList<File> files;
	private MyExportedListAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		loadFiles();
		if (files.size() == 0) {
			return inflater.inflate(R.layout.fragment_exported_no_data, container, false);
		}
		View view = inflater.inflate(R.layout.fragment_exported, container, false);
		ListView listView = (ListView) view.findViewById(R.id.exported_list);
		adapter = new MyExportedListAdapter(getActivity(), files);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				Utils.openFile(files.get(pos), getActivity());
			}
		});

		return view;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getActivity().getMenuInflater();
		inflater.inflate(R.menu.exported_context, menu);
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		final int pos = (int) ((AdapterContextMenuInfo) item.getMenuInfo()).id;
		switch (item.getItemId()) {
		case R.id.exported_context_open:
			Utils.openFile(files.get(pos), getActivity());
			break;
		case R.id.exported_context_rename:
			final RenameFileFragment dialogFragment = new RenameFileFragment();
			dialogFragment.setValues(files.get(pos).getName().replaceFirst("[.][^.]+$", ""), Utils.getFileEnding(files.get(pos)),
			        new View.OnClickListener() {
				        @Override
				        public void onClick(View v) {
					        if (dialogFragment.getEditString().contains(".")) {
						        Toast.makeText(getActivity(), R.string.exported_fragment_toast_wrong_char_filename, Toast.LENGTH_LONG).show();
					        } else {
						        String folder = Environment.getExternalStorageDirectory() + File.separator
						                + getActivity().getString(R.string.export_foldername) + File.separator;
						        String newFileName = folder + dialogFragment.getEditString() + Utils.getFileEnding(files.get(pos));
						        File newFile = new File(newFileName);
						        if (newFile.exists()) {
							        Toast.makeText(getActivity(), R.string.exported_fragment_toast_file_exists, Toast.LENGTH_LONG).show();
							        return;
						        }
						        if (!files.get(pos).renameTo(newFile)) {
							        Toast.makeText(getActivity(), R.string.exported_fragment_toast_wrong_char_filename, Toast.LENGTH_LONG).show();
							        return;
						        }
						        files.set(pos, newFile);
						        adapter.notifyDataSetChanged();

					        }
				        }
			        });
			dialogFragment.show(getActivity().getFragmentManager(), "renameDialog");
			break;
		case R.id.exported_context_delete:
			MyAlertDialog builder = new MyAlertDialog(getActivity());
			builder.setTitle(R.string.exported_fragment_dialog_delete_title);
			builder.setIcon(R.drawable.ic_menu_delete);
			builder.setMessage(String.format(getActivity().getString(R.string.exported_fragment_dialog_delete_message), files.get(pos).getName()));
			builder.setMyNegativeButton(R.string.exported_fragment_dialog_delete_negative, null);
			builder.setMyPositiveButton(R.string.exported_fragment_dialog_delete_positive, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					files.get(pos).delete();
					files.remove(pos);
					adapter.notifyDataSetChanged();
				}
			});
			builder.show();
			break;
		case R.id.exported_context_share:
			Intent myIntent = new Intent(android.content.Intent.ACTION_SEND);
			String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(files.get(pos)).toString());
			String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			myIntent.setDataAndType(Uri.fromFile(files.get(pos)), mimetype);
			myIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(files.get(pos)));
			startActivity(Intent.createChooser(myIntent, getActivity().getString(R.string.exported_fragment_share_via)));
			break;
		}
		return false;
	}

	private void loadFiles() {
		String folder = Environment.getExternalStorageDirectory() + File.separator + getActivity().getString(R.string.export_foldername)
		        + File.separator;
		File dir = new File(folder);
		dir.mkdirs();
		files = new ArrayList<File>();
		File[] fileArray = dir.listFiles();
		for (int i = 0; i < fileArray.length; i++) {
			files.add(fileArray[i]);
		}
	}
}
