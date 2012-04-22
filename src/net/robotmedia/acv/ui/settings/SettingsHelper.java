package net.robotmedia.acv.ui.settings;

import net.robotmedia.acv.provider.HistoryManager;
import android.content.Context;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public abstract class SettingsHelper {
	
	private Context context;
	
	public final static String PREFERENCE_CLEAR_HISTORY = "clear_history";
	public final static String PREFERENCE_PURCHASE_PREMIUM = "purchase_premium";
	
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
	
	public void preparePurchasePremium() {
		this.setOnPreferenceClickListener(SettingsHelper.PREFERENCE_PURCHASE_PREMIUM, new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				purchasePremium();
				return true;
			}
		});		
	}
	
	
	public void setOnPreferenceClickListener(String key, OnPreferenceClickListener listener) {
		final Preference preference = findPreference(key);
		if (preference == null) return;
		
		preference.setOnPreferenceClickListener(listener);
	}
	
	protected abstract Preference findPreference(String key);
	
	protected void clearHistory() {
		HistoryManager.getInstance(context).clear();
	}
	
	protected void purchasePremium() {
		Toast msg = Toast.makeText(context, "Buy", Toast.LENGTH_LONG);
		msg.show();
	}
		
}
