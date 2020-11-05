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
import android.content.ContentProvider;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.TypedValue;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class FileChooserBuilder implements Serializable {

    private static String LOGTAG = FileChooserBuilder.class.getSimpleName();

    public enum DefaultNavFoldersType {

        FOLDER_SDCARD_STORAGE("SD Card", R.attr.namedFolderSDCardIcon, BaseFolderPathType.BASE_PATH_TYPE_SDCARD),
        FOLDER_PICTURES("Pictures", R.attr.namedFolderPicsIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_PICTURES),
        FOLDER_CAMERA("Camera", R.attr.namedFolderCameraIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_PICTURES),
        FOLDER_SCREENSHOTS("Screenshots", R.attr.namedFolderScreenshotsIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_SCREENSHOTS),
        FOLDER_DOWNLOADS("Downloads", R.attr.namedFolderDownloadsIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_DOWNLOADS),
        FOLDER_USER_HOME("Home", R.attr.namedFolderUserHomeIcon, BaseFolderPathType.BASE_PATH_TYPE_USER_DATA_DIR),
        FOLDER_MEDIA_VIDEO("Media", R.attr.namedFolderMediaIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_DCIM);

        public static final Map<DefaultNavFoldersType, String> NAV_FOLDER_NAME_LOOKUP_MAP = new HashMap<>();
        public static final Map<DefaultNavFoldersType, String> NAV_FOLDER_DESC_MAP = new HashMap<>();
        public static final Map<DefaultNavFoldersType, BaseFolderPathType> NAV_FOLDER_PATHS_MAP = new HashMap<>();
        public static final Map<Integer, Integer> NAV_FOLDER_ICON_RESIDS_MAP = new HashMap<>();
        static {
            for (DefaultNavFoldersType navType : values()) {
                NAV_FOLDER_NAME_LOOKUP_MAP.put(navType, navType.toString());
                NAV_FOLDER_DESC_MAP.put(navType, navType.getFolderLabel());
                NAV_FOLDER_PATHS_MAP.put(navType, navType.getBaseFolderPathType());
                NAV_FOLDER_ICON_RESIDS_MAP.put(navType.ordinal(), navType.getFolderIconResId());
            }
        }

        private String folderLabel;
        private BaseFolderPathType baseFolderSpec;
        private int folderIconResId;
        private Drawable customIconObj;

        DefaultNavFoldersType(String folderLabel, int folderIconResId, BaseFolderPathType baseFolderSpec) {
            this.folderLabel = folderLabel;
            this.baseFolderSpec = baseFolderSpec;
            this.folderIconResId = folderIconResId;
            this.customIconObj = null;
        }

        public String getFolderLabel() {
            return folderLabel;
        }

        public BaseFolderPathType getBaseFolderPathType() {
            return baseFolderSpec;
        }

        public int getFolderIconResId() {
            return folderIconResId;
        }

        public Drawable getFolderIconDrawable() {
            if(customIconObj == null) {
                return GradientDrawableFactory.resolveDrawableFromAttribute(folderIconResId);
            }
            return customIconObj;
        }

        public void setCustomFolderIconDrawable(Drawable folderIconDrawInst) {
            customIconObj = folderIconDrawInst;
        }

        public static int getFolderIconResIdFromName(DefaultNavFoldersType folderType) {
            return NAV_FOLDER_ICON_RESIDS_MAP.get(folderType.ordinal()).intValue();
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
        BASE_PATH_TYPE_FILES_DIR,
        BASE_PATH_TYPE_CACHE_DIR,
        BASE_PATH_TYPE_EXTERNAL_FILES_DOWNLOADS,
        BASE_PATH_TYPE_EXTERNAL_FILES_MOVIES,
        BASE_PATH_TYPE_EXTERNAL_FILES_MUSIC,
        BASE_PATH_TYPE_EXTERNAL_FILES_DOCUMENTS,
        BASE_PATH_TYPE_EXTERNAL_FILES_DCIM,
        BASE_PATH_TYPE_EXTERNAL_FILES_PICTURES,
        BASE_PATH_TYPE_EXTERNAL_FILES_SCREENSHOTS,
        BASE_PATH_TYPE_EXTERNAL_CACHE_DIR,
        BASE_PATH_TYPE_USER_DATA_DIR,
        BASE_PATH_TYPE_MEDIA_STORE,
        BASE_PATH_TYPE_SDCARD,
        BASE_PATH_SECONDARY_STORAGE,
        BASE_PATH_DEFAULT,
        BASE_PATH_EXTERNAL_PROVIDER;

        public static final Map<Integer, BaseFolderPathType> NAV_FOLDER_ICON_RESIDS_MAP = new HashMap<>();
        static {
            for (BaseFolderPathType folderType : values()) {
                NAV_FOLDER_ICON_RESIDS_MAP.put(folderType.ordinal(), folderType);
            }
        }

        public static BaseFolderPathType getInstanceByName(BaseFolderPathType folderTypeName) {
            return BaseFolderPathType.NAV_FOLDER_ICON_RESIDS_MAP.get(folderTypeName.ordinal());
        }

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

    private WeakReference<Activity> activityContextRef;
    private FileChooserException.AndroidFilePickerLightException defaultExceptionType;
    private DisplayConfigInterface displayUIConfig;
    private int activityActionCode;
    private List<DefaultNavFoldersType> defaultNavFoldersList;
    private boolean showHidden;
    private int maxSelectedFiles;
    private int localThemeResId;
    private BaseFolderPathType initFolderBasePathType;
    private String initFolderSubDirName;
    private SelectionModeType pathSelectMode;
    private ContentProvider externalFilesProvider;
    private long idleTimeoutMillis;
    private FileFilter.FileFilterInterface localFileFilter;
    private FileTypes.FileItemsListSortFunc customSortFunc;

    public static final long NO_ABORT_TIMEOUT = -1;
    public static final int DEFAULT_MAX_SELECTED_FILES = 10;

    public FileChooserBuilder(Activity activityContextInst) {
        activityContextRef = new WeakReference<Activity>(activityContextInst);
        defaultExceptionType = FileChooserException.CommunicateSelectionDataException.getNewInstance();
        displayUIConfig = DisplayConfigInterface.getDefaultsInstance();
        activityActionCode = ACTIVITY_CODE_SELECT_FILE;
        defaultNavFoldersList = getDefaultNavFoldersList();
        showHidden = false;
        maxSelectedFiles = DEFAULT_MAX_SELECTED_FILES;
        localThemeResId = R.style.LibraryDefaultTheme;
        initFolderBasePathType = BaseFolderPathType.getInstanceByName(BaseFolderPathType.BASE_PATH_TYPE_FILES_DIR);
        pathSelectMode = SelectionModeType.SELECT_OMNIVORE;
        externalFilesProvider = null;
        idleTimeoutMillis = NO_ABORT_TIMEOUT;
        localFileFilter = null;
    }

    public static FileChooserBuilder getSingleFilePickerInstance(Activity activityContextInst) {
        FileChooserBuilder pickerBuilderInst = new FileChooserBuilder(activityContextInst);
        pickerBuilderInst.setSelectionMode(SelectionModeType.SELECT_DIRECTORY_ONLY);
        pickerBuilderInst.maxSelectedFiles = 1;
        pickerBuilderInst.setActionCode(ACTIVITY_CODE_SELECT_DIRECTORY_ONLY);
        return pickerBuilderInst;
    }

    public static FileChooserBuilder getDirectoryChooserInstance(Activity activityContextInst) {
        FileChooserBuilder pickerBuilderInst = new FileChooserBuilder(activityContextInst);
        pickerBuilderInst.setSelectionMode(SelectionModeType.SELECT_FILE);
        pickerBuilderInst.maxSelectedFiles = 1;
        pickerBuilderInst.setActionCode(ACTIVITY_CODE_SELECT_FILE);
        return pickerBuilderInst;
    }

    public boolean allowSelectFileItems() {
        return pathSelectMode.ordinal() != SelectionModeType.SELECT_DIRECTORY_ONLY.ordinal();
    }

    public boolean allowSelectFolderItems() {
        return pathSelectMode.ordinal() == SelectionModeType.SELECT_DIRECTORY_ONLY.ordinal() ||
                pathSelectMode.ordinal() != SelectionModeType.SELECT_FILE.ordinal();
    }

    public FileChooserBuilder setDisplayUIConfig(DisplayConfigInterface uiCfg) {
        displayUIConfig = uiCfg;
        return this;
    }

    public FileChooserBuilder setActionCode(int activityResultCode) {
        activityActionCode = activityResultCode;
        return this;
    }

    public FileChooserBuilder setNavigationFoldersList(List<DefaultNavFoldersType> navFoldersList) {
        defaultNavFoldersList = navFoldersList;
        return this;
    }

    public FileChooserBuilder showHidden(boolean enable) {
        showHidden = enable;
        return this;
    }

    public FileChooserBuilder setBaseTheme(int localThemeResId) {
        throw new FileChooserException.NotImplementedException();
    }

    public FileChooserBuilder setSelectMultiple(int maxFileInsts) {
        maxSelectedFiles = maxFileInsts;
        return this;
    }

    public FileChooserBuilder setSelectionMode(SelectionModeType modeType) {
        pathSelectMode = modeType;
        return this;
    }

    public FileChooserBuilder setPickerInitialPath(BaseFolderPathType storageAccessBase) {
        initFolderBasePathType = storageAccessBase;
        //BasicFileProvider.getInstance().selectBaseDirectoryByType(storageAccessBase);
        return this;
    }

    public FileChooserBuilder enforceSandboxTopLevelDirectory(BaseFolderPathType storageAccessBase,
                                                              boolean excludeOtherOutsideNav) {
        throw new FileChooserException.NotImplementedException();
    }

    public FileChooserBuilder setDefaultExceptionType(FileChooserException.AndroidFilePickerLightException exInst) {
        defaultExceptionType = exInst;
        return this;
    }

    public FileChooserBuilder setActivityIdleTimeout(long timeoutMillis) {
        idleTimeoutMillis = timeoutMillis;
        return this;
    }

    public static final boolean INCLUDE_FILES_IN_FILTER_PATTERN = true;
    public static final boolean EXCLUDE_FILES_IN_FILTER_PATTERN = false;

    public FileChooserBuilder filterByDefaultFileTypes(List<FileTypes.DefaultFileTypes> fileTypesList, boolean includeExcludeInList) {
        localFileFilter = new FileFilter.FileFilterByDefaultTypesList(fileTypesList, includeExcludeInList);
        return this;
    }

    public FileChooserBuilder filterByMimeTypes(List<String> fileTypesList, boolean includeExcludeInList) {
        localFileFilter = new FileFilter.FileFilterByMimeType(fileTypesList, includeExcludeInList);
        return this;
    }

    public FileChooserBuilder filterByRegex(String fileFilterPattern, boolean includeExcludeInList) {
        localFileFilter = new FileFilter.FileFilterByRegex(fileFilterPattern, includeExcludeInList);
        return this;
    }

    public FileChooserBuilder setDataItemsFormatter() {
        throw new FileChooserException.NotImplementedException();
    }

    public FileChooserBuilder setFilesListSortCompareFunction(FileTypes.FileItemsListSortFunc customSortFunc) {
        this.customSortFunc = customSortFunc;
        return this;
    }

    public FileChooserBuilder setExternalFilesProvider(ContentProvider extFileProvider) {
        throw new FileChooserException.NotImplementedException();
    }

    public long getIdleTimeout() {
        return idleTimeoutMillis;
    }

    public DisplayConfigInterface getDisplayConfig() {
        return displayUIConfig;
    }

    public BaseFolderPathType getInitialBaseFolder() {
        return initFolderBasePathType;
    }

    public boolean showHidden() {
        return showHidden;
    }

    public int getMaxSelectedFilesCount() {
        return maxSelectedFiles;
    }

    public SelectionModeType getSelectionMode() {
        return pathSelectMode;
    }

    public List<DefaultNavFoldersType> getNavigationFoldersList() {
        return defaultNavFoldersList;
    }

    public FileFilter.FileFilterInterface getFileFilter() {
        return localFileFilter;
    }

    public FileTypes.FileItemsListSortFunc getCustomSortFunc() {
        return customSortFunc;
    }

    public static List<FileTypes.FileType> filterAndSortFileItemsList(List<FileTypes.FileType> inputFileItems,
                                                                      FileFilter.FileFilterInterface fileFilter, FileTypes.FileItemsListSortFunc sortCompFunc) {
        List<FileTypes.FileType> allowedFileItemsList = new ArrayList<FileTypes.FileType>();
        for(FileTypes.FileType fileItem : inputFileItems) {
            if(fileFilter == null) {
                allowedFileItemsList = inputFileItems;
                break;
            }
            if(fileFilter.fileMatchesFilter(fileItem)) {
                allowedFileItemsList.add(fileItem);
            }
        }
        if(sortCompFunc != null) {
            return sortCompFunc.sortFileItemsList(allowedFileItemsList);
        }
        else {
            return allowedFileItemsList;
        }
    }

    public static final String FILE_PICKER_INTENT_DATA_TYPE_KEY = "FilePickerSelectedIntentDataType";
    public static final String FILE_PICKER_INTENT_DATA_PAYLOAD_KEY = "FilePickerSelectedIntentDataPayloadList";

    public <DataItemTypeT extends Object> List<DataItemTypeT> launchFilePicker() throws FileChooserException.AndroidFilePickerLightException {
        Intent launchPickerIntent = new Intent(activityContextRef.get().getApplicationContext(), FileChooserActivity.class);
        launchPickerIntent.setAction(Intent.ACTION_PICK_ACTIVITY);
        launchPickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        FileChooserActivity.activityBuilderLaunchedRefs.push(this);
        activityContextRef.get().startActivityForResult(launchPickerIntent, activityActionCode);
        try {
            Looper.loop();
        } catch(FileChooserException.AndroidFilePickerLightException rteInst) {
            try {
                if(rteInst.isError()) {
                    throw rteInst;
                }
                else if(!rteInst.hasDataItems()) {
                    return new ArrayList<DataItemTypeT>();
                }
                List<DataItemTypeT> selectedDataItems = rteInst.getTypedDataAsList();
                /* The next procedure is necessary because for some reason the app otherwise
                 * freezes without bringing the original Activity context back to the front:
                 */
                activityContextRef.get().moveTaskToBack(false);
                Intent bringToFrontIntent = new Intent(activityContextRef.get(), activityContextRef.get().getClass());
                bringToFrontIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                activityContextRef.get().startActivity(bringToFrontIntent);
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
    public static void handleActivityResult(Activity activityInst, int requestCode, int resultCode, Intent data) throws FileChooserException.AndroidFilePickerLightException {
        if(activityInst == null || data == null) {
            throw new FileChooserException.CommunicateNoDataException();
        }
        switch (requestCode) {
            case ACTIVITY_CODE_SELECT_FILE:
            case ACTIVITY_CODE_SELECT_DIRECTORY_ONLY:
            case ACTIVITY_CODE_SELECT_MULTIPLE_FILES:
                if(resultCode == RESULT_OK) {
                    FileChooserException.AndroidFilePickerLightException resultOKException = new FileChooserException.CommunicateSelectionDataException();
                    resultOKException.packageDataItemsFromIntent(data);
                    throw resultOKException;
                }
                break;
            default:
                break;

        }
        FileChooserException.AndroidFilePickerLightException resultNotOKException = new FileChooserException.GenericRuntimeErrorException();
        resultNotOKException.packageDataItemsFromIntent(data);
        throw resultNotOKException;
    }

}
