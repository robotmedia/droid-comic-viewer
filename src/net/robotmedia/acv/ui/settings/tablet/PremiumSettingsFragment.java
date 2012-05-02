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
import net.robotmedia.acv.billing.BillingManager;
import net.robotmedia.acv.billing.BillingManager.IObserver;
import net.robotmedia.acv.ui.settings.PremiumSettingsHelper;
import android.app.Activity;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class PremiumSettingsFragment extends ExtendedPreferenceFragment implements IObserver {

	private PremiumSettingsHelper helper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.premium_settings);		

		this.helper = new PremiumSettingsHelper(this.getPurchaseActivity());
		BillingManager.getInstance(getActivity()).setObserver(this);

		Preference preference = findPreference(PremiumSettingsHelper.PREFERENCE_PURCHASE_PREMIUM);
		helper.preparePurchasePremium(preference);
	}
	
	@Override
	public void onDestroy() {
		BillingManager.getInstance(getActivity()).setObserver(null);
		super.onDestroy();
	}

	
	@Override
	public void onPremiumPurchased() {
		if (!(this.getPurchaseActivity() instanceof PreferenceActivity)) return;
		
		PreferenceActivity activity = (PreferenceActivity) this.getPurchaseActivity();
		
		// Doesn't select the correct header due to an Android bug. See: http://code.google.com/p/android/issues/detail?id=22430
		// String fragmentName = AboutSettingsFragment.class.getName();
		// activity.switchToHeader(fragmentName, null);
		
		activity.invalidateHeaders(); // Removes Premium header
	}

	@Override
	public Activity getPurchaseActivity() {
		return getActivity();
	}
	
}
