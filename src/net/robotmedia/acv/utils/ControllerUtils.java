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

import java.util.HashMap;

import net.androidcomics.acv.R;
import net.robotmedia.acv.Constants;

import android.app.Activity;

public class ControllerUtils {

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
	
}
