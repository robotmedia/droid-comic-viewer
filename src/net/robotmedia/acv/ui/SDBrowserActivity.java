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
import java.util.*;

import net.androidcomics.acv.R;
import net.robotmedia.acv.Constants;
import net.robotmedia.acv.logic.AdsManager;
import net.robotmedia.acv.logic.PreferencesController;
import net.robotmedia.acv.provider.HistoryManager;
import net.robotmedia.acv.utils.FileUtils;
import android.app.*;
import android.content.*;
import android.os.Bundle;
import android.os.Environment;
import android.view.*;
import android.widget.*;
import android.widget.TabHost.OnTabChangeListener;

public class SDBrowserActivity extends TabActivity {

	private static final int NO_SD = 3;
	private static final String TAB_BROWSER = "tab_browser";
	private static final String TAB_RECENT = "tab_recent";

	private File currentDirectory;
	private static HashMap<String, Integer> supportedExtensions = null;
	private TabHost tabHost;
	private FrameLayout tabBrowser, tabRecent;
	private ListView browserListView;
	private ListView recentListView;
	private LayoutInflater mInflater;
	private PreferencesController preferencesController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		supportedExtensions = Constants.getSupportedExtensions(this);
		preferencesController = new PreferencesController(this);

		setContentView(R.layout.sd_browser);
		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		tabHost = getTabHost();
		tabBrowser = (FrameLayout) findViewById(R.id.sdBrowserTabBrowser);
		tabRecent = (FrameLayout) findViewById(R.id.sdBrowserTabRecent);

		tabHost.addTab(tabHost.newTabSpec(TAB_BROWSER).setIndicator(getIndicator(R.string.sd_browser_open))
				.setContent(R.id.sdBrowserTabBrowser));
		tabHost.addTab(tabHost.newTabSpec(TAB_RECENT).setIndicator(getIndicator(R.string.sd_browser_recent))
				.setContent(R.id.sdBrowserTabRecent));

