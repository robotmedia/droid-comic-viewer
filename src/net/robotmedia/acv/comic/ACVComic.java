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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.robotmedia.acv.Constants;
import net.robotmedia.acv.utils.FileUtils;
import net.robotmedia.acv.utils.StringUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebView.PictureListener;
import android.webkit.WebViewClient;

public class ACVComic extends Comic {

	private class ACVZipComic extends ZipComic {

		private TreeMap<String, String> unorderedThumbnails; // Do no initialize; used from super constructor
		
		private TreeMap<String, String> unorderedOriginals; // Do no initialize; used from super constructor
				
		protected ACVZipComic(String path) {
			super(path);
			this.sort(unorderedThumbnails, thumbnails);
			this.sort(unorderedOriginals, originals);
		}
		
		private TreeMap<String, String> classify(String path) {
			{
				final String imagePattern = ACVComic.this.getImageNamePattern();
				if (imagePattern != null) {
					if (path.matches(imagePattern)) {
						return unorderedScreens;
					}
				}
			}
			{
				final String thumbnailPattern = ACVComic.this.getThumbnailNamePattern();
				if (thumbnailPattern != null) {
					if (path.matches(thumbnailPattern)) {
						if (unorderedThumbnails == null) {
							unorderedThumbnails = new TreeMap<String, String>();
						}
						return unorderedThumbnails;
					}
				}
			}
			{
				final String originalPattern = ACVComic.this.getOriginalNamePattern();
				if (originalPattern != null) {
					if (path.matches(originalPattern)) {
						if (unorderedOriginals == null) {
							unorderedOriginals = new TreeMap<String, String>();
						}
						return unorderedOriginals;
					}
				}
			}
			
			// Legacy code
			String[] splitPath = path.split("/");
			if (isLegacyThumbnail(splitPath)) {
				if (unorderedThumbnails == null) {
					unorderedThumbnails = new TreeMap<String, String>();
				}
				return unorderedThumbnails;
			} else {
				return unorderedScreens;				
			}
		}
		/**
		 * 
		 * @param splitEntryName
		 * @return
		 */
		private boolean isLegacyThumbnail(String[] splitEntryName) {
			int i = 0;
			while (i < splitEntryName.length) {
				if (splitEntryName[i] != null
						&& splitEntryName[i].toLowerCase().equals(
								THUMBNAIL_FOLDER.toLowerCase())) {
					return true;
				} else {
					i++;
				}
			}
			return false;
		}
				
		@Override
		protected void processEntry(ZipEntry entry) {
			String entryName = entry.getName();
			if (FileUtils.isHidden(entryName)) {
				return;
			}
			String extension = FileUtils.getFileExtension(entryName);
			if (FileUtils.isImage(extension)) {
				final String key = this.addLeadingZeroes(entryName);
				TreeMap<String, String> bucket = classify(entryName);
				bucket.put(key, entryName);
			} else if (FileUtils.isVideo(extension)) {
				String[] split = entryName.split("\\.");
				if (split.length > 1) {
					String numberSuffix = split[split.length - 2];
					Pattern pattern = Pattern.compile("\\d+$");
					Matcher matcher = pattern.matcher(numberSuffix);
					if (matcher.find()) {
						numberSuffix = matcher.group();
						int index = Integer.parseInt(numberSuffix);
						File file = extract(entry, entry.getName());
						if (file != null) {
							ACVScreen screen = getOrCreateACVScreen(index);
							screen.setVideoFile(file);
						}
					}
				}
			} else if (FileUtils.isAudio(extension) || FileUtils.isFont(entryName) || FileUtils.isWebpage(entryName)) {
				final File file = extract(entry, entry.getName());
				ACVComic.this.addFile(file.getName(), file);
			}
		}
		
		protected void processMetadata(ZipEntry entry) {
			InputStream is = null;
			try {
				is = getInputStream(entry);
		        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		        XmlPullParser xml = factory.newPullParser();
		        xml.setInput(is, null);
		        ACVParser.parse(xml, ACVComic.this);
				is.close();
			} catch (Exception e) {
				this.error();
				e.printStackTrace();
				if (is != null) {
					try { is.close(); } catch (Exception e1) {}
				}
			}
		}
		
