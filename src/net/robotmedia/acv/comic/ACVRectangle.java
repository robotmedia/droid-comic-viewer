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
package net.robotmedia.acv.comic;

import android.graphics.Point;
import android.graphics.Rect;

public class ACVRectangle {

	protected float relativeX;
	protected float relativeY;
	protected float relativeWidth;
	protected float relativeHeight;
	
	public void setArea(float relativeX, float relativeY, float relativeWidth, float relativeHeight) {
		this.relativeX = relativeX;
		this.relativeY = relativeY;
		this.relativeWidth = relativeWidth;
		this.relativeHeight = relativeHeight;
	}
	
	public Rect createRect(int containerWidth, int containerHeight) {
		final int x = Math.round(containerWidth * relativeX);
		final int y = Math.round(containerHeight * relativeY);
		final int width = Math.round(containerWidth * relativeWidth);
		final int height = Math.round(containerHeight * relativeHeight);
		final Rect rect = new Rect(x, y, x + width, y + height);
		return rect;
	}
	
	public boolean matches(Point p, int containerWidth, int containerHeight) {
		final Rect r = this.createRect(containerWidth, containerHeight);
		return r.contains(p.x, p.y);
	}
}
