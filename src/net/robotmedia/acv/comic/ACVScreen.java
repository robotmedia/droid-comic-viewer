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

import java.io.File;
import java.util.ArrayList;

import net.robotmedia.acv.comic.ACVComic.Message;

public class ACVScreen {

	protected static final float DEFAULT_DURATION = 0;
	protected static final float DEFAULT_TRANSITION_DURATION = 0.5f;
	
	private boolean autoplay = false;

	private String bgcolorString = null;

	private String description;

	private float duration = DEFAULT_DURATION;

	private ArrayList<ACVFrame> frames;

	private int index;

	private Message message;

	private String sound;

	private String title;

	private String transition;

	private float transitionDuration = DEFAULT_TRANSITION_DURATION;

	private boolean vibrate;

	private File videoFile;

	private ArrayList<ACVContent> contents = new ArrayList<ACVContent>();
	
	public void add(ACVContent html) {
		this.contents.add(html);
	}
	
	public ArrayList<ACVContent> getContents() {
		return contents;
	}
	
	public ACVScreen(int index) {
		this.index = index;
		frames = new ArrayList<ACVFrame>();
	}

	public void addFrame(ACVFrame frame) {
		frames.add(frame);
	}

	public int framesSize() {
		return frames.size();
	}

	public String getBgcolorString() {
		return bgcolorString;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Return the duration in miliseconds. The default duration is
	 * DEFAULT_DURATION.
	 * 
	 * @return The duration in miliseconds.
	 */
	public long getDuration() {
		return Math.round(duration * 1000);
	}

	public ACVFrame getFrame(int index) {
		return index < frames.size() ? frames.get(index) : null;
	}

	public int getIndex() {
		return index;
	}

	public Message getMessage() {
		return message;
	}

	public String getSound() {
		return sound;
	}

	public String getTitle() {
		return title;
	}

	public String getTransition() {
		return transition;
	}

	public String getTransition(int frameIndex) {
		ACVFrame frame = getFrame(frameIndex);
		return frame != null ? frame.getTransition() : null;
	}

	/**
	 * Return the transition duration in miliseconds. The default transition
	 * duration is DEFAULT_TRANSITION_DURATION.
	 * 
	 * @return The transition duration in miliseconds.
	 */
	public long getTransitionDuration() {
		return Math.round(transitionDuration * 1000);
	}

	public File getVideoFile() {
		return videoFile;
	}

	public boolean isAutoplay() {
		return autoplay;
	}

	public void setAutoplay(boolean autoplay) {
		this.autoplay = autoplay;
	}
	
	public boolean isVibrate() {
		return vibrate;
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

	public void setIndex(int index) {
		this.index = index;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

	public void setSound(String sound) {
		this.sound = sound;
	}

	public void setTitle(String title) {
		this.title = title;
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

	public void setVideoFile(File file) {
		this.videoFile = file;
	}

}
