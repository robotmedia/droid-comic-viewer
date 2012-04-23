package net.robotmedia.acv.billing;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import net.androidcomics.acv.R;
import net.robotmedia.acv.logic.AdsManager;
import net.robotmedia.billing.BillingController;
import net.robotmedia.billing.BillingRequest.ResponseCode;
import net.robotmedia.billing.IBillingObserver;
import net.robotmedia.billing.model.Transaction.PurchaseState;

public class BillingManager implements IBillingObserver {

	public interface IObserver {
		public void onPremiumPurchased();
	}
	
	private Activity context;
	private static final String ITEM_PREMIUM = "android.test.purchased";
	private IObserver observer;

	public BillingManager(Activity context) {
		this.context = context;
		BillingController.registerObserver(this);
	}
	
	public void onDestroy() {
		BillingController.unregisterObserver(this);
	}
	
	public void purchasePremium() {
		BillingController.requestPurchase(this.context, ITEM_PREMIUM);
	}
	
	public static boolean isPremium(Context context) {
		return BillingController.isPurchased(context, ITEM_PREMIUM);
	}

	@Override
	public void onBillingChecked(boolean supported) {		
	}

	@Override
	public void onPurchaseIntent(String itemId, PendingIntent purchaseIntent) {
		if (!ITEM_PREMIUM.equals(itemId)) return;
		
		BillingController.startPurchaseIntent(context, purchaseIntent, null);
	}

	@Override
	public void onPurchaseStateChanged(String itemId, PurchaseState state) {
		if (!ITEM_PREMIUM.equals(itemId)) return;

		if (state != PurchaseState.PURCHASED) return;
		
		AdsManager.disableAds();
		
		final AlertDialog dialog = new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_menu_info_details)
				.setTitle(R.string.alert_premium_purchased_title).setMessage(R.string.alert_premium_purchased_message)
				.setPositiveButton(android.R.string.ok, null).create();
		dialog.show();
		
		if (this.observer != null) {
			observer.onPremiumPurchased();
		}
	}

	@Override
	public void onRequestPurchaseResponse(String itemId, ResponseCode response) {}

	@Override
	public void onTransactionsRestored() {}

	public IObserver getObserver() {
		return observer;
	}

	public void setObserver(IObserver observer) {
		this.observer = observer;
	}
	

}
