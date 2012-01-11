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
package net.robotmedia.acv.ui.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class DialogFactory {
	
	public static void showSimpleAlert(Activity activity, boolean good, int titleId, int messageId) {
		AlertDialog dialog = new AlertDialog.Builder(activity).setIcon(
				good ? android.R.drawable.ic_menu_info_details : android.R.drawable.ic_menu_close_clear_cancel).setTitle(titleId).setMessage(messageId).setPositiveButton(
				android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).create();
		dialog.show();
	}
	
}
