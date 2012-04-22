package net.robotmedia.acv.logic;

import java.lang.reflect.*;

import net.androidcomics.acv.BuildConfig;
import android.app.Activity;
import android.view.View;

public class AdMobProxy {

	private static Class<?> adViewClass = null;
	private static Class<?> adSizeClass = null;
	private static Class<?> adRequestClass = null;
	private static Method adRequestSetTestingMethod = null;
	private static Method adRequestAddTestDeviceMethod = null;
	private static Constructor<?> adViewConstructor = null;
	private static Method adViewLoadMethod = null;
	private static Method adViewDestroyMethod = null;

	private static void init() {
		if (adViewConstructor == null)  {
			try {
				adViewClass = Class.forName("com.google.ads.AdView");
				adSizeClass = Class.forName("com.google.ads.AdSize");
				adRequestClass = Class.forName("com.google.ads.AdRequest");
				adRequestSetTestingMethod = adRequestClass.getMethod("setTesting", boolean.class);
				adRequestAddTestDeviceMethod = adRequestClass.getMethod("addTestDevice", String.class);
				adViewConstructor = adViewClass.getConstructor(Activity.class, adSizeClass, String.class);
				adViewLoadMethod = adViewClass.getMethod("loadAd", adRequestClass);
				adViewDestroyMethod = adViewClass.getMethod("destroy");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}

	public static View getAd(Activity activity, int size, String publisherId, String testDevice) {
		init();

		if (adViewConstructor != null) {
			View view;
			try {
				// == new AdView(activity, size, publisherId)
				view = (View) adViewConstructor.newInstance(activity, sizeToAdSize(size), publisherId);

				// == new AdRequest()
				Object adRequest = adRequestClass.newInstance();

				// == adRequest.setTesting(true);
				// == adRequest.addTestDevice(...);
				// Only if we are in debug mode (requires Android SDK >= 18)
				// and the proper test device id has been added as a resource string.
				if(BuildConfig.DEBUG && testDevice != null) {
					adRequestSetTestingMethod.invoke(adRequest, true);
					adRequestAddTestDeviceMethod.invoke(adRequest, testDevice);
					adRequestAddTestDeviceMethod.invoke(adRequest, adRequestClass.getField("TEST_EMULATOR").get(null));
				}

				// == adView.loadAd(adRequest);
				adViewLoadMethod.invoke(view, adRequest);

				return view;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Apparently there's a serious bug with WebViews + Activity.onDestroy in Android 1.5 and 1.6.
	 * As AdMob uses WebViews, it forces the application to crash.
	 * The work-around is to destroy AdViews in activity.onDestroy.
	 * This method removes the ad with id = viewId in the activity view hierarchy.
	 * 
	 * @param activity
	 * @param viewId
	 */
	public static void destroyAds(Activity activity, int viewId) {
		if(adViewDestroyMethod != null) {
			View v = activity.findViewById(viewId);
			if(v != null) {
				try {
					adViewDestroyMethod.invoke(v);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static final String BANNER = "BANNER";
	private static final String IAB_MRECT = "IAB_MRECT";
	private static final String IAB_BANNER = "IAB_BANNER";
	private static final String IAB_LEADERBOARD = "IAB_LEADERBOARD";
	
	private static Object sizeToAdSize(int size) {

		Field f = null;
		
		try {
			switch(size) {
				case AdsManager.SIZE_FULL_BANNER:
					f = adSizeClass.getField(IAB_BANNER);
					break;
				default:
					case AdsManager.SIZE_BANNER:
					f = adSizeClass.getField(BANNER);	
			}
			if(f != null)
				return f.get(null);

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		}

		return null;
	}
}
