package net.robotmedia.acv.logic;

import android.app.Activity;
import android.content.Context;
import android.view.View;

public class AdsManager {

	private static String publisherId = null;
	private static String testDeviceId = null;
	private final static String PUBLISHER_ID_RESOURCE_NAME = "admob_publisher_id";
	private final static String PUBLISHER_TEST_DEVICE_ID = "admob_test_device_id";
	private static boolean usesAds = true;

	public static int SIZE_BANNER = 0;

	public static View getAd(Activity activity, int size) {
		init(activity);
		if (usesAds) {
			return AdMobProxy.getAd(activity, size, publisherId, testDeviceId);
		}
		return null;
	}

	protected static void init(Context context) {
		if (usesAds) {
			if (publisherId == null) {
				final int resourceId = context.getResources().getIdentifier(PUBLISHER_ID_RESOURCE_NAME, "string", context.getPackageName());
				final int testDeviceResourceId = context.getResources().getIdentifier(PUBLISHER_TEST_DEVICE_ID, "string",
						context.getPackageName());

				if (resourceId != 0) {
					publisherId = context.getString(resourceId);
				} else {
					usesAds = false;
				}

				if (testDeviceResourceId != 0) {
					testDeviceId = context.getString(testDeviceResourceId);
				}
			}
		}
	}
}
