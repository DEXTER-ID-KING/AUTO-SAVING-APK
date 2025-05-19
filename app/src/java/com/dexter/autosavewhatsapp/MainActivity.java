package com.dexter.autosavewhatsapp;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.EditText;
import android.text.TextWatcher;
import android.text.Editable;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MainActivity extends Activity {

    Switch enableSwitch;
    EditText nameInput;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        enableSwitch = findViewById(R.id.enableSwitch);
        nameInput = findViewById(R.id.nameInput);

        enableSwitch.setChecked(prefs.getBoolean("enabled", true));
        nameInput.setText(prefs.getString("custom_name", "Dexter"));

        enableSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("enabled", isChecked).apply();
        });

        nameInput.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {
                prefs.edit().putString("custom_name", s.toString()).apply();
            }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
    }
}