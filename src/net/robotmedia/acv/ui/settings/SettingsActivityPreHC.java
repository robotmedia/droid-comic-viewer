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
package net.robotmedia.acv.ui.settings;

import net.androidcomics.acv.R;
import net.robotmedia.acv.logic.AdsManager;
import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class SettingsActivityPreHC extends ExtendedPreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.settings);
		
		ViewGroup adsContainer = (ViewGroup) findViewById(R.id.adsContainer);
		View ad = AdsManager.getAd(this);
		if(ad != null) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			lp.gravity = Gravity.CENTER_HORIZONTAL;
			adsContainer.addView(ad, lp);
		}
		
		addPreferencesFromResource(R.xml.preferences);
	}
	
	@Override
	protected void onDestroy() {
		
		AdsManager.destroyAds(this);
		
		super.onDestroy();
	}
}
