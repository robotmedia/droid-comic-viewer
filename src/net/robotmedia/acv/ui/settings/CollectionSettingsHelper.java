package net.robotmedia.acv.ui.settings;

import net.robotmedia.acv.provider.HistoryManager;
import android.app.Activity;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

public class CollectionSettingsHelper extends SettingsHelper {
	
	public final static String PREFERENCE_CLEAR_HISTORY = "clear_history";
		
	public CollectionSettingsHelper(Activity activity) {
		super(activity);
	}
	
	public void prepareClearHistory(Preference preference) {
		preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				clearHistory();
				return true;
			}
		});
	}
	
	protected void clearHistory() {
		HistoryManager.getInstance(this.getActivity()).clear();
	}
	
}
