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
package net.robotmedia.acv.ui.settings;

import net.androidcomics.acv.R;
import net.robotmedia.acv.Constants;
import net.robotmedia.acv.ui.SubscribeActivity;
import net.robotmedia.acv.ui.widget.DialogFactory;
import net.robotmedia.acv.utils.AlertUtils;
import net.robotmedia.acv.utils.IntentUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

public class SupportSettingsFragment extends ExtendedPreferenceFragment {

	private static final String KEY_REPORT_PROBLEM = "report_problem";
	private static final String KEY_SUBSCRIBE = "subscribe";
	private static final String SOURCE_VALUE = "DroidComicViewer";

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.SUBSCRIBE_CODE) {
			switch (resultCode) {
			case -1:
				DialogFactory.showSimpleAlert(this.getActivity(), true, R.string.dialog_subscribe_success_title, R.string.dialog_subscribe_success_text);
				break;
			case SubscribeActivity.RESULT_ERROR:
				DialogFactory.showSimpleAlert(this.getActivity(), false, R.string.dialog_subscribe_error_title, R.string.dialog_subscribe_error_text);
				break;
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.support_settings);
		
		final Preference subscribe = findPreference(KEY_SUBSCRIBE);
		subscribe.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Intent myIntent = new Intent(SupportSettingsFragment.this.getActivity(), SubscribeActivity.class);
				myIntent.putExtra(SubscribeActivity.SOURCE_EXTRA, SOURCE_VALUE);
				startActivityForResult(myIntent, Constants.SUBSCRIBE_CODE);
				return true;
			}
		});
		
		final Preference pReportProblem = findPreference(KEY_REPORT_PROBLEM);
		pReportProblem.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				reportProblem();
				return true;
			}
		});
		
	}
	
	private static final String ACTION_REPORT_BUG = "net.robotmedia.bugreporter.intent.action.REPORT_BUG";
	private static final String EXTRA_PACKAGE = "net.robotmedia.supportrequest.intent.extra.package";
	private static final String EXTRA_EMAIL = "net.robotmedia.supportrequest.intent.extra.email";
	private static final String EMAIL = "acv@robotcomics.net";
	private static final String PACKAGE_BUG_REPORTER = "net.robotmedia.bugreporter";
	
	private void reportProblem() {	
		final Context context = this.getActivity();
		try {
			this.getActivity().getPackageManager().getPackageInfo(PACKAGE_BUG_REPORTER, 0);
			final Intent intent = new Intent(ACTION_REPORT_BUG);
			intent.putExtra(EXTRA_PACKAGE, context.getPackageName());
			intent.putExtra(EXTRA_EMAIL, EMAIL);
			startActivity(intent);
		} catch (NameNotFoundException e) {
			AlertUtils.showYesNoAlert(this.getActivity(), false, 
					R.string.dialog_bug_reporter_title, 
					R.string.dialog_bug_reporter_text, 
					new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							IntentUtils.openMarket(context, PACKAGE_BUG_REPORTER);
						}
					}, null);
		}
	}

}
