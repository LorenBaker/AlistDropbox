package com.lbconsulting.dropbox.alist.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFields;
import com.dropbox.sync.android.DbxRecord;
import com.dropbox.sync.android.DbxTable;
import com.lbconsulting.dropbox.alist.activities.ListsActivity;
import com.lbconsulting.dropbox.alist.classes.ListsApplication;
import com.lbconsulting.dropbox.alist.classes.MyLog;
import com.lbconsulting.dropbox.alist.classes.MySettings;

public class ListsTable {

	// Part of AlistDatastore.
	public static final String TABLE_LISTS = "tblLists";
	public static final String COL_LIST_DATASTORE_ID = "listDatastoreID";
	public static final String COL_LIST_TITLE = "listTitle";
	public static final String COL_LIST_DATE_CREATED = "listDateCreated";

	// List Settings
	public static final String COL_LIST_COLOR_ID = "listColorID";
	public static final String COL_DELETE_NOTE_UPON_DESELECTING_ITEM = "deleteNoteUponDeslectingItem";

	public static final String COL_LIST_SORT_ORDER = "listSortOrder";
	public static final String COL_MASTER_LIST_SORT_ORDER = "masterListSortOrder";

	public static final String COL_LISTVIEW_FIRST_VISIBLE_POSITION = "listViewFirstVisiblePosition";
	public static final String COL_LISTVIEW_TOP = "listViewTop";

	public static final String COL_MASTER_LISTVIEW_FIRST_VISIBLE_POSITION = "masterListView1stVisPosition";
	public static final String COL_MASTER_LISTVIEW_TOP = "masterListViewTop";

	private static ListsApplication app;
	public static final String NOT_AVAILABLE = "**N/A**";

	// private static DbxDatastore alistDatastore = null;

