package net.robotmedia.acv.ui.settings.tablet;

import net.androidcomics.acv.R;
import android.os.Bundle;

public class PremiumSettingsFragment extends ExtendedPreferenceFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.premium_settings);		
	}
	
}
