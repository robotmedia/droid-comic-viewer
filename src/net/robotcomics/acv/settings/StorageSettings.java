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
package net.robotcomics.acv.settings;

import net.androidcomics.acv.R;
import net.robotcomics.acv.model.HistoryManager;
import net.robotmedia.activity.ExtendedPreferenceActivity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

public class StorageSettings extends ExtendedPreferenceActivity {

	private final static String CLEAR_HISTORY = "clear_history";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.storage_settings);
		
		final Preference clearHistory = findPreference(CLEAR_HISTORY);
		clearHistory.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				HistoryManager.getInstance(StorageSettings.this).clear();
				finish();
				return true;
			}
		});
	}
	
}
