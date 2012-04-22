package net.robotmedia.acv.ui.settings.tablet;

import net.androidcomics.acv.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public class PremiumSettingsFragment extends PreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.premium_settings);
	}
	
}
