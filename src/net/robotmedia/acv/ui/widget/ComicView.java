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

import java.io.File;
import java.util.HashMap;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageSwitcher;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.ViewSwitcher;
import net.androidcomics.acv.R;
import net.robotmedia.acv.Constants;
import net.robotmedia.acv.comic.ACVComic;
import net.robotmedia.acv.comic.Comic;
import net.robotmedia.acv.logic.PreferencesController;
import net.robotmedia.acv.logic.ServiceManager;
import net.robotmedia.acv.ui.widget.SuperImageView.LayoutMeasures;
import net.robotmedia.acv.utils.IntentUtils;
import net.robotmedia.acv.utils.MathUtils;

public class ComicView extends RelativeLayout implements OnCompletionListener, OnErrorListener  {

	protected void initializeWithResources(Context context) {
	    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    inflater.inflate(R.layout.comic_view, this);
	    
		animationFadeIn = R.anim.fade_in;
		animationFadeOut = R.anim.fade_out;
		animationKeep = R.anim.keep;
		animationPushDownIn = R.anim.push_down_in;
		animationPushDownOut = R.anim.push_down_out;
		animationPushLeftIn = R.anim.push_left_in;
		animationPushLeftOut = R.anim.push_left_out;
		animationPushRightIn = R.anim.push_right_in;
		animationPushRightOut = R.anim.push_right_out;
		animationPushUpIn = R.anim.push_up_in;
		animationPushUpOut = R.anim.push_up_out;
		messageButton = (Button) findViewById(R.id.message_button);
		mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
		mVideoView = (VideoView) findViewById(R.id.video_view);
		stringScreenProgressMessage = R.string.dialog_page_progress_text;
		stringScreenProgressTitle = R.string.dialog_page_progress_title;
		stringUnavailableText = R.string.dialog_unavailable_text;
		stringUnavailableTitle = R.string.dialog_unavailable_title;
	}

	protected boolean isLeftToRight() {
		return new PreferencesController(getContext()).isLeftToRight();
	}

	private class SwitchImageTask extends AsyncTask<Object, Object, Drawable> {

		private boolean forward;
		private boolean sequential;
		
		private ProgressDialog progressDialog;
		final Runnable mNotifyLoadScreenRunning = new Runnable() {
			public void run() {
				if (progressDialogRequired && !mDestroyed) {
					progressDialog = new ProgressDialog(getContext());
					progressDialog.setTitle(stringScreenProgressTitle);
					progressDialog.setIcon(android.R.drawable.ic_menu_info_details);
					progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
					if (SwitchImageTask.this.isImageSwitch()) {
						progressDialog.setMessage(getContext().getString(stringScreenProgressMessage).replace("@number", String.valueOf(imageIndex + 1)));
					} else {
						// TODO: Localize text
						progressDialog.setMessage("Loading...");						
					}
					progressDialog.show();
				}
			}
		};
		
		private final static int IMAGE_SWITCH = -1;
		
		private int imageIndex;
		private int frameIndex = IMAGE_SWITCH; // Screen change by default
		
	    boolean progressDialogRequired;
		
	    private int getFrameIndex() {
	    	if (this.isImageSwitch()) { // Screen change
	    		return sequential && !forward ? comic.getFramesSize(position) - 1 : 0;
	    	} else { // Frame change with transition
	    		return frameIndex;
	    	}
	    }
	    
	    private boolean isImageSwitch() {
	    	return frameIndex == IMAGE_SWITCH;
	    }
	    
		private void postScreenChangedActions() {
			showScreenNumber();
			prepareMessageButton();
			prepareCaption();
			
			boolean stopAnimation = true;

			if (comic instanceof ACVComic) {
				final ACVComic acv = (ACVComic) comic;
				if (mustConsiderFrames()) {
					stopAnimation = false; // The frame will stop the animation if necessary
					final int frameIndex = this.getFrameIndex();
					setFrame(frameIndex, forward, false);
				} else {
					stopAnimation = true;
					final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
					current.getLetterbox().hide(false); // In case we come from a screen with frames
				}
				
				if (forward) {
					final boolean autoplay = ((ACVComic) comic).isAutoplay(position);
					if (autoplay || mAutoplay) {
						stopAnimation = false;
						long duration = ((ACVComic) comic).getDuration(position);
						startAnimating();
						mAutoplayRunnable = new Runnable() {
							@Override
							public void run() {
							 if (position + 1 < comic.getLength()) {
								moveForwardAfterVideo = true;
								setPosition(position + 1, true, true);
							 }
							}
						};
						ComicView.this.postDelayed(mAutoplayRunnable, duration);
					}
				}
				
				if (acv.hasVibration(position)) {
					vibrate(stopAnimation);
					stopAnimation = false; // The vibration or frame will stop the animation
				}
				
				File sound = acv.getSound(position);
				if (sound != null) {
					playSound(sound);
				}
			}
			
			if (stopAnimation && !mVideoPlaying) {
				stopAnimating();
			}
			
			if (mListener != null) {
				mListener.onScreenChanged(position);
			}
		}
		