	/*	public static void setAlistDatastore(String alistDatastoreID) {

			app = ListsApplication.getInstance();
			try {
				// Open the Alist Datastore.
				alistDatastore = app.datastoreManager.openDatastore(alistDatastoreID);
			} catch (DbxException e) {
				MyLog.e(TABLE_LISTS, "DbxException openDatastore in setAlistDatastore() alistDatastoreID:"
						+ alistDatastoreID);
				e.printStackTrace();
			}
		}*/

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Create Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static String CreateNewList(String listTitle, String listDatastoreID) {
		String newListID = NOT_AVAILABLE;

		if (listTitle != null && listDatastoreID != null) {
			listTitle = listTitle.trim();
			DbxDatastore alistDatastore = ListsActivity.getAlistDatastore();
			if (!listTitle.isEmpty() && !listDatastoreID.isEmpty() && alistDatastore != null) {
				if (alistDatastore.isOpen()) {

					// determine if the item is already in the database
					DbxFields queryParams = new DbxFields().set(COL_LIST_DATASTORE_ID, listDatastoreID);
					DbxTable.QueryResult queryResults = Query(queryParams);

					if (queryResults != null && queryResults.hasResults()) {
						DbxRecord firstResult = queryResults.iterator().next();
						newListID = firstResult.getId();
						if (!newListID.isEmpty()) {
							// the item already exists in the database
							return newListID;
						}
					}

					// add the List to the database
					DbxFields fields = new DbxFields();
					fields.set(COL_LIST_TITLE, listTitle);
					fields.set(COL_LIST_DATASTORE_ID, listDatastoreID);
					fields.set(COL_LIST_COLOR_ID, ColorsTable.getNextColorID(alistDatastore));
					fields.set(COL_LIST_DATE_CREATED, System.currentTimeMillis());

					// default values
					fields.set(COL_DELETE_NOTE_UPON_DESELECTING_ITEM, true);

					fields.set(COL_LIST_SORT_ORDER, MySettings.SORT_ALPHABETICAL);
					fields.set(COL_MASTER_LIST_SORT_ORDER, MySettings.SORT_ALPHABETICAL);

					fields.set(COL_LISTVIEW_FIRST_VISIBLE_POSITION, 0);
					fields.set(COL_LISTVIEW_TOP, 0);

					fields.set(COL_LISTVIEW_FIRST_VISIBLE_POSITION, 0);
					fields.set(COL_LISTVIEW_TOP, 0);

					// Insert the new record in the "items" table.
					try {
						DbxRecord record = alistDatastore.getTable(TABLE_LISTS).insert(fields);
						alistDatastore.sync();
						newListID = record.getId();

					} catch (DbxException e) {
						MyLog.e(TABLE_LISTS, "Unable to create new List. DbxException in CreateNewList!");
						e.printStackTrace();
					}

				} else {
					MyLog.e(TABLE_LISTS,
							"Unable to create new List. alistDatastore is closed!");
				}
			} else {
				MyLog.e(TABLE_LISTS,
						"Unable to create new List. listTitle or listDatastoreID is empty! OR alistDatastore is null!");
			}
		} else {
			MyLog.e(TABLE_LISTS, "Unable to create new List. listTitle or listDatastoreID is null!");
		}
		return newListID;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Read Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static DbxTable.QueryResult Query(DbxFields queryParams) {

		DbxTable.QueryResult results = null;
		DbxDatastore alistDatastore = ListsActivity.getAlistDatastore();
		if (alistDatastore != null) {
			try {
				if (queryParams == null) {
					results = alistDatastore.getTable(TABLE_LISTS).query();
				} else {
					results = alistDatastore.getTable(TABLE_LISTS).query(queryParams);
				}
			} catch (DbxException e) {
				MyLog.e(TABLE_LISTS, "DbxException in Query!");
				e.printStackTrace();
			}
		} else {
			MyLog.e(TABLE_LISTS, "Query(): alistDatastore is null!");
		}
		return results;
	}

	public static ArrayList<DbxRecord> QueryAsList(DbxFields queryParams, int sortOrder) {

		ArrayList<DbxRecord> resultsList = new ArrayList<DbxRecord>();
		List<DbxRecord> queryResult = null;
		DbxDatastore alistDatastore = ListsActivity.getAlistDatastore();
		if (alistDatastore != null) {
			if (alistDatastore.isOpen()) {
				try {
					if (queryParams == null) {
						queryResult = alistDatastore.getTable(TABLE_LISTS).query().asList();
					} else {
						queryResult = alistDatastore.getTable(TABLE_LISTS).query(queryParams).asList();
					}
				} catch (DbxException e) {
					MyLog.e(TABLE_LISTS, "DbxException in QueryAsList()!");
					e.printStackTrace();
				}
				if (queryResult != null && queryResult.size() > 0) {
					switch (sortOrder) {

						case MySettings.SORT_ALPHABETICAL:
							Collections.sort(queryResult, new Comparator<DbxRecord>() {

								@Override
								public int compare(DbxRecord list1, DbxRecord list2)
								{
									return list1.getString(COL_LIST_TITLE).compareToIgnoreCase(
											list2.getString(COL_LIST_TITLE));
								}
							});

							break;

						case MySettings.SORT_MANUAL:

							break;

						case MySettings.SORT_GROUP_THEN_ALPHABETICAL:

							break;

						default:
							break;
					}

					resultsList.addAll(queryResult);
				}
			} else {
				MyLog.e(TABLE_LISTS, "QueryAsList(): alistDatastore is not open!");
			}
		} else {
			MyLog.e(TABLE_LISTS, "QueryAsList(): alistDatastore is null!");
		}
		return resultsList;
	}

	public static DbxRecord getRecord(String listDatastoreID) {
		DbxRecord result = null;
		DbxDatastore alistDatastore = ListsActivity.getAlistDatastore();
		if (alistDatastore != null && alistDatastore.isOpen() && !listDatastoreID.isEmpty()) {
			DbxFields queryParams = new DbxFields().set(COL_LIST_DATASTORE_ID, listDatastoreID);
			DbxTable.QueryResult queryResult = Query(queryParams);
			if (queryResult != null && queryResult.hasResults()) {
				result = queryResult.iterator().next();
				if (queryResult.count() > 1) {
					MyLog.e(TABLE_LISTS, "getRecord(): more than one List record found!");
				}
			}
		}
		return result;
	}

	public static boolean ListTitleExists(String listTitle) {
		boolean listTitleExists = false;

		DbxDatastore alistDatastore = ListsActivity.getAlistDatastore();
		if (alistDatastore != null && alistDatastore.isOpen() && !listTitle.isEmpty()) {
			DbxFields queryParams = new DbxFields().set(COL_LIST_TITLE, listTitle);
			DbxTable.QueryResult queryResult = Query(queryParams);
			if (queryResult != null && queryResult.hasResults()) {
				listTitleExists = true;
			}
		}

		return listTitleExists;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Update Methods
	// /////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void setListViewPosition(String listDatastoreID, int firstVisiblePosition, int top) {
		DbxDatastore alistDatastore = ListsActivity.getAlistDatastore();
		if (alistDatastore != null && alistDatastore.isOpen() && !listDatastoreID.isEmpty()) {
			DbxRecord listRecord = getRecord(listDatastoreID);
			if (listRecord != null) {
				try {
					listRecord.set(COL_LISTVIEW_FIRST_VISIBLE_POSITION, firstVisiblePosition)
							.set(COL_LISTVIEW_TOP, top);
					alistDatastore.sync();
				} catch (DbxException e) {
					MyLog.e(TABLE_LISTS, "DbxException in setListViewPosition()!");
					e.printStackTrace();
				}
			}
		}
	}

	/*	public static void UpdateListRecords(DbxTable.QueryResult dbxRecords, ContentValues values) {
			if (alistDatastore != null && dbxRecords != null) {
				while (dbxRecords.iterator().hasNext()) {
					setDbxRecordValues(dbxRecords.iterator().next(), values);
				}
			}
		}

		public static void UpdateListRecord(String listID, ContentValues values) {
			if (alistDatastore != null) {
				DbxRecord dbxRecord = getRecord(alistDatastore, listID);
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
			if (itemID > 0) {
				ContentResolver cr = context.getContentResolver();
				Uri itemUri = Uri.withAppendedPath(CONTENT_URI, String.valueOf(itemID));
				String selection = null;
				String[] selectionArgs = null;
				numberOfUpdatedRecords = cr.update(itemUri, newFieldValues, selection, selectionArgs);
			}
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
			Cursor cursor = getItem(context, itemID);
			if (cursor != null) {
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndexOrThrow(COL_STRUCK_OUT);
				int strikeOutIntValue = cursor.getInt(columnIndex);
				boolean strikeOutValue = strikeOutIntValue > 0;
				cursor.close();
				StrikeItem(context, itemID, !strikeOutValue);
			}
		}

		public static void ToggleSelection(String itemID) {
			Cursor cursor = getItem(context, itemID);
			if (cursor != null) {
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndexOrThrow(COL_SELECTED);
				int selectedIntValue = cursor.getInt(columnIndex);
				boolean selectedValue = selectedIntValue > 0;
				cursor.close();
				SelectItem(context, itemID, !selectedValue);
			}

		}

		public static void ToggleCheckBox(String itemID) {
			Cursor cursor = getItem(context, itemID);
			if (cursor != null) {
				cursor.moveToFirst();
				int columnIndex = cursor.getColumnIndexOrThrow(COL_CHECKED);
				int checkIntValue = cursor.getInt(columnIndex);
				boolean checkValue = checkIntValue > 0;
				cursor.close();
				CheckItem(context, itemID, !checkValue);
			}
		}

		public static int CheckItemsUnused(String listID, long numberOfDays) {
			int numberOfCheckedItems = -1;
			if (listID > 1) {

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
			}
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
	*/
}
