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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.robotmedia.acv.comic.ACVComic.Message;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.Configuration;

public class ACVParser {

	private static final String ATTRIBUTE_AUTOPLAY = "autoplay";
	private static final String ATTRIBUTE_BGCOLOR = "bgcolor";
	private static final String ATTRIBUTE_DIRECTION = "direction";
	private static final String ATTRIBUTE_DURATION = "duration";
	private static final String ATTRIBUTE_ID = "id";
	private static final String ATTRIBUTE_INDEX = "index";

	private static final String ATTRIBUTE_IMAGE_NAME_PATTERN = "imageNamePattern";
	private static final String ATTRIBUTE_THUMBNAIL_NAME_PATTERN = "thumbnailNamePattern";
	private static final String ATTRIBUTE_ORIGINAL_NAME_PATTERN = "originalNamePattern";
	
	private static final String ATTRIBUTE_LENGTH = "length";
	private static final String ATTRIBUTE_MIN_VIEWER_VERSION = "minViewerVersion";
	private static final String ATTRIBUTE_ORIENTATION = "orientation";
	private static final String ATTRIBUTE_PAID = "paid";
	private static final String ATTRIBUTE_RELATIVE_AREA = "relativeArea";
	private static final String ATTRIBUTE_SCALE_MODE = "scaleMode";
	private static final String ATTRIBUTE_SOUND = "sound";
	private static final String ATTRIBUTE_SOURCE = "src";
	private static final String ATTRIBUTE_STARTS_AT = "startAt";
	private static final String ATTRIBUTE_TITLE = "title";
	private static final String ATTRIBUTE_TRANSITION = "transition";
	private static final String ATTRIBUTE_TRANSITION_DURATION = "transitionDuration";
	private static final String ATTRIBUTE_URI = "uri";
	private static final String ATTRIBUTE_VALUE = "value";
	private static final String ATTRIBUTE_VERSION = "version";
	private static final String ATTRIBUTE_VIBRATE = "vibrate";
	@Deprecated
	private static final String ELEMENT_COMIC = "comic";
	private static final String ELEMENT_ACV = "acv";
	private static final String ELEMENT_CONTENT = "content";
	private static final String ELEMENT_DESCRIPTION = "description";
	private static final String ELEMENT_FRAME = "frame";
	@Deprecated
	private static final String ELEMENT_IMAGE = "image";
	private static final String ELEMENT_IMAGES = "images";
	private static final String ELEMENT_MESSAGE = "message";
	private static final String ELEMENT_SCREEN = "screen";
	private static final String ELEMENT_THUMBNAILS = "thumbnails";
	@Deprecated
	private static final String VALUE_YES = "yes";