		@Override
		protected void setZip(ZipFile zip) {
			super.setZip(zip);
			ZipEntry entry = zip.getEntry(METADATA_FILE);
			if (entry == null) {
				entry = zip.getEntry(LEGACY_METADATA_FILE);
			}
			if (entry != null) {
				processMetadata(entry);
			}
		}
		
		private void sort(TreeMap<String, String> unordered, ArrayList<String> ordered) {
			if (unordered != null) {
				ArrayList<String> orderedKeys = new ArrayList<String>(unordered.keySet());				
				for (int i = 0; i < orderedKeys.size(); i++) {
					ordered.add(unordered.get(orderedKeys.get(i)));
				}
			}
		}

	}
	public static final String THUMBNAIL_FOLDER = "z_tn";
	public static final String COMIC_URI_PREFIX = "comic://";
	@Deprecated
	protected static final String LEGACY_METADATA_FILE = "comic.xml";
	protected static final String METADATA_FILE = "acv.xml";
	
	private ACVZipComic innerComic;
	private ArrayList<String> thumbnails = new ArrayList<String>();
	
	private ArrayList<String> originals = new ArrayList<String>();
		
	public ACVComic(String comicPath) {
		super(comicPath);
		innerComic = new ACVZipComic(path);
		init();
	}
	
	@Override
	public void destroy() {
		innerComic.destroy();
	} 
						
	public String getContentBaseURL() {
		final File dir = new File(Environment.getExternalStorageDirectory(), this.getRelativePath());
		return Uri.fromFile(dir).toString() + "/";
	}

	protected InputStream getInputStream(String resource) {
		try {
			return new FileInputStream(this.getFile(resource));
		} catch (FileNotFoundException e) {
			Log.w(this.getClass().getSimpleName(), "Resource not found", e);
			return null;
		}
	}

	@Override
	public int getLength() {
		return innerComic.getLength();
	}

	@Override
	public Drawable getScreen(int position) {
		return innerComic.getScreen(position);
	}

	@Override
	public Drawable getThumbnail(int position) {
		if (position < thumbnails.size()) {
			String entryName = thumbnails.get(position);
			InputStream is = null;
			Drawable drawable = null;
			try {
				is = innerComic.getInputStream(entryName);
				drawable = Drawable.createFromStream(is, entryName);
				is.close();
			} catch (Exception e) {
				if (is != null) {
					try { is.close(); } catch (Exception e1) {}
				}
			}
			return drawable;
		} else {
			return null;
		}
	}
	
	@Override
	public Uri getUri(int position) {
		return innerComic.getUri(position);
	}

	private void init() {
		if (this.getID() == null) {
			final String fileName = new File(path).getName();
			this.setId(fileName);
		}
	}

	@Override
	public void prepareScreen(int position) {
		innerComic.prepareScreen(position);
	}
	
	public static class Message {
		public int index;
		public String text;
		public String uri;
		public String nonMarketUri;
	}

	protected static final String VALUE_NONE = "none";
	protected static final String VALUE_LANDSCAPE = "landscape";

	protected static final String VALUE_PORTRAIT = "portrait";
	protected static final int NOT_SPECIFIED = -1;
	private int specificationVersion = 1;

	private int minViewerVersion = NOT_SPECIFIED;
	private HashMap<Integer, ACVScreen> acvScreens = new HashMap<Integer, ACVScreen>();
	private String bgcolorString;
	private String scaleMode = Constants.SCALE_MODE_BEST_VALUE;
	private String defaultTransition = Constants.TRANSITION_MODE_NONE_VALUE;
	private String imageNamePattern;
	private String thumbnailNamePattern;
	private String originalNamePattern;

	private HashMap<String, File> files = new HashMap<String, File>();

	private String direction = Constants.DIRECTION_LEFT_TO_RIGHT_VALUE;

	private boolean paid;

	private int orientation = Configuration.ORIENTATION_LANDSCAPE;

	protected void addFile(String name, File file) {
		files.put(name, file);
	}

