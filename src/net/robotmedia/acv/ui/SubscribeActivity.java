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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.androidcomics.acv.R;
import net.robotmedia.acv.logic.ServiceManager;
import net.robotmedia.acv.ui.widget.DialogFactory;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SubscribeActivity extends ExtendedActivity {

	private void initializeWithResources() {
        setContentView(R.layout.subscribe);
        btnCancel = (Button) findViewById(R.id.btn_subcribe_cancel);
        btnSubmit = (Button) findViewById(R.id.btn_subcribe_submit);
        txtEmail = (EditText) findViewById(R.id.txt_subscribe_email);
        emailErrorTitle = R.string.dialog_email_error_title;
        emailErrorMessage = R.string.dialog_email_error_text;
	}
	
	
	protected Button btnCancel;
	protected Button btnSubmit;
	protected EditText txtEmail;
	protected int emailErrorTitle;
	protected int emailErrorMessage;
	public static final int RESULT_ERROR = RESULT_FIRST_USER + 1;
	private String mSource = null;
	public static final String SOURCE_EXTRA = "source";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	initializeWithResources();
        
        Intent intent = getIntent();
        mSource = intent.getStringExtra(SOURCE_EXTRA);
        
        btnCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}});
        btnSubmit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String email = txtEmail.getText().toString();
				
				Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
			    Matcher m = p.matcher(email);
			    if (m.matches()) {
			    	SubscribeTask task = new SubscribeTask();
			    	task.execute(email);
			    } else {
			    	DialogFactory.showSimpleAlert(SubscribeActivity.this, false, emailErrorTitle, emailErrorMessage);
			    }
			}});
        txtEmail.selectAll();
    }

    
	private class SubscribeTask extends AsyncTask<String, Object, Boolean> {

		@Override
		protected void onPreExecute () {
			btnSubmit.setEnabled(false);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			String email = params[0];
			boolean result = ServiceManager.subscribe(email, mSource);
			return result;
		}
		
		protected void onPostExecute (Boolean result) {
			if (result) {
				setResult(RESULT_OK);
			} else {
				setResult(RESULT_ERROR);
			}
			finish();
		}
	}
}
