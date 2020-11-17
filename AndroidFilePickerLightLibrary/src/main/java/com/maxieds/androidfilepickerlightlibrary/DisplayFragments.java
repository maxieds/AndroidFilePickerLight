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
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

public class DisplayFragments {

    private static String LOGTAG = DisplayFragments.class.getSimpleName();

    private static DisplayFragments localStaticInst = new DisplayFragments();
    private FileChooserRecyclerView recyclerView = null;

    public DisplayFragments() {
        folderIconInst = DisplayUtils.getDrawableFromResource(R.drawable.folder_icon32);
        fileIconInst = DisplayUtils.getDrawableFromResource(R.drawable.generic_file_icon32);
        hiddenFileIconInst = DisplayUtils.getDrawableFromResource(R.drawable.hidden_file_icon32);
        localFilesListFilter = null;
        localFilesListSortFunc = null;
    }

    public FileFilter.FileFilterBase localFilesListFilter;
    public FileFilter.FileItemsSortFunc localFilesListSortFunc;

    private Drawable folderIconInst;
    private Drawable fileIconInst;
    private Drawable hiddenFileIconInst;
    private CustomThemeBuilder.FileItemLayoutStylizer fileItemLayoutStylizer;

    public boolean setFileItemLayoutStylizer(CustomThemeBuilder.FileItemLayoutStylizer layoutStylizer, boolean freeOtherSpace) {
        if(layoutStylizer == null) {
            return false;
        }
        fileItemLayoutStylizer = layoutStylizer;
        if(freeOtherSpace) {
            folderIconInst = fileIconInst = hiddenFileIconInst = null;
        }
        return true;
    }

    public CustomThemeBuilder.FileItemLayoutStylizer getFileItemLayoutStylizer() { return fileItemLayoutStylizer; }

    public static FileChooserRecyclerView getMainRecyclerView() {
        return getInstance().recyclerView;
    }

    public void setRecyclerView(FileChooserRecyclerView rview) { recyclerView = rview; }

    public static DisplayFragments getInstance() { return FileChooserActivity.getInstance().getDisplayFragmentsInstance(); }

    public DisplayTypes.DirectoryResultContext getCwdFolderContext() {
        return FileChooserActivity.getInstance().getCwdFolderContext();
    }

    public void setCwdFolderContext(DisplayTypes.DirectoryResultContext nextCwdCtx) {
        FileChooserActivity.getInstance().setCwdFolderContext(nextCwdCtx);
    }

    public boolean recyclerViewAdapterInit = false;
    public boolean viewportCapacityMesaured = false;

    public int maxAllowedSelections = 0;
    public int curSelectionCount = 0;
    public boolean allowSelectFiles = true;
    public boolean allowSelectFolders = true;
    public int lastFileDataStartIndex = 0, lastFileDataEndIndex = DEFAULT_VIEWPORT_FILE_ITEMS_COUNT - 1;

    public  List<DisplayTypes.FileType> activeSelectionsList = new ArrayList<DisplayTypes.FileType>();
    public  List<DisplayTypes.FileType> activeFileItemsDataList = new ArrayList<DisplayTypes.FileType>();
    public List<String> fileItemBasePathsList = new ArrayList<String>();
    public Stack<DisplayTypes.DirectoryResultContext> pathHistoryStack;

    public static final int DEFAULT_VIEWPORT_FILE_ITEMS_COUNT = 50; // set large enough to overfill the window on first load
    private int viewportMaxFileItemsCount = DEFAULT_VIEWPORT_FILE_ITEMS_COUNT;
    public int fileItemDisplayHeight = 0;

    public static int getViewportMaxFilesCount() {
        return getInstance().viewportMaxFileItemsCount;
    }

    private void setViewportMaxFilesCount(int viewportFilesCap) {
        viewportMaxFileItemsCount = viewportFilesCap;
    }