		@Override
		protected Drawable doInBackground(Object... params) {
			System.gc();
			try {
				if (comic instanceof ACVComic) {
					final ACVComic acv = (ACVComic) comic;
					if (acv.getVideoFile(imageIndex) != null) {
						if (forward && imageIndex + 1 < comic.getLength()) {
							return comic.getScreen(imageIndex + 1);
						}
						if (!forward && imageIndex > 0) {
							return comic.getScreen(imageIndex - 1);
						}
					} else {
						return acv.getScreenWithContents(mHTMLRenderer, imageIndex);
					}
				}
				return comic.getScreen(imageIndex);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
				return null;
			}
		}
		
		@Override
		protected void onPostExecute(Drawable result) {
			if (result != null && result instanceof BitmapDrawable) {
				Log.d("Image width", String.valueOf(((BitmapDrawable) result).getBitmap().getWidth()));
				Log.d("Image height", String.valueOf(((BitmapDrawable) result).getBitmap().getHeight()));
			}
			loadingPage = false;
			progressDialogRequired = false;
			if (progressDialog != null) progressDialog.dismiss();
			// if (!viewer.destroyed) {
	    		if (result != null) {
	    			
	    			previousPosition = position;
	    			position = imageIndex;
	    			framePosition = -1; 
	    			
	    			if (comic instanceof ACVComic) {
	    				File videoFile = ((ACVComic) comic).getVideoFile(position);
	    				if (videoFile != null) {
		    				mVideoView.setVisibility(View.VISIBLE);
		    				mSwitcher.setVisibility(View.INVISIBLE);
		    				mVideoView.setVideoPath(videoFile.getAbsolutePath());
		    				mVideoView.start();
		    				mVideoView.requestFocus();
		    				mSwitcher.setInAnimation(null);
		    				mSwitcher.setOutAnimation(null);
		    				mVideoPlaying  = true;
		    				startAnimating();
	    				}
	    			}

	    			final ComicFrame current = (ComicFrame) mSwitcher.getNextView();
	    			current.getImage().setImageDrawable(result);
	    			mSwitcher.showNext();
	    			nextImageUp = !nextImageUp; // HACK
	    			
	    			Integer backgroundColor = comic.getBackgroundColor(position);
	    			if (backgroundColor == null) {
	    				backgroundColor = Color.BLACK;
	    			}
	    			
	    			setBackgroundColor(backgroundColor); 
	    			mSwitcher.setBackgroundColor(backgroundColor);
	    			// TODO: Check if it's necessary to set both background colors
	    			scale(current.getImage());
	    			
	    			Animation animation = mSwitcher.getInAnimation();	    			
	    			if (animation == null) {
	    				animation = mSwitcher.getOutAnimation();			
	    			};
	    			
	    			if (animation != null) {
	    				if (mustConsiderFrames()) { // Animate to the first frame
	    					final ACVComic acv = (ACVComic) comic;
	    					final int frameIndex = this.getFrameIndex();
	    					final int width = current.getImage().getOriginalWidth();
	    					final int height = current.getImage().getOriginalHeight();
	    					final Rect frame = acv.rectForSize(position, frameIndex, width, height);
	    					final Integer color = acv.getBackgroundColor(position, frameIndex);
	    					boolean letterbox = color != null;
	    					if (!this.isImageSwitch()) {
	    						ComicView.this.framePosition = frameIndex; // Need to set this because we're bypassing setFrame
	    					}
	    					
    						final Rect newFrame = current.getImage().scrollTo(frame, !letterbox);
							final LayoutMeasures imageMeasures = current.getImage().calculateLayout(frame, !letterbox);
    						current.showContent(acv, position, frameIndex, forward, imageMeasures);
    						if (letterbox) {
    							current.getLetterbox().show(newFrame.width(), newFrame.height(), color, 0);
    						} else {
    							current.getLetterbox().hide(false);
    						}    						
	    				}
	    				
	    				animation.setAnimationListener(new AnimationListener() {
							@Override
							public void onAnimationEnd(Animation arg0) {
								if (SwitchImageTask.this.isImageSwitch()) {
									postScreenChangedActions();
								} else {
									ComicView.this.postFrameDisplayedActions(forward);
								}
							}
							@Override public void onAnimationRepeat(Animation arg0) {}
							@Override public void onAnimationStart(Animation arg0) { startAnimating(); }
	    				});
	    			} else {
	    				postScreenChangedActions();
	    			}
	    			
	    			if (isImageSwitch()) {
		    			int cachePosition;
		    			if (previousPosition == position + 1) {
		    				// The next screen is already prepared, so we attempt to prepare the previous screen.
		    				cachePosition = position - 1;
		    			} else {
		    				cachePosition = position + 1;
		    			}
		    			if (preload) {
			    			mPrepareScreenTask = new PrepareScreenTask(comic);
			    			mPrepareScreenTask.execute(cachePosition);
		    			}
	    			}
	    		} else {
	    			if (mListener != null) {
	    				mListener.onScreenLoadFailed();
	    			}
	    		}
		}

