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

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class FilePickerBuilder implements Serializable {

    private static String LOGTAG = FilePickerBuilder.class.getSimpleName();

    private static WeakReference<FileChooserActivity> activityContextRef;
    public void setActivityContext(FileChooserActivity activityRef) {
        activityContextRef = new WeakReference<FileChooserActivity>(activityRef);
    }

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
        public static final Map<DefaultNavFoldersType, Drawable> NAV_FOLDER_ICON_RESIDS_MAP = new HashMap<>();
        static {
            for (DefaultNavFoldersType navType : values()) {
                NAV_FOLDER_NAME_LOOKUP_MAP.put(navType, navType.toString());
                NAV_FOLDER_DESC_MAP.put(navType, navType.getFolderLabel());
                NAV_FOLDER_PATHS_MAP.put(navType, navType.getBaseFolderPathType());
                NAV_FOLDER_ICON_RESIDS_MAP.put(navType, navType.getFolderIconDrawable());
            }
        }

        private String folderLabel;
        private BaseFolderPathType baseFolderSpec;
        private int folderIconResId;
        private Drawable customIconObj;

        private DefaultNavFoldersType(String folderLabel, int folderIconResId, BaseFolderPathType baseFolderSpec) {
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

        public Drawable getFolderIconDrawable() {
            if(customIconObj == null && activityContextRef != null) {
                return GradientDrawableFactory.getDrawableFromResource(folderIconResId);
            }
            else if(customIconObj != null) {
                return customIconObj;
            }
            throw new FilePickerException.InvalidActivityContextException();
        }

        public void setCustomFolderIconDrawable(Drawable folderIconDrawInst) {
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
    private ContentProvider externalFilesProvider;
    private long idleTimeoutMillis;
    private FileFilter.FileFilterInterface localFileFilter;
    private FileTypes.FileItemsListSortFunc customSortFunc;

    public static final long NO_ABORT_TIMEOUT = -1;
    public static final int DEFAULT_MAX_SELECTED_FILES = 10;

    public FilePickerBuilder(FileChooserActivity activityContextInst) {
        activityContextRef = new WeakReference<FileChooserActivity>(activityContextInst);
        defaultExceptionType = FilePickerException.CommunicateSelectionDataException.getNewInstance();
        lastError = null;
        displayUIConfig = DisplayConfigInterface.getDefaultsInstance();
        activityActionCode = ACTIVITY_CODE_SELECT_FILE;
        defaultNavFoldersList = getDefaultNavFoldersList();
        showHidden = false;
        maxSelectedFiles = DEFAULT_MAX_SELECTED_FILES;
        localThemeResId = R.style.LibraryDefaultTheme;
        initFolderBasePathType = BaseFolderPathType.BASE_PATH_TYPE_FILES_DIR;
        pathSelectMode = SelectionModeType.SELECT_OMNIVORE;
        externalFilesProvider = null;
        idleTimeoutMillis = NO_ABORT_TIMEOUT;
        localFileFilter = null;
    }

    public static FilePickerBuilder getSingleFilePickerInstance(FileChooserActivity activityContextInst) {
        FilePickerBuilder pickerBuilderInst = new FilePickerBuilder(activityContextInst);
        pickerBuilderInst.pathSelectMode = SelectionModeType.SELECT_DIRECTORY_ONLY;
        pickerBuilderInst.maxSelectedFiles = 1;
        return pickerBuilderInst;
    }

    public static FilePickerBuilder getDirectoryChooserInstance(FileChooserActivity activityContextInst) {
        FilePickerBuilder pickerBuilderInst = new FilePickerBuilder(activityContextInst);
        pickerBuilderInst.pathSelectMode = SelectionModeType.SELECT_FILE;
        pickerBuilderInst.maxSelectedFiles = 1;
        return pickerBuilderInst;
    }

    public boolean allowSelectFileItems() {
        return pathSelectMode.ordinal() != SelectionModeType.SELECT_DIRECTORY_ONLY.ordinal();
    }

    public boolean allowSelectFolderItems() {
        return pathSelectMode.ordinal() == SelectionModeType.SELECT_DIRECTORY_ONLY.ordinal() ||
                pathSelectMode.ordinal() != SelectionModeType.SELECT_FILE.ordinal();
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

    public FilePickerBuilder setBaseTheme(int localThemeResId) {
        throw new FilePickerException.NotImplementedException();
    }

    public FilePickerBuilder setSelectMultiple(int maxFileInsts) {
        maxSelectedFiles = maxFileInsts;
        return this;
    }

    public FilePickerBuilder setSelectionMode(SelectionModeType modeType) {
        pathSelectMode = modeType;
        return this;
    }

    public FilePickerBuilder setPickerInitialPath(BaseFolderPathType storageAccessBase) {
        initFolderBasePathType = storageAccessBase;
        activityContextRef.get().getFileProviderInstance().selectBaseDirectoryByType(storageAccessBase);
        return this;
    }

    public FilePickerBuilder enforceSandboxTopLevelDirectory(BaseFolderPathType storageAccessBase,
                                                             boolean excludeOtherOutsideNav) {
        throw new FilePickerException.NotImplementedException();
    }

    public FilePickerBuilder setDefaultExceptionType(FilePickerException.AndroidFilePickerLightException exInst) {
        defaultExceptionType = exInst;
        return this;
    }

    public FilePickerBuilder setActivityIdleTimeout(long timeoutMillis) {
        idleTimeoutMillis = timeoutMillis;
        return this;
    }

    public static final boolean INCLUDE_FILES_IN_FILTER_PATTERN = true;
    public static final boolean EXCLUDE_FILES_IN_FILTER_PATTERN = false;

    public FilePickerBuilder filterByDefaultFileTypes(List<FileTypes.DefaultFileTypes> fileTypesList, boolean includeExcludeInList) {
        localFileFilter = new FileFilter.FileFilterByDefaultTypesList(fileTypesList, includeExcludeInList);
        return this;
    }

    public FilePickerBuilder filterByMimeTypes(List<String> fileTypesList, boolean includeExcludeInList) {
        localFileFilter = new FileFilter.FileFilterByMimeType(fileTypesList, includeExcludeInList);
        return this;
    }

    public FilePickerBuilder filterByRegex(String fileFilterPattern, boolean includeExcludeInList) {
        localFileFilter = new FileFilter.FileFilterByRegex(fileFilterPattern, includeExcludeInList);
        return this;
    }

    public FilePickerBuilder setDataItemsFormatter() {
        throw new FilePickerException.NotImplementedException();
    }

    public FilePickerBuilder setFilesListSortCompareFunction(FileTypes.FileItemsListSortFunc customSortFunc) {
        this.customSortFunc = customSortFunc;
        return this;
    }

    public FilePickerBuilder setExternalFilesProvider(ContentProvider extFileProvider) {
        throw new FilePickerException.NotImplementedException();
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

    public static final String FILE_PICKER_BUILDER_EXTRA_DATA_KEY = "FilePickerBuilderExtraData";
    public static final String FILE_PICKER_INTENT_DATA_TYPE_KEY = "FilePickerSelectedIntentDataType";
    public static final String FILE_PICKER_INTENT_DATA_PAYLOAD_KEY = "FilePickerSelectedIntentDataPayloadList";

    public <DataItemTypeT extends Object> List<DataItemTypeT> launchFilePicker() throws FilePickerException.AndroidFilePickerLightException {
        Intent launchPickerIntent = new Intent(activityContextRef.get(), FileChooserActivity.class);
        launchPickerIntent.setAction(Intent.ACTION_PICK_ACTIVITY);
        launchPickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        launchPickerIntent.putExtra(FILE_PICKER_BUILDER_EXTRA_DATA_KEY, this);
        activityContextRef.get().startActivityForResult(launchPickerIntent, activityActionCode);
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
    public static void handleActivityResult(Activity activityInst, int requestCode, int resultCode, Intent data) throws FilePickerException.AndroidFilePickerLightException {
        if(activityInst == null || data == null) {
            throw new FilePickerException.CommunicateNoDataException();
        }
        switch (requestCode) {
            case ACTIVITY_CODE_SELECT_FILE:
            case ACTIVITY_CODE_SELECT_DIRECTORY_ONLY:
            case ACTIVITY_CODE_SELECT_MULTIPLE_FILES:
                if(resultCode == RESULT_OK) {
                    FilePickerException.AndroidFilePickerLightException resultOKException = new FilePickerException.CommunicateSelectionDataException();
                    resultOKException.packageDataItemsFromIntent(data);
                    throw resultOKException;
                }
                break;
            default:
                break;

        }
        FilePickerException.AndroidFilePickerLightException resultNotOKException = new FilePickerException.GenericRuntimeErrorException();
        resultNotOKException.packageDataItemsFromIntent(data);
        throw resultNotOKException;
    }

}
