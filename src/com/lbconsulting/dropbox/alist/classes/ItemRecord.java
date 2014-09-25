package com.lbconsulting.dropbox.alist.classes;

import com.dropbox.sync.android.DbxRecord;
import com.lbconsulting.dropbox.alist.database.ItemsTable;

public class ItemRecord {

	private String mID = null;

	private String mName = ItemsTable.NOT_AVAILABLE;
	private String mNote = ItemsTable.NOT_AVAILABLE;
	private String mGroupID = ItemsTable.NOT_AVAILABLE;

	private boolean mChecked = false;
	private boolean mSelected = false;
	private boolean mStruckOut = false;

	private long mDateTimeLastUsed = -1;

	public ItemRecord(DbxRecord record) {
		if (record != null) {
			mID = record.getId();

			mName = record.getString(ItemsTable.COL_ITEM_NAME);
			mNote = record.getString(ItemsTable.COL_ITEM_NOTE);
			mGroupID = record.getString(ItemsTable.COL_GROUP_ID);

			mChecked = record.getBoolean(ItemsTable.COL_CHECKED);
			mSelected = record.getBoolean(ItemsTable.COL_SELECTED);
			mStruckOut = record.getBoolean(ItemsTable.COL_STRUCK_OUT);

			mDateTimeLastUsed = record.getLong(ItemsTable.COL_DATE_TIME_LAST_USED);
		}
	}

	public String getID() {
		return mID;
	}

	public String getName() {
		return mName;
	}

	public String getNote() {
		return mNote;
	}

	public String getGroupID() {
		return mGroupID;
	}

	public boolean isChecked() {
		return mChecked;
	}

	public boolean isSelected() {
		return mSelected;
	}

	public boolean isStruckOut() {
		return mStruckOut;
	}

	public long getDateTimeLastUsed() {
		return mDateTimeLastUsed;
	}

}
