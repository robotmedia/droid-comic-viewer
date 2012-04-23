package net.robotmedia.acv.ui.settings.tablet;

import net.androidcomics.acv.R;
import net.robotmedia.acv.billing.BillingManager;
import net.robotmedia.acv.billing.BillingManager.IObserver;
import net.robotmedia.acv.ui.settings.PremiumSettingsHelper;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class PremiumSettingsFragment extends ExtendedPreferenceFragment implements IObserver {

	private BillingManager billing;
	private PremiumSettingsHelper helper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.premium_settings);		

		this.helper = new PremiumSettingsHelper(this.getActivity());
		this.billing = new BillingManager(this.getActivity());
		this.billing.setObserver(this);

		Preference preference = findPreference(PremiumSettingsHelper.PREFERENCE_PURCHASE_PREMIUM);
		helper.preparePurchasePremium(preference, this.billing);
	}
	
	@Override
	public void onDestroy() {
		this.billing.setObserver(null);
		this.billing.onDestroy();
		super.onDestroy();
	}

	
	@Override
	public void onPremiumPurchased() {
		if (!(this.getActivity() instanceof PreferenceActivity)) return;
		
		PreferenceActivity activity = (PreferenceActivity) this.getActivity();
		
		// Doesn't select the correct header due to an Android bug. See: http://code.google.com/p/android/issues/detail?id=22430
		// String fragmentName = AboutSettingsFragment.class.getName();
		// activity.switchToHeader(fragmentName, null);
		
		activity.invalidateHeaders(); // Removes Premium header
	}
	
}
