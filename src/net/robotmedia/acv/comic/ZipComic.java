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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.robotmedia.acv.Constants;
import net.robotmedia.acv.logic.TrackingManager;
import net.robotmedia.acv.utils.FileUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class ZipComic extends Comic {

	private ZipFile mZip;

	private ArrayList<String> screens;
	
	protected TreeMap<String, String> unorderedScreens = new TreeMap<String, String>();
	
	protected ZipComic(String comicPath) {
		super(comicPath);
		init(comicPath);
	}
	
	public void destroy() {
		try {
			if (this.mZip != null) { this.mZip.close(); }
		} catch (IOException e) {}
	}
	
	protected File extract(ZipEntry entry, String name) {
		BufferedInputStream in = null;
		FileOutputStream out = null;
		File file = null;
		try {
			InputStream zipInputStream = getInputStream(entry);
			in = new BufferedInputStream(zipInputStream, Constants.BUFFER_SIZE); 
			file = createTempFile(name);
			out = new FileOutputStream(file);
			int count;
			byte[] buffer = new byte[Constants.BUFFER_SIZE];
			while ((count = in.read(buffer, 0, Constants.BUFFER_SIZE)) != -1) {
				out.write(buffer, 0, count);
			}
			out.flush();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			TrackingManager.trackError("ZipComic.extract", e);
		}
	    return file;
	}
	
	private synchronized Bitmap getBitmapFromEntryIfNeeded(String entryName, boolean recycle) {
		Bitmap bitmap = null;		
		ImageState status = imageState.get(entryName);
		if (status == null || status.equals(ImageState.UNKNOWN)) {
			ZipEntry entry = mZip.getEntry(entryName);
			try {
				File file = extract(entry, entry.getName());
				BitmapFactory.decodeFile(file.getPath(), bounds);
				if (bounds.outWidth == -1) { 
					// TODO: Error
				}
				int width = bounds.outWidth;
				int height = bounds.outHeight;
				boolean landscape = height > width;
				int maxHeight = getMaxHeight(landscape);
				int maxWidth = getMaxWidth(landscape);
				boolean withinBounds = width <= maxWidth && height <= maxHeight;
				if (withinBounds) {
					imageState.put(entryName, ImageState.ORIGINAL);
				} else {
					bitmap = resampleAndSave(entryName, width, height);					
				}
			} catch (Exception e) {
				e.printStackTrace();
				TrackingManager.trackError("ZipComic.getBitmapFromEntryIfNeeded", e);
			}
		}
		if (bitmap != null && recycle) {
			bitmap.recycle();
			return null;
		} else {
			return bitmap;
		}
	}
	
	protected InputStream getInputStream(String name) throws IOException {
		ZipEntry entry = mZip.getEntry(name);
		return getInputStream(entry);
	}
	
	protected InputStream getInputStream(ZipEntry entry) throws IOException {
		return mZip.getInputStream(entry);
	}	
	
	
	public int getLength() {
		return screens != null ? screens.size() : 0;
	}
	
	protected Drawable getDrawable(String entryName) {
		ImageState status = imageState.get(entryName);
		if (status == null) status = ImageState.UNKNOWN;
		String filePath = getTempFilePath(entryName);
		switch (status) {
		case MODIFIED:
		case ORIGINAL:
			if (filePath != null) {
				return Drawable.createFromPath(filePath);
			} else {
				// HACK: The temp file was deleted, so we're back to square one. Unknown is not the best description for this case.
				imageState.put(entryName, ImageState.UNKNOWN);
			}
		default:
			Bitmap bitmap = getBitmapFromEntryIfNeeded(entryName, false);
			if (bitmap == null) {
				status = imageState.get(entryName);
				if (status == null) status = ImageState.UNKNOWN; // This shouldn't happen
				filePath = getTempFilePath(entryName);
				switch (status) {
				case MODIFIED:
				case ORIGINAL:
					if (filePath != null) {
						return Drawable.createFromPath(filePath);
					}
				default:
					error();
					return null;
				}
			} else {
				return new BitmapDrawable(bitmap);
			}
		}		
	}
		
	public Drawable getScreen(final int position) {
		
		if(position < 0 || position >= screens.size()) {
			return null;
		}
		
		final String entryName = screens.get(position);
		return getDrawable(entryName);
	}
	
	public Drawable getThumbnail(int position) {
		return null;
	}

	@Override
	public Uri getUri(int position) {
		final String entryName = screens.get(position);
		String filePath = getTempFilePath(entryName);
		return filePath != null ? Uri.fromFile(new File(filePath)) : null;
	}
	
	private void init(String comicPath) {
		try {
			this.setZip(new ZipFile(comicPath));
			Enumeration<? extends ZipEntry> entries = mZip.entries();
	 		while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				if (!entry.isDirectory()) {
					this.processEntry(entry);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			error();
		}
		
		{
			ArrayList<String> orderedScreenKeys = new ArrayList<String>(unorderedScreens.keySet());
			screens = new ArrayList<String>(orderedScreenKeys.size());
			for (int i = 0; i < orderedScreenKeys.size(); i++) {
				screens.add(unorderedScreens.get(orderedScreenKeys.get(i)));
			}
		}
			
	}
	
	public void prepareScreen(int position) {
		if (position >= 0 && position < this.getLength()) {
			final String entryName = screens.get(position);
			ImageState status = imageState.get(entryName);
			if (status == null || status.equals(ImageState.UNKNOWN)) {
				try {
					getBitmapFromEntryIfNeeded(entryName, true);
				} catch (Exception e) {
					e.printStackTrace();
					TrackingManager.trackError("ZipComic.prepareScreen", e);
				}
			}
		}
	}
	
	protected void setZip(ZipFile zip) {
		this.mZip = zip;
	}
	
	protected void processEntry(ZipEntry entry) {
		String entryName = entry.getName();
		String extension = FileUtils.getFileExtension(entryName);
		if (FileUtils.isImage(extension)) {
			final String entryNameWithLeadingZeroes = this.addLeadingZeroes(entryName);
			unorderedScreens.put(entryNameWithLeadingZeroes, entryName);
		}
	}

	private Bitmap resample(String filePath, int sampleSize) {
		BitmapFactory.Options resample = new BitmapFactory.Options();
		resample.inPreferredConfig = Config.RGB_565;
		resample.inSampleSize = sampleSize;
		return BitmapFactory.decodeFile(filePath, resample);
	}

	private Bitmap resampleAndSave(String entryName, int width, int height) {
		String filePath = getTempFilePath(entryName);
		Bitmap bitmap = null;
		int sampleSize = calculateSampleSize(width, height);
		if (filePath != null) {
			bitmap = resample(filePath, sampleSize);
			filePath = saveBitmap(entryName, bitmap);
			if (filePath != null) {
				imageState.put(entryName, ImageState.MODIFIED);
			}
		}
		return bitmap;
	}

}
