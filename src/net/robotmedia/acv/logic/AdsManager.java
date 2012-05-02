package net.robotmedia.acv.logic;

import net.androidcomics.acv.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.View;

public class AdsManager {

	private static String publisherId = null;
	private static String testDeviceId = null;
	private final static String PUBLISHER_ID_RESOURCE_NAME = "admob_publisher_id";
	private final static String PUBLISHER_TEST_DEVICE_ID = "admob_test_device_id";
	private static boolean usesAds = true;

	public static final int SIZE_BANNER = 0;
	public static final int SIZE_FULL_BANNER = 1;

	// For compatibility with older versions
	// (equivalent to Configuration.SCREENLAYOUT_SIZE_XLARGE, but avoiding reflection for this)
	protected static final int RETRO_SCREENLAYOUT_SIZE_XLARGE = 4;
	
	public static View getAd(Activity activity, int size) {
		init(activity);
		if (!usesAds) return null;

		View ad = AdMobProxy.getAd(activity, size, publisherId, testDeviceId);
		ad.setId(R.id.ad);
		return ad;
	}
	
	public static View getAd(Activity activity) {
		init(activity);
		if (!usesAds) return null;
		
		int adaptedSize;
		int screenSize = activity.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
		if(screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == RETRO_SCREENLAYOUT_SIZE_XLARGE) {
			adaptedSize = SIZE_FULL_BANNER;
		} else {
			adaptedSize = SIZE_BANNER;
		}
		return getAd(activity, adaptedSize);
	}
	
	public static void destroyAds(Activity activity) {
		if (!usesAds) return;
		
		AdMobProxy.destroyAds(activity, R.id.ad);
	}

	protected static void init(Context context) {
		if (!usesAds) return;
		
		if (publisherId != null) return;
		
		final int resourceId = context.getResources().getIdentifier(PUBLISHER_ID_RESOURCE_NAME, "string", context.getPackageName());
		final int testDeviceResourceId = context.getResources().getIdentifier(PUBLISHER_TEST_DEVICE_ID, "string", context.getPackageName());

		if (resourceId != 0) {
			publisherId = context.getString(resourceId);
			usesAds = true;
		} else {
			usesAds = false;
		}

		if (testDeviceResourceId != 0) {
			testDeviceId = context.getString(testDeviceResourceId);
		}
	}
	
	public static void disableAds() {
		usesAds = false;
	}
}