		/**
		 * Scales the current image based on (in order of priority): the comic scale mode, the zoom of the previous image or the scale mode preference. 
		 * @param current
		 */
		private void scale(SuperImageView current) {
			String scaleMode = comic.getScaleMode();
			// Legacy code
			if (comic instanceof ACVComic && Constants.SCALE_MODE_NONE_VALUE.equals(scaleMode)) {
				int width = current.getOriginalWidth();
				int height = current.getOriginalHeight();
				if (MathUtils.isEqual(width, 480, 2) && MathUtils.isEqual(height, 320, 2)) {
					scaleMode = Constants.SCALE_MODE_BEST_VALUE;
				}
			}
			
			if (scaleMode == null) {
				final ComicFrame previous = (ComicFrame) mSwitcher.getCurrentView();
				if (previous != null && !previous.getImage().isScaled() && previous.getImage().getZoomFactor() > 0) { // If a previous screen view exists with a valid zoom factor not obtained by scaling, use it
					current.scaleByFactor(previous.getImage().getZoomFactor());
					return;
				} else { // Scale mode preference
					scaleMode = preferences.getString(Constants.SCALE_MODE_KEY, Constants.SCALE_MODE_NONE_VALUE);
				}
			}
			current.scale(scaleMode, false);
		}
		
		@Override
		protected void onPreExecute() {
			System.gc();
			loadingPage = true;
			progressDialogRequired = true;
			ComicView.this.postDelayed(mNotifyLoadScreenRunning, 1000);
			final ComicFrame next = (ComicFrame) mSwitcher.getNextView();
			if (next != null) next.getImage().recycleBitmap();
			
			if (lowMemoryTransitions) {
				final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
				if (current != null) {
					if (position != imageIndex) { // No need to recycle the bitmap if the image is the same.
						current.getImage().recycleBitmap();
					}
				}
			}
		}
	
	}
	
	private MediaPlayer soundPlayer = null;
	private HashMap<File, Integer> soundHistory = new HashMap<File, Integer>();
	
