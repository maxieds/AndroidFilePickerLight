# 📂 Android File Chooser (Picker) Light Library

<hr />

<img src="https://jitpack.io/v/maxieds/AndroidFilePickerLight.svg" /><img src="https://img.shields.io/badge/API%2029%2B-Tested%20on%20Android%2010-yellowgreen" /><img src="https://badges.frapsoft.com/os/gpl/gpl.svg?v=103" /> 

<img src="https://forthebadge.com/images/badges/made-with-java.svg" /><img src="https://forthebadge.com/images/badges/powered-by-coffee.svg" /><img src="https://forthebadge.com/images/badges/built-for-android.svg" />

<img src="https://raw.githubusercontent.com/maxieds/AndroidFileChooserLight/master/Screenshots/ReadmeEmojiBadges/HackerCatEmoji-v1.png" /><img src="https://badges.frapsoft.com/os/v2/open-source-175x29.png?v=103" /><img src="https://raw.githubusercontent.com/maxieds/AndroidFileChooserLight/master/Screenshots/ReadmeEmojiBadges/tRUTH.png" />

<hr />

## About the library 

A file and directory chooser widget for Android that focuses on presenting an easy to configure lightweight UI.
This library is intended to be a replacement for other picker libraries that 
works with the new **Android 11** file system and 
[storage management changes](https://developer.android.com/about/versions/11/privacy/storage). 
The source is made openly available as free software according to the 
[project license](https://github.com/maxieds/AndroidFilePickerLight/blob/main/LICENSE). 

The main design considerations were to 
create a file picker library with as minimal a footprint as possible to do basic file selection 
operations, and that the resulting library chooser display must be very easy to extend and 
configure with respect to its look-and-feel themes, color schemes, icons and other UI options that 
users will want to customize to their client application. 
I was unable to find a solid external library for my application use cases that was not 
bloated with respect to media loaders and image processing features, that could be easily 
extended, and that was not limited by only a cumbersome list of built-in themes that the 
user can select. Therefore, I decided to take the best functionality I found in other libraries 
(many written in Kotlin) and write a custom implementation in Java while keeping the 
media file processing minimal. 

### Feature set

Key features in the library include the following:
* Easy to configure themes and UI display settings including icons and color choices
  (see [docs link below](https://github.com/maxieds/AndroidFileChooserLight#configuring-the-client-theme-and-ui-look-and-feel-properties))
* Simple actions and extendable Java interface to select and filter files/directories
  (see [docs link below](https://github.com/maxieds/AndroidFileChooserLight#extending-file-types-for-filtering-and-sorting-purposes-in-the-picker-ui))
* Allows client code to access many standard file system types on the Android device without 
  complicated procedures and permissions issues inherited by the new Android 11 policy changes
  
### Screenshots of the library in action

<img src="https://raw.githubusercontent.com/maxieds/AndroidFileChooserLight/master/Screenshots/WorkingUI-Screenshot_20201112-052224.png" width="250" /> <img src="https://raw.githubusercontent.com/maxieds/AndroidFileChooserLight/master/Screenshots/WorkingUI-Screenshot_20201113-134724.png" width="250" /> <img src="https://raw.githubusercontent.com/maxieds/AndroidFilePickerLight/master/Screenshots/SampleApplicationDemo-ProgressBarDisplay.png" width="250" />
<img src="https://raw.githubusercontent.com/maxieds/AndroidFileChooserLight/master/Screenshots/SampleAppGreeenThemeWithPosixStylePerms-Screenshot_20201117-124350.png" width="250" /> <img src="https://raw.githubusercontent.com/maxieds/AndroidFileChooserLight/master/Screenshots/SampleAppThemeOrangeWithGradientToolbars-Screenshot_20201117-065739.png" width="250" /> <img src="https://raw.githubusercontent.com/maxieds/AndroidFileChooserLight/master/Screenshots/SampleAppThemeOrangeWithNavLongLabels-Screenshot_20201117-094425.png" width="250" />
## Including the library for use in a client Android application

There are a couple of quickstart items covered in the sections below to handle before this
library can be included in the client Android application:
* Include the library using [Jitpack.io/GitHub](https://jitpack.io/#maxieds/AndroidFilePickerLight) 
  in the application *build.gradle* configuration.
* Update the project *AndroidManifest.xml* file to extend the documents provider, 
  request required permissions, and setup some helpful legacy file handling options for devices 
  targeting Android platforms with SDK < Android OS 11.

### Application build.gradle modifications

We will require the following small modifications to the client **project** *build.gradle* 
configuration:
```bash
android {
     defaultConfig {
        minSdkVersion 29
     }
     compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
dependencies {
    // FOR the most recent build:
    implementation 'com.github.maxieds:AndroidFilePickerLight:master-SNAPSHOT'
    // -- OR: for the latest release:
    implementation 'com.github.maxieds:AndroidFilePickerLight:release-v6'
}
allprojects {
    repositories {
        maven {
            url 'https://maven.fabric.io/public'
        }
    }
}
```

### Project manifest modifications

For applications targeting so-called legacy platforms, that is Android devices where the new 
Android 11 storage management options are not explicitly required, it is 
recommended for compatibility sake by the Google developer docs 
that the application set the option
``requestLegacyExternalStorage="true"``. For example, use the following code:
```xml
<application
        android:name=".MyAndroidProjectName"
        android:description="@string/appDesc"
        android:icon="@drawable/appIcon"
        android:label="@string/appLabelDesc"
        android:roundIcon="@drawable/appRoundIcon"
        android:theme="${appTheme}"
        android:launchMode="singleTop"
        android:manageSpaceActivity=".MyAndroidProjectMainActivity"
        android:requestLegacyExternalStorage="true"
        android:preserveLegacyExternalStorage="true"
        android:allowBackup="true"
        >
     <!-- Complete the internals of the application tag (activities, etc.) below -->
</application>
```
Note that unlike some samples to get other Android libraries up and running, there is no need to define references 
to the custom ``FileProvider`` implemented by the library. It is sufficient to just use the standardized wrappers 
to launch a new ``FileChooserActivity`` instance and use the file picker functionality bundled within that interface.

## Sample client source code in Java

The next examples document basic, advanced, and custom uses of the library in client code. 
The file chooser instance is launched via a traditional ``startActivityForResult`` call 
from within the client caller's code. The following is a suggestion as to how to handle 
the results:
```java
@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Handle activity codes:
        // FileChooserBuilder.ACTIVITY_CODE_SELECT_FILE_ONLY ||
        // FileChooserBuilder.ACTIVITY_CODE_SELECT_DIRECTORY_ONLY || 
        // FileChooserBuilder.ACTIVITY_CODE_SELECT_MULTIPLE_FILES:
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
    }
```
The following are the unique ``Intent`` keys that are associated with the returned data (if available):
```java
    public static final String FILE_PICKER_INTENT_DATA_TYPE_KEY = "FilePickerIntentKey.SelectedIntentDataType";
    public static final String FILE_PICKER_INTENT_DATA_PAYLOAD_KEY = "FilePickerIntentKey.SelectedIntentDataPayloadList";
    public static final String FILE_PICKER_EXCEPTION_MESSAGE_KEY = "FilePickerIntentKey.UnexpectedExitMessage";
    public static final String FILE_PICKER_EXCEPTION_CAUSE_KEY = "FilePickerIntentKey.ExceptionCauseDescKey";
```

### Basic usage: Returning a file path selected by the user

This is a quick method to select a file and/or directory picked by the user:
```java
    public void actionButtonLaunchSingleFilePickerActivity(View btnView) {
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
        fpInst.setPickerInitialPath(FileChooserBuilder.BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_DOWNLOADS);
        fpInst.launchFilePicker();
    }
```

### Detailed list of non-display type options

The next options are available to configure the non-display type (e.g., properties of the 
file chooser that do not depend on how it looks) properties of the library. 
These can be set using the ``AndroidFilePickerLight.Builder`` class as follows:
```java
    FileChooserBuilder fcBuilderConfig = new FileChooserBuilder.getDirectoryChooserInstance(FileChooserActivity.getInstance())
        .allowSelectFileItems()
        .allowSelectFolderItems()
        .setCustomThemeStylizerConfig(CustomThemeBuilder uiCfg) // See docs below
        .setActionCode(int activityResultCode)
        .setNavigationFoldersList(List<DefaultNavFoldersType> navFoldersList)
        .setNavigationLongForm(true)
        .showHidden(boolean enable)
        .setInitialPathAbsolute(String startPathAbs)
        .setInitialPathRelative(String startPathRelOffset) /* Relative to the base folder path selected */
        .setInitialPath(BaseFolderPathType storageAccessBase)
        .setInitialPath(BaseFolderPathType storageAccessBase, String startPathRelOffset)
        .setSelectMultiple(int maxFileInsts)
        .setSelectionMode(SelectionModeType modeType)
        .setActivityIdleTimeout(long timeoutMillis)
        .setExternalFilesProvider(DocumentsProvider extFileProvider);
```
The relevant ``enum`` types that can be passed as arguments to these methods include the following:
```java
    public enum SelectionModeType {
        SELECT_FILE_ONLY,
        SELECT_MULTIPLE_FILES,
        SELECT_DIRECTORY_ONLY,
        SELECT_OMNIVORE
    }
    public enum BaseFolderPathType {
        BASE_PATH_TYPE_FILES_DIR,
        BASE_PATH_TYPE_EXTERNAL_FILES_DOWNLOADS,
        BASE_PATH_TYPE_EXTERNAL_FILES_MOVIES,
        BASE_PATH_TYPE_EXTERNAL_FILES_MUSIC,
        BASE_PATH_TYPE_EXTERNAL_FILES_DOCUMENTS,
        BASE_PATH_TYPE_EXTERNAL_FILES_DCIM,
        BASE_PATH_TYPE_EXTERNAL_FILES_PICTURES,
        BASE_PATH_TYPE_EXTERNAL_FILES_SCREENSHOTS,
        BASE_PATH_TYPE_USER_DATA_DIR,
        BASE_PATH_TYPE_MEDIA_STORE,
        BASE_PATH_SECONDARY_STORAGE,
        BASE_PATH_DEFAULT,
        BASE_PATH_EXTERNAL_PROVIDER;
    }
    public enum DefaultNavFoldersType {
        FOLDER_ROOT_STORAGE("Root", R.attr.namedFolderSDCardIcon, BaseFolderPathType.BASE_PATH_DEFAULT),
        FOLDER_PICTURES("Pictures", R.attr.namedFolderPicsIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_PICTURES),
        FOLDER_CAMERA("Camera", R.attr.namedFolderCameraIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_PICTURES),
        FOLDER_SCREENSHOTS("Screenshots", R.attr.namedFolderScreenshotsIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_SCREENSHOTS),
        FOLDER_DOWNLOADS("Downloads", R.attr.namedFolderDownloadsIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_DOWNLOADS),
        FOLDER_USER_HOME("Home", R.attr.namedFolderUserHomeIcon, BaseFolderPathType.BASE_PATH_TYPE_USER_DATA_DIR),
        FOLDER_MEDIA_VIDEO("Media", R.attr.namedFolderMediaIcon, BaseFolderPathType.BASE_PATH_TYPE_EXTERNAL_FILES_DCIM);
    }
```
Some other non-display type configuration options that can be set include the following
(*be cautious at tweaking these too much unless you really understand what you are doing!*):
```java
FileChooserBuilder fcBuilderConfig = new FileChooserBuilder.getDirectoryChooserInstance(FileChooserActivity.getInstance())
     /* How many default file items to store in the RecyclerView when it initially loads? */
     .setRecyclerViewStartBufferSize(50)
     /* The size of the non-visible, offscreen items buffer at the top and bottom of the display?
      * Increasing this can improve scroll speed, but may also introduce delays in prefetching these items
      * if there is not much scrolling going on from the user.
      */
     .setRecyclerViewNotVisibleBufferSizes(35)
     /* Set the default fling velocity after which we dampen to improve animation speeds: */
     .setRecyclerViewLayoutFlingDampenThreshold(500)
     /* Speed up, or slow down the interval at which we prefetch file items to pre-buffer the
      * offscreen RecyclerView loading for fast scrolling (in milliseconds):
      */
     .setRecyclerViewPrefetchThreadUpdateDelay(550L)
```

### Configuring the client theme and UI look-and-feel properties

Now that we have the scheme for passing resources to the library to skin/color/custom theme its UI down,
the next bits are to discuss the full listing and type specs for what attributes can actually be
changed and reset on-the-fly. Unfortunately, current Android mechanisms for
[styling and theming](https://developer.android.com/guide/topics/ui/look-and-feel/themes)
UI elements are limited in so much as they are confined to a single resources context.
Among other issues, users would find that trying to merge themes with an application and a library will
result in incompatible ``AndroidManifest.xml`` files. It also means that we cannot (yet) construct an instance
of a ``Theme`` type class that can be invoked to stylize the layouts of an activity context external to that within
which we are currently confined.
The way to get around this technicality of a void specification is not sophisticated.
It is however handled by the user that canuse a ``CustomThemeBuilder``
instance to spec out how they want their custom style for this chooser library to look and feel

**NOTE:** To keep the library instance launched running smoothly, tightly, and without unavoidable bloat,
*please* compress all icon ``Drawable`` references from their native image file format to the lossless
(but nevertheless, highly compressed)
[*WEBP* format (linked instructions for conversions using Android Studio)](https://developer.android.com/studio/write/convert-webp).

Helper and conveninece methods, such as those to obtain a color, resource, or ``Drawable``
object from an attribute resource reference (e.g., ``R.attr.myColorNameRef``) are found as static methods at the top of
[DisplayUtils.java](https://github.com/maxieds/AndroidFileChooserLight/blob/master/AndroidFilePickerLightLibrary/src/main/java/com/maxieds/androidfilepickerlightlibrary/DisplayUtils.java).

#### Full example (detailed usage of the current custom theme/UI display options)

```java
CustomThemeBuilder customThemeBuilder = new CustomThemeBuilder((Activity) myActivityInst)
     .setPickerTitleText(R.string.title_text)
     .setNavBarPrefixText(R.string.navbar_prefix_text)
     .setDoneActionButtonText(R.string.done_action_text)
     .setCancelActionButtonText(R.string.cancel_action_text)
     .setGlobalBackButtonIcon(R.drawable.my_global_back_btn_icon_32x32)
     .setDoneActionButtonIcon(R.drawable.my_done_checkmark_icon_16x16)
     .setCancelActionButtonIcon(R.drawable.my_cancel_xmark_icon_16x16)
     .generateThemeColors(R.color.themeColorBase)
     .setActivityToolbarIcon(R.drawable.my_toolbar_logo_icon_48x48)
     .setNavigationByPathButtonIcon(R.drawable.my_icon_32x32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_ROOT_STORAGE)
     .setNavigationByPathButtonIcon(R.drawable.my_icon_32x32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_PICTURES)
     .setNavigationByPathButtonIcon(R.drawable.my_icon_32x32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_CAMERA)
     .setNavigationByPathButtonIcon(R.drawable.my_icon_32x32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_SCREENSHOTS)
     .setNavigationByPathButtonIcon(R.drawable.my_icon_32x32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_DOWNLOADS)
     .setNavigationByPathButtonIcon(R.drawable.my_icon_32x32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_USER_HOME)
     .setNavigationByPathButtonIcon(R.drawable.my_icon_32x32, FileChooserBuilder.DefaultNavFoldersType.FOLDER_MEDIA_VIDEO)
     .setDefaultFileIcon(R.drawable.my_file_icon_16x16)
     .useToolbarGradients(true)
     .setDefaultHiddenFileIcon(R.drawable.my_hidden_file_icon_16x16)
     .setDefaultFolderIcon(R.drawable.my_folder_icon_16x16);

/* Text size, typeface and style + other customizations: */
/* Action Buttons: Done and Cancel buttons at the bottom navigation part of the screen. */
/* Nav Button Prefix: The "Navigate" prefix text before displaying the stock path nav buttons. */
/* Nav Button Long Display: If FileChooserBuilder.setNavigationLongForm(true) is set, these options
 *                          stylize the text labels shown below the buttons.
 */
/* File Item Meta Data: Stylize the file size and permissions display to the left of the file item names. */
/* File Item Name: Style the actual displayed file (base) names shown on screen. */
ccustomThemeBuilder
     .setActionButtonTextSize(int textSize)
     .setActionButtonTypeface(Typeface textTypeFace)
     .setActionButtonTextAllCaps(boolean allCaps)
     .setActionButtonTextStyle(int textStyle)
     .setNavButtonPrefixTextSize(int textSize)
     .setNavButtonPrefixTextAllCaps(boolean allCaps)
     .setNavButtonPrefixTypeface(Typeface textTypeFace)
     .setNavButtonPrefixTextStyle(int textStyle)
     .setNavButtonLongDisplayTextSize(int textSize)
     .setNavButtonLongDisplayTextAllCaps(boolean allCaps)
     .setNavButtonLongDisplayTypeface(Typeface textTypeFace)
     .setNavButtonLongDisplayTextStyle(int textStyle)
     .setFileItemMetaDataTextVisible(boolean show)
     .setFileItemMetaDataTextSize(int textSize)
     .setFileItemMetaDataTextStyle(int textStyle)
     .setFileItemMetaDataPermissionsDisplayStyle(CustomThemeBuilder.FileItemPermissionsDisplayStyle.PERMISSIONS_DISPLAY_OCTAL_STYLE | PERMISSIONS_DISPLAY_POSIX_STYLE)
     .setFileItemNameTextSize(int textSize)
     .setFileItemNameTypeface(Typeface textTypeface)
     .setFileItemNameTextStyle(int textStyle)

FileChooserBuilder fcbConfig = new FileChooserBuilder();
fcbConfig.setCustomThemeStylizerConfig(customThemeBuilder);
```
Alternately, the exact colors for the theme can be specified explicitly using:
```java
public static final int COLOR_PRIMARY = 0;            /* The main RecyclerView (file items list) background color */
public static final int COLOR_PRIMARY_DARK = 1;       /* Text color of the upper paths folder history views */
public static final int COLOR_PRIMARY_VERY_DARK = 2;
public static final int COLOR_ACCENT = 3;             /* Text color of the file items list */
public static final int COLOR_ACCENT_MEDIUM = 4;      /* Text color of the Done/Cancel action buttons */
public static final int COLOR_ACCENT_LIGHT = 5;       /* Background color of the Done/Cancel action buttons navigation bar */
public static final int COLOR_TOOLBAR_BG = 6;         /* The darker background color of the topmost toolbar */
public static final int COLOR_TOOLBAR_FG = 7;         /* The lighter text color of the topmost toolbar */
public static final int COLOR_TOOLBAR_NAV = 8;        /* The background of the navigate to stock paths navigation bar (prefixed with "Navigate: ") */
public static final int COLOR_TOOLBAR_DIVIDER = 9;    /* The in-between divider color separating the top toolbar and lower navigation */

CustomThemeBuilder customThemeBuilder = new CustomThemeBuilder((Activity) myActivityInst)
     .setThemeColors(@ColorRes int[] colorsList);
```

### Extending file types for filtering and sorting purposes in the picker UI

Many other good file chooser libraries for Android implement extendable ways for users to filter,
select and sort the files that are presented to the user. We choose to offer the same extendable
functionality here while staying tightly coupled with more Java language standard constructs.

The following is an example of how to create a custom file filter for use with this library.
The full interface specification is found in the source file
[FileFilter.java](https://github.com/maxieds/AndroidFileChooserLight/blob/master/AndroidFilePickerLightLibrary/src/main/java/com/maxieds/androidfilepickerlightlibrary/FileFilter.java#L35):
```java
    public static class FileFilterByRegex extends FileFilterBase {
        private Pattern patternSpec;
        public FileFilterByRegex(String regexPatternSpec, boolean inclExcl) {
            patternSpec = Pattern.compile(regexPatternSpec);
            setIncludeExcludeMatchesOption(inclExcl);
        }
        public boolean fileMatchesFilter(String fileAbsName) {
            if(patternSpec.matcher(fileAbsName).matches()) {
                return includeExcludeMatches == INCLUDE_FILES_IN_FILTER_PATTERN;
            }
            return includeExcludeMatches == EXCLUDE_FILES_IN_FILTER_PATTERN;
        }
    }
```
The main interface in the base class for the example above extends the stock Java ``FilenameFilter``
interface. There is a difference in what our derived classes must implement. Namely, subject to the
next defines, the code can decide whether to include or exclude the file matches based on whether the
filename filter matches the user specified pattern:
```java
static final boolean INCLUDE_FILES_IN_FILTER_PATTERN = FileChooserBuilder.INCLUDE_FILES_IN_FILTER_PATTERN;
static final boolean EXCLUDE_FILES_IN_FILTER_PATTERN = FileChooserBuilder.EXCLUDE_FILES_IN_FILTER_PATTERN;
```
Similarly, an overloaded sorting class that can be extended is sampled below:
```java
    public static class FileItemsSortFunc implements Comparator<File> {
        public File[] sortFileItemsList(File[] folderContentsList) {
            Arrays.sort(folderContentsList, this);
            return folderContentsList;
        }
        @Override
        public int compare(File f1, File f2) {
            // default is standard lexicographical ordering (override the compare functor base classes for customized sorting):
            return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
        }
    }
```
Here is an example of how to utilize these customized classes with the library's core
``FileChooserBuilder`` class instances:
```java
FileChooserBuilder fcConfig = new FileChooserBuilder();
fcConfig.setFilesListSortCompareFunction(FileFilter.FileItemsSortFunc);

// Some defaults for convenience:
fcConfig.filterByDefaultFileTypes(List<DefaultFileTypes> fileTypesList, boolean includeExcludeInList);
fcConfig.filterByMimeTypes(List<String> fileTypesList, boolean includeExcludeInList);
fcConfig.filterByRegex(String fileFilterPattern, boolean includeExcludeInList);
```

### Misc other useful utilities and customizations bundled with the main library

#### Displaying a visual linear bar style progress bar for slow directory loads

This functionality may be useful at some point for those willing to extend this code with 
custom external file providers, e.g., to read and recurse into directories on Dropbox or GitHub. 
I have a simple visual Toast-like display that can be updated and/or canceled in real time to 
let the user know that the directory is loading and that the client application is just "thinking" 
(as opposed to freezing with an inexplicable runtime error).

To invoke this progress bar display in realtime, consider calling the following 
[code examples](https://github.com/maxieds/AndroidFileChooserLight/blob/master/AndroidFilePickerLightLibrary/src/main/java/com/maxieds/androidfilepickerlightlibrary/DisplayUtils.java#L159):
```java
DisplayUtils.SetProgressBarMarkerTintColor(Activity activityInst, @ColorRes int colorTintResId)
DisplayUtils.DisplayProgressBar(Activity activityInst, String thingsName, int curPos, int totalPos);
DisplayUtils.EnableProgressBarDisplay(true);
// ... Then whenever the long process completes, kill the progress bar update callbacks with: ...
DisplayUtils.EnableProgressBarDisplay(false);
```
In principle, the status bar is useful when the underlying operation takes longer than, say 8-10 seconds to complete. 
The core of the progress bar is shown by periodically posting Toast messages with a custom layout ``View``.
