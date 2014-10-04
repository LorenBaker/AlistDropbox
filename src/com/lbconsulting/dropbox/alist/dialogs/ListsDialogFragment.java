package com.lbconsulting.dropbox.alist.dialogs;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.dropbox.sync.android.DbxDatastore;
import com.dropbox.sync.android.DbxException;
import com.lbconsulting.dropbox.alist.R;
import com.lbconsulting.dropbox.alist.activities.ListsActivity;
import com.lbconsulting.dropbox.alist.classes.ListsApplication;
import com.lbconsulting.dropbox.alist.classes.MyLog;
import com.lbconsulting.dropbox.alist.database.ItemsTable;
import com.lbconsulting.dropbox.alist.database.ListsTable;

public class ListsDialogFragment extends DialogFragment {

	public static final int EDIT_LIST_TITLE = 30;
	public static final int NEW_LIST = 40;

	private static final int BLANK_LIST_TEMPLATE = 0;
	private static final int GROCERIES_LIST_TEMPLATE = 1;
	private static final int TO_DO_LIST_TEMPLATE = 2;

	private ListsApplication app;

	private Button btnApply;
	private Button btnCancel;
	private static EditText txtEditListTitle;
	private static Spinner spinListTemplate;
	private static ProgressBar pbLoadingIndicator;
	private static LinearLayout llButtons;

	ProgressDialog progressDialog;

	private int mDialogType;
	private String mActiveListTitle;

	public interface SortOrderDialogListener {

		void onApplySortOrderDialog(int sortOrderResult);
	}

	public ListsDialogFragment() {
		// Empty constructor required for DialogFragment
	}

	public static ListsDialogFragment newInstance(String listTitle, int dialogType) {
		MyLog.i("ListsDialogFragment", "newInstance. listTitle:" + listTitle + ". dialogType:" + dialogType);
		ListsDialogFragment f = new ListsDialogFragment();
		// Supply itemID input as an argument.
		Bundle args = new Bundle();
		args.putString("listTitle", listTitle);
		args.putInt("dialogType", dialogType);
		f.setArguments(args);
		return f;
	}

	@Override
	public void onAttach(Activity activity) {
		MyLog.i("ListsDialogFragment", "onAttach");
		super.onAttach(activity);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		MyLog.i("ListsDialogFragment", "onSaveInstanceState");
		// Store our listID
		outState.putString("listTitle", this.mActiveListTitle);
		outState.putLong("dialogType", this.mDialogType);
		super.onSaveInstanceState(outState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		MyLog.i("ListsDialogFragment", "onCreateView");
		if (savedInstanceState != null && savedInstanceState.containsKey("listTitle")) {
			mActiveListTitle = savedInstanceState.getString("listTitle");
			mDialogType = savedInstanceState.getInt("dialogType", 0);
		} else {
			Bundle bundle = getArguments();
			if (bundle != null) {
				mActiveListTitle = bundle.getString("listTitle");
				mDialogType = bundle.getInt("dialogType", 0);
			}
		}

		this.app = ListsApplication.getInstance();

		/*		if (mActiveListID > 0) {
					mListSettings = new ListSettings(getActivity(), mActiveListID);
				}*/

		// inflate view
		View view = null;
		switch (mDialogType) {

			case EDIT_LIST_TITLE:
				MyLog.i("ListsDialogFragment", "onCreateView: Edit List Title");
				view = inflater.inflate(R.layout.dialog_edit_list_title, container);
				getDialog().setTitle(R.string.dialog_edit_list_title);
				break;

			case NEW_LIST:
				MyLog.i("ListsDialogFragment", "onCreateView: New List");
				view = inflater.inflate(R.layout.dialog_new_list, container);
				getDialog().setTitle(R.string.dialog_title_create_new_list);
				break;

			default:
				break;
		}

		if (view != null) {
			btnApply = (Button) view.findViewById(R.id.btnApply);
			btnApply.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// ContentValues newFieldValues = new ContentValues();
					switch (mDialogType) {

						case EDIT_LIST_TITLE:
							/*							String newListTitle = txtEditListTitle.getText().toString();
														newListTitle = newListTitle.trim();
														newFieldValues.put(ListsTable.COL_LIST_TITLE, newListTitle);
														mListSettings.updateListsTableFieldValues(newFieldValues);
														EventBus.getDefault().post(new ListTitleChanged(mActiveListID, newListTitle));*/
							getDialog().dismiss();
							break;

						case NEW_LIST:
							mActiveListTitle = txtEditListTitle.getText().toString().trim();
							if (!mActiveListTitle.isEmpty()) {

								try {
									// Create a new datastore and set its title.
									DbxDatastore listDatastore = app.datastoreManager.createDatastore();
									listDatastore.setTitle(mActiveListTitle);

									// Sync (to send the title change).
									listDatastore.sync();

									DbxDatastore alistDatastore = ListsActivity.getAlistDatastore();
									if (alistDatastore != null) {
										if (alistDatastore.isOpen()) {
											ListsTable.CreateNewList(mActiveListTitle, listDatastore.getId());
										} else {
											MyLog.e("ListsDialogFragment",
													"btnApply.onClick NEW_LIST: alistDatastore is not open!");
										}
									} else {
										MyLog.e("ListsDialogFragment",
												"btnApply.onClick NEW_LIST: alistDatastore is null!");
									}

									switch (spinListTemplate.getSelectedItemPosition()) {
										case BLANK_LIST_TEMPLATE:
											// EventBus.getDefault().post(new NewListCreated(newListID));
											// getDialog().dismiss();
											break;

										case GROCERIES_LIST_TEMPLATE:
											FillGroceriesList(listDatastore);
											listDatastore.sync();
											break;

										case TO_DO_LIST_TEMPLATE:
											/*											FillToDoList(datastore);
																						EventBus.getDefault().post(new NewListCreated(newListID));
																						getDialog().dismiss();*/
											break;

										default:
											break;
									}

									// Close the datastore. (It will be opened again if the user taps on that list.)
									listDatastore.close();
									/*									listInput.setText("");
																		listInput.requestFocus();*/
								} catch (DbxException e) {
									e.printStackTrace();
								}

							}
							break;

						default:
							break;
					}

					getDialog().dismiss();
				}
			});

