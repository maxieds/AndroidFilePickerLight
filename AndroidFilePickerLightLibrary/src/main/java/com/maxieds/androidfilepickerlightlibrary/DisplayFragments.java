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

public class DisplayFragments {

    private static String LOGTAG = DisplayFragments.class.getSimpleName();

    public static class FileListItemFragment {

        private View layoutContainer;
        private FileTypes.FileType localFileItem;
        private int displayPositionIndex;
        private boolean isCheckable;
        public static RecyclerView mainFileListRecyclerView;
        public static DisplayAdapters.FileListAdapter rvAdapter;
        public static RecyclerView.LayoutManager rvLayoutManager;

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
            recyclerViewDisplay.setHasFixedSize(true); // TODO: Check if this causes errors ...
            rvLayoutManager = new LinearLayoutManager(FileChooserActivity.getInstance());
            recyclerViewDisplay.setLayoutManager(rvLayoutManager);
            rvAdapter = new DisplayAdapters.FileListAdapter(new ArrayList<String>()); // TODO: Is this where we need to initialize the View with the initial dir contents?
            recyclerViewDisplay.setAdapter(rvAdapter);
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
                     FileTypes.DirectoryResultContext.descendIntoNextDirectory();
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
        // (Not sure if this violates the premise of calling things by Intent only ??? )
    }

}
