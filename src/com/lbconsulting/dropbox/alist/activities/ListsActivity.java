package com.lbconsulting.dropbox.alist.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccount;
import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxDatastoreManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxRecord;
import com.lbconsulting.dropbox.alist.R;
import com.lbconsulting.dropbox.alist.adapters.ListsPagerAdapter;
import com.lbconsulting.dropbox.alist.classes.AlistDropboxEvents.UpdateLists;
import com.lbconsulting.dropbox.alist.classes.ListsApplication;
import com.lbconsulting.dropbox.alist.classes.MyLog;
import com.lbconsulting.dropbox.alist.classes.MySettings;
import com.lbconsulting.dropbox.alist.database.ListsTable;
import com.lbconsulting.dropbox.alist.dialogs.ListsDialogFragment;

import de.greenrobot.event.EventBus;

// Our main activity, which displays a list of lists and allows a user to link or unlink a Dropbox account.
public class ListsActivity extends FragmentActivity {

	private DbxAccountManager mAccountManager = null;
	private ListsApplication app = null;
	private static DbxDatastore mAlistDatastore = null;
	private ArrayList<DbxRecord> mListRecords = null;
	private ListsPagerAdapter mListsPagerAdapter = null;
	private ViewPager mPager = null;
	private int mListStyle = MySettings.STYLE_SHOW_LIST;
	private DbxRecord mActiveListRecord = null;

	public static DbxDatastore getAlistDatastore() {
		return mAlistDatastore;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		MyLog.i("Lists_ACTIVITY", "onCreate()");
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_lists);
		setContentView(R.layout.activity_lists_pager);

		this.app = ListsApplication.getInstance();
		MySettings.setContext(this);
		setUpAccountManager();

		if (MySettings.isFirstTimeListsActivityShown()) {
			// Create a new Alist datastore and set its title.

			String alistDatastoreID = MySettings.NOT_AVAILABLE;
			try {
				mAlistDatastore = app.datastoreManager.createDatastore();
				mAlistDatastore.setTitle("AlistDatastore");
				// Sync (to send the title change).
				mAlistDatastore.sync();
				alistDatastoreID = mAlistDatastore.getId();

				Bundle listsActivityBundle = new Bundle();
				listsActivityBundle.putBoolean(MySettings.STATE_LISTS_ACTIVITY_FIRST_TIME_SHOWN, false);
				listsActivityBundle.putString(MySettings.STATE_ALIST_DATASTORE_ID, alistDatastoreID);
				MySettings.set("", listsActivityBundle);

				// CreateNewList();

			} catch (DbxException e) {
				MyLog.e("Lists_ACTIVITY", "DbxException in Resume() while creating Alist Datastore.");
				e.printStackTrace();
			}
		} else {
			openAlistDatastore();
		}