	public ACVScreen getACVScreen(int index) {
		return acvScreens.get(index);
	}

	@Override
	public Integer getBackgroundColor(int index) {
		ACVScreen screen = acvScreens.get(index);
		String screenBgcolorString = null;
		if (screen != null) {
			screenBgcolorString = screen.getBgcolorString();
		}
		if (screenBgcolorString == null) {
			screenBgcolorString = bgcolorString;
		}
		if (VALUE_NONE.equals(screenBgcolorString)) {
			return Color.TRANSPARENT;
		} else if (screenBgcolorString != null) {
			return Color.parseColor(screenBgcolorString);
		} else {
			return Color.TRANSPARENT;
		}
	}

	public Integer getBackgroundColor(int screenIndex, int frameIndex) {
		ACVScreen screen = acvScreens.get(screenIndex);
		if (screen != null) {
			ACVFrame frame = screen.getFrame(frameIndex);
			if (frame != null) {
				String frameBgcolorString = frame.getBgcolorString();
				if (frameBgcolorString != null) {
					if (VALUE_NONE.equals(frameBgcolorString)) {
						return null;
					} else {
						return Color.parseColor(frameBgcolorString);
					}
				}
			}
		}
		return getBackgroundColor(screenIndex);
	}

	private Bitmap getBitmap(ACVContent content, final WebView w, int containerWidth, int containerHeight) {
		final Rect rect = content.createRect(containerWidth, containerHeight);
		final CountDownLatch signal = new CountDownLatch(1);
		final Bitmap b = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.RGB_565);
		final AtomicBoolean ready = new AtomicBoolean(false);
		final String html = this.getContentFromSource(content);
		final String baseURL = this.getContentBaseURL();
		w.post(new Runnable() {

			@Override
			public void run() {
				w.setWebViewClient(new WebViewClient() {
					@Override
					public void onPageFinished(WebView view, String url) {
						ready.set(true);
					}
				});
				w.setPictureListener(new PictureListener() {
					@Override
					public void onNewPicture(WebView view, Picture picture) {
						if (ready.get()) {
							final Canvas c = new Canvas(b);
							view.draw(c);
							w.setPictureListener(null);
							signal.countDown();
						}
					}
				});
				w.layout(0, 0, rect.width(), rect.height());
				w.loadDataWithBaseURL(baseURL, html, "text/html", "UTF-8", null);
			}
		});
		try {
			signal.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return b;
	}

	public String getContentFromSource(ACVContent content) {
		final String source = content.getSource();
		final String simpleContent = content.getContent();
		if (source == null)
			return simpleContent;
		final InputStream is = this.getInputStream(source);
		if (is == null)
			return simpleContent;
		try {
			String contentFromSource = StringUtils.convertStreamToString(is, "UTF-8");
			if (simpleContent != null) {
				contentFromSource = contentFromSource.replaceFirst(Pattern.quote(ACVContent.PLACEHOLDER_CONTENT), simpleContent);
			}
			return contentFromSource;
		} catch (Exception e) {
			Log.w(this.getClass().getSimpleName(), "Could not read content source", e);
			return simpleContent;
		}
	}

	public List<ACVContent> getContents(int screenIndex) {
		final ACVScreen screen = acvScreens.get(screenIndex);
		return screen != null ? screen.getContents() : new ArrayList<ACVContent>();
	}

	public List<ACVContent> getContents(int screenIndex, int frameIndex) {
		final ACVFrame frame = getFrame(screenIndex, frameIndex);
		return frame != null ? frame.getContents() : new ArrayList<ACVContent>();
	}

	public String getDescription(int index) {
		final ACVScreen screen = acvScreens.get(index);
		return screen != null ? screen.getDescription() : null;
	}

	public String getDirection() {
		return direction;
	}

	public long getDuration(int index) {
		final ACVScreen screen = getACVScreen(index);
		return screen != null ? screen.getDuration() : Math.round(ACVScreen.DEFAULT_DURATION * 1000);
	}

