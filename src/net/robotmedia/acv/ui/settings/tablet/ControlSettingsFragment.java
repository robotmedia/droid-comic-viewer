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
package net.robotmedia.acv.ui.settings.tablet;

import net.androidcomics.acv.R;
import net.robotmedia.acv.Constants;
import net.robotmedia.acv.logic.PreferencesController;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;

public class ControlSettingsFragment extends ExtendedPreferenceFragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.control_settings);
		
		Preference controlDefaults = findPreference(Constants.CONTROL_DEFAULTS_KEY);
		controlDefaults.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				PreferencesController pController = new PreferencesController(ControlSettingsFragment.this.getActivity());
				pController.restoreControlDefaults();
				return true;
			}
		});
	}
	
	public final String[] customControlKeys = new String[] {Constants.SINGLE_TAP_KEY,
			Constants.INPUT_DOUBLE_TAP, Constants.LONG_TAP_KEY, Constants.TRACKBALL_UP_KEY,
			Constants.TRACKBALL_DOWN_KEY, Constants.TRACKBALL_LEFT_KEY, Constants.TRACKBALL_RIGHT_KEY,
			Constants.TRACKBALL_CENTER_KEY, Constants.INPUT_FLING_UP, Constants.INPUT_FLING_DOWN, Constants.INPUT_FLING_LEFT,
			Constants.INPUT_FLING_RIGHT, Constants.INPUT_CORNER_BOTTOM_LEFT, Constants.INPUT_CORNER_BOTTOM_RIGHT,
			Constants.INPUT_CORNER_TOP_LEFT, Constants.INPUT_CORNER_TOP_RIGHT, Constants.BACK_KEY, Constants.INPUT_VOLUME_UP, Constants.INPUT_VOLUME_DOWN};
	
	@Override 
	public void onResume() {
		super.onResume();
		for (int i = 0; i < customControlKeys.length; i++) {
			updatePreference(customControlKeys[i]);			
		}
	}

	private String translateAction(String action) {
		Resources resources = getResources();
		String[] values = resources.getStringArray(R.array.action_values);
		int i;
		for (i = 0; i < values.length; i++) {
			if (values[i].equals(action)) {
				break;
			}
		}
		String[] labels = resources.getStringArray(R.array.action_labels);
   	    return labels[i];
	}
	
	private void updatePreference(String key) {
		for (int i = 0; i < customControlKeys.length; i++) {
			if (customControlKeys[i].equals(key)) {
				Preference preference = findPreference(key);
				String value = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString(key, null);
				if (value != null) {
					String actionLabel = translateAction(value);
					preference.setSummary(actionLabel);
				}
				break;
			}
		}
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		super.onSharedPreferenceChanged(sharedPreferences, key);
		updatePreference(key);
	}

}
