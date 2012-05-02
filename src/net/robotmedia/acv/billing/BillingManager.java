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
package net.robotmedia.acv.billing;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import net.androidcomics.acv.R;
import net.robotmedia.acv.logic.AdsManager;
import net.robotmedia.billing.BillingController;
import net.robotmedia.billing.BillingController.BillingStatus;
import net.robotmedia.billing.BillingController.IConfiguration;
import net.robotmedia.billing.BillingRequest.ResponseCode;
import net.robotmedia.billing.IBillingObserver;
import net.robotmedia.billing.model.Transaction.PurchaseState;

public class BillingManager implements IBillingObserver {

	protected static final String KEY_TRANSACTIONS_RESTORED = "net.robotmedia.billing.transactionsRestored";

	private static BillingManager instance;
	
	public static BillingManager getInstance(Activity activity) {
		return getInstance(activity.getApplicationContext());
	}
	
	public static BillingManager getInstance(Context context) {
		if (instance == null) {
			instance = new BillingManager(context);
		}
		return instance;
	}
	
	public void initialize() {
		this.restoreTransactions();
		this.disableAds();
	}
	
	private void disableAds() {
		if (isPremium()) {
			AdsManager.disableAds();
		}
	}
	
	public interface IObserver {
		public void onPremiumPurchased();
		public Activity getPurchaseActivity();
	}
	
	private Context context;
	private static final String ITEM_PREMIUM = "premium";
	//private static final String ITEM_PREMIUM = "android.test.purchased";
	private IObserver observer;
	private static final String THANKS_DIALOG_SHOWN = "thanks_dialog_shown";

	private BillingManager(Context context) {
		this.context = context;
		BillingController.registerObserver(this);

		try {
			Class<?> c = Class.forName("net.robotmedia.acv.billing.Configuration");
			IConfiguration configuration = (IConfiguration) c.newInstance();
			BillingController.setDebug(true);
			BillingController.setConfiguration(configuration);
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		}

		BillingController.checkBillingSupported(context);	
	}
	
	public void purchasePremium() {
		BillingController.requestPurchase(this.context, ITEM_PREMIUM);
	}
	
	public boolean canPurchasePremium() {
		return BillingController.checkBillingSupported(context) == BillingStatus.SUPPORTED && !isPremium();
	}
	
	public boolean isPremium() {
		return BillingController.isPurchased(context, ITEM_PREMIUM);
	}
	
	private void restoreTransactions() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean restored = preferences.getBoolean(KEY_TRANSACTIONS_RESTORED, false);

		if (restored) return;

		BillingController.restoreTransactions(context);
	}

	@Override
	public void onBillingChecked(boolean supported) {
	}

	@Override
	public void onPurchaseIntent(String itemId, PendingIntent purchaseIntent) {
		if (!ITEM_PREMIUM.equals(itemId)) return;
		if (this.observer == null) return;
		
		BillingController.startPurchaseIntent(this.observer.getPurchaseActivity(), purchaseIntent, null);
	}

	@Override
	public void onPurchaseStateChanged(String itemId, PurchaseState state) {
		if (!ITEM_PREMIUM.equals(itemId)) return;

		if (state != PurchaseState.PURCHASED) return;

		this.disableAds();
		
		if(observer == null) {
			return;
		}
	
		Activity purchaseActivity = observer.getPurchaseActivity();
		final AlertDialog dialog = new AlertDialog.Builder(purchaseActivity).setIcon(android.R.drawable.ic_menu_info_details)
				.setTitle(R.string.alert_premium_purchased_title).setMessage(R.string.alert_premium_purchased_message)
				.setPositiveButton(android.R.string.ok, null).create();
		
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		boolean thanksDialogShown = preferences.getBoolean(THANKS_DIALOG_SHOWN, false);
		
		if(!thanksDialogShown) {
			dialog.show();
		}
		
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(THANKS_DIALOG_SHOWN, true);
		editor.commit();
		
		observer.onPremiumPurchased();
		
	}

	@Override
	public void onRequestPurchaseResponse(String itemId, ResponseCode response) {}

	@Override
	public void onTransactionsRestored() {
		final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		final Editor editor = preferences.edit();
		editor.putBoolean(KEY_TRANSACTIONS_RESTORED, true);
		editor.commit();
		
		this.disableAds();
	}

	public IObserver getObserver() {
		return observer;
	}

	public void setObserver(IObserver observer) {
		this.observer = observer;
	}
	

}
