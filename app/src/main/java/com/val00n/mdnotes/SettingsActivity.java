package com.val00n.mdnotes;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

// A HUGE TODO: settings
public class SettingsActivity extends Activity {

    // UI
    private Spinner renderMdSpinner;
    private Spinner renderTxtSpinner;
    private Spinner renderHtmlSpinner;
    private ArrayAdapter<String> renderMdSpinnerAdapter;
    private ArrayAdapter<String> renderTxtSpinnerAdapter;
    private ArrayAdapter<String> renderHtmlSpinnerAdapter;
    // OTHERS
    private SharedPreferences preferences;
    private String[] renderOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        preferences = getSharedPreferences("globalSettings", 0);

        renderOptions = new String[]{"TextView", "WebView", "Raw text"};
        renderMdSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, renderOptions);
        renderTxtSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, renderOptions);
        renderHtmlSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, renderOptions);

        renderMdSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        renderTxtSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        renderHtmlSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        renderMdSpinner = (Spinner) findViewById(R.id.settingsRenderMdSpinner);
        renderTxtSpinner = (Spinner) findViewById(R.id.settingsRenderTxtSpinner);
        renderHtmlSpinner = (Spinner) findViewById(R.id.settingsRenderHtmlSpinner);

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

        renderMdSpinner.setAdapter(renderMdSpinnerAdapter);
        renderTxtSpinner.setAdapter(renderTxtSpinnerAdapter);
        renderHtmlSpinner.setAdapter(renderHtmlSpinnerAdapter);

        loadSettings();
    }

    private void loadSettings() {
        renderMdSpinner.setSelection(preferences.getInt("mdRender", 0));
        renderTxtSpinner.setSelection(preferences.getInt("txtRender", 2));
        renderHtmlSpinner.setSelection(preferences.getInt("htmlRender", 1));
    }
}
