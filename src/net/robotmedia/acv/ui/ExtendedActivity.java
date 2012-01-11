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

import java.util.HashSet;

import net.robotmedia.acv.logic.TrackingManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

public class ExtendedActivity extends Activity {

	public void setCanBeKilledByChild(boolean value) {
		this.canBeKilledByChild = value;
	}

	private final static int RESULT_DIE = 777;
	private final static int RESULT_KAMIKAZE = 888;

	private boolean canBeKilledByChild = true;
	
	private HashSet<AsyncTask<?, ?, ?>> mTasks = new HashSet<AsyncTask<?,?,?>>();
	
	@Override
	public void onStart()
	{
	   super.onStart();
	   TrackingManager.onStart(this);
	   TrackingManager.pageView(String.valueOf(this.getTitle()));
	}
	
	@Override
	public void onStop()
	{
	   super.onStop();
	   TrackingManager.onStop(this);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		for (AsyncTask<?, ?, ?> task : mTasks) {
			if (task.getStatus() != AsyncTask.Status.FINISHED) {
				task.cancel(true);
			}
		}
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	if (canBeKilledByChild) {
	        if (resultCode == RESULT_DIE) {
	        	finish();
	        } else if (resultCode == RESULT_KAMIKAZE) {
	        	setResult(RESULT_KAMIKAZE);
	        	finish();
	        }
    	}
    }

}
