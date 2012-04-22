package net.robotmedia.acv.billing;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.widget.Toast;
import net.androidcomics.acv.R;
import net.robotmedia.billing.BillingController;
import net.robotmedia.billing.BillingRequest.ResponseCode;
import net.robotmedia.billing.IBillingObserver;
import net.robotmedia.billing.model.Transaction.PurchaseState;

public class BillingManager implements IBillingObserver {
	
	private Activity context;
	private static final String ITEM_PREMIUM = "android.test.purchased";

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
		
		Toast toast = Toast.makeText(context, context.getString(R.string.premium_purchased), Toast.LENGTH_SHORT);
		toast.show();
		
		context.finish();
	}

	@Override
	public void onRequestPurchaseResponse(String itemId, ResponseCode response) {}

	@Override
	public void onTransactionsRestored() {}
	

}
