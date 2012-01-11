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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import android.os.Build;

public class ServiceManager {

	// TODO: Allow to change folder
	private final static String GET_NATIVE_URL_URL = "http://api.libiri.com/get_native_url.php?platform=android&comic=";
	private final static String SUBSCRIBE_URL = "http://api.libiri.com/subscribe.php?platform=@platform&email=@email&source=@source";
	private final static String RESPONSE_ERROR = "NOK";
	private final static String RESPONSE_SUCCESS = "OK";
	
	public static String getNativeURL(String comicId) {
		BufferedReader in = null;
		String nativeURL = null;
		try {
			String urlAsString = GET_NATIVE_URL_URL + comicId;
			URL url = new URL(urlAsString);
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			nativeURL  = in.readLine();
			in.close();
			if (RESPONSE_ERROR.equals(nativeURL)) {
				nativeURL = null;
			}
		} catch (Exception e) {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
				}
			}
		}
		return nativeURL;
	}
	
	public static boolean subscribe(String email, String source) {
		BufferedReader in = null;
		try {
			String urlAsString = SUBSCRIBE_URL;
			final String platform;
			if (Build.MODEL.toLowerCase().contains("archos")) {
				platform = "archos";
			} else {
				platform = "android";
			}
			urlAsString = urlAsString.replaceAll("@platform", platform);
			urlAsString = urlAsString.replaceAll("@email", email);
			if (source == null) {
				source = "";
			}
			urlAsString = urlAsString.replaceAll("@source", source);
			URL url = new URL(urlAsString);
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			String response  = in.readLine();
			in.close();
			if (RESPONSE_SUCCESS.equals(response)) {
				return true;
			}
		} catch (Exception e) {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
				}
			}
		}
		return false;
	}
	
}
