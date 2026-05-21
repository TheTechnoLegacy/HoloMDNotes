package com.val00n.mdnotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

	// UI elements
	private EditText searchEditText;
	private ListView noteListView;
	private ImageButton settingsButton;
    private ImageButton sortButton;
	private Button newNoteButton;
	private TextView emptyListText;
	// MENU
	private static final int MENU_ABOUT = 1;
	private static final int MENU_SETTINGS = 2;
	// Others
	private FileHelper fileHelper;
	private ArrayAdapter<String> arrayAdapter;
	private SharedPreferences preferences;
	private int sortMode = 0;
	private boolean hasSDCard;
	ArrayList<FileMeta> filteredFiles;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		final Context context = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		searchEditText = (EditText) findViewById(R.id.mainSearchEditText);
		noteListView = (ListView) findViewById(R.id.mainNoteList);
		settingsButton = (ImageButton) findViewById(R.id.mainSettingsButton);
        sortButton = (ImageButton) findViewById(R.id.mainSortButton);
		newNoteButton = (Button) findViewById(R.id.mainNewNoteButton);
		emptyListText = (TextView) findViewById(R.id.mainEmptyListTextView);

		preferences = getSharedPreferences("mainSettings", 0);
		sortMode = preferences.getInt("sortMode", 0);

		if (!preferences.contains("hasSDCard")) {
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
				preferences.edit().putBoolean("hasSDCard", true).commit();
			} else {
				preferences.edit().putBoolean("hasSDCard", false).commit();
			}
		}

		hasSDCard = preferences.getBoolean("hasSDCard", false);

		checkVaultPath();

		fileHelper = new FileHelper(this);

		arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		noteListView.setAdapter(arrayAdapter);

		noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String fileName = filteredFiles.get(position).fileName + filteredFiles.get(position).extension;
				Intent intent = new Intent(MainActivity.this, ViewNoteActivity.class);
				intent.putExtra("FILE_NAME", fileName);
				intent.putExtra("FILE_EXTENSION", filteredFiles.get(position).extension);
				startActivity(intent);
			}
		});

        noteListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final String fileTitle = filteredFiles.get(position).fileName;
				final String fileExtension = filteredFiles.get(position).extension;

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete note \"" + fileTitle + "\"?")
                        .setMessage("Are you sure? File can't be recovered after this.")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String fileName = fileTitle + fileExtension; // FIXME
                                fileHelper.deleteFile(fileName);
                                Toast.makeText(MainActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                refreshNoteList(null);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();

                return false;
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });

		newNoteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, EditNoteActivity.class);
				intent.putExtra("IS_NEW", true);
				startActivity(intent);
			}
		});

		searchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				String query = s.toString();

				if (query.length() != 0) {
					refreshNoteList(query);
				} else {
					refreshNoteList(null);
				}
			}
		});

		sortButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] options = {"Date (new to old)", "Date (old to new)", "Title (A-Z)", "Title (Z-A)"};

				new AlertDialog.Builder(MainActivity.this)
						.setTitle("Sort by")
						.setSingleChoiceItems(options, sortMode, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
							sortMode = which;
							}
						})
						.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								preferences.edit().putInt("sortMode", sortMode).commit();
								refreshNoteList(null);
							}
						})
						.setNegativeButton("Cancel", null)
						.show();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		MenuItem deleteItem = menu.add(0, MENU_ABOUT, 0, "About");
		deleteItem.setIcon(android.R.drawable.ic_menu_info_details);
		MenuItem saveItem = menu.add(0, MENU_SETTINGS, 1, "Settings");
		saveItem.setIcon(android.R.drawable.ic_menu_preferences);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
			case MENU_ABOUT:
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("About")
						.setMessage("MDNotes 1.0.0\n\nSimple notes app for old android devices, with markdown support :3\n\nval00n  2026")
						.setNeutralButton("Ok", null)
						.show();
				return true;
			case MENU_SETTINGS:
				Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
				startActivity(intent);
				return true;
			default:
				return  super.onOptionsItemSelected(menuItem);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshNoteList(null);
		checkVaultPath();
	}

	private void checkVaultPath() {
		boolean currentState = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
		if (hasSDCard != currentState) {
			if (currentState) {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("Vault location changed")
						.setMessage("MDNotes detected change in vault path. This happened because sdcard was inserted. You should copy your notes from MDNotes data directory into sdcard to see them, or eject sdcard.")
						.setNeutralButton("Ok", null)
						.show();

				preferences.edit().putBoolean("hasSDCard", true).commit();
				hasSDCard = true;
			}
			else {
				new AlertDialog.Builder(MainActivity.this)
						.setTitle("Vault location changed")
						.setMessage("MDNotes detected change in vault path. This happened because sdcard was ejected. You should copy your notes from sdcard into MDNotes data directory to see them, or put sdcard back.")
						.setNeutralButton("Ok", null)
						.show();

				preferences.edit().putBoolean("hasSDCard", false).commit();
				hasSDCard = false;
			}
		}
	}

	private void refreshNoteList(String query) {
		arrayAdapter.clear();
		emptyListText.setVisibility(View.GONE);

		ArrayList<FileMeta> availableFiles = fileHelper.getAvailableNotesList();
		if (availableFiles == null || availableFiles.size() == 0) {
			emptyListText.setVisibility(View.VISIBLE);
			emptyListText.setText("there is no notes yet");
			return;
		}

		if (query != null) {
			query.toLowerCase();
			filteredFiles = new ArrayList<FileMeta>();

			FileMeta currentFile;
			for (int i = 0; i < availableFiles.size(); i++) {
				currentFile = availableFiles.get(i);
				if (currentFile.fileName.toLowerCase().contains(query)) {
					filteredFiles.add(currentFile);
				}
			}
		} else {
			filteredFiles = new ArrayList<FileMeta>(availableFiles);
		}

		if (filteredFiles.size() == 0) {
			emptyListText.setVisibility(View.VISIBLE);
			emptyListText.setText("nothing found");
			return;
		}

		switch (sortMode) {
			case 0:
				Collections.sort(filteredFiles, new LastModifiedComparator());
				break;
			case 1:
				Collections.sort(filteredFiles, new LastModifiedComparator());
				Collections.reverse(filteredFiles);
				break;
			case 2:
				Collections.sort(filteredFiles, new FileNameComparator());
				break;
			case 3:
				Collections.sort(filteredFiles, new FileNameComparator());
				Collections.reverse(filteredFiles);
				break;
			default:
				Collections.sort(filteredFiles, new LastModifiedComparator());
				break;
		}

		String fileName;
		for (int i = 0; i < filteredFiles.size(); i++) {
			fileName = filteredFiles.get(i).fileName;
			arrayAdapter.add(fileName);
		}

		arrayAdapter.notifyDataSetChanged();
	}
}