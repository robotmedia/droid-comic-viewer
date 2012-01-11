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

import net.robotmedia.acv.utils.MathUtils;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;

public class CaptionView extends RelativeLayout {

	private WebView mCaptionWebView;
	private Button mCaptionButton;
	
	public CaptionView(Context context) {
		super(context);
		init();
	}

	public CaptionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	
	private void init() {
		final int margin = MathUtils.dipToPixel(getContext(), 5);
		mCaptionButton = new Button(getContext());
		{
			final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.setMargins(0, 0, margin, margin);
			mCaptionButton.setLayoutParams(params);
		}
		mCaptionButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				showCaption();
			}
		});
		this.addView(mCaptionButton);

		mCaptionWebView = new WebView(getContext());
		{
			final RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
			mCaptionWebView.setLayoutParams(params);
		}
		mCaptionWebView.getSettings().setJavaScriptEnabled(true);
		final GestureDetector g = new GestureDetector(new OnGestureListener(){

			@Override
			public boolean onDown(MotionEvent arg0) {
				return false;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float arg2, float arg3) {
				if (e1.getY() < e2.getY()) { // Fling down
					if (mCaptionWebView.getScrollY() == 0) {
						hideCaption();
						return true;
					}
				}
				return false;
			}

			@Override
			public void onLongPress(MotionEvent arg0) {				
			}

			@Override
			public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
				return false;
			}

			@Override
			public void onShowPress(MotionEvent arg0) {
			}

			@Override
			public boolean onSingleTapUp(MotionEvent arg0) {
				hideCaption();
				return true;
			}});
		mCaptionWebView.setOnTouchListener(new View.OnTouchListener() {
				
				@Override
				public boolean onTouch(View arg0, MotionEvent arg1) {
					return g.onTouchEvent(arg1);
				}
		});		
		this.addView(mCaptionWebView);	
	}
	
	public void setCaptionButtonText(String text) {
		mCaptionButton.setText(text);
	}
	
	public void setCaption(String caption) {
		mCaptionWebView.loadDataWithBaseURL("file:///android_asset/", caption, "text/html", "utf-8", null);
	}
	
	public void hideCaption() {
		toggle(mCaptionWebView, false);
	}

	public void showCaption() {
		toggle(mCaptionWebView, true);
	}
	
	private void toggle(final View v, final boolean show) {
		TranslateAnimation animation = new TranslateAnimation(0, 0, show ? this.getHeight() : 0, show ? 0 : this.getHeight());
		animation.setInterpolator(new DecelerateInterpolator());
		animation.setDuration(500);
		animation.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation arg0) {
				v.setVisibility(show ? View.VISIBLE : View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {}

			@Override
			public void onAnimationStart(Animation arg0) {}});
		v.startAnimation(animation);	
	}

}
