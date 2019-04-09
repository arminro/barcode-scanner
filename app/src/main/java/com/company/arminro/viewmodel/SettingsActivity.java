package com.company.arminro.viewmodel;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity {

    private boolean launchBrowserAutomatically;
    private Switch s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // setting the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        // getting the info out from the intent and feeding it to the Switch ui element
        launchBrowserAutomatically = getIntent().getBooleanExtra(getString(R.string.browse_automatically), false);
        s = (Switch)findViewById(R.id.launchSwitch);
        s.setChecked(launchBrowserAutomatically);


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public  void onBackPressed(){
        Intent intent = new Intent(this, ScannerActivity.class);
        intent.putExtra(getString(R.string.browse_automatically), s.isChecked());
        startActivity(intent);
    }
}
