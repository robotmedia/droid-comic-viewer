package net.robotmedia.acv.utils;

import android.os.Build;

public class BuildUtils {

	public static boolean isHoneycombOrLater() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}
	
	public static boolean isIceCreamSandwichOrLater() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
	}
	
}
