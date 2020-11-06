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

public class DisplayTypes {

    private static String LOGTAG = DisplayTypes.class.getSimpleName();

    public static class DirectoryResultContext {

        public static Stack<DirectoryResultContext> pathHistoryStack = new Stack<DisplayTypes.DirectoryResultContext>();

        private MatrixCursor initMatrixCursorListing;
        private List<FileType> directoryContentsList;
        private String activeCWDAbsPath;

        public DirectoryResultContext(MatrixCursor mcResult, MatrixCursor parentDirCtx) {
            initMatrixCursorListing = mcResult;
            computeDirectoryContents();
            if(parentDirCtx != null) {
                BasicFileProvider fpInst = BasicFileProvider.getInstance();
                activeCWDAbsPath = fpInst.getAbsPathAtCurrentRow(parentDirCtx);
            }
            else {
                activeCWDAbsPath = "";
            }
        }

        public List<FileType> getWorkingDirectoryContents() {
            if(directoryContentsList.size() == 0) {
                computeDirectoryContents();
            }
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
                nextFileItem.setRelativeCursorPosition(mcRowIdx);
                filesDataList.add(nextFileItem);
                mcResult.moveToNext();
            }
            mcResult.moveToFirst();
            directoryContentsList = filesDataList;
        }

        public void clearDirectoryContentsList() {
            directoryContentsList.clear();
        }

        public DirectoryResultContext loadNextFolderAtIndex(int posIndex, boolean initNewFileTree) {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            DisplayFragments.updateFolderHistoryPaths(activeCWDAbsPath.replace("\\(.*/\\)*/(.*)", "$0"), initNewFileTree);
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
        private int cwdRelativeCursorPos;

        public FileType(File fileOnDisk, DirectoryResultContext parentFolder) {
            this.parentFolder = parentFolder;
            this.fileOnDisk = fileOnDisk;
            this.isChecked = false;
            this.fileItemLayoutContainer = null;
            this.cwdRelativeCursorPos = -1;
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
            String filePosixPerms = FileUtils.getFilePosixPermissionsString(fileOnDisk);
            return FileUtils.filePermsStringToShortChmodStyleCode(filePosixPerms, isDirectory());
        }

        public String getFileSizeString() {
            if(isDirectory()) {
                return "DIR";
            }
            else {
                return FileUtils.getFileSizeString(fileOnDisk);
            }
        }

        public boolean isChecked() {
            return isChecked;
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