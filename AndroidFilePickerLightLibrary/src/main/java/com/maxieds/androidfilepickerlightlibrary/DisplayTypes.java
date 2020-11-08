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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class DisplayTypes {

    private static String LOGTAG = DisplayTypes.class.getSimpleName();

    public static class DirectoryResultContext {

        private static String LOGTAG = DirectoryResultContext.class.getSimpleName();

        public static Stack<DirectoryResultContext> pathHistoryStack = new Stack<DisplayTypes.DirectoryResultContext>();

        private MatrixCursor initMatrixCursorListing;
        private List<FileType> directoryContentsList;
        private String activeCWDAbsPath;

        public DirectoryResultContext(MatrixCursor mcResult, MatrixCursor parentDirCtx) {
            directoryContentsList = new ArrayList<FileType>();
            initMatrixCursorListing = mcResult;
            mcResult.moveToFirst();
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            activeCWDAbsPath = fpInst.getAbsPathAtCurrentRow(mcResult, !BasicFileProvider.CURSOR_TYPE_IS_ROOT);
            Log.i(LOGTAG, String.format(Locale.getDefault(), "Initializing new folder at path: \"%s\" ... ", activeCWDAbsPath));
        }

        public MatrixCursor getInitialMatrixCursor() { return initMatrixCursorListing; }

        public List<FileType> getWorkingDirectoryContents() { return directoryContentsList; }

        public void setNextDirectoryContents(List<FileType> nextFolderFiles) { directoryContentsList = nextFolderFiles; }

        public void computeDirectoryContents(int startIndexPos, int maxIndexPos) {
            Log.i(LOGTAG, String.format(Locale.getDefault(), "STARTING: Computing dir contents [%d, %d] -- %s", startIndexPos, maxIndexPos, activeCWDAbsPath));
            if(startIndexPos >= getInitialMatrixCursor().getCount() || maxIndexPos < startIndexPos) {
                Log.e(LOGTAG, String.format("RETURNING cursor positions out of range %d / [%d, %d] ... ", getInitialMatrixCursor().getCount(), startIndexPos, maxIndexPos));
                directoryContentsList.clear();
                return;
            }
            directoryContentsList.clear();
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            MatrixCursor mcResult = getInitialMatrixCursor();
            mcResult.moveToPosition(startIndexPos);
            List<FileType> filesDataList = new ArrayList<FileType>();
            for(int mcRowIdx = startIndexPos; mcRowIdx <= Math.min(mcResult.getCount() - 1, maxIndexPos); mcRowIdx++) {
                String[] filePropertiesList =  fpInst.getPropertiesOfCurrentRow(mcResult, !BasicFileProvider.CURSOR_TYPE_IS_ROOT);
                String fileAbsPath = filePropertiesList[BasicFileProvider.PROPERTY_ABSPATH];
                String fileSize = filePropertiesList[BasicFileProvider.PROPERTY_FILE_SIZE];
                String filePosixPerms = filePropertiesList[BasicFileProvider.PROPERTY_POSIX_PERMS];
                boolean isDir = Boolean.parseBoolean(filePropertiesList[BasicFileProvider.PROPERTY_ISDIR]);
                boolean isHidden = Boolean.parseBoolean(filePropertiesList[BasicFileProvider.PROPERTY_ISHIDDEN]);
                FileType nextFileItem = new FileType(fileAbsPath, fileSize, filePosixPerms, isDir, isHidden,  this);
                nextFileItem.setRelativeCursorPosition(mcRowIdx - maxIndexPos);
                filesDataList.add(nextFileItem);
                mcResult.moveToNext();
            }
            setNextDirectoryContents(filesDataList);

        }

        public void clearDirectoryContentsList() {
            directoryContentsList.clear();
        }

        public DirectoryResultContext loadNextFolderAtIndex(int posIndex, boolean initNewFileTree) {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            DisplayFragments.updateFolderHistoryPaths(FileUtils.getFileBaseNameFromPath(activeCWDAbsPath), initNewFileTree);
            MatrixCursor nextDirCursor = null;
            try {
                int curCursorIndex = initMatrixCursorListing.getPosition();
                initMatrixCursorListing.moveToPosition(posIndex);
                String nextActiveDocId = initMatrixCursorListing.getString(BasicFileProvider.ROOT_PROJ_DOCID_COLUMN_INDEX);
                initMatrixCursorListing.moveToPosition(curCursorIndex);
                nextDirCursor = (MatrixCursor) fpInst.queryChildDocuments(nextActiveDocId, BasicFileProvider.DEFAULT_DOCUMENT_PROJECTION, "");
            } catch(Exception ioe) {
                ioe.printStackTrace();
                pathHistoryStack.pop();
                return null;
            }
            clearDirectoryContentsList();
            DirectoryResultContext nextFolderCtx = new DirectoryResultContext(nextDirCursor, initMatrixCursorListing);
            pathHistoryStack.push(nextFolderCtx);
            return nextFolderCtx;
        }

        public DirectoryResultContext loadNextFolderAtIndex(int posIndex) {
            return loadNextFolderAtIndex(posIndex, false);
        }

        public DirectoryResultContext loadNextFolderAtIndex(boolean initNewFiletree) {
            return loadNextFolderAtIndex(initMatrixCursorListing.getPosition(), initNewFiletree);
        }

        public static DirectoryResultContext probeAtCursoryFolderQuery(FileChooserBuilder.BaseFolderPathType baseFolderChoice) {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            fpInst.selectBaseDirectoryByType(baseFolderChoice);
            try {
                MatrixCursor cursoryProbe = (MatrixCursor) fpInst.queryRoots(BasicFileProvider.DEFAULT_ROOT_PROJECTION);
                cursoryProbe.moveToFirst();
                String initDirBaseName = fpInst.getBaseNameAtCurrentRow(cursoryProbe, BasicFileProvider.CURSOR_TYPE_IS_ROOT);
                DisplayFragments.updateFolderHistoryPaths(initDirBaseName, false);
                Log.i(LOGTAG, String.format(Locale.getDefault(), "Updating history nav to: %s", initDirBaseName));
                String parentDocsId = cursoryProbe.getString(BasicFileProvider.ROOT_PROJ_DOCID_COLUMN_INDEX); // ???
                MatrixCursor expandedFolderContents = (MatrixCursor) fpInst.queryChildDocuments(parentDocsId, BasicFileProvider.DEFAULT_DOCUMENT_PROJECTION, "");
                return new DirectoryResultContext(expandedFolderContents, null);
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
        private String fileAbsPath;
        private String fileSizeLabel;
        private String filePosixStylePerms;
        private boolean isDir;
        private boolean isHidden;

        private int cwdRelativeCursorPos;
        private boolean isChecked;
        private Drawable fileTypeIcon;
        private View fileItemLayoutContainer;

        public FileType(String fileAbsPath, String fileSizeLabel, String posixPerms,
                        boolean isDirectory, boolean isHidden,
                        DirectoryResultContext parentFolder) {
            this.parentFolder = parentFolder;
            this.fileAbsPath = fileAbsPath;
            this.fileSizeLabel = fileSizeLabel;
            this.filePosixStylePerms = posixPerms;
            this.isDir = isDirectory;
            this.isHidden = isHidden;
            this.isChecked = false;
            this.fileItemLayoutContainer = null;
            this.cwdRelativeCursorPos = -1;
        }

        public DirectoryResultContext getParentFolderContext() {
            return parentFolder;
        }

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

        public void setFileTypeIcon(Drawable fileIcon) {
            fileTypeIcon = fileIcon;
        }

        public Drawable getFileTypeIcon() {
            return fileTypeIcon;
        }

        public View getLayoutContainer() {
            return fileItemLayoutContainer;
        }

        public void setLayoutContainer(View fileItemLayoutContainer) {
            this.fileItemLayoutContainer = fileItemLayoutContainer;
        }

        public int getRelativeCursorPosition() {
            return cwdRelativeCursorPos;
        }

        public void setRelativeCursorPosition(int activeCursorPos) {
            cwdRelativeCursorPos = activeCursorPos;
        }

    }

}