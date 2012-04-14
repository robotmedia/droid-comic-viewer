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
package net.robotmedia.acv;

import java.util.HashMap;

import net.androidcomics.acv.R;
import android.app.Activity;

public class Constants {

	// State
	public static final String COMIC_PATH_KEY = "comic_path";
	public static final String LOAD_LAST_KEY = "load_last";
	public static final String VERSION_KEY = "version";
	public static final String CLEAN_EXIT_KEY = "clean_exit";
	public static final String BRIGHTNESS_KEY = "brightness";	
		
	// Events
	public static final String SINGLE_TAP_KEY = "single_tap";
	public static final String LONG_TAP_KEY = "long_tap";
	public static final String TRACKBALL_UP_KEY = "trackball_up";
	public static final String TRACKBALL_DOWN_KEY = "trackball_down";	
	public static final String TRACKBALL_LEFT_KEY = "trackball_left";	
	public static final String TRACKBALL_RIGHT_KEY = "trackball_right";
	public static final String TRACKBALL_CENTER_KEY = "trackball_center";
	public static final String BACK_KEY = "back";
	public static final String INPUT_DOUBLE_TAP = "double_tap";
	public static final String INPUT_FLING_LEFT = "fling_left";
	public static final String INPUT_FLING_RIGHT = "fling_right";
	public static final String INPUT_FLING_UP = "fling_up";
	public static final String INPUT_FLING_DOWN = "fling_down";
	public static final String INPUT_CORNER_TOP_LEFT = "corner_top_left";
	public static final String INPUT_CORNER_TOP_RIGHT = "corner_top_right";
	public static final String INPUT_CORNER_BOTTOM_LEFT = "corner_bottom_left";
	public static final String INPUT_CORNER_BOTTOM_RIGHT = "corner_bottom_right";
	public static final String INPUT_VOLUME_UP = "volume_up";
	public static final String INPUT_VOLUME_DOWN = "volume_down";

	
	// Control keys
	public static final String CONTROL_DEFAULTS_KEY = "control_defaults";
	public static final String AUTO_ROTATE_KEY = "auto_rotate";
	public static final String ORIENTATION_KEY = "orientation";
	public static final String REQUESTED_ROTATION_KEY = "requested_rotation";

	
	public static final String TRANSITION_MODE_KEY = "transition_mode";
	public static final String TRANSITION_MODE_TRANSLATE_VALUE = "translate";
	public static final String TRANSITION_MODE_FADE_VALUE = "fade";
	public static final String TRANSITION_MODE_NONE_VALUE = "none";
	public static final String TRANSITION_MODE_PUSH_UP_VALUE = "pushUp";
	public static final String TRANSITION_MODE_PUSH_DOWN_VALUE = "pushDown";

	public static final String SCALE_MODE_KEY = "scale_mode";
	public static final String SCALE_MODE_WIDTH_VALUE = "width";
	public static final String SCALE_MODE_HEIGHT_VALUE = "height";
	public static final String SCALE_MODE_BEST_VALUE = "best";
	public static final String SCALE_MODE_FRAME_VALUE = "frame";
	public static final String SCALE_MODE_NONE_VALUE = "none";
	
	public static final String SHOW_NUMBER_KEY = "show_number";
	public static final String PREFERENCE_INVISIBLE_CORNERS = "invisible_corners";
	
	public static final String COMICS_PATH_KEY = "comics_path";

	public static final String DIRECTION_KEY = "direction";
	public static final String DIRECTION_LEFT_TO_RIGHT_VALUE = "ltr";
	
	public static final int DIALOG_LOAD_ERROR = 1;
	public static final int DIALOG_FLIP_CONTROLS = 2;
	public static final int DIALOG_PAGE_ERROR = 4;
	
	// File extensions
	public static final String JPG_EXTENSION = "jpg";
	public static final String JPEG_EXTENSION = "jpeg";
	public static final String PNG_EXTENSION = "png";
	public static final String GIF_EXTENSION = "gif";
	public static final String BMP_EXTENSION = "bmp";
	public static final String ACV_EXTENSION = "acv";
	public static final String CBZ_EXTENSION = "cbz";
	public static final String ZIP_EXTENSION = "zip";
	public static final String RAR_EXTENSION = "rar";
	public static final String CBR_EXTENSION = "cbr";
	public static final String MP4_EXTENSION = "mp4";
	public static final String MP3_EXTENSION = "mp3";
	
