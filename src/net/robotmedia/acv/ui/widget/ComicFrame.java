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

import java.util.HashSet;
import java.util.List;

import net.robotmedia.acv.comic.ACVComic;
import net.robotmedia.acv.comic.ACVContent;
import net.robotmedia.acv.ui.widget.SuperImageView.LayoutMeasures;
import net.robotmedia.acv.utils.IntentUtils;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;

@SuppressWarnings("deprecation")
public class ComicFrame extends FrameLayout {

	private Letterbox mLetterbox;
	private SuperImageView mImage;
	private HashSet<WebView> mContentViews = new HashSet<WebView>();
	private AbsoluteLayout mContentContainer;

	private void init(Context context) {		
		// FIXME: Do this programatically
		final int defStyle = context.getResources().getIdentifier("scrollViewStyle", "attr", context.getPackageName());
		mImage = new SuperImageView(context, null, defStyle);
		mImage.setScaleType(ImageView.ScaleType.CENTER);
		mImage.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.addView(mImage);

		mContentContainer = new AbsoluteLayout(context);
		mContentContainer.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		this.addView(mContentContainer);

		mLetterbox = new Letterbox(context);
		mLetterbox.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		this.addView(mLetterbox);

	}	

	public void removeContent() {
		for (WebView w : mContentViews) {
			mContentContainer.removeView(w);
		}
		mContentViews.clear();
	}

	public void showContent(ACVComic acv, int screenIndex, int frameIndex, boolean forward, LayoutMeasures imageMeasures) {
		this.removeContent();
		final List<ACVContent> contents = acv.getContents(screenIndex, frameIndex);
		final String baseURL = acv.getContentBaseURL();
		final Context context = getContext();
		for (final ACVContent content : contents) {
			final Rect rect = content.createRect(imageMeasures.width, imageMeasures.height);
			final WebView w = new WebView(context);
			final int x = rect.left - imageMeasures.scrollX + imageMeasures.left;
			final int y = rect.top - imageMeasures.scrollY + imageMeasures.top;
			w.setLayoutParams(new AbsoluteLayout.LayoutParams(rect.width(), rect.height(), x, y));
			w.setVerticalScrollBarEnabled(false);
			w.setHorizontalScrollBarEnabled(false);
			w.setBackgroundColor(Color.TRANSPARENT);
			w.setClickable(false); // TODO: Enable links
			w.setLongClickable(false);
			final String html = acv.getContentFromSource(content);
			w.loadDataWithBaseURL(baseURL, html, "text/html", "UTF-8", null);
			mContentContainer.addView(w);
			mContentViews.add(w);
			w.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageFinished(WebView view, String url) {
					final long transitionDuration = content.getTransitionDuration();
					if (transitionDuration > 0) {
						final Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
						animation.setDuration(transitionDuration);
						view.startAnimation(animation);
					}
				};

				@Override
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					IntentUtils.view(context, url);
					return true;
				}
			});
		}
	}

	public ComicFrame(Context context) {
		super(context);
		init(context);
	}

	public SuperImageView getImage() {
		return mImage;
	}

	public Letterbox getLetterbox() {
		return mLetterbox;
	}

}
