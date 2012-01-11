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

public class ACVContent extends ACVRectangle {

	protected final static String PLACEHOLDER_CONTENT = "{content}";
	
	private long transitionDuration;
	
	public String getSource() {
		return source;
	}

	public String getContent() {
		return content;
	}
	
	private String source;
	private String content;
	
	public void setSource(String source) {
		this.source = source;
	}
		
	public void setContent(String content) {
		this.content = content;
	}

	public void setTransitionDuration(long transitionDuration) {
		this.transitionDuration = transitionDuration;
	}

	public long getTransitionDuration() {
		return transitionDuration;
	}
	
	
}
