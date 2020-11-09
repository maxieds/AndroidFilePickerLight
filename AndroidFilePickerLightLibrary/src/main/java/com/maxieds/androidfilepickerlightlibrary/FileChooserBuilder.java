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
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class FileChooserBuilder implements Serializable {

    private static String LOGTAG = FileChooserBuilder.class.getSimpleName();

    public enum DefaultFileTypes {

        PLAINTEXT_FILE_TYPE(R.drawable.text_file_icon32, "Text",
                new String[] { "txt", "out", "rtf", "sh", "py", "lst", "csv", "xml", "keys", "cfg", "dat", "log", "run" },
                new String[] { "text/plain", "text/*" }),
        BINARY_FILE_TYPE(R.drawable.binary_file_icon32, "Binary",
                new String[] { "dmp", "dump", "hex", "bin", "mfd", "exe" },
                new String[] { "application/octet-stream" }),
        DOCUMENTS_FILE_TYPE(R.drawable.document_file_icon32, "Document",
                new String[] { "pdf", "doc", "docx", "odt", "xls", "ppt", "numbers" },
                new String[] { "text/*", "application/pdf", "application/doc" }),
        IMAGE_FILE_TYPE(R.drawable.image_file_icon32, "Image",
                new String[] { "bmp", "gif", "ico", "jpeg", "jpg", "pcx", "png", "psd", "tga", "tiff", "tif", "xcf" },
                new String[] { "image/*" }),
        MEDIA_FILE_TYPE(R.drawable.media_file_icon32, "Media",
                new String[] { "aiff", "aif", "wav", "flac", "m4a", "wma", "amr", "mp2", "mp3", "wma", "aac", "mid", "m3u",
                        "avi", "mov", "wmv", "mkv", "3gp", "f4v", "flv", "mp4", "mpeg", "webm" },
                new String[] { "audio/*", "video/*" }),
        FOLDER_FILE_TYPE(R.drawable.folder_icon32, "Folder", new String[] {},
                new String[] { "vnd.android.document/directory" }),
        HIDDEN_FILE_TYPE(R.drawable.hidden_file_icon32, "Hidden", new String[] {},
                new String[] { "*/*" }),
        COMPRESSED_ARCHIVE_FILE_TYPE(R.drawable.compressed_archive_file_icon32, "Archive",
                new String[] { "cab", "7z", "alz", "arj", "bzip2", "bz2", "dmg", "gzip", "gz", "jar", "lz",
                        "lzip", "lzma", "zip", "rar", "tar", "tgz"},
                new String[] { "application/octet-stream", "text/*" }),
        APK_FILE_TYPE(R.drawable.apk_file_icon32, "APK",
                new String[] { "apk", "aab" }, new String[] { "application/vnd.android.package-archive" }),
        CUSTOM_FILE_TYPE(R.drawable.unknown_file_icon32, "Custom", new String[] {}, new String[] { "*/*" }),
        UNKNOWN_FILE_TYPE(R.drawable.unknown_file_icon32, "Unknown", new String[] {}, new String[] { "*/*" });

        private int iconResId;
        private String typeShortDesc;
        private String[] fileExtList;
        private String[] supportedMimeTypes;

        DefaultFileTypes(int iconResIdParam, String shortDesc, String[] fileExtListParam, String[] mimeTypesParam) {
            iconResId = iconResIdParam;
            typeShortDesc = shortDesc;
            fileExtList = fileExtListParam;
            supportedMimeTypes = mimeTypesParam;
        }

        /* TODO: Add accessor methods and ability to construct a FileFilter comparator for the type ... */

    }

    public enum DefaultNavFoldersType {

        FOLDER_SDCARD_STORAGE("SD Card", R.attr.namedFolderSDCardIcon, BaseFolderPathType.BASE_PATH_TYPE_SDCARD),
        FOLDER_PICTURES("Pictures", R.attr.namedFolderPicsIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_PICTURES),
        FOLDER_CAMERA("Camera", R.attr.namedFolderCameraIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_PICTURES),
        FOLDER_SCREENSHOTS("Screenshots", R.attr.namedFolderScreenshotsIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_SCREENSHOTS),
        FOLDER_DOWNLOADS("Downloads", R.attr.namedFolderDownloadsIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_DOWNLOADS),
        FOLDER_USER_HOME("Home", R.attr.namedFolderUserHomeIcon, BaseFolderPathType.BASE_PATH_TYPE_USER_DATA_DIR),
        FOLDER_MEDIA_VIDEO("Media", R.attr.namedFolderMediaIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_DCIM);

        public static final Map<String, DefaultNavFoldersType> NAV_FOLDER_NAME_LOOKUP_MAP = new HashMap<>();
        public static final Map<DefaultNavFoldersType, String> NAV_FOLDER_DESC_MAP = new HashMap<>();
        public static final Map<DefaultNavFoldersType, BaseFolderPathType> NAV_FOLDER_PATHS_MAP = new HashMap<>();
        public static final Map<Integer, Integer> NAV_FOLDER_ICON_RESIDS_MAP = new HashMap<>();
        static {
            for (DefaultNavFoldersType navType : values()) {
                NAV_FOLDER_NAME_LOOKUP_MAP.put(navType.toString(), navType);
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
        navFoldersList.add(DefaultNavFoldersType.NAV_FOLDER_NAME_LOOKUP_MAP.get("FOLDER_SDCARD_STORAGE"));
        navFoldersList.add(DefaultNavFoldersType.NAV_FOLDER_NAME_LOOKUP_MAP.get("FOLDER_USER_HOME"));
        navFoldersList.add(DefaultNavFoldersType.NAV_FOLDER_NAME_LOOKUP_MAP.get("FOLDER_PICTURES"));
        navFoldersList.add(DefaultNavFoldersType.NAV_FOLDER_NAME_LOOKUP_MAP.get("FOLDER_DOWNLOADS"));
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

        public static final Map<Integer, BaseFolderPathType> NAV_FOLDER_INDEX_TO_INST_MAP = new HashMap<>();
        public static final Map<String, BaseFolderPathType> NAV_FOLDER_NAME_TO_INST_MAP = new HashMap<>();
        static {
            for (BaseFolderPathType folderType : values()) {
                 NAV_FOLDER_INDEX_TO_INST_MAP.put(folderType.ordinal(), folderType);
                 NAV_FOLDER_NAME_TO_INST_MAP.put(folderType.name(), folderType);
            }
        }

        public static BaseFolderPathType getInstanceByName(String folderTypeName) {
            return BaseFolderPathType.NAV_FOLDER_INDEX_TO_INST_MAP.get(folderTypeName);
        }

        public static BaseFolderPathType getInstanceByType(BaseFolderPathType folderType) {
            return BaseFolderPathType.NAV_FOLDER_INDEX_TO_INST_MAP.get(folderType.ordinal());
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

    private static FileChooserBuilder localActivityBuildeStaticInst;
    public static FileChooserBuilder getInstance() { return localActivityBuildeStaticInst; }

    private WeakReference<Activity> activityContextRef;
    public Activity getClientActivityReference() {
        return activityContextRef.get();
    }

    private FileChooserException.AndroidFilePickerLightException defaultExceptionType;
    private ThemesConfigInterface displayUIConfig;
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
    private FileFilter.FileItemsListSortFunc customSortFunc;

    public static final long NO_ABORT_TIMEOUT = -1;
    public static final long DEFAULT_TIMEOUT = 250 * 1000;
    public static final int DEFAULT_MAX_SELECTED_FILES = 10;

    public FileChooserBuilder(Activity activityContextInst) {
        localActivityBuildeStaticInst = this;
        activityContextRef = new WeakReference<Activity>(activityContextInst);
        defaultExceptionType = FileChooserException.CommunicateSelectionDataException.getNewInstance();
        displayUIConfig = ThemesConfigInterface.getDefaultsInstance();
        activityActionCode = ACTIVITY_CODE_SELECT_FILE;
        defaultNavFoldersList = getDefaultNavFoldersList();
        showHidden = false;
        maxSelectedFiles = DEFAULT_MAX_SELECTED_FILES;
        localThemeResId = R.style.LibraryDefaultTheme;
        initFolderBasePathType = BaseFolderPathType.getInstanceByType(BaseFolderPathType.BASE_PATH_TYPE_FILES_DIR);
        pathSelectMode = SelectionModeType.SELECT_OMNIVORE;
        externalFilesProvider = null;
        idleTimeoutMillis = DEFAULT_TIMEOUT;
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

    public FileChooserBuilder setDisplayUIConfig(ThemesConfigInterface uiCfg) {
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

    public FileChooserBuilder filterByDefaultFileTypes(List<DefaultFileTypes> fileTypesList, boolean includeExcludeInList) {
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

    public FileChooserBuilder setFilesListSortCompareFunction(FileFilter.FileItemsListSortFunc customSortFunc) {
        this.customSortFunc = customSortFunc;
        return this;
    }

    public FileChooserBuilder setExternalFilesProvider(ContentProvider extFileProvider) {
        throw new FileChooserException.NotImplementedException();
    }

    public long getIdleTimeout() {
        return idleTimeoutMillis;
    }

    public ThemesConfigInterface getDisplayConfig() {
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

    public FileFilter.FileItemsListSortFunc getCustomSortFunc() {
        return customSortFunc;
    }

    public static List<DisplayTypes.FileType> filterAndSortFileItemsList(List<DisplayTypes.FileType> inputFileItems,
                                                                         FileFilter.FileFilterInterface fileFilter, FileFilter.FileItemsListSortFunc sortCompFunc) {
        Log.i(LOGTAG, String.format(Locale.getDefault(), "Sorting lst of size = %d : %s", inputFileItems.size(),
                inputFileItems.size() == 0 ? "[NO PATHS TO SHOW]" : inputFileItems.get(0).getAbsolutePath()));
        List<DisplayTypes.FileType> allowedFileItemsList = new ArrayList<DisplayTypes.FileType>();
        for(DisplayTypes.FileType fileItem : inputFileItems) {
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

    public static final String FILE_PICKER_INTENT_DATA_TYPE_KEY = "FilePickerIntentKey.SelectedIntentDataType";
    public static final String FILE_PICKER_INTENT_DATA_PAYLOAD_KEY = "FilePickerIntentKey.SelectedIntentDataPayloadList";
    public static final String FILE_PICKER_EXCEPTION_MESSAGE_KEY = "FilePickerIntentKey.UnexpectedExitMessage";
    public static final String FILE_PICKER_EXCEPTION_CAUSE_KEY = "FilePickerIntentKey.ExceptionCauseDescKey";

    public void launchFilePicker() throws FileChooserException.AndroidFilePickerLightException {
        Intent launchPickerIntent = new Intent(activityContextRef.get().getApplicationContext(), FileChooserActivity.class);
        launchPickerIntent.setAction(Intent.ACTION_PICK_ACTIVITY);
        launchPickerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activityContextRef.get().startActivityForResult(launchPickerIntent, activityActionCode);
    }

    public static final int ACTIVITY_CODE_SELECT_FILE = 1;
    public static final int ACTIVITY_CODE_SELECT_DIRECTORY_ONLY = 2;
    public static final int ACTIVITY_CODE_SELECT_MULTIPLE_FILES = 3;

    /* Client code should call this method in their main Activity's onActivityResult function
     * to handle the logic there when the activity was created as a file picker instance:
     */
    public static List<String> handleActivityResult(Activity activityInst, int requestCode, int resultCode, Intent data)
            throws FileChooserException.AndroidFilePickerLightException {
        if(activityInst == null || data == null) {
            throw new FileChooserException.CommunicateNoDataException();
        }
        switch(requestCode) {
            case ACTIVITY_CODE_SELECT_FILE:
            case ACTIVITY_CODE_SELECT_DIRECTORY_ONLY:
            case ACTIVITY_CODE_SELECT_MULTIPLE_FILES:
                if(resultCode == RESULT_OK) {
                    FileChooserException.AndroidFilePickerLightException resultOKException = new FileChooserException.CommunicateSelectionDataException();
                    List<String> selectedDataItems = resultOKException.packageDataItemsFromIntent(data);
                    finishActivityResultHandler(activityInst);
                    return selectedDataItems;
                }
                try {
                    String getExitErrorMsg = data.getStringExtra(FILE_PICKER_EXCEPTION_MESSAGE_KEY);
                    finishActivityResultHandler(activityInst); // ???
                    throw FileChooserException.getExceptionForExitCause(data.getStringExtra(FILE_PICKER_EXCEPTION_CAUSE_KEY), getExitErrorMsg);
                } catch(NullPointerException npe) {}
                break;
            default:
                break;

        }
        FileChooserException.AndroidFilePickerLightException resultNotOKException = new FileChooserException.GenericRuntimeErrorException();
        resultNotOKException.packageDataItemsFromIntent(data);
        finishActivityResultHandler(activityInst); // ???
        throw resultNotOKException;
    }

    /* The next procedure is necessary because for some reason the app otherwise
     * freezes without bringing the original Activity context back to the front:
     */
    public static void finishActivityResultHandler(Activity activityInst) {
        activityInst.moveTaskToBack(false);
        Intent bringToFrontIntent = new Intent(activityInst, activityInst.getClass());
        bringToFrontIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        FileChooserActivity.getInstance().startActivity(bringToFrontIntent);
    }

    public StringBuilder readFileContentsAsString() {
        return null;
    }

    public byte[] readFileContentsAsBytesArray() {
        return null;
    }

}
