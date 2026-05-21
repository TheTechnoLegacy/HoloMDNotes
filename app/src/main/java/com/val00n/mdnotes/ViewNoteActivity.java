package com.val00n.mdnotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.text.Spanned;
import android.view.MenuItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.markdownj.*;

public class ViewNoteActivity extends Activity {

    // UI elements
    private TextView loadingPlaceholder;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView contentTextView;
    private Button editButton;
    private Button exitButton;
    // MENU
    private static final int MENU_SHARE = 1;
    private static final int MENU_DELETE = 2;
    // Others
    private static final MarkdownProcessor PROCESSOR = new MarkdownProcessor();
    private SharedPreferences preferences;
    private FileHelper fileHelper;
    private String fileName;
    private String fileExtension;
    private Note note;
    private String parsedContent;
    private boolean isLoading;
    private Spanned noteHtml;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_note);

        loadingPlaceholder = (TextView) findViewById(R.id.viewNoteLoadingPlaceholder);
        titleTextView = (TextView) findViewById(R.id.viewNoteTitleText);
        dateTextView = (TextView) findViewById(R.id.viewNoteDateText);
        contentTextView = (TextView) findViewById(R.id.viewNoteContentText);
        editButton = (Button) findViewById(R.id.viewNoteEditButton);
        exitButton = (Button) findViewById(R.id.viewNoteExitButton);

        fileHelper = new FileHelper(ViewNoteActivity.this);

        fileName = getIntent().getStringExtra("FILE_NAME");
        fileExtension = getIntent().getStringExtra("FILE_EXTENSION");

        if (fileName != null && fileName.length() != 0) {
            loadNote(fileName);
        } else {
            Toast.makeText(this, "Loading \"" + fileName + "\" failed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewNoteActivity.this, EditNoteActivity.class);
                intent.putExtra("FILE_NAME", fileName);
                intent.putExtra("FILE_EXTENSION", fileExtension);
                startActivity(intent);
                finish();
            }
        });

        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem shareItem = menu.add(0, MENU_SHARE, 0, "Share");
        shareItem.setIcon(android.R.drawable.ic_menu_share);
        MenuItem deleteItem = menu.add(0, MENU_DELETE, 1, "Delete");
        deleteItem.setIcon(android.R.drawable.ic_menu_delete);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case MENU_DELETE:
                if (!isLoading) {
                    deleteNote(note.title);
                } else {
                    Toast.makeText(this, "File not loaded yet!", Toast.LENGTH_SHORT).show();
                }
                return true;
            case MENU_SHARE:
                if (!isLoading) {
                    shareNote();
                } else {
                    Toast.makeText(this, "File not loaded yet!", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return  super.onOptionsItemSelected(menuItem);
        }
    }

    private void shareNote() {
        new AlertDialog.Builder(ViewNoteActivity.this)
                .setTitle("Send note")
                .setMessage("Choose send method:")
                .setPositiveButton("As plain text", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, note.getWholeNote());
                        startActivity(Intent.createChooser(sendIntent, "Send \"" + note.title + "\""));
                    }
                })
                .setNeutralButton("As file", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.setType("application/octet-stream");
                        sendIntent.putExtra(Intent.EXTRA_STREAM, fileHelper.getFileUri(note, fileExtension));
                        try {
                            startActivity(Intent.createChooser(sendIntent, "Send \"" + note.title + "\""));
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            Toast.makeText(ViewNoteActivity.this, "Failed to send note as file", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNote(final String fileTitle) {
        new AlertDialog.Builder(ViewNoteActivity.this)
                .setTitle("Delete note \"" + fileTitle + "\"?")
                .setMessage("Are you sure? File can't be recovered after this.")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String fileName = fileTitle + fileExtension;
                        fileHelper.deleteFile(fileName);
                        Toast.makeText(ViewNoteActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadDate() {
        String lastModified;
        long timestamp = note.lastModified;
        if (timestamp > 0) {
            Date date = new Date(timestamp);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

            lastModified = simpleDateFormat.format(date);
            dateTextView.setText("Last modified: " + lastModified);
        }
    }

    private void loadNote(final String fileName) {
        isLoading = true;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final Note loadedNote = fileHelper.loadFile(fileName);
                if (loadedNote == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                    return;
                }

                final String markdownHtml = PROCESSOR.markdown(loadedNote.content);
                final Note noteRef = loadedNote;
                final String htmlRef = markdownHtml;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            note = loadedNote;
                            parsedContent = markdownHtml;

                            loadingPlaceholder.setText("Parsing html...");
                            noteHtml = Html.fromHtml(markdownHtml);

                            loadingPlaceholder.setVisibility(View.GONE);
                            isLoading = false;
                            titleTextView.setText(note.title);
                            loadDate();
                            contentTextView.setText(noteHtml);
                        }
                    }
                });
            }
        });
        thread.start();
    }
}
