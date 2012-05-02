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

import net.robotmedia.acv.billing.BillingManager;
import android.app.Activity;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

public class PremiumSettingsHelper extends SettingsHelper {

	public PremiumSettingsHelper(Activity activity) {
		super(activity);
	}

	public final static String PREFERENCE_PURCHASE_PREMIUM = "purchase_premium";

	public void preparePurchasePremium(Preference preference) {
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				BillingManager.getInstance(getActivity()).purchasePremium();
				return true;
			}
		});
	}
	
}
