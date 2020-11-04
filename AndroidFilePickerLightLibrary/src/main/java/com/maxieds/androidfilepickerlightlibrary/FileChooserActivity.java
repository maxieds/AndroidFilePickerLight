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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.BuildConfig;
import pub.devrel.easypermissions.EasyPermissions;

public class FileChooserActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static String LOGTAG = FileChooserActivity.class.getSimpleName();

    private static BasicFileProvider defaultFileProviderStaticInst = new BasicFileProvider();
    public static BasicFileProvider getFileProviderInstance() { return defaultFileProviderStaticInst; }

    private static FileChooserActivity staticRunningInst = null;
    public static FileChooserActivity getInstance() { return staticRunningInst; }

    @ColorInt
    public static int getColorVariantFromTheme(int attrID) {
        return getInstance().getTheme().obtainStyledAttributes(new int[] { attrID }).getColor(0, attrID);
    }

    private static DisplayFragments.FolderNavigationFragment mainFolderNavFragment = null;
    public static List<FileTypes.FileType> activeSelectionsList = new ArrayList<FileTypes.FileType>();
    public static int maxAllowedSelections = 0;
    public static int curSelectionCount = 0;
    public static boolean allowSelectFiles = true;
    public static boolean allowSelectFolders = true;

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

        super.onCreate(lastSettingsBundle);
        setUnhandledExceptionHandler();
        if(getIntent() == null || getIntent().getAction() == null || !getIntent().getAction().equals(Intent.ACTION_PICK_ACTIVITY)) {
            finish();
            System.exit(0);
        }
        if(staticRunningInst == null) {
            staticRunningInst = this;
        }

        AndroidPermissionsHandler.obtainRequiredPermissions(this, ACTIVITY_REQUIRED_PERMISSIONS);
        AndroidPermissionsHandler.requestOptionalPermissions(this, ACTIVITY_OPTIONAL_PERMISSIONS);

        FilePickerBuilder fpConfig = (FilePickerBuilder) getIntent().getSerializableExtra(FilePickerBuilder.FILE_PICKER_BUILDER_EXTRA_DATA_KEY);
        setContentView(R.layout.main_picker_activity_base_layout);
        configureInitialMainLayout(fpConfig);

        // Keep the app from crashing when the screen rotates:
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        DisplayAdapters.FileListAdapter.localFilesListFilter = fpConfig.getFileFilter();
        DisplayAdapters.FileListAdapter.localFilesListSortFunc = fpConfig.getCustomSortFunc();
        maxAllowedSelections = fpConfig.getMaxSelectedFilesCount();
        curSelectionCount = 0;
        activeSelectionsList.clear();
        allowSelectFiles = fpConfig.allowSelectFileItems();
        allowSelectFolders = fpConfig.allowSelectFolderItems();

        long idleTimeout = fpConfig.getIdleTimeout();
        if(idleTimeout != FilePickerBuilder.NO_ABORT_TIMEOUT) {
            Handler execIdleTimeoutHandler = new Handler();
            Runnable execIdleTimeoutRunner = new Runnable() {
                @Override
                public void run() {
                    postSelectedFilesActivityResult();
                }
            };
            execIdleTimeoutHandler.postDelayed(execIdleTimeoutRunner, idleTimeout);
        }

    }

    private void configureInitialMainLayout(FilePickerBuilder fpConfig) {

        /* Setup the toolbar first: */
        Toolbar actionBar = (Toolbar) findViewById(R.id.mainLayoutToolbarActionBar);
        actionBar.setTitle(String.format(Locale.getDefault(), "%s | v%s", getString(R.string.libraryName), String.valueOf(BuildConfig.VERSION_NAME)));
        actionBar.setSubtitle(getString(R.string.filePickerTitleText)); /* TODO: Later, let the user override this default ... */
        getWindow().setTitleColor(getColorVariantFromTheme(R.attr.mainToolbarBackgroundColor));
        getWindow().setStatusBarColor(getColorVariantFromTheme(R.attr.colorPrimaryDark));
        getWindow().setNavigationBarColor(getColorVariantFromTheme(R.attr.colorPrimaryDark));

        /* Initialize the next level of nav for the default folder paths selection buttons: */
        List<FilePickerBuilder.DefaultNavFoldersType> defaultDirNavFolders = fpConfig.getNavigationFoldersList();
        final LinearLayout fileDirsNavButtonsContainer = (LinearLayout) findViewById(R.id.mainFileNavBtnsContainer);
        for(int folderIdx = 0; folderIdx < defaultDirNavFolders.size(); folderIdx++) {
            FilePickerBuilder.BaseFolderPathType baseFolderType = defaultDirNavFolders.get(folderIdx).getBaseFolderPathType();
            ImageButton dirNavBtn = new ImageButton(this);
            dirNavBtn.setPadding(10, 10, 10, 10);
            dirNavBtn.setImageDrawable(FilePickerBuilder.DefaultNavFoldersType.NAV_FOLDER_ICON_RESIDS_MAP.get(baseFolderType));
            dirNavBtn.setTag(baseFolderType);
            Button.OnClickListener stockDirNavBtnClickHandler = new Button.OnClickListener() {
                @Override
                public void onClick(View btnView) {
                    DisplayFragments.cancelAllOperationsInProgress();
                    FileTypes.DirectoryResultContext.pathHistoryStack.clear(); // reset the directory traversal history
                    FilePickerBuilder.BaseFolderPathType navBtnInitFolder = (FilePickerBuilder.BaseFolderPathType) btnView.getTag();
                    FileTypes.DirectoryResultContext newCwdContext = FileTypes.DirectoryResultContext.probeAtCursoryFolderQuery(navBtnInitFolder);
                    FileTypes.DirectoryResultContext.pathHistoryStack.push(newCwdContext);
                    DisplayFragments.FileListItemFragment.rvAdapter.displayNextDirectoryFilesList(newCwdContext.getWorkingDirectoryContents());
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
                DisplayFragments.cancelAllOperationsInProgress();
                getInstance().postSelectedFilesActivityResult();
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
        dirHistoryNavContainer.setBackground(GradientDrawableFactory.generateNamedGradientType(
                GradientDrawableFactory.BorderStyleSpec.BORDER_STYLE_DASHED_SHORT,
                GradientDrawableFactory.NamedGradientColorThemes.NAMED_COLOR_SCHEME_STEEL_BLUE
                )
        );
        mainFolderNavFragment = DisplayFragments.FolderNavigationFragment.createNewFolderNavFragment(dirHistoryNavContainer);

        /* Setup some theme related styling on the main file list container: */
        LinearLayout mainFileListContainer = (LinearLayout) findViewById(R.id.mainRecyclerViewContainer);
        mainFileListContainer.setBackground(GradientDrawableFactory.generateNamedGradientType(
                     GradientDrawableFactory.GradientMethodSpec.GRADIENT_METHOD_LINEAR,
                     GradientDrawableFactory.GradientTypeSpec.GRADIENT_FILL_TYPE_BL_TR,
                     GradientDrawableFactory.BorderStyleSpec.BORDER_STYLE_NONE,
                     45.0f,
                     getColorVariantFromTheme(R.color.colorTransparent),
                     new int[] {
                             getColorVariantFromTheme(R.attr.colorPrimaryVeryDark),
                             getColorVariantFromTheme(R.attr.colorAccent),
                             getColorVariantFromTheme(R.attr.colorPrimaryDark)
                     }
                )
        );
        DisplayFragments.FileListItemFragment.configureStaticInstanceMembers(mainFileListContainer);
        // TODO ...
        //RecyclerViewTouchListener rviewTouchListener = new RecyclerViewTouchListener(FileChooserActivity.getInstance(), mainFileListContainer, )

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
        else if(requestCode == AndroidPermissionsHandler.REQUEST_REQUIRED_PERMISSIONS_CODE) {
            throw new FilePickerException.PermissionsErrorException();
        }
    }

    @AfterPermissionGranted(AndroidPermissionsHandler.REQUEST_REQUIRED_PERMISSIONS_CODE)
    private void handleRequiredPermissionsGranted() {
        String[] permsList = ACTIVITY_REQUIRED_PERMISSIONS;
        if (EasyPermissions.hasPermissions(this, permsList)) {}
        else {
            EasyPermissions.requestPermissions(this, getString(R.string.requiredPermsRationale),
                                               AndroidPermissionsHandler.REQUEST_REQUIRED_PERMISSIONS_CODE, permsList);
        }
    }

    @AfterPermissionGranted(AndroidPermissionsHandler.REQUEST_OPTIONAL_PERMISSIONS_CODE)
    private void handleOptionalPermissionsGranted() {
        String[] permsList = ACTIVITY_OPTIONAL_PERMISSIONS;
        if (EasyPermissions.hasPermissions(this, permsList)) {}
        else {
            EasyPermissions.requestPermissions(this, getString(R.string.optionalPermsRationale),
                    AndroidPermissionsHandler.REQUEST_OPTIONAL_PERMISSIONS_CODE, permsList);
        }
    }

    public Intent getSelectedFilesActivityResultIntent() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(FilePickerBuilder.FILE_PICKER_INTENT_DATA_TYPE_KEY, String.class);
        int selectedFilesCount = activeSelectionsList != null ? activeSelectionsList.size() : 0;
        String[] filePathsList = new String[selectedFilesCount];
        for(int fileIndex = 0; fileIndex < selectedFilesCount; fileIndex++) {
            filePathsList[fileIndex] = activeSelectionsList.get(fileIndex).getAbsolutePath();
        }
        resultIntent.putStringArrayListExtra(FilePickerBuilder.FILE_PICKER_INTENT_DATA_PAYLOAD_KEY, new ArrayList<String>(Arrays.asList(filePathsList)));
        return resultIntent;
    }

    public void postSelectedFilesActivityResult() {
        Intent filesResultIntent = getSelectedFilesActivityResultIntent();
        setResult(Activity.RESULT_OK, filesResultIntent);
        finish();
        System.exit(0);
    }

    public void postSelectedFilesActivityResult(Exception runtimeExcpt) {
        runtimeExcpt.printStackTrace();
        Intent filesResultIntent = getSelectedFilesActivityResultIntent();
        setResult(Activity.RESULT_CANCELED, filesResultIntent);
        finish();
        System.exit(0);
    }

}
