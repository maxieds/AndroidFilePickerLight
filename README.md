# Android File Picker Light

## About the library 

A file and directory chooser widget for Android that focuses on presenting an easy to configure lightweight UI.
This library is intended to be a replacement for other picker libraries that 
works with the new **Android 11** file system and 
[storage management changes](https://developer.android.com/about/versions/11/privacy/storage). 
The source is made openly available as free software according to the 
[project license](https://github.com/maxieds/AndroidFilePickerLight/blob/main/LICENSE). 

### Feature set

Key features in the library include the following:
* Easy to configure theming and UI display settings including icons and color choices
* Simple actions and extendable Java interface to select and filter files/directories
* Allows client code to access many standard file system types on the Android device without 
  complicated procedures and permissions headaches inherited by the new Android 11 policy changes
* Exceptions and errors thrown at runtime extend the standard Java ``RuntimeException`` class for 
  ease of handling
  
### Screenshots of the library in action

TODO

## Including the library in an Android application

There are a couple of quickstart items covered in the sections below to handle before this
library can be included in the client Android application:
* Include the library using [Jitpack.io/GitHub]() 
  in the application *build.gradle* configuration.
* Update the project *AndroidManifest.xml* file to extend the documents provider, 
  request required permissions, and setup some helpful legacy file handling options for devices 
  targeting Android platforms with SDK < 11.
  
Examples of using the library to pick files and directories from client Java code is also 
included in the detailed documentation in the next section.

### Application build.gradle modifications

### Project manifest modifications

It is necessary to declare that the client application receive minimal permissions. 
We request the ``MANAGE_EXTERNAL_STORAGE`` permission for ease of 
handling of multiple file path types (internal/external storage, emulated storage, user home directories). 
The new [Android and Play Store policies](https://developer.android.com/training/data-storage/manage-all-files#enable-manage-external-storage-for-testing) 
for applications requesting this 
permission are clearly stated -- 
and be aware that this library has no malicious usages of this permission, through we cannot verify illicit 
permissions abuse from the client code once it is granted.
Moreover, for client applications that utilize this library for users to select files on the device, 
the Play Store review of this permission may reference the stock 
[library privacy policy](https://github.com/maxieds/AndroidFilePickerLight/blob/main/LibraryPrivacyPolicy.md) that 
explains why these permissions are requested, and what the library does with them in handling the 
file picker selection requests from the client application. 

Near the top of the project manifest file, append the following permissions-related 
statements:
```xml
    <!-- Core storage permissions required: -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:required="true" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:required="true" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" android:required="false" />

    <!-- A useful (non-mandatory, fairly innocuous) permission needed to use the DownloadManager -->
    <uses-permission android:name="android.permission.INTERNET" android:required="false" />
```
For applications targeting so-called legacy platforms, that is Android devices where the new 
Android 11 storage management options are not explicitly required, it is 
[recommended]() for compatibility sake by the Google developer docs 
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
        android:allowBackup="true"
        >
     <!-- Complete the internals of the application tag (activities, etc.) -->
</application>
```
TODO: Does the library require a DocumentsProvider or FileProvider definition yet ???

## Sample client Java source code

### Basic usage: Returning a file path selected by the user

This is a quick method to select a file and/or directory picked by the user:
```java
```

### Detailed list of non-display type options

The next options are available to configure the non-display type (e.g., properties of the 
file chooser that do not depend on how it looks) properties of the library. 
These can be set using the ``AndroidFilePickerLight.Builder`` class as follows:
```java
/* Set the action type return code (for use with onActivityResult): */

/* Select whether or not the user can pick a file, a directory, or either: */

/* Set whether multiple paths can be chosen, and set limits on how many if the 
 * chooser is configured to enable multiple path selection: 
 */

/* Define whether the chooser displays hidden files or not: */

/* Set a custom file and directory sort function: 
 * (Can prioritize directories versus files, or decide that certain types should be 
 *  displayed first in the file list -- sorted lexographically, or reverse lexographically)
 */

/* Set the current working directory, and/or limit the scope of the top level path up to 
 * which the user can navigate: 
 */

/* Enable/disable the default directory navigation buttons at the top of the chooser display: */

/* Default start path locations: */
// External/internal storage; User home directory; SDCard; Downloads/Pictures/DCIM/Screenshots/Camera/Video; 

/* Filter by file types, or exclude certain file types from being displayed: */

/* Set a timeout after which the display returns with an invalid path 
 * (should keep the client application from hanging on neglected chooser instances):
 */
// include whether to throw a certain exception type
// throw an invalid selection exception on error ??? 
// TODO: Set unhandled exception handler 

/* Set desired file path return types (String path, File, URI, etc.): */

/* 
 * -- See next sections below to configure theme and UI display options like colors, default strings, 
 *    and to setup custom default icons in the chooser display --
 */
```

### Extending file types for filtering and sorting purposes in the picker UI

### Handling runtime exceptions and extending the runtime exception class (for custom error handling)

#### List of default exception sub-types

```java
/* Basic interface methods to implement for subclasses: */
public interface AndroidFilePickerLightException extends RuntimeException  { 
     // constructors -- building a new descriptive exception from the usual causal data:
     public AndroidFilePickerLightException(Exception javaBaseExcpt);
     public AndroidFilePickerLightException(String errorMsg);
     public AndroidFilePickerLightException(String errorMsg, Exception javaBaseExcpt);
     protected void configureExceptionParams(int errorCode, String baseExcptDesc, boolean defaultIsError, 
                                             Object dataTypeT, ExceptionDataFieldFormatter dataFmtObjType);
     // standard-ish Exception class handling and methods:
     public String getExceptionMessage();
     public String[] getStackTrace();
     public void printStackTrace();
     public String toString();
     // communicating other information by exception:
     public boolean isError();
     public boolean getErrorCode();
     public Intent getAsIntent();
     // custom error messages and printing methods:
     public String getExceptionName();
     public String getExceptionBaseDesc();
     public String getErrorMessage();
     public String getErrorMessageFull();
     public String getInvokingSourceFile();
     public int getInvokingLineNumber();
     public String prettyPrintException(boolean verboseStackTrace);
     // obtaining file selection data passed by the exceptional instance:
     public boolean hasDataItems();
     public <DataTypeT extends Object> DataTypeT getDataSingle();
     public <DataTypeT extends Object> List<DataTypeT> getDataAsList();
     public int dataTypeItemsCount();
     public <DataTypeT extends Object> List<DataTypeT> getTypedDataAsList();
     // custom formatting and preparation of the returned data expected by the 
     // client application: 
     public interface ExceptionDataFieldFormatter {
          public <DataTypeT extends Object> List<DataTypeT> formatDataItems(List<File> fileItems);
          public <DataTypeT extends Object> List<DataTypeT> formatDataItems(List<String> fileItems);
          public <DataTypeT extends Object> List<DataTypeT> formatDataItems(List<URI> fileItems);
          public <DataTypeT extends Object> String toString(List<DataTypeT> lstView);
          public static <DataTypeT extends Object> byte[] toSerializedDataBuffer(List<DataTypeT> lstView);
          public static <DataTypeT extends Object> List<DataTypeT> recoverDataItemsList(byte[] serializedBufferData);
     }
     public static <DataTypeT extends Object> void setDataItemsFieldFormatterInterface(ExceptionDataFieldFormatter<DataTypeT> dataFormatter);
}
/* Some predefined options for exceptions that can be thrown by the chooser selection activity: */
public class GenericRuntimeErrorException extends AndroidFilePickerLightException { /* ... */ }
public class FileIOException extends AndroidFilePickerLightException { /* ... */ }
public class PermissionsErrorException extends AndroidFilePickerLightException { /* ... */ }
public class AbortedByTimeoutException extends AndroidFilePickerLightException { /* ... */ }
public class AbortedByUserActionException extends AndroidFilePickerLightException { /* ... */ }
public class CommunicateSelectionDataException extends AndroidFilePickerLightException { /* ... */ }
public class CommunicateNoDataException extends AndroidFilePickerLightException { /* ... */ }
public class InvalidInitialPathException extends AndroidFilePickerLightException { /* ... */ }
public class InvalidThemeResourceException extends AndroidFilePickerLightException { /* ... */ }
```

#### Checking whether the returned file path is valid



#### Determine whether a timeout occured to close the picker

#### Query whether items in the list of paths are directories versus files

### Configuring the client theme and UI look-and-feel properties

#### Basic example (quickstart)



#### Full example (detailed usage of all theming/UI display options)

Note that with the exception of a few predefined themes that are included as 
default resources within the library packaging, all UI display related 
resources passed to customize the look-and-feel of the file picker display 
need to be resolved explicitly by the calling client activity. 
This means, in general, if the client passes a resource identifier (integer type) 
as the value for one of these configuration items, there is going to be an error 
where the library does not know where to find the resource. 
Some examples of converting the resources in the application ``/res/*`` directory 
from ID names to a format the library can use are suggested in the next code 
snippets:
```java
/* Assumptions about the activity (not the only, option, just for clarity): */
public class MyMainActivity extends AppCompatActivity { /* ... */ }
MyMainActivity myRunningActivityInst = MyMainActivity.getInstance();

/* Pass a new icon Drawable to replace the default setting: */
Drawable newFolderIcon = myRunningActivityInst.getResources().getDrawable(R.id.custom_folder_icon_24x24, myRunningActivityInst.getTheme());

/* Select a new custom color and get its hex-coded value: */
int newBackgroundColor = myRunningActivityInst.getResources().getColor(R.color.custom_picker_bgcolor, myRunningActivityInst.getTheme());

/* Set a new text value from a string resource: */
String newChoosetTitleText = myRunningActivityInst.getString(R.string.custom_chooser_title_msg);
```
Now that we have the scheme for passing resources to the library to skin/color/custom theme its UI down, 
here is the full listing and type specs for what attributes can actually be changed and reset on-the-fly:
```java
/* Get a handle to the configuration object for the file picker UI: */
AndroidFilePickerLight.UIDisplayConfig customUIDisplayCfg = new AndroidFilePickerLight.UIDisplayConfig.getNewInstance();
// ... Later, after setting its properties, set the active file picker's display configuration with: ... 
AndroidFilePickerLight.Builder filePickerBuilder = new AndroidFilePickerLight.Builder.getDefaultInstance();
filePickerBuilder.setDisplayUIConfig().runUserFilePicker();

/* Configure the Strings and text displays with custom messages or text values: */
customUIDisplayCfg.setPickerTitleText("[String] Select a text file ...");
customUIDisplayCfg.setNavigationBarPrefixText("[String] Navigation Options: ");
customUIDisplayCfg.setDoneActionButtonText("[String] !! Done with selection");
customUIDisplayCfg.setCancelActionButtonText("[String] X Cancel -- Pick nothing");

/* Select a base built-in color theme to start with: */
customUIDisplayCfg.setDefaultTheme(AndroidFilePickerLight.UIDisplayConfig.BaseThemeLight);
customUIDisplayCfg.setDefaultTheme(AndroidFilePickerLight.UIDisplayConfig.BaseThemeDark);
customUIDisplayCfg.setDefaultTheme(AndroidFilePickerLight.UIDisplayConfig.BaseThemeDefault);

/* Define custom colors and (optionally) background gradient types: */
customUIDisplayCfg.setBaseBackgroundColor(int hexColor);
customUIDisplayCfg.setBaseForgroundTextColor(int hexColor);
customUIDisplayCfg.setBaseForgroundAccentColor(int hexColor);
customUIDisplayCfg.useGradientBasedDisplays(boolean enableGradientBGInUIDisplay);

public enum AndroidFilePickerLight.UIDisplayConfig.GradientMethodSpec {
     GRADIENT_METHOD_SWEEP,
     GRADIENT_METHOD_LINEAR,
     GRADIENT_METHOD_RADIAL,
};
public enum AndroidFilePickerLight.UIDisplayConfig.GradientTypeSpec {
     GRADIENT_FILL_TYPE_BL_TR,
     GRADIENT_FILL_TYPE_BOTTOM_TOP,
     GRADIENT_FILL_TYPE_BR_TL,
     GRADIENT_FILL_TYPE_LEFT_RIGHT,
     GRADIENT_FILL_TYPE_RIGHT_LEFT,
     GRADIENT_FILL_TYPE_TL_BR,
     GRADIENT_FILL_TYPE_TOP_BOTTOM,
     GRADIENT_FILL_TYPE_TR_BL,
};
public enum AndroidFilePickerLight.UIDisplayConfig.BorderStyleSpec {
     BORDER_STYLE_SOLID,
     BORDER_STYLE_DASHED,
     BORDER_STYLE_DASHED_LONG,
     BORDER_STYLE_DASHED_SHORT,
     BORDER_STYLE_NONE,
};
public enum AndroidFilePickerLight.UIDisplayConfig.NamedGradientColorThemes {
     NAMED_COLOR_SCHEME_TURQUOISE,
     NAMED_COLOR_SCHEME_YELLOW_TO_BLUE,
     NAMED_COLOR_SCHEME_GREEN_YELLOW_GREEN,
     NAMED_COLOR_SCHEME_METAL_STREAK_BILINEAR,
     NAMED_COLOR_SCHEME_SILVER_BALLS,
     NAMED_COLOR_SCHEME_EVENING_SKYLINE,
     NAMED_COLOR_SCHEME_RAINBOW_STREAK,
};

customUIDisplayCfg.setGradientDisplayProperties(int[] gradientColorsList, GradientMethodSpec gradMethodType, BorderStyleSpec borderStyle, float roundedRectSetting = 0.0f);
customUIDisplayCfg.setGradientDisplayProperties(NamedGradientColorThemes presetColorTheme, GradientMethodSpec gradMethodType, BorderStyleSpec borderStyle, float roundedRectSetting = 0.0f);

/* Set default icons for files/folders and the navigation UI folder button shortcuts for 
 * common standard paths: 
 */
public enum AndroidFilePickerLight.UIDisplayConfig.NamedDisplayIcons {
     DEFAULT_FILE_ICON,
     DEFAULT_HIDDEN_FILE_ICON,
     DEFAULT_FOLDER_ICON,
     NAVBTN_HDSDCARD_ICON,
     NAVBTN_PICTURES_ICON,
     NAVBTN_CAMERA_ICON,
     NAVBTN_SCREENSHOTS_ICON,
     NAVBTN_USER_HOME_ICON,
     NAVBTN_DOWNLOADS_FOLDER_ICON,
     NAVBTN_MEDIA_FOLDER_ICON,
};

customUIDisplayCfg.setDefaultChooserIcon(Drawable replacementIcon, NamedDisplayIcons whichIconToReplace);


```

### Other useful utilities and examples

#### Extending the inclusion/exclusion mechanism of files by type

#### Sending the selected file to another application (or emailing the file results)

#### Extending the library as a picker for more complicated media file types

#### Misc utility functions

## Documentation and approach to handling files internally within the picker library

The next links provide some context and developer reference docs for the internal schemes used by the 
library to implement listing files on the local device file system. 
* [Android Storage Access Framework (SAF)](https://developer.android.com/guide/topics/providers/document-provider)
* [Accessing app-specific files (developer docs)](https://developer.android.com/training/data-storage/app-specific)
* [Access documents and other files from shared storage (developer docs)](https://developer.android.com/training/data-storage/shared/documents-files)
* [Data and file storage overview (developer docs)](https://developer.android.com/training/data-storage)
* [Overview of shared storage (developer docs)](https://developer.android.com/training/data-storage/shared)
* [Requesting a shared file (developer docs)](https://developer.android.com/training/secure-file-sharing/request-file.html)
We have also made use of some of the functionality provided in Kotlin and/or Java code from the 
following alternative Android file chooser libraries:
* [AndroidFilePicker library](https://github.com/rosuH/AndroidFilePicker -- Kotlin based implementation by **@rosuH**.
* **TODO** -- LINKS to other good projects used as a reference point ... 
Much of the reason for re-writing the Kotlin-based scheme in 
[my initial fork](https://github.com/maxieds/AndroidFilePicker) 
the first library above is 
centered on needing to re-theme and customize the UI, and also resolve some consistency issues 
when selecting only directory based paths. This library also deals with the timely issue of the 
new storage and access permissions that are popping up as Android devices being to migrate to 
SDK >= 11 (codename *Android* **Q**).

### Rationale and comparison to past mechanisms for file selection 

#### Tentative explanation for why file selection on Android is so difficult by default 

I have the **opinion** that the overly complex interface for a files/documents provider 
is to excessively generalize something that is a priori a simple operation for defining files 
for big-time Google API clients, e.g., Dropbox and media providers. 
This API functionality was as such introduced to generalize a way of listing out files on non-hardware 
based filesystems or media platforms, for example to retrieve files oragnized on an external 
internet provider, or stream in content from a large media content provider. 
As this complexity is not really needed for lightweight applications that just need a quick, 
bare bones way to select a standard issue (e.g., text or image or PDF documentt) file off the Android device, 
the design decision for this library is to focus on providing as minimal an interface, 
and to pull in only the smallest amounts of extra components, as are needed for all 
but large scale applications -- and those big content providers, mind you, will be very much apt to 
write their own file picker displays and theme the UI their way. 

N.b., there may be some good initiative type 
security related permissions restrictions at play too, but an 
intuition on this suggests that this explicit not-niceness the be for core file system based 
operations on any recent Android would have to be motivated by the Google/Android 
developer folks typically deferring to largest common denominator, rather than say, 
the convenience of smaller order Android Application developers that need simplicity for 
basic access ... :thumbsdown:

#### Historical procedures for file selection

Prior to approximately Android 8-10+, the scheme for 
picking files was limited to launching a new picker 
activity instance, looping and waiting for a Java ``RuntimeException`` containing the 
file selection data to signal the procedure had completed, and handling the termination 
of the first activity using the main activity's overriden ``onActivityResult`` function. 
For example, this may have worked a long time ago:
```java

```
Along the way and over time things became more complex with reading out the actual 
file path on disk from a URI type for the same reasons I surmise the file provider 
type complexity was added: namely, to provide access to non-standard file types and 
locations. Given the right ``READ|WRITE_EXTERNAL_STORAGE`` permissions, and by having the 
library and client code cooperate on sharing the URI read permissions still allowed for 
things to work easily enough prior to Android 11:
```java

```
With the new changes at and after Android 11, the mechanism for reading and obtaining 
file path locations and other file attribute datum have required a new interface 
(see the library sources). In particular, former approaches to this by returning a 
stock Java ``File`` type no longer work reliably with the most recent Android API 
boxen. Determining whether a file path is a directory, for example, or creating 
a new file given its path, when the path references something like 
``//storage/emulated/0/myFilePath.txt`` have been outcast by the Android police. 
What we end up with is the hodgepodge of ways to select/read/write files that are 
wrapped by this current library implementation. 