			btnCancel = (Button) view.findViewById(R.id.btnCancel);
			btnCancel.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getDialog().dismiss();
				}
			});

			txtEditListTitle = (EditText) view.findViewById(R.id.txtEditListTitle);
			if (txtEditListTitle != null) {
				switch (mDialogType) {
					case EDIT_LIST_TITLE:
						// We're displaying the Edit List Title dialog
						/*						mListTitle = mListSettings.getListTitle();
												txtEditListTitle.setText(mListTitle);*/
						break;

					case NEW_LIST:
						// We're creating a new List
						pbLoadingIndicator = (ProgressBar) view.findViewById(R.id.pbLoadingIndicator);
						// rlCircularLoadingProgressIndicator = (RelativeLayout)
						// view.findViewById(R.id.rlCircularLoadingProgressIndicator);
						llButtons = (LinearLayout) view.findViewById(R.id.llButtons);
						spinListTemplate = (Spinner) view.findViewById(R.id.spinListTemplate);
						if (spinListTemplate != null) {
							fillSpinListTemplate();
							spinListTemplate.setOnItemSelectedListener(new OnItemSelectedListener() {

								@Override
								public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
									switch (position) {
										case BLANK_LIST_TEMPLATE:
											txtEditListTitle.setText("");
											break;
										case GROCERIES_LIST_TEMPLATE:
											txtEditListTitle.setText(
													getActivity().getString(R.string.dialog_lists_groceries_list_text));
											break;

										case TO_DO_LIST_TEMPLATE:
											txtEditListTitle.setText(
													getActivity().getString(R.string.dialog_lists_to_do_list_text));
											break;

										default:
											break;
									}
								}

								@Override
								public void onNothingSelected(AdapterView<?> arg0) {
									// do nothing
								}
							});
						}
						break;

					default:
						break;

				}

				if (txtEditListTitle.requestFocus()) {
					getDialog().getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				}
			}
		}

		return view;
	}

	public static void ShowLoadingIndicator() {

		if (pbLoadingIndicator != null) {
			pbLoadingIndicator.setVisibility(View.VISIBLE);
			spinListTemplate.setVisibility(View.GONE);
			txtEditListTitle.setVisibility(View.GONE);
			llButtons.setVisibility(View.GONE);
		}
	}

	/*	protected void FillToDoList(long todosListID) {
			AListContentProvider.setSuppressDropboxChanges(false);

			ArrayList<Long> todoGroupIDs = new ArrayList<Long>();
			// create to do groups
			String[] todoGroups = this.getResources().getStringArray(R.array.todo_groups);
			for (int i = 0; i < todoGroups.length; i++) {
				todoGroupIDs.add(GroupsTable.CreateNewGroup(getActivity(), todosListID, todoGroups[i]));
			}

			// create to do items
			String[] todoItems = getActivity().getResources().getStringArray(R.array.todo_items);

			for (int i = 0; i < todoItems.length; i++) {
				ItemsTable.CreateNewItem(getActivity(), todosListID, todoItems[i], todoGroupIDs.get(i));
			}
			mActiveListID = todosListID;
			// AListContentProvider.setSuppressDropboxChanges(false);
		}*/

	private void fillSpinListTemplate() {
		ArrayList<String> spinListItems = new ArrayList<String>();
		spinListItems.add(getActivity().getString(R.string.dialog_lists_blank_list_text)); // 0
		spinListItems.add(getActivity().getString(R.string.dialog_lists_groceries_list_text)); // 1
		spinListItems.add(getActivity().getString(R.string.dialog_lists_to_do_list_text)); // 2
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_item, spinListItems);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinListTemplate.setAdapter(dataAdapter);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		MyLog.i("ListsDialogFragment", "onActivityCreated");
		super.onActivityCreated(savedInstanceState);

		Bundle bundle = this.getArguments();
		if (bundle != null) {
			mActiveListTitle = bundle.getString("listTitle");
		}
	}

	/*	public void onRadioButtonClicked(View view) {
			MyLog.i("ListsDialogFragment", "onRadioButtonClicked; view id = " + view.getId());
			switch (view.getId()) {
			case R.id.rbAlphabetical_list:
				mSortOrderResult = ListPreferencesFragment.ALPHABETICAL;
				break;

			case R.id.rbManual:
				mSortOrderResult = ListPreferencesFragment.MANUAL;
				break;
			case R.id.rbAlphabetical_master_list:
				mSortOrderResult = ListPreferencesFragment.ALPHABETICAL;
				break;
			case R.id.rbSelectedItemsAtTop:
				mSortOrderResult = ListPreferencesFragment.SELECTED_AT_TOP;
				break;
			case R.id.rbSelectedItemsAtBottom:
				mSortOrderResult = ListPreferencesFragment.SELECTED_AT_BOTTOM;
				break;
			case R.id.rbLastUsed:
				mSortOrderResult = ListPreferencesFragment.LAST_USED;
				break;
			default:
				break;
			}
			String toastMsg = "Selection = " + mSortOrderResult;
			Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_SHORT).show();
		}*/

	@Override
	public void onCreate(Bundle savedInstanceState) {
		MyLog.i("ListsDialogFragment", "onCreate");
		super.onCreate(savedInstanceState);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		MyLog.i("ListsDialogFragment", "onCreateDialog");
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		MyLog.i("ListsDialogFragment", "onDestroyView");
		super.onDestroyView();
	}

	@Override
	public void onDetach() {
		MyLog.i("ListsDialogFragment", "onDetach");
		super.onDetach();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		MyLog.i("ListsDialogFragment", "onDismiss");
		super.onDismiss(dialog);
	}

	@Override
	public void onStart() {
		MyLog.i("ListsDialogFragment", "onStart");
		super.onStart();
	}

	@Override
	public void onStop() {
		MyLog.i("ListsDialogFragment", "onStop");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		MyLog.i("ListsDialogFragment", "onDestroy");
		super.onDestroy();
	}

	/*	private class CreateGroceriesList extends AsyncTask<Long, Void, Long> {

			@Override
			protected void onPreExecute() {
				getDialog().setTitle(R.string.dialog_lists_loading_groceries_list_text);
				ShowLoadingIndicator();
			}

			@Override
			protected Long doInBackground(Long... newListID) {
				FillGroceriesList(newListID[0]);
				return (newListID[0]);
			}

			@Override
			protected void onPostExecute(Long newListID) {
				EventBus.getDefault().post(new NewListCreated(newListID));
				getDialog().dismiss();
			}

			@Override
			protected void onProgressUpdate(Void... values) {
			}

		}*/

	private void FillGroceriesList(DbxDatastore datastore) {

		// create grocery items
		// NOTE: this only works if R.array.grocery_items and
		// R.array.grocery_items_groups
		// are in the proper order!!!!!!
		String[] groceryItems = this.getResources().getStringArray(R.array.grocery_items);
		// String[] groceryItemGroups = this.getResources().getStringArray(R.array.grocery_items_groups);

		for (int i = 0; i < groceryItems.length; i++) {
			ItemsTable.CreateNewItem(datastore, groceryItems[i], null, null);
		}

	}

}
