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

import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class FileTypes {

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

    public static class DirectoryResultContext {

        public static Stack<DirectoryResultContext> pathHistoryStack = new Stack<FileTypes.DirectoryResultContext>();

        private MatrixCursor initMatrixCursorListing;
        private List<FileType> directoryContentsList;
        private String activeCWDAbsPath;
        private String activeDocId;

        public DirectoryResultContext(MatrixCursor mcResult, MatrixCursor parentDirCtx) {
            initMatrixCursorListing = mcResult;
            computeDirectoryContents();
            if(parentDirCtx != null) {
                BasicFileProvider fpInst = BasicFileProvider.getInstance();
                activeCWDAbsPath = fpInst.getAbsPathAtCurrentRow(parentDirCtx);
                activeDocId = mcResult.getString(BasicFileProvider.ROOT_PROJ_DOCID_COLUMN_INDEX);
            }
        }

        public List<FileType> getWorkingDirectoryContents() {
            return directoryContentsList;
        }

        public void computeDirectoryContents() {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            MatrixCursor mcResult = initMatrixCursorListing;
            mcResult.moveToFirst();
            List<FileType> filesDataList = new ArrayList<FileType>();
            for(int mcRowIdx = 0; mcRowIdx < mcResult.getCount(); mcRowIdx++) {
                MatrixCursor mcRow = mcResult;
                File fileOnDisk = fpInst.getFileAtCurrentRow(mcResult);
                FileType nextFileItem = new FileType(fileOnDisk, this);
                filesDataList.add(nextFileItem);
                mcResult.moveToNext();
            }
            mcResult.moveToFirst();
            directoryContentsList = filesDataList;
        }

        public DirectoryResultContext loadNextFolderAtIndex(int posIndex) {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            directoryContentsList.clear();
            pathHistoryStack.push(this);
            MatrixCursor nextDirCursor = null;
            try {
                nextDirCursor = (MatrixCursor) fpInst.queryChildDocuments(activeDocId, BasicFileProvider.DEFAULT_DOCUMENT_PROJECTION, "");
            } catch(Exception ioe) {
                ioe.printStackTrace();
                pathHistoryStack.pop();
                return null;
            }
            return new DirectoryResultContext(nextDirCursor, initMatrixCursorListing);
        }

        public static DirectoryResultContext probeAtCursoryFolderQuery(FileChooserBuilder.BaseFolderPathType baseFolderChoice) {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            fpInst.selectBaseDirectoryByType(baseFolderChoice);
            try {
                MatrixCursor cursoryProbe = (MatrixCursor) fpInst.queryRoots(BasicFileProvider.DEFAULT_ROOT_PROJECTION);
                return new DirectoryResultContext(cursoryProbe, null);
            } catch(IOException ioe) {
                ioe.printStackTrace();
                return null;
            }
        }

    }

    public static class FileType {

        private DirectoryResultContext parentFolder;
        private File fileOnDisk;
        private boolean isChecked;
        private Drawable fileTypeIcon;
        private View fileItemLayoutContainer;

        public FileType(File fileOnDisk, DirectoryResultContext parentFolder) {
            this.parentFolder = parentFolder;
            this.fileOnDisk = fileOnDisk;
            this.isChecked = false;
            this.fileItemLayoutContainer = null;
        }

        public void setFileTypeIcon(Drawable fileIcon) {
            fileTypeIcon = fileIcon;
        }

        public Drawable getFileTypeIcon() {
            return fileTypeIcon;
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

        public String getChmodStylePermissions() {
            return FileUtils.filePermsStringToShortChmodStyleCode(FileUtils.getFilePosixPermissionsString(fileOnDisk));
        }

        public String getFileSizeString() {
            return FileUtils.getFileSizeString(fileOnDisk);
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean enable) {
            isChecked = enable;
        }

        public View getLayoutContainer() {
            return fileItemLayoutContainer;
        }

        public void setLayoutContainer(View fileItemLayoutContainer) {
            this.fileItemLayoutContainer = fileItemLayoutContainer;
        }

    }

    public static class FileItemsListSortFunc {
        public static List<FileType> sortFileItemsList(List<FileType> fileItemsList) {
            // default is standard lexicographical ordering (override in base classes for customized sorting):
            Collections.sort(fileItemsList, (fi1, fi2) -> { return fi1.getAbsolutePath().compareTo(fi2.getAbsolutePath()); });
            return fileItemsList;
        }
    }

}