    public boolean resetViewportMaxFilesCount(View parentViewContainer) {
        if(!viewportCapacityMesaured) {
            int viewportDisplayHeight = parentViewContainer.getMeasuredHeight();
            if(fileItemDisplayHeight == 0 || viewportDisplayHeight == 0) {
                return false;
            }
            setViewportMaxFilesCount((int) Math.floor((double) viewportDisplayHeight / fileItemDisplayHeight));
            Log.i(LOGTAG, String.format("DELAYED RESPONSE: VP Height = %d, FItemDisp Height = %d   ====>  %d",
                    viewportDisplayHeight, fileItemDisplayHeight, getViewportMaxFilesCount()));
            getMainRecyclerView().setItemViewCacheSize(getViewportMaxFilesCount());
            getMainRecyclerView().setDrawingCacheEnabled(true);
            getMainRecyclerView().setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            viewportCapacityMesaured = true;
        }
        return true;
    }

    public void initializeRecyclerViewLayout(FileChooserRecyclerView rview, FileChooserBuilder fpConfig) {
        if(!recyclerViewAdapterInit) {
            viewportMaxFileItemsCount = fpConfig.getRecyclerViewStartBufferSize();
            FileChooserRecyclerView.setFlingVelocityDampenAtThreshold(fpConfig.getRecyclerViewLayoutFlingDampenThreshold());
            rview.setupRecyclerViewLayout();
            setRecyclerView(rview);
            fileItemBasePathsList = new ArrayList<String>();
            activeSelectionsList = new ArrayList<DisplayTypes.FileType>();
            activeFileItemsDataList = new ArrayList<DisplayTypes.FileType>();
            DisplayAdapters.FileListAdapter rvAdapter = new DisplayAdapters.FileListAdapter(fileItemBasePathsList, activeFileItemsDataList);
            rview.setAdapter(rvAdapter);
            recyclerView.smoothScrollToPosition(0);
            recyclerViewAdapterInit = true;
        }
    }

    public void resetRecyclerViewLayoutContext() {
        activeSelectionsList.clear();
        activeFileItemsDataList.clear();
        fileItemBasePathsList.clear();
        viewportMaxFileItemsCount = DEFAULT_VIEWPORT_FILE_ITEMS_COUNT;
        lastFileDataStartIndex = 0;
        lastFileDataEndIndex = viewportMaxFileItemsCount - 1;
        recyclerViewAdapterInit = false;
        viewportCapacityMesaured = false;
        pathHistoryStack = new Stack<DisplayTypes.DirectoryResultContext>();
        if(BasicFileProvider.getInstance() != null) {
            BasicFileProvider.getInstance().setCustomFileFilter(null);
            BasicFileProvider.getInstance().setCustomFolderSort(null);
        }
    }

    public void clearExistingRecyclerViewLayout() {
        // Completely clear out the previously displayed contents:
        FileChooserRecyclerView mainRV = getMainRecyclerView();
        mainRV.invalidate();
        DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();
        int priorAdapterCount = rvAdapter.getItemCount();
        rvAdapter.reloadDataSets(new ArrayList<String>(), new ArrayList<DisplayTypes.FileType>(), false);
        rvAdapter.notifyItemRangeRemoved(0, priorAdapterCount);
        rvAdapter.notifyDataSetChanged();
        mainRV.removeAllViews();
        mainRV.removeAllViewsInLayout();
    }

