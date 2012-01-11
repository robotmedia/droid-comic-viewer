/*******************************************************************************
 * Copyright 2009 Robot Media SL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.robotmedia.acv.ui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import net.androidcomics.acv.R;
import net.robotmedia.acv.Constants;
import net.robotmedia.acv.logic.PreferencesController;
import net.robotmedia.acv.utils.ControllerUtils;
import net.robotmedia.acv.utils.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class SDBrowserActivity extends ListActivity {

	public class ListAdapter extends BaseAdapter {

		ArrayList<File> contents = new ArrayList<File>();
		private File current;

		public ListAdapter(Context context, File current) {
			this.current = current;
			filterContents();
		}

		private void filterContents() {
			String[] allContents = current.list();
			TreeMap<String, File> auxContents = new TreeMap<String, File>();
			contents = new ArrayList<File>();
			File parent = current.getParentFile();
			if (parent != null) {
				contents.add(parent);
			}
			if (allContents != null) {
				String path = current.getPath();
				for (int i = 0; i < allContents.length; i++) {
					String contentName = allContents[i];
					if (contentName.indexOf(".") != 0) { // Exclude hidden files
						String extension = Utils.getFileExtension(contentName);
						if (supportedExtensions.containsKey(extension.toLowerCase())) {
							File contentFile = new File(path, contentName);
							auxContents.put(contentFile.getName().toLowerCase(), contentFile);
						} else {
							File contentFile = new File(path, contentName);
							if (contentFile.isDirectory()) {
								auxContents.put(contentFile.getName().toLowerCase(), contentFile);
							}
						}
					}
				}
			}
			contents.addAll(auxContents.values());
		}

		public int getCount() {
			return contents.size();
		}

		public Object getItem(int position) {
			return contents.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (position == 0 && current.getParent() != null) { // First element
				TextView textView = (TextView) mInflater.inflate(
						android.R.layout.simple_list_item_1, parent, false);
				textView.setText(R.string.sd_browser_back);
				textView.setTag("back");
				return textView;
			} else {
				ViewHolder holder;
				if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
					convertView = mInflater.inflate(R.layout.sd_item, parent, false);
					holder = new ViewHolder();

					holder.icon = (ImageView) convertView.findViewById(R.id.sd_item_icon);
					holder.name = (TextView) convertView.findViewById(R.id.sd_item_name);
					holder.size = (TextView) convertView.findViewById(R.id.sd_item_size);
					
					convertView.setTag(holder);
				} else {
					holder = (ViewHolder) convertView.getTag();
				}

				File file = contents.get(position);
				String name = file.getName();
				String extension = Utils.getFileExtension(name);
				int iconId;
				if (supportedExtensions.containsKey(extension)) {
					iconId = supportedExtensions.get(extension);
				} else {
					iconId = R.drawable.folder;
				}

				holder.icon.setImageResource(iconId);
				holder.name.setText(name);
				if (file.isDirectory()) {
					holder.size.setVisibility(View.GONE);
				} else {
					holder.size.setVisibility(View.VISIBLE);
					long size = file.length() / 1024;
					holder.size.setText(String.valueOf(size) + " KB");
				}
				return convertView;
			}
		}

	}

	static class ViewHolder {
		ImageView icon;
		TextView name;
		TextView size;
	}
	
	private static final int NO_SD = 3;

	private static HashMap<String, Integer> supportedExtensions = null;
	private ListView listView;
	private LayoutInflater mInflater;
	private PreferencesController preferencesController;

	private void changeDirectory(File directory) {
		this.setTitle(directory.getName());
		preferencesController.savePreference(Constants.COMICS_PATH_KEY, directory.getAbsolutePath());
		listView.setAdapter(new ListAdapter(SDBrowserActivity.this, directory));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.sd_browser);
		supportedExtensions = ControllerUtils.getSupportedExtensions(this);
		preferencesController = new PreferencesController(this);
		
		String storageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(storageState)) {
			
	        Intent intent = getIntent();
	        String comicsPath = intent.getStringExtra(Constants.COMICS_PATH_KEY);
	        File directory;
			if (comicsPath != null) {
				directory = new File(comicsPath);
				if (!directory.isDirectory()) {
					directory = Environment.getExternalStorageDirectory();
				}
			} else {
				directory = Environment.getExternalStorageDirectory();
			}

			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			Button btn_download = (Button) findViewById(R.id.btn_download);
			btn_download.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(getString(R.string.market_uri)));
					startActivity(intent);
				}
			});
			Button btn_cancel = (Button) findViewById(R.id.btn_cancel);
			btn_cancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					finish();
				}
			});
			listView = getListView();
			changeDirectory(directory);
			listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					File file = (File) parent.getItemAtPosition(position);
					if ((file != null) && (file.isDirectory())) {
						String[] images = file.list(new FilenameFilter() {
							public boolean accept(File dir, String filename) {
								String ext = Utils.getFileExtension(filename);
								return Utils.isImage(ext);
							}});
						if (images.length > 0) {
							setResultAndFinish(file);
							return true;							
						}
					}
					return false;
				}
			});
			listView
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {

						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							File file = (File) parent
									.getItemAtPosition(position);
							if (file.isDirectory()) {
								changeDirectory(file);
							} else {
								setResultAndFinish(file);
							}
						}
					});
		} else {
			showDialog(NO_SD);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case NO_SD:
			return new AlertDialog.Builder(this).setIcon(
					android.R.drawable.ic_menu_info_details).setTitle(
					R.string.dialog_no_sd_title).setMessage(
					R.string.dialog_no_sd_text).setPositiveButton(
					android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							SDBrowserActivity.this.setResult(RESULT_CANCELED);
							SDBrowserActivity.this.finish();
						}
					}).create();
		}
		return null;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int code = event.getKeyCode();
		if (code == KeyEvent.KEYCODE_BACK) {
			if (listView.findViewWithTag("back") != null) {
				// FIXME: Assumes the back option will be at the beginning
				File file = (File) listView.getItemAtPosition(0);
				if (file != null && file.isDirectory()) { 
					changeDirectory(file);
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
		
	private void setResultAndFinish(File file) {
		Intent result = new Intent();
		String absolutePath = file.getAbsolutePath();
		result.putExtra(Constants.COMIC_PATH_KEY, absolutePath);
		setResult(RESULT_OK, result);
		finish();
	}
	
}
