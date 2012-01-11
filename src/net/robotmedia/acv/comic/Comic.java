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
package net.robotmedia.acv.comic;

import java.io.File;
import java.io.FileOutputStream;
import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.robotmedia.acv.Constants;
import net.robotmedia.acv.logic.TrackingManager;
import net.robotmedia.acv.utils.FileUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

public abstract class Comic {

	protected enum ImageState {
		MODIFIED, UNKNOWN, ORIGINAL
	}

	private static final HashMap<String, Comic> comics = new HashMap<String, Comic>();
	// TODO: Remove comics from collection
	
	private static int MAX_HEIGHT = 1200;
	private static int MAX_WIDTH = 1200;
	
	public static void setMaxWidth(int value) {
		MAX_WIDTH = value;
	}

	
	public static void setMaxHeight(int value) {
		MAX_HEIGHT = value;
	}

	private static Comic instance = null;
	
	protected static final Pattern numberPattern = Pattern.compile("(?<!\\d)0*(\\d{1,3})(?!\\d)");
	private static String TAG = "Comic";

	protected void setId(String id) {
		this.id = id;
	}
	
	public static Comic createComic(String path) {
		File file = new File(path);
		Comic comic = null;
		String type;
		if (file.isDirectory()) {
			comic = new FolderComic(path);
			type = Constants.EVENT_VALUE_FOLDER;
		} else {
			type = FileUtils.getFileExtension(path);
			if (Constants.ZIP_EXTENSION.equals(type) || Constants.CBZ_EXTENSION.equals(type)) {
				try {
					comic = new ZipComic(path);
				} catch (Exception e) {
					Log.w(TAG, "Failed to load zip comic", e);
				}
				if (comic == null || comic.isError()) {
					try {
						comic = new RarComic(path);
					} catch (Exception e) {
						Log.w(TAG, "Failed to load zip comic as rar comic", e);
					}
					if (comic != null && !comic.isError()) {
						Log.i(TAG, "Loaded rar comic with wrong extension");
						comic.type = Constants.RAR_EXTENSION;
					}
				}
			} else if (Constants.RAR_EXTENSION.equals(type) || Constants.CBR_EXTENSION.equals(type)) {
				try {
					comic = new RarComic(path);
				} catch (Exception e) {
					Log.w(TAG, "Failed to load rar comic", e);
				}
				if (comic == null || comic.isError()) {
					try {
						comic = new ZipComic(path);
					} catch (Exception e) {
						Log.w(TAG, "Failed to load rar comic as zip comic", e);
					}
					if (comic != null && !comic.isError()) {
						Log.i(TAG, "Loaded rar comic with wrong extension");
						comic.type = Constants.ZIP_EXTENSION;
					}
				}
			} else if (FileUtils.isImage(type)) {  
				comic = new FileComic(path);
			} else if (Constants.ACV_EXTENSION.equals(type)) {
				comic = new ACVComic(path);
			}
		}
		if (comic != null) {
			comic.type = type;
			comics.put(comic.getID(), comic);
		}
		return comic;
	}
	
	public static Comic getComic(String id) {
		return comics.get(id);
	}
	@Deprecated
	public static Comic getInstance() {
		return instance;
	}

	protected BitmapFactory.Options bounds;

	protected String description;
	private boolean error = false;
	protected String id;
	protected String name;
	
	protected String path;
	
	protected HashMap<String, ImageState> imageState;
	
	private String type;

	protected Comic(String path) {
		instance = this;
		name = FileUtils.getFileName(path);
		this.path = path;
		this.imageState = new HashMap<String, ImageState>();
		bounds = new BitmapFactory.Options();
		this.bounds.inJustDecodeBounds = true;
	}
	
