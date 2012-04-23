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

	public void preparePurchasePremium(Preference preference, final BillingManager billing) {
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				billing.purchasePremium();
				return true;
			}
		});
	}
	
}
