package com.val00n.mdnotes;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

// A HUGE TODO: settings
public class SettingsActivity extends Activity {

    // UI
    private Spinner renderMdSpinner;
    private Spinner renderTxtSpinner;
    private Spinner renderHtmlSpinner;
    private Spinner uiActionsShortTapSpinner;
    private Spinner uiActionsLongTapSpinner;
    private ArrayAdapter<String> renderMdSpinnerAdapter;
    private ArrayAdapter<String> renderTxtSpinnerAdapter;
    private ArrayAdapter<String> renderHtmlSpinnerAdapter;
    private ArrayAdapter<String> uiActionsShortTapSpinnerAdapter;
    private ArrayAdapter<String> uiActionsLongTapSpinnerAdapter;
    private CheckBox uiActionsDisableDeleteDialogCheckBox;
    private CheckBox uiActionsShowNoteDateCheckBox;
    // OTHERS
    private SharedPreferences preferences;
    private String[] renderOptions;
    private String[] tapOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        preferences = getSharedPreferences("globalSettings", 0);

        renderOptions = new String[]{"TextView", "WebView", "Raw text"};
        renderMdSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, renderOptions);
        renderTxtSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, renderOptions);
        renderHtmlSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, renderOptions);

        tapOptions = new String[]{"View", "Edit", "Delete", "Nothing"};
        uiActionsShortTapSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tapOptions);
        uiActionsLongTapSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tapOptions);

        renderMdSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        renderTxtSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        renderHtmlSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        uiActionsShortTapSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        uiActionsLongTapSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        renderMdSpinner = (Spinner) findViewById(R.id.settingsRenderMdSpinner);
        renderTxtSpinner = (Spinner) findViewById(R.id.settingsRenderTxtSpinner);
        renderHtmlSpinner = (Spinner) findViewById(R.id.settingsRenderHtmlSpinner);
        uiActionsShortTapSpinner = (Spinner) findViewById(R.id.settingsShortTapActionSpinner);
        uiActionsLongTapSpinner = (Spinner) findViewById(R.id.settingsLongTapActionSpinner);
        uiActionsDisableDeleteDialogCheckBox = (CheckBox) findViewById(R.id.settingsDisableDeleteDialogCheckBox);
        uiActionsShowNoteDateCheckBox = (CheckBox) findViewById(R.id.settingsShowDateCheckBox);

        renderMdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putInt("mdRender", position).commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        renderTxtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putInt("txtRender", position).commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        renderHtmlSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putInt("htmlRender", position).commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        uiActionsShortTapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putInt("shortTapAction", position).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        uiActionsLongTapSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                preferences.edit().putInt("longTapAction", position).commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        uiActionsDisableDeleteDialogCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("disableDeleteDialog", isChecked).commit();
            }
        });
        uiActionsShowNoteDateCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean("showNoteDate", isChecked).commit();
            }
        });

        renderMdSpinner.setAdapter(renderMdSpinnerAdapter);
        renderTxtSpinner.setAdapter(renderTxtSpinnerAdapter);
        renderHtmlSpinner.setAdapter(renderHtmlSpinnerAdapter);
        uiActionsShortTapSpinner.setAdapter(uiActionsShortTapSpinnerAdapter);
        uiActionsLongTapSpinner.setAdapter(uiActionsLongTapSpinnerAdapter);

        loadSettings();
    }

    private void loadSettings() {
        renderMdSpinner.setSelection(preferences.getInt("mdRender", 0));
        renderTxtSpinner.setSelection(preferences.getInt("txtRender", 2));
        renderHtmlSpinner.setSelection(preferences.getInt("htmlRender", 1));
        uiActionsShortTapSpinner.setSelection(preferences.getInt("shortTapAction", 0));
        uiActionsLongTapSpinner.setSelection(preferences.getInt("longTapAction", 2));
        uiActionsDisableDeleteDialogCheckBox.setChecked(preferences.getBoolean("disableDeleteDialog", false));
        uiActionsShowNoteDateCheckBox.setChecked(preferences.getBoolean("showNoteDate", true));
    }
}
