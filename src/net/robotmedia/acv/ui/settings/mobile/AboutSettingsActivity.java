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

import net.androidcomics.acv.R;
import net.robotmedia.acv.Constants;
import net.robotmedia.acv.ui.SubscribeActivity;
import net.robotmedia.acv.ui.widget.DialogFactory;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

public class AboutSettingsActivity extends ExtendedPreferenceActivity {

	private static final String KEY_SUBSCRIBE = "subscribe";
	private static final String SOURCE_VALUE = "DroidComicViewer";

	@Override
	protected int getPreferencesResource() {
		return R.xml.about_settings;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Preference version = findPreference(Constants.VERSION_KEY);
		String versionName; 
		try {
			PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), 0);
			versionName = info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			versionName = "1";
		}
		version.setSummary(version.getSummary() + versionName);
		
		
		final Preference subscribe = findPreference(KEY_SUBSCRIBE);
		subscribe.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent myIntent = new Intent(AboutSettingsActivity.this, SubscribeActivity.class);
				myIntent.putExtra(SubscribeActivity.SOURCE_EXTRA, SOURCE_VALUE);
				startActivityForResult(myIntent, Constants.SUBSCRIBE_CODE);
				return true;
			}
		});

	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.SUBSCRIBE_CODE) {
			switch (resultCode) {
			case RESULT_OK:
				DialogFactory.showSimpleAlert(this, true, R.string.dialog_subscribe_success_title, R.string.dialog_subscribe_success_text);
				break;
			case SubscribeActivity.RESULT_ERROR:
				DialogFactory.showSimpleAlert(this, false, R.string.dialog_subscribe_error_title, R.string.dialog_subscribe_error_text);
				break;
			}
		}
	}
	
}
