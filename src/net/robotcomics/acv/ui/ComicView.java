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
package net.robotcomics.acv.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.VideoView;
import net.androidcomics.acv.PreferencesController;
import net.androidcomics.acv.R;
import net.robotcomics.ui.AbstractComicView;

public class ComicView extends AbstractComicView {

	public ComicView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
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

	@Override
	protected boolean isLeftToRight() {
		return new PreferencesController(getContext()).isLeftToRight();
	}

}
