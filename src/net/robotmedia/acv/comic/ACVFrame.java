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

import java.util.ArrayList;

import net.robotmedia.acv.Constants;

public class ACVFrame extends ACVRectangle {

	protected static final boolean DEFAULT_AUTOPLAY = false;

	protected static final float DEFAULT_DURATION = 0;

	protected static final float DEFAULT_TRANSITION_DURATION = 0;

	protected static final boolean DEFAULT_VIBRATE = false;

	private boolean autoplay = DEFAULT_AUTOPLAY;

	private String bgcolorString;

	private String description;

	private float duration = DEFAULT_DURATION;

	private String sound;

	private String transition = Constants.TRANSITION_MODE_TRANSLATE_VALUE;

	private float transitionDuration = DEFAULT_TRANSITION_DURATION;

	private boolean vibrate = DEFAULT_VIBRATE;
	
	private ArrayList<ACVContent> contents = new ArrayList<ACVContent>();
	
	public void add(ACVContent html) {
		this.contents.add(html);
	}
	
	public ArrayList<ACVContent> getContents() {
		return contents;
	}

	public String getBgcolorString() {
		return bgcolorString;
	}

	public String getDescription() {
		return description;
	}

	public float getDuration() {
		return duration;
	}

	public float getRelativeHeight() {
		return relativeHeight;
	}

	public float getRelativeWidth() {
		return relativeWidth;
	}

	public float getRelativeX() {
		return relativeX;
	}

	public float getRelativeY() {
		return relativeY;
	}

	public String getSound() {
		return sound;
	}

	public String getTransition() {
		return transition;
	}

	public float getTransitionDuration() {
		return transitionDuration;
	}

	public boolean isAutoplay() {
		return autoplay;
	}

	public boolean isVibrate() {
		return vibrate;
	}

	public void setAutoplay(boolean autoplay) {
		this.autoplay = autoplay;
	}

	public void setBgcolorString(String bgcolorString) {
		this.bgcolorString = bgcolorString;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setDuration(float duration) {
		this.duration = duration;
	}

	public void setSound(String sound) {
		this.sound = sound;
	}

	public void setTransition(String transition) {
		this.transition = transition;
	}

	public void setTransitionDuration(float transitionDuration) {
		this.transitionDuration = transitionDuration;
	}

	public void setVibrate(boolean vibrate) {
		this.vibrate = vibrate;
	}
}
