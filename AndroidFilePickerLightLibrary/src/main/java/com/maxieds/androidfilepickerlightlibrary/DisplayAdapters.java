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
            this.setHasStableIds(false); // TODO ???
            notifyDataSetChanged();
        }

        public void reloadDataSets(List<String> nextDataSet, List<DisplayTypes.FileType> nextFileItemsData) {
            fileListData.clear();
            fileListData.addAll(nextDataSet);
            fileItemsData.clear();
            fileItemsData.addAll(nextFileItemsData);
            notifyDataSetChanged();
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Log.i(LOGTAG,"onCreateViewHolder");
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_file_entry_item, parent, false);
            return new BaseViewHolder(rowItem);
        }

        @Override
        public void onBindViewHolder(BaseViewHolder bvHolder, int posIndex) {
            bvHolder.getDisplayText().setText(fileListData.get(posIndex));
            //Log.i(LOGTAG, String.format(Locale.getDefault(), "onBindViewHolder @ %d -- %s", posIndex, bvHolder.getDisplayText().getText()));
            if(!fileItemsData.isEmpty()) {
                DisplayTypes.FileType fileItem = fileItemsData.get(posIndex);
                fileItem.setLayoutContainer(bvHolder.getMainViewLayoutContainer());
                View viewItemContainer = bvHolder.getMainViewLayoutContainer();
                DisplayFragments.FileListItemFragment.resetLayout(viewItemContainer, fileItem, posIndex);
           }
        }

        @Override
        public void onViewRecycled(BaseViewHolder bvHolder){
            //Log.i(LOGTAG,"onViewRecycled: " + bvHolder);
        }

        @Override
        public void onViewDetachedFromWindow(BaseViewHolder bvHolder){
            //Log.i(LOGTAG,"onViewDetachedFromWindow: " + bvHolder);
        }

        @Override
        public void onViewAttachedToWindow(BaseViewHolder bvHolder){
            //Log.i(LOGTAG,"onViewAttachedToWindow: " + bvHolder);
        }

        @Override
        public int getItemCount() {
            return fileListData.size();
        }

        //@Override
        //public long getItemId(int posIndex) {
        //    return posIndex;
        //}

    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, RecyclerView.OnItemTouchListener {

        private View fileItemContainerView;
        public  TextView displayText;

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
            displayText = (TextView) v.findViewById(R.id.fileEntryBaseName);
            //setIsRecyclable(false);
        }

        public TextView getDisplayText() { return displayText; }

        public View getMainViewLayoutContainer() { return fileItemContainerView; }

        public boolean performNewFileItemClick(DisplayTypes.FileType fileItem) {
            if(fileItem != null && fileItem.getLayoutContainer() != null) {
                return performNewFileItemClick(fileItem.getLayoutContainer().findViewById(R.id.fileSelectCheckBox), fileItem);
            }
            return false;
        }

        public static boolean performNewFileItemClick(CheckBox cbView, DisplayTypes.FileType fileItem) {
            Log.i(LOGTAG, String.format(Locale.getDefault(), "INIT PERFORM CLICK: (selected, max allowed) = (%d, %d)",
                    DisplayFragments.getInstance().curSelectionCount, DisplayFragments.getInstance().maxAllowedSelections));
            if(cbView == null || fileItem == null) {
                return false;
            }
            boolean isDir = fileItem.isDirectory();
            if(!isDir && !DisplayFragments.getInstance().allowSelectFiles) {
                Log.i(LOGTAG, "Blocking file item selection I");
                cbView.setChecked(false);
                return false;
            }
            else if(isDir && !DisplayFragments.getInstance().allowSelectFolders) {
                Log.i(LOGTAG, "Blocking file item selection II");
                cbView.setChecked(false);
                return false;
            }
            if(!cbView.isEnabled()) {
                cbView.setChecked(false);
                return false;
            }
            if(fileItem.isChecked()) {
                // Deselect: uncheck GUI widget item and remove the fileItem from the active selections list:
                fileItem.setChecked(false);
                cbView.setChecked(false);
                cbView.setEnabled(true);
                DisplayFragments.getInstance().activeSelectionsList.remove(fileItem);
                DisplayFragments.getInstance().curSelectionCount--;
                Log.i(LOGTAG, "DE-Selected next checkbox (file item)");
                Log.i(LOGTAG, String.format(Locale.getDefault(), "RETURNING PERFORM CLICK: (selected, max allowed) = (%d, %d)",
                        DisplayFragments.getInstance().curSelectionCount, DisplayFragments.getInstance().maxAllowedSelections));
                return true;
            }
            else if(DisplayFragments.getInstance().curSelectionCount >= DisplayFragments.getInstance().maxAllowedSelections) {
                cbView.setChecked(false);
                return false;
            }
            fileItem.setChecked(true);
            cbView.setChecked(true);
            cbView.setEnabled(true);
            DisplayFragments.getInstance().activeSelectionsList.add(fileItem);
            DisplayFragments.getInstance().curSelectionCount++;
            Log.i(LOGTAG, "Selected next checkbox (file item)");
            Log.i(LOGTAG, String.format(Locale.getDefault(), "RETURNING PERFORM CLICK: (selected, max allowed) = (%d, %d)",
                    DisplayFragments.getInstance().curSelectionCount, DisplayFragments.getInstance().maxAllowedSelections));
            return true;
        }

        @Override
        public void onClick(View v) {
            Log.i(LOGTAG, "onClick");
            int fileItemPosIndex = DisplayFragments.getInstance().findFileItemIndexByLayout(v);
            if(fileItemPosIndex < 0) {
                return;
            }
            DisplayTypes.FileType fileItem = DisplayFragments.getInstance().activeFileItemsDataList.get(fileItemPosIndex);
            if(fileItem != null && (!fileItem.isDirectory() || DisplayFragments.getInstance().allowSelectFolders)) {
                if(performNewFileItemClick(fileItem)) {
                    String filePathType = fileItem.isDirectory() ? "DIR" : "FILE";
                    String displaySelectMsg = String.format(Locale.getDefault(), "Selected %s \"%s\".", filePathType, fileItem.getBaseName());
                    DisplayUtils.displayToastMessageShort(displaySelectMsg);
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            Log.i(LOGTAG, "onLongClick");
            int fileItemPosIndex = DisplayFragments.getInstance().findFileItemIndexByLayout(v);
            if(fileItemPosIndex < 0) {
                return false;
            }
            DisplayTypes.FileType fileItem = DisplayFragments.getInstance().activeFileItemsDataList.get(fileItemPosIndex);
            if(fileItem == null || !fileItem.isDirectory()) {
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
                DisplayTypes.DirectoryResultContext workingFolder = DisplayFragments.getInstance().pathHistoryStack.peek();
                DisplayFragments.getInstance().pathHistoryStack.push(nextFolder);
                if(workingFolder == null) {
                    nextFolder.loadNextFolderAtIndex(fileItemPosIndex, true);
                    DisplayFragments.getInstance().descendIntoNextDirectory(true);
                }
                else {
                    nextFolder.loadNextFolderAtIndex(fileItemPosIndex, false);
                    DisplayFragments.getInstance().descendIntoNextDirectory(false);
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
