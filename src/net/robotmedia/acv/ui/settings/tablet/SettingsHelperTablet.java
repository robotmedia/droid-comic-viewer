package net.robotmedia.acv.ui.settings.tablet;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import net.robotmedia.acv.ui.settings.SettingsHelper;

public class SettingsHelperTablet extends SettingsHelper {

	private PreferenceFragment preferences;
	
	public SettingsHelperTablet(PreferenceFragment preferences) {
		super(preferences.getActivity());
		this.preferences = preferences;
	}
	
	@Override
	protected Preference findPreference(String key) {
		return preferences.findPreference(key);
	}

}
