package com.lbconsulting.dropbox.alist.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.lbconsulting.dropbox.alist.classes.ItemRecord;
import com.lbconsulting.dropbox.alist.classes.MyLog;

public class ItemsTable {

	// Items data table
	// Version 1
	public static final String TABLE_ITEMS = "tblItems";

	public static final String COL_ITEM_NAME = "itemName";
	public static final String COL_ITEM_NOTE = "itemNote";
	public static final String COL_GROUP_ID = "groupID";

	public static final String COL_SELECTED = "itemSelected";
	public static final String COL_STRUCK_OUT = "itemStruckOut";
	public static final String COL_CHECKED = "itemChecked";

	public static final String COL_DATE_TIME_LAST_USED = "dateTimeLastUsed";

	public static final String NOT_AVAILABLE = "**N/A**";

	/*	public static final int SELECTED_TRUE = 1;
		public static final int SELECTED_FALSE = 0;

		public static final int STRUCKOUT_TRUE = 1;
		public static final int STRUCKOUT_FALSE = 0;

		public static final int CHECKED_TRUE = 1;
		public static final int CHECKED_FALSE = 0;

		public static final int MANUAL_SORT_SWITCH_INVISIBLE = 0;
		public static final int MANUAL_SORT_SWITCH_VISIBLE = 1;
		public static final int MANUAL_SORT_SWITCH_ITEM_SWITCHED = 2;*/

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static String CreateNewItem(DbxDatastore datastore, String itemName, String itemNote, String groupID) {
		String newItemID = NOT_AVAILABLE;

		if (itemName != null) {
			itemName = itemName.trim();
			if (!itemName.isEmpty()) {

				// determine if the item is already in the database
				DbxFields queryParams = new DbxFields().set(COL_ITEM_NAME, itemName);
				DbxTable.QueryResult queryResults = Query(datastore, queryParams);

				if (queryResults != null && queryResults.hasResults()) {
					DbxRecord firstResult = queryResults.iterator().next();
					newItemID = firstResult.getId();
					if (!newItemID.isEmpty()) {
						// the item already exists in the database
						return newItemID;
					}
				}

				// add the item to the database
				if (itemNote == null) {
					itemNote = "";
				}

				if (groupID == null) {
					groupID = "";
				}

				DbxFields fields = new DbxFields();
				fields.set(COL_ITEM_NAME, itemName);
				fields.set(COL_ITEM_NOTE, itemNote.trim());
				fields.set(COL_GROUP_ID, groupID);

				// default values
				fields.set(COL_SELECTED, true);
				fields.set(COL_STRUCK_OUT, false);
				fields.set(COL_CHECKED, false);

				fields.set(COL_DATE_TIME_LAST_USED, System.currentTimeMillis());

				// Insert the new record in the "items" table.
				DbxRecord record = datastore.getTable(TABLE_ITEMS).insert(fields);
				newItemID = record.getId();

			} else {
				MyLog.e("ItemsTable", "Unable to create new item. itemName is empty!");
			}
		} else {
			MyLog.e("ItemsTable", "Unable to create new item. itemName is null!");
		}
		return newItemID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static DbxTable.QueryResult Query(DbxDatastore datastore, DbxFields queryParams) {

		DbxTable.QueryResult results = null;
		try {
			if (queryParams == null) {
				results = datastore.getTable(TABLE_ITEMS).query();
			} else {
				results = datastore.getTable(TABLE_ITEMS).query(queryParams);
			}
		} catch (DbxException e) {
			MyLog.e("ItemsTable", "DbxException in Query!");
			e.printStackTrace();
		}
		return results;
	}

	public static ArrayList<DbxRecord> QueryAsList(DbxDatastore datastore, DbxFields queryParams) {

		ArrayList<DbxRecord> resultsList = new ArrayList<DbxRecord>();
		List<DbxRecord> queryResult = null;
		try {
			if (queryParams == null) {
				queryResult = datastore.getTable(TABLE_ITEMS).query().asList();
			} else {
				queryResult = datastore.getTable(TABLE_ITEMS).query(queryParams).asList();
			}
		} catch (DbxException e) {
			MyLog.e("ItemsTable", "DbxException in QueryAsList!");
			e.printStackTrace();
		}
		if (queryResult != null && queryResult.size() > 0) {
			resultsList.addAll(queryResult);
		}
		return resultsList;
	}

	public static DbxTable.QueryResult getAllRecords(DbxDatastore datastore) {
		return Query(datastore, null);
	}

	public static DbxTable.QueryResult getAllSelectedRecords(DbxDatastore datastore) {
		DbxFields queryParams = new DbxFields().set(COL_SELECTED, true);
		return Query(datastore, queryParams);
	}

	public static DbxTable.QueryResult getAllCheckedRecords(DbxDatastore datastore) {
		DbxFields queryParams = new DbxFields().set(COL_CHECKED, true);
		return Query(datastore, queryParams);
	}

	public static DbxTable.QueryResult getAllStruckOutRecords(DbxDatastore datastore) {
		DbxFields queryParams = new DbxFields().set(COL_STRUCK_OUT, true);
		return Query(datastore, queryParams);
	}

	private static DbxRecord getRecord(DbxDatastore datastore, String itemID) {
		DbxRecord result = null;
		if (datastore != null && !TextUtils.isEmpty(itemID)) {
			try {
				result = datastore.getTable(TABLE_ITEMS).get(itemID);
			} catch (DbxException e) {
				MyLog.e("ItemsTable", "DbxException in getRecord!");
				e.printStackTrace();
			}
		}
		return result;
	}

	public static ItemRecord getItemRecord(DbxDatastore datastore, String itemID) {
		return new ItemRecord(getRecord(datastore, itemID));
	}

	public static String getItemName(DbxDatastore datastore, String itemID) {
		String result = NOT_AVAILABLE;
		DbxRecord record = getRecord(datastore, itemID);
		if (record != null) {
			result = record.getString(COL_ITEM_NAME);
		}
		return result;
	}

	public static String getItemNote(DbxDatastore datastore, String itemID) {
		String result = NOT_AVAILABLE;
		DbxRecord record = getRecord(datastore, itemID);
		if (record != null) {
			result = record.getString(COL_ITEM_NOTE);
		}
		return result;
	}

	public static String getGroupID(DbxDatastore datastore, String itemID) {
		String result = NOT_AVAILABLE;
		DbxRecord record = getRecord(datastore, itemID);
		if (record != null) {
			result = record.getString(COL_GROUP_ID);
		}
		return result;
	}

	public static boolean isItemChecked(DbxDatastore datastore, String itemID) {
		boolean result = false;
		DbxRecord record = getRecord(datastore, itemID);
		if (record != null) {
			result = record.getBoolean(COL_CHECKED);
		}
		return result;
	}

	public static boolean isItemSelected(DbxDatastore datastore, String itemID) {
		boolean result = false;
		DbxRecord record = getRecord(datastore, itemID);
		if (record != null) {
			result = record.getBoolean(COL_SELECTED);
		}
		return result;
	}

	public static boolean isItemStruckOut(DbxDatastore datastore, String itemID) {
		boolean result = false;
		DbxRecord record = getRecord(datastore, itemID);
		if (record != null) {
			result = record.getBoolean(COL_STRUCK_OUT);
		}
		return result;
	}

	public static long getDate(DbxDatastore datastore, String itemID) {
		long result = -1;
		DbxRecord record = getRecord(datastore, itemID);
		if (record != null) {
			result = record.getLong(COL_DATE_TIME_LAST_USED);
		}
		return result;
	}

	/*	public static int getNumberOfCheckedItmes(datastore datastore, String listID) {
			int numberOfCheckedItmes = -1;

			return numberOfCheckedItmes;
		}*/

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void UpdateItemRecords(DbxDatastore datastore, DbxTable.QueryResult dbxRecords, ContentValues values) {
		if (datastore != null && dbxRecords != null) {
			while (dbxRecords.iterator().hasNext()) {
				setDbxRecordValues(dbxRecords.iterator().next(), values);
			}
		}
	}

	public static void UpdateItemRecord(DbxDatastore datastore, String itemID, ContentValues values) {
		if (datastore != null) {
			DbxRecord dbxRecord = getRecord(datastore, itemID);
			if (dbxRecord != null) {
				setDbxRecordValues(dbxRecord, values);
			}
		}
	}

	private static void setDbxRecordValues(DbxRecord dbxRecord, ContentValues values) {
		if (dbxRecord != null) {
			Set<Entry<String, Object>> s = values.valueSet();
			Iterator<Entry<String, Object>> itr = s.iterator();
			while (itr.hasNext()) {
				Entry<String, Object> me = itr.next();
				String key = me.getKey().toString();

				if (key.equals(COL_ITEM_NAME)) {
					String itemName = (String) me.getValue();
					if (itemName == null) {
						itemName = "";
					}
					dbxRecord.set(key, itemName);

				} else if (key.equals(COL_ITEM_NOTE)) {
					String itemNote = (String) me.getValue();
					if (itemNote == null) {
						itemNote = "";
					}
					dbxRecord.set(key, itemNote);

				} else if (key.equals(COL_GROUP_ID)) {
					String groupID = (String) me.getValue();
					dbxRecord.set(key, groupID);

				} else if (key.equals(COL_SELECTED)) {
					int selectedValue = (Integer) me.getValue();
					boolean selected = false;
					if (selectedValue == 1) {
						selected = true;
					}
					dbxRecord.set(key, selected);

				} else if (key.equals(COL_STRUCK_OUT)) {
					int itemStrikoutValue = (Integer) me.getValue();
					boolean itemStrikout = false;
					if (itemStrikoutValue == 1) {
						itemStrikout = true;
					}
					dbxRecord.set(key, itemStrikout);

				} else if (key.equals(COL_CHECKED)) {
					int itemCheckedValue = (Integer) me.getValue();
					boolean itemChecked = false;
					if (itemCheckedValue == 1) {
						itemChecked = true;
					}
					dbxRecord.set(key, itemChecked);

				} else if (key.equals(COL_DATE_TIME_LAST_USED)) {
					long lastDate = (Long) me.getValue();
					dbxRecord.set(key, lastDate);

				} else {
					MyLog.e("ItemsTable: setDbxRecordValues ", "Unknown column name:" + key);
				}
			}
		}
	}

	public static int UpdateItemFieldValues(DbxDatastore datastore, String itemID, ContentValues newFieldValues) {
		int numberOfUpdatedRecords = -1;
		/*if (itemID > 0) {
			ContentResolver cr = context.getContentResolver();
			Uri itemUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(itemID));
			String selection = null;
			String[] selectionArgs = null;
			numberOfUpdatedRecords = cr.update(itemUri, newFieldValues, selection, selectionArgs);
		}*/
		return numberOfUpdatedRecords;
	}

	public static int SelectItem(String itemID, boolean selected) {
		int numberOfUpdatedRecords = -1;

		return numberOfUpdatedRecords;
	}

	public static int CheckItem(String itemID, boolean selected) {
		int numberOfUpdatedRecords = -1;

		return numberOfUpdatedRecords;
	}

	public static int StrikeOutItem(String itemID, boolean selected) {
		int numberOfUpdatedRecords = -1;

		return numberOfUpdatedRecords;
	}

	public static int DeselectAllItemsInList(String listID, boolean deleteNoteUponDeslectingItem) {
		int numberOfUpdatedRecords = -1;

		return numberOfUpdatedRecords;
	}

	public static int DeselectAllItemsInGroup(String groupID) {
		int numberOfUpdatedRecords = -1;

		return numberOfUpdatedRecords;
	}

	public static void ToggleStrikeOut(String itemID) {
		/*Cursor cursor = getItem(context, itemID);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndexOrThrow(COL_STRUCK_OUT);
			int strikeOutIntValue = cursor.getInt(columnIndex);
			boolean strikeOutValue = strikeOutIntValue > 0;
			cursor.close();
			StrikeItem(context, itemID, !strikeOutValue);
		}*/
	}

	public static void ToggleSelection(String itemID) {
		/*Cursor cursor = getItem(context, itemID);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndexOrThrow(COL_SELECTED);
			int selectedIntValue = cursor.getInt(columnIndex);
			boolean selectedValue = selectedIntValue > 0;
			cursor.close();
			SelectItem(context, itemID, !selectedValue);
		}*/

	}

	public static void ToggleCheckBox(String itemID) {
		/*Cursor cursor = getItem(context, itemID);
		if (cursor != null) {
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndexOrThrow(COL_CHECKED);
			int checkIntValue = cursor.getInt(columnIndex);
			boolean checkValue = checkIntValue > 0;
			cursor.close();
			CheckItem(context, itemID, !checkValue);
		}*/
	}

	public static int CheckItemsUnused(String listID, long numberOfDays) {
		int numberOfCheckedItems = -1;
		/*if (listID > 1) {

			long numberOfMilliSeconds = numberOfDays * milliSecondsPerDay;
			Calendar now = Calendar.getInstance();
			long dateTimeCutOff = now.getTimeInMillis() - numberOfMilliSeconds;

			ContentResolver cr = context.getContentResolver();
			Uri itemUri = CONTENT_URI;
			String selection = COL_DATE_TIME_LAST_USED + " < ?";
			String[] selectionArgs = { String.valueOf(dateTimeCutOff) };

			ContentValues values = new ContentValues();
			values.put(COL_CHECKED, CHECKED_TRUE);

			numberOfCheckedItems = cr.update(itemUri, values, selection, selectionArgs);
		}*/
		return numberOfCheckedItems;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Delete Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static int DeleteItem(String itemID) {
		int numberOfDeletedRecords = -1;

		return numberOfDeletedRecords;
	}

	public static int DeleteAllItemsInList(String listID) {
		int numberOfDeletedRecords = -1;

		return numberOfDeletedRecords;
	}

	public static int DeleteAllCheckedItemsInList(Context context, long listID) {
		int numberOfDeletedRecords = -1;

		return numberOfDeletedRecords;
	}

}
