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

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DisplayFragments {

    private static String LOGTAG = DisplayFragments.class.getSimpleName();

    public static RecyclerView mainFileListRecyclerView = null;
    public static LinearLayoutManager rvLayoutManager = null;
    public static DisplayAdapters.FileListAdapter rvAdapter = null;
    public static boolean recyclerViewAdapterInit = false;

    public static int maxAllowedSelections = 0;
    public static int curSelectionCount = 0;
    public static boolean allowSelectFiles = true;
    public static boolean allowSelectFolders = true;

    public static  List<DisplayTypes.FileType> activeSelectionsList = new ArrayList<DisplayTypes.FileType>();
    private static List<String> fileItemBasePathsList = new ArrayList<String>();

    private static final String EMPTY_FOLDER_HISTORY_PATH = "----";

    private static String folderHistoryOneBackPath = EMPTY_FOLDER_HISTORY_PATH;
    private static String folderHistoryTwoBackPath = EMPTY_FOLDER_HISTORY_PATH;

    public static void updateFolderHistoryPaths(String nextFolderEntryPointPath, boolean initNewFileTree) {
        if(nextFolderEntryPointPath == null) {
            nextFolderEntryPointPath = EMPTY_FOLDER_HISTORY_PATH;
        }
        if(initNewFileTree) {
            folderHistoryTwoBackPath = EMPTY_FOLDER_HISTORY_PATH;
            folderHistoryOneBackPath = nextFolderEntryPointPath;
        }
        else {
            folderHistoryTwoBackPath = folderHistoryOneBackPath;
            folderHistoryOneBackPath = nextFolderEntryPointPath;
        }
    }

    public static void initializeRecyclerViewLayout(RecyclerView rview) {
        if(!recyclerViewAdapterInit) {
            mainFileListRecyclerView = rview;
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            mainFileListRecyclerView.setLayoutParams(layoutParams);
            rvLayoutManager = new LinearLayoutManager(FileChooserActivity.getInstance());
            rvLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mainFileListRecyclerView.setLayoutManager(rvLayoutManager);
            rvAdapter = new DisplayAdapters.FileListAdapter(fileItemBasePathsList);
            mainFileListRecyclerView.setAdapter(rvAdapter);
            recyclerViewAdapterInit = true;
        }
    }

    public static FileFilter.FileFilterInterface localFilesListFilter = null;
    public static FileFilter.FileItemsListSortFunc localFilesListSortFunc = null;

    public static void descendIntoNextDirectory(boolean initNewFileTree) {
        if(DisplayTypes.DirectoryResultContext.pathHistoryStack.empty()) {
            DisplayFragments.cancelAllOperationsInProgress();
            FileChooserException.GenericRuntimeErrorException rte = new FileChooserException.GenericRuntimeErrorException("Empty context for folder history (no back?)");
            FileChooserActivity.getInstance().postSelectedFilesActivityResult(rte);
        }
        DisplayTypes.DirectoryResultContext nextFolder = DisplayTypes.DirectoryResultContext.pathHistoryStack.peek();
        nextFolder.computeDirectoryContents();
        DisplayFragments.displayNextDirectoryFilesList(nextFolder.getWorkingDirectoryContents());
    }

    /* Re-initiate the inquisition: Static reusable wrapper function to invoke loading a new directory
     * from scratch (reinitializing objects, starting the initial root query, and launching the
     * RecyclerView pattern making compendia on a whole new dataset):
     */
    public static void initiateNewFolderLoad(FileChooserBuilder.BaseFolderPathType initBaseFolder) {
        DisplayTypes.DirectoryResultContext newCwdContext = DisplayTypes.DirectoryResultContext.probeAtCursoryFolderQuery(initBaseFolder);
        DisplayTypes.DirectoryResultContext.pathHistoryStack.push(newCwdContext);
        newCwdContext.loadNextFolderAtIndex(0, true);
        DisplayFragments.displayNextDirectoryFilesList(newCwdContext.getWorkingDirectoryContents());
    }

    public static void displayNextDirectoryFilesList(List<DisplayTypes.FileType> workingDirContentsList) {

        if(!recyclerViewAdapterInit) {
            initializeRecyclerViewLayout(mainFileListRecyclerView);
        }
        DisplayFragments.FolderNavigationFragment.dirsOneBackText.setText(folderHistoryOneBackPath);
        DisplayFragments.FolderNavigationFragment.dirsTwoBackText.setText(folderHistoryTwoBackPath);
        activeSelectionsList.clear();
        fileItemBasePathsList.clear();
        rvAdapter.notifyDataSetChanged();

        List<DisplayTypes.FileType> filteredFileContents = FileChooserBuilder.filterAndSortFileItemsList(workingDirContentsList, localFilesListFilter, localFilesListSortFunc);
        fileItemBasePathsList.clear();
        rvLayoutManager.removeAllViews();

        int fileItemIndex = 0;
        for(DisplayTypes.FileType fileItem : filteredFileContents) {
            fileItemBasePathsList.add(fileItem.getBaseName());
            rvAdapter.notifyDataSetChanged();
            DisplayFragments.FileListItemFragment fileItemUIFragment = new DisplayFragments.FileListItemFragment(fileItem, fileItemIndex);
            fileItem.setLayoutContainer(fileItemUIFragment.getLayoutContainer());
            DisplayAdapters.BaseViewHolder viewHolderAtIndex = rvAdapter.createViewHolder((ViewGroup) fileItemUIFragment.getLayoutContainer(), 0);
            viewHolderAtIndex.setFileItemData(fileItem);
            rvAdapter.bindViewHolder(viewHolderAtIndex, fileItemIndex);
        }

    }

    public static class FileListItemFragment {

        private View layoutContainer;
        private DisplayTypes.FileType localFileItem;
        private int displayPositionIndex;
        private boolean isCheckable;

        public FileListItemFragment(DisplayTypes.FileType fileItem, int displayPosition) {
            displayPositionIndex = displayPosition;
            isCheckable = true;
            localFileItem = fileItem;
            layoutContainer = View.inflate(FileChooserActivity.getInstance(), R.layout.single_file_entry_item, null);
            TextView fileSizeText = layoutContainer.findViewById(R.id.fileEntrySizeText);
            TextView filePermsText = layoutContainer.findViewById(R.id.fileEntryPermsSummaryText);
            if(fileSizeText != null) {
                fileSizeText.setText(fileItem.getFileSizeString());
            }
            if(filePermsText != null) {
                filePermsText.setText(fileItem.getChmodStylePermissions());
            }
            resetLayout(fileItem, displayPositionIndex);
        }

        public void resetLayout(DisplayTypes.FileType fileItem, int displayPosition) {
            displayPositionIndex = displayPosition;
            ImageView fileTypeIcon = layoutContainer.findViewById(R.id.fileTypeIcon);
            fileTypeIcon.setImageDrawable(localFileItem.getFileTypeIcon());
            TextView fileSizeText = layoutContainer.findViewById(R.id.fileEntrySizeText);
            fileSizeText.setText(localFileItem.getFileSizeString());
            TextView filePermsSummary = layoutContainer.findViewById(R.id.fileEntryPermsSummaryText);
            filePermsSummary.setText(localFileItem.getChmodStylePermissions());
            TextView fileBaseNameDisplayText = layoutContainer.findViewById(R.id.fileEntryBaseName);
            fileBaseNameDisplayText.setText(localFileItem.getBaseName());
            CheckBox selectFileCheckBox = layoutContainer.findViewById(R.id.fileSelectCheckBox);
            if(!isCheckable) {
                selectFileCheckBox.setEnabled(false);
                selectFileCheckBox.setVisibility(View.INVISIBLE);
            }
        }

        public void setSelectable(boolean enabled, boolean updateUI) {
            isCheckable = enabled;
            if(updateUI && !enabled) {
                CheckBox selectFileCheckBox = layoutContainer.findViewById(R.id.fileSelectCheckBox);
                selectFileCheckBox.setEnabled(false);
                selectFileCheckBox.setVisibility(View.INVISIBLE);
            }
            else if(updateUI) {
                CheckBox selectFileCheckBox = layoutContainer.findViewById(R.id.fileSelectCheckBox);
                selectFileCheckBox.setEnabled(true);
                selectFileCheckBox.setVisibility(View.VISIBLE);
            }
        }

        public View getLayoutContainer() {
            return layoutContainer;
        }

    }

    public static FolderNavigationFragment mainFolderNavFragment = null;

    public static class FolderNavigationFragment {

        private static FolderNavigationFragment folderNavFragmentStaticInst = null;
        public static TextView dirsTwoBackText = null;
        public static TextView dirsOneBackText = null;
        public static ImageButton globalNavBackBtn = null;

        public FolderNavigationFragment() {
            folderNavFragmentStaticInst = this;
        }

        public static FolderNavigationFragment createNewFolderNavFragment(View navBtnsContainerView) {
            FolderNavigationFragment folderNavFragment = new FolderNavigationFragment();
            dirsTwoBackText = FileChooserActivity.getInstance().findViewById(R.id.mainDirNavBackTwoPathDisplayText);
            dirsOneBackText = FileChooserActivity.getInstance().findViewById(R.id.mainDirNavBackOnePathDisplayText);
            globalNavBackBtn = FileChooserActivity.getInstance().findViewById(R.id.mainDirNavGlobalBackBtn);
            updateFolderHistoryPaths(null, true);
            Button.OnClickListener backBtnClickListener = new Button.OnClickListener() {
                @Override
                public void onClick(View btnView) {
                     Button navBtn = (Button) btnView;
                     // TODO: Get DirectoryResultContext for the button clicked, and push it atop the working history stack ...
                     DisplayTypes.DirectoryResultContext nextFolderCtx = null; // TODO: call DirResCtx.load* ...
                     DisplayTypes.DirectoryResultContext.pathHistoryStack.push(nextFolderCtx);
                     DisplayFragments.descendIntoNextDirectory(true);
                }
            };
            globalNavBackBtn.setOnClickListener(backBtnClickListener);
            return folderNavFragment;
        }
    }

    public static void cancelAllOperationsInProgress() {
        // TODO: Need a way to cleanup any hanging processes with the file system before quitting the activity ...
        // Can we run the FileProvider routines in a thread and then kill it if the user wants another op before
        // it has finished?
    }

}
