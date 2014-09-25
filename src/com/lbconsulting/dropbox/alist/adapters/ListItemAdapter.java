package com.lbconsulting.dropbox.alist.adapters;

import java.util.List;

import android.content.Context;

import com.dropbox.sync.android.DbxRecord;
import com.lbconsulting.dropbox.alist.database.ItemsTable;

// An adapter for items within a list. Each item is backed by a record in the list's datastore.
public class ListItemAdapter extends DeletableItemAdapter<DbxRecord> {

	public ListItemAdapter(List<DbxRecord> items, Context ctx, Boolean editable) {
		super(items, ctx, editable);
	}

	@Override
	protected String getText(int position) {
		return this.getItem(position).getString(ItemsTable.COL_ITEM_NAME);
	}
}
