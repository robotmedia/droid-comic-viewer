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
package net.robotmedia.acv.logic;

import java.io.File;

import net.robotmedia.acv.Constants;
import net.robotmedia.acv.comic.Comic;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class PreferencesController {

	public static final String PREFERENCE_LOW_MEMORY = "low_memory";
	public static final String PREFERENCE_MAX_IMAGE_WIDTH = "max_image_width";
	public static final String PREFERENCE_MAX_IMAGE_HEIGHT = "max_image_height";
	
	private SharedPreferences preferences;

	public PreferencesController(Context context) {
		this.preferences = PreferenceManager.getDefaultSharedPreferences(context);		
	}
	
	@SuppressWarnings("deprecation")
	public void legacy() {
		File legacyTempPath = new File(Environment.getExternalStorageDirectory(), Constants.LEGACY_TEMP_PATH);
		if(legacyTempPath.exists()) {
			String[] files = legacyTempPath.list();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					File file = new File(legacyTempPath, files[i]);
					file.delete();
				}
			}
			legacyTempPath.delete();
		}
		
		Editor editor = preferences.edit();
		if (preferences.contains(Constants.LEGACY_FLING_ENABLED_KEY)) {
			if (!preferences.getBoolean(Constants.LEGACY_FLING_ENABLED_KEY, true)) {
				editor.putString(Constants.INPUT_FLING_LEFT, Constants.ACTION_VALUE_NONE);
				editor.putString(Constants.INPUT_FLING_RIGHT, Constants.ACTION_VALUE_NONE);
			}
			editor.remove(Constants.LEGACY_FLING_ENABLED_KEY);
		}
		editor.remove(Constants.LEGACY_STARTUP_UPDATE_CHECK_KEY);
		
		// Bug fix
		int orientation = preferences.getInt(Constants.ORIENTATION_KEY, Configuration.ORIENTATION_LANDSCAPE);
		if (orientation == 0) {
			editor.putInt(Constants.ORIENTATION_KEY, Configuration.ORIENTATION_LANDSCAPE);
		}

		editor.commit();
	}
	
	public boolean isLeftToRight() {
		String direction = preferences.getString(Constants.DIRECTION_KEY, Constants.DIRECTION_LEFT_TO_RIGHT_VALUE);
		return Constants.DIRECTION_LEFT_TO_RIGHT_VALUE.equals(direction);
	}
	
	public boolean isUsedForPreviousNext(String previousInput, String nextInput) {
		String previousInputAction = preferences.getString(previousInput, null);
		String nextInputAction = preferences.getString(nextInput, null);
		return Constants.ACTION_VALUE_PREVIOUS.equals(previousInputAction) && Constants.ACTION_VALUE_NEXT.equals(nextInputAction);
	}
	
	public void savePreference(String key, String value) {
		Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	public void restoreControlDefaults() {
		Editor editor = preferences.edit();
		if (isLeftToRight()) {
			editor.putString(Constants.TRACKBALL_LEFT_KEY, Constants.ACTION_VALUE_PREVIOUS);
			editor.putString(Constants.TRACKBALL_RIGHT_KEY, Constants.ACTION_VALUE_NEXT);
			editor.putString(Constants.INPUT_FLING_LEFT, Constants.ACTION_VALUE_NEXT);
			editor.putString(Constants.INPUT_FLING_RIGHT, Constants.ACTION_VALUE_PREVIOUS);
			editor.putString(Constants.INPUT_CORNER_BOTTOM_LEFT, Constants.ACTION_VALUE_PREVIOUS);
			editor.putString(Constants.INPUT_CORNER_BOTTOM_RIGHT, Constants.ACTION_VALUE_NEXT);
			editor.putString(Constants.INPUT_VOLUME_UP, Constants.ACTION_VALUE_PREVIOUS);		
			editor.putString(Constants.INPUT_VOLUME_DOWN, Constants.ACTION_VALUE_NEXT);		
		} else {
			editor.putString(Constants.TRACKBALL_LEFT_KEY, Constants.ACTION_VALUE_NEXT);
			editor.putString(Constants.TRACKBALL_RIGHT_KEY, Constants.ACTION_VALUE_PREVIOUS);
			editor.putString(Constants.INPUT_FLING_LEFT, Constants.ACTION_VALUE_PREVIOUS);
			editor.putString(Constants.INPUT_FLING_RIGHT, Constants.ACTION_VALUE_NEXT);
			editor.putString(Constants.INPUT_CORNER_BOTTOM_LEFT, Constants.ACTION_VALUE_NEXT);
			editor.putString(Constants.INPUT_CORNER_BOTTOM_RIGHT, Constants.ACTION_VALUE_PREVIOUS);			
			editor.putString(Constants.INPUT_VOLUME_UP, Constants.ACTION_VALUE_NEXT);		
			editor.putString(Constants.INPUT_VOLUME_DOWN, Constants.ACTION_VALUE_PREVIOUS);
		}
		editor.putString(Constants.SINGLE_TAP_KEY, Constants.ACTION_VALUE_NONE);
		editor.putString(Constants.INPUT_DOUBLE_TAP, Constants.ACTION_VALUE_ZOOM_IN);
		editor.putString(Constants.LONG_TAP_KEY, Constants.ACTION_VALUE_SCREEN_BROWSER);
		editor.putString(Constants.TRACKBALL_CENTER_KEY, Constants.ACTION_VALUE_NEXT);
		editor.putString(Constants.TRACKBALL_UP_KEY, Constants.ACTION_VALUE_ZOOM_IN);
		editor.putString(Constants.TRACKBALL_DOWN_KEY, Constants.ACTION_VALUE_ZOOM_OUT);
		editor.putString(Constants.BACK_KEY, Constants.ACTION_VALUE_NONE);
		editor.putString(Constants.INPUT_FLING_UP, Constants.ACTION_MENU);
		editor.putString(Constants.INPUT_FLING_DOWN, Constants.ACTION_VALUE_NONE);
		editor.putString(Constants.INPUT_CORNER_TOP_LEFT, Constants.ACTION_VALUE_NONE);
		editor.putString(Constants.INPUT_CORNER_TOP_RIGHT, Constants.ACTION_VALUE_NONE);			
		editor.commit();	
	}

	private void flipControls(String control1, String control2) {
		if (isUsedForPreviousNext(control1, control2)) {
			String action1 = preferences.getString(control1, Constants.ACTION_VALUE_PREVIOUS);
			String action2 = preferences.getString(control2, Constants.ACTION_VALUE_NEXT);
			Editor editor = preferences.edit();
			editor.putString(control1, action2);
			editor.putString(control2, action1);
			editor.commit();
		}
	}
	
	public void flipControls() {
		if (isLeftToRight()) {
			flipControls(Constants.TRACKBALL_RIGHT_KEY, Constants.TRACKBALL_LEFT_KEY);
			flipControls(Constants.INPUT_FLING_LEFT, Constants.INPUT_FLING_RIGHT);
			flipControls(Constants.INPUT_CORNER_BOTTOM_RIGHT, Constants.INPUT_CORNER_BOTTOM_LEFT);
			flipControls(Constants.INPUT_VOLUME_DOWN, Constants.INPUT_VOLUME_UP);
		} else {
			flipControls(Constants.TRACKBALL_LEFT_KEY, Constants.TRACKBALL_RIGHT_KEY); 
			flipControls(Constants.INPUT_FLING_RIGHT, Constants.INPUT_FLING_LEFT);
			flipControls(Constants.INPUT_CORNER_BOTTOM_LEFT, Constants.INPUT_CORNER_BOTTOM_RIGHT);
			flipControls(Constants.INPUT_VOLUME_UP, Constants.INPUT_VOLUME_DOWN);
		}
	}
	
	public void checkCleanExit() {
		Editor editor = preferences.edit();
		if (preferences.contains(Constants.CLEAN_EXIT_KEY)) {
			if (!preferences.getBoolean(Constants.CLEAN_EXIT_KEY, false)) {
				editor.remove(Constants.COMIC_PATH_KEY);
			}
		}
		editor.putBoolean(Constants.CLEAN_EXIT_KEY, false);
		editor.commit();
	}
	
	public void markCleanExit() {
		Editor editor = preferences.edit();
		editor.putBoolean(Constants.CLEAN_EXIT_KEY, true);
		editor.commit();
	}
	
	private static final int DEFAULT_MAX_IMAGE_WIDTH = 1000;
	private static final int DEFAULT_MAX_IMAGE_HEIGHT = 1500;
	private static final int MIN_IMAGE_DIMENSION = 480;
	
	public void setMaxImageResolution() {
		try {
			int maxWidth = Integer.valueOf(preferences.getString(PreferencesController.PREFERENCE_MAX_IMAGE_WIDTH, String.valueOf(DEFAULT_MAX_IMAGE_WIDTH)));
			int maxHeight = Integer.valueOf(preferences.getString(PreferencesController.PREFERENCE_MAX_IMAGE_HEIGHT, String.valueOf(DEFAULT_MAX_IMAGE_HEIGHT)));
			Comic.setMaxWidth(Math.max(MIN_IMAGE_DIMENSION, maxWidth));
			Comic.setMaxHeight(Math.max(MIN_IMAGE_DIMENSION, maxHeight));
		} catch (NumberFormatException e) {
			Log.w(this.getClass().getSimpleName(), "Failed to set max image resolution" , e);
		}
	}
	
}