	/**
	 * Returns a new string by adding leading zeroes to all numbers of the given string. Used to sort strings alphabetically.
	 * @param value
	 * @return
	 */
	protected String addLeadingZeroes(String s) {
		Matcher m = numberPattern.matcher(s);
        int lastMatch = 0;
        ArrayList<String> splitted = new ArrayList<String>();
		while (m.find()) {
            splitted.add(s.substring(lastMatch,m.start()));
            String numberAsString = m.group(1);
            int number = Integer.parseInt(numberAsString);
			String formattedNumber = String.format("%04d", number);
			splitted.add(formattedNumber);
			lastMatch = m.end(1);
		}
        splitted.add(s.substring(lastMatch));
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < splitted.size(); i++) {
        	buffer.append(splitted.get(i));
        }
        return buffer.toString();
	}
	
	protected int calculateSampleSize(int width, int height) {
		boolean landscape = width > height;
		int maxHeight = getMaxHeight(landscape);
		int maxWidth = getMaxWidth(landscape);
		int newWidth;
		if (landscape) {
			float ratio = (float) height / (float) width;
			float containerRatio = (float) maxHeight / (float) maxWidth;
			if (ratio < containerRatio) {
				newWidth = maxWidth; 
			} else {
				newWidth = width * maxHeight / height; 
			}
		} else {
			float ratio = (float) width / (float) height;
			float containerRatio = (float) maxWidth / (float) maxHeight;
			if (ratio < containerRatio) {
				newWidth = width * maxHeight / height; 
			} else {
				newWidth = maxWidth; 
			}
		}
		
		float sampleSizeF = (float) width / (float) newWidth;
	
		int sampleSize = (int) Math.round(Math.ceil(sampleSizeF));
		return sampleSize;
	}
	
	protected String getRelativePath() {
		return Constants.TEMP_PATH;
	}
	
	protected File createTempFile(String name) {
		File dir = new File(Environment.getExternalStorageDirectory(), this.getRelativePath());
		if (id != null) {
			dir = new File(dir, this.getID());
		}
		dir.mkdirs();
		if (name.contains(File.separator)) {
			String nameDir = name.substring(0, name.lastIndexOf(File.separator));
			String nameFile = name.substring(name.lastIndexOf(File.separator)+1);
			File f = new File(dir, nameDir);
			f.mkdirs();
			return new File(f, nameFile);
		} else {
			return new File(dir, name);
		}
	}
	
	/**
	 * Releases all resources.
	 */
	public abstract void destroy();
	
	protected void error() {
		error = true;
	}
	
	public Integer getBackgroundColor(int index) {
		return Color.BLACK;
	}
	
	public String getDescription() {
		return description;
	}
	
	public int getFramesSize(int index) {
		return 0;
	}

	public String getID() {
		return id;
	}
	
	/**
	 * Returns the screen length of the comic.
	 * @return the screen length of the comic.
	 */
	public abstract int getLength();
	
	protected int getMaxHeight(boolean landscape) {
		return MAX_HEIGHT;
	}
	
	protected int getMaxWidth(boolean landscape) {
		return MAX_WIDTH;
	}
	
	public String getName() {
		return name;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getScaleMode() {
		return null;
	}
	
	/**
	 * Returns the screen.	
	 * @param position Position of the screen.
	 * @return Drawable with the screen of the given position, or null if the screen can not be generated or if the position is invalid.
	 */
	public abstract Drawable getScreen(int position);

	protected String getDefaultFileName(int index) {
		return index + "." + Constants.JPG_EXTENSION;
	}
	
	protected String getTempFilePath(String name) {
		File file = createTempFile(name);
		if (file.exists()) {
			return file.getPath();
		} else {
			return null;
		}
	}
	
	/**
	 * Returns a thumbnail of the screen.
	 * @param position Position of the screen whose thumbnail is requested.
	 * @return Drawable with a thumbnail of the screen of the given position, or null if the thumbnail can not be generated or if the position is invalid.
	 */
	public abstract Drawable getThumbnail(int position);
	
	
	public String getType() {
		return type;
	}

	/**
	 * Returns the Uri of the screen, if available.	
	 * @param position Position of the screen.
	 * @return Uri of the screen or null if not available.
	 */
	public abstract Uri getUri(int position);

	public boolean hasFrames(int index) {
		return getFramesSize(index) > 0;
	}

	/**
	 * Returns true if the comic is compatible with the given version or if compatibility can not be checked, false otherwise. The default implementation returns true.
	 * @param version Viewer version code.
	 * @return True if the comic is compatible with the given version or if compatibility can not be checked, false otherwise. 
	 */
	public boolean isCompatible(int version) {
		return true;
	}

	public boolean isError() {
		return error;
	}
	
	/**
	 * Ensures the screen is prepared for a getScreen call. A prepareScreen call is highly likely to be followed by a getScreen call for the same screen.
	 * @param position Position of the screen that will be prepared. Invalid positions will be ignored.
	 */
	public abstract void prepareScreen(int position);
	
	protected String saveBitmap(String name, Bitmap bitmap) {
		FileOutputStream out = null;
		try {
			File file = createTempFile(name);
			out = new FileOutputStream(file);
			boolean success = bitmap.compress(CompressFormat.JPEG, Constants.COMPRESSION_QUALITY, out);
			out.close();
			if (success) {
				return file.getPath();
			}
		} catch (Exception e) {
			e.printStackTrace();
			TrackingManager.trackError("Comic.saveBitmap", e);
			if (out != null) {
				try { out.close(); } catch (Exception e1) {}
			}
		} finally {
			bitmap = null;
		}
		return null;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
