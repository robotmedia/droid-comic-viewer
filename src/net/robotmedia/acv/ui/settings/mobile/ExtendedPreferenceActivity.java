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
package net.robotmedia.acv.ui.settings.mobile;

import java.util.HashSet;

import net.androidcomics.acv.R;
import net.robotmedia.acv.logic.AdsManager;
import net.robotmedia.acv.logic.TrackingManager;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.*;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public abstract class ExtendedPreferenceActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private HashSet<String> showValueOnSummaryKeys = new HashSet<String>();
	private ViewGroup adsContainer;
	
	/**
	 * Show the value of the given preference on its summary. Use this function
	 * on onCreate.
	 * 
	 * @param key Preference key
	 */
	protected void showValueOnSummary(String key) {
		showValueOnSummaryKeys.add(key);
	}

	private void showValues() {
		final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		for (String key : showValueOnSummaryKeys) {
			final String value = sharedPreferences.getString(key, "");
			final Preference preference = this.findPreference(key);
			preference.setSummary(value);
		}
	}

	protected void showAd() {
		this.removeAd();
		View ad = AdsManager.getAd(this);
		if (ad != null) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			lp.gravity = Gravity.CENTER_HORIZONTAL;
			adsContainer.addView(ad, lp);
		}		
	}
	
	protected void removeAd() {
		adsContainer.removeAllViews();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);				
		setContentView(R.layout.settings);
		adsContainer = (ViewGroup) findViewById(R.id.adsContainer);
		this.showAd();
		addPreferencesFromResource(this.getPreferencesResource());
	}
	
	protected abstract int getPreferencesResource();
	
	
	@Override
	public void onStart() {
		super.onStart();
		TrackingManager.onStart(this);
		TrackingManager.pageView(String.valueOf(this.getTitle()));
	}

	@Override
	public void onStop() {
		super.onStop();
		TrackingManager.onStop(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		showValues();
		// Set up a listener whenever a key changes
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		this.showAd();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Unregister the listener whenever a key changes
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (this.showValueOnSummaryKeys.contains(key)) {
			final String value = sharedPreferences.getString(key, "");
			final Preference preference = this.findPreference(key);
			preference.setSummary(value);
		}
	}
	
	@Override
	protected void onDestroy() {	
		AdsManager.destroyAds(this);
		super.onDestroy();
	}
}
