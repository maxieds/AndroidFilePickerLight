package com.maxieds.androidfilepickerlight;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.TextView;

import java.util.List;

public class AndroidFilePickerLightExampleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_file_picker_light_example);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Android File Picker Light Library Demo");
        toolbar.setLogo(R.drawable.toolbar_icon32);
        toolbar.setBackgroundColor(getColor(R.color.colorAccent));
        toolbar.setTitleTextColor(getColor(R.color.colorPrimaryDark));
        toolbar.setSubtitleTextColor(getColor(R.color.colorPrimaryDark));
        toolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        toolbar.setTitleMargin(15, 5, 15, 5);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    public void showFileChooserResultsDialog(List<String> fileItemsList, boolean resultError) {
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        if(resultError) {
            adBuilder.setIcon(R.drawable.file_picker_error);
            adBuilder.setTitle("Unfortunately the file picker has failed :(");
            adBuilder.setNegativeButton("That sucks, boo ...", null);
            adBuilder.setNegativeButtonIcon(getDrawable(R.drawable.fp_error_dismiss_btn_on_error));
            adBuilder.create().show();
            return;
        }
        adBuilder.setIcon(R.drawable.file_picker_success);
        adBuilder.setTitle("Success selecting your files :)");
        adBuilder.setMessage("The stylized file picker worked. Congratulations!\n\nHere is a list of your favorites:\n");
        TextView fileSelectionsDisplayText = new TextView(this);
        StringBuilder displayText = new StringBuilder();
        for(String fileItem : fileItemsList) {
            displayText.append(" • " + fileItem + "\n");
        }
        fileSelectionsDisplayText.setText(displayText.toString());
        adBuilder.setView(fileSelectionsDisplayText);
        adBuilder.setNegativeButton("OK, Great!", null);
        adBuilder.setNegativeButtonIcon(getDrawable(R.drawable.fp_error_dismiss_btn_on_success));
        adBuilder.create().show();
    }

}