		tabHost.setCurrentTab(0);

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				if (tabId.contentEquals(TAB_RECENT)) {
					setTitle("");
				} else {
					setTitle(currentDirectory.getName());
				}
			}
		});

		String storageState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(storageState)) {

			// Setup file list
			Intent intent = getIntent();
			String comicsPath = intent.getStringExtra(Constants.COMICS_PATH_KEY);
			File directory;
			if (comicsPath != null) { // TODO simplify
				directory = new File(comicsPath);
				if (!directory.isDirectory()) {
					directory = Environment.getExternalStorageDirectory();
				}
			} else {
				directory = Environment.getExternalStorageDirectory();
			}

			browserListView = (ListView) tabBrowser.findViewById(android.R.id.list);
			browserListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					File file = (File) parent.getItemAtPosition(position);
					if ((file != null) && (file.isDirectory())) {
						String[] images = file.list(new FilenameFilter() {
							public boolean accept(File dir, String filename) {
								String ext = FileUtils.getFileExtension(filename);
								return FileUtils.isImage(ext);
							}
						});
						if (images.length > 0) {
							setResultAndFinish(file);
							return true;
						}
					}
					return false;
				}
			});
			browserListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					File file = (File) parent.getItemAtPosition(position);

					if(file == null) {
						return;
					}

					if (file.isDirectory()) {
						changeDirectory(file);
					} else if (file.exists()) {
						setResultAndFinish(file);
					}
				}
			});

			changeDirectory(directory);

			// Setup recent items list
			recentListView = (ListView) tabRecent.findViewById(android.R.id.list);
			recentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					File file = new File((String) parent.getItemAtPosition(position));
					if (file.exists()) {
						setResultAndFinish(file);
					}
				}
			});
			recentListView.setAdapter(new RecentListAdapter(this, R.layout.sd_item_empty));

			// Ads
			View ad = AdsManager.getAd(this);
			if(ad != null) {
				ViewGroup adsContainer = (ViewGroup) findViewById(R.id.adsContainer);
				adsContainer.addView(ad);
			}
		} else {
			showDialog(NO_SD);
		}
	}

	private void changeDirectory(File directory) {
		currentDirectory = directory;
		this.setTitle(directory.getName());
		preferencesController.savePreference(Constants.COMICS_PATH_KEY, directory.getAbsolutePath());
		browserListView.setAdapter(new ListAdapter(SDBrowserActivity.this, directory, R.layout.sd_item_empty));
	}

	protected ViewGroup getIndicator(int resourceId) {
		return getIndicator(getString(resourceId));
	}

	protected ViewGroup getIndicator(String text) {
		ViewGroup indicator = (ViewGroup) mInflater.inflate(R.layout.sd_browser_tab, null);
		TextView label = (TextView) indicator.findViewById(R.id.sd_browser_tab_title);
		label.setText(text); // TODO custom font
		return indicator;
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case NO_SD:
			return new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_menu_info_details).setTitle(R.string.dialog_no_sd_title)
					.setMessage(R.string.dialog_no_sd_text).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
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
			finish();
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

	public class ListAdapter extends BaseAdapter {

		ArrayList<File> contents = new ArrayList<File>();
		private File current;
		private View emptyView;
		private boolean isEmpty;

		public ListAdapter(Context context, File current, int emptyResourceId) {
			this.current = current;

			if (emptyResourceId != 0) {
				LayoutInflater inflater = getLayoutInflater();
				emptyView = inflater.inflate(emptyResourceId, null);
			} else {
				TextView t = new TextView(context);
				t.setText(R.string.sd_browser_empty);
				emptyView = t;
			}

			filterContents();
		}

		private void filterContents() {
			String[] allContents = current.list();
			TreeMap<String, File> filteredContents = new TreeMap<String, File>();
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
						String extension = FileUtils.getFileExtension(contentName);
						if (supportedExtensions.containsKey(extension.toLowerCase())) {
							File contentFile = new File(path, contentName);
							filteredContents.put(contentFile.getName().toLowerCase(), contentFile);
						} else {
							File contentFile = new File(path, contentName);
							if (contentFile.isDirectory()) {
								filteredContents.put(contentFile.getName().toLowerCase(), contentFile);
							}
						}
					}
				}
			}
			isEmpty = (filteredContents.size() == 0);
			contents.addAll(filteredContents.values());
		}

		public int getCount() {
			if (isEmpty) {
				return contents.size() + 1;
			} else {
				return contents.size();
			}
		}

		public File getItem(int position) {
			if (position < contents.size()) {
				return contents.get(position);
			} else {
				return null;
			}
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// First element
			if (position == 0 && current.getParent() != null) {
				TextView textView = (TextView) mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
				textView.setText(R.string.sd_browser_back);
				textView.setTag("back");
				return textView;
			} else {

				if (isEmpty) {
					return emptyView;
				}

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
				String extension = FileUtils.getFileExtension(name);
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

	// TODO refactor both list adapters
	public class RecentListAdapter extends BaseAdapter {
		ArrayList<String> contents = new ArrayList<String>();
		private View emptyView;
		private boolean isEmpty;

		public RecentListAdapter(Context context, int emptyResourceId) {
			if (emptyResourceId != 0) {
				LayoutInflater inflater = getLayoutInflater();
				emptyView = inflater.inflate(emptyResourceId, null);
			} else {
				TextView t = new TextView(context);
				t.setText(R.string.sd_browser_empty);
				emptyView = t;
			}

			populate();
		}

		private void populate() {
			contents.clear();
			List<String> lastFiles = HistoryManager.getInstance(SDBrowserActivity.this).getRecentFiles();
			contents.addAll(lastFiles);
		}

		public int getCount() {
			return contents.size();
		}

		public String getItem(int position) {
			return contents.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (isEmpty) {
				return emptyView;
			}

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

			File file = new File(contents.get(position));
			String name = file.getName();
			String extension = FileUtils.getFileExtension(name);
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

	static class ViewHolder {
		ImageView icon;
		TextView name;
		TextView size;
	}
}
