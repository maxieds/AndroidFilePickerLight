package com.maxieds.androidfilepickerlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.maxieds.androidfilepickerlightlibrary.FileChooserBuilder;

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
        toolbar.setTitle("Android File Picker Light");
        toolbar.setLogo(R.drawable.toolbar_icon32);
        toolbar.setBackgroundColor(getColor(R.color.colorAccent));
        toolbar.setTitleTextColor(getColor(R.color.colorPrimaryDark));
        toolbar.setSubtitleTextColor(getColor(R.color.colorPrimaryDark));
        toolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        toolbar.setTitleMargin(15, 5, 15, 5);
        setSupportActionBar(toolbar);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FileChooserBuilder.handleActivityResult(this, requestCode, resultCode, data);
    }

    public void actionButtonLaunchSingleFilePickerActivity(View btnView) {
        FileChooserBuilder fpInst = FileChooserBuilder.getDirectoryChooserInstance(this);
        fpInst.showHidden(true);
        fpInst.setPickerInitialPath(FileChooserBuilder.BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_DOWNLOADS);
        fpInst.launchFilePicker();
    }

    public void actionButtonLaunchSingleFolderPickerActivity(View btnView) {
        FileChooserBuilder fpInst = FileChooserBuilder.getDirectoryChooserInstance(this);
        fpInst.showHidden(true);
        fpInst.setPickerInitialPath(FileChooserBuilder.BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_SCREENSHOTS);
        fpInst.launchFilePicker();
    }

    public void actionButtonLaunchOmnivorousMultiPickerActivity(View btnView) {
        FileChooserBuilder fpInst = new FileChooserBuilder(this);
        fpInst.setSelectionMode(FileChooserBuilder.SelectionModeType.SELECT_FILE);
        fpInst.setSelectMultiple(5);
        fpInst.setActionCode(FileChooserBuilder.ACTIVITY_CODE_SELECT_MULTIPLE_FILES);
        fpInst.showHidden(true);
        fpInst.setPickerInitialPath(FileChooserBuilder.BaseFolderPathType.BASE_PATH_TYPE_SDCARD);
        fpInst.launchFilePicker();
    }

}