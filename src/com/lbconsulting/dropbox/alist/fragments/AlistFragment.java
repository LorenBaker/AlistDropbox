package com.lbconsulting.dropbox.alist.fragments;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
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
import com.lbconsulting.dropbox.alist.classes.ListsApplication;
import com.lbconsulting.dropbox.alist.classes.MyLog;
import com.lbconsulting.dropbox.alist.classes.MySettings;
import com.lbconsulting.dropbox.alist.database.ItemsTable;

public class AlistFragment extends Fragment implements OnClickListener {

	private ListsApplication app;

	private String mDatastoreID = "";
	private DbxDatastore mDatastore = null;
	private int mStyle;

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

	public static AlistFragment newInstance(String datastoreID, int style) {
		AlistFragment f = new AlistFragment();
		// Supply datastoreID input as an argument.
		Bundle args = new Bundle();
		args.putString(MySettings.DATASTORE_ID, datastoreID);
		args.putInt(MySettings.STYLE, style);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		MyLog.i("AlistFragment", "onAttach() datastoreID:" + mDatastoreID);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MyLog.i("AlistFragment", "onCreate() datastoreID:" + mDatastoreID);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		MyLog.i("AlistFragment", "onSaveInstanceState() datastoreID:" + mDatastoreID);
		// Store our listID
		/*		outState.putLong("listID", this.mActiveListID);
				outState.putLong("storeID", this.mActiveStoreID);*/
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("AlistFragment", "onCreateView() datastoreID:" + mDatastoreID);
		Bundle bundle = getArguments();
		if (bundle != null) {
			mDatastoreID = bundle.getString(MySettings.DATASTORE_ID);
			mStyle = bundle.getInt(MySettings.STYLE, MySettings.STYLE_SHOW_LIST);
		}

		app = ListsApplication.getInstance();

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

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("AlistFragment", "onActivityCreated() datastoreID:" + mDatastoreID);
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onStart() {
		super.onStart();
		MyLog.i("AlistFragment", "onStart() datastoreID:" + mDatastoreID);
	}

	@Override
	public void onResume() {
		super.onResume();

		MyLog.i("AlistFragment", "onResume() datastoreID:" + mDatastoreID);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mDatastoreID = bundle.getString(MySettings.DATASTORE_ID);
			mStyle = bundle.getInt(MySettings.STYLE, MySettings.STYLE_SHOW_LIST);
		}

		try {
			// Open the right datastore (list).
			mDatastore = app.datastoreManager.openDatastore(mDatastoreID);
		} catch (DbxException e) {
			MyLog.e("AlistFragment", "DbxException openDatastore in onResume() datastoreID:" + mDatastoreID);
			e.printStackTrace();
		}

		if (mDatastore != null) {
			/*ArrayList<DbxRecord> items = ItemsTable.QueryAsList(mDatastore, null);
			mListItemArrayAdapter = new ListItemArrayAdapter(getActivity(), items, mStyle);
			mItemsListView.setAdapter(mListItemArrayAdapter);*/

			// Set up a listener for when the list of datastores changed.
			// In it, we'll see if our list was removed, and if so we'll go back in the back stack.
			mListListener = new DbxDatastoreManager.ListListener() {

				@Override
				public void onDatastoreListChange(DbxDatastoreManager dbxDatastoreManager) {
					Boolean found = false;
					try {
						for (DbxDatastoreInfo info : dbxDatastoreManager.listDatastores()) {
							if (info.id.equals(mDatastoreID)) {
								found = true;
								break;
							}
						}
					} catch (DbxException e) {
						e.printStackTrace();
					}
					if (!found) {
						// TODO: figure out how to remvoe this list!
						// This datastore has been deleted.
						// finish();
						return;
					}
				}
			};

			app.datastoreManager.addListListener(mListListener);

			// Listen for changes to this list (datastore).
			mDatastore.addSyncStatusListener(new DbxDatastore.SyncStatusListener() {

				@Override
				public void onDatastoreStatusChange(DbxDatastore dbxDatastore) {
					updateList();
				}
			});
			updateList();
		}
	}

	protected void updateList() {
		// TODO Auto-generated method stub
		if (mDatastore != null) {
			ArrayList<DbxRecord> items = ItemsTable.QueryAsList(mDatastore, null);
			mListItemArrayAdapter = new ListItemArrayAdapter(getActivity(), items, mStyle);
			mItemsListView.setAdapter(mListItemArrayAdapter);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		MyLog.i("AlistFragment", "onPause() datastoreID:" + mDatastoreID);
		mDatastore.close();
		mDatastore = null;
		app.datastoreManager.removeListListener(mListListener);
	}

	@Override
	public void onStop() {
		super.onStop();
		MyLog.i("AlistFragment", "onStop() datastoreID:" + mDatastoreID);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		MyLog.i("AlistFragment", "onDestroyView() datastoreID:" + mDatastoreID);
	}

	@Override
	public void onDestroy() {
		MyLog.i("AlistFragment", "onDestroy() datastoreID:" + mDatastoreID);
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		MyLog.i("AlistFragment", "onDetach() datastoreID:" + mDatastoreID);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		MyLog.i("AlistFragment", "onViewCreated() datastoreID:" + mDatastoreID);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

}
