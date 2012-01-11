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
import net.robotmedia.acv.logic.PreferencesController;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

public class ACVDialogFactory {

	private Activity viewer;

	public ACVDialogFactory(Activity viewer) {
		this.viewer = viewer;
	}

	public AlertDialog createLoadErrorDialog() {
		AlertDialog dialog = new AlertDialog.Builder(viewer).setIcon(android.R.drawable.ic_menu_close_clear_cancel).setTitle(R.string.dialog_load_error_title)
				.setMessage(R.string.dialog_load_error_text).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).create();
		return dialog;
	}

	public AlertDialog createPageErrorDialog() {
		AlertDialog dialog = new AlertDialog.Builder(viewer).setIcon(android.R.drawable.ic_menu_close_clear_cancel).setTitle(R.string.dialog_page_error_title)
				.setMessage(R.string.dialog_page_error_text).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				}).create();
		return dialog;
	}

	public ProgressDialog createLoadProgressDialog() {
		ProgressDialog progressDialog;
		progressDialog = new ProgressDialog(viewer);
		progressDialog.setTitle(R.string.dialog_loading_progress_title);
		progressDialog.setIcon(android.R.drawable.ic_menu_info_details);
		progressDialog.setMessage(viewer.getString(R.string.dialog_loading_progress_text));
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		return progressDialog;
	}


	public AlertDialog createFlipControlsDialog() {
		AlertDialog dialog = new AlertDialog.Builder(viewer).setIcon(android.R.drawable.ic_menu_info_details).setTitle(
				R.string.dialog_flip_controls_title).setMessage(viewer.getString(R.string.dialog_flip_controls_text)).setPositiveButton(
				android.R.string.yes, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						PreferencesController pController = new PreferencesController(viewer);
						pController.flipControls();
					}
				}).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		}).create();
		return dialog;
	}

}
