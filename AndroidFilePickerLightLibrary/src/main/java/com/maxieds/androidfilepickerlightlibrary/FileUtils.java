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

import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;

public class FileUtils {

    private static final String FILE_PATH_SEPARATOR = "/";

    // URI: read path data (isDirectory, isMediaFile, isBlobFile, etc.) ...
    // Construct some helper methods to send the result: in general, by email, over SMS, etc. ...

    public static boolean validFileBaseName(String filePath) {
        return filePath != null
                && !filePath.equals("")
                && !filePath.contains(FILE_PATH_SEPARATOR)
                && !filePath.equals(".")
                && !filePath.equals("..");
    }

    public static boolean isHiddenFile(String filePath) {
        return validFileBaseName(filePath) && filePath.length() >= 1 && filePath.charAt(0) == '.';
    }

    public static String getFileExtension(String filePath) {
        if(filePath == null) {
            return "";
        }
        else if(!filePath.contains(".")) {
            return "";
        }
        int extSepPosIndex = filePath.lastIndexOf('.');
        return filePath.substring(extSepPosIndex);
    }

    public static String getFileMimeType(String filePath) {
        if(getFileExtension(filePath).equals("")) {
            return "*/*";
        }
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(getFileExtension(filePath).toLowerCase());
    }

    public static String getFilePosixPermissionsString(File fileOnDisk) {
        try {
            return Files.getPosixFilePermissions(fileOnDisk.toPath()).toString();
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return "";
        }
    }

    public static String filePermsStringToShortChmodStyleCode(String rwxDashPerms) {
        if(rwxDashPerms.length() < 9) {
            return "";
        }
        String[] rwxTriplet = rwxDashPerms.split("...(?!$)");
        if(rwxTriplet.length != 3) {
            return "";
        }
        String chmodStylePermsCode = "0";
        for(String rwxTriple : rwxTriplet) {
            rwxTriple = rwxTriple.toLowerCase(Locale.getDefault());
            int octalBits[] = new int[] {
                    rwxTriple.charAt(0) == 'r' ? 4 : 0,
                    rwxTriple.charAt(1) == 'w' ? 2 : 0,
                    rwxTriple.charAt(2) == 'x' ? 1 : 0
            };
            int octalCode = octalBits[0] + octalBits[1] + octalBits[2];
            chmodStylePermsCode += String.format(Locale.getDefault(), "%d", octalCode);
        }
        return chmodStylePermsCode;
    }

    public static String getFileSizeString(File fileOnDisk) {
        long fileSizeBytes = fileOnDisk.getTotalSpace();
        if(fileSizeBytes < 1024) {
            return String.format(Locale.getDefault(), "%ldB", fileSizeBytes);
        }
        long fileSizeKB = fileSizeBytes / 1024;
        if(fileSizeKB < 1024) {
            return String.format(Locale.getDefault(), "%ldK", fileSizeKB);
        }
        long fileSizeMB = fileSizeKB / 1024;
        if(fileSizeMB < 1024) {
            return String.format(Locale.getDefault(), "%ldM", fileSizeMB);
        }
        long fileSizeGB = fileSizeMB / 1024;
        if(fileSizeGB < 1024) {
            return String.format(Locale.getDefault(), "%ldG", fileSizeGB);
        }
        return String.format(Locale.getDefault(), "%ldT+", fileSizeGB / 1024);
    }

}