	public static void parse(XmlPullParser xml, ACVComic comic) throws XmlPullParserException, IOException {
		int eventType = xml.getEventType();
		boolean isParsingScreen = false;
		boolean isParsingFrame = false;
		boolean isParsingDescription = false;
		boolean isParsingContent = false;
		ACVScreen currentScreen = null;
		ACVFrame currentFrame = null;
		ACVContent currentContent = null;
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				String elementName = xml.getName();
				if (ELEMENT_COMIC.equals(elementName) || ELEMENT_ACV.equals(elementName)) {
					parseComic(xml, comic);
				} else if (ELEMENT_IMAGES.equals(elementName)) {
					parseImagePattern(xml, comic);
				} else if (ELEMENT_THUMBNAILS.equals(elementName)) {
					parseThumbnailPattern(xml, comic);
				} else if (ELEMENT_IMAGE.equals(elementName) || ELEMENT_SCREEN.equals(elementName)) {
					isParsingScreen = true;
					currentScreen = parseScreen(xml, comic);
				} else if (ELEMENT_MESSAGE.equals(elementName)) {
					Message message = ACVParser.parseMessage(xml);
					if (message != null) {
						ACVScreen screen = comic.getOrCreateACVScreen(message.index);
						screen.setMessage(message);
					}
				} else if (isParsingScreen && currentScreen != null && ELEMENT_FRAME.equals(elementName)) {
					isParsingFrame = true;
					currentFrame = parseFrame(xml, comic);
					currentScreen.addFrame(currentFrame);
				} else if (ELEMENT_DESCRIPTION.equalsIgnoreCase(elementName)) {
					isParsingDescription = true;
					final String description = xml.getText();
					if (isParsingScreen) {
						currentScreen.setDescription(description);
					} else if (isParsingFrame) {
						currentFrame.setDescription(description);
					} else { // Comic description
						comic.setDescription(description);
					}
				} else if (ELEMENT_CONTENT.equalsIgnoreCase(elementName)) {
					isParsingContent = true;
					currentContent = parseContent(xml);
				}
			} else if (eventType == XmlPullParser.TEXT) {
				if (isParsingDescription) {
					final String description = xml.getText();
					if (isParsingScreen) {
						currentScreen.setDescription(description);
					}
				} else if (isParsingContent) {
					currentContent.setContent(xml.getText());
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				String elementName = xml.getName();
				if (ELEMENT_IMAGE.equals(elementName) || ELEMENT_SCREEN.equals(elementName)) {
					isParsingScreen = false;
					currentScreen = null;
				} else if (ELEMENT_FRAME.equalsIgnoreCase(elementName)) {
					isParsingFrame = false;
					currentFrame = null;
				} else if (ELEMENT_DESCRIPTION.equalsIgnoreCase(elementName)) {
					isParsingDescription = false;
				} else if (ELEMENT_CONTENT.equalsIgnoreCase(elementName)) {
					isParsingContent = false;
					if (isParsingFrame) {
						currentFrame.add(currentContent);
					} else if (isParsingScreen) {
						currentScreen.add(currentContent);
					}
					currentContent = null;
				}
			}
			eventType = xml.next();
		}
	}

	private static ACVContent parseContent(XmlPullParser xml) {
		final ACVContent content = new ACVContent();

		final String relativeAreaString = xml.getAttributeValue(null, ATTRIBUTE_RELATIVE_AREA);
		parseRelativeArea(relativeAreaString, content);

		final String source = xml.getAttributeValue(null, ATTRIBUTE_SOURCE);
		if (source != null) {
			content.setSource(source);
		}
		
		final String transitionDurationString = xml.getAttributeValue(null, ATTRIBUTE_TRANSITION_DURATION);
		if (transitionDurationString != null) {
			final long transitionDuration = (long) (Float.parseFloat(transitionDurationString) * 1000);
			content.setTransitionDuration(transitionDuration);
		}

		return content;
	}

	private static boolean parseBoolean(String value) {
		return VALUE_YES.equals(value.toLowerCase()) || Boolean.parseBoolean(value);
	}

	private static void parseComic(XmlPullParser xml, ACVComic comic) {
		final String bgcolorString = xml.getAttributeValue(null, ATTRIBUTE_BGCOLOR);
		if (bgcolorString != null) {
			comic.setBgcolorString(bgcolorString);
		}

		final String paidString = xml.getAttributeValue(null, ATTRIBUTE_PAID);
		if (paidString != null) {
			final boolean paid = parseBoolean(paidString);
			comic.setPaid(paid);
		}

		final String scaleMode = xml.getAttributeValue(null, ATTRIBUTE_SCALE_MODE);
		if (scaleMode != null) {
			comic.setScaleMode(scaleMode);
		}

		final String transition = xml.getAttributeValue(null, ATTRIBUTE_TRANSITION);
		if (transition != null) {
			comic.setTransition(transition);
		}

		final String specificationVersionString = xml.getAttributeValue(null, ATTRIBUTE_VERSION);
		if (specificationVersionString != null) {
			final int specificationVersion = Math.round(Float.parseFloat(specificationVersionString));
			comic.setSpecificationVersion(specificationVersion);
		}

		final String minViewerVersionString = xml.getAttributeValue(null, ATTRIBUTE_MIN_VIEWER_VERSION);
		if (minViewerVersionString != null) {
			final int minViewerVersion = Integer.parseInt(minViewerVersionString);
			comic.setMinViewerVersion(minViewerVersion);
		}

		final String title = xml.getAttributeValue(null, ATTRIBUTE_TITLE);
		if (title != null) {
			comic.setTitle(title);
		}

		final String id = xml.getAttributeValue(null, ATTRIBUTE_ID);
		if (id != null) {
			comic.setId(id);
		}

		final String direction = xml.getAttributeValue(null, ATTRIBUTE_DIRECTION);
		if (direction != null) {
			comic.setDirection(direction);
		}

		final String orientationString = xml.getAttributeValue(null, ATTRIBUTE_ORIENTATION);
		if (orientationString != null) {
			final int orientation;
			if (ACVComic.VALUE_PORTRAIT.equalsIgnoreCase(orientationString)) {
				orientation = Configuration.ORIENTATION_PORTRAIT;
			} else if (ACVComic.VALUE_LANDSCAPE.equalsIgnoreCase(orientationString)) {
				orientation = Configuration.ORIENTATION_LANDSCAPE;
			} else {
				orientation = Integer.parseInt(orientationString);
			}
			comic.setOrientation(orientation);
		}
		
		final String lengthString = xml.getAttributeValue(null, ATTRIBUTE_LENGTH);
		if (lengthString != null) {
			final int length = Integer.parseInt(lengthString);
			comic.setLength(length);
		}

		{
			final String imageNamePattern = xml.getAttributeValue(null, ATTRIBUTE_IMAGE_NAME_PATTERN);
			if (imageNamePattern != null) {
				comic.setImageNamePattern(toRegex(imageNamePattern));
			}
		}
		{
			final String thumbnailNamePattern = xml.getAttributeValue(null, ATTRIBUTE_THUMBNAIL_NAME_PATTERN);
			if (thumbnailNamePattern != null) {
				comic.setThumbnailNamePattern(toRegex(thumbnailNamePattern));
			}
		}
		{
			final String originalNamePattern = xml.getAttributeValue(null, ATTRIBUTE_ORIGINAL_NAME_PATTERN);
			if (originalNamePattern != null) {
				comic.setOriginalNamePattern(toRegex(originalNamePattern));
			}
		}
	}

	private static ACVFrame parseFrame(XmlPullParser xml, ACVComic comic) {
		ACVFrame frame = new ACVFrame();

		String relativeAreaString = xml.getAttributeValue(null, ATTRIBUTE_RELATIVE_AREA);
		parseRelativeArea(relativeAreaString, frame);

		String autoplayString = xml.getAttributeValue(null, ATTRIBUTE_AUTOPLAY);
		if (autoplayString != null) {
			final boolean autoplay = parseBoolean(autoplayString);
			frame.setAutoplay(autoplay);
		}

		String transition = xml.getAttributeValue(null, ATTRIBUTE_TRANSITION);
		if (transition != null) {
			frame.setTransition(transition);
		}

		String transitionDurationString = xml.getAttributeValue(null, ACVParser.ATTRIBUTE_TRANSITION_DURATION);
		if (transitionDurationString != null) {
			final float transitionDuration = Float.parseFloat(transitionDurationString);
			frame.setTransitionDuration(transitionDuration);
		}

		String durationString = xml.getAttributeValue(null, ATTRIBUTE_DURATION);
		if (durationString != null) {
			final float duration = Float.parseFloat(durationString);
			frame.setDuration(duration);
		}

		String vibrateString = xml.getAttributeValue(null, ATTRIBUTE_VIBRATE);
		if (vibrateString != null) {
			final boolean vibrate = parseBoolean(vibrateString);
			frame.setVibrate(vibrate);
		}

		final String bgcolorString = xml.getAttributeValue(null, ATTRIBUTE_BGCOLOR);
		if (bgcolorString != null) {
			frame.setBgcolorString(bgcolorString);
		}

		final String sound = xml.getAttributeValue(null, ATTRIBUTE_SOUND);
		if (sound != null) {
			frame.setSound(sound);
			comic.registerSound(sound);
		}

		return frame;
	}

	private static void parseImagePattern(XmlPullParser xml, ACVComic comic) {
		final String lengthString = xml.getAttributeValue(null, ATTRIBUTE_LENGTH);
		if (lengthString != null) {
			final int length = Integer.parseInt(lengthString);
			comic.setLength(length);
		}

		final String startsAtString = xml.getAttributeValue(null, ATTRIBUTE_STARTS_AT);
		if (startsAtString != null) {
			final int starstAt = Integer.parseInt(startsAtString);
			comic.setImageStartsAt(starstAt);
		}
	}

	private static Message parseMessage(XmlPullParser xml) {
		String aux = xml.getAttributeValue(null, ATTRIBUTE_INDEX);
		if (aux != null) {
			int index = Integer.parseInt(aux);
			Message message = new Message();
			message.index = index;
			message.text = xml.getAttributeValue(null, ATTRIBUTE_VALUE);
			message.uri = xml.getAttributeValue(null, ATTRIBUTE_URI);
			message.nonMarketUri = xml.getAttributeValue(null, "nonMarketUri");
			return message;
		}
		return null;
	}

	private static void parseRelativeArea(String relativeAreaString, ACVRectangle r) {
		if (relativeAreaString != null) {
			final String[] relativeAreaArray = relativeAreaString.split(" ");
			if (relativeAreaArray.length == 4) {
				final float relativeX = Float.parseFloat(relativeAreaArray[0]);
				final float relativeY = Float.parseFloat(relativeAreaArray[1]);
				final float relativeWidth = Float.parseFloat(relativeAreaArray[2]);
				final float relativeHeight = Float.parseFloat(relativeAreaArray[3]);
				r.setArea(relativeX, relativeY, relativeWidth, relativeHeight);
			}
		}
	}

	private static ACVScreen parseScreen(XmlPullParser xml, ACVComic comic) {
		int index = -1;
		String aux = xml.getAttributeValue(null, ATTRIBUTE_INDEX);
		if (aux != null)
			index = Integer.parseInt(aux);
		if (index >= 0) {
			ACVScreen screen = comic.getOrCreateACVScreen(index);

			final String autoplayString = xml.getAttributeValue(null, ATTRIBUTE_AUTOPLAY);
			if (autoplayString != null) {
				final boolean autoplay = parseBoolean(autoplayString);
				screen.setAutoplay(autoplay);
			}
			
			final String transition = xml.getAttributeValue(null, ATTRIBUTE_TRANSITION);
			if (transition != null) {
				screen.setTransition(transition);
			}

			final String bgcolorString = xml.getAttributeValue(null, ATTRIBUTE_BGCOLOR);
			if (bgcolorString != null) {
				screen.setBgcolorString(bgcolorString);
			}

			final String vibrateString = xml.getAttributeValue(null, ATTRIBUTE_VIBRATE);
			if (vibrateString != null) {
				final boolean vibrate = parseBoolean(vibrateString);
				screen.setVibrate(vibrate);
			}

			final String transitionDurationString = xml
					.getAttributeValue(null, ACVParser.ATTRIBUTE_TRANSITION_DURATION);
			if (transitionDurationString != null) {
				final float transitionDuration = Float.parseFloat(transitionDurationString);
				screen.setTransitionDuration(transitionDuration);
			}

			final String durationString = xml.getAttributeValue(null, ATTRIBUTE_DURATION);
			if (durationString != null) {
				final float duration = Float.parseFloat(durationString);
				screen.setDuration(duration);
			}

			final String title = xml.getAttributeValue(null, ATTRIBUTE_TITLE);
			if (title != null) {
				screen.setTitle(title);
			}

			final String sound = xml.getAttributeValue(null, ATTRIBUTE_SOUND);
			if (sound != null) {
				screen.setSound(sound);
				comic.registerSound(sound);
			}
			return screen;
		}
		return null;
	}

	private static void parseThumbnailPattern(XmlPullParser xml, ACVComic comic) {
		final String startsAtString = xml.getAttributeValue(null, ATTRIBUTE_STARTS_AT);
		if (startsAtString != null) {
			final int startsAt = Integer.parseInt(startsAtString);
			comic.setThumbnailStartsAt(startsAt);
		}
	}
	
	/**
	 * Converts a format string with a single decimal integer parameter (with zero-padding) into the corresponding regex.
	 * @param format format string with a single decimal integer parameter (with zero-padding)
	 * @return the corresponding regex
	 */
	private static String toRegex(String format) {
		final Pattern indexPattern = Pattern.compile("(.*)%0(\\d)d(.*)");
		final Matcher m = indexPattern.matcher(format);
		if (m.find()) {
			StringBuffer regexBuffer = new StringBuffer(m.group(1));
			final String digits = m.group(2);
			regexBuffer.append("\\d{").append(digits).append(",}+");
			regexBuffer.append(m.group(3));
			return regexBuffer.toString();
		}
		return format;
	}

}
