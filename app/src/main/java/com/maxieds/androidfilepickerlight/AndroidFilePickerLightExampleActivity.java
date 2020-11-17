/*
        This program (the AndroidFilePickerLight library) is free software written by
        Maxie Dion Schmidt: you can redistribute it and/or modify
        it under the terms of the GNU General Public License as published by
        the Free Software Foundation, either version 3 of the License, or
        (at your option) any later version.

        This program is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
        GNU General Public License for more details.

        The complete license provided with source distributions of this library is
        available at the following link:
        https://github.com/maxieds/AndroidFilePickerLight
*/

package com.maxieds.androidfilepickerlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.maxieds.androidfilepickerlightlibrary.CustomThemeBuilder;
import com.maxieds.androidfilepickerlightlibrary.DisplayUtils;
import com.maxieds.androidfilepickerlightlibrary.FileChooserBuilder;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
        toolbar.setTitleTextColor(getColor(R.color.colorToolbarFGText));
        toolbar.setLogo(R.drawable.toolbar_icon48);
        toolbar.setBackgroundColor(getColor(R.color.colorAccent));
        toolbar.setTitleTextColor(getColor(R.color.colorToolbarFGText));
        toolbar.setSubtitleTextColor(getColor(R.color.colorToolbarFGText));
        toolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        toolbar.setTitleMargin(15, 5, 15, 5);
        setSupportActionBar(toolbar);
    }

    public void showFileChooserResultsDialog(List<String> fileItemsList, String onErrorMsg) {
        AlertDialog.Builder adBuilder = new AlertDialog.Builder(this);
        boolean resultIsError = onErrorMsg != null;
        if(resultIsError) {
            adBuilder.setIcon(R.drawable.file_picker_error);
            adBuilder.setTitle("Unfortunately the file picker has failed :(");
            TextView errorRationaleDisplayText = new TextView(this);
            errorRationaleDisplayText.setPadding(25, 10, 25, 10);
            String errorDisplayText = String.format(Locale.getDefault(), "Error Rationale: %s", onErrorMsg);
            errorRationaleDisplayText.setTextColor(getColor(R.color.colorOnErrorDisplayText));
            errorRationaleDisplayText.setText(errorDisplayText.toString());
            adBuilder.setView(errorRationaleDisplayText);
            adBuilder.setNegativeButton("[X] That bytes, boo.", null);
            adBuilder.create().show();
            return;
        }
        adBuilder.setIcon(R.drawable.file_picker_success);
        adBuilder.setTitle("Success selecting your files :)");
        String descTrailingPathsList = fileItemsList.size() > 0 ?
                "Here is a list of your favorites:\n" :
                "No file nor directory paths were selected.";
        adBuilder.setMessage(String.format(Locale.getDefault(), "The stylized file picker worked. Congratulations!\n\n%s", descTrailingPathsList));
        TextView fileSelectionsDisplayText = new TextView(this);
        fileSelectionsDisplayText.setPadding(25, 10, 25, 10);
        StringBuilder displayText = new StringBuilder();
        for(String fileItem : fileItemsList) {
            displayText.append(" • " + fileItem + "\n");
        }
        fileSelectionsDisplayText.setText(displayText.toString());
        adBuilder.setView(fileSelectionsDisplayText);
        adBuilder.setNegativeButton("[X] OK, Great!", null);
        adBuilder.create().show();
    }

    private static String rteErrorMsg = null;
    private static  List<String> selectedFilePaths =  new ArrayList<String>();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            selectedFilePaths = FileChooserBuilder.handleActivityResult(this, requestCode, resultCode, data);
        } catch (RuntimeException rte) {
            if (data != null) {
                rteErrorMsg = rte.getMessage();
            }
            if (rteErrorMsg == null) {
                rteErrorMsg = "Unknown reason for exception.";
            }
        }
        showFileChooserResultsDialog(selectedFilePaths, rteErrorMsg);
        selectedFilePaths = new ArrayList<String>();
        rteErrorMsg = null;
    }

    public void actionButtonLaunchSingleFolderPickerActivity(View btnView) {
        FileChooserBuilder fpInst = FileChooserBuilder.getDirectoryChooserInstance(this);
        fpInst.showHidden(true);
        fpInst.setPickerInitialPath(FileChooserBuilder.BaseFolderPathType.BASE_PATH_DEFAULT);
        fpInst.launchFilePicker();
    }

    public void actionButtonLaunchSingleFilePickerActivity(View btnView) {
        FileChooserBuilder fpInst = FileChooserBuilder.getSingleFilePickerInstance(this);
        fpInst.showHidden(true);
        fpInst.setPickerInitialPath(FileChooserBuilder.BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_SCREENSHOTS);
        fpInst.launchFilePicker();
    }

    public void actionButtonLaunchOmnivorousMultiPickerActivity(View btnView) {
        FileChooserBuilder fpInst = new FileChooserBuilder(this);
        fpInst.setSelectionMode(FileChooserBuilder.SelectionModeType.SELECT_OMNIVORE);
        fpInst.setSelectMultiple(5);
        fpInst.setActionCode(FileChooserBuilder.ACTIVITY_CODE_SELECT_MULTIPLE_FILES);
        fpInst.showHidden(true);
        fpInst.setPickerInitialPath(FileChooserBuilder.BaseFolderPathType.BASE_PATH_DEFAULT);
        fpInst.launchFilePicker();
    }

    public void actionButtonLaunchMultiPickerActivityNewThemeGreen(View btnView) {

        FileChooserBuilder fpInst = new FileChooserBuilder(this);
        fpInst.setSelectionMode(FileChooserBuilder.SelectionModeType.SELECT_OMNIVORE);
        fpInst.setSelectMultiple(5);
        fpInst.setActionCode(FileChooserBuilder.ACTIVITY_CODE_SELECT_MULTIPLE_FILES);
        fpInst.showHidden(true);
        fpInst.setPickerInitialPath(FileChooserBuilder.BaseFolderPathType.BASE_PATH_DEFAULT);

        CustomThemeBuilder customThemeBuilder = new CustomThemeBuilder(this)
                .setPickerTitleText(R.string.picker_title_text)
                .setNavBarPrefixText(R.string.navbar_prefix_text)
                .setDoneActionButtonText(R.string.done_action_text)
                .setCancelActionButtonText(R.string.cancel_action_text)
                .setGlobalBackButtonIcon(R.drawable.greentheme_nav_back_button_icon32)
                .setDoneActionButtonIcon(R.drawable.greentheme_done_button_check_icon24)
                .setCancelActionButtonIcon(R.drawable.greentheme_cancel_button_x_icon24)
                .generateThemeColors(R.color.exampleCustomThemeGreen)
                .setActivityToolbarIcon(R.drawable.greentheme_file_chooser_default_toolbar_icon48)
                .useToolbarGradients(true)
                .setNavigationByPathButtonIcon(R.drawable.greentheme_named_folder_sdcard_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_ROOT_STORAGE)
                .setNavigationByPathButtonIcon(R.drawable.greentheme_named_folder_images_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_PICTURES)
                .setNavigationByPathButtonIcon(R.drawable.greentheme_named_folder_camera_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_CAMERA)
                .setNavigationByPathButtonIcon(R.drawable.greentheme_named_folder_screenshots_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_SCREENSHOTS)
                .setNavigationByPathButtonIcon(R.drawable.greentheme_named_folder_downloads_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_DOWNLOADS)
                .setNavigationByPathButtonIcon(R.drawable.greentheme_named_folder_user_home_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_USER_HOME)
                .setNavigationByPathButtonIcon(R.drawable.greentheme_named_folder_media_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_MEDIA_VIDEO)
                .setDefaultFileIcon(R.drawable.greentheme_generic_file_icon32)
                .setDefaultHiddenFileIcon(R.drawable.greentheme_hidden_file_icon32)
                .setDefaultFolderIcon(R.drawable.greentheme_folder_icon32);

        fpInst.setCustomThemeStylizerConfig(customThemeBuilder);
        fpInst.launchFilePicker();

    }

    public void actionButtonLaunchMultiPickerActivityNewThemeOrange(View btnView) {

        FileChooserBuilder fpInst = new FileChooserBuilder(this);
        fpInst.setSelectionMode(FileChooserBuilder.SelectionModeType.SELECT_OMNIVORE);
        fpInst.setSelectMultiple(5);
        fpInst.setActionCode(FileChooserBuilder.ACTIVITY_CODE_SELECT_MULTIPLE_FILES);
        fpInst.showHidden(true);
        fpInst.setPickerInitialPath(FileChooserBuilder.BaseFolderPathType.BASE_PATH_DEFAULT);

        CustomThemeBuilder customThemeBuilder = new CustomThemeBuilder(this)
                .setPickerTitleText(R.string.picker_title_text)
                .setNavBarPrefixText(R.string.navbar_prefix_text)
                .setDoneActionButtonText(R.string.done_action_text)
                .setCancelActionButtonText(R.string.cancel_action_text)
                .setGlobalBackButtonIcon(R.drawable.orangetheme_nav_back_button_icon32)
                .setDoneActionButtonIcon(R.drawable.orangetheme_done_button_check_icon24)
                .setCancelActionButtonIcon(R.drawable.orangetheme_cancel_button_x_icon24)
                .generateThemeColors(R.color.exampleCustomThemeOrange)
                .setActivityToolbarIcon(R.drawable.orangetheme_file_chooser_default_toolbar_icon48)
                .useToolbarGradients(true)
                .setNavigationByPathButtonIcon(R.drawable.orangetheme_named_folder_sdcard_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_ROOT_STORAGE)
                .setNavigationByPathButtonIcon(R.drawable.orangetheme_named_folder_images_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_PICTURES)
                .setNavigationByPathButtonIcon(R.drawable.orangetheme_named_folder_camera_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_CAMERA)
                .setNavigationByPathButtonIcon(R.drawable.orangetheme_named_folder_screenshots_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_SCREENSHOTS)
                .setNavigationByPathButtonIcon(R.drawable.orangetheme_named_folder_downloads_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_DOWNLOADS)
                .setNavigationByPathButtonIcon(R.drawable.orangetheme_named_folder_user_home_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_USER_HOME)
                .setNavigationByPathButtonIcon(R.drawable.orangetheme_named_folder_media_icon32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_MEDIA_VIDEO)
                .setDefaultFileIcon(R.drawable.orangetheme_generic_file_icon32)
                .setDefaultHiddenFileIcon(R.drawable.orangetheme_hidden_file_icon32)
                .setDefaultFolderIcon(R.drawable.orangetheme_folder_icon32);

        fpInst.setCustomThemeStylizerConfig(customThemeBuilder);
        fpInst.launchFilePicker();

    }

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
        DisplayUtils.SetProgressBarMarkerTintColor(this, R.color.exampleCustomThemeGreen);
        progressBarDemoHandler.postDelayed(progressBarDemoRunner, progressBarDemoDelta);
        DisplayUtils.EnableProgressBarDisplay(true);
    }

}