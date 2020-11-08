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

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DisplayAdapters {

    private static String LOGTAG = DisplayAdapters.class.getSimpleName();

    public static class FileListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

        private List<String> fileListData;
        private List<DisplayTypes.FileType> fileItemsData;

        public FileListAdapter(List<String> nextFileListData, List<DisplayTypes.FileType> nextFileItemsData) {
            this.fileListData = new ArrayList<String>();
            this.fileListData.addAll(nextFileListData);
            this.fileItemsData = new ArrayList<DisplayTypes.FileType>();
            this.fileItemsData.addAll(nextFileItemsData);
            notifyDataSetChanged();
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.i(LOGTAG,"onCreateViewHolder");
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_file_entry_item, parent, false);
            return new BaseViewHolder(rowItem);
        }

        @Override
        public void onBindViewHolder(BaseViewHolder bvHolder, int posIndex) {
            Log.i(LOGTAG,"onCreateViewHolder @ " + posIndex);
            bvHolder.getDisplayText().setText(fileListData.get(posIndex));
            Log.i(LOGTAG, String.format(Locale.getDefault(), "onBindViewHolder @ %d -- %s", posIndex, bvHolder.getDisplayText().getText()));
            if(!fileItemsData.isEmpty()) {
                DisplayTypes.FileType fileItem = fileItemsData.get(posIndex);
                bvHolder.setFileItemData(fileItem);
                View viewItemContainer = bvHolder.getMainViewLayoutContainer();
                DisplayFragments.FileListItemFragment.resetLayout(viewItemContainer, fileItem, posIndex);
           }
        }

        @Override
        public void onViewRecycled(BaseViewHolder bvHolder){
            Log.i(LOGTAG,"onViewRecycled: " + bvHolder);
        }

        @Override
        public void onViewDetachedFromWindow(BaseViewHolder bvHolder){
            Log.i(LOGTAG,"onViewDetachedFromWindow: " + bvHolder);
        }

        @Override
        public void onViewAttachedToWindow(BaseViewHolder bvHolder){
            Log.i(LOGTAG,"onViewAttachedToWindow: " + bvHolder);
        }

        @Override
        public int getItemCount() {
            return fileListData.size();
        }

    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, RecyclerView.OnItemTouchListener {

        private View fileItemContainerView;
        public  View iconView;
        public  TextView displayText;
        public  DisplayTypes.FileType fileItem;

        private GestureDetector gestureDetector = new GestureDetector(FileChooserActivity.getInstance(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
            @Override
            public void onLongPress(MotionEvent e) {
                RecyclerView mainFileListRecyclerView = DisplayFragments.getMainRecyclerView();
                View childView = mainFileListRecyclerView.findChildViewUnder(e.getX(), e.getY());
                if(childView != null) {
                    ////clickListener.onLongClick(child, DisplayFragments.mainFileListRecyclerView, DisplayFragments.mainFileListRecyclerView.getChildPosition(child));
                    onLongClick(childView);
                }
            }
        });

        public BaseViewHolder(View v) {
            super(v);
            fileItemContainerView = v;
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            //iconView = v.findViewById(R.id.fileTypeIcon);
            displayText = (TextView) v.findViewById(R.id.fileEntryBaseName);
        }

        public void setFileItemData(DisplayTypes.FileType storedFileItem) {
            fileItem = storedFileItem;
        }

        public DisplayTypes.FileType getFileItemReference() {
            return fileItem;
        }

        public TextView getDisplayText() { return displayText; }

        public View getMainViewLayoutContainer() { return fileItemContainerView; }

        public boolean performNewFileItemClick(DisplayTypes.FileType fileItem) {
            return performNewFileItemClick(fileItem.getLayoutContainer().findViewById(R.id.fileSelectCheckBox), fileItem);
        }

        public static boolean performNewFileItemClick(CheckBox cbView, DisplayTypes.FileType fileItem) {
            if(fileItem == null) {
                return false;
            }
            boolean isDir = fileItem.isDirectory();
            if(!isDir && !DisplayFragments.allowSelectFiles) {
                Log.i(LOGTAG, "Blocking file item selection I");
                return false;
            }
            else if(isDir && !DisplayFragments.allowSelectFolders) {
                Log.i(LOGTAG, "Blocking file item selection II");
                return false;
            }
            CheckBox selectionMarker = cbView;
            if(!cbView.isEnabled()) {
                return false;
            }
            if(fileItem.isChecked()) {
                // Deselect: uncheck GUI widget item and remove the fileItem from the active selections list:
                fileItem.setChecked(false);
                selectionMarker.setChecked(false);
                selectionMarker.setEnabled(true);
                DisplayFragments.activeSelectionsList.remove(fileItem);
                DisplayFragments.curSelectionCount--;
                Log.i(LOGTAG, "DE-Selected next checkbox (file item)");
                return true;
            }
            else if(DisplayFragments.curSelectionCount >= DisplayFragments.maxAllowedSelections) {
                return false;
            }
            fileItem.setChecked(true);
            selectionMarker.setChecked(true);
            selectionMarker.setEnabled(true);
            DisplayFragments.activeSelectionsList.add(fileItem);
            DisplayFragments.curSelectionCount++;
            Log.i(LOGTAG, "Selected next checkbox (file item)");
            return true;
        }

        @Override
        public void onClick(View v) {
            if(fileItem != null && (!fileItem.isDirectory() || DisplayFragments.allowSelectFolders)) {
                if(performNewFileItemClick(fileItem)) {
                    String filePathType = fileItem.isDirectory() ? "DIR" : "FILE";
                    String displaySelectMsg = String.format(Locale.getDefault(), "Selected %s \"%s\".", filePathType, fileItem.getBaseName());
                    DisplayUtils.displayToastMessageShort(displaySelectMsg);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(fileItem != null && !fileItem.isDirectory()) {
                if(performNewFileItemClick(fileItem)) {
                    String displaySelectMsg = String.format(Locale.getDefault(), "Selected FILE \"%s\".", fileItem.getBaseName());
                    DisplayUtils.displayToastMessageShort(displaySelectMsg);
                    return true;
                }
                return false;
            }
            // Otherwise, descend recursively into the clicked directory location:
            if(fileItem != null) {
                DisplayTypes.DirectoryResultContext nextFolder = fileItem.getParentFolderContext();
                if(nextFolder == null) {
                    return false;
                }
                DisplayTypes.DirectoryResultContext workingFolder = DisplayTypes.DirectoryResultContext.pathHistoryStack.peek();
                DisplayTypes.DirectoryResultContext.pathHistoryStack.push(nextFolder);
                if(workingFolder == null) {
                    workingFolder.loadNextFolderAtIndex(true);
                    DisplayFragments.descendIntoNextDirectory(true);
                }
                else {
                    workingFolder.loadNextFolderAtIndex(fileItem.getRelativeCursorPosition(), false);
                    DisplayFragments.descendIntoNextDirectory(false);
                }
                String displayRecurseMsg = String.format(Locale.getDefault(), "Descending recursively into DIR \"%s\".", fileItem.getBaseName());
                DisplayUtils.displayToastMessageShort(displayRecurseMsg);
                return true;
            }
            return false;
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rview, MotionEvent mevt) {
            View childView = rview.findChildViewUnder(mevt.getX(), mevt.getY());
            if(childView != null && gestureDetector.onTouchEvent(mevt)) {
                ////clickListener.onClick(child, rview.getChildPosition(child));
                onClick(childView);
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rview, MotionEvent mevt) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean state) {}

    }

}
