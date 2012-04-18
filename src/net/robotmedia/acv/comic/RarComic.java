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
import java.io.IOException;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.github.junrar.unpack.decode.Compress;

import net.robotmedia.acv.logic.TrackingManager;
import net.robotmedia.acv.utils.FileUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class RarComic extends Comic {

	private Archive arc = null;

	private ArrayList<String> orderedScreens;
	private TreeMap<String, FileHeader> fileHeaders;

	protected RarComic(String comicPath) {
		super(comicPath);
		File file = new File(comicPath);

		try {
			arc = new Archive(file);
		} catch (Exception e) {
			e.printStackTrace();
			TrackingManager.trackError("RarComic", e);
		}

		if (arc != null && !arc.isEncrypted()) {
			final TreeMap<String, String> headers = new TreeMap<String, String>();
			fileHeaders = new TreeMap<String, FileHeader>();
			List<FileHeader> files = arc.getFileHeaders();
			for (FileHeader fh : files) {
				if (!fh.isEncrypted() && fh.isFileHeader()) {
					String fileName;
					if (fh.isUnicode()) {
						fileName = fh.getFileNameW();
					} else {
						fileName = fh.getFileNameString();
					}
					String extension = FileUtils.getFileExtension(fileName);
					if (FileUtils.isImage(extension)) {
						fileHeaders.put(fileName, fh);
						final String key = this.addLeadingZeroes(fileName);
						headers.put(key, fileName);
					}
				}
			}

			ArrayList<String> ordered = new ArrayList<String>(headers.keySet());
			orderedScreens = new ArrayList<String>(ordered.size());
			for (int i = 0; i < ordered.size(); i++) {
				orderedScreens.add(headers.get(ordered.get(i)));
			}
		} else {
			error();
		}
	}

	private File extract(String fileName, int position) {
		File file = null;
		FileOutputStream os = null;
		try {
			FileHeader fh = fileHeaders.get(fileName);
			final String tempFileName = getDefaultFileName(position);
			file = createTempFile(tempFileName); 
			os = new FileOutputStream(file);
			arc.extractFile(fh, os);
		} catch (Exception e) {
			Log.e("RarComic.extract", e.getLocalizedMessage());
			// Try with small window size in we're using the big one 
			Compress.adjustWindowSize(false);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
		return file;
	}

	public synchronized void prepareScreen(int position) {
		if (position >= 0 && position < this.getLength()) {
			ImageState status = imageState.get(String.valueOf(position));
			if (status == null || status.equals(ImageState.UNKNOWN)) {
				String entryName = orderedScreens.get(position);
				try {
					getBitmapFromFileHeaderIfNeeded(position, entryName, true);
				} catch (Exception e) {
					e.printStackTrace();
					TrackingManager.trackError("RarComic.prepareScreen", e);
				}
			}
		}
	}

	private synchronized Bitmap getBitmapFromFileHeaderIfNeeded(int position, String fileName, boolean recycle) {
		Bitmap bitmap = null;
		ImageState status = imageState.get(String.valueOf(position));
		if (status == null || status.equals(ImageState.UNKNOWN)) {
			try {
				File file = extract(fileName, position);
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
					imageState.put(String.valueOf(position), ImageState.ORIGINAL);
				} else {
					bitmap = resampleAndSave(position, width, height);
				}
			} catch (Exception e) {
				e.printStackTrace();
				TrackingManager.trackError("RarComic.getBitmapFromFileHeaderIfNeeded", e);
			}
		}
		if (bitmap != null && recycle) {
			bitmap.recycle();
			return null;
		} else {
			return bitmap;
		}
	}

	public int getLength() {
		return orderedScreens != null ? orderedScreens.size() : 0;
	}

	@Override
	public Drawable getScreen(final int position) {
		ImageState status = imageState.get(String.valueOf(position));
		if (status == null) status = ImageState.UNKNOWN;

		try {
			String entryName = orderedScreens.get(position);

			switch (status) {
				case ORIGINAL:
				case MODIFIED:
					String filePath = getTempFilePath(position);
					if (filePath != null) {
						return Drawable.createFromPath(filePath);
					}
				default:
					Bitmap bitmap = getBitmapFromFileHeaderIfNeeded(position, entryName, false);
					if (bitmap == null) {
						status = imageState.get(String.valueOf(position));
						if (status == null) status = ImageState.UNKNOWN;
						switch (status) {
						case ORIGINAL:
						case MODIFIED:
							filePath = getTempFilePath(position);
							if (filePath != null) {
								return Drawable.createFromPath(filePath);
							}
							return Drawable.createFromPath(filePath);
						default:
							error();
							return null;
						}
					} else {
						return new BitmapDrawable(bitmap);
					}
				}

		} catch(IndexOutOfBoundsException e) {
			return null;
		}
	}

	public Drawable getThumbnail(int position) {
		return null;
	}

	private Bitmap resample(String filePath, int sampleSize, int position) {
		BitmapFactory.Options resample = new BitmapFactory.Options();
		resample.inPreferredConfig = Config.RGB_565;
		resample.inSampleSize = sampleSize;
		return BitmapFactory.decodeFile(filePath, resample);
	}
	
	private Bitmap resampleAndSave(int position, int width, int height) {
		String filePath = getTempFilePath(position);
		Bitmap bitmap = null;
		int sampleSize = calculateSampleSize(width, height);
		if (filePath != null) {
			bitmap = resample(filePath, sampleSize, position);
		}
		if (bitmap != null) {
			final String tempFileName = getDefaultFileName(position);
			String resampledFilePath = saveBitmap(tempFileName, bitmap);
			if (resampledFilePath != null) {
				imageState.put(String.valueOf(position), ImageState.MODIFIED);
			}
		}
		return bitmap;
	}

	public void destroy() {
		try {
			if (this.arc != null) { this.arc.close(); }
		} catch (IOException e) {}
	}

	private String getTempFilePath(int position) {
		final String tempFileName = getDefaultFileName(position);
		return getTempFilePath(tempFileName);
	}
	
	@Override
	public Uri getUri(int position) {
		String filePath = getTempFilePath(position);
		return filePath != null ? Uri.fromFile(new File(filePath)) : null;
	}


}
