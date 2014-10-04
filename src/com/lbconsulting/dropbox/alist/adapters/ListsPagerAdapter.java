package com.lbconsulting.dropbox.alist.adapters;

import java.util.ArrayList;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dropbox.sync.android.DbxRecord;
import com.lbconsulting.dropbox.alist.database.ListsTable;
import com.lbconsulting.dropbox.alist.fragments.AlistFragment;

//FragmentStatePagerAdapter
//FragmentPagerAdapter
public class ListsPagerAdapter extends FragmentStatePagerAdapter {

	private ArrayList<DbxRecord> mListRecords = null;
	private int mCount;
	private int mListStyle;

	public ListsPagerAdapter(FragmentManager fm, int listStyle) {
		super(fm);
		mListStyle = listStyle;
	}

	@Override
	public Fragment getItem(int position) {
		DbxRecord record = mListRecords.get(position);
		String listDatastoreID = record.getString(ListsTable.COL_LIST_DATASTORE_ID);
		AlistFragment frag = AlistFragment.newInstance(listDatastoreID, mListStyle);
		return frag;
	}

	@Override
	public int getCount() {
		return mCount;
	}

	public void setListRecords(ArrayList<DbxRecord> listRecords) {
		mListRecords = listRecords;
		mCount = listRecords.size();
	}

	public void setListStyle(int listStyle) {
		mListStyle = listStyle;
	}
}
