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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DisplayFragments {

    private static String LOGTAG = DisplayFragments.class.getSimpleName();

    public static RecyclerView mainFileListRecyclerView = null;
    public static RecyclerView.LayoutManager rvLayoutManager = null;
    public static DisplayAdapters.FileListAdapter rvAdapter = null;
    public static boolean recyclerViewAdapterInit = false;

    public static int maxAllowedSelections = 0;
    public static int curSelectionCount = 0;
    public static boolean allowSelectFiles = true;
    public static boolean allowSelectFolders = true;
    public static List<FileTypes.FileType> activeSelectionsList = new ArrayList<FileTypes.FileType>();

    public static FileFilter.FileFilterInterface localFilesListFilter = null;
    public static FileTypes.FileItemsListSortFunc localFilesListSortFunc = null;

    public static void descendIntoNextDirectory() {
        if(FileTypes.DirectoryResultContext.pathHistoryStack.empty()) {
            DisplayFragments.cancelAllOperationsInProgress();
            FileChooserActivity.getInstance().postSelectedFilesActivityResult();
        }
        FileTypes.DirectoryResultContext lastWorkingDir = FileTypes.DirectoryResultContext.pathHistoryStack.pop();
        lastWorkingDir.computeDirectoryContents();
        DisplayFragments.displayNextDirectoryFilesList(lastWorkingDir.getWorkingDirectoryContents());
    }

    /* Re-initiate the inquisition: Static reusable wrapper function to invoke loading a new directory
     * from scratch (reinitializing objects, starting the initial root query, and launching the
     * RecyclerView pattern making compendia on a whole new dataset):
     */
    public static void initiateNewFolderLoad(FileChooserBuilder.BaseFolderPathType initBaseFolder) {
        FileTypes.DirectoryResultContext newCwdContext = FileTypes.DirectoryResultContext.probeAtCursoryFolderQuery(initBaseFolder);
        FileTypes.DirectoryResultContext.pathHistoryStack.push(newCwdContext);
        DisplayFragments.displayNextDirectoryFilesList(newCwdContext.getWorkingDirectoryContents());
    }

    public static void displayNextDirectoryFilesList(List<FileTypes.FileType> workingDirContentsList) {
        List<FileTypes.FileType> filteredFileContents = FileChooserBuilder.filterAndSortFileItemsList(workingDirContentsList, localFilesListFilter, localFilesListSortFunc);
        if(!recyclerViewAdapterInit) {
            List<String> fileItemBasePathsList = new ArrayList<String>();
            for(FileTypes.FileType fileItem : filteredFileContents) {
                fileItemBasePathsList.add(fileItem.getBaseName());
            }
            rvAdapter = new DisplayAdapters.FileListAdapter(fileItemBasePathsList);
            mainFileListRecyclerView.setAdapter(rvAdapter);
            recyclerViewAdapterInit = true;
        }
        DisplayFragments.FolderNavigationFragment.dirsOneBackText.setText("----");
        DisplayFragments.FolderNavigationFragment.dirsTwoBackText.setText("----");
        activeSelectionsList.clear();
        mainFileListRecyclerView.removeAllViews();
        rvAdapter.notifyDataSetChanged();
        int fileItemIndex = 0;
        for(FileTypes.FileType fileItem : filteredFileContents) {
            DisplayFragments.FileListItemFragment fileItemUIFragment = new DisplayFragments.FileListItemFragment(fileItem, fileItemIndex);
            fileItem.setLayoutContainer(fileItemUIFragment.getLayoutContainer());
            mainFileListRecyclerView.addView(fileItemUIFragment.getLayoutContainer());
        }
        rvAdapter.notifyDataSetChanged();
    }

    public static class FileListItemFragment {

        private View layoutContainer;
        private FileTypes.FileType localFileItem;
        private int displayPositionIndex;
        private boolean isCheckable;

        public FileListItemFragment(FileTypes.FileType fileItem, int displayPosition) {
            displayPositionIndex = displayPosition;
            isCheckable = true;
            localFileItem = fileItem;
            layoutContainer = View.inflate(FileChooserActivity.getInstance(), R.layout.single_file_entry_item, null);
            resetLayout(fileItem, displayPositionIndex);
        }

        public static void configureStaticInstanceMembers(View mainContainerLayout) {
            RecyclerView recyclerViewDisplay = (RecyclerView) FileChooserActivity.getInstance().findViewById(R.id.mainRecyclerViewContainer);
            mainFileListRecyclerView = recyclerViewDisplay;
            rvLayoutManager = new LinearLayoutManager(FileChooserActivity.getInstance());
            recyclerViewDisplay.setLayoutManager(rvLayoutManager);
        }

        public void resetLayout(FileTypes.FileType fileItem, int displayPosition) {
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
            dirsTwoBackText.setText("----");
            dirsOneBackText.setText("----");
            Button.OnClickListener backBtnClickListener = new Button.OnClickListener() {
                @Override
                public void onClick(View btnView) {
                     DisplayFragments.descendIntoNextDirectory();
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
