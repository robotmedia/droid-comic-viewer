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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.TreeMap;

import net.robotmedia.acv.logic.TrackingManager;
import net.robotmedia.acv.utils.FileUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class FolderComic extends Comic {
	
	private ArrayList<String> orderedScreens;
	
	protected FolderComic(String path) {
		super(path);
		File folder = new File(path);
		if (folder.isDirectory()) {
			String[] files = folder.list(new FilenameFilter() {
				public boolean accept(File dir, String filename) {
					String ext = FileUtils.getFileExtension(filename);
					return FileUtils.isImage(ext);
				}});
			TreeMap<String, String> images = new TreeMap<String, String>();
			for (int i = 0; i < files.length; i++) {
				final File current = new File(folder, files[i]);
				String currentPath = current.getPath();
				String pathWithLeadinZeroes = this.addLeadingZeroes(currentPath);
				images.put(pathWithLeadinZeroes, currentPath);
			}
			ArrayList<String> ordered = new ArrayList<String>(images.keySet());
			orderedScreens = new ArrayList<String>(ordered.size());
			for (int i = 0; i < ordered.size(); i++) {
				orderedScreens.add(images.get(ordered.get(i)));
			}
		} else {
			error();
		}
	}

	@Override
	public void destroy() {}

	@Override
	public int getLength() {
		return orderedScreens != null ? orderedScreens.size() : 0;
	}

	@Override
	public Drawable getScreen(int position) {
		ImageState status = imageState.get(String.valueOf(position));
		if (status == null) status = ImageState.UNKNOWN;
		String filePath;
		switch (status) {
		case MODIFIED:
			filePath = getTempFilePath(position);
			return Drawable.createFromPath(filePath);
		case ORIGINAL:
			filePath = orderedScreens.get(position);
			return Drawable.createFromPath(filePath);
		default:
			Bitmap bitmap = resampleAndSave(position, false);
			if (bitmap == null) {
				status = imageState.get(String.valueOf(position));
				if (status == null) status = ImageState.UNKNOWN;
				switch (status) {
				case MODIFIED:
					filePath = getTempFilePath(position);
					return Drawable.createFromPath(filePath);
				case ORIGINAL:
					filePath = orderedScreens.get(position);
					return Drawable.createFromPath(filePath);
				default:
					error();
					return null;
				}
			} else {
				return new BitmapDrawable(bitmap);
			}
		}		
	}		

	private synchronized Bitmap resampleAndSave(int position, boolean recycle) {
		String filePath = orderedScreens.get(position);
		BitmapFactory.decodeFile(filePath, bounds);
		int width = bounds.outWidth;
		int height = bounds.outHeight;
		boolean landscape = height > width;
		int maxHeight = getMaxHeight(landscape);
		int maxWidth = getMaxWidth(landscape);
		Bitmap bitmap;
		boolean avoidResample = width <= maxWidth && height <= maxHeight;
		if (avoidResample) {
			imageState.put(String.valueOf(position), ImageState.ORIGINAL);
			bitmap = BitmapFactory.decodeFile(filePath);
		} else {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = calculateSampleSize(width, height);
			bitmap = BitmapFactory.decodeFile(filePath, options);
			if (bitmap != null) {
				final String tempFileName = getDefaultFileName(position);
				saveBitmap(tempFileName, bitmap);
				imageState.put(String.valueOf(position), ImageState.MODIFIED);
			}
		}
		if (recycle && bitmap != null) {
			bitmap.recycle();
		}
		return bitmap;
	}
	
	@Override
	public Drawable getThumbnail(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void prepareScreen(int position) {
		if (position >= 0 && position < this.getLength()) {
			ImageState status = imageState.get(String.valueOf(position));
			if (status == null || status.equals(ImageState.UNKNOWN)) {
				try {
					resampleAndSave(position, true);
				} catch (Exception e) {
					e.printStackTrace();
					TrackingManager.trackError("FolderComic.prepareScreen", e);
				}
			}
		}
	}
	
	private String getTempFilePath(int position) {
		final String tempFileName = getDefaultFileName(position);
		return getTempFilePath(tempFileName);
	}
	
	@Override
	public Uri getUri(int position) {
		ImageState status = imageState.get(String.valueOf(position));
		if (status == null) status = ImageState.UNKNOWN;
		String filePath = null;
		switch (status) {
		case MODIFIED:
			filePath = getTempFilePath(position);
			break;
		case ORIGINAL:
		case UNKNOWN:
			filePath = orderedScreens.get(position);
			break;
		}
		return filePath != null ? Uri.fromFile(new File(filePath)) : null;
	}
	
}