	private synchronized void playSound(final File sound) {
		if (soundPlayer == null || !soundPlayer.isPlaying()) {
			if (!soundHistory.containsKey(sound)) {
				soundHistory.put(sound, 1);
				soundPlayer = new MediaPlayer();
				if (soundPlayer != null) {
					try {
						soundPlayer.setDataSource(sound.getAbsolutePath());
						soundPlayer.prepare();
						soundPlayer.start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public void setPreload(boolean preload) {
		this.preload = preload;
	}

	public void setLowMemoryTransitions(boolean lowMemoryTransitions) {
		this.lowMemoryTransitions = lowMemoryTransitions;
	}

	private boolean preload = true;
	private boolean lowMemoryTransitions = false;

	// TODO: Does not belong to this class
	private class GetNativeUrlTask extends AsyncTask<String, Object, String> {

		@Override
		protected String doInBackground(String... params) {
			String comicId = params[0];
			String result = ServiceManager.getNativeURL(comicId);
			return result;
		}
		
		protected void onPostExecute (String result) {
			messageButton.setEnabled(true);
			if (result != null) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse(result));
				getContext().startActivity(intent);
			} else {
				showOfferSubscribeDialog();
			}
		}
		
		@Override
		protected void onPreExecute () {
			messageButton.setEnabled(false);
		}
	}
	private class PrepareScreenTask extends AsyncTask<Integer, Object, Object> {

		private Comic comic;
		
		public PrepareScreenTask(Comic comic) {
			this.comic = comic;
		}
		
		@Override
		protected Object doInBackground(Integer... params) {
			try {
				comic.prepareScreen(params[0]);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
			return null;
		}
		
	}
	
	private PrepareScreenTask mPrepareScreenTask;
	private SwitchImageTask mImageSwitchTask;
	private GetNativeUrlTask mGetNativeUrlTask;
	
	private boolean mAutoplay;
	private Comic comic;
	private int framePosition;
	private boolean isBottomMost;
	private boolean isLeftMost;
	private boolean isRightMost;
	private boolean isTopMost;
	private boolean loadingPage = false;
	private boolean mAnimating = false;
	private boolean mDestroyed = false;
	private ComicViewListener mListener;
	private boolean moveForwardAfterVideo;
	private boolean mVideoPlaying = false;
	private boolean nextImageUp; // HACK
	private int position;
	private SharedPreferences preferences;
	private int previousPosition;
	private Toast toast = null;
	protected int animationFadeIn;
	protected int animationFadeOut;
	protected int animationKeep;
	protected int animationPushDownIn;
	protected int animationPushDownOut;
	protected int animationPushLeftIn;
	protected int animationPushLeftOut;
	protected int animationPushRightIn;
	protected int animationPushRightOut;
	protected int animationPushUpIn;
	protected int animationPushUpOut;
	protected Button messageButton;
	protected ViewSwitcher mSwitcher;
	protected VideoView mVideoView;
	protected CaptionView mCaption;
	protected int stringScreenProgressMessage;
	protected int stringScreenProgressTitle;
	protected int stringUnavailableText;
	protected int stringUnavailableTitle;
	protected WebView mHTMLRenderer;

	public void setAutoplay(boolean autoplay) {
		if (!mAutoplay && autoplay) {
			mAutoplay = true;
			this.goToCurrent();
		} else if (mAutoplay && !autoplay) {
			mAutoplay = false;
			this.removeCallbacks(mAutoplayRunnable);
			this.stopAnimating();
		}
	}
	
	public ComicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		nextImageUp = true;
		mHTMLRenderer = new WebView(context);
		mHTMLRenderer.setVisibility(View.INVISIBLE);
		mHTMLRenderer.setBackgroundColor(Color.TRANSPARENT);
		mHTMLRenderer.setVerticalScrollBarEnabled(false);
		mHTMLRenderer.setHorizontalScrollBarEnabled(false);
		this.addView(mHTMLRenderer);
		initializeWithResources(context);
		
		preferences = PreferenceManager.getDefaultSharedPreferences(context);

		mSwitcher.setFactory(new ViewSwitcher.ViewFactory() {

			public View makeView() {
				final ComicFrame view = new ComicFrame(getContext());
				view.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				// SuperImageView i = new SuperImageView(getContext(), null, attributeScrollViewStyle);
				// i.setScaleType(ImageView.ScaleType.CENTER);
				// FIXME: Why does the following show the image vertically
				// centered? See onLoadPageSuccess for workaround.
				// i.setLayoutParams(new
				// FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				// ViewGroup.LayoutParams.WRAP_CONTENT));
				return view;
			}

		});
		
		messageButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (comic instanceof ACVComic) {
					ACVComic.Message message = ((ACVComic) comic).getMessage(position);
					if (message != null) {
						if (message.uri.startsWith(ACVComic.COMIC_URI_PREFIX)) {
							String comicId = message.uri.substring(ACVComic.COMIC_URI_PREFIX.length());
							mGetNativeUrlTask = new GetNativeUrlTask();
							mGetNativeUrlTask.execute(comicId);
						} else {
							IntentUtils.openURI(getContext(), message.uri, message.nonMarketUri);
						}
					}					
				}
			}
		});
		mVideoView.setOnCompletionListener(this);
		mVideoView.setOnErrorListener(this);
		
	}

	public boolean actualSize() {
		return fit(Constants.SCALE_MODE_NONE_VALUE);
	}
	
	public void destroy() {
		this.recycleBitmaps();
		mDestroyed = true;
		if (mImageSwitchTask != null) {
			mImageSwitchTask.cancel(true);
			mImageSwitchTask = null;
		}
		if (mPrepareScreenTask != null) {
			mPrepareScreenTask.cancel(true);
			mPrepareScreenTask = null;
		}
		if (mGetNativeUrlTask != null) {
			mGetNativeUrlTask.cancel(true);
			mGetNativeUrlTask = null;
		}
		if (soundPlayer != null && soundPlayer.isPlaying()) {
			soundPlayer.stop();
		}
	}
	
	public boolean fitHeight() {
		return fit(Constants.SCALE_MODE_HEIGHT_VALUE);
	}
	
	public boolean fitScreen() {
		return fit(Constants.SCALE_MODE_BEST_VALUE);
	}
	
	public boolean fitWidth() {
		return fit(Constants.SCALE_MODE_WIDTH_VALUE);
	}
	
