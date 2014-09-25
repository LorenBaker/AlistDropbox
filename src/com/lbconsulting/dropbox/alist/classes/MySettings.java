/*
 * Copyright 2014 Loren A. Baker
 * All rights reserved.
 */

package com.lbconsulting.dropbox.alist.classes;

import android.content.Context;

public class MySettings {

	private static Context mContext;

	public static void setContext(Context context) {
		MySettings.mContext = context;
	}

	public static final int STYLE_SHOW_LIST = 10;
	public static final int STYLE_SHOW_MASTER_LIST = 20;
	public static final int STYLE_SHOW_CONTEXT_LIST = 30;

	public static final String DATASTORE_ID = "listDataStoreID";
	public static final String STYLE = "listStyle";
}
