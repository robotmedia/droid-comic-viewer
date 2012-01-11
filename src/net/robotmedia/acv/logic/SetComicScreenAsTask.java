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
package net.robotmedia.acv.logic;

import java.io.FileNotFoundException;

import net.androidcomics.acv.R;
import net.robotmedia.acv.comic.Comic;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

public class SetComicScreenAsTask extends AsyncTask<Integer, Object, String> {

	private Context mContext;
	private Comic mComic;
	
	public SetComicScreenAsTask(Context context, Comic comic) {
		mContext = context;
		mComic = comic;
	}
	
	@Override
	protected String doInBackground(Integer... params) {
		final int index = params[0];
		Uri uri = mComic.getUri(index);
		String path = uri.getPath();
		try {
			return MediaStore.Images.Media.insertImage(mContext.getContentResolver(), path, mComic.getName(), mComic.getName());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	protected void onPostExecute (String result) {
		if (result != null) {
			Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
			intent.setDataAndType(Uri.parse(result), "image/jpeg");
			intent.putExtra("mimeType", "image/jpeg");
			Intent chooser = Intent.createChooser(intent, mContext.getString(R.string.item_set_as_title));
			mContext.startActivity(chooser);
		}
	}
}
