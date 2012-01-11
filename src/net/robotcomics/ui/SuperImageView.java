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
package net.robotcomics.ui;

import net.robotcomics.acv.common.Constants;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;

/**
 * @author hermespique
 *
 */
public class SuperImageView extends ImageView {

	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private Rect mCurrentFrame;
	private float zoomFactor = -1;
	private SuperImageViewListener mCSVListener;
	private boolean mScaled = false;
	
	public void setCSVListener(SuperImageViewListener listener) {
		mCSVListener = listener;
	}
	
	public SuperImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}
	
	public SuperImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView();
	}

	private void initView() {
		mScroller = new Scroller(getContext());
	}

	public void abortScrollerAnimation() {
		if (!mScroller.isFinished()) {
			mScroller.abortAnimation();
		}
	}

	private FrameLayout.LayoutParams createLayoutParams(int width, int height) {
		int gravity = Gravity.NO_GRAVITY;
		if (width < getRootViewWidth()) {
			gravity = gravity | Gravity.CENTER_HORIZONTAL;
		}
		if (height < getRootViewHeight()) {
			gravity = gravity | Gravity.CENTER_VERTICAL;
		}
		final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height, gravity);
		return params;
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			// This is called at drawing time by ViewGroup. We don't want to
			// re-show the scrollbars at this point, which scrollTo will do,
			// so we replicate most of scrollTo here.
			//
			// It's a little odd to call onScrollChanged from inside the
			// drawing.
			//
			// It is, except when you remember that computeScroll() is used to
			// animate scrolling. So unless we want to defer the
			// onScrollChanged()
			// until the end of the animated scrolling, we don't really have a
			// choice here.
			//
			// I agree. The alternative, which I think would be worse, is to
			// post
			// something and tell the subclasses later. This is bad because
			// there
			// will be a window where mScrollX/Y is different from what the app
			// thinks it is.
			//
			int oldX = getScrollX();
			int oldY = getScrollY();
			int x = mScroller.getCurrX();
			int y = mScroller.getCurrY();
			// Log.d("computeScroll", "scrollX =" + String.valueOf(x) + "; scrollY = " + String.valueOf(y));
			super.scrollTo(x, y);

			if (oldX != x || oldY != y) {
				onScrollChanged(x, y, oldX, oldY);
				postInvalidate();
			}
		}
	}

	private int fitHeight(int height, boolean layout) {
		int newHeight = Math.round((float) getRootViewHeight() * ((float) getOriginalHeight() / (float) height));
		int newWidth = Math.round((float) getOriginalWidth() * (float) newHeight / (float) getOriginalHeight());
		if (layout) {
			setScaleType(ScaleType.FIT_CENTER);
			setLayoutParams(createLayoutParams(newWidth, newHeight));
		}
		return newWidth;
	}
	
	private int fitHeight() {
		int height = getOriginalHeight();
		return fitHeight(height, true);
	}

	private int fitWidth(int width, boolean layout) {
		int newWidth = Math.round((float) getRootViewWidth() * ((float) getOriginalWidth() / (float) width));
		if (layout) {
			int newHeight = Math.round((float) getOriginalHeight() * (float) newWidth / (float) getOriginalWidth());
			setScaleType(ScaleType.FIT_CENTER);
			setLayoutParams(createLayoutParams(newWidth, newHeight));
		}
		return newWidth;
	}
	
	private int fitWidth() {
		int width = getOriginalWidth();
		return fitWidth(width, true);
	}

	private int fitFrame() {
		setScaleType(ScaleType.CENTER_CROP); 
		int newWidth = mCurrentFrame.width();
		int newHeight = mCurrentFrame.height();
		setLayoutParams(createLayoutParams(newWidth, newHeight));
		return newWidth;
	}
	
	public boolean flingXY(int initialVelocityX, int initialVelocityY) {
		mScroller.fling(getScrollX(), getScrollY(), initialVelocityX, initialVelocityY, 0, getWidth() - getRootViewWidth(), 0, getHeight()
				- getRootViewHeight());
		invalidate();
		return true;
	}
	
	private int getInitialScrollX(int width) {
		SharedPreferences preferences = PreferenceManager
		.getDefaultSharedPreferences(getContext());
		String direction = preferences.getString(Constants.DIRECTION_KEY, Constants.DIRECTION_LEFT_TO_RIGHT_VALUE);
		if (Constants.DIRECTION_LEFT_TO_RIGHT_VALUE.equals(direction)) {
			return 0;
		} else {
			return Math.max(0, width - getRootViewWidth());
		}		
	}

	private int getRootViewHeight() {
		return getRootView().getHeight();
	}

	private int getRootViewWidth() {
		return getRootView().getWidth();
	}
	
	public int getOriginalWidth() {
		Drawable image = getDrawable();
		BitmapDrawable bitmapDrawable;
		if (image instanceof BitmapDrawable && (bitmapDrawable = (BitmapDrawable) image).getBitmap() != null) {
			return bitmapDrawable.getBitmap().getWidth();
		} else if (image != null) {
			// Should ComicScreenView work with anything else than bitmap drawables?
			return image.getIntrinsicWidth();
		} else {
			return 0;
		}
	}

	public Rect getOriginalSize() {
		final Rect frame = new Rect(0, 0, getOriginalWidth(), getOriginalHeight());
		return frame;
	}
	
	public int getOriginalHeight() {
		Drawable image = getDrawable();
		BitmapDrawable bitmapDrawable;
		if (image instanceof BitmapDrawable && (bitmapDrawable = (BitmapDrawable) image).getBitmap() != null) {
			return bitmapDrawable.getBitmap().getHeight();
		} else if (image != null) {
			// Should ComicScreenView work with anything else than bitmap drawables?
			return image.getIntrinsicHeight();
		} else {
			return 0;
		}
	}
	
	private float getMaxWidth() {
		return getOriginalWidth() * Constants.MAX_ZOOM_FACTOR;	
	}
	
	private float getMaxHeight() {
		return getOriginalHeight() * Constants.MAX_ZOOM_FACTOR;		
	}
	
	public float getZoomFactor() {
		return zoomFactor;
	}	


	private boolean isBiggerThanAllowed(int width, int height) {
		return width > getMaxWidth() || height > getMaxHeight();
	}

	private boolean isSmallerThanAllowed(int width, int height) {
		int originalWidth = getOriginalWidth();
		int originalHeight = getOriginalHeight();
		int rootViewWidth = getRootViewWidth();
		int rootViewHeight = getRootViewHeight();
		if (originalWidth < rootViewWidth && originalHeight < rootViewHeight) {
			return width < originalWidth || height < originalHeight;			
		} else {
			return width < rootViewWidth && height < rootViewHeight;
		}
	}
	
	public boolean isLeftMost() {
		return getScrollX() <= 0;
	}

	public boolean isRightMost() {
		return getScrollX() >= getWidth() - getRootViewWidth(); 
	}
	
	public boolean isTopMost() {
		return getScrollY() <= 0;
	}

	public boolean isBottomMost() {
		return getScrollY() >= getHeight() - getRootViewHeight(); 
	}

	
	public boolean isSmallerThanRootView() {
		return isSmallerThanRootView(getWidth(), getHeight());
	}

	private boolean isSmallerThanRootView(int width, int height) {
		return width <= getRootViewWidth() && height <= getRootViewHeight();
	}

	private int scaleNone() {
		int width = getOriginalWidth();
		int height = getOriginalHeight();
		setScaleType(ScaleType.FIT_CENTER); 
		setLayoutParams(createLayoutParams(width, height));
		return width;
	}

	private void recalculateScroll(float ratio, int newWidth, int newHeight) {
		int scrollX = Math.round((float) getScrollX() * ratio);
		int scrollY = Math.round((float) getScrollY() * ratio);
		// TODO: Explain this formula
		scrollX += (newWidth - getWidth()) * getRootViewWidth() / (2 * getWidth());
		scrollY += (newHeight - getHeight()) * getRootViewHeight() / (2 * getHeight());
		this.safeScrollTo(scrollX, scrollY, newWidth, newHeight);
	}	

	private Point calculateSafeScroll(int scrollX, int scrollY, int width, int height) {
		scrollX = Math.min(width - getRootViewWidth(), scrollX);
		scrollY  = Math.min(height - getRootViewHeight(), scrollY);
		scrollX = Math.max(0, scrollX);
		scrollY  = Math.max(0, scrollY);
		return new Point(scrollX, scrollY);
	}
	
	private void safeScrollTo(int scrollX, int scrollY, int width, int height) {
		Point scroll = calculateSafeScroll(scrollX, scrollY, width, height);
		scrollTo(scroll.x, scroll.y);
	}
	
	private void safeScrollBy(int distanceX, int distanceY, int width, int height) {
		int scrollX = getScrollX() + distanceX;
		int scrollY = getScrollY() + distanceY;
		safeScrollTo(scrollX, scrollY, width, height);
	}
	
	public void safeScrollBy(int distanceX, int distanceY) {
		safeScrollBy(distanceX, distanceY, getWidth(), getHeight());
	}	

	public void scale(String scaleMode, boolean recalculateScroll) {
		abortScrollerAnimation();
		int newWidth;
		if (Constants.SCALE_MODE_BEST_VALUE.equals(scaleMode)) {
			newWidth = scaleFit();
		} else if (Constants.SCALE_MODE_WIDTH_VALUE.equals(scaleMode)) {
			newWidth = fitWidth();
		} else if (Constants.SCALE_MODE_HEIGHT_VALUE.equals(scaleMode)) {
			newWidth = fitHeight();
		} else if (Constants.SCALE_MODE_FRAME_VALUE.equals(scaleMode)) {
			newWidth = fitFrame();
		} else {
			newWidth = scaleNone();
		}
		int oldWidth = getWidth();
		int oldHeight = getHeight();
		if (recalculateScroll && oldWidth > 0 && oldHeight > 0) {
			float ratio = (float) newWidth / (float) oldWidth;
			int newHeight = Math.round(ratio * oldHeight);
			recalculateScroll(ratio, newWidth, newHeight);				
		} else { // First scroll
			scrollTo(getInitialScrollX(newWidth), 0);
		}

		zoomFactor = (float) newWidth / getOriginalWidth();
		mScaled = true;
	}

	/**
	 * Scrolls to the given frame of the image.
	 * @param frame Frame of the image to scroll to.
	 * @param keepInside 
	 * @return Frame Frame of the view to where it was scrolled.
	 */
	public Rect scrollTo(Rect frame, boolean keepInside) {
		abortScrollerAnimation();
		int newWidth = scaleFit(frame.width(), frame.height(), true);
		Rect newFrame = resize(frame, newWidth, keepInside);
		scrollTo(newFrame.left, newFrame.top);
		zoomFactor = (float) newWidth / getOriginalWidth();
		mScaled = false;
		return newFrame;
	}
	
	public class LayoutMeasures {
		public int width;
		public int height;
		public int scrollX;
		public int scrollY;
		public int top;
		public int left;
	}
	
	public LayoutMeasures calculateLayout(Rect frame, boolean keepInside) {
		final LayoutMeasures result = new LayoutMeasures();
		result.width = scaleFit(frame.width(), frame.height(), false);
		result.height = Math.round((float) getOriginalHeight() * (float) result.width / (float) getOriginalWidth());
		Rect newFrame = resize(frame, result.width, keepInside);
		result.scrollX = newFrame.left;
		result.scrollY = newFrame.top;
		final int rootWidth = getRootViewWidth();
		if (result.width < rootWidth) {
			result.left = (rootWidth - result.width) / 2;
		}
		final int rootHeight = getRootViewHeight();
		if (result.height < rootHeight) {
			result.top = (rootHeight - result.height) / 2;
		}
		return result;		
	}
		
	/**
	 * Recalculates the given frame of the original image considering the given width as the width of the image.
	 * Also centers the new frame in the parent view
	 * @param frame Frame of the original image.
	 * @param newWidth Width of the image.
	 * @param keepInside
	 * @return
	 */
	private Rect resize(Rect frame, int newWidth, boolean keepInside) {
		// Recalculate frame based on new width
		final Rect newFrame = new Rect();
		float scale = (float) newWidth / (float) getOriginalWidth();
		newFrame.left = Math.round(frame.left * scale);
		newFrame.top = Math.round(frame.top * scale);
		newFrame.right = Math.round(frame.right * scale);
		newFrame.bottom = Math.round(frame.bottom * scale);
		
		int dx = - (Math.min(newWidth, getRootViewWidth()) - newFrame.width()) / 2;
		final int newHeight = Math.round(getOriginalHeight() * scale);
		int dy = - (Math.min(newHeight, getRootViewHeight()) - newFrame.height()) / 2;
		newFrame.offset(dx, dy);
		
		if (keepInside) { // No letterbox
			Point scroll = calculateSafeScroll(newFrame.left, newFrame.top, newWidth, newHeight);
			newFrame.offsetTo(scroll.x, scroll.y);
		}
		return newFrame;
	}

	private boolean mAnimating = false;
	
	public boolean isAnimating() {
		return mAnimating;
	}
	
	public boolean isScaled() {
		return mScaled;
	}
	
	public Rect animateTo(final Rect frame, final boolean keepInside, long duration) {
		mAnimating = true;
		abortScrollerAnimation();
		final int newWidth = scaleFit(frame.width(), frame.height(), false);
		final Rect newFrame = resize(frame, newWidth, keepInside);
		final AnimationSet animation = createAnimation(newWidth, newFrame.left, newFrame.top, duration);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationEnd(Animation arg0) {

				SuperImageView.this.postDelayed(new Runnable() {
					@Override
					public void run() {
						SuperImageView.this.scrollTo(frame, keepInside);						
						SuperImageView.this.clearAnimation();
						mAnimating = false;
						if (mCSVListener != null) {
							mCSVListener.onAnimationEnd(SuperImageView.this);
						}
					}}, 50);
			}
			
			@Override public void onAnimationRepeat(Animation arg0) {}
			@Override public void onAnimationStart(Animation arg0) {}
		});
		this.startAnimation(animation);
		return newFrame;
	}

	private AnimationSet createAnimation(final int newWidth, final int newScrollX, final int newScrollY, long duration) {
		final AnimationSet animation = new AnimationSet(true);
		animation.setFillAfter(true);
		animation.setInterpolator(new DecelerateInterpolator());

		final float scale = (float) newWidth / (float) getWidth();

		
		final int fromXDelta = getScrollX();
		final int fromYDelta = getScrollY();
		this.scrollTo(0, 0);
		int toXDelta = Math.round((float) newScrollX / scale);
		int toYDelta = Math.round((float) newScrollY / scale);
		
		// Because the imageSwitcher centers the view if smaller
		final int fromXCenteringDelta = Math.max(getRootViewWidth() -  getWidth(), 0) / 2;
		final int toXCenteringDelta = Math.max(getRootViewWidth() - newWidth, 0) / 2;
		final int xCenteringDelta = Math.round((toXCenteringDelta - fromXCenteringDelta) / scale);
		toXDelta -= xCenteringDelta;

		final int newHeight = Math.round((float) getOriginalHeight() * (float) newWidth / (float) getOriginalWidth());
		final int fromYCenteringDelta = Math.max(getRootViewHeight() -  getHeight(), 0) / 2;
		final int toYCenteringDelta = Math.max(getRootViewHeight() - newHeight, 0) / 2;
		final int yCenteringDelta = Math.round((toYCenteringDelta - fromYCenteringDelta) / scale);
		toYDelta -= yCenteringDelta;
				
		final TranslateAnimation translateAnimation = new TranslateAnimation(-fromXDelta, -toXDelta, -fromYDelta, -toYDelta);
		translateAnimation.setDuration(duration);

		animation.addAnimation(translateAnimation);

		final ScaleAnimation scaleAnimation = new ScaleAnimation(1, scale, 1, scale);
		scaleAnimation.setDuration(duration);
		animation.addAnimation(scaleAnimation);
		return animation;
	}
	
	private int scaleFit(int width, int height, boolean layout) {
		if (width < height) { // Portrait
			float ratio = (float) width / (float) height;
			float containerRatio = (float) getRootViewWidth() / (float) getRootViewHeight();
			if (ratio < containerRatio) {
				return fitHeight(height, layout);
			} else {
				return fitWidth(width, layout);
			}
		} else { // Landscape
			float ratio = (float) height / (float) width;
			float containerRatio = (float) getRootViewHeight() / (float) getRootViewWidth();
			if (ratio < containerRatio) {
				return fitWidth(width, layout);
			} else {
				return fitHeight(height, layout);
			}
		}
	}
	
	private int scaleFit() {
		int width = getOriginalWidth();
		int height = getOriginalHeight();
		return scaleFit(width, height, true);
	}

	public void smoothScroll(MotionEvent motionEvent) {
		// Log.d("smoothScroll", "enter");
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(motionEvent);

		final int action = motionEvent.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			abortScrollerAnimation();
			break;
		case MotionEvent.ACTION_UP:
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000);
			int initialVelocityX = (int) velocityTracker.getXVelocity();
			int initialVelocityY = (int) velocityTracker.getYVelocity();
			int initialFilteredVelocityX = 0;
			int initialFilteredVelocityY = 0;

			if (Math.abs(initialVelocityX) > ViewConfiguration.getMinimumFlingVelocity()) {
				initialFilteredVelocityX = -initialVelocityX;
			}

			if (Math.abs(initialVelocityY) > ViewConfiguration.getMinimumFlingVelocity()) {
				initialFilteredVelocityY = -initialVelocityY;
			}

			if (initialFilteredVelocityX != 0 || initialFilteredVelocityY != 0) {
				flingXY(initialFilteredVelocityX, initialFilteredVelocityY);
			}

			if (mVelocityTracker != null) {
				mVelocityTracker.recycle();
				mVelocityTracker = null;
			}
			break;
		}

	}
	
	/**
	 * Scales the view by the given factor without assuming anything of the size of the view. 
	 * Scaling is performed based on the original size of the drawable.
	 * Like all other scale methods, scroll is positioned in the initial position for reading.
	 * @param factor
	 */
	public void scaleByFactor(float factor) {
		int newWidth = Math.round(getOriginalWidth() * factor);
		int newHeight = Math.round(getOriginalHeight() * factor);
		if (isBiggerThanAllowed(newWidth, newHeight)) {
			newWidth = Math.round(getMaxWidth());
			newHeight = Math.round(getMaxHeight());
			factor = (float) newWidth / (float) getWidth();
			setScaleType(ScaleType.FIT_CENTER);
			setLayoutParams(createLayoutParams(newWidth, newHeight));
			zoomFactor = Constants.MAX_ZOOM_FACTOR;
		} else if (isSmallerThanAllowed(newWidth, newHeight)) {
			if (isSmallerThanRootView(getOriginalWidth(), getOriginalHeight())) {
				scaleNone();
				zoomFactor = 1;
			} else {
				newWidth = scaleFit();
				zoomFactor = (float) newWidth / getOriginalWidth();
			}
		} else {
			setScaleType(ScaleType.FIT_CENTER);
			setLayoutParams(createLayoutParams(newWidth, newHeight));
			zoomFactor = (float) newWidth / getOriginalWidth();
		}
		scrollTo(getInitialScrollX(newWidth), 0);
		mScaled = false;
	}
	
	public void zoom(int increment, Point viewPoint) {
		float factor = (float)Math.pow(Constants.ZOOM_STEP, increment);
		Point imagePoint = viewPoint != null ? toImagePoint(viewPoint) : null;
		zoom(factor, imagePoint);
	}

	public void zoom(float factor, Point imagePoint) {
		abortScrollerAnimation();
		if (imagePoint != null) { // Scroll to point before zooming
			final float scale = (float) getWidth() / (float) getOriginalWidth();
			int x = Math.round(imagePoint.x * scale);
			int y = Math.round(imagePoint.y * scale);
			x -= Math.round((float) Math.min(getWidth(), getRootViewWidth()) / 2f);
			y -= Math.round((float) Math.min(getHeight(), getRootViewHeight()) / 2f);
			this.scrollTo(x, y);
		}
		int newWidth = Math.round(getWidth() * factor);
		int newHeight = Math.round(getHeight() * factor);
		if (isBiggerThanAllowed(newWidth, newHeight)) {
			newWidth = Math.round(getMaxWidth());
			newHeight = Math.round(getMaxHeight());
			factor = (float) newWidth / (float) getWidth();
			setScaleType(ScaleType.FIT_CENTER);
			setLayoutParams(createLayoutParams(newWidth, newHeight));
			zoomFactor = Constants.MAX_ZOOM_FACTOR;
			recalculateScroll(factor, newWidth, newHeight);		
		} else if (isSmallerThanAllowed(newWidth, newHeight)) {
			if (isSmallerThanRootView(getOriginalWidth(), getOriginalHeight())) {
				scaleNone();
				zoomFactor = 1;
			} else {
				newWidth = scaleFit();
				zoomFactor = (float) newWidth / getOriginalWidth();
			}
			scrollTo(0, 0);
		} else {
			setScaleType(ScaleType.FIT_CENTER);
			setLayoutParams(createLayoutParams(newWidth, newHeight));
			zoomFactor = (float) newWidth / getOriginalWidth();
			recalculateScroll(factor, newWidth, newHeight);		
		}
		mScaled = false;
	}
	
	public void recycleBitmap() {
		Drawable drawable = getDrawable();
		if (drawable != null && drawable instanceof BitmapDrawable) {
			Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
			bitmap.recycle();
			// FIXME: Is this the best way to say the view should show nothing?
			setImageDrawable(new ColorDrawable());
			setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
	}

	public Point toImagePoint(Point viewPoint) {
		Point imagePoint = new Point();
		int marginX = Math.max(0, getRootViewWidth() - getWidth()) / 2;
		int marginY = Math.max(0, getRootViewHeight() - getHeight()) / 2;
		imagePoint.x = getScrollX() + viewPoint.x - marginX;
		imagePoint.y = getScrollY() + viewPoint.y - marginY;
		imagePoint.x = Math.min(getWidth(), imagePoint.x);
		imagePoint.x = Math.max(0, imagePoint.x);
		imagePoint.y = Math.min(getHeight(), imagePoint.y);
		imagePoint.y = Math.max(0, imagePoint.y);
		float scale = (float) getOriginalWidth() / (float)getWidth();
		imagePoint.x = Math.round(imagePoint.x * scale);
		imagePoint.y = Math.round(imagePoint.y * scale);
		return imagePoint;

	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			this.abortScrollerAnimation();
		}
		return super.onTouchEvent(event);
	}
	
}
