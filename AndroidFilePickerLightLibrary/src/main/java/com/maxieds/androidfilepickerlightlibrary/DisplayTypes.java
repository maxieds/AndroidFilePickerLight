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
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.apache.commons.lang3.ArrayUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DisplayTypes {

    private static String LOGTAG = DisplayTypes.class.getSimpleName();

    public static class DirectoryResultContext {

        private static String LOGTAG = DirectoryResultContext.class.getSimpleName();

        private MatrixCursor initMatrixCursorListing;
        private List<FileType> directoryContentsList;
        private String parentDocId;
        private String activeCWDAbsPath;
        private int folderMaxChildCount;
        private boolean isTopLevelFolder;
        private boolean isRecentDocsFolder;

        public DirectoryResultContext(MatrixCursor mcResult, String parentFolderDocId, String parentFolderAbsPath) {
            directoryContentsList = new ArrayList<FileType>();
            parentDocId = parentFolderDocId;
            initMatrixCursorListing = mcResult;
            mcResult.moveToFirst();
            activeCWDAbsPath = parentFolderAbsPath;
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            fpInst.noUpdateQueryFilesList();
            try {
                if(isRecentDocuments()) {
                    folderMaxChildCount = mcResult.getCount();
                }
                else {
                    folderMaxChildCount = fpInst.getFolderChildCount(parentDocId);
                }
            } catch(FileNotFoundException nfe) {
                nfe.printStackTrace();
                folderMaxChildCount = 0;
            }
            isTopLevelFolder = false;
            isRecentDocsFolder = false;
            Log.d(LOGTAG, String.format(Locale.getDefault(), "Initializing new folder at path: \"%s\" ... ", activeCWDAbsPath));
        }

        public List<FileType> getWorkingDirectoryContents() { return directoryContentsList; }

        public void setNextDirectoryContents(List<FileType> nextFolderFiles) { directoryContentsList = nextFolderFiles; }

        public int getFolderChildCount() { return folderMaxChildCount; }

        public boolean isTopLevelFolder() { return isTopLevelFolder; }
        public void setTopLevelFolder(boolean topLevel) { isTopLevelFolder = topLevel; }

        public boolean isRecentDocuments() {
            return isRecentDocsFolder;
        }
        public void setIsRecentDocuments(boolean isRecentDocs) {
            isRecentDocsFolder = isRecentDocs;
            setTopLevelFolder(isRecentDocs);
        }

        public String getCWDBasePath() {
            return FileUtils.getFileBaseNameFromPath(activeCWDAbsPath);
        }

        public void computeDirectoryContents(int startIndexPos, int maxIndexPos,
                                             int trimFromFrontCount, int trimFromBackCount, int newItemsCount,
                                             boolean updateGlobalIndices) {
            Log.i(LOGTAG, String.format(Locale.getDefault(), "STARTING: Computing dir contents [%d, %d] -- %s", startIndexPos, maxIndexPos, activeCWDAbsPath));
            if(startIndexPos >= getFolderChildCount() || maxIndexPos < startIndexPos) {
                Log.e(LOGTAG, String.format("RETURNING positions out of range %d / [%d, %d] ... ", getFolderChildCount(), startIndexPos, maxIndexPos));
                directoryContentsList.clear();
                return;
            }
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            if(fpInst == null) {
                return;
            }
            if(newItemsCount > 0) {
                fpInst.setFilesStartIndex(maxIndexPos + 1 - Math.abs(newItemsCount));
                fpInst.setFilesListLength(Math.abs(newItemsCount));
                Log.d(LOGTAG, "REQUESTING start index = " + (maxIndexPos + 1 - Math.abs(newItemsCount)) + ", LEN = " + Math.abs(newItemsCount));
            }
            else {
                fpInst.setFilesStartIndex(startIndexPos);
                fpInst.setFilesListLength(Math.abs(newItemsCount));
                Log.d(LOGTAG, "REQUESTING start index = " + startIndexPos + ", LEN = " + Math.abs(newItemsCount));
            }
            int initStartIndexPos = startIndexPos;
            try {
                fpInst.noUpdateQueryFilesList(); // save some time processing if we haven't recently loaded a new folder to process
                String parentDocsId = parentDocId;
                initMatrixCursorListing = (MatrixCursor) fpInst.queryChildDocuments(parentDocsId, BasicFileProvider.DEFAULT_DOCUMENT_PROJECTION, "");
                initMatrixCursorListing.moveToPosition(0);
                boolean appendNewItems = newItemsCount > 0;
                List<FileType> filesDataList = directoryContentsList.subList(trimFromFrontCount, directoryContentsList.size() - trimFromBackCount);
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
                    Log.d(LOGTAG, String.format(Locale.getDefault(), "UPDATING GLOBAL INDICES: [%d, %d] -> [%d, %d]",
                            DisplayFragments.getInstance().lastFileDataStartIndex, DisplayFragments.getInstance().lastFileDataEndIndex,
                            initStartIndexPos, maxIndexPos - resultSizeDiff));
                    DisplayFragments.getInstance().lastFileDataStartIndex = initStartIndexPos;
                    DisplayFragments.getInstance().lastFileDataEndIndex = maxIndexPos - resultSizeDiff;
                }
                setNextDirectoryContents(filesDataList);
                Log.d(LOGTAG, "computeDirectoryContents: PRINTING NEXT (truncated) folder contents list:");
                for(int fcidx = Math.max(0, directoryContentsList.size() - 3); fcidx < directoryContentsList.size(); fcidx++) {
                    Log.d(LOGTAG, String.format(Locale.getDefault(), "   [#%02d => %02d ACTUAL Idx] FILE BASE NAME => \"%s\" ... ", fcidx + 1,
                            fcidx + 1 + DisplayFragments.getInstance().lastFileDataStartIndex, directoryContentsList.get(fcidx).getBaseName()));
                }
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
                // Load the document ID for the current position in case it is out of range:
                if(!isRecentDocuments()) {
                    computeDirectoryContents(posIndex, posIndex);
                    FileType selectedFileItem = directoryContentsList.get(0);
                    return probeAtCursoryFolderQuery(selectedFileItem.getBaseName());
                }
                else {
                    FileType selectedFileItem = directoryContentsList.get(posIndex);
                    return probeAtCursoryFolderQuery(selectedFileItem.getAbsolutePath().replaceAll(fpInst.getCWD(), ""));
                }
            } catch(Exception ioe) {
                ioe.printStackTrace();
                DisplayFragments.getInstance().pathHistoryStack.pop();
                return null;
            }
        }

        private static DirectoryResultContext probeAtCursoryFolderQueryGetNextRecents() {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            try {
                MatrixCursor cursoryProbe = (MatrixCursor) fpInst.queryRoots(BasicFileProvider.DEFAULT_ROOT_PROJECTION);
                cursoryProbe.moveToFirst();
                String initDirBaseName = fpInst.getBaseNameAtCurrentRow(cursoryProbe, BasicFileProvider.CURSOR_TYPE_IS_ROOT);
                String cursoryProbeFolderCwd = "Recent Documents";
                DisplayFragments.updateFolderHistoryPaths(cursoryProbeFolderCwd, true);
                String parentDocsId = cursoryProbe.getString(BasicFileProvider.ROOT_PROJ_ROOTID_COLUMN_INDEX);
                MatrixCursor expandedFolderContents = (MatrixCursor) fpInst.queryRecentDocuments(parentDocsId, BasicFileProvider.DEFAULT_DOCUMENT_PROJECTION);
                DirectoryResultContext exploredFolderCtx = new DirectoryResultContext(expandedFolderContents, parentDocsId, cursoryProbeFolderCwd);
                exploredFolderCtx.setIsRecentDocuments(true);
                return exploredFolderCtx;
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
                return null;
            }
        }

        private static DirectoryResultContext probeAtCursoryFolderQueryGetNext() {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            try {
                fpInst.updateQueryFilesList(); // cancel any previously pending noUpdate requests
                MatrixCursor cursoryProbe = (MatrixCursor) fpInst.queryRoots(BasicFileProvider.DEFAULT_ROOT_PROJECTION);
                cursoryProbe.moveToFirst();
                String initDirBaseName = fpInst.getBaseNameAtCurrentRow(cursoryProbe, BasicFileProvider.CURSOR_TYPE_IS_ROOT);
                DisplayFragments.updateFolderHistoryPaths(initDirBaseName, false);
                String cursoryProbeFolderCwd = cursoryProbe.getString(ArrayUtils.indexOf(cursoryProbe.getColumnNames(), BasicFileProvider.ROOT_COLUMN_NAME_ABSPATH));
                String parentDocsId = cursoryProbe.getString(BasicFileProvider.ROOT_PROJ_ROOTID_COLUMN_INDEX);
                MatrixCursor expandedFolderContents = (MatrixCursor) fpInst.queryChildDocuments(parentDocsId, BasicFileProvider.DEFAULT_DOCUMENT_PROJECTION, "");
                return new DirectoryResultContext(expandedFolderContents, parentDocsId, cursoryProbeFolderCwd);
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
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
            if(baseFolderChoice.ordinal() != FileChooserBuilder.BaseFolderPathType.BASE_PATH_TYPE_RECENT_DOCUMENTS.ordinal()) {
                fpInst.selectBaseDirectoryByType(baseFolderChoice);
                return probeAtCursoryFolderQueryGetNext();
            }
            else {
                fpInst.selectBaseDirectoryByType(FileChooserBuilder.BaseFolderPathType.BASE_PATH_DEFAULT);
                return probeAtCursoryFolderQueryGetNextRecents();
            }
        }

        public static DirectoryResultContext probeAtCursoryFolderQuery(String nextSubfolderPath) {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            if(fpInst == null) {
                return null;
            }
            else {
                fpInst.setCustomFileFilter(DisplayFragments.getInstance().localFilesListFilter);
                fpInst.setCustomFolderSort(DisplayFragments.getInstance().localFilesListSortFunc);
            }
            if(!fpInst.enterNextSubfolder(nextSubfolderPath)) {
                return null;
            }
            Log.i(LOGTAG, "ENTERING subfolder \"" + nextSubfolderPath + "\" ...");
            return probeAtCursoryFolderQueryGetNext();
        }

        public static DirectoryResultContext probeAtCursoryFolderQuery() {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            if(fpInst == null) {
                return null;
            }
            else {
                fpInst.setCustomFileFilter(DisplayFragments.getInstance().localFilesListFilter);
                fpInst.setCustomFolderSort(DisplayFragments.getInstance().localFilesListSortFunc);
            }
            return probeAtCursoryFolderQueryGetNext();
        }

        public static DirectoryResultContext probePreviousFolder(int howManyBackwards) {
            BasicFileProvider fpInst = BasicFileProvider.getInstance();
            if(fpInst == null) {
                return null;
            }
            else {
                fpInst.setCustomFileFilter(DisplayFragments.getInstance().localFilesListFilter);
                fpInst.setCustomFolderSort(DisplayFragments.getInstance().localFilesListSortFunc);
            }
            if(howManyBackwards <= 0) {
                return null;
            }
            while(--howManyBackwards >= 0) {
                if(!fpInst.backOneFolder()) {
                    return null;
                }
            }
            return probeAtCursoryFolderQueryGetNext();
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

        public String getBasePath() {
            return FileUtils.getFileBasePath(getAbsolutePath());
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
            return fileSizeLabel;
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

}