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
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.provider.DocumentsProvider;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class FilePickerBuilder implements Serializable {

    private static Activity activityContextRef;
    public void setActivityContext(Activity activityRef) {
        activityContextRef = activityRef;
    }

    public enum DefaultNavFoldersType {
        FOLDER_SDCARD_STORAGE("SD Card", R.attr.namedFolderSDCardIcon),
        FOLDER_PICTURES("Pictures", R.attr.namedFolderPicsIcon),
        FOLDER_CAMERA("Camera", R.attr.nameFolderCameraIcon),
        FOLDER_SCREENSHOTS("Screenshots", R.attr.namedFolderScreenshotsIcon),
        FOLDER_DOWNLOADS("Downloads", R.attr.namedFolderDownloadsIcon),
        FOLDER_USER_HOME("Home", R.attr.namedFolderUserHomeIcon),
        FOLDER_MEDIA_VIDEO("Media", R.attr.namedFolderMediaIcon);

        private String folderLabel;
        private int folderIconResId;
        private Drawable customIconObj;

        DefaultNavFoldersType(String folderLabel, int folderIconResId) {
            this.folderLabel = folderLabel;
            this.folderIconResId = folderIconResId;
            this.customIconObj = null;
        }

        public String getFolderLabel() {
            return folderLabel;
        }

        public Drawable getFolderIconDrawable() {
            if(customIconObj == null && activityContextRef != null) {
                return activityContextRef.getResources().getDrawable(folderIconResId, activityContextRef.getTheme());
            }
            else if(customIconObj != null) {
                return customIconObj;
            }
            throw new FilePickerException.InvalidActivityContextException();
        }

        public void setFolderIconDrawable(Drawable folderIconDrawInst) {
            customIconObj = folderIconDrawInst;
        }

    }

    public static List<DefaultNavFoldersType> getDefaultNavFoldersList() {
        List<DefaultNavFoldersType> navFoldersList = new ArrayList<DefaultNavFoldersType>();
        navFoldersList.add(DefaultNavFoldersType.FOLDER_SDCARD_STORAGE);
        navFoldersList.add(DefaultNavFoldersType.FOLDER_USER_HOME);
        navFoldersList.add(DefaultNavFoldersType.FOLDER_PICTURES);
        navFoldersList.add(DefaultNavFoldersType.FOLDER_DOWNLOADS);
        return navFoldersList;
    }

    public enum BaseFolderPathType {
        BASE_PATH_TYPE_EXTERNAL_STORAGE,
        BASE_PATH_TYPE_INTERNAL_STORAGE,
        BASE_PATH_TYPE_USER_HOME,
        BASE_PATH_TYPE_SDCARD,
        BASE_PATH_TYPE_DEFAULT_EMULATED,
        BASE_PATH_SECONDARY_STORAGE,
        BASE_PATH_DEFAULT,
        BASE_PATH_EXTERNAL_PROVIDER
    }

    public enum SelectionModeType {
        SELECT_FILE,
        SELECT_MULTIPLE_FILES,
        SELECT_DIRECTORY_ONLY,
        SELECT_OMNIVORE
    }
    public int getFileSelectionModeMask(SelectionModeType selectMode) {
        return 1 << selectMode.ordinal();
    }

    private FilePickerException.AndroidFilePickerLightException defaultExceptionType, lastError;
    private DisplayConfigInterface displayUIConfig;
    private int activityActionCode;
    private List<DefaultNavFoldersType> defaultNavFoldersList;
    private boolean showHidden;
    private int maxSelectedFiles;
    private int localThemeResId;
    private BaseFolderPathType initFolderBasePathType;
    private String initFolderSubDirName;
    private SelectionModeType pathSelectMode;
    private DocumentsProvider externalFilesProvider;
    private long idleTimeoutMillis;

    public static final long NO_ABORT_TIMEOUT = -1;
    public static final int DEFAULT_MAX_SELECTED_FILES = 10;

    public FilePickerBuilder(Activity activityContextInst) {
        activityContextRef = activityContextInst;
        defaultExceptionType = FilePickerException.CommunicateSelectionDataException.getNewInstance();
        lastError = null;
        displayUIConfig = DisplayConfigInterface.getDefaultsInstance();
        activityActionCode = ACTIVITY_CODE_SELECT_FILE;
        defaultNavFoldersList = getDefaultNavFoldersList();
        showHidden = false;
        maxSelectedFiles = DEFAULT_MAX_SELECTED_FILES;
        localThemeResId = R.style.LibraryDefaultTheme;
        initFolderBasePathType = BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_STORAGE;
        pathSelectMode = SelectionModeType.SELECT_OMNIVORE;
        externalFilesProvider = null;
        idleTimeoutMillis = NO_ABORT_TIMEOUT;
    }

    public static FilePickerBuilder getSingleFilePickerInstance() {
        return null; // TODO
    }

    public static FilePickerBuilder getDirectoryChooserInstance() {
        return null; // TODO
    }

    public FilePickerBuilder setDisplayUIConfig(DisplayConfigInterface uiCfg) {
        displayUIConfig = uiCfg;
        return this;
    }

    public FilePickerBuilder setActionCode(int activityResultCode) {
        activityActionCode = activityResultCode;
        return this;
    }

    public FilePickerBuilder setNavigationFoldersList(List<DefaultNavFoldersType> navFoldersList) {
        defaultNavFoldersList = navFoldersList;
        return this;
    }

    public FilePickerBuilder showHidden(boolean enable) {
        showHidden = enable;
        return this;
    }

    private static final String FILE_PICKER_BUILDER_EXTRA_DATA_KEY = "FilePickerBuilderExtraData";

    public <DataItemTypeT extends Object> List<DataItemTypeT> runFilePicker() throws FilePickerException.AndroidFilePickerLightException {
        Intent launchPickerIntent = new Intent(activityContextRef, FilePickerChooserActivity.class);
        launchPickerIntent.setAction(Intent.ACTION_PICK_ACTIVITY);
        launchPickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        launchPickerIntent.putExtra(FILE_PICKER_BUILDER_EXTRA_DATA_KEY, this);
        activityContextRef.startActivityForResult(launchPickerIntent, activityActionCode);
        try {
            Looper.loop();
        } catch(FilePickerException.AndroidFilePickerLightException rteInst) {
            try {
                if(rteInst.isError()) {
                    throw rteInst;
                }
                else if(!rteInst.hasDataItems()) {
                    lastError = rteInst;
                    return new ArrayList<DataItemTypeT>();
                }
                lastError = null;
                List<DataItemTypeT> selectedDataItems = rteInst.getTypedDataAsList();
                /* The next procedure is necessary because for some reason the app otherwise
                 * freezes without bringing the original Activity context back to the front:
                 */
                activityContextRef.moveTaskToBack(false);
                Intent bringToFrontIntent = new Intent(activityContextRef, activityContextRef.getClass());
                bringToFrontIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activityContextRef.startActivity(bringToFrontIntent);
                /* Now resume to return the data we requested: */
                return selectedDataItems;
            } catch(Exception ex) {
                ex.printStackTrace();
                throw defaultExceptionType.newExceptionInstance(rteInst.getMessage(), ex);
            }
        }
        return null;
    }

    public static final int ACTIVITY_CODE_SELECT_FILE = 1;
    public static final int ACTIVITY_CODE_SELECT_DIRECTORY_ONLY = 2;
    public static final int ACTIVITY_CODE_SELECT_MULTIPLE_FILES = 3;

    /* Client code should call this method in their main Activity's onActivityResult function
     * to handle the logic there when the activity was created as a file picker instance:
     */
    public static void handleActivityResult(Activity activityInst, int requestCode, int resultCode, Intent data) throws FilePickerException.AndroidFilePickerLightException {
        if(activityInst == null || data == null) {
            throw new FilePickerException.CommunicateNoDataException();
        }
        switch (requestCode) {
            case ACTIVITY_CODE_SELECT_FILE:
            case ACTIVITY_CODE_SELECT_DIRECTORY_ONLY:
            case ACTIVITY_CODE_SELECT_MULTIPLE_FILES:
                if (resultCode == RESULT_OK) {
                    throw new RuntimeException("TODO: Encode the logic to return the correct RTE data here ... ");
                }
                break;
            default:
                break;

        }
        throw new FilePickerException.GenericRuntimeErrorException();
    }

}
