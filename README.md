# Android File Picker Light Library

<!--<img src="https://raw.githubusercontent.com/maxieds/AndroidFilePickerLight/master/Screenshots/LibraryProfileIconPlayStore.png" width="750" height="350" />-->

<hr /><hr />

<img src="https://jitpack.io/v/maxieds/AndroidFilePickerLight.svg" /><img src="https://img.shields.io/badge/NOTE%3A-Project%20is%20a%20work%20in%20progress-orange" /><img src="https://img.shields.io/badge/API%2029%2B-Tested%20on%20Android%2010-yellowgreen" /><img src="https://badges.frapsoft.com/os/gpl/gpl.svg?v=103" /> 


<img src="https://forthebadge.com/images/badges/made-with-java.svg" /><img src="https://forthebadge.com/images/badges/powered-by-coffee.svg" /><img src="https://forthebadge.com/images/badges/built-for-android.svg" />

<img src="https://badges.frapsoft.com/os/v2/open-source-175x29.png?v=103" />

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
* Easy to configure theming and UI display settings including icons and color choices
* Simple actions and extendable Java interface to select and filter files/directories
* Allows client code to access many standard file system types on the Android device without 
  complicated procedures and permissions headaches inherited by the new Android 11 policy changes
* Exceptions and errors thrown at runtime extend the standard Java ``RuntimeException`` class for 
  ease of handling. Many of these exceptions are just wrappers around data returned by a newly 
  spawned file picker activity and do not necessarily indicate errors in the file selection process.
  
### Screenshots of the library in action (TODO)

<img src="https://raw.githubusercontent.com/maxieds/AndroidFileChooserLight/master/Screenshots/WorkingUI-Screenshot_20201112-052224.png" width="250" /><img src="" width="250" /><img src="" width="250" />

<img src="" width="250" /><img src="" width="250" /><img src="" width="250" />

## Including the library in an Android application

There are a couple of quickstart items covered in the sections below to handle before this
library can be included in the client Android application:
* Include the library using [Jitpack.io/GitHub](https://jitpack.io/#maxieds/AndroidFilePickerLight) 
  in the application *build.gradle* configuration.
* Update the project *AndroidManifest.xml* file to extend the documents provider, 
  request required permissions, and setup some helpful legacy file handling options for devices 
  targeting Android platforms with SDK < 11.
  
Examples of using the library to pick files and directories from client Java code is also 
included in the detailed documentation in the next section.

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
    implementation 'com.github.maxieds:AndroidFilePickerLight:-SNAPSHOT'
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

Near the top of the project manifest file, append the following permissions-related 
statements:
```xml
    <!-- Core storage permissions required: -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:required="true" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:required="true" />
    <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" android:required="false" />
    <uses-permission android:name="android.permission.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION" android:required="false" />
    <uses-permission android:name="android.permission.INTERNET" android:required="false" />
```
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

## Sample client Java source code

The next examples document basic, advanced, and custom uses of the library in client code. 

### Basic usage: Returning a file path selected by the user

This is a quick method to select a file and/or directory picked by the user:
```java
/* TODO */
```

### Detailed list of non-display type options

The next options are available to configure the non-display type (e.g., properties of the 
file chooser that do not depend on how it looks) properties of the library. 
These can be set using the ``AndroidFilePickerLight.Builder`` class as follows:
```java
/* TODO */
```

### Extending file types for filtering and sorting purposes in the picker UI

### Handling runtime exceptions and extending the runtime exception class (for custom error handling)

#### List of default exception sub-types

```java
/* Basic interface methods to implement for subclasses: */
public interface AndroidFilePickerLightException extends RuntimeException  {}

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

#### Determine whether an idle timeout occurred to close the picker

#### Query whether items in the list of paths are directories versus files


### Configuring the client theme and UI look-and-feel properties

#### Basic example (quickstart guide to using the file picker library)





#### Full example (detailed usage of the current custom theme/UI display options)

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
/* TODO: Need to add this documentation ... */
```

### Misc other useful utilities and customizations bundled with the main library

### Displaying a visual linear bar style progress bar for slow directory loads (TODO)

This functionality may be useful at some point for those willing to extend this code with 
custom external file providers, e.g., to read and recurse into directories on Dropbox or GitHub. 
I have a simple visual Toast-like display that can be updated and/or canceled in real time to 
let the user know that the directory is loading and that the client application is just "thinking" 
(as opposed to freezing with a runtime error). The stock progress bar looks something like the following screenshot: 

<img src="https://raw.githubusercontent.com/maxieds/AndroidFilePickerLight/master/Screenshots/SampleApplicationDemo-ProgressBarDisplay.png" width="250" />

To invoke this progress bar display in realtime, consider calling the following code examples:
```java
DisplayUtils.DisplayProgressBar(String waitingOnProcessLabel, int curPos, int totalPos);
DisplayUtils.EnableProgressBarDisplay(true);
// ... Then whenever the long process completes, kill the progress bar update callbacks with: ...
DisplayUtils.EnableProgressBarDisplay(true);
```
In principle, the status bar is useful when the underlying operation takes longer than, say 8-10 seconds to complete. 
This code is modified from a status timer to keep the user informed while scanning for a long duration read of 
NFC tags on Android (see [the MFCToolLibrary](https://github.com/maxieds/MifareClassicToolLibrary) and 
its demo application). The core of the progress bar is 
shown by periodically posting Toast messages with a custom layout ``View``. Please post a new issue message 
if anyone using this library in their own application finds this useful, or amusing too.

#### Overriding the default file and directory sorting (TODO)

An example of how to do this is already seen by glancing through the sources. 
In particular, we define an extendable base class as follows: 
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
Subclasses that override the method above are free to sort the file 
contents in the order that they are best displayed in the client application. 
Some examples would be to override the default of placing directories at the top of the 
files list, to perform a case-insensitive lexicographical sort (like happens on many Linux), 
or otherwise to prioritize the file rankings where the most importantly valued presentations 
are shown first. 

Note that in the source the custom objects to filter (match, then include/exclude) the full 
directory listings, and then sort those files that remain are applied together. See, for example, 
the next code:
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

#### Extending the inclusion/exclusion mechanism of files by type

The specification (by Java interface and a few utility derived instances) are found in the 
library source file ``FileFilter.java``. The bulk of the interface is reproduced as 
follows:
```java
    public interface FileFilterInterface {

        static final boolean INCLUDE_FILES_IN_FILTER_PATTERN = FilePickerBuilder.INCLUDE_FILES_IN_FILTER_PATTERN;
        static final boolean EXCLUDE_FILES_IN_FILTER_PATTERN = FilePickerBuilder.EXCLUDE_FILES_IN_FILTER_PATTERN;

        void    setIncludeExcludeMatchesOption(boolean includeExcludeParam);
        boolean includeFileInSearchResults(FileTypes.FileType fileItem);
        boolean fileMatchesFilter(FileTypes.FileType fileItem);

    }
```
