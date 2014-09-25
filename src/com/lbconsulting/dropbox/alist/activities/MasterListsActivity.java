package com.lbconsulting.dropbox.alist.activities;

import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import com.dropbox.sync.android.DbxDatastore;

public class MasterListsActivity extends FragmentActivity implements DbxDatastore.SyncStatusListener {

	@Override
	public void onDatastoreStatusChange(DbxDatastore arg0) {
		// TODO Auto-generated method stub
		
	}

}