	public long getDuration(int screenIndex, int frameIndex) {
		float durationInSeconds = ACVFrame.DEFAULT_DURATION;
		ACVScreen screen = acvScreens.get(screenIndex);
		if (screen != null) {
			ACVFrame frame = screen.getFrame(frameIndex);
			if (frame != null) {
				durationInSeconds = frame.getDuration();
			}
		}
		long duration = Math.round(durationInSeconds * 1000);
		return duration;
	}

	protected File getFile(String name) {
		return files.get(name);
	}

	private ACVFrame getFrame(int screenIndex, int frameIndex) {
		ACVScreen screen = acvScreens.get(screenIndex);
		if (screen != null) {
			return screen.getFrame(frameIndex);
		} else {
			return null;
		}
	}

	@Override
	public int getFramesSize(int index) {
		ACVScreen screen = acvScreens.get(index);
		if (screen != null) {
			return screen.framesSize();
		}
		return 0;
	}

	protected String getImageNamePattern() {
		return imageNamePattern;
	}

	public Message getMessage(int index) {
		ACVScreen screen = acvScreens.get(index);
		return screen != null ? screen.getMessage() : null;
	}

	public ACVScreen getOrCreateACVScreen(int index) {
		if (acvScreens == null) {
			acvScreens = new HashMap<Integer, ACVScreen>();
		}
		ACVScreen screen = acvScreens.get(index);
		if (screen == null) {
			screen = new ACVScreen(index);
		}
		acvScreens.put(index, screen);
		return screen;
	}

	public int getOrientation() {
		return orientation;
	}

	protected String getOriginalNamePattern() {
		return originalNamePattern;
	}

	@Override
	public String getScaleMode() {
		return scaleMode;
	}

	public Drawable getScreenWithContents(WebView w, int screenIndex) {
		final List<ACVContent> contents = this.getContents(screenIndex);
		final Drawable drawable = this.getScreen(screenIndex);
		if (contents.size() == 0) {
			return drawable;
		}
		// TODO: Cache modified bitmap
		final Bitmap original = ((BitmapDrawable) drawable).getBitmap();
		final Bitmap result = original.copy(Config.RGB_565, true);
		original.recycle();
		final int width = result.getWidth();
		final int height = result.getHeight();
		final Canvas canvas = new Canvas(result);
		for (ACVContent content : contents) {
			final Rect contentRect = content.createRect(width, height);
			final Bitmap contentBitmap = getBitmap(content, w, width, height);
			canvas.drawBitmap(contentBitmap, contentRect.left, contentRect.top, null);
			contentBitmap.recycle();
		}
		return new BitmapDrawable(result);
	}

	public File getSound(int index) {
		final ACVScreen screen = acvScreens.get(index);
		if (screen != null) {
			final String soundName = screen.getSound();
			if (soundName != null) {
				return files.get(soundName);
			}
		}
		return null;
	}

	public File getSound(int screenIndex, int frameIndex) {
		final ACVScreen screen = acvScreens.get(screenIndex);
		if (screen != null) {
			final ACVFrame frame = screen.getFrame(frameIndex);
			if (frame != null) {
				final String soundName = frame.getSound();
				if (soundName != null) {
					return files.get(soundName);
				}
			}
		}
		return null;
	}
	
	protected String getThumbnailNamePattern() {
		return thumbnailNamePattern;
	}

	public ArrayList<ACVScreen> getTitledScreens() {
		ArrayList<ACVScreen> titledScreens = new ArrayList<ACVScreen>();
		final int length = getLength();
		for (int i = 0; i < length; i++) {
			ACVScreen screen = acvScreens.get(i);
			if (screen != null && screen.getTitle() != null && screen.getTitle().trim().length() > 0) {
				titledScreens.add(screen);
			}
		}
		return titledScreens;
	}

	public String getTransition(int index) {
		// Legacy code. The first version of the ACV specification defined the
		// transition to a screen in its previous screen.
		if (specificationVersion == 1) {
			index -= 1;
		}
		ACVScreen screen = acvScreens.get(index);
		if (screen != null) {
			String transition = screen.getTransition();
			if (transition != null) {
				return transition;
			}
		}
		return defaultTransition;
	}

