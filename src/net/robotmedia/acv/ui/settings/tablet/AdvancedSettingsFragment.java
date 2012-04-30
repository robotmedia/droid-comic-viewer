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
import net.robotmedia.acv.logic.PreferencesController;
import android.content.SharedPreferences;
import android.os.Bundle;

public class AdvancedSettingsFragment extends ExtendedPreferenceFragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.advanced_settings);
		this.showValueOnSummary(PreferencesController.PREFERENCE_MAX_IMAGE_WIDTH);
		this.showValueOnSummary(PreferencesController.PREFERENCE_MAX_IMAGE_HEIGHT);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		super.onSharedPreferenceChanged(sharedPreferences, key);
		final PreferencesController preferences = new PreferencesController(this.getActivity());
		preferences.setMaxImageResolution();
	}
	
}
