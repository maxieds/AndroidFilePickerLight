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

public class PermissionsHandler {

    protected static boolean hasAccessPermission(Activity activityCtx, String permName) {
        return ContextCompat.checkSelfPermission(activityCtx, permName) == PackageManager.PERMISSION_GRANTED;
    }

    public static final int REQUEST_REQUIRED_PERMISSIONS_CODE = 0;
    public static final int REQUEST_OPTIONAL_PERMISSIONS_CODE = 1;

    public static boolean obtainRequiredPermissions(Activity activityCtx, String[] permsList) {
        if(android.os.Build.VERSION.SDK_INT >= 23) {
             activityCtx.requestPermissions(permsList, REQUEST_REQUIRED_PERMISSIONS_CODE);
        }
        for(int pidx = 0; pidx < permsList.length; pidx++) {
            if(!hasAccessPermission(activityCtx, permsList[pidx])) {
                //throw new FileChooserException.PermissionsErrorException();
                return false;
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
