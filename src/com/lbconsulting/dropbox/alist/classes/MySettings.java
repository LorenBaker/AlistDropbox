/*
 * Copyright 2014 Loren A. Baker
 * All rights reserved.
 */

package com.lbconsulting.dropbox.alist.classes;

import java.util.Iterator;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.lbconsulting.dropbox.alist.database.ListsTable;

public class MySettings {

	private static Context mContext;

	public static void setContext(Context context) {
		MySettings.mContext = context;
	}

	public static final String DROPBOX_APP_KEY = "obn7vqh7n96lidu";
	public static final String DROPBOX_APP_SECRET = "h6fkey749txt42q";
	public static final int REQUEST_LINK_TO_DBX = 0;

	public static final int STYLE_SHOW_LIST = 10;
	public static final int STYLE_SHOW_MASTER_LIST = 20;
	public static final int STYLE_SHOW_CONTEXT_LIST = 30;

	public static final String LIST_DATASTORE_ID = "listDatastoreID";
	public static final String LIST_STYLE = "listStyle";

	public static final int SORT_ALPHABETICAL = 10;
	public static final int SORT_MANUAL = 20;
	public static final int SORT_GROUP_THEN_ALPHABETICAL = 30;

	public static final String NOT_AVAILABLE = "**N/A**";

	public static final String ALIST_DROPBOX_SHARED_PREFERENCES = "AlistDropboxSharedPreferences";
	public static final String STATE_LISTS_ACTIVITY_FIRST_TIME_SHOWN = "stateListsActivityFirstTimeShown";
	public static final String STATE_ALIST_DATASTORE_ID = "stateAlistDatastoreID";

	public static void set(String header, Bundle bundle) {
		SharedPreferences outStates = mContext.getSharedPreferences(ALIST_DROPBOX_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		SharedPreferences.Editor ed = outStates.edit();

		Set<String> keySet = bundle.keySet();
		Iterator<String> it = keySet.iterator();
		String key;
		Object o;

		while (it.hasNext()) {
			key = it.next();
			o = bundle.get(key);
			if (o == null) {
				ed.remove(key);

			} else if (o instanceof Integer) {
				ed.putInt(header + key, (Integer) o);

			} else if (o instanceof Long) {
				ed.putLong(header + key, (Long) o);

			} else if (o instanceof Boolean) {
				ed.putBoolean(header + key, (Boolean) o);

			} else if (o instanceof CharSequence) {
				ed.putString(header + key, ((CharSequence) o).toString());

			} else if (o instanceof String) {
				ed.putString(header + key, (String) o);

			} else if (o instanceof Float) {
				ed.putFloat(header + key, (Float) o);

			} else if (o instanceof Bundle) {
				set(key, (Bundle) o);
			} else {
				MyLog.e("MySettings", "set(): Unknown value type; key=" + key);
			}
		}
		ed.commit();
	}

	public static boolean isFirstTimeListsActivityShown() {
		SharedPreferences storedStates = mContext.getSharedPreferences(ALIST_DROPBOX_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getBoolean(STATE_LISTS_ACTIVITY_FIRST_TIME_SHOWN, true);
	}

	public static String getAlistDatastoreID() {
		SharedPreferences storedStates = mContext.getSharedPreferences(ALIST_DROPBOX_SHARED_PREFERENCES,
				Context.MODE_PRIVATE);
		return storedStates.getString(STATE_ALIST_DATASTORE_ID, ListsTable.NOT_AVAILABLE);
	}
}