	public int getFrameIndex() {
		return framePosition;
	}

	public int getIndex() {
		return position;
	}
	
	public boolean goToScreen(int index) {
		if (!isAnimating()) {
			if (index < 0 || index >= comic.getLength()) {
				index = 0;
			}
			moveForwardAfterVideo = true;
			setPosition(index, index > position, false);
			return true;
		}
		return false;
	}
	
	public boolean isBottomMost() {
		return isBottomMost;
	}
	
	public boolean isLeftMost() {
		return isLeftMost;
	}
	
	public boolean isLoading() {
		return loadingPage;
	}

	public boolean isMaxZoom() {
		final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
		return current.getImage().getZoomFactor() >= Constants.MAX_ZOOM_FACTOR;
	}
	
	public boolean isRightMost() {
		return isRightMost;
	}
	
	public boolean isTopMost() {
		return isTopMost;
	}
	
	public boolean goToCurrent() {
		if (!isAnimating()) {
			if (mustConsiderFrames()) {
				setFrame(framePosition, true, true);
			}
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Shows the next frame or the next screen if the current screen has no more frames or frames are disabled.
	 * @return
	 */
	public boolean next() {
		if (!isAnimating()) {
			if (mustConsiderFrames()) {
				if (framePosition >= comic.getFramesSize(position)-1) { // Last frame
					return nextScreen();
				} else { // Any other frame
					setFrame(framePosition + 1, true, true);
					return true;
				}
			} else {
				return nextScreen();
			}
		}
		return false;
	}
	
	public boolean nextScreen() {
		if (!isAnimating()) {
			return forceNextScreen();
		}
		return false;
	}
	
	public void onCompletion(MediaPlayer mp) {
		mVideoPlaying = false;
    	mSwitcher.setVisibility(View.VISIBLE);
    	mVideoView.setVisibility(View.INVISIBLE);
    	if (moveForwardAfterVideo) {
    		forceNextScreen();
    	} else {
    		forcePreviousScreen();
    	}
    }
	
	public boolean onError(MediaPlayer mp, int what, int extra) {
		mVideoPlaying = false;
		stopAnimating();
		mVideoView.setVisibility(View.INVISIBLE);
		mSwitcher.setVisibility(View.VISIBLE);
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
		current.getImage().abortScrollerAnimation();
		return false;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		super.onTouchEvent(motionEvent);
		if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
			ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
			if (current != null) {
				isLeftMost = current.getImage().isLeftMost();
				isRightMost = current.getImage().isRightMost();
				isTopMost = current.getImage().isTopMost();
				isBottomMost = current.getImage().isBottomMost();
			}
		}
		// TODO: Abort scroller animation here?
		return false;
	}
	
	/**
	 * Shows the previous frame or the previous screen if the current has no previous frames or frames are disabled.
	 * @return
	 */
	public boolean previous() {
		if (!isAnimating()) {
			if (mustConsiderFrames()) {
				if (framePosition <= 0) { // First frame
					return previousScreen();
				} else { // Any other frame
					setFrame(framePosition - 1, false, true);
					return true;
				}
			} else {
				return previousScreen();
			}
		} 
		return false;
	}
	
	public boolean previousScreen() {
		if (!isAnimating()) {
			return this.forcePreviousScreen();
		}
		return false;
	}
	
	public void recycleBitmaps() {
		final ComicFrame next = (ComicFrame) mSwitcher.getNextView();
		if (next != null) next.getImage().recycleBitmap();
		final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
		if (current != null) current.getImage().recycleBitmap();
	}
	
	public boolean scroll(int distanceX, int distanceY) {
		if (!isAnimating()) {
			ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
			current.getLetterbox().hide(true);
			current.removeContent();
			current.getImage().safeScrollBy(distanceX, distanceY);
			return true;
		}
		return false;

	}
	
	public void scroll(MotionEvent event) {
		if (!isAnimating()) {
			ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
			if (!current.getImage().isSmallerThanRootView()) {
				current.getLetterbox().hide(true);
				current.removeContent();
				current.getImage().smoothScroll(event);
			}
		}
	}
	
	public void setComic(Comic comic) {
		this.comic = comic;
		position = -1; // TODO: Why -1?
		framePosition = 0;
		messageButton.setVisibility(GONE);
    	final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
    	if (current != null) {
    		current.getLetterbox().hide(false); // TODO: Is this necessary?
    	}
	}
	
    public void setListener(ComicViewListener listener) {
		this.mListener = listener;
	}
    
    public Point toImagePoint(Point viewPoint) {
    	final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
		return current.getImage().toImagePoint(viewPoint);
    }
    
    public Rect getOriginalSize() {
    	final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
		return current.getImage().getOriginalSize();  	
    }
    
    // TODO: Combine in a single zoom public method
	public boolean zoom(int increment, Point viewPoint) {
		if (!isAnimating()) {
			final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
			if (current != null) {
				current.getImage().zoom(increment, viewPoint);
				current.getLetterbox().hide(true);
				current.removeContent();
				return true;
			}
		}
		return false;
	}
	
	public boolean zoom(float factor, Point imagePoint) {
		if (!isAnimating()) {
			final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
			if (current != null) {
				current.getLetterbox().hide(true);
				current.removeContent();
				current.getImage().zoom(factor, imagePoint);
				return true;
			}
		}
		return false;
	}
	
	private boolean fit(String scaleMode) {
		if (!isAnimating()) {
			final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
			if (current != null) {
				Editor editor = preferences.edit();
				editor.putString(Constants.SCALE_MODE_KEY, scaleMode);
				editor.commit();
				current.getImage().scale(scaleMode, true);
				return true;
			}
		}
		return false;		
	}
	
	private boolean forceNextScreen() {
		int newPosition = position + 1;
		if (newPosition >= comic.getLength()) { // Load next comic
			return false;
		} else {
			moveForwardAfterVideo = true;
			setPosition(position + 1, true, true);
			return true;
		}		
	}
	
	private boolean forcePreviousScreen() {
		int newPosition = position - 1;
		if (newPosition < 0) { // Load next comic
			return false;
		} else {
			moveForwardAfterVideo = false;
			setPosition(position - 1, false, true);
			return true;
		}
	}
	
	private boolean isAnimating() {
		if (mAnimating) {
			return true;
		} else {
			final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();
			if (current != null) {
				return current.getImage().isAnimating();
			}
		}
		return false;
	}
	
	/**
	 * Determines if frames must be considered for the current screen. 
	 * @return
	 */
	private boolean mustConsiderFrames() {
		return comic.hasFrames(position);
	}
	
	private Runnable mAutoplayRunnable;
	
	private void postFrameDisplayedActions(boolean forward) {
		final ACVComic acv = (ACVComic) comic; // If it has frames it's an ACV comic
		
		final boolean vibrate = acv.isVibrate(position, framePosition);
		final boolean autoplay;
		if (forward) {
			autoplay = acv.isAutoplay(position, framePosition);
			if (autoplay || mAutoplay) {
				long duration = acv.getDuration(position, framePosition);
				startAnimating();
				mAutoplayRunnable = new Runnable() {
					@Override
					public void run() {
						if (framePosition >= comic.getFramesSize(position) - 1) { 
							if (position + 1 < comic.getLength()) {
								moveForwardAfterVideo = true;
								setPosition(position + 1, true, true);
							}
						} else {
							setFrame(framePosition + 1, true, true);
						}
					}
				};
				this.postDelayed(mAutoplayRunnable, duration);
			}
		} else {
			autoplay = false;
		}
		if (vibrate) {
			vibrate(!autoplay);
		}
		if (!vibrate && !autoplay) {
			stopAnimating();
		}
		File sound = acv.getSound(position, framePosition);
		if (sound != null) {
			playSound(sound);
		}
	}
	
	private void prepareMessageButton() {
		if (comic instanceof ACVComic) {
			ACVComic.Message message = ((ACVComic) comic).getMessage(position);
			if (message != null) {
				messageButton.setEnabled(true);
				messageButton.setText(message.text);
				messageButton.setVisibility(View.VISIBLE);
			} else {
				messageButton.setVisibility(View.GONE);
			}
		} else {
			messageButton.setVisibility(View.GONE);
		}
	}
	
	private void prepareCaption() {
		if (mCaption != null) {
			if (comic instanceof ACVComic) {
				final String caption = ((ACVComic) comic).getDescription(position);
				if (caption != null) {
					mCaption.setVisibility(View.VISIBLE);
					mCaption.setCaption(caption);
					mCaption.showCaption();
				} else {
					mCaption.setVisibility(View.GONE);
				}
			} else {
				mCaption.setVisibility(View.GONE);
			}
		}
	}
		
	private void setFrame(final int framePosition, final boolean forward, final boolean sequential) {
		this.framePosition = framePosition;
		if (comic instanceof ACVComic) {
			final ACVComic acv = (ACVComic) comic;
			final ComicFrame current = (ComicFrame) mSwitcher.getCurrentView();

			final int width = current.getImage().getOriginalWidth();
			final int height = current.getImage().getOriginalHeight();
			final Rect frame = acv.rectForSize(position, framePosition, width, height);

			final Integer color = acv.getBackgroundColor(position, framePosition);
			final boolean letterbox = color != null;

			final long transitionDuration;
			final int transitionIndex = forward ? framePosition : framePosition + 1;
			if (!sequential) {
				transitionDuration = 0;
			} else {
				transitionDuration = acv.getTransitionDuration(position, transitionIndex);
			}

			final String transitionString = acv.getTransition(position, transitionIndex);
			if (Constants.TRANSITION_MODE_TRANSLATE_VALUE.equalsIgnoreCase(transitionString) || transitionDuration == 0) {
				// Scroll to frame
				final Rect newFrame;
				final LayoutMeasures imageMeasures = current.getImage().calculateLayout(frame, !letterbox);
				if (transitionDuration > 0) {
					startAnimating();
					current.getImage().setCSVListener(new SuperImageViewListener() {
	
						@Override
						public void onAnimationEnd(SuperImageView view) {
    						current.showContent(acv, position, framePosition, forward, imageMeasures);
							postFrameDisplayedActions(forward);
							view.setCSVListener(null);
						}
					});
					current.removeContent();
					newFrame = current.getImage().animateTo(frame, !letterbox, transitionDuration);
				} else {
					newFrame = current.getImage().scrollTo(frame, !letterbox);
					current.showContent(acv, position, framePosition, forward, imageMeasures);
				}
				if (letterbox) {
					current.getLetterbox().show(newFrame.width(), newFrame.height(), color, transitionDuration);
				} else {
					current.getLetterbox().hide(true);
				}
				if (transitionDuration == 0) {
					this.postFrameDisplayedActions(forward);
				}
			} else { // Switch to frame
				setTransition(forward, transitionString, transitionDuration);
				mImageSwitchTask = new SwitchImageTask();
				mImageSwitchTask.forward = forward;
				mImageSwitchTask.sequential = sequential;
				mImageSwitchTask.imageIndex = position;
				mImageSwitchTask.frameIndex = framePosition;
				mImageSwitchTask.execute();			
			}
		}
	}
	
	private void startChangeScreenTask(final int value, final boolean forward, final boolean sequential) {
		mImageSwitchTask = new SwitchImageTask();
		mImageSwitchTask.forward = forward;
		mImageSwitchTask.sequential = sequential;
		mImageSwitchTask.imageIndex = value;
		mImageSwitchTask.execute();
	}
	
	private void setPosition(final int value, final boolean forward, final boolean sequential) {
		if (!loadingPage) {
			if (sequential) {
				setSequentialTransition(value, forward);
			} else {
				mSwitcher.setInAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
				mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
			}
			if (lowMemoryTransitions) {
    			final Integer backgroundColor = comic.getBackgroundColor(value);
    			final Animation animation;
    			final Animation inAnimation = mSwitcher.getInAnimation();
    			final Animation outAnimation = mSwitcher.getOutAnimation();
    			if (inAnimation != null) {
    				animation = inAnimation;
    				inAnimation.setDuration(inAnimation.getDuration() / 2);
    			} else {
    				animation = outAnimation;
    			}
    			if (outAnimation != null) {
    				outAnimation.setDuration(outAnimation.getDuration() / 2);
    			}
    			if (animation != null) {
    				animation.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationEnd(Animation a) {
							a.setAnimationListener(null);
							ComicView.this.stopAnimating();
							startChangeScreenTask(value, forward, sequential);
						}
						@Override public void onAnimationRepeat(Animation arg0) {}
						@Override public void onAnimationStart(Animation arg0) {}
					});
    				this.startAnimating();
    				final ComicFrame current = (ComicFrame) mSwitcher.getNextView();
    				current.getImage().setImageDrawable(new ColorDrawable(backgroundColor));
    				mSwitcher.showNext();
    			} else {
					startChangeScreenTask(value, forward, sequential);
    			}
			} else {
				startChangeScreenTask(value, forward, sequential);
			}
		}
	}
	
	private void setSequentialTransition(int index, boolean forward) {
		String transitionMode = null;
		final long transitionDuration;
		if (comic instanceof ACVComic) {
			transitionMode = ((ACVComic) comic).getTransition(forward ? index : index + 1);
			transitionDuration = ((ACVComic) comic).getTransitionDuration(forward ? index : index + 1);
		} else {
			transitionDuration = -1;
		}
		if (transitionMode == null) {
			transitionMode = preferences.getString(Constants.TRANSITION_MODE_KEY, Constants.TRANSITION_MODE_NONE_VALUE);
		}
		setTransition(forward, transitionMode, transitionDuration);
	}

	private void setTransition(boolean forward, String transitionString, long duration) {
		final Animation inAnimation;
		final Animation outAnimation;
		if (Constants.TRANSITION_MODE_FADE_VALUE.equals(transitionString)) {
			if (nextImageUp) {
				inAnimation = AnimationUtils.loadAnimation(getContext(), animationFadeIn);
				outAnimation = AnimationUtils.loadAnimation(getContext(), animationKeep);
			} else {
				inAnimation = AnimationUtils.loadAnimation(getContext(), animationKeep);
				outAnimation = AnimationUtils.loadAnimation(getContext(), animationFadeOut);
			}
		} else if (Constants.TRANSITION_MODE_NONE_VALUE.equals(transitionString)) {
			inAnimation = null;
			outAnimation = null;
		} else if (Constants.TRANSITION_MODE_PUSH_UP_VALUE.equals(transitionString)){
			inAnimation = AnimationUtils.loadAnimation(getContext(), forward ? animationPushUpIn : animationPushDownIn);
			outAnimation = AnimationUtils.loadAnimation(getContext(), forward ? animationPushUpOut : animationPushDownOut);
		} else if (Constants.TRANSITION_MODE_PUSH_DOWN_VALUE.equals(transitionString)){
			inAnimation = AnimationUtils.loadAnimation(getContext(), forward ? animationPushDownIn : animationPushUpIn);
			outAnimation = AnimationUtils.loadAnimation(getContext(), forward ? animationPushDownOut : animationPushUpOut);
		} else {
			boolean leftToRight = isLeftToRight();
			if ((forward && leftToRight) || (forward && !leftToRight)) {
				inAnimation = AnimationUtils.loadAnimation(getContext(), animationPushLeftIn);
				outAnimation = AnimationUtils.loadAnimation(getContext(), animationPushLeftOut);
			} else {
				inAnimation = AnimationUtils.loadAnimation(getContext(), animationPushRightIn);
				outAnimation = AnimationUtils.loadAnimation(getContext(), animationPushRightOut);
			}
		}
		if (duration >= 0) {
			if (inAnimation != null) {
				inAnimation.setDuration(duration);
			}
			if (outAnimation != null) {
				outAnimation.setDuration(duration);
			}
		}
		mSwitcher.setInAnimation(inAnimation);
		mSwitcher.setOutAnimation(outAnimation);
	}

	private void showOfferSubscribeDialog() {
		AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle(
				stringUnavailableTitle).setMessage(stringUnavailableText)
				.setPositiveButton(android.R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								if (mListener != null) {
									mListener.onRequestSubscription();
								}
							}
						}).setNegativeButton(android.R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).create();
		dialog.show();
	}
	
	private void showScreenNumber() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		boolean showNumber = preferences.getBoolean(Constants.SHOW_NUMBER_KEY, false);
		if (showNumber) {
			if (toast != null) {
				toast.cancel();
			}
			String message;
			if (previousPosition == -1) {
				String path = comic.getPath();
				File file = new File(path);
				message = file.getName();
			} else {
				message = String.valueOf(1 + position);
			}
			toast  = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.BOTTOM|Gravity.RIGHT, 0, 0);
			toast.show();
		}		
	}
	private void startAnimating() {
		mAnimating = true;
		if (mListener != null) {
			mListener.onAnimationStart(this);
		}
	}
	
	private void stopAnimating() {
		mAnimating = false;
		if (mListener != null) {
			mListener.onAnimationEnd(this);
		}
	}
	
	private void vibrate(final boolean stopAnimationWhenDone) {
		final Vibrator vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
		if (vibrator != null) {
			vibrator.vibrate(500);
		}
		final AnimationSet set = new AnimationSet(true);
		int fromXDelta = 1;
		int fromYDelta = 1;
		final int maxDeltaScaled = MathUtils.dipToPixel(getContext(), 30);
		for (int i = 0; i < 5; i++) {
			int toXDelta = Math.round((float) (Math.random() * maxDeltaScaled) - ((float) maxDeltaScaled / 2f));
			int toYDelta = Math.round((float) (Math.random() * maxDeltaScaled) - ((float) maxDeltaScaled / 2f));
			TranslateAnimation t = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
			t.setDuration(50);
			t.setStartOffset(50 * i);
			set.addAnimation(t);
			fromXDelta = toXDelta;
			fromYDelta = toYDelta;
		}
		set.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				if (stopAnimationWhenDone)
					stopAnimating();
			}

			@Override
			public void onAnimationRepeat(Animation arg0) {
			}

			@Override
			public void onAnimationStart(Animation arg0) {
				startAnimating();
			}
		});
		this.startAnimation(set);
	}
	
}
