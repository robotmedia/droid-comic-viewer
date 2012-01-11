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

import net.robotmedia.acv.utils.FileUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

public class FileComic extends Comic {

	private Context context;
	private int resourceID;
	private boolean isResource = false;
	
	protected FileComic(Context context, int id) {
		super("");
		isResource = true;
		this.resourceID = id;
		this.context = context;
		final String resourceName = context.getResources().getResourceEntryName(resourceID);
		this.setId(resourceName);
	}
	
	protected FileComic(String path) {
		super(path);
		String extension = FileUtils.getFileExtension(path);
		if (FileUtils.isImage(extension)) {
			File file = new File(path);	
			if (file.isDirectory()) {
				error();
			}
		} else {
			error();
		}
	}

	@Override
	public void destroy() {}

	@Override
	public int getLength() {
		return 1;
	}
	
	@Override
	public Drawable getScreen(int position) {
		ImageState status = imageState.get(KEY_IMAGE_STATE);
		if (status == null) status = ImageState.UNKNOWN;
		switch (status) {
		case MODIFIED:
			final String fileName = getDefaultFileName(0);
			String filePath = getTempFilePath(fileName);
			return Drawable.createFromPath(filePath);
		case ORIGINAL:
			if (isResource) {
				return context.getResources().getDrawable(resourceID);
			} else {
				return Drawable.createFromPath(path);				
			}
		default:
			Bitmap bitmap = resampleAndSave();
			if (bitmap == null) {
				error();
				return null;
			} else {
				return new BitmapDrawable(bitmap);
			}
		}		
	}		

	private static final String KEY_IMAGE_STATE = "0";
	
	private Bitmap resampleAndSave() {
		if (isResource) {
			BitmapFactory.decodeResource(context.getResources(), resourceID, bounds);
		} else {
			BitmapFactory.decodeFile(path, bounds);
		}
		int width = bounds.outWidth;
		int height = bounds.outHeight;
		boolean landscape = height > width;
		int maxHeight = getMaxHeight(landscape);
		int maxWidth = getMaxWidth(landscape);
		Bitmap bitmap;
		boolean avoidResample = width <= maxWidth && height <= maxHeight;
		if (avoidResample) {
			imageState.put(KEY_IMAGE_STATE, ImageState.ORIGINAL);
			if (isResource) {
				bitmap = BitmapFactory.decodeResource(context.getResources(), resourceID);
			} else {
				bitmap = BitmapFactory.decodeFile(path);
			}
		} else {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPreferredConfig = Config.RGB_565;
			options.inSampleSize = calculateSampleSize(width, height);
			if (isResource) {
				bitmap =BitmapFactory.decodeResource(context.getResources(), resourceID, options);
			} else {
				bitmap = BitmapFactory.decodeFile(path, options);
			}
			if (bitmap != null) {
				final String fileName = getDefaultFileName(0);
				saveBitmap(fileName, bitmap);
				imageState.put(KEY_IMAGE_STATE, ImageState.MODIFIED);
			}
		}
		return bitmap;
	}
	
	@Override
	public Drawable getThumbnail(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void prepareScreen(int position) {}

	@Override
	public Uri getUri(int position) {
		ImageState status = imageState.get(KEY_IMAGE_STATE);
		if (status == null) status = ImageState.UNKNOWN;
		String filePath = null;
		switch (status) {
		case MODIFIED:
			final String fileName = getDefaultFileName(0);
			filePath = getTempFilePath(fileName);
			break;
		case ORIGINAL:
		case UNKNOWN:
			filePath = path;
			break;
		}
		return filePath != null ? Uri.fromFile(new File(filePath)) : null;
	}
}
