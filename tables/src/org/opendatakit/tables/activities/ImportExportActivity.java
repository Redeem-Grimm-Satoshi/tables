/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.tables.activities;


import org.opendatakit.tables.R;
import org.opendatakit.tables.utils.Constants;
import org.opendatakit.tables.utils.IntentUtil;
import org.opendatakit.tables.utils.TableFileUtils;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * An activity for importing and exporting tables.
 */
public class ImportExportActivity extends TabActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String appName = IntentUtil.retrieveAppNameFromBundle(
		    this.getIntent().getExtras());
		if ( appName == null ) {
		  throw new IllegalStateException("appName was not present in bundle");
		}
		setContentView(R.layout.importexport_view);
		TabHost th = getTabHost();
		TabHost.TabSpec spec;
		Intent intent;
		// the import CSV option
		intent = new Intent().setClass(this, ImportCSVActivity.class);
		intent.putExtra(Constants.IntentKeys.APP_NAME, appName);
		spec = th.newTabSpec("importcsv");
		spec.setIndicator(getString(R.string.import_csv));
		spec.setContent(intent);
		th.addTab(spec);
		// the export CSV option
		intent = new Intent().setClass(this, ExportCSVActivity.class);
      intent.putExtra(Constants.IntentKeys.APP_NAME, appName);
		spec = th.newTabSpec("exportcsv");
		spec.setIndicator(getString(R.string.export_csv));
		spec.setContent(intent);
		th.addTab(spec);
	}

}