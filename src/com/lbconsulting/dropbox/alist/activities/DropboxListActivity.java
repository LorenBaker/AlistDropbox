package com.lbconsulting.dropbox.alist.activities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.lbconsulting.dropbox.alist.R;
import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxDatastoreInfo;
import com.dropbox.sync.android.DbxDatastoreManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxRecord;
import com.lbconsulting.dropbox.alist.adapters.ListItemAdapter;
import com.lbconsulting.dropbox.alist.classes.ListsApplication;
import com.lbconsulting.dropbox.alist.database.ItemsTable;
import com.lbconsulting.dropbox.alist.dialogs.SharingDialogFragment;
import com.lbconsulting.dropbox.alist.listeners.OnItemDeletedListener;

// This activity shows the contents of a single list and allows adding and deleting items.
public class DropboxListActivity extends FragmentActivity {

	private ListsApplication app;
	private String dsid;
	private DbxDatastore datastore;
	private EditText listInput;
	private DbxDatastoreManager.ListListener listListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		app = ListsApplication.getInstance();

		setContentView(R.layout.list);

		// Set up the text input for adding an item to the list.
		listInput = new EditText(this);
		listInput.setSingleLine();
		listInput.setHint("Add an item...");
		listInput.setSingleLine();
		listInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// If the enter key is pressed...
				if ((actionId & EditorInfo.IME_ACTION_GO) > 0 || (actionId & EditorInfo.IME_ACTION_SEND) > 0 ||
						(event.getAction() == KeyEvent.ACTION_DOWN &&
								event.getKeyCode() == KeyEvent.KEYCODE_ENTER && listInput.getText().length() > 0)) {

					ItemsTable.CreateNewItem(datastore, listInput.getText().toString(), "No note", "No group");

					// updateList also calls sync()
					updateList();

					listInput.setText("");
					listInput.requestFocus();
					return true;
				}

				// We may also get a key up event.
				if (event.getAction() == KeyEvent.ACTION_UP &&
						event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					listInput.setText("");
					listInput.requestFocus();
					return true;
				}
				return false;
			}
		});
		((ListView) findViewById(R.id.listView)).addFooterView(listInput);

		((Button) findViewById(R.id.shareButton)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!app.datastoreManager.isLocal()) {
					new SharingDialogFragment(datastore).show(getSupportFragmentManager(), "SharingDialogFragment");
				} else {
					new AlertDialog.Builder(DropboxListActivity.this)
							.setTitle("Connect with Dropbox first")
							.setMessage(
									"Sharing is only enabled once you've connected your Dropbox account with this app. Please return to the main screen and log in with Dropbox first.")
							.setPositiveButton(android.R.string.ok, null)
							.show();
				}
			}
		});

		Intent intent = getIntent();

		// Get the datastore ID.
		if (intent.getAction() == Intent.ACTION_VIEW) {
			// This means we were opened via the user clicking a link somewhere.
			// In that case, the URL looks like "https://dslists.site44.com/#<DSID>"
			dsid = intent.getData().getFragment();
		} else {
			// This means we were opened from another activity, which will pass the DSID as an extra.
			dsid = intent.getStringExtra("com.lbconsulting.dropbox.alist.activities.DSID");
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		try {
			// Open the right datastore (list).
			datastore = app.datastoreManager.openDatastore(dsid);
		} catch (DbxException e) {
			e.printStackTrace();
		}

		// Set up a listener for when the list of datastores changed.
		// In it, we'll see if our list was removed, and if so we'll go back in the back stack.
		listListener = new DbxDatastoreManager.ListListener() {

			@Override
			public void onDatastoreListChange(DbxDatastoreManager dbxDatastoreManager) {
				Boolean found = false;
				try {
					for (DbxDatastoreInfo info : dbxDatastoreManager.listDatastores()) {
						if (info.id.equals(dsid)) {
							found = true;
							break;
						}
					}
				} catch (DbxException e) {
					e.printStackTrace();
				}
				if (!found) {
					// This datastore has been deleted.
					finish();
					return;
				}
			}
		};

		app.datastoreManager.addListListener(listListener);

		// Listen for changes to this list (datastore).
		datastore.addSyncStatusListener(new DbxDatastore.SyncStatusListener() {

			@Override
			public void onDatastoreStatusChange(DbxDatastore dbxDatastore) {
				updateList();
			}
		});
		updateList();
	}

	@Override
	public void onPause() {
		super.onPause();

		datastore.close();
		datastore = null;

		app.datastoreManager.removeListListener(listListener);
	}

	// This calls sync() on the datastore and then updates the UI.
	private void updateList() {
		// Call sync()
		try {
			datastore.sync();
		} catch (DbxException e) {
			e.printStackTrace();
		}

		// Just in case the title has changed.
		this.setTitle(datastore.getTitle());

		// Create an adapter for the list and bind everything.
		ListView listView = (ListView) findViewById(R.id.listView);
		ArrayList<DbxRecord> records = ItemsTable.QueryAsList(datastore, null);
		// Sort the records by item name.
		Collections.sort(records,
				new Comparator<DbxRecord>() {

					@Override
					public int compare(DbxRecord a, DbxRecord b) {

						if (!a.getString(ItemsTable.COL_ITEM_NAME).equals(ItemsTable.NOT_AVAILABLE)
								&& !b.getString(ItemsTable.COL_ITEM_NAME).equals(ItemsTable.NOT_AVAILABLE)) {
							return a.getString(ItemsTable.COL_ITEM_NAME).compareTo(
									b.getString(ItemsTable.COL_ITEM_NAME));

						} else {
							return a.getId().compareTo(b.getId());
						}
					}
				});
		// Create a new adapter with the sorted records.
		ListItemAdapter adapter = new ListItemAdapter(records, this, datastore.isWritable());
		// Set the handler for when an item should be deleted.
		adapter.setOnItemDeleted(new OnItemDeletedListener<DbxRecord>() {

			@Override
			public void onItemDeleted(DbxRecord item) {
				// Delete the record and call updateList, which will call sync().
				item.deleteRecord();
				updateList();
			}
		});

		// Only have the footer present if the datastore is writable.
		listView.removeFooterView(listInput);
		if (datastore.isWritable()) listView.addFooterView(listInput);

		listView.setAdapter(adapter);
		listInput.requestFocus();
	}
}
