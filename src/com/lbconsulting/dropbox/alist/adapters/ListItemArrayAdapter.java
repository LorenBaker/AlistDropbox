package com.lbconsulting.dropbox.alist.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.dropbox.sync.android.DbxRecord;
import com.lbconsulting.dropbox.alist.R;
import com.lbconsulting.dropbox.alist.classes.MySettings;
import com.lbconsulting.dropbox.alist.database.ItemsTable;

public class ListItemArrayAdapter extends ArrayAdapter<DbxRecord> {

	private ArrayList<DbxRecord> mItems;
	private Context mContext;
	private int mStyle;

	public ListItemArrayAdapter(Context context, ArrayList<DbxRecord> items, int style) {
		super(context, android.R.layout.simple_list_item_1, items);
		this.mItems = items;
		this.mContext = context;
		this.mStyle = style;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.row_list_items, parent, false);
		}

		TextView tvListItemName = (TextView) convertView.findViewById(R.id.tvListItemName);
		TextView tvListItemNote = (TextView) convertView.findViewById(R.id.tvListItemNote);
		CheckBox ckItemChecked = (CheckBox) convertView.findViewById(R.id.ckItemChecked);
		CheckBox ckItemSelected = (CheckBox) convertView.findViewById(R.id.ckItemSelected);

		DbxRecord record = mItems.get(position);
		String itemName = record.getString(ItemsTable.COL_ITEM_NAME);
		String itemNote = record.getString(ItemsTable.COL_ITEM_NOTE);

		switch (mStyle) {
			case MySettings.STYLE_SHOW_LIST:
				ckItemChecked.setVisibility(View.GONE);
				ckItemSelected.setVisibility(View.GONE);
				boolean itemStruckOut = record.getBoolean(ItemsTable.COL_STRUCK_OUT);
				tvListItemName.setText(itemName);
				if (!itemNote.isEmpty()) {
					tvListItemNote.setText("(" + itemNote + ")");
				}
				break;

			case MySettings.STYLE_SHOW_MASTER_LIST:
				ckItemChecked.setVisibility(View.GONE);
				ckItemSelected.setVisibility(View.VISIBLE);
				boolean itemSelected = record.getBoolean(ItemsTable.COL_SELECTED);
				ckItemSelected.setChecked(itemSelected);
				tvListItemName.setText(itemName);
				if (!itemNote.isEmpty()) {
					tvListItemNote.setText("(" + itemNote + ")");
				}

				break;

			case MySettings.STYLE_SHOW_CONTEXT_LIST:
				ckItemChecked.setVisibility(View.VISIBLE);
				ckItemSelected.setVisibility(View.GONE);
				boolean itemChecked = record.getBoolean(ItemsTable.COL_CHECKED);
				ckItemSelected.setChecked(itemChecked);
				tvListItemName.setText(itemName);
				if (!itemNote.isEmpty()) {
					tvListItemNote.setText("(" + itemNote + ")");
				}
				break;
			default:
				break;

		}

		return convertView;
	}

	public void setStyle(int style) {
		mStyle = style;
	}
}
