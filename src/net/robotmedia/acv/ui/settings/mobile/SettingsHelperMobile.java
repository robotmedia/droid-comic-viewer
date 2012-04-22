package net.robotmedia.acv.ui.settings.mobile;

import android.preference.Preference;
import android.preference.PreferenceActivity;
import net.robotmedia.acv.ui.settings.SettingsHelper;

public class SettingsHelperMobile extends SettingsHelper {

	private PreferenceActivity preferences;
	
	public SettingsHelperMobile(PreferenceActivity preferences) {
		super(preferences);
		this.preferences = preferences;
	}

	@Override
	protected Preference findPreference(String key) {
		return this.preferences.findPreference(key);
	}
	
	@Override
	protected void clearHistory() {
		super.clearHistory();
		this.preferences.finish();
	}
	
}
