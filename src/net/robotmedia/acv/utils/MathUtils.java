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
package net.robotmedia.acv.utils;

import android.content.Context;
import android.util.FloatMath;

public class MathUtils {
	
	public static float distance(float x1, float y1, float x2, float y2) {
		float deltaX = x1 - x2;
		float deltaY = y1 - y2;
		return FloatMath.sqrt(deltaX * deltaX + deltaY * deltaY);
	}
	
	public static double getAngle(float x1, float y1, float x2, float y2) {
		return Math.toDegrees(Math.acos((x1 - x2) / Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2))));
	}
	
	public static boolean isEqual(int valueA, int valueB, int tolerance) {
		return valueA >= valueB - tolerance && valueA <= valueB + tolerance;
	}
	
	public static int dipToPixel(Context context, float dip) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}

}