	public String getTransition(int screenIndex, int frameIndex) {
		final ACVScreen screen = getACVScreen(screenIndex);
		return screen != null ? screen.getTransition(frameIndex) : null;
	}

	public long getTransitionDuration(int index) {
		final ACVScreen screen = getACVScreen(index);
		return screen != null ? screen.getTransitionDuration() : Math.round(ACVScreen.DEFAULT_TRANSITION_DURATION * 1000);
	}

	public long getTransitionDuration(int screenIndex, int frameIndex) {
		float transitionDurationInSeconds = ACVFrame.DEFAULT_TRANSITION_DURATION;
		ACVScreen screen = acvScreens.get(screenIndex);
		if (screen != null) {
			ACVFrame frame = screen.getFrame(frameIndex);
			if (frame != null) {
				transitionDurationInSeconds = frame.getTransitionDuration();
			}
		}
		long transitionDuration = Math.round(transitionDurationInSeconds * 1000);
		return transitionDuration;
	}

	public File getVideoFile(int index) {
		ACVScreen screen = acvScreens.get(index);
		return screen != null ? screen.getVideoFile() : null;
	}

	public boolean hasVibration(int index) {
		ACVScreen screen = acvScreens.get(index);
		return screen != null ? screen.isVibrate() : false;
	}

	public boolean isAutoplay(int screenIndex) {
		ACVScreen screen = acvScreens.get(screenIndex);
		return screen != null ? screen.isAutoplay() : false;
	}

	public boolean isAutoplay(int screenIndex, int frameIndex) {
		ACVScreen screen = acvScreens.get(screenIndex);
		if (screen != null) {
			ACVFrame frame = screen.getFrame(frameIndex);
			if (frame != null) {
				return frame.isAutoplay();
			}
		}
		return ACVFrame.DEFAULT_AUTOPLAY;
	}

	@Override
	public boolean isCompatible(int version) {
		if (minViewerVersion == NOT_SPECIFIED) {
			return true;
		} else {
			return minViewerVersion <= version;
		}
	}

	public boolean isLeftToRight() {
		return Constants.DIRECTION_LEFT_TO_RIGHT_VALUE.equals(direction);
	}

	public boolean isPaid() {
		return paid;
	}

	public boolean isVibrate(int screenIndex, int frameIndex) {
		ACVScreen screen = acvScreens.get(screenIndex);
		if (screen != null) {
			ACVFrame frame = screen.getFrame(frameIndex);
			if (frame != null) {
				return frame.isVibrate();
			}
		}
		return ACVFrame.DEFAULT_VIBRATE;
	}

	public Rect rectForSize(int screenIndex, int frameIndex, int width, int height) {
		final ACVFrame frame = getFrame(screenIndex, frameIndex);
		return frame != null ? frame.createRect(width, height) : null;
	}

	public void registerSound(String sound) {
		// Do nothing;
	}

	public void setBgcolorString(String bgcolorString) {
		this.bgcolorString = bgcolorString;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	protected void setImageNamePattern(String imageNamePattern) {
		this.imageNamePattern = imageNamePattern;
	}

	public void setImageStartsAt(int starstAt) {
		// TODO Auto-generated method stub

	}

	public void setLength(int length) {
		// Do nothing
	}

	public void setMinViewerVersion(int minViewerVersion) {
		this.minViewerVersion = minViewerVersion;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}

	protected void setOriginalNamePattern(String originalNamePattern) {
		this.originalNamePattern = originalNamePattern;
	}

	public void setPaid(boolean paid) {
		this.paid = paid;
	}

	public void setScaleMode(String scaleMode) {
		this.scaleMode = scaleMode;
	}

	public void setSpecificationVersion(int specificationVersion) {
		this.specificationVersion = specificationVersion;
	}

	protected void setThumbnailNamePattern(String thumbnailNamePattern) {
		this.thumbnailNamePattern = thumbnailNamePattern;
	}

	public void setThumbnailStartsAt(int startsAt) {
		// TODO Auto-generated method stub
	}

	public void setTitle(String title) {
		this.name = title;
	}

	public void setTransition(String transition) {
		this.defaultTransition = transition;
	}
	
}


