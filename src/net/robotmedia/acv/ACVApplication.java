/*******************************************************************************
 * Copyright 2009 Robot Media SL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.robotmedia.acv;

import net.robotmedia.acv.logic.PreferencesController;
import net.robotmedia.billing.BillingController;
import net.robotmedia.billing.BillingController.IConfiguration;
import net.robotmedia.billing.BillingRequest.ResponseCode;
import net.robotmedia.billing.IBillingObserver;
import net.robotmedia.billing.model.Transaction.PurchaseState;
import android.app.Application;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class ACVApplication extends Application implements IBillingObserver {
	
	protected static final String KEY_TRANSACTIONS_RESTORED = "net.robotmedia.billing.transactionsRestored";
	
	@Override
	public void onCreate() {
		super.onCreate();
		final PreferencesController preferences = new PreferencesController(this);
		preferences.legacy();
		preferences.setMaxImageResolution();
		
		this.initializeBilling();
	}
	
	private void initializeBilling() {
		try {
			Class<?> c = Class.forName("net.robotmedia.acv.billing.Configuration");
			IConfiguration configuration = (IConfiguration) c.newInstance();
			BillingController.setConfiguration(configuration);
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}
				
		BillingController.checkBillingSupported(this);		
	}
	
	private void restoreTransactions() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean restored = preferences.getBoolean(KEY_TRANSACTIONS_RESTORED, false);
		if (restored) return;
		
		BillingController.registerObserver(this);		
		BillingController.restoreTransactions(this);
	}

	@Override
	public void onTransactionsRestored() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		final Editor editor = preferences.edit();
		editor.putBoolean(KEY_TRANSACTIONS_RESTORED, true);
		editor.commit();

		BillingController.unregisterObserver(this);
	}

	@Override
	public void onBillingChecked(boolean supported) {
		if (!supported) return;
		
		this.restoreTransactions();
	}


	@Override
	public void onPurchaseStateChanged(String itemId, PurchaseState state) {}


	@Override
	public void onRequestPurchaseResponse(String itemId, ResponseCode response) {}


	@Override
	public void onPurchaseIntent(String itemId, PendingIntent purchaseIntent) {}
}
