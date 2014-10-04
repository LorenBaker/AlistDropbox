package com.lbconsulting.dropbox.alist.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxDatastoreInfo;
import com.dropbox.sync.android.DbxDatastoreManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxRecord;
import com.lbconsulting.dropbox.alist.R;
import com.lbconsulting.dropbox.alist.adapters.ListItemArrayAdapter;
import com.lbconsulting.dropbox.alist.classes.AlistDropboxEvents.UpdateLists;
import com.lbconsulting.dropbox.alist.classes.ListsApplication;
import com.lbconsulting.dropbox.alist.classes.MyLog;
import com.lbconsulting.dropbox.alist.classes.MySettings;
import com.lbconsulting.dropbox.alist.database.ItemsTable;
import com.lbconsulting.dropbox.alist.database.ListsTable;

import de.greenrobot.event.EventBus;

public class AlistFragment extends Fragment implements OnClickListener {

	private ListsApplication app;

	private String mListDatastoreID = "";
	private DbxDatastore mListDatastore = null;
	private DbxRecord mListRecord = null;
	private int mStyle = MySettings.STYLE_SHOW_LIST;

	private TextView txtItemName;
	private TextView txtItemNote;
	private Button btnAddToMasterList;
	private Button btnClearEditText;
	private LinearLayout llItemNoteInput;
	private ListView mItemsListView;

	private ListItemArrayAdapter mListItemArrayAdapter;

	private DbxDatastoreManager.ListListener mListListener;

	public AlistFragment() {
		// Empty constructor
	}

	public static AlistFragment newInstance(String listDatastoreID, int listStyle) {
		AlistFragment f = new AlistFragment();
		// Supply datastoreID input as an argument.
		Bundle args = new Bundle();
		args.putString(MySettings.LIST_DATASTORE_ID, listDatastoreID);
		args.putInt(MySettings.LIST_STYLE, listStyle);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		MyLog.i("AlistFragment", "onAttach() datastoreID:" + mListDatastoreID);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.i("AlistFragment", "onCreate() datastoreID:" + mListDatastoreID);
	}

	public void onEvent(UpdateLists event) {
		SaveItemsListViewPosition();
		mStyle = event.getStyle();
		updateList();
	}

	private void updateList() {
		if (mListDatastore != null && mListDatastore.isOpen()) {
			setFramentStyle();
			new QueryListDatastore().execute();
			/*// get and then show all items in the list
			ArrayList<DbxRecord> items = ItemsTable.QueryAsList(mListDatastore, null, MySettings.SORT_ALPHABETICAL);
			mListItemArrayAdapter = new ListItemArrayAdapter(getActivity(), items, mStyle);
			mItemsListView.setAdapter(mListItemArrayAdapter);

			// move the list view to its starting position
			int firstVisiblePosition = (int) mListRecord.getLong(ListsTable.COL_LISTVIEW_FIRST_VISIBLE_POSITION);
			int top = (int) mListRecord.getLong(ListsTable.COL_LISTVIEW_TOP);
			mItemsListView.setSelectionFromTop(firstVisiblePosition, top);*/
		}
	}

	private class QueryListDatastore extends AsyncTask<Void, Void, ArrayList<DbxRecord>> {

		@Override
		protected ArrayList<DbxRecord> doInBackground(Void... arg0) {
			// get and then show all items in the list
			ArrayList<DbxRecord> items = ItemsTable.QueryAsList(mListDatastore, null, MySettings.SORT_ALPHABETICAL);
			return items;
		}