		mListsPagerAdapter = new ListsPagerAdapter(getSupportFragmentManager(), mListStyle);
		mListRecords = ListsTable.QueryAsList(null, MySettings.SORT_ALPHABETICAL);
		mListsPagerAdapter.setListRecords(mListRecords);
		mPager = (ViewPager) findViewById(R.id.listsPager);
		mPager.setAdapter(mListsPagerAdapter);
		mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int state) {
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}

			@Override
			public void onPageSelected(int position) {
				// A list page has been selected
				MyLog.d("Lists_ACTIVITY", "onPageSelected() - position = " + position);
				mActiveListRecord = mListRecords.get(position);
				String listTitle = mActiveListRecord.getString(ListsTable.COL_LIST_TITLE);
				getActionBar().setTitle(listTitle);
			}
		});

	}

	// Called when the user finishes the linking process.
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		MyLog.i("Lists_ACTIVITY", "onActivityResult()");
		if (requestCode == MySettings.REQUEST_LINK_TO_DBX) {
			if (resultCode == Activity.RESULT_OK) {
				DbxAccount account = mAccountManager.getLinkedAccount();
				try {
					// Migrate any local datastores.
					app.datastoreManager.migrateToAccount(account);
					// Start using the remote datastore manager.
					app.datastoreManager = DbxDatastoreManager.forAccount(account);
					setUpDatastoreChangeListeners();
				} catch (DbxException e) {
					e.printStackTrace();
				}
				// TODO: Show unlink button in the menu
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	private void setUpDatastoreChangeListeners() {
		app.datastoreManager.addListListener(new DbxDatastoreManager.ListListener() {

			@Override
			public void onDatastoreListChange(DbxDatastoreManager dbxDatastoreManager) {
				// TODO: Update for the addition or deletion of a list
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MyLog.i("Lists_ACTIVITY", "onCreateOptionsMenu");
		getMenuInflater().inflate(R.menu.lists_activity, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {
			case R.id.action_removeStruckOffItems:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.", Toast.LENGTH_SHORT)
						.show();
				return true;

			case R.id.action_addItem:
				/*Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.", Toast.LENGTH_SHORT)
						.show();*/
				mListStyle = MySettings.STYLE_SHOW_MASTER_LIST;
				mListsPagerAdapter.setListStyle(mListStyle);
				EventBus.getDefault().post(new UpdateLists(mListStyle));
				getActionBar().setDisplayHomeAsUpEnabled(true);
				return true;

			case R.id.action_newList:
				CreateNewList();
				return true;

			case R.id.action_clearList:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.", Toast.LENGTH_SHORT)
						.show();
				return true;

			case R.id.action_emailList:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.", Toast.LENGTH_SHORT)
						.show();
				return true;

			case R.id.action_editListTitle:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.", Toast.LENGTH_SHORT)
						.show();
				return true;

			case R.id.action_deleteList:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.", Toast.LENGTH_SHORT)
						.show();
				return true;

			case R.id.action_Preferences:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.", Toast.LENGTH_SHORT)
						.show();
				return true;

			case R.id.action_about:
				Toast.makeText(this, "\"" + item.getTitle() + "\"" + " is under construction.", Toast.LENGTH_SHORT)
						.show();
				return true;

			case R.id.action_link_to_dropbox:
				mAccountManager.startLink(ListsActivity.this, MySettings.REQUEST_LINK_TO_DBX);
				// TODO: show Dropbox unlink in the menu
				return true;

			case R.id.action_unlink_dropbox:
				if (mAccountManager.hasLinkedAccount()) {
					// If we're linked, unlink and start using a local datastore manager again.
					mAccountManager.unlink();
					app.datastoreManager = DbxDatastoreManager.localManager(mAccountManager);
					// TODO: show Dropbox link in the menu
				}
				return true;

			default:
				return super.onMenuItemSelected(featureId, item);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				mListStyle = MySettings.STYLE_SHOW_LIST;
				mListsPagerAdapter.setListStyle(mListStyle);
				EventBus.getDefault().post(new UpdateLists(mListStyle));
				getActionBar().setDisplayHomeAsUpEnabled(false);
				return true;
			default:
				return super.onOptionsItemSelected(item);

		}

	}

	private void CreateNewList() {
		MyLog.i("Lists_ACTIVITY", "CreateNewList()");
		FragmentManager fm = this.getSupportFragmentManager();
		// Remove any currently showing dialog
		Fragment prev = fm.findFragmentByTag("listsDialogFragment");
		if (prev != null) {
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(prev);
			ft.commit();
		}

		ListsDialogFragment listsDialogFragment = ListsDialogFragment.newInstance("", ListsDialogFragment.NEW_LIST);
		listsDialogFragment.show(fm, "listsDialogFragment");

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MyLog.i("Lists_ACTIVITY", "onPrepareOptionsMenu()");
		if (menu != null) {

		}
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		MyLog.i("Lists_ACTIVITY", "onResume()");
		if (app == null) {
			this.app = ListsApplication.getInstance();
		}

		setUpAccountManager();
		setUpDatastoreChangeListeners();
		MySettings.setContext(this);
		openAlistDatastore();

		/*		if (mAlistDatastore != null) {
					mListRecords = ListsTable.QueryAsList(null, MySettings.SORT_ALPHABETICAL);
					if (mListRecords != null && mListRecords.size() > 0) {
						DbxRecord record = mListRecords.get(mListRecords.size() - 1);
						String datastoreID = record.getString(ListsTable.COL_LIST_DATASTORE_ID);
						if (!datastoreID.isEmpty()) {
							AlistFragment fragment = AlistFragment.newInstance(datastoreID, MySettings.STYLE_SHOW_LIST);
							if (fragment != null) {
								FragmentManager manager = getSupportFragmentManager();
								FragmentTransaction ft = manager.beginTransaction();
								// ft.replace(R.id.content_frame, fragment);
								ft.replace(R.id.content_frame, fragment, record.getString(ListsTable.COL_LIST_TITLE));
								ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
								ft.commit();
							}

							getActionBar().setTitle(record.getString(ListsTable.COL_LIST_TITLE));
						}

					}

				}*/

		/*		ArrayList<DbxDatastoreInfo> infos = new ArrayList<DbxDatastoreInfo>();
				// this.app = ListsApplication.getInstance();
				try {
					infos.addAll(app.datastoreManager.listDatastores());
				} catch (DbxException e) {
					e.printStackTrace();
				}
				// Sort by List Title.
				Collections.sort(infos,
						new Comparator<DbxDatastoreInfo>() {

							@Override
							public int compare(DbxDatastoreInfo a, DbxDatastoreInfo b) {
								if (!a.title.isEmpty() && !b.title.isEmpty()) {
									return a.title.compareToIgnoreCase(b.title);
								} else {
									return a.id.compareTo(b.id);
								}
							}
						});

				if (infos.size() > 0) {
					String datastoreID = infos.get(1).id;
					if (!datastoreID.isEmpty()) {
						AlistFragment fragment = AlistFragment.newInstance(datastoreID, MySettings.STYLE_SHOW_LIST);
						if (fragment != null) {
							FragmentManager manager = getFragmentManager();
							FragmentTransaction ft = manager.beginTransaction();
							ft.replace(R.id.content_frame, fragment);
							// ft.replace(R.id.content_frame, fragment, "fragTag");
							ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
							ft.commit();
						}
					}
				}*/
	}

	private void openAlistDatastore() {
		String datastoreID = MySettings.getAlistDatastoreID();
		if (mAlistDatastore == null || !mAlistDatastore.isOpen()) {
			try {
				mAlistDatastore = app.datastoreManager.openDatastore(MySettings.getAlistDatastoreID());
			} catch (DbxException e) {
				MyLog.e("Lists_ACTIVITY", "DbxException in openAlistDatastore() while opneing Alist Datastore.");
				e.printStackTrace();
			}
		}
	}

	private void setUpAccountManager() {
		// Get the account manager for our app (using our app key and secret).
		if (mAccountManager == null) {
			mAccountManager = DbxAccountManager.getInstance(getApplicationContext(),
					MySettings.DROPBOX_APP_KEY, MySettings.DROPBOX_APP_SECRET);
		}

		if (app.datastoreManager == null) {
			if (mAccountManager.hasLinkedAccount()) {
				// If there's a linked account, use use Dropbox datastores.
				try {
					app.datastoreManager = DbxDatastoreManager.forAccount(mAccountManager.getLinkedAccount());
					// TODO: show unlink menu
				} catch (DbxException.Unauthorized unauthorizedAccount) {
					MyLog.e("Lists_ACTIVITY", "Unauthorized account");
					unauthorizedAccount.printStackTrace();
				}
			} else {
				// Otherwise, use a local datastore manager.
				app.datastoreManager = DbxDatastoreManager.localManager(mAccountManager);
			}
		}
	}

	@Override
	protected void onPause() {
		MyLog.i("Lists_ACTIVITY", "onPause()");

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		MyLog.i("Lists_ACTIVITY", "onDestroy()");
		mAlistDatastore.close();
		mAlistDatastore = null;
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		MyLog.i("Lists_ACTIVITY", "onStart()");
		super.onStart();
	}

	@Override
	protected void onStop() {
		MyLog.i("Lists_ACTIVITY", "onStop()");
		super.onStop();
	}
}
