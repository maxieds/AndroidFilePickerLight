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

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class FileChooserActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private String LOGTAG = FileChooserActivity.class.getSimpleName();

    private static BasicFileProvider defaultFileProviderStaticInst = new BasicFileProvider();
    public static BasicFileProvider getFileProviderInstance() { return defaultFileProviderStaticInst; }

    private List<File> selectedFilePaths; // TODO: Need to store the UI FileType lists somewhere ...

    /**
     * Default handler for  all uncaught exceptions.
     */
    private void setUnhandledExceptionHandler() {
        final AppCompatActivity localActivityContext = this;
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread paramThread, Throwable paramExcpt) {
                // TODO: Handle returning with the necessary action code ...
                localActivityContext.finish();
                System.exit(-1);
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

        AndroidPermissionsHandler.obtainRequiredPermissions(this, ACTIVITY_REQUIRED_PERMISSIONS);
        AndroidPermissionsHandler.requestOptionalPermissions(this, ACTIVITY_OPTIONAL_PERMISSIONS);

        setContentView(R.layout.main_picker_activity_base_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR); // keep app from crashing when the screen rotates

        FilePickerBuilder fpConfig = (FilePickerBuilder) getIntent().getSerializableExtra(FilePickerBuilder.FILE_PICKER_BUILDER_EXTRA_DATA_KEY);
        // TODO: ...
        // Create layout ... (including setting defaults, set default theme, etc. ) ...
        // set an idle timeout to kill the activity if it hanges or the user ignores it for too long ...
        // Need fragments for loading files in the UI nav (see the forked library code) ...
        // need a RecyclerView interface ...

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
        return null;
    }

    public void postSelectedFilesActivityResult() {

        Intent filesResultIntent = getSelectedFilesActivityResultIntent();
        // TODO
        finish();
        System.exit(0);
        //System.exit(TODO:activityResultCode???);

    }

}
