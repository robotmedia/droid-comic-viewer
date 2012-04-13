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
package net.robotmedia.acv.ui;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import net.androidcomics.acv.R;
import net.robotmedia.acv.Constants;
import net.robotmedia.acv.adapter.ACVListAdapter;
import net.robotmedia.acv.adapter.RecentListBaseAdapter;
import net.robotmedia.acv.comic.Comic;
import net.robotmedia.acv.logic.*;
import net.robotmedia.acv.provider.HistoryManager;
import net.robotmedia.acv.ui.settings.SettingsActivityPostHC;
import net.robotmedia.acv.ui.settings.SettingsActivityPreHC;
import net.robotmedia.acv.ui.widget.*;
import net.robotmedia.acv.utils.*;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.*;

public class ComicViewerActivity extends ExtendedActivity implements OnGestureListener, GestureDetector.OnDoubleTapListener, ComicViewListener {

	public final static String POSITION_EXTRA = "position";
	
	private class LoadComicTask extends AsyncTask<String, Object, Comic> {

		public int initialIndex = 0;
		
		private ProgressDialog progressDialog;
		
		@Override
		protected Comic doInBackground(String... params) {
			final String path = params[0];
			Comic result = Comic.createComic(path);
			if (result != null) {
				HistoryManager.getInstance(ComicViewerActivity.this).remember(new File(path));
			}
			// result.setDensityScale(getResources().getDisplayMetrics().density);
			return result;
		}
		
		protected void onPostExecute (Comic result) {
			if (progressDialog != null) progressDialog.dismiss();

			if (result != null && !result.isError()) {
				comic = result;
				
				trackOpen();
				
				mScreen.setVisibility(View.VISIBLE);
				hideRecentItems();
				pController.savePreference(Constants.COMIC_PATH_KEY, comic.getPath());

				mScreen.setComic(comic);
				mScreen.goToScreen(initialIndex);
				
				if(isHoneyComb()) {
					invalidateOptionsMenu();
				}
				hideActionBar();
				
			} else {
				mScreen.setVisibility(View.GONE);
				showRecentItems();
				showDialog(Constants.DIALOG_LOAD_ERROR);
			}
		}
		
		@Override
		protected void onPreExecute () {
			progressDialog = new ACVDialogFactory(ComicViewerActivity.this).createLoadProgressDialog();
			progressDialog.show();
			removePreviousComic(true);
		}
	}

	private GestureDetector mGestureDetector;
	private ImageButton mCornerTopLeft;
	private ImageButton mCornerTopRight;
	private ImageButton mCornerBottomLeft;
	private ImageButton mCornerBottomRight;
	private RelativeLayout mAdsContainer;
	private boolean mainMenuActive = false;

	protected Comic comic;
	protected boolean destroyed = false;
	protected ACVDialogFactory dialogFactory;
	protected LoadComicTask loadComicTask = null;

	protected boolean markCleanExitPending = false;
	protected ViewGroup mRecentItems = null;
	protected ListView mRecentItemsList = null;
	protected ACVListAdapter mRecentItemsListAdapter = null;
	protected View mMain;
	protected ComicView mScreen;
	protected PreferencesController pController;
	protected SharedPreferences preferences;
	protected boolean requestedRotation = false;

	private String mComicPath; // Used for testing
	
	public String getComicPath() {
		return mComicPath;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.SCREEN_BROWSER_CODE && resultCode == RESULT_OK) {
			int index = data.getIntExtra(BrowseActivity.POSITION_EXTRA, mScreen.getIndex());
			if (isComicLoaded()) mScreen.goToScreen(index);
		} else if (requestCode == Constants.SD_BROWSER_CODE && resultCode == RESULT_OK) {
			String absolutePath = data.getStringExtra(Constants.COMIC_PATH_KEY);
			this.loadComic(absolutePath);
		} else if (requestCode == Constants.SETTINGS_CODE) {
			boolean sensor = preferences.getBoolean(Constants.AUTO_ROTATE_KEY, false);
			if (sensor) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
			}

			if (pController.isLeftToRight()) {
				if (pController.isUsedForPreviousNext(Constants.TRACKBALL_RIGHT_KEY, Constants.TRACKBALL_LEFT_KEY) || 
						pController.isUsedForPreviousNext(Constants.INPUT_FLING_LEFT, Constants.INPUT_FLING_RIGHT) || 
						pController.isUsedForPreviousNext(Constants.INPUT_CORNER_BOTTOM_RIGHT, Constants.INPUT_CORNER_BOTTOM_LEFT)) {
					showDialog(Constants.DIALOG_FLIP_CONTROLS);
				}
			} else {
				if (pController.isUsedForPreviousNext(Constants.TRACKBALL_LEFT_KEY, Constants.TRACKBALL_RIGHT_KEY) || 
						pController.isUsedForPreviousNext(Constants.INPUT_FLING_RIGHT, Constants.INPUT_FLING_LEFT) || 
						pController.isUsedForPreviousNext(Constants.INPUT_CORNER_BOTTOM_LEFT, Constants.INPUT_CORNER_BOTTOM_RIGHT)) {
					showDialog(Constants.DIALOG_FLIP_CONTROLS);
				}
			}
			this.adjustCornersVisibility(true); // Actions assigned to corners might have changed

			adjustBrightness();
			adjustLowMemoryMode();
		
