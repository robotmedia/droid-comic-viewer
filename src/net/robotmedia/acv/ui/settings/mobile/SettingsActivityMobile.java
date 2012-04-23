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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceGroup;
import net.androidcomics.acv.R;
import net.robotmedia.acv.billing.BillingManager;
import net.robotmedia.acv.billing.BillingManager.IObserver;
import net.robotmedia.acv.ui.settings.SettingsHelper;

public class SettingsActivityMobile extends ExtendedPreferenceActivity implements IObserver {

	private static final String PREFERENCE_ROOT = "root";
	private static final String PREFERENCE_PREMIUM = "premium";
	
	private BillingManager billing;
	
	@Override
	protected int getPreferencesResource() {
		return R.xml.preferences;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.billing = new BillingManager(this);
		this.billing.setObserver(this);
		
		if (BillingManager.isPremium(this)) {
			this.removePremium();
		} else {	
			this.helper.setOnPreferenceClickListener(SettingsHelper.PREFERENCE_PURCHASE_PREMIUM, new OnPreferenceClickListener() {
				
				@Override
				public boolean onPreferenceClick(Preference preference) {
					billing.purchasePremium();
					return true;
				}
			});
		}
	}
	
	@Override
	protected void onDestroy() {
		this.billing.setObserver(null);
		this.billing.onDestroy();
		super.onDestroy();
	}

	@Override
	public void onPremiumPurchased() {
		this.removePremium();
	}
	
	private void removePremium() {
		PreferenceGroup root = (PreferenceGroup) findPreference(PREFERENCE_ROOT);
		Preference premium = root.findPreference(PREFERENCE_PREMIUM);
		if (premium == null) return;
		
		root.removePreference(premium);
	}
	
}
