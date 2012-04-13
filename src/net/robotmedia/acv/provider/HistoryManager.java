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
package net.robotmedia.acv.provider;

import java.io.File;
import java.util.List;

import android.content.Context;

public class HistoryManager {

	private static HistoryManager instance = null;
	
	private DBHelper mDB;
	
	public static HistoryManager getInstance(Context context) {
		if (instance == null) {
			instance = new HistoryManager(context);
		}
		return instance;
	}
	
	private HistoryManager(Context context) {
		mDB = new DBHelper(context);
	}
	
	public void remember(final File file) {
		final String path = file.getAbsolutePath();
		if (mDB.existsFile(path)) {
			mDB.increaseFileViews(path);
			mDB.updateLastOpened(path);
		} else {
			mDB.insertFile(path);
		}
	}
	
	public int getBookmark(final File file) {
		final String path = file.getAbsolutePath();
		return mDB.selectFileBookmark(path);
	}
	
	public void setBookmark(final File file, final int bookmark) {
		final String path = file.getAbsolutePath();
		if (!mDB.existsFile(path)) {
			mDB.insertFile(path);
		}
		mDB.updateFileBookmark(path, bookmark);
	}

	public void clear() {
		mDB.deleteFiles();
	}
	
	public String getLast() {
		return mDB.selectMostRecentFile();
	}
	
	public List<String> getRecentFiles() {
		return mDB.getRecentFiles();
	}
	
}
