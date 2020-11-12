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

package com.maxieds.androidfilepickerlightlibrary;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.widget.Toolbar;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class FileChooserActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static String LOGTAG = FileChooserActivity.class.getSimpleName();

    private static FileChooserActivity staticRunningInst = null;
    public static  FileChooserActivity getInstance() { return staticRunningInst; }

    private DisplayFragments displayFragmentsInst;
    public DisplayFragments getDisplayFragmentsInstance() { return displayFragmentsInst; }

    private DisplayTypes.DirectoryResultContext cwdFolderCtx;
    public DisplayTypes.DirectoryResultContext getCwdFolderContext() { return cwdFolderCtx; }
    public void setCwdFolderContext(DisplayTypes.DirectoryResultContext nextCwdCtx) { cwdFolderCtx = nextCwdCtx; }

    private FileChooserBuilder.BaseFolderPathType topLevelBaseFolder;
    public FileChooserBuilder.BaseFolderPathType getTopLevelBaseFolder() { return topLevelBaseFolder; }
    public void setTopLevelBaseFolder(FileChooserBuilder.BaseFolderPathType tlFolder) { topLevelBaseFolder = tlFolder; }

    private PrefetchFilesUpdater prefetchFilesUpdaterInst;
    public void startPrefetchFileUpdatesThread() {
        prefetchFilesUpdaterInst.start();
    }
    public void stopPrefetchFileUpdatesThread() { prefetchFilesUpdaterInst.interrupt(); }

    /**
     * Default handler for  all uncaught exceptions.
     */
    private void setUnhandledExceptionHandler() {
        final AppCompatActivity localActivityContext = this;
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramExcpt) {
                getInstance().postSelectedFilesActivityResult((Exception) paramExcpt);
            }
        });
    }

    @Override
    public void onCreate(Bundle lastSettingsBundle) {

        Log.i(LOGTAG, "Activity onCreate");
        super.onCreate(lastSettingsBundle);
        setUnhandledExceptionHandler();
        staticRunningInst = this;
        displayFragmentsInst = new DisplayFragments();
        displayFragmentsInst.resetRecyclerViewLayoutContext();
        cwdFolderCtx = null;

        PermissionsHandler.obtainRequiredPermissions(this, ACTIVITY_REQUIRED_PERMISSIONS);
        PermissionsHandler.requestOptionalPermissions(this, ACTIVITY_OPTIONAL_PERMISSIONS);

        setTheme(R.style.LibraryDefaultTheme);
        setContentView(R.layout.main_picker_activity_base_layout);
        FileChooserBuilder fpConfig = FileChooserBuilder.getInstance();
        configureInitialMainLayout(fpConfig);

        // Keep the app from crashing when the screen rotates:
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        getDisplayFragmentsInstance().resetRecyclerViewLayoutContext();
        getDisplayFragmentsInstance().localFilesListFilter = fpConfig.getFileFilter();
        getDisplayFragmentsInstance().localFilesListSortFunc = fpConfig.getCustomSortFunc();
        getDisplayFragmentsInstance().maxAllowedSelections = fpConfig.getMaxSelectedFilesCount();
        getDisplayFragmentsInstance().curSelectionCount = 0;
        getDisplayFragmentsInstance().activeSelectionsList.clear();
        getDisplayFragmentsInstance().allowSelectFiles = fpConfig.allowSelectFileItems();
        getDisplayFragmentsInstance().allowSelectFolders = fpConfig.allowSelectFolderItems();

        topLevelBaseFolder = fpConfig.getInitialBaseFolder();
        prefetchFilesUpdaterInst = new PrefetchFilesUpdater();
        startPrefetchFileUpdatesThread();

        long idleTimeout = fpConfig.getIdleTimeout();
        if(idleTimeout != FileChooserBuilder.NO_ABORT_TIMEOUT) {
            Handler execIdleTimeoutHandler = new Handler();
            Runnable execIdleTimeoutRunner = new Runnable() {
                @Override
                public void run() {
                    postSelectedFilesActivityResult(new FileChooserException.AbortedByTimeoutException());
                }
            };
            execIdleTimeoutHandler.postDelayed(execIdleTimeoutRunner, idleTimeout);
        }

    }

    private void configureInitialMainLayout(FileChooserBuilder fpConfig) {

        /* Setup the toolbar first: */
        Toolbar actionBar = (Toolbar) findViewById(R.id.mainLayoutToolbarActionBar);
        actionBar.setTitle(String.format(Locale.getDefault(), "    %s (v%s)", getString(R.string.libraryName), String.valueOf(BuildConfig.VERSION_NAME)));
        actionBar.setSubtitle(String.format(Locale.getDefault(), "    ⇤%s⇥", getString(R.string.filePickerTitleText)));
        actionBar.setTitleTextColor(getColor(R.color.colorMainToolbarForegroundText));
        actionBar.setSubtitleTextColor(getColor(R.color.colorMainToolbarForegroundText));
        actionBar.setTitleMargin(10, 3, 5, 3);
        actionBar.setPadding(5, 8, 5, 6);
        actionBar.setElevation(1.25f);
        actionBar.setLogo(getDrawable(R.drawable.file_chooser_default_toolbar_icon48));
        getWindow().setTitleColor(DisplayUtils.getColorVariantFromTheme(R.attr.mainToolbarBackgroundColor));
        getWindow().setStatusBarColor(DisplayUtils.getColorVariantFromTheme(R.attr.colorPrimaryDark));
        getWindow().setNavigationBarColor(DisplayUtils.getColorVariantFromTheme(R.attr.colorPrimaryDark));

        /* Initialize the next level of nav for the default folder paths selection buttons: */
        List<FileChooserBuilder.DefaultNavFoldersType> defaultDirNavFolders = fpConfig.getNavigationFoldersList();
        final LinearLayout fileDirsNavButtonsContainer = (LinearLayout) findViewById(R.id.mainFileNavBtnsContainer);
        for(int folderIdx = 0; folderIdx < defaultDirNavFolders.size(); folderIdx++) {
            FileChooserBuilder.BaseFolderPathType baseFolderType = defaultDirNavFolders.get(folderIdx).getBaseFolderPathType();
            ImageButton dirNavBtn = new ImageButton(this);
            dirNavBtn.setPadding(10, 10, 10, 10);
            dirNavBtn.setTag(baseFolderType.name());
            Button.OnClickListener stockDirNavBtnClickHandler = new Button.OnClickListener() {
                @Override
                public void onClick(View btnView) {
                    getDisplayFragmentsInstance().cancelAllOperationsInProgress();
                    getDisplayFragmentsInstance().pathHistoryStack.clear(); // reset the directory traversal history
                    FileChooserBuilder.BaseFolderPathType navBtnInitFolder = (FileChooserBuilder.BaseFolderPathType) btnView.getTag();
                    getDisplayFragmentsInstance().initiateNewFolderLoad(navBtnInitFolder);
                    Button navBtn = (Button) btnView;
                    navBtn.setEnabled(false);
                    navBtn.setElevation(-1.0f);
                    // Re-enable all of the other stock directory nav buttons:
                    for(int btnIdx = 0; btnIdx < fileDirsNavButtonsContainer.getChildCount(); btnIdx++) {
                        Button nextNavBtn = (Button) fileDirsNavButtonsContainer.getChildAt(btnIdx);
                        if(!navBtn.equals(nextNavBtn)) {
                            nextNavBtn.setEnabled(true);
                            nextNavBtn.setElevation(0.0f);
                        }
                    }
                }
            };
            dirNavBtn.setOnClickListener(stockDirNavBtnClickHandler);
            dirNavBtn.setBackgroundColor(DisplayUtils.getColorVariantFromTheme(R.attr.colorToolbarNav));
            dirNavBtn.setImageDrawable(DisplayUtils.resolveDrawableFromAttribute(defaultDirNavFolders.get(folderIdx).getFolderIconResId()));
            fileDirsNavButtonsContainer.addView(dirNavBtn);
        }

        /* The next level of navigation in a top down order is the action buttons to finish or cancel
         * the user's file selection procedure. These need onClick handlers that are mostly separate
         * and less involved that the corresponding RecyclerView and files listing UI actions.
         * These effectively have the same function attached to them, but for clarity of what the UI
         * expects from the user, it's better to distinguish that "Done" does not mean to quit or
         * "Cancel" unexpectedly.
         */
        Button doneActionBtn = findViewById(R.id.mainNavBtnActionDone);
        Button cancelActionBtn = findViewById(R.id.mainNavBtnActionCancel);
        Button.OnClickListener quitActivityBtnClickListener = new Button.OnClickListener() {
            @Override
            public void onClick(View btnView) {
                getDisplayFragmentsInstance().cancelAllOperationsInProgress();
                getInstance().postSelectedFilesActivityResult(new FileChooserException.CommunicateNoDataException());
            }
        };
        doneActionBtn.setOnClickListener(quitActivityBtnClickListener);
        cancelActionBtn.setOnClickListener(quitActivityBtnClickListener);

        /*
         * Last, there is the global back button and previous directory history display.
         * Note that unless a completely different path trajectory is selected by the user
         * (with the last level of directory select nav buttons), we keep a working
         * stack of the last paths for context. If the back button is pressed and the stack is
         * empty, this action is handled the same way as a cancel button press by the user.
         */
        LinearLayout dirHistoryNavContainer = (LinearLayout) findViewById(R.id.mainDirPrevPathsNavContainer);
        DisplayFragments.mainFolderNavFragment = DisplayFragments.FolderNavigationFragment.createNewFolderNavFragment(dirHistoryNavContainer);

        /* Setup some theme related styling on the main file list container: */
        FileChooserRecyclerView mainLayoutRecyclerView = findViewById(R.id.mainRecyclerView);
        getDisplayFragmentsInstance().initializeRecyclerViewLayout(mainLayoutRecyclerView);
        getDisplayFragmentsInstance().initiateNewFolderLoad(fpConfig.getInitialBaseFolder());

    }

    @Override
    public void onNewIntent(Intent broadcastIntent) {
        super.onNewIntent(broadcastIntent);
    }

    public static final String[] ACTIVITY_REQUIRED_PERMISSIONS = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE",
    };

    public static final String[] ACTIVITY_OPTIONAL_PERMISSIONS = {
            "android.permission.MANAGE_EXTERNAL_STORAGE",
            "android.permission.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION",
            "android.permission.INTERNET"
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permsList, int[] grantedPermsList) {
        super.onRequestPermissionsResult(requestCode, permsList, grantedPermsList);
        EasyPermissions.onRequestPermissionsResult(requestCode, permsList, grantedPermsList, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {}

    @Override
    public void onPermissionsDenied(int requestCode, List<String> permsList) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, permsList)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
        else if(requestCode == PermissionsHandler.REQUEST_REQUIRED_PERMISSIONS_CODE) {
            throw new FileChooserException.PermissionsErrorException();
        }
    }

    @AfterPermissionGranted(PermissionsHandler.REQUEST_REQUIRED_PERMISSIONS_CODE)
    private void handleRequiredPermissionsGranted() {
        String[] permsList = ACTIVITY_REQUIRED_PERMISSIONS;
        if (EasyPermissions.hasPermissions(this, permsList)) {}
        else {
            EasyPermissions.requestPermissions(this, getString(R.string.requiredPermsRationale),
                                               PermissionsHandler.REQUEST_REQUIRED_PERMISSIONS_CODE, permsList);
        }
    }

    @AfterPermissionGranted(PermissionsHandler.REQUEST_OPTIONAL_PERMISSIONS_CODE)
    private void handleOptionalPermissionsGranted() {
        String[] permsList = ACTIVITY_OPTIONAL_PERMISSIONS;
        if (EasyPermissions.hasPermissions(this, permsList)) {}
        else {
            EasyPermissions.requestPermissions(this, getString(R.string.optionalPermsRationale),
                    PermissionsHandler.REQUEST_OPTIONAL_PERMISSIONS_CODE, permsList);
        }
    }

    public Intent getSelectedFilesActivityResultIntent() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(FileChooserBuilder.FILE_PICKER_INTENT_DATA_TYPE_KEY, String.class);
        int selectedFilesCount = getDisplayFragmentsInstance().activeSelectionsList != null ? getDisplayFragmentsInstance().activeSelectionsList.size() : 0;
        String[] filePathsList = new String[selectedFilesCount];
        for(int fileIndex = 0; fileIndex < selectedFilesCount; fileIndex++) {
            filePathsList[fileIndex] = getDisplayFragmentsInstance().activeSelectionsList.get(fileIndex).getAbsolutePath();
        }
        resultIntent.putStringArrayListExtra(FileChooserBuilder.FILE_PICKER_INTENT_DATA_PAYLOAD_KEY, new ArrayList<String>(Arrays.asList(filePathsList)));
        return resultIntent;
    }

    public void postSelectedFilesActivityResult() {
        Intent filesResultIntent = getSelectedFilesActivityResultIntent();
        setResult(Activity.RESULT_OK, filesResultIntent);
        finish();
        getDisplayFragmentsInstance().resetRecyclerViewLayoutContext();
        //System.exit(0); // ??? TODO ???
    }

    public void postSelectedFilesActivityResult(Exception runtimeExcpt) {
        runtimeExcpt.printStackTrace();
        Intent filesResultIntent = getSelectedFilesActivityResultIntent();
        if(runtimeExcpt instanceof FileChooserException.AndroidFilePickerLightException) {
            FileChooserException.AndroidFilePickerLightException rteLocal = (FileChooserException.AndroidFilePickerLightException) runtimeExcpt;
            filesResultIntent.putExtra(FileChooserBuilder.FILE_PICKER_EXCEPTION_MESSAGE_KEY, rteLocal.getMessage());
            filesResultIntent.putExtra(FileChooserBuilder.FILE_PICKER_EXCEPTION_CAUSE_KEY, rteLocal.getCauseAsString());
        }
        setResult(Activity.RESULT_CANCELED, filesResultIntent);
        finish();
        getDisplayFragmentsInstance().resetRecyclerViewLayoutContext();
        //ActivityCompat.finishAffinity(this);
        //System.exit(0); // ??? TODO ???
    }

}
