package net.robotmedia.acv.ui.settings;

import net.robotmedia.acv.provider.HistoryManager;
import android.content.Context;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

public abstract class SettingsHelper {
	
	private Context context;
	
	public final static String PREFERENCE_CLEAR_HISTORY = "clear_history";
	
	protected SettingsHelper(Context context) {
		this.context = context;
	}
	
	public void prepareClearHistory() {
		this.setOnPreferenceClickListener(SettingsHelper.PREFERENCE_CLEAR_HISTORY, new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				clearHistory();
				return true;
			}
		});		
	}
	
	
	public void setOnPreferenceClickListener(String key, OnPreferenceClickListener listener) {
		final Preference preference = findPreference(key);
		if (preference == null) return;
		
		preference.setOnPreferenceClickListener(listener);
	}
	
	public void clearHistory() {
		HistoryManager.getInstance(context).clear();
	}
	
	protected abstract Preference findPreference(String key);
	
}
