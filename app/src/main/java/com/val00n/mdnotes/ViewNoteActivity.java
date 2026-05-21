package com.val00n.mdnotes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.Menu;
import android.text.Spanned;
import android.view.MenuItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.markdownj.*;

import static android.content.ContentValues.TAG;

public class ViewNoteActivity extends Activity {

    // UI elements
    private FrameLayout contentContainer;
    private TextView loadingPlaceholder;
    private TextView titleTextView;
    private TextView dateTextView;
    private TextView contentTextView;
    private WebView contentWebView;
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
    private String parsedMarkdownContent;
    private boolean isLoading;
    private Spanned parsedHtmlText;
    private int renderMode; // 0 Markdownj + TextView; 1 Markdownj + WebView; 2 plain text

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_note);
        preferences = getSharedPreferences("globalSettings", 0);

        fileName = getIntent().getStringExtra("FILE_NAME");
        fileExtension = getIntent().getStringExtra("FILE_EXTENSION");

        fileHelper = new FileHelper(ViewNoteActivity.this);

        contentContainer = (FrameLayout) findViewById(R.id.viewNoteContentContainer);
        loadingPlaceholder = (TextView) findViewById(R.id.viewNoteLoadingPlaceholder);
        titleTextView = (TextView) findViewById(R.id.viewNoteTitleText);
        dateTextView = (TextView) findViewById(R.id.viewNoteDateText);
        editButton = (Button) findViewById(R.id.viewNoteEditButton);
        exitButton = (Button) findViewById(R.id.viewNoteExitButton);

        if (isFileMetaValid()) {
            loadNote(fileName);
        } else {
            finish();
            return;
        }

        renderMode = loadRenderMode();

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

    private boolean isFileMetaValid() {
        if (fileName == null || fileName.length() == 0 || fileExtension == null || fileExtension.length() == 0) {
            Toast.makeText(this, "Loading \"" + fileName + "\" failed", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private int loadRenderMode() {
        switch (fileExtension) {
            case ".md":
                return preferences.getInt("mdRender", 0);
            case ".txt":
                return preferences.getInt("txtRender", 2);
            case ".html":
                return preferences.getInt("htmlRender", 1);
            default:
                Log.d(TAG, "onCreate: Unknown note extension. Using plain text render.");
                return 2; // plain text;
        }
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

    private void displayNote() {
        titleTextView.setText(note.title);
        loadDate();

        switch (renderMode) {
            case 0: // Markdownj HTML to TextView text
                contentTextView = new TextView(this);
                contentTextView.setTextSize(16);
                contentTextView.setLinksClickable(true);
                contentContainer.addView(contentTextView);

                parsedHtmlText = Html.fromHtml(parsedMarkdownContent);
                contentTextView.setText(parsedHtmlText);
                break;
            case 1: // Markdownj HTML direct display via WebView
                contentWebView = new WebView(this);
                contentWebView.setBackgroundColor(Color.TRANSPARENT);
                contentWebView.setPadding(0,0,0,0);
                contentWebView.getSettings().setBuiltInZoomControls(false);
                contentWebView.getSettings().setUseWideViewPort(false);
                contentWebView.setInitialScale(100);
                contentWebView.setVerticalScrollBarEnabled(false);
                contentWebView.setHorizontalScrollBarEnabled(false);
                contentWebView.setScrollContainer(false);
                contentWebView.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.FILL_PARENT,
                        FrameLayout.LayoutParams.FILL_PARENT
                ));

                contentContainer.addView(contentWebView);

                parsedMarkdownContent = setWebViewAppearance(parsedMarkdownContent);
                contentWebView.loadDataWithBaseURL(null, parsedMarkdownContent, "text/html", "UTF-8", null);
                break;
            case 2: // just raw file text :D
                contentTextView = new TextView(this);
                contentTextView.setTextSize(16);
                contentContainer.addView(contentTextView);

                contentTextView.setText(note.content);
                break;
        }

        loadingPlaceholder.setVisibility(View.GONE);
    }

    private String setWebViewAppearance(String body) {
        int textColorInt = getThemeColor(android.R.attr.textColorSecondary); // wtf
        String textColorHex = String.format("#%06X", (0xFFFFFF & textColorInt));

        return "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "  body {\n" +
                "    margin: 0;\n" +
                "    padding: 0;\n" +
                "    color: " + textColorHex +";\n" +
                "    font-size: 16px;\n" +
                "    font-family: sans-serif;\n" +
                "    line-height: 1.4;\n" +
                "    background-color: transparent;\n" +
                "  }\n" +
                "  blockquote {\n" +
                "    margin: 0.1em 0;\n" +
                "    padding: 0.1em 0.9em;\n" +
                "    border-left: 3px solid " + textColorHex + ";\n" +
                "  }\n" +
                "  pre, code {\n" +
                "    white-space: pre-wrap;\n" +
                "    word-wrap: break-word;\n" +
                "    max-width: 100%;\n" +
                "    font-family: monospace;\n" +
                "  }\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" + body + "</body>\n</html>";
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

                final String markdownHtml;
                if (renderMode != 2) {
                    markdownHtml = PROCESSOR.markdown(loadedNote.content);
                } else {
                    markdownHtml = null;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            note = loadedNote;
                            parsedMarkdownContent = markdownHtml;
                            isLoading = false;
                            displayNote();
                        }
                    }
                });
            }
        });
        thread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (contentWebView != null) {
            contentWebView.destroy();
            contentContainer.removeView(contentWebView);
        }
    }

    private int getThemeColor(int attrResId) {
        TypedValue out = new TypedValue();
        getTheme().resolveAttribute(attrResId, out, true);

        if (out.resourceId != 0) {
            return getResources().getColor(out.resourceId);
        }
        return out.data;
    }
}
