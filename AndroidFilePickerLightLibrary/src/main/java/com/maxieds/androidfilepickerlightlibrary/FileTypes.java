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

import android.graphics.drawable.Drawable;

import java.io.File;

public class FileTypes {

    /*
    DIRECTORY(R.drawable.ic_folder_48dp, R.string.type_directory),
    DOCUMENT(R.drawable.ic_document_box, R.string.type_document),
    CERTIFICATE(R.drawable.ic_certificate_box, R.string.type_certificate, "cer", "der", "pfx", "p12", "arm", "pem"),
    DRAWING(R.drawable.ic_drawing_box, R.string.type_drawing, "ai", "cdr", "dfx", "eps", "svg", "stl", "wmf", "emf", "art", "xar"),
    EXCEL(R.drawable.ic_excel_box, R.string.type_excel, "xls", "xlk", "xlsb", "xlsm", "xlsx", "xlr", "xltm", "xlw", "numbers", "ods", "ots"),
    IMAGE(R.drawable.ic_image_box, R.string.type_image, "bmp", "gif", "ico", "jpeg", "jpg", "pcx", "png", "psd", "tga", "tiff", "tif", "xcf"),
    MUSIC(R.drawable.ic_music_box, R.string.type_music, "aiff", "aif", "wav", "flac", "m4a", "wma", "amr", "mp2", "mp3", "wma", "aac", "mid", "m3u"),
    VIDEO(R.drawable.ic_video_box, R.string.type_video, "avi", "mov", "wmv", "mkv", "3gp", "f4v", "flv", "mp4", "mpeg", "webm"),
    PDF(R.drawable.ic_pdf_box, R.string.type_pdf, "pdf"),
    POWER_POINT(R.drawable.ic_powerpoint_box, R.string.type_power_point, "pptx", "keynote", "ppt", "pps", "pot", "odp", "otp"),
    WORD(R.drawable.ic_word_box, R.string.type_word, "doc", "docm", "docx", "dot", "mcw", "rtf", "pages", "odt", "ott"),
    ARCHIVE(R.drawable.ic_zip_box, R.string.type_archive, "cab", "7z", "alz", "arj", "bzip2", "bz2", "dmg", "gzip", "gz", "jar", "lz", "lzip", "lzma", "zip", "rar", "tar", "tgz"),
    APK(R.drawable.ic_apk_box, R.string.type_apk, "apk");
    */

    public enum DefaultFileTypes {
        PLAINTEXT_FILE_TYPE,
        BINARY_FILE_TYPE,
        DOCUMENTS_FILE_TYPE,
        IMAGE_FILE_TYPE,
        MEDIA_FILE_TYPE,
        FOLDER_FILE_TYPE,
        HIDDEN_FILE_TYPE,
        COMPRESSED_ARCHIVE_FILE_TYPE,
        APK_FILE_TYPE,
        CUSTOM_FILE_TYPE,
        UNKNOWN_FILE_TYPE
    }

    public static class FileType {

        private FileType parentFolder;
        private File fileOnDisk;
        private boolean isChecked;

        public FileType(File fileOnDisk, FileType parentFolder) {
            this.parentFolder = parentFolder;
            this.fileOnDisk = fileOnDisk;
            this.isChecked = false;
        }

        public String getAbsolutePath() {
            return fileOnDisk.getAbsolutePath();
        }

        public String getBaseName() {
            return fileOnDisk.getName();
        }

        public boolean isDirectory() {
            return fileOnDisk.isDirectory();
        }

        public boolean isHidden() {
            return fileOnDisk.isHidden();
        }

        public String getExtension() {
            return FileUtils.getFileExtension(fileOnDisk.getPath());
        }

        public String getMimeType() {
            return FileUtils.getFileMimeType(fileOnDisk.getPath());
        }

        public String getPosixPermissions() {
            return FileUtils.getFilePosixPermissionsString(fileOnDisk);
        }

        public String getFileSizeString() {
            return FileUtils.getFileSizeString(fileOnDisk);
        }

        //public boolean isLocalFile() { return false; }
        //public boolean isMediaFile() { return false; }
        //public boolean isBlobFile() { return false; }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean enable) {
            isChecked = enable;
        }

        // TODO: Handle adding a reference to the UI display data and widgets for this file ...

        public void setClickableUI(boolean enableClicks) {}

    }

}
