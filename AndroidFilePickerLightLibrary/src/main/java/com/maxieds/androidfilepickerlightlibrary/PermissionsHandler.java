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
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

import java.util.jar.Manifest;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

public class PermissionsHandler {

    protected static boolean hasAccessPermission(Activity activityCtx, String permName) {
        return ContextCompat.checkSelfPermission(activityCtx, permName) == PackageManager.PERMISSION_GRANTED;
    }

    public static final int REQUEST_REQUIRED_PERMISSIONS_CODE = 0;
    public static final int REQUEST_OPTIONAL_PERMISSIONS_CODE = 1;
    public static final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    public static boolean ABORT_ON_DENIED_PERMISSION = false;

    private static Map<String, String> REQUIRED_PERMISSIONS_LOOKUP_RATIONALE_MAP;
    static {
        REQUIRED_PERMISSIONS_LOOKUP_RATIONALE_MAP.put(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                R.string.permReadExternalRationale
        );
        REQUIRED_PERMISSIONS_LOOKUP_RATIONALE_MAP.put(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                R.string.permWriteExternalRationale
        );
        REQUIRED_PERMISSIONS_LOOKUP_RATIONALE_MAP.put(
                Manifest.permission.ACCESS_MEDIA_LOCATION,
                R.string.permAccessMediaLocRationale
        );
        REQUIRED_PERMISSIONS_LOOKUP_RATIONALE_MAP.put(
                Manifest.permission.INTERNET,
                R.string.permInternetRationale
        );
    }
    public static String lookupPermissionRationale(@NotNull String permID) {
        String permRationaleLookupResult = REQUIRED_PERMISSIONS_LOOKUP_RATIONALE_MAP.get(permID);
        if (permID == null) {
            return String.format(R.string.permOptionalRationaleFormat, permID);
        }
        return permRationaleLookupResult;
    }

    public static boolean obtainLocalPermission(@NotNull Activity activityCtx, String whichPerm) {
        int hasPerm = activityCtx.checkSelfPermission(whichPerm);
        int hasPermCompat = ContextCompat.checkSelfPermission(activityCtx, whichPerm)
        if (hasPerm != PackageManager.PERMISSION_GRANTED ||
            hasPermCompat == PackageManager.PERMISSION_DENIED) {
            if (activityCtx.shouldShowRequestPermissionRationale()) {
                String permRationale = lookupPermissionRationale(whichPerm);
                displayToastMessageShort(activityCtx, permRationale);
            }
            String[] reqPerms = new String[] { whichPerm };
            activityCtx.requestPermissions(reqPerms, PermissionsHandler.REQUEST_CODE_ASK_PERMISSIONS);
            return false;
        }
        return true;
    }
    public static boolean obtainRequiredPermissions(Activity activityCtx, String[] permsList) {
        if(android.os.Build.VERSION.SDK_INT >= 23) {
             activityCtx.requestPermissions(permsList, REQUEST_REQUIRED_PERMISSIONS_CODE);
             EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(activityCtx, REQUEST_REQUIRED_PERMISSIONS_CODE, permsList)
                            .setRationale(R.string.grantPermsDialogRationaleText)
                            .setPositiveButtonText(R.string.grantPermsDialogOkBtnText)
                            .setNegativeButtonText(R.string.grantPermsDialogCancelBtnText)
                            .setTheme(R.style.LibraryDefaultTheme)
                            .build());
        }
        for(int pidx = 0; pidx < permsList.length; pidx++) {
            if(!hasAccessPermission(activityCtx, permsList[pidx])) {
                if (ABORT_ON_DENIED_PERMISSION) {
                    throw new FileChooserException.PermissionsErrorException();
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean requestOptionalPermissions(Activity activityCtx, String[] permsList) {
        if(android.os.Build.VERSION.SDK_INT >= 23) {
            activityCtx.requestPermissions(permsList, REQUEST_OPTIONAL_PERMISSIONS_CODE);
        }
        for(int pidx = 0; pidx < permsList.length; pidx++) {
            if(!hasAccessPermission(activityCtx, permsList[pidx])) {
                return false;
            }
        }
        return true;
    }

}
