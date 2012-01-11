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
package net.robotmedia.acv.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.RelativeLayout;

public class Letterbox extends RelativeLayout {

	protected View mLetterboxTop;
	protected View mLetterboxBottom;
	protected View mLetterboxLeft;
	protected View mLetterboxRight;
	private int mLetterboxWidth;
	private int mLetterboxHeight;
	private int mColor;
		
	private View addLetterbox(Context context, int width, int height, int align) {
		final View letterbox = new View(context);
		final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
		params.addRule(align);
		letterbox.setLayoutParams(params);
		letterbox.setBackgroundColor(android.R.color.transparent);
		this.addView(letterbox);
		return letterbox;
	}
	
	private void init(Context context) {
		mLetterboxTop = addLetterbox(context, LayoutParams.FILL_PARENT, 1, RelativeLayout.ALIGN_PARENT_TOP);
		mLetterboxBottom = addLetterbox(context, LayoutParams.FILL_PARENT, 1, RelativeLayout.ALIGN_PARENT_BOTTOM);
		mLetterboxLeft = addLetterbox(context, 1, LayoutParams.FILL_PARENT, RelativeLayout.ALIGN_PARENT_LEFT);	
		mLetterboxRight = addLetterbox(context, 1, LayoutParams.FILL_PARENT, RelativeLayout.ALIGN_PARENT_RIGHT);		
	}
	
	public Letterbox(Context context) {
		super(context);
		init(context);
	}
	
	public Letterbox(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void layoutTop() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, mLetterboxHeight);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		mLetterboxTop.setLayoutParams(params);
		mLetterboxTop.setBackgroundColor(mLetterboxHeight > 1 ? mColor : Color.TRANSPARENT);
	}

	private void layoutBottom() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, mLetterboxHeight);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		mLetterboxBottom.setLayoutParams(params);
		mLetterboxBottom.setBackgroundColor(mLetterboxHeight > 1 ? mColor : Color.TRANSPARENT);
	}
	
	private void layoutLeft() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mLetterboxWidth, ViewGroup.LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		mLetterboxLeft.setLayoutParams(params);
		mLetterboxLeft.setBackgroundColor(mLetterboxWidth > 1 ? mColor : Color.TRANSPARENT);
	}
	
	private void layoutRight() {
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(mLetterboxWidth, ViewGroup.LayoutParams.FILL_PARENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mLetterboxRight.setLayoutParams(params);
		mLetterboxRight.setBackgroundColor(mLetterboxWidth > 1 ? mColor : Color.TRANSPARENT);
	}
	
	private void configureAndStart(View target, Animation animation, long duration) {
		Drawable background = target.getBackground();
		animation.setInterpolator(new DecelerateInterpolator());
		animation.setDuration(duration);
		target.startAnimation(animation);
		if (background instanceof TransitionDrawable) {
			((TransitionDrawable) background).startTransition((int)duration);
		}
	}
	
	private void animateTop(long duration) {
		// Utils.logFrame("top", mLetterboxTop);
		float topScale = (float) mLetterboxHeight / (float) mLetterboxTop.getHeight();
		ScaleAnimation topAnimation = new ScaleAnimation(1, 1, 1, topScale, 0, 0);
		topAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				mLetterboxTop.clearAnimation(); // TODO: Find out why is this needed.
				layoutTop();
			}

			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationStart(Animation animation) {}}
		);
		configureAndStart(mLetterboxTop, topAnimation, duration);
	}
	
	private void animateBottom(long duration) {
		// Utils.logFrame("bottom", mLetterboxBottom);
		float topScale = (float) mLetterboxHeight / (float) mLetterboxTop.getHeight();
		ScaleAnimation bottomAnimation = new ScaleAnimation(1, 1, 1, topScale, 0, mLetterboxBottom.getHeight());
		bottomAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				mLetterboxBottom.clearAnimation();
				layoutBottom();
			}

			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationStart(Animation animation) {}}
		);
		configureAndStart(mLetterboxBottom, bottomAnimation, duration);
	}
	
	private void animateLeft(long duration) {
		// Utils.logFrame("left", mLetterboxLeft);
		float leftScale = (float) mLetterboxWidth / (float) mLetterboxLeft.getWidth();
		ScaleAnimation leftAnimation = new ScaleAnimation(1, leftScale, 1, 1, 0, 0);
		leftAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				mLetterboxLeft.clearAnimation();
				layoutLeft();
			}

			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationStart(Animation animation) {}}
		);
		configureAndStart(mLetterboxLeft, leftAnimation, duration);
	}
	
	private void animateRight(long duration) {
		// Utils.logFrame("right", mLetterboxRight);
		float leftScale = (float) mLetterboxWidth / (float) mLetterboxLeft.getWidth();
		ScaleAnimation rightAnimation = new ScaleAnimation(1, leftScale, 1, 1, mLetterboxRight.getWidth(), 0);
		rightAnimation.setAnimationListener(new AnimationListener() {

			public void onAnimationEnd(Animation animation) {
				mLetterboxRight.clearAnimation();
				layoutRight();
			}

			public void onAnimationRepeat(Animation animation) {}
			public void onAnimationStart(Animation animation) {}}
		);
		configureAndStart(mLetterboxRight, rightAnimation, duration);
	}
	/**
	 * Hides the letterbox.
	 * @param animated Not implemented yet.
	 */
	public void hide(boolean animated) {
		// TODO: Implement animation. Just changing the duration does not work when the user is scrolling.
		show(getWidth(), getHeight(), Color.TRANSPARENT, 0);
	}
	
	/**
	 * Shows a letterbox for the given area with the given color. The area is assumed to be centered.  	
	 * @param width Width of the area that the letterbox will surround.
	 * @param height Height of the area that the letterbox wil surround.
	 * @param color Color of the letterbox.
	 * @param duration Duration of the animation in miliseconds.
	 */
	public void show(int width, int height, int color, long duration) {
		final ColorDrawable initialColor = new ColorDrawable(mColor);
		final ColorDrawable finalColor = new ColorDrawable(color);
		final TransitionDrawable backgroundTransition = new TransitionDrawable(new Drawable[] {initialColor, finalColor});
		
		final int previousWidth = mLetterboxWidth;
		final int previousHeight = mLetterboxHeight;

		mLetterboxWidth = Math.max(1, (getWidth() - width) / 2);
		mLetterboxHeight = Math.max(1, (getHeight() - height) / 2);
		
		if (mLetterboxWidth <= 1 && previousWidth <= 1) {
			mLetterboxLeft.setBackgroundColor(Color.TRANSPARENT);
			mLetterboxRight.setBackgroundColor(Color.TRANSPARENT);
		} else {
			mLetterboxLeft.setBackgroundDrawable(backgroundTransition);
			mLetterboxRight.setBackgroundDrawable(backgroundTransition);
		}
		if (mLetterboxHeight <= 1 && previousHeight <= 1) {
			mLetterboxTop.setBackgroundColor(Color.TRANSPARENT);
			mLetterboxBottom.setBackgroundColor(Color.TRANSPARENT);
		} else {
			mLetterboxTop.setBackgroundDrawable(backgroundTransition);
			mLetterboxBottom.setBackgroundDrawable(backgroundTransition);
		}

		mColor = color;
		
		if (duration > 0) {
			animateTop(duration);
			animateBottom(duration);
			animateLeft(duration);
			animateRight(duration);
		} else {
			layoutTop();
			layoutBottom();
			layoutLeft();
			layoutRight();
		}
	}	
}
