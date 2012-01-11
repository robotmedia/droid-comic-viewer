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

import java.util.HashMap;


import android.content.Context;

public class TrackingManager {
	
	private static String flurryKey = null;
	private static boolean usesFlurry = true;
	private final static String FLURRY_KEY_RESOURCE_NAME = "flurry_key";
	
	public static void onStart(Context context) {
		if (usesFlurry) {
			if (flurryKey == null) {
				final int resid = context.getResources().getIdentifier(FLURRY_KEY_RESOURCE_NAME, "string", context.getPackageName());
				if (resid != 0) {
					flurryKey = context.getString(resid);
					FlurryAgentProxy.onStartSession(context, flurryKey);
				} else {
					usesFlurry = false;
				}
			} else {
				FlurryAgentProxy.onStartSession(context, flurryKey);
			}
		}
	}
	
	public static void onStop(Context context) {
		if (usesFlurry) {
			FlurryAgentProxy.onEndSession(context);
		}
	}
	
	public static void track(String event, String... paramValues) {
		if (usesFlurry) {
			HashMap<String, String> parameters = new HashMap<String, String>();
			for (int i = 0; i + 1 < paramValues.length; i+= 2) {
				parameters.put(paramValues[i], paramValues[i + 1]);
			}
			FlurryAgentProxy.onEvent(event, parameters);
		}
	}

	public static void pageView(String name) {
		TrackingManager.track("pageView", "name", name);
	}
	
	public static void trackError(String method, Exception e) {
		if (usesFlurry) {
			FlurryAgentProxy.onError(method, e.getMessage(), e.getClass().toString());
		}
	}
	
}