	public static HashMap<String, Integer> getSupportedExtensions(Activity activity) {
		HashMap<String, Integer> supportedExtensions = new HashMap<String, Integer>();
		supportedExtensions.put(Constants.ACV_EXTENSION, R.drawable.icon);
		supportedExtensions.put(Constants.ZIP_EXTENSION, R.drawable.compress);			
		supportedExtensions.put(Constants.RAR_EXTENSION, R.drawable.compress);
		supportedExtensions.put(Constants.CBZ_EXTENSION, R.drawable.comment);
		supportedExtensions.put(Constants.JPG_EXTENSION, R.drawable.image);
		supportedExtensions.put(Constants.JPEG_EXTENSION, R.drawable.image);
		supportedExtensions.put(Constants.GIF_EXTENSION, R.drawable.image);
		supportedExtensions.put(Constants.BMP_EXTENSION, R.drawable.image);
		supportedExtensions.put(Constants.PNG_EXTENSION, R.drawable.image);
		supportedExtensions.put(Constants.CBR_EXTENSION, R.drawable.comment);
		return supportedExtensions;
	}
	
	public static final int SCREEN_BROWSER_CODE = 0;
	public static final int SD_BROWSER_CODE = 1;
	public static final int SETTINGS_CODE = 2;
	public static final int SUBSCRIBE_CODE = 3;
	
	public static final int BUFFER_SIZE = 0x4000;
	public static final int COMPRESSION_QUALITY = 80;
	
	@Deprecated
	public static String TEMP_PATH = "acv/.temp";
	
	public static float ZOOM_STEP = 1.5f;
	public static float MAX_ZOOM_FACTOR = 2f;

	public static final float MIN_FLING_DIFFERENCE_DIP = 120.0f;
	public static final int MAX_FLING_ANGLE = 25;
	public static final float MANUAL_SCROLL_INCREMENT_DIP = 100.0f;
	public static final float CORNER_WIDTH_DIP = 80;
	
	// Actions
	public static final String ACTION_VALUE_NEXT = "next";
	public static final String ACTION_VALUE_PREVIOUS = "previous";
	public static final String ACTION_VALUE_NEXT_SCREEN = "next_screen";
	public static final String ACTION_VALUE_PREVIOUS_SCREEN = "previous_screen";
	public static final String ACTION_VALUE_ZOOM_IN = "zoom_in";
	public static final String ACTION_VALUE_ZOOM_OUT = "zoom_out";
	public static final String ACTION_VALUE_ROTATE = "rotate";
	public static final String ACTION_VALUE_SCROLL_UP = "scroll_up";
	public static final String ACTION_VALUE_SCROLL_DOWN = "scroll_down";	
	public static final String ACTION_VALUE_SCROLL_LEFT = "scroll_left";
	public static final String ACTION_VALUE_SCROLL_RIGHT = "scroll_right";	
	public static final String ACTION_VALUE_FIRST = "first";
	public static final String ACTION_VALUE_LAST = "last";
	public static final String ACTION_VALUE_FIT_WIDTH = "fit_width";
	public static final String ACTION_VALUE_FIT_HEIGHT = "fit_height";
	public static final String ACTION_VALUE_FIT_SCREEN = "fit_screen";
	public static final String ACTION_VALUE_ACTUAL_SIZE = "actual_size";
	public static final String ACTION_VALUE_SCREEN_BROWSER = "screen_browser";
	public static final String ACTION_VALUE_SD_BROWSER = "sd_browser";
	public static final String ACTION_VALUE_SETTINGS = "settings";
	public static final String ACTION_VALUE_SHARE_APP = "share_app";
	public static final String ACTION_VALUE_SHARE_SCREEN = "share_screen";
	public static final String ACTION_VALUE_NONE = "none";
	public static final String ACTION_CLOSE = "close";
	public static final String ACTION_MENU = "menu";
	public static final String ACTION_SET_AS = "set_as";

	// Event tracking
	public static final String EVENT_OPEN = "open";
	public static final String EVENT_PARAM_TYPE = "type";
	public static final String EVENT_PARAM_INPUT = "input";
	public static final String EVENT_VALUE_LANDSCAPE = "landscape";
	public static final String EVENT_VALUE_PORTRAIT = "portrait";
	public static final String EVENT_VALUE_SQUARE = "square";
	public static final String EVENT_VALUE_UNDEFINED = "undefined";
	public static final String EVENT_VALUE_FOLDER = "folder";
	public static final String EVENT_VALUE_MENU = "menu";

	public static final String METADATA_FILE = "comic.xml";
	
	// Legacy constants
	@Deprecated
	public static final String COMIC_PATH_LEGACY_KEY = "file";
	@Deprecated
	public static final String LEGACY_FLING_ENABLED_KEY = "fling_enabled";	
	@Deprecated
	public static final String LEGACY_STARTUP_UPDATE_CHECK_KEY = "startup_update_check";
	@Deprecated
	public static final String LEGACY_TEMP_PATH = "acv/temp";
}
