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
import android.icu.util.LocaleData;
import android.icu.util.Measure;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class DisplayTypes {

    private static String LOGTAG = DisplayTypes.class.getSimpleName();

    public static class DirectoryResultContext {

        private static String LOGTAG = DirectoryResultContext.class.getSimpleName();

        private MatrixCursor initMatrixCursorListing;
        private List<FileType> directoryContentsList;
        private String parentDocId;
        private String activeCWDAbsPath;

        public DirectoryResultContext(MatrixCursor mcResult, String parentFolderDocId, String parentFolderAbsPath) {
            directoryContentsList = new ArrayList<FileType>();
            parentDocId = parentFolderDocId;
            initMatrixCursorListing = mcResult;
            mcResult.moveToFirst();
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            activeCWDAbsPath = parentFolderAbsPath;
            Log.i(LOGTAG, String.format(Locale.getDefault(), "Initializing new folder at path: \"%s\" ... ", activeCWDAbsPath));
        }

        public MatrixCursor getInitialMatrixCursor() { return initMatrixCursorListing; }

        public List<FileType> getWorkingDirectoryContents() { return directoryContentsList; }

        public void setNextDirectoryContents(List<FileType> nextFolderFiles) { directoryContentsList = nextFolderFiles; }

        public void computeDirectoryContents(int startIndexPos, int maxIndexPos,
                                             int trimFromFrontCount, int trimFromBackCount, int newItemsCount,
                                             boolean updateGlobalIndices) {
            Log.i(LOGTAG, String.format(Locale.getDefault(), "STARTING: Computing dir contents [%d, %d] -- %s", startIndexPos, maxIndexPos, activeCWDAbsPath));
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            if(fpInst == null) {
                return;
            }
            fpInst.setFilesStartIndex(startIndexPos);
            int initStartIndexPos = startIndexPos;
            try {
                fpInst.noUpdateQueryFilesList(); // save some time processing if we haven't recently loaded a new folder to process
                String parentDocsId = parentDocId;
                initMatrixCursorListing = (MatrixCursor) fpInst.queryChildDocuments(parentDocsId, BasicFileProvider.DEFAULT_DOCUMENT_PROJECTION, "");
                int numItemsRequested = maxIndexPos + 1 - startIndexPos;
                maxIndexPos = maxIndexPos - startIndexPos;
                startIndexPos = 0;
                if(startIndexPos >= getInitialMatrixCursor().getCount() || maxIndexPos < startIndexPos) {
                    Log.e(LOGTAG, String.format("RETURNING cursor positions out of range %d / [%d, %d] ... ", getInitialMatrixCursor().getCount(), startIndexPos, maxIndexPos));
                    directoryContentsList.clear();
                    return;
                }
                startIndexPos = newItemsCount < 0 ? startIndexPos : startIndexPos + numItemsRequested - newItemsCount;
                maxIndexPos = newItemsCount > 0 ? maxIndexPos : maxIndexPos + newItemsCount;
                initMatrixCursorListing.moveToFirst();
                boolean appendNewItems = newItemsCount > 0;
                List<FileType> filesDataList = new ArrayList<FileType>(directoryContentsList.subList(trimFromFrontCount, directoryContentsList.size() - trimFromBackCount));
                int prependInsertIdx = 0, mcRowIdx;
                for (mcRowIdx = 0; mcRowIdx < Math.min(initMatrixCursorListing.getCount(), Math.abs(newItemsCount)); mcRowIdx++) {
                    String[] filePropertiesList = fpInst.getPropertiesOfCurrentRow(initMatrixCursorListing, !BasicFileProvider.CURSOR_TYPE_IS_ROOT);
                    String fileAbsPath = filePropertiesList[BasicFileProvider.PROPERTY_ABSPATH];
                    String fileProviderDocId = filePropertiesList[BasicFileProvider.PROPERTY_FILE_PROVIDER_DOCID];
                    String fileSize = filePropertiesList[BasicFileProvider.PROPERTY_FILE_SIZE];
                    String filePosixPerms = filePropertiesList[BasicFileProvider.PROPERTY_POSIX_PERMS];
                    boolean isDir = Boolean.parseBoolean(filePropertiesList[BasicFileProvider.PROPERTY_ISDIR]);
                    boolean isHidden = Boolean.parseBoolean(filePropertiesList[BasicFileProvider.PROPERTY_ISHIDDEN]);
                    FileType nextFileItem = new FileType(fileAbsPath, fileSize, filePosixPerms, isDir, isHidden, fileProviderDocId, this);
                    if(appendNewItems) {
                        filesDataList.add(nextFileItem);
                    }
                    else {
                        filesDataList.add(prependInsertIdx, nextFileItem);
                        prependInsertIdx++;
                    }
                    initMatrixCursorListing.moveToNext();
                }
                if(updateGlobalIndices) {
                    int resultSizeDiff = Math.abs(newItemsCount) - mcRowIdx;
                    Log.i(LOGTAG, String.format(Locale.getDefault(), "UPDATING GLOBAL INDICES: [%d, %d] -> [%d, %d]", initStartIndexPos,
                            DisplayFragments.getInstance().lastFileDataEndIndex, initStartIndexPos,
                            DisplayFragments.getInstance().lastFileDataEndIndex - resultSizeDiff));
                    DisplayFragments.getInstance().lastFileDataEndIndex = DisplayFragments.getInstance().lastFileDataEndIndex - resultSizeDiff;
                }
                setNextDirectoryContents(filesDataList);
            }
            catch(FileNotFoundException ioe) {
                ioe.printStackTrace();
                return;
            }

        }

        public void computeDirectoryContents(int startIndexPos, int maxIndexPos) {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            fpInst.setFilesListLength(DisplayFragments.getViewportMaxFilesCount());
            computeDirectoryContents(startIndexPos, maxIndexPos,
                    0, directoryContentsList.size(),maxIndexPos + 1 - startIndexPos, true);
        }

        public void clearDirectoryContentsList() {
            directoryContentsList.clear();
        }

        public DirectoryResultContext loadNextFolderAtIndex(int posIndex, boolean initNewFileTree) {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            DisplayFragments.updateFolderHistoryPaths(FileUtils.getFileBaseNameFromPath(activeCWDAbsPath), initNewFileTree);
            try {
                fpInst.updateQueryFilesList(); // cancel any previously pending noUpdate requests
                FileType requestedFileItem = directoryContentsList.get(posIndex);
                String nextActiveDocId = requestedFileItem.getFileProviderDocumentId();
                MatrixCursor nextDirCursor = (MatrixCursor) fpInst.queryChildDocuments(nextActiveDocId, BasicFileProvider.DEFAULT_DOCUMENT_PROJECTION, "");
                clearDirectoryContentsList();
                DirectoryResultContext nextFolderCtx = new DirectoryResultContext(nextDirCursor, requestedFileItem.getFileProviderDocumentId(), requestedFileItem.getAbsolutePath());
                return nextFolderCtx;
            } catch(Exception ioe) {
                ioe.printStackTrace();
                DisplayFragments.getInstance().pathHistoryStack.pop();
                return null;
            }
        }

        public static DirectoryResultContext probeAtCursoryFolderQuery(FileChooserBuilder.BaseFolderPathType baseFolderChoice) {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            if(fpInst == null) {
                return null;
            }
            else {
                fpInst.setCustomFileFilter(DisplayFragments.getInstance().localFilesListFilter);
                fpInst.setCustomFolderSort(DisplayFragments.getInstance().localFilesListSortFunc);
            }
            fpInst.selectBaseDirectoryByType(baseFolderChoice);
            try {
                fpInst.updateQueryFilesList(); // cancel any previously pending noUpdate requests
                MatrixCursor cursoryProbe = (MatrixCursor) fpInst.queryRoots(BasicFileProvider.DEFAULT_ROOT_PROJECTION);
                cursoryProbe.moveToFirst();
                String initDirBaseName = fpInst.getBaseNameAtCurrentRow(cursoryProbe, BasicFileProvider.CURSOR_TYPE_IS_ROOT);
                DisplayFragments.updateFolderHistoryPaths(initDirBaseName, false);
                String cursoryProbeFolderCwd = fpInst.getPropertiesOfCurrentRow(cursoryProbe, BasicFileProvider.CURSOR_TYPE_IS_ROOT)[BasicFileProvider.PROPERTY_ABSPATH];
                String parentDocsId = cursoryProbe.getString(BasicFileProvider.ROOT_PROJ_ROOTID_COLUMN_INDEX);
                MatrixCursor expandedFolderContents = (MatrixCursor) fpInst.queryChildDocuments(parentDocsId, BasicFileProvider.DEFAULT_DOCUMENT_PROJECTION, "");
                return new DirectoryResultContext(expandedFolderContents, parentDocsId, cursoryProbeFolderCwd);
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
                return null;
            }
        }

    }

    public static class FileType {

        private static String LOGTAG = FileType.class.getSimpleName();

        private DirectoryResultContext parentFolder;
        private String fileProviderDocId;
        private String fileAbsPath;
        private String fileSizeLabel;
        private String filePosixStylePerms;
        private boolean isDir;
        private boolean isHidden;
        private boolean isChecked;
        private View fileItemLayoutContainer;

        public FileType(String fileAbsPath, String fileSizeLabel, String posixPerms,
                        boolean isDirectory, boolean isHidden, String fpDocId,
                        DirectoryResultContext parentFolder) {
            this.parentFolder = parentFolder;
            this.fileProviderDocId = fpDocId;
            this.fileAbsPath = fileAbsPath;
            this.fileSizeLabel = fileSizeLabel;
            this.filePosixStylePerms = posixPerms;
            this.isDir = isDirectory;
            this.isHidden = isHidden;
            this.isChecked = false;
            this.fileItemLayoutContainer = null;
        }

        public DirectoryResultContext getParentFolderContext() {
            return parentFolder;
        }

        public String getFileProviderDocumentId() { return fileProviderDocId; }

        public String getAbsolutePath() {
            return fileAbsPath;
        }

        public String getBaseName() {
            return FileUtils.getFileBaseNameFromPath(getAbsolutePath());
        }

        public boolean isDirectory() {
            return isDir;
        }

        public boolean isHidden() {
            return isHidden;
        }

        public String getExtension() {
            return FileUtils.getFileExtension(getAbsolutePath());
        }

        public String getMimeType() {
            return FileUtils.getFileMimeType(getAbsolutePath());
        }

        public String getPosixPermissions() {
            return filePosixStylePerms;
        }

        public String getChmodStylePermissions() {
            String filePosixPerms = getPosixPermissions();
            return FileUtils.filePermsStringToShortChmodStyleCode(filePosixPerms, isDirectory());
        }

        public String getFileSizeString() {
            if(isDirectory()) {
                return "DIR";
            }
            else {
                return fileSizeLabel;
            }
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean enable) {
            isChecked = enable;
        }

        public boolean setFileTypeIcon(Drawable fileIcon) {
            if(fileIcon == null || fileItemLayoutContainer == null) {
                return false;
            }
            ImageView fileIconImageView = fileItemLayoutContainer.findViewById(R.id.fileTypeIcon);
            if(fileIconImageView == null) {
                return false;
            }
            fileIconImageView.setImageDrawable(fileIcon);
            return true;
        }

        public Drawable getFileTypeIcon() {
            if(fileItemLayoutContainer == null) {
                return null;
            }
            ImageView fileIconImageView = fileItemLayoutContainer.findViewById(R.id.fileTypeIcon);
            if(fileIconImageView == null) {
                return null;
            }
            return fileIconImageView.getDrawable();
        }

        public View getLayoutContainer() {
            return fileItemLayoutContainer;
        }

        public void setLayoutContainer(View fileItemLayoutContainer) {
            this.fileItemLayoutContainer = fileItemLayoutContainer;
        }

    }

}