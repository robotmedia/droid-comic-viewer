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
package net.robotmedia.acv.utils;

import java.util.List;

import net.robotcomics.acv.common.Constants;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.util.FloatMath;

public class Utils {

	public static String getFileExtension(String fileName) {
		String[] splitExtension = fileName.split("\\.");
		if (splitExtension.length > 1) {
			String extension = splitExtension[splitExtension.length - 1];
			return extension.toLowerCase();
		} else {
			return "";
		}
	}
	
	public static String getFileName(String filePath) {
		String[] split = filePath.split("/");
		if (split.length > 1) {
			String fileName = split[split.length - 1];
			return fileName;
		} else {
			return "";
		}
	}
	
	public static float distance(float x1, float y1, float x2, float y2) {
		float deltaX = x1 - x2;
		float deltaY = y1 - y2;
		return FloatMath.sqrt(deltaX * deltaX + deltaY * deltaY);
	}
	
	public static double getAngle(float x1, float y1, float x2, float y2) {
		return Math.toDegrees(Math.acos((x1 - x2) / Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2))));
	}
	
	public static boolean isEqual(int valueA, int valueB, int tolerance) {
		return valueA >= valueB - tolerance && valueA <= valueB + tolerance;
	}
	
	public static boolean isImage(String ext) {
		return (Constants.JPG_EXTENSION.equalsIgnoreCase(ext) || Constants.JPEG_EXTENSION.equalsIgnoreCase(ext) || Constants.PNG_EXTENSION.equalsIgnoreCase(ext) || Constants.GIF_EXTENSION.equalsIgnoreCase(ext) || Constants.BMP_EXTENSION.equalsIgnoreCase(ext));
	}
	
	public static boolean isVideo(String ext) {
		return Constants.MP4_EXTENSION.equalsIgnoreCase(ext);
	}
	
	public static boolean isAudio(String ext) {
		return Constants.MP3_EXTENSION.equalsIgnoreCase(ext);
	}
	
	public static int dipToPixel(Context context, float dip) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}
	
	public static void openURI(final Context context, final String uri, final String alternateUri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(uri));
		List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (list != null && list.size() > 0) {
			context.startActivity(intent);
		} else {
			if (alternateUri != null) {
				intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(alternateUri));
				context.startActivity(intent);
			}
		}
	}

}