    public void descendIntoNextDirectory(boolean initNewFileTree) {

        if(pathHistoryStack.empty()) {
            cancelAllOperationsInProgress();
            FileChooserException.GenericRuntimeErrorException rte = new FileChooserException.GenericRuntimeErrorException("Empty context for folder history ( no more history ??? )");
            FileChooserActivity.getInstance().postSelectedFilesActivityResult(rte);
        }
        DisplayTypes.DirectoryResultContext nextFolder = pathHistoryStack.pop();
        if(nextFolder != null) {

            // Stop the prefetch thread for the current directory:
            FileChooserActivity.getInstance().stopPrefetchFileUpdatesThread();
            clearExistingRecyclerViewLayout();
            updateFolderHistoryPaths(FileUtils.getFileBaseNameFromPath(nextFolder.getCWDBasePath()), initNewFileTree);

            // Descend into the next directory:
            lastFileDataStartIndex = 0;
            lastFileDataEndIndex = Math.min(Math.max(0, nextFolder.getFolderChildCount() - 1), lastFileDataStartIndex + getViewportMaxFilesCount() - 1);
            setCwdFolderContext(nextFolder);
            getCwdFolderContext().computeDirectoryContents(lastFileDataStartIndex, lastFileDataEndIndex);
            displayNextDirectoryFilesList(getCwdFolderContext().getWorkingDirectoryContents());

            // Restart the prefetch thread for the current directory:
            FileChooserActivity.getInstance().startPrefetchFileUpdatesThread();

        }
        else {
            Log.i(LOGTAG, "descendIntoNextDirectory: CWD Ctx is NULL!");
        }

    }

    /* Re-initiate the inquisition: Static reusable wrapper function to invoke loading a new directory
     * from scratch (reinitializing objects, starting the initial root query, and launching the
     * RecyclerView pattern making compendia on a whole new dataset):
     */
    public void initiateNewFolderLoad(FileChooserBuilder.BaseFolderPathType initBaseFolder) {
        clearExistingRecyclerViewLayout();
        FileChooserActivity.getInstance().setTopLevelBaseFolder(initBaseFolder);
        DisplayTypes.DirectoryResultContext cwdFolderContext = DisplayTypes.DirectoryResultContext.probeAtCursoryFolderQuery(initBaseFolder);
        cwdFolderContext.setTopLevelFolder(true); // cannot go up higher in the filesystem from here
        setCwdFolderContext(cwdFolderContext);
        pathHistoryStack.clear();
        lastFileDataStartIndex = 0;
        lastFileDataEndIndex = Math.min(Math.max(0, cwdFolderContext.getFolderChildCount() - 1), lastFileDataStartIndex + getViewportMaxFilesCount() - 1);
        getCwdFolderContext().computeDirectoryContents(lastFileDataStartIndex, lastFileDataEndIndex);
        displayNextDirectoryFilesList(getCwdFolderContext().getWorkingDirectoryContents());
        updateFolderHistoryPaths(FileUtils.getFileBaseNameFromPath(getCwdFolderContext().getCWDBasePath()), true);
    }

    public void displayNextDirectoryFilesList(List<DisplayTypes.FileType> workingDirContentsList, boolean notifyAdapter) {

        if(workingDirContentsList == null) {
            return;
        }
        RecyclerView mainFileListRecyclerView = getMainRecyclerView();
        if(mainFileListRecyclerView == null) {
            return;
        }

        activeSelectionsList.clear();
        activeFileItemsDataList.clear();
        fileItemBasePathsList.clear();

        final List<DisplayTypes.FileType> filteredFileContents = workingDirContentsList;
        for(int fidx = 0; fidx < filteredFileContents.size(); fidx++) {
            DisplayTypes.FileType fileItem = filteredFileContents.get(fidx);
            fileItemBasePathsList.add(fileItem.getBaseName());
            activeFileItemsDataList.add(fileItem);
        }
        if(notifyAdapter) {
            DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainFileListRecyclerView.getAdapter();
            rvAdapter.reloadDataSets(fileItemBasePathsList, activeFileItemsDataList);
        }

    }

    public void displayNextDirectoryFilesList(List<DisplayTypes.FileType> workingDirContentsList) {
        displayNextDirectoryFilesList(workingDirContentsList, true);
    }

    public static class FileItemFragment {

        private static String LOGTAG = FileItemFragment.class.getSimpleName();

