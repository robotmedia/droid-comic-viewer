package net.robotmedia.acv.ui.settings;

import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

public abstract class SettingsHelper {
		
	public final static String PREFERENCE_CLEAR_HISTORY = "clear_history";
	public final static String PREFERENCE_PURCHASE_PREMIUM = "purchase_premium";
		
	public void setOnPreferenceClickListener(String key, OnPreferenceClickListener listener) {
		final Preference preference = findPreference(key);
		if (preference == null) return;
		
		preference.setOnPreferenceClickListener(listener);
	}
	
	protected abstract Preference findPreference(String key);

}