			if (isComicLoaded()) mScreen.goToScreen(mScreen.getIndex());
		} else if (requestCode == Constants.SUBSCRIBE_CODE) {
			switch (resultCode) {
			case RESULT_OK:
				DialogFactory.showSimpleAlert(this, true, R.string.dialog_subscribe_success_title, R.string.dialog_subscribe_success_text);
				break;
			case SubscribeActivity.RESULT_ERROR:
				DialogFactory.showSimpleAlert(this, false, R.string.dialog_subscribe_error_title, R.string.dialog_subscribe_error_text);
				break;
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if(!isHoneyComb()) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.main);

		dialogFactory = new ACVDialogFactory(this);
		mRecentItems = (ViewGroup) findViewById(R.id.main_recent);
		mRecentItemsList = (ListView) findViewById(R.id.main_recent_list);
		mRecentItemsList.setEmptyView(findViewById(R.id.main_recent_list_no_items));
		mRecentItemsListAdapter = new RecentListBaseAdapter(this, R.layout.list_item_recent);
		mRecentItemsList.setAdapter(mRecentItemsListAdapter);
		mRecentItemsList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String path = (String) parent.getItemAtPosition(position);
				loadComic(path);
			}
		});
		
		mGestureDetector = new GestureDetector(this);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);

		mMain = findViewById(R.id.main_layout);

		ImageView logo = (ImageView) findViewById(R.id.main_logo);
		logo.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				showMenu();
		}});

		mScreen = (ComicView) findViewById(R.id.screen);
		mScreen.setListener(this);

		mCornerTopLeft = (ImageButton) findViewById(R.id.corner_top_left);
		mCornerTopRight = (ImageButton) findViewById(R.id.corner_top_right);
		mCornerBottomLeft = (ImageButton) findViewById(R.id.corner_bottom_left);
		mCornerBottomRight = (ImageButton) findViewById(R.id.corner_bottom_right);
		adjustCornersVisibility(true);

		pController = new PreferencesController(this);

		mAdsContainer = (RelativeLayout) findViewById(R.id.mainAdsContainer);

		adjustBrightness();
		adjustLowMemoryMode();

		// TODO: Shouldn't this be first?
		if (startupOrientation(savedInstanceState)) { // If no orientation change was requested
			pController.checkCleanExit();
			markCleanExitPending = true;

			String savedFilePath = savedInstanceState != null ? savedInstanceState.getString(Constants.COMIC_PATH_KEY) : null;
			//loadComicOnStartup(savedFilePath);
			showRecentItems();
		}

		showAds();
	}
	
	@Override
	public void onResume() {
		mRecentItemsListAdapter.refresh();
		super.onResume();
	}

	private void adjustLowMemoryMode() {
		boolean lowMemory = preferences.getBoolean(PreferencesController.PREFERENCE_LOW_MEMORY, false);
		mScreen.setPreload(!lowMemory);
		mScreen.setLowMemoryTransitions(lowMemory);
	}

	private void adjustBrightness() {
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		 float brightness = (float) preferences.getInt(Constants.BRIGHTNESS_KEY, Math.round(lp.screenBrightness * 100)) / 100f;
		 if (brightness == 0) { // 0 renders the phone unusable
			 brightness = 1f/100f;
		 }
		 lp.screenBrightness = brightness; 
		 getWindow().setAttributes(lp);
	}

	private void adjustCornerVisibility(ImageButton corner, String key, String defaultAction, boolean allInvisible) {
		final String action = preferences.getString(key, defaultAction);
		final boolean visible = !allInvisible && !Constants.ACTION_VALUE_NONE.equals(action);
		if (visible) {
			corner.setImageResource(R.drawable.corner_button);
		} else {
			corner.setImageDrawable(null);
		}
	}

	private void adjustCornersVisibility(final boolean visible) {
		final boolean allInvisible = !visible || preferences.getBoolean(Constants.PREFERENCE_INVISIBLE_CORNERS, false);
		adjustCornerVisibility(mCornerTopLeft, Constants.INPUT_CORNER_TOP_LEFT, Constants.ACTION_VALUE_NONE, allInvisible);
		adjustCornerVisibility(mCornerTopRight, Constants.INPUT_CORNER_TOP_RIGHT, Constants.ACTION_VALUE_NONE, allInvisible);
		adjustCornerVisibility(mCornerBottomLeft, Constants.INPUT_CORNER_BOTTOM_LEFT, Constants.ACTION_VALUE_PREVIOUS, allInvisible);
		adjustCornerVisibility(mCornerBottomRight, Constants.INPUT_CORNER_BOTTOM_RIGHT, Constants.ACTION_VALUE_NEXT, allInvisible);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onDoubleTap(MotionEvent e) {
		String action = preferences.getString(Constants.INPUT_DOUBLE_TAP, Constants.ACTION_VALUE_ZOOM_IN);
		Point p = new Point(Math.round(e.getX()), Math.round(e.getY()));
		if (Constants.ACTION_VALUE_ZOOM_IN.equals(action) && isComicLoaded() && mScreen.isMaxZoom()) {
			return mScreen.zoom(-1, p);
		} else {
			return action(Constants.INPUT_DOUBLE_TAP, Constants.ACTION_VALUE_ZOOM_IN, p);
		}
	}
	
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}
	
	public boolean onDown(MotionEvent e) {
		return false;
	}
	
	public boolean onFling(MotionEvent e1, MotionEvent e2, float arg2, float arg3) {
		if (mPinch) return false;
		final double angle = MathUtils.getAngle(e1.getX(), e1.getY(), e2.getX(), e2.getY());
		final int minFlingDifference = MathUtils.dipToPixel(this, Constants.MIN_FLING_DIFFERENCE_DIP);
		final float distance = MathUtils.distance(e1.getX(), e1.getY(), e2.getX(), e2.getY());

		boolean comicLoaded = isComicLoaded();
		if (distance > minFlingDifference) {
			if ((angle < Constants.MAX_FLING_ANGLE || Math.abs(angle - 180) < Constants.MAX_FLING_ANGLE)) {
				if (e1.getX() > e2.getX()) { // Fling left
					if (!comicLoaded || mScreen.isRightMost()) {	return action(Constants.INPUT_FLING_LEFT, Constants.ACTION_VALUE_NEXT);	}
				} else { // Fling right
					if (!comicLoaded || mScreen.isLeftMost()) { return action(Constants.INPUT_FLING_RIGHT, Constants.ACTION_VALUE_PREVIOUS);	}
				}
			} else if (angle - 90 < Constants.MAX_FLING_ANGLE) {
				if (e1.getY() > e2.getY()) { // Fling up
					if (!comicLoaded || mScreen.isBottomMost()) { return action(Constants.INPUT_FLING_UP, Constants.ACTION_MENU); }
				} else { // Fling down
					if (!comicLoaded || mScreen.isTopMost()) { return action(Constants.INPUT_FLING_DOWN, Constants.ACTION_VALUE_NONE); }
				}
			}
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		String action = Constants.ACTION_VALUE_NONE;
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			action = preferences.getString(Constants.INPUT_VOLUME_UP, Constants.ACTION_VALUE_PREVIOUS);
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) { 
			action = preferences.getString(Constants.INPUT_VOLUME_DOWN, Constants.ACTION_VALUE_NEXT);
		}
		if (Constants.ACTION_VALUE_NONE.equals(action)) {
			return super.onKeyUp(keyCode, event);
		} else {
			return true;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			final String action = preferences.getString(Constants.INPUT_VOLUME_UP, Constants.ACTION_VALUE_PREVIOUS);
			if (Constants.ACTION_VALUE_NONE.equals(action)) {
				return super.onKeyDown(keyCode, event);
			} else {
				action(Constants.INPUT_VOLUME_UP, Constants.ACTION_VALUE_PREVIOUS);
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) { 
			final String action = preferences.getString(Constants.INPUT_VOLUME_DOWN, Constants.ACTION_VALUE_NEXT);
			if (Constants.ACTION_VALUE_NONE.equals(action)) {
				return super.onKeyDown(keyCode, event);
			} else {
				action(Constants.INPUT_VOLUME_DOWN, Constants.ACTION_VALUE_NEXT);
				return true;
			}
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			return action(Constants.TRACKBALL_RIGHT_KEY, Constants.ACTION_VALUE_NEXT);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			return action(Constants.TRACKBALL_LEFT_KEY, Constants.ACTION_VALUE_PREVIOUS);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			return action(Constants.TRACKBALL_UP_KEY, Constants.ACTION_VALUE_ZOOM_IN);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			return action(Constants.TRACKBALL_DOWN_KEY, Constants.ACTION_VALUE_ZOOM_OUT);
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			return action(Constants.TRACKBALL_CENTER_KEY, Constants.ACTION_VALUE_NEXT);
		}  else if (keyCode == KeyEvent.KEYCODE_BACK) {
			final String action = preferences.getString(Constants.BACK_KEY, Constants.ACTION_VALUE_NONE);
			if (Constants.ACTION_VALUE_NONE.equals(action)) {
				return super.onKeyDown(keyCode, event);
			} else if (Constants.ACTION_VALUE_PREVIOUS.equals(action) && mScreen.getIndex() == 0) {
				return super.onKeyDown(keyCode, event);
			} else {
				action(Constants.BACK_KEY, Constants.ACTION_VALUE_NONE);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void onLongPress(MotionEvent arg0) {
		if (!mPinch) {
			action(Constants.LONG_TAP_KEY, Constants.ACTION_VALUE_SCREEN_BROWSER);
		}
	}

	@Override
	public boolean onMenuItemSelected (int featureId, MenuItem item){

		if(isHoneyComb()) {
			boolean comicLoaded = isComicLoaded();

			if(item.hasSubMenu()) {
				Menu menu = item.getSubMenu();
				
				switch(featureId) {
					case R.id.item_share:
						menu.findItem(R.id.item_share_screen).setVisible(comicLoaded);
						menu.findItem(R.id.item_set_as).setVisible(comicLoaded);
						break;
				}
			} else {
			}
		}
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public void onPanelClosed (int featureId, Menu menu) {
		super.onPanelClosed(featureId, menu);
		hideActionBarDelayed();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String actionValue = null;
		switch (item.getItemId()) {
		case R.id.item_zoom_in:
			actionValue = Constants.ACTION_VALUE_ZOOM_IN;
			break;
		case R.id.item_zoom_out:
			actionValue = Constants.ACTION_VALUE_ZOOM_OUT;
			break;
		case R.id.item_fit_width:
			actionValue = Constants.ACTION_VALUE_FIT_WIDTH;
			break;
		case R.id.item_fit_height:
			actionValue = Constants.ACTION_VALUE_FIT_HEIGHT;
			break;
		case R.id.item_actual_size:
			actionValue = Constants.ACTION_VALUE_ACTUAL_SIZE;
			break;
		case R.id.item_first:
			actionValue = Constants.ACTION_VALUE_FIRST;
			break;
		case R.id.item_previous:
			actionValue = Constants.ACTION_VALUE_PREVIOUS;
			break;
		case R.id.item_next:
			actionValue = Constants.ACTION_VALUE_NEXT;
			break;
		case R.id.item_previous_screen:
			actionValue = Constants.ACTION_VALUE_PREVIOUS_SCREEN;
			break;
		case R.id.item_next_screen:
			actionValue = Constants.ACTION_VALUE_NEXT_SCREEN;
			break;
		case R.id.item_last:
			actionValue = Constants.ACTION_VALUE_LAST;
			break;
		case R.id.item_browse:
			actionValue = Constants.ACTION_VALUE_SCREEN_BROWSER;
			break;
		case R.id.item_rotate:
			actionValue = Constants.ACTION_VALUE_ROTATE;
			break;
		case R.id.item_settings:
			actionValue = Constants.ACTION_VALUE_SETTINGS;
			break;
		case R.id.item_open:
			actionValue = Constants.ACTION_VALUE_SD_BROWSER;
			break;
		case R.id.item_share_app:
			actionValue = Constants.ACTION_VALUE_SHARE_APP;
			break;
		case R.id.item_set_as:
			actionValue = Constants.ACTION_SET_AS;
			break;
		case R.id.item_share_screen:
			actionValue = Constants.ACTION_VALUE_SHARE_SCREEN;
			break;
		case R.id.menu_close:
			actionValue = Constants.ACTION_CLOSE;
			break;
		}
		if (actionValue != null) {
			return this.actionWithValue(actionValue, Constants.EVENT_VALUE_MENU, null);
		} else {
			return false;
		}
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		if(!isHoneyComb() && mScreen.isLoading()) {
			return false;
		}
		
		boolean comicLoaded = isComicLoaded();
		menu.findItem(R.id.item_share_screen).setVisible(comicLoaded);
		menu.findItem(R.id.item_set_as).setVisible(comicLoaded);
		menu.findItem(R.id.item_navigate).setVisible(comicLoaded);
		menu.findItem(R.id.item_zoom).setVisible(comicLoaded);
		menu.findItem(R.id.item_browse).setVisible(comicLoaded);
		menu.findItem(R.id.item_rotate).setVisible(comicLoaded);
		
		if (comicLoaded) {
			boolean considerFrames = comic.hasFrames(mScreen.getIndex()) && preferences.getBoolean(Constants.ACV_FRAMES_KEY, true);
			menu.findItem(R.id.item_next_screen).setVisible(considerFrames);
			menu.findItem(R.id.item_previous_screen).setVisible(considerFrames);
		}
		return true;
		
	}
	
	public void onRequestSubscription() {
		startSubscribeActivity();		
	}
	
	public void onScreenLoadFailed() {
		mScreen.setVisibility(View.INVISIBLE);
		showRecentItems();

		// Remove the comic path in case the comic is defective. 
		// If the page load failed because of an orientation change, the comic path is saved in the instance state anyway.
		pController.savePreference(Constants.COMIC_PATH_KEY, null);
		
		removePreviousComic(true);
		
		// Don't want to show an error if the activity was destroyed 
		if (!destroyed) { 
			showDialog(Constants.DIALOG_PAGE_ERROR);
		}		
	}

	private boolean mScrolling;
	
	public boolean onScroll(MotionEvent downEvent, MotionEvent dragEvent, float distanceX, float distanceY) {
		mScrolling = true;
		if (isComicLoaded() && !mPinch) {
			return mScreen.scroll(Math.round(distanceX), Math.round(distanceY));
		} else {
			return false;
		}
	}

	public void onShowPress(MotionEvent arg0) {
	}

	
	private boolean detectCornerButton(MotionEvent e, boolean pressed, boolean action) {
		final float x = e.getX();
		final float y = e.getY();
		final int width = mMain.getWidth();
		final int height = mMain.getHeight();
		final int cornerWidth = MathUtils.dipToPixel(this, Constants.CORNER_WIDTH_DIP);

		String inputKey = null;
		String defaultAction = null;
		View button = null;
		if (x <= cornerWidth && y <= cornerWidth) {
			button = mCornerTopLeft;
			inputKey = Constants.INPUT_CORNER_TOP_LEFT;
			defaultAction = Constants.ACTION_VALUE_NONE;
		} else if (x <= cornerWidth && y >= height - cornerWidth) {
			button = mCornerBottomLeft;
			inputKey = Constants.INPUT_CORNER_BOTTOM_LEFT;
			defaultAction = Constants.ACTION_VALUE_PREVIOUS;
		} else if (x >= width - cornerWidth && y <= cornerWidth) {
			button = mCornerTopRight;
			inputKey = Constants.INPUT_CORNER_TOP_RIGHT;
			defaultAction = Constants.ACTION_VALUE_NONE;
		} else if (x >= width - cornerWidth && y >= height - cornerWidth) {
			button = mCornerBottomRight;
			inputKey = Constants.INPUT_CORNER_BOTTOM_RIGHT;
			defaultAction = Constants.ACTION_VALUE_NEXT;
		}
		boolean actionPerformed;
		if (action && inputKey != null) {
			actionPerformed = action(inputKey, defaultAction);
		} else {
			actionPerformed = false;
		}
		if (button != null) {
			button.setPressed(pressed);
		} else {
			unpressCornerButtons();
		}
		return actionPerformed;
	}
	
	public boolean onSingleTapConfirmed(MotionEvent e) {
		
		if (mPinch || mCornerButtonConsumed) return false; // TODO is pinch necessary?
		final Point p = new Point(Math.round(e.getX()), Math.round(e.getY()));
		boolean processed = action(Constants.SINGLE_TAP_KEY, Constants.ACTION_VALUE_NONE, p);
		if(!processed)
			toggleControls();
		return processed;
	}

	private boolean mCornerButtonConsumed = false;
	
	public boolean onSingleTapUp(MotionEvent e) {
		if (mPinch) return false; // TODO is this necessary?
		mCornerButtonConsumed = detectCornerButton(e, false, true);
		return mCornerButtonConsumed;
	}

	public void onStop()
	{
	   super.onStop();
		if (markCleanExitPending) {
			pController.markCleanExit();
		}
	}
	
	private void onActionDown(MotionEvent e) {
		mPinch = false;
		detectCornerButton(e, true, false);
	}
	
	private void onActionMove(MotionEvent e) {
		if (mPinching && Reflect.getPointerCount(e) == 2) {
			final float x0 = Reflect.getX(e, 0);
			final float y0 = Reflect.getY(e, 0);
			final float x1 = Reflect.getX(e, 1);
			final float y1 = Reflect.getY(e, 1);
			final float newDistance = MathUtils.distance(x0, y0, x1, y1);
			float ratio =  newDistance / pinchDistance;
			ratio = 1 + (ratio - 1) * 0.5f;
			mScreen.zoom(ratio, pinchCenter);
			pinchDistance = newDistance;
		}
		detectCornerButton(e, true, false);
	}

	private void unpressCornerButtons() {
		mCornerBottomLeft.setPressed(false);
		mCornerBottomRight.setPressed(false);
		mCornerTopLeft.setPressed(false);
		mCornerTopRight.setPressed(false);		
	}
	
	private void onActionUp(MotionEvent e) {
		mPinching = false;
		mScrolling = false;
		unpressCornerButtons();
	}
	
	private void onActionPointerDown(MotionEvent e) {
		if (!mScrolling && isComicLoaded()) {
			mPinch = true;
			mPinching = true;
			final float x0 = Reflect.getX(e, 0);
			final float y0 = Reflect.getY(e, 0);
			final float x1 = Reflect.getX(e, 1);
			final float y1 = Reflect.getY(e, 1);
			pinchDistance = MathUtils.distance(x0, y0, x1, y1);
			final int centerX = Math.round(x0 + x1) / 2;
			final int centerY = Math.round(y0 + y1) / 2;
			Point center = new Point(centerX, centerY);
			pinchCenter = mScreen.toImagePoint(center);
		}
	}

	private void onActionPointerUp(MotionEvent e) {}

	
	private boolean mPinch = false;
	private boolean mPinching = false;
	private Point pinchCenter;
	private float pinchDistance;
	// TODO: Move pinch logic to SuperImageView.
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		final boolean wasScrolling = mScrolling;
		final int action = e.getAction() & Reflect.ACTION_MASK();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			this.onActionDown(e);
			break;
		case MotionEvent.ACTION_MOVE:
			this.onActionMove(e);
			break;
		case MotionEvent.ACTION_UP:
			this.onActionUp(e);
			break;
		}

		if (action == Reflect.ACTION_POINTER_DOWN()) {
			this.onActionPointerDown(e);
		} else if (action == Reflect.ACTION_POINTER_UP()) {
			this.onActionPointerUp(e);
		}
		
		if (mPinching) {
			return true;
		} else {
			if (isComicLoaded() && (mScrolling || wasScrolling)) {
				mScreen.scroll(e);
			}
			return mGestureDetector.onTouchEvent(e);
		}
	}
	
	private boolean action(String preferenceKey, String defaultValue) {
		return action(preferenceKey, defaultValue, null);
	}

	
	private boolean action(String preferenceKey, String defaultValue, Point p) {
		final String actionValue = preferences.getString(preferenceKey, defaultValue);
		return actionWithValue(actionValue, preferenceKey, p);
	}
	
	private boolean actionWithValue(String actionValue, String preferenceKey, Point p) {
		boolean action = false;
		
		// Actions that require a comic
		if (isComicLoaded()) {
			final int scrollIncrement = MathUtils.dipToPixel(this, Constants.MANUAL_SCROLL_INCREMENT_DIP);
			if (Constants.ACTION_VALUE_PREVIOUS.equals(actionValue)) {
				action = previous();
			} else if (Constants.ACTION_VALUE_PREVIOUS_SCREEN.equals(actionValue)) {
				action = previousScreen();
			} else if (Constants.ACTION_VALUE_ZOOM_IN.equals(actionValue)) {
				action = mScreen.zoom(1, p);
			} else if (Constants.ACTION_VALUE_ZOOM_OUT.equals(actionValue)) {
				action = mScreen.zoom(-1, p);
			} else if (Constants.ACTION_VALUE_SCROLL_UP.equals(actionValue)) {
				action = mScreen.scroll(0, -scrollIncrement);
			} else if (Constants.ACTION_VALUE_SCROLL_DOWN.equals(actionValue)) {
				action = mScreen.scroll(0, scrollIncrement);
			} else if (Constants.ACTION_VALUE_SCROLL_LEFT.equals(actionValue)) {
				action = mScreen.scroll(-scrollIncrement, 0);
			} else if (Constants.ACTION_VALUE_SCROLL_RIGHT.equals(actionValue)) {
				action = mScreen.scroll(scrollIncrement, 0);
			} else if (Constants.ACTION_VALUE_FIRST.equals(actionValue)) {
				action = first();
			} else if (Constants.ACTION_VALUE_LAST.equals(actionValue)) {
				action = last();
			} else if (Constants.ACTION_VALUE_SCREEN_BROWSER.equals(actionValue)) {
				startBrowseActivity();
				action = true;
			}  else if (Constants.ACTION_VALUE_NEXT.equals(actionValue)) {
				action = next();
			} else if (Constants.ACTION_VALUE_NEXT_SCREEN.equals(actionValue)) {
				action = nextScreen();
			} else if (Constants.ACTION_VALUE_FIT_WIDTH.equals(actionValue)) {
				action = mScreen.fitWidth();
			} else if (Constants.ACTION_VALUE_FIT_HEIGHT.equals(actionValue)) {
				action = mScreen.fitHeight();
			} else if (Constants.ACTION_VALUE_FIT_SCREEN.equals(actionValue)) {
				action = mScreen.fitScreen();
			} else if (Constants.ACTION_VALUE_ACTUAL_SIZE.equals(actionValue)) {
				action = mScreen.actualSize();
			} else if (Constants.ACTION_VALUE_SHARE_SCREEN.equals(actionValue)) {
				shareScreen();
				action = true;
			} else if (Constants.ACTION_SET_AS.equals(actionValue)) {
				setAs();
				action = true;
			}
		}
		
		// Actions that do not require a comic
		if (Constants.ACTION_VALUE_SETTINGS.equals(actionValue)) {
			startSettingsActivity();
			action = true;
		} else if (Constants.ACTION_CLOSE.equals(actionValue)){
			close();
			action = true;
		} else if (Constants.ACTION_VALUE_SD_BROWSER.equals(actionValue)) {
			startSDBrowserActivity();
			action = true;
		} else if (Constants.ACTION_VALUE_ROTATE.equals(actionValue)) {
			rotate();
			action = true;
		} else if (Constants.ACTION_VALUE_SHARE_APP.equals(actionValue)) {
			shareApp();
			return true;
		} else if (Constants.ACTION_MENU.equals(actionValue)) {
			showMenu();
			return true;
		}

		if (action) {
			TrackingManager.track(actionValue, Constants.EVENT_PARAM_INPUT, preferenceKey);	
		}
		return action;
	}

	private void setAs() {
		SetComicScreenAsTask task = new SetComicScreenAsTask(this, comic);
		task.execute(mScreen.getIndex());		
	}

	private void close() {
		if (isComicLoaded()) {
			removePreviousComic(true);
			mScreen.setVisibility(View.GONE);
			showRecentItems();
			pController.savePreference(Constants.COMIC_PATH_KEY, null);
			showAds();
		} else {
			finish();
		}
	}

	private String describeOrientation(int orientation) {
		switch (orientation) {
			case Configuration.ORIENTATION_LANDSCAPE:
				return Constants.EVENT_VALUE_LANDSCAPE;
			case Configuration.ORIENTATION_PORTRAIT:
				return Constants.EVENT_VALUE_PORTRAIT;
			case Configuration.ORIENTATION_SQUARE:
				return Constants.EVENT_VALUE_SQUARE;
			default:
				return Constants.EVENT_VALUE_UNDEFINED;
		}
	}

	private ArrayList<File> findCandidates(File file) {
		File parent = file.getParentFile();
		
		String[] allContents = parent.list();
		TreeMap<String, File> aux = new TreeMap<String, File>();
		HashMap<String, Integer> supportedExtensions = Constants.getSupportedExtensions(this);
		if (allContents != null) {
			String path = parent.getPath();
			for (int i = 0; i < allContents.length; i++) {
				String contentName = allContents[i];
				String extension = FileUtils.getFileExtension(contentName);
				if (!net.robotmedia.acv.utils.FileUtils.isHidden(contentName) && supportedExtensions.containsKey(extension.toLowerCase())) {
					File contentFile = new File(path, contentName);
					aux.put(contentFile.getName().toLowerCase(), contentFile);
				}
			}
		}
		ArrayList<File> candidates = new ArrayList<File>();
		candidates.addAll(aux.values());
		return candidates;
	}

	private File findNextComic() {
		String comicPath = comic.getPath();
		File file = new File(comicPath);
		ArrayList<File> candidates = findCandidates(file);
		String fileName = file.getName().toLowerCase();
		boolean next = false;
		File nextComic = null;
		for (File candidate : candidates) {
			if (next) {
				nextComic = candidate;
				break;
			} else if (fileName.equals(candidate.getName().toLowerCase())) {
				next = true;
			}
		}
		return nextComic;
	}

	private File findPreviousComic() {
		String comicPath = comic.getPath();
		File file = new File(comicPath);
		ArrayList<File> candidates = findCandidates(file);
		String fileName = file.getName().toLowerCase();
		File previousComic = null;
		boolean previous = false;
		for (File candidate : candidates) {
			if (fileName.equals(candidate.getName().toLowerCase())) {
				previous = true;
				break;
			} else {
				previousComic = candidate;
			}
		}
		if (previous) {
			return previousComic;
		} else {
			return null;
		}
	}

	private boolean first() {
		return mScreen.goToScreen(0);
	}

	private boolean isComicLoaded() {
		return (comic != null && comic.getLength() > 0 && !comic.isError());
	}
	
	private boolean last() {
		return mScreen.goToScreen(comic.getLength() - 1);
	}
	
	private void loadComic(final String comicPath, final Intent intent) {
		final File file = new File(comicPath);
		int initialIndex = intent.getIntExtra(POSITION_EXTRA, 0);
		if (initialIndex == 0) {
			initialIndex = HistoryManager.getInstance(this).getBookmark(file);
		}
		loadComic(comicPath, initialIndex);
	}
	
	private void loadComic(final String comicPath) {
		final File file = new File(comicPath);
		final int initialIndex = HistoryManager.getInstance(this).getBookmark(file);
		loadComic(comicPath, initialIndex);
	}
	
	private void loadComic(final String comicPath, final int initialIndex) {
		final File file = new File(comicPath);
		if (file.exists()) {
			mComicPath = comicPath;
			loadComicTask = new LoadComicTask();
			loadComicTask.initialIndex = initialIndex;
			loadComicTask.execute(comicPath);
			
			hideAds();
		}
	}
	
	private boolean next() {
		int index = mScreen.getIndex();
		int frameIndex = mScreen.getFrameIndex();
		if (comic.hasFrames(index) && frameIndex + 1 < comic.getFramesSize(index)) {
			return mScreen.next();
		} else {
			return nextScreen();
		}
	}

	private boolean nextScreen() {
		int index = mScreen.getIndex();
		if (index + 1 >= comic.getLength()) { // Load next comic
			File next = findNextComic();
			if (next != null) {
				this.loadComic(next.getPath(), 0);
				return true;
			}
		} else {
			return mScreen.nextScreen(); 
		}
		return false;
	}
	
	private boolean previous() {
		int index = mScreen.getIndex();
		int frameIndex = mScreen.getFrameIndex();
		if (comic.hasFrames(index) && frameIndex > 0) {
			return mScreen.previous();
		} else {
			return previousScreen();
		}
	}

	private boolean previousScreen() {
		int index = mScreen.getIndex();
		if (index - 1 < 0) { // Load previous comic
			File previous = findPreviousComic();
			if (previous != null) {
				this.loadComic(previous.getPath(), 0);
				return true;
			}
		} else {
			return mScreen.previousScreen();
		}
		return false;
	}

	private void rotate() {
		int orientation = getResources().getConfiguration().orientation;
		int requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		switch (orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
			orientation = Configuration.ORIENTATION_PORTRAIT;
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
			orientation = Configuration.ORIENTATION_LANDSCAPE;
			break;
		}
		Editor editor = preferences.edit();
		editor.putInt(Constants.ORIENTATION_KEY, orientation);
		editor.commit();
		requestedRotation = true;
		setRequestedOrientation(requestedOrientation);
	}

	private void shareApp() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_app_title));
		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app_message));
		Intent chooser = Intent.createChooser(intent, getString(R.string.item_share_app_title));
		startActivity(chooser);
	}
	
	private void shareScreen() {
		ShareViewTask task = new ShareViewTask(this);
		task.setRelativeTempPath(Constants.TEMP_PATH);
		task.setChooserTitle(getString(R.string.item_share_screen_title));
		task.setExtraSubject(getString(R.string.share_screen_title));
		task.setExtraText(getString(R.string.share_screen_message));
		task.setName(comic.getName());
		task.execute(mScreen);
	}

	/**
	 * Shows the menu.
	 */
	private void showMenu() {
		showActionBar();
		openOptionsMenu();
	}
	
	private void startBrowseActivity() {
		if (isComicLoaded()) {
			final Intent intent = new Intent(this, BrowseActivity.class);
			intent.putExtra(BrowseActivity.POSITION_EXTRA, mScreen.getIndex());
			final String comicID = comic.getID();
			intent.putExtra(BrowseActivity.EXTRA_COMIC_ID, comicID);
			startActivityForResult(intent, Constants.SCREEN_BROWSER_CODE);
		}
	}

	private void startSDBrowserActivity() {
		Intent myIntent = new Intent(this, SDBrowserActivity.class);
		String comicsPath = preferences.getString(Constants.COMICS_PATH_KEY, Environment.getExternalStorageDirectory().getAbsolutePath());
		myIntent.putExtra(Constants.COMICS_PATH_KEY, comicsPath);
		startActivityForResult(myIntent, Constants.SD_BROWSER_CODE);
	}
	
	private void startSettingsActivity() {
		if(!isHoneyComb()) {
			startActivityForResult(new Intent(this, SettingsActivityPreHC.class), Constants.SETTINGS_CODE);
		} else {
			startActivityForResult(new Intent(this, SettingsActivityPostHC.class), Constants.SETTINGS_CODE);
		}
	}
	
	private void startSubscribeActivity() {
		Intent myIntent = new Intent(this, SubscribeActivity.class);
		startActivityForResult(myIntent, Constants.SUBSCRIBE_CODE);
	}
	
	private boolean startupOrientation(Bundle savedInstanceState) {
		boolean wasRequestedRotation = savedInstanceState != null ? savedInstanceState.getBoolean(Constants.REQUESTED_ROTATION_KEY) : false;
		if (!wasRequestedRotation) { // If the activity was not created because of a rotation request
			boolean sensor = preferences.getBoolean(Constants.AUTO_ROTATE_KEY, false);
			if (sensor) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			} else {
				int currentOrientation = getResources().getConfiguration().orientation;
				int lastOrientation = preferences.getInt(Constants.ORIENTATION_KEY, currentOrientation);
				int requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
				switch (lastOrientation) {
				case Configuration.ORIENTATION_LANDSCAPE:
					requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
					break;
				case Configuration.ORIENTATION_PORTRAIT:
					requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
					break;
				}
				setRequestedOrientation(requestedOrientation);
				return currentOrientation == lastOrientation;
			}
		}
		return true;
	}
	
	private void trackOpen() {
		TrackingManager.track(Constants.EVENT_OPEN,
				Constants.EVENT_PARAM_TYPE, comic.getType(),
				Constants.AUTO_ROTATE_KEY, String.valueOf(preferences.getBoolean(Constants.AUTO_ROTATE_KEY, false)),
				Constants.DIRECTION_KEY, preferences.getString(Constants.DIRECTION_KEY, Constants.DIRECTION_LEFT_TO_RIGHT_VALUE),
				Constants.SCALE_MODE_KEY, preferences.getString(Constants.SCALE_MODE_KEY, Constants.SCALE_MODE_NONE_VALUE),
				Constants.TRANSITION_MODE_KEY, preferences.getString(Constants.TRANSITION_MODE_KEY, Constants.TRANSITION_MODE_TRANSLATE_VALUE),
				Constants.SHOW_NUMBER_KEY, String.valueOf(preferences.getBoolean(Constants.SHOW_NUMBER_KEY, false)),
				Constants.LOAD_LAST_KEY, String.valueOf(preferences.getBoolean(Constants.LOAD_LAST_KEY, true)),
				Constants.ORIENTATION_KEY, describeOrientation(getResources().getConfiguration().orientation));
	}
	
	
	
	private void loadComicOnStartup(String comicPath) {
		
		if (comicPath != null) {
			loadComic(comicPath);
			return;
		}
		
		final Intent intent = getIntent();
		if (intent.getAction().equals(Intent.ACTION_VIEW)) {
			final Uri uri = intent.getData();
			try {
				final File file = new File(new URI(uri.toString()));
				comicPath = file.getAbsolutePath();
				if (comicPath != null) {
					loadComic(comicPath, intent);
					return;
				}
			} catch (URISyntaxException e) {
				Log.w("loadComicOnStartup", "Invalid intent data");
			}
		}
		
		// Attempt to find comic path in intent
		comicPath = intent.getStringExtra(Constants.COMIC_PATH_LEGACY_KEY); 
		// Compatibility with previous versions of ACV
		if (comicPath == null) {
			comicPath = intent.getStringExtra(Constants.COMIC_PATH_KEY);
		}
		if (comicPath != null) {
			loadComic(comicPath, intent);
			return;
		}

		boolean loadLast = preferences.getBoolean(Constants.LOAD_LAST_KEY, true);
		if (loadLast) {
			comicPath = preferences.getString(Constants.COMIC_PATH_KEY, null);
			if (comicPath != null) {
				loadComic(comicPath);
			}
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Constants.DIALOG_LOAD_ERROR:
			return dialogFactory.createLoadErrorDialog();
		case Constants.DIALOG_PAGE_ERROR:
			return dialogFactory.createPageErrorDialog();
		case Constants.DIALOG_FLIP_CONTROLS:
			return dialogFactory.createFlipControlsDialog();
		}
		return null;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyed = true;
		removePreviousComic(true);
		if (loadComicTask != null) {
			loadComicTask.cancel(true);
		}
		mScreen.destroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (isComicLoaded()) {
			outState.putString(Constants.COMIC_PATH_KEY, comic.getPath());
		}
		outState.putBoolean(Constants.REQUESTED_ROTATION_KEY, requestedRotation);
	}

	protected void removePreviousComic(boolean emptyTemp) {
		// Free the memory of the current comic
		mScreen.recycleBitmaps();
		
		if (emptyTemp) {
			File tempDirectory = new File(Environment.getExternalStorageDirectory(), Constants.TEMP_PATH);
			FileUtils.deleteDirectory(tempDirectory);
		}
		if (comic != null) {
			comic.destroy();
			comic = null;
		}
	}

	public void onAnimationEnd(ComicView comicView) {
		this.adjustCornersVisibility(true);
	}

	public void onAnimationStart(ComicView comicView) {
		this.adjustCornersVisibility(false);
	}

	@Override
	public void onScreenChanged(int index) {
		final String path = comic.getPath();
		HistoryManager.getInstance(this).setBookmark(new File(path), index);
	}
	
	private void showAds() {
		hideAds();
		View ad = AdsManager.getAd(this);
		if(ad != null) {
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			//lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
			lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
			mAdsContainer.addView(ad, lp);
		}
	}
	
	private void hideAds() {
		mAdsContainer.removeAllViews();
	}
	
	@Override
	protected boolean toggleControls() {
		boolean shown = super.toggleControls();
		if(shown) {
			showMenu();
		}
		return shown;
	}
		
	private void showRecentItems() {
		mRecentItemsListAdapter.refresh();
		mRecentItems.setVisibility(View.VISIBLE);
	}
	
	private void hideRecentItems() {
		mRecentItems.setVisibility(View.GONE);
	}
	
}