		protected void onPostExecute(ArrayList<DbxRecord> items) {
			mListItemArrayAdapter = new ListItemArrayAdapter(getActivity(), items, mStyle);
			mItemsListView.setAdapter(mListItemArrayAdapter);

			// move the list view to its starting position
			int firstVisiblePosition = (int) mListRecord.getLong(ListsTable.COL_LISTVIEW_FIRST_VISIBLE_POSITION);
			int top = (int) mListRecord.getLong(ListsTable.COL_LISTVIEW_TOP);
			mItemsListView.setSelectionFromTop(firstVisiblePosition, top);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		MyLog.i("AlistFragment", "onSaveInstanceState() datastoreID:" + mListDatastoreID);
		outState.putString(MySettings.LIST_DATASTORE_ID, this.mListDatastoreID);
		outState.putInt(MySettings.LIST_STYLE, this.mStyle);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("AlistFragment", "onCreateView() datastoreID:" + mListDatastoreID);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(MySettings.LIST_DATASTORE_ID)) {
				mListDatastoreID = savedInstanceState.getString(MySettings.LIST_DATASTORE_ID);
			}
			if (savedInstanceState.containsKey(MySettings.LIST_STYLE)) {
				mStyle = savedInstanceState.getInt(MySettings.LIST_STYLE, MySettings.STYLE_SHOW_LIST);
			}
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				mListDatastoreID = bundle.getString(MySettings.LIST_DATASTORE_ID);
				mStyle = bundle.getInt(MySettings.LIST_STYLE, MySettings.STYLE_SHOW_LIST);
			}
		}
		app = ListsApplication.getInstance();
		mListRecord = ListsTable.getRecord(mListDatastoreID);

		View view = inflater.inflate(R.layout.frag_lists, container, false);

		txtItemName = (TextView) view.findViewById(R.id.txtItemName);
		txtItemNote = (TextView) view.findViewById(R.id.txtItemNote);

		btnAddToMasterList = (Button) view.findViewById(R.id.btnAddToMasterList);
		btnAddToMasterList.setOnClickListener(this);
		btnClearEditText = (Button) view.findViewById(R.id.btnClearEditText);
		btnClearEditText.setOnClickListener(this);

		llItemNoteInput = (LinearLayout) view.findViewById(R.id.llItemNoteInput);

		mItemsListView = (ListView) view.findViewById(R.id.lvItems);
		if (mItemsListView != null) {

			mItemsListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					// ItemsTable.ToggleStrikeOut(getActivity(), id);
				}
			});
		}

		setFramentStyle();
		return view;

	}

	private void setFramentStyle() {
		switch (mStyle) {
			case MySettings.STYLE_SHOW_LIST:
				if (llItemNoteInput != null) {
					llItemNoteInput.setVisibility(View.GONE);
				}
				break;

			case MySettings.STYLE_SHOW_MASTER_LIST:
				if (llItemNoteInput != null) {
					llItemNoteInput.setVisibility(View.VISIBLE);
				}
				break;
			default:
				break;

		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("AlistFragment", "onActivityCreated() datastoreID:" + mListDatastoreID);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		MyLog.i("AlistFragment", "onStart() datastoreID:" + mListDatastoreID);
	}

	@Override
	public void onResume() {
		super.onResume();

		MyLog.i("AlistFragment", "onResume() datastoreID:" + mListDatastoreID);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mListDatastoreID = bundle.getString(MySettings.LIST_DATASTORE_ID);
			mStyle = bundle.getInt(MySettings.LIST_STYLE, MySettings.STYLE_SHOW_LIST);
		}
		if (mListRecord == null) {
			mListRecord = ListsTable.getRecord(mListDatastoreID);
		}

		EventBus.getDefault().register(this);

		try {
			// Open the right datastore (list).
			mListDatastore = app.datastoreManager.openDatastore(mListDatastoreID);
		} catch (DbxException e) {
			MyLog.e("AlistFragment", "DbxException openDatastore in onResume() datastoreID:" + mListDatastoreID);
			e.printStackTrace();
		}

		if (mListDatastore != null && mListDatastore.isOpen()) {
			// Set up a listener for when the list of datastores changed.
			// In it, we'll see if our list was removed, and if so we'll go back in the back stack.
			mListListener = new DbxDatastoreManager.ListListener() {

				@Override
				public void onDatastoreListChange(DbxDatastoreManager dbxDatastoreManager) {
					Boolean found = false;
					try {
						for (DbxDatastoreInfo info : dbxDatastoreManager.listDatastores()) {
							if (info.id.equals(mListDatastoreID)) {
								found = true;
								break;
							}
						}
					} catch (DbxException e) {
						e.printStackTrace();
					}
					if (!found) {
						// TODO: figure out how to remove this list!
						// This datastore has been deleted.
						// finish();
						return;
					}
				}
			};

			app.datastoreManager.addListListener(mListListener);

			// Listen for changes to this list (datastore).
			mListDatastore.addSyncStatusListener(new DbxDatastore.SyncStatusListener() {

				@Override
				public void onDatastoreStatusChange(DbxDatastore dbxDatastore) {
					updateList();
				}
			});
			updateList();
		}

	}

	@Override
	public void onPause() {
		super.onPause();

		MyLog.i("AlistFragment", "onPause() datastoreID:" + mListDatastoreID);

		// save the list view's position
		SaveItemsListViewPosition();

		mListDatastore.close();
		mListDatastore = null;
		app.datastoreManager.removeListListener(mListListener);
		EventBus.getDefault().unregister(this);
	}

	private void SaveItemsListViewPosition() {
		View v = mItemsListView.getChildAt(0);
		int top = (v == null) ? 0 : v.getTop();
		ListsTable.setListViewPosition(mListDatastoreID,
				mItemsListView.getFirstVisiblePosition(), top);
	}

	@Override
	public void onStop() {
		super.onStop();
		MyLog.i("AlistFragment", "onStop() datastoreID:" + mListDatastoreID);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		MyLog.i("AlistFragment", "onDestroyView() datastoreID:" + mListDatastoreID);
	}

	@Override
	public void onDestroy() {
		MyLog.i("AlistFragment", "onDestroy() datastoreID:" + mListDatastoreID);
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		MyLog.i("AlistFragment", "onDetach() datastoreID:" + mListDatastoreID);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		MyLog.i("AlistFragment", "onViewCreated() datastoreID:" + mListDatastoreID);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
