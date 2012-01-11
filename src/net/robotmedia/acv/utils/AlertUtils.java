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
package net.robotmedia.acv.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class AlertUtils {
	
	public static AlertDialog showYesNoAlert(Context context, boolean good,
			int titleId, int messageId,
			DialogInterface.OnClickListener positiveListener,
			DialogInterface.OnClickListener negativeListener) {
		final AlertDialog dialog = new AlertDialog.Builder(context).setIcon(
				good ? android.R.drawable.ic_menu_info_details
						: android.R.drawable.ic_menu_close_clear_cancel)
				.setTitle(titleId).setMessage(messageId).setPositiveButton(
						android.R.string.ok, positiveListener)
				.setNegativeButton(android.R.string.no, negativeListener)
				.create();
		dialog.show();
		return dialog;
	}

}
