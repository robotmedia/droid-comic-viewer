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

import java.util.HashSet;

import net.robotmedia.acv.logic.TrackingManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class ExtendedPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
	
	private HashSet<String> showValueOnSummaryKeys = new HashSet<String>();
	
	/**
	 * Show the value of the given preference on its summary. Use this function on onCreate.
	 * @param key Preference key
	 */
	protected void showValueOnSummary(String key) {
		showValueOnSummaryKeys.add(key);
	}
	
	private void showValues() {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
		for (String key : showValueOnSummaryKeys) {
			final String value = sharedPreferences.getString(key, "");
			final Preference preference = this.findPreference(key);
			preference.setSummary(value);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onStart()
	{
	   super.onStart();
	   TrackingManager.onStart(this.getActivity());
	   TrackingManager.pageView(String.valueOf(this.getActivity().getTitle()));
	}
	
	@Override
	public void onStop()
	{
	   super.onStop();
	   TrackingManager.onStop(this.getActivity());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		showValues();
		// Set up a listener whenever a key changes
		PreferenceManager.getDefaultSharedPreferences(this.getActivity()).registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		PreferenceManager.getDefaultSharedPreferences(this.getActivity()).unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (this.showValueOnSummaryKeys.contains(key)) {
			final String value = sharedPreferences.getString(key, "");
			final Preference preference = this.findPreference(key);
			preference.setSummary(value);
		}
	}
}
