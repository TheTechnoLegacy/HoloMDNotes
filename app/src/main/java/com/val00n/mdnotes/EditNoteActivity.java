package com.val00n.mdnotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import static android.content.ContentValues.TAG;

public class EditNoteActivity extends Activity {

    // UI elements
    private EditText titleEditText;
    private EditText contentEditText;
    private Button viewButton;
    private Button exitButton;
    // MENU
    private static final int MENU_DELETE = 1;
    private static final int MENU_SAVE = 2;
    // Others
    private FileHelper fileHelper;
    private boolean isNewNote;
    private Note note;
    private String originalFileName;
    private String newFileName;
    private String fileExtension;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_note);

        titleEditText = (EditText) findViewById(R.id.editNoteTitleEditText);
        contentEditText = (EditText) findViewById(R.id.editNoteContentEditText);
        viewButton = (Button) findViewById(R.id.editNoteViewButton);
        exitButton = (Button) findViewById(R.id.editNoteExitButton);

        fileHelper = new FileHelper(EditNoteActivity.this);

        isNewNote = getIntent().getBooleanExtra("IS_NEW", false);
        originalFileName = getIntent().getStringExtra("FILE_NAME");

        if (getIntent().hasExtra("FILE_EXTENSION")) {
            fileExtension = getIntent().getStringExtra("FILE_EXTENSION");
        } else {
            fileExtension = ".md";
        }


        if (!isNewNote) {
            if (originalFileName != null && originalFileName.length() != 0) {
                loadNote(originalFileName);
            } else {
                Toast.makeText(this, "Loading \"" + originalFileName + "\" failed", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        }

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSaveSuccess = saveNote();
                if (isSaveSuccess) {
                    Intent intent = new Intent(EditNoteActivity.this, ViewNoteActivity.class);
                    intent.putExtra("FILE_NAME", newFileName);
                    intent.putExtra("FILE_EXTENSION", fileExtension);
                    startActivity(intent);
                    finish();
                    return;
                }
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSaveSuccess = saveNote();
                if (isSaveSuccess) {
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            boolean isModified = false;
            String currentTitle = titleEditText.getText().toString();
            String currentContent = contentEditText.getText().toString();

            if (note != null) {
                isModified = !currentContent.equals(note.content) || !currentTitle.equals(note.title);
            }

            if (isNewNote && currentTitle.length() > 0) {
                isModified = true;
            }

            if (isModified) {
                new AlertDialog.Builder(this)
                        .setTitle("Save changes?")
                        .setMessage("You can lost unsaved data")
                        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveNote();
                                finish();
                            }
                        })
                        .setNegativeButton("Do not save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNeutralButton("Cancel", null)
                        .show();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem deleteItem = menu.add(0, MENU_DELETE, 0, "Delete");
        deleteItem.setIcon(android.R.drawable.ic_menu_delete);
        MenuItem saveItem = menu.add(0, MENU_SAVE, 1, "Save");
        saveItem.setIcon(android.R.drawable.ic_menu_save);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case MENU_DELETE:
                deleteNote(note.title);
                return true;
            case MENU_SAVE:
                saveNote();
                return true;
            default:
                return  super.onOptionsItemSelected(menuItem);
        }
    }

    private void deleteNote(final String fileTitle) {
        new AlertDialog.Builder(EditNoteActivity.this)
                .setTitle("Delete note \"" + fileTitle + "\"?")
                .setMessage("Are you sure? File can't be recovered after this.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fileName = fileTitle + fileExtension; // wtf
                        fileHelper.deleteFile(fileName);
                        Toast.makeText(EditNoteActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private boolean saveNote() {
        if (isTitleValid(titleEditText.getText().toString())) {
            String title = titleEditText.getText().toString();
            String content = contentEditText.getText().toString();

            note = new Note(title, content);
            newFileName = note.title + fileExtension;

            if (fileHelper.isFileExist(newFileName) && !newFileName.equals(originalFileName)) {
                Toast.makeText(this, "Filename already exist", Toast.LENGTH_SHORT).show();
                return false;
            }

            boolean isSaveSuccessful = fileHelper.saveFile(note, fileExtension); // should create new file if does not exists

            if (isSaveSuccessful) {
                if (!isNewNote && !newFileName.equals(originalFileName)) {
                    fileHelper.deleteFile(originalFileName);
                }

                originalFileName = newFileName;
                Toast.makeText(this, "Saved successfully", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Toast.makeText(this, "Saving failed", Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            Toast.makeText(this, "Invalid title symbols/length", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void loadNote(String fileName) {
        Note loadedNote = fileHelper.loadFile(fileName);
        note = loadedNote;

        titleEditText.setText(loadedNote.title);
        contentEditText.setText(loadedNote.content);
    }

    private boolean isTitleValid(String title) {
        if (title == null) return false;

        if (title.length() > 128 || title.length() < 1) return false;

        return title.indexOf('/') == -1
                && title.indexOf('\\') == -1
                && title.indexOf(':') == -1
                && title.indexOf('*') == -1
                && title.indexOf('?') == -1
                && title.indexOf('"') == -1
                && title.indexOf('\'') == -1
                && title.indexOf('<') == -1
                && title.indexOf('>') == -1
                && title.indexOf('|') == -1
                && title.indexOf('\n') == -1
                && title.indexOf('\r') == -1
                && title.indexOf('#') == -1
                && title.indexOf('$') == -1
                && title.indexOf('{') == -1
                && title.indexOf('}') == -1;
    }
}