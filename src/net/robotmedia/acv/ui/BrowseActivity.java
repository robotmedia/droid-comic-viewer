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

import net.androidcomics.acv.R;
import net.robotmedia.acv.comic.Comic;
import net.robotmedia.acv.logic.AdsManager;
import net.robotmedia.acv.logic.PreferencesController;
import net.robotmedia.acv.utils.MathUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class BrowseActivity extends ExtendedActivity {

	private boolean isLeftToRight() {
		return new PreferencesController(this).isLeftToRight();
	}

	protected void initializeWithResources() {
        setContentView(R.layout.pick_screen);
    	layoutNoThumbnail = R.layout.no_thumbnail;
        gallery = (Gallery) findViewById(R.id.gallery);
        editText = (EditText) findViewById(R.id.txt_screen_number);
        button = (Button) findViewById(R.id.btn_screen_browser); 
	}
	
	public final static String POSITION_EXTRA = "position";
	public final static String EXTRA_COMIC_ID = "comic_id";
	
	protected int layoutNoThumbnail;
	protected Gallery gallery;
	protected EditText editText;
	protected Button button;
	protected Comic comic;
		
	   public class ImageAdapter extends BaseAdapter {
	        private Context mContext;
			private LayoutInflater mInflater;
	        
	        public ImageAdapter(Context c) {
	            mContext = c;
	            mInflater = LayoutInflater.from(c);
	        }

	        public int getCount() {
	            return comic.getLength();
	        }

	        public Object getItem(int position) {
	            return position;
	        }

	        public long getItemId(int position) {
	            return position;
	        }
      
	        public View getView(int position, View convertView, ViewGroup parent) {
	        	Drawable thumbnail = comic.getThumbnail(position);
	        	if (thumbnail == null) {
	        		TextView noThumbnail = (TextView) mInflater.inflate(layoutNoThumbnail, null);
	        		int screenNumber = isLeftToRight() ? position + 1 : getCount() - position;
	        		noThumbnail.setText(Integer.toString(screenNumber));
	        		return noThumbnail;
	        	} else {
		            ImageView i = new ImageView(mContext);
		            int screenPosition = isLeftToRight() ? position : getCount() - position - 1;
		            i.setImageDrawable(comic.getThumbnail(screenPosition));
		            int thumbnailWidth = MathUtils.dipToPixel(mContext, THUMBNAIL_WIDTH_DIP);
		            int thumbnailHeight = MathUtils.dipToPixel(mContext, THUMBNAIL_HEIGTH_DIP);
		            i.setLayoutParams(new Gallery.LayoutParams(thumbnailWidth, thumbnailHeight));
		            i.setScaleType(ImageView.ScaleType.FIT_CENTER);
		            return i;
	        	}
	        }
	    }
	
	private static final int THUMBNAIL_WIDTH_DIP = 128;
	private static final int THUMBNAIL_HEIGTH_DIP = 85;
	
	private void returnPosition(int galleryPosition) {
        Intent result = new Intent();
        int screenPosition = isLeftToRight() ? galleryPosition : gallery.getCount() - galleryPosition - 1;
        result.putExtra(POSITION_EXTRA, screenPosition);
        setResult(RESULT_OK, result);
        finish();				
	}
	
	private void updatePosition(int position) {
		editText.setText(String.valueOf(position + 1));
	}
	
	private void updatePosition() {
		int galleryPosition = gallery.getSelectedItemPosition();
		int screenPosition = isLeftToRight() ? galleryPosition : gallery.getCount() - galleryPosition - 1;
		updatePosition(screenPosition);
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		initializeWithResources();
        
        Intent intent = getIntent();
        int position = intent.getIntExtra(POSITION_EXTRA, 0);
        final String comicID = intent.getStringExtra(EXTRA_COMIC_ID);
        if (comicID != null) {
        	comic = Comic.getComic(comicID);
        } else {
        	comic = Comic.getInstance();
        }
        
        ImageAdapter adapter = new ImageAdapter(this);
        gallery.setAdapter(adapter);        
        gallery.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				returnPosition(position);
			}
        	
        });
        int galleryPosition = isLeftToRight() ? position : adapter.getCount() - position - 1;
        gallery.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
				updatePosition();
			}

			public void onNothingSelected(AdapterView<?> arg0) {}

        });
        gallery.setSelection(galleryPosition, true);

       	updatePosition(position);
       
        button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				int position;
				try {
					position = Integer.valueOf(editText.getText().toString());
					position -= 1;
					if (position >= gallery.getCount()) {
						position = gallery.getCount() - 1;
					} else if (position < 0) {
						position = 0;
					}
			        int galleryPosition = isLeftToRight() ? position : gallery.getCount() - position - 1;
			        gallery.setSelection(galleryPosition, true);
			        updatePosition();
				} catch (NumberFormatException e) {
			        updatePosition();
				}
		        returnPosition(gallery.getSelectedItemPosition());
			}
        	
        });
        
        View ad = AdsManager.getAd(this);
        RelativeLayout root = (RelativeLayout) findViewById(R.id.pickScreenRoot);
        if(ad != null) {
        	RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        	lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        	root.addView(ad, lp);
        }
    }
}