        public static void resetLayout(View layoutContainer, DisplayTypes.FileType fileItem, int displayPosition) {

            CustomThemeBuilder.FileItemLayoutStylizer fileItemLayoutStylizer = DisplayFragments.getInstance().getFileItemLayoutStylizer();
            if(fileItemLayoutStylizer == null) {
                ImageView fileTypeIcon = layoutContainer.findViewById(R.id.fileTypeIcon);
                if (!fileItem.isDirectory()) {
                    if (!fileItem.isHidden()) {
                        fileTypeIcon.setImageDrawable(getInstance().fileIconInst);
                    } else {
                        fileTypeIcon.setImageDrawable(getInstance().hiddenFileIconInst);
                    }
                } else {
                    fileTypeIcon.setImageDrawable(getInstance().folderIconInst);
                }
            }
            else {
                fileItemLayoutStylizer.applyStyleToLayout(layoutContainer, fileItem);
            }

            TextView fileSizeText = layoutContainer.findViewById(R.id.fileEntrySizeText);
            fileSizeText.setText(fileItem.getFileSizeString());
            TextView filePermsSummary = layoutContainer.findViewById(R.id.fileEntryPermsSummaryText);
            filePermsSummary.setText(fileItem.getChmodStylePermissions());
            TextView fileBaseNameDisplayText = layoutContainer.findViewById(R.id.fileEntryBaseName);
            fileBaseNameDisplayText.setText(fileItem.getBaseName());

            boolean displaySelectionBox = (fileItem.isDirectory() && getInstance().allowSelectFolders) ||
                    (!fileItem.isDirectory() && getInstance().allowSelectFiles);
            CheckBox selectionBox = layoutContainer.findViewById(R.id.fileSelectCheckBox);
            if(!displaySelectionBox) {
                selectionBox.setEnabled(false);
                selectionBox.setVisibility(LinearLayout.INVISIBLE);
            }
            selectionBox.setTag(displayPosition);
            selectionBox.setOnCheckedChangeListener(new DisplayAdapters.OnSelectListener());

        }

    }

    private static final String EMPTY_FOLDER_HISTORY_PATH = "";
    private static String folderHistoryOneBackPath = EMPTY_FOLDER_HISTORY_PATH;
    private static String folderHistoryTwoBackPath = EMPTY_FOLDER_HISTORY_PATH;

    public static FolderNavigationFragment mainFolderNavFragment = null;

    public static class FolderNavigationFragment {

        public static TextView dirsOneBackText = null;

        public static FolderNavigationFragment createNewFolderNavFragment(View navBtnsContainerView) {
            FolderNavigationFragment folderNavFragment = new FolderNavigationFragment();
            dirsOneBackText = FileChooserActivity.getInstance().findViewById(R.id.mainDirNavBackOnePathDisplayText);
            updateFolderHistoryPaths(null, true);
            return folderNavFragment;
        }
    }

    public static void updateFolderHistoryPaths(String nextFolderEntryPointPath, boolean initNewFileTree) {
        if(nextFolderEntryPointPath == null || nextFolderEntryPointPath.equals("")) {
            nextFolderEntryPointPath = EMPTY_FOLDER_HISTORY_PATH;
        }
        else {
            nextFolderEntryPointPath = String.format(Locale.getDefault(), "➤  %s", nextFolderEntryPointPath);
        }
        if(initNewFileTree) {
            folderHistoryTwoBackPath = EMPTY_FOLDER_HISTORY_PATH;
            folderHistoryOneBackPath = nextFolderEntryPointPath;
        }
        else {
            folderHistoryTwoBackPath = folderHistoryOneBackPath;
            folderHistoryOneBackPath = nextFolderEntryPointPath;
        }
        DisplayFragments.FolderNavigationFragment.dirsOneBackText.setText(folderHistoryOneBackPath);
    }

    public static void backupFolderHistoryPaths() {
        folderHistoryOneBackPath = folderHistoryTwoBackPath;
        folderHistoryTwoBackPath = EMPTY_FOLDER_HISTORY_PATH;
        DisplayFragments.FolderNavigationFragment.dirsOneBackText.setText(folderHistoryOneBackPath);
    }

    public void cancelAllOperationsInProgress() {
        FileChooserActivity.getInstance().stopPrefetchFileUpdatesThread();
        pathHistoryStack.clear();
    }

}
