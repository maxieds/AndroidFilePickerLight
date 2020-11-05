package com.maxieds.androidfilepickerlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.maxieds.androidfilepickerlightlibrary.DisplayUtils;
import com.maxieds.androidfilepickerlightlibrary.FileChooserBuilder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import java.util.List;

public class AndroidFilePickerLightExampleActivity extends AppCompatActivity {

    private static Activity runningActivityInst = null;
    public static Activity getInstance() { return runningActivityInst; }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        runningActivityInst = this;
        setContentView(R.layout.activity_android_file_picker_light_example);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("    Android File Picker Light");
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

    public void actionButtonLaunchMultiPickerActivity(View btnView) {}

    public void actionButtonLaunchOmnivorousMultiPickerActivityWithCustomSort(View btnView) {}

    private static int progressBarDemoCount = 0;
    private static final int progressBarDemoDelta = 50;
    private static final int progressBarDemoUpper = 500;

    private static Handler  progressBarDemoHandler = new Handler();
    private static Runnable progressBarDemoRunner = new Runnable() {
        @Override
        public void run() {
            progressBarDemoCount += progressBarDemoDelta;
            if(progressBarDemoCount < progressBarDemoUpper) {
                DisplayUtils.DisplayProgressBar(AndroidFilePickerLightExampleActivity.getInstance(),
                        "My countdown:", progressBarDemoCount, progressBarDemoUpper);
                progressBarDemoHandler.postDelayed(progressBarDemoRunner, progressBarDemoDelta);
            }
            else {
                progressBarDemoHandler.removeCallbacks(progressBarDemoRunner);
                DisplayUtils.EnableProgressBarDisplay(false);
            }
        }
    };

    public void actionButtonLaunchProgressBarDemo(View btnView) {
        progressBarDemoCount = 0;
        progressBarDemoHandler.postDelayed(progressBarDemoRunner, progressBarDemoDelta);
        DisplayUtils.EnableProgressBarDisplay(true);
    }

}