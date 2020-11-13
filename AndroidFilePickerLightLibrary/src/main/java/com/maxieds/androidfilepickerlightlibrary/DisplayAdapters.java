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

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DisplayAdapters {

    private static String LOGTAG = DisplayAdapters.class.getSimpleName();

    public static class FileListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

        private static String LOGTAG = FileListAdapter.class.getSimpleName();

        private List<String> fileListData;
        private List<DisplayTypes.FileType> fileItemsData;

        public FileListAdapter(List<String> nextFileListData, List<DisplayTypes.FileType> nextFileItemsData) {
            this.fileListData = new ArrayList<String>();
            this.fileItemsData = new ArrayList<DisplayTypes.FileType>();
            this.setHasStableIds(true);
            reloadDataSets(nextFileListData, nextFileItemsData, false);
        }

        public void reloadDataSets(List<String> nextDataSet, List<DisplayTypes.FileType> nextFileItemsData, boolean notifyAdapter) {
            if(fileListData != nextDataSet) {
                fileListData.clear();
                fileListData.addAll(nextDataSet);
            }
            if(fileItemsData != nextFileItemsData) {
                fileItemsData.clear();
                fileItemsData.addAll(nextFileItemsData);
            }
            if(notifyAdapter) {
                notifyDataSetChanged();
            }
        }

        public void reloadDataSets(List<String> nextDataSet, List<DisplayTypes.FileType> nextFileItemsData) {
            reloadDataSets(nextDataSet, nextFileItemsData, true);
        }

        public DisplayTypes.FileType getFileItemByIndex(int indexPos) {
            if(indexPos < 0 || indexPos >= fileItemsData.size()) {
                return null;
            }
            return fileItemsData.get(indexPos);
        }

        private static final int VIEW_TYPE_FILE_ITEM = 0;

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //Log.i(LOGTAG,"onCreateViewHolder");
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_file_entry_item, parent, false);
            return new BaseViewHolder(rowItem);
        }

        @Override
        public void onBindViewHolder(BaseViewHolder bvHolder, int posIndex) {
            bvHolder.setInitialIndexPosition(posIndex);
            if(!fileItemsData.isEmpty()) {
                DisplayTypes.FileType fileItem = fileItemsData.get(posIndex);
                fileItem.setLayoutContainer(bvHolder.getMainViewLayoutContainer());
                View viewItemContainer = bvHolder.getMainViewLayoutContainer();
                DisplayFragments.FileListItemFragment.resetLayout(viewItemContainer, fileItem, posIndex);
            }
            //Log.i(LOGTAG, String.format(Locale.getDefault(), "onBindViewHolder @ %d -- (ADAPTER -> %s) [DATA ITEMS SIZE = %d]", posIndex,
            //        fileListData.get(posIndex), fileItemsData.size()));
        }

        @Override
        public void onViewRecycled(BaseViewHolder bvHolder) {
            //Log.i(LOGTAG,"onViewRecycled: " + bvHolder + " AT initial INDEX " + bvHolder.getInitialIndexPosition());
        }

        @Override
        public void onViewDetachedFromWindow(BaseViewHolder bvHolder) {
            //Log.i(LOGTAG,"onViewDetachedFromWindow: " + bvHolder + " AT initial INDEX " + bvHolder.getInitialIndexPosition());
        }

        @Override
        public void onViewAttachedToWindow(BaseViewHolder bvHolder) {
            //Log.i(LOGTAG,"onViewAttachedToWindow: " + bvHolder + " AT initial INDEX " + bvHolder.getInitialIndexPosition());
        }

        @Override
        public int getItemCount() {
            return fileListData.size();
        }

        @Override
        public long getItemId(int posIndex) {
            if(posIndex < fileListData.size()) {
                return fileListData.get(posIndex).hashCode();
            }
            return posIndex;
        }

        @Override
        public int getItemViewType(int posIndex) {
            return posIndex;
        }

    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, RecyclerView.OnItemTouchListener {

        private static String LOGTAG = BaseViewHolder.class.getSimpleName();

        private View fileItemContainerView;
        private int initIndexPos;

        private GestureDetector gestureDetector = new GestureDetector(FileChooserActivity.getInstance(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }
            @Override
            public void onLongPress(MotionEvent e) {
                Log.i(LOGTAG, "BaseViewHolder::GestureDetector::onLongPress");
                RecyclerView mainFileListRecyclerView = DisplayFragments.getMainRecyclerView();
                View childView = mainFileListRecyclerView.findChildViewUnder(e.getX(), e.getY());
                if(childView != null) {
                    onLongClick(childView);
                }
            }
        });

        public BaseViewHolder(View v) {
            super(v);
            fileItemContainerView = v;
            v.setClickable(true);
            v.setFocusable(true);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            initIndexPos = -1;

        }

        public int getInitialIndexPosition() { return initIndexPos; }
        public void setInitialIndexPosition(int initIdx) { initIndexPos = initIdx; }

        public View getMainViewLayoutContainer() { return fileItemContainerView; }

        private static int getPositionForView(View v) {
            DisplayFragments displayCtx = DisplayFragments.getInstance();
            FileChooserRecyclerView mainRV = displayCtx.getMainRecyclerView();
            BaseViewHolder bvHolder = (BaseViewHolder) mainRV.getChildViewHolder(v);
            if(bvHolder == null) {
                return -1;
            }
            return bvHolder.getInitialIndexPosition();
        }

        public static DisplayTypes.FileType getFileItemForView(View v) {
            DisplayFragments displayCtx = DisplayFragments.getInstance();
            FileChooserRecyclerView mainRV = displayCtx.getMainRecyclerView();
            DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();
            return rvAdapter.getFileItemByIndex(getPositionForView(v));
        }

        @Override
        public void onClick(View v) {
            Log.i(LOGTAG, "BaseViewHolder::onClick");
            onLongClick(v);
        }

        @Override
        public boolean onLongClick(View v) {
            Log.i(LOGTAG, "BaseViewHolder::onLongClick");
            DisplayTypes.FileType fileItem = getFileItemForView(v);
            if(fileItem != null && fileItem.isDirectory()) {
                // Recursively descend into the clicked directory location:
                DisplayTypes.DirectoryResultContext nextFolder = fileItem.getParentFolderContext();
                if(nextFolder == null) {
                    return false;
                }
                int fileItemPosIndex = getPositionForView(v);
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
            /*else if(fileItem != null && !fileItem.isDirectory()) {
                View cbView = v.findViewById(R.id.fileSelectCheckBox);
                if(cbView == null) {
                    return false;
                }
                CheckBox selectionBox = (CheckBox) cbView;
                if(!selectionBox.isEnabled()) {
                    return false;
                }
                boolean isPrevSelected = selectionBox.isSelected();
                return (isPrevSelected == !selectionBox.performClick()); // if the click performed changed the selection, then success
            }*/
            return false;
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rview, MotionEvent mevt) {
            View childView = rview.findChildViewUnder(mevt.getX(), mevt.getY());
            if(childView != null && gestureDetector.onTouchEvent(mevt)) {
                onClick(childView);
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rview, MotionEvent mevt) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean state) {}

    }

    public static class OnSelectListener implements CompoundButton.OnCheckedChangeListener {

        private static String LOGTAG = OnSelectListener.class.getSimpleName();

        public static boolean performNewFileItemClick(CheckBox cbView, DisplayTypes.FileType fileItem) {
            Log.i(LOGTAG, String.format(Locale.getDefault(), "INIT PERFORM CLICK: (selected, max allowed) = (%d, %d)",
                    DisplayFragments.getInstance().curSelectionCount, DisplayFragments.getInstance().maxAllowedSelections));
            if(cbView == null || fileItem == null) {
                return false;
            }
            boolean isDir = fileItem.isDirectory();
            if(!isDir && !DisplayFragments.getInstance().allowSelectFiles) {
                Log.i(LOGTAG, "Blocking FILE item selection I");
                cbView.setChecked(false);
                cbView.jumpDrawablesToCurrentState(); // No animations
                return false;
            }
            else if(isDir && !DisplayFragments.getInstance().allowSelectFolders) {
                Log.i(LOGTAG, "Blocking DIR item selection II");
                cbView.setChecked(false);
                cbView.jumpDrawablesToCurrentState();
                return false;
            }
            if(!cbView.isEnabled()) {
                cbView.setChecked(false);
                cbView.jumpDrawablesToCurrentState();
                return false;
            }
            if(fileItem.isChecked()) {
                // Deselect: uncheck GUI widget item and remove the fileItem from the active selections list:
                cbView.setChecked(false);
                cbView.jumpDrawablesToCurrentState();
                fileItem.setChecked(false);
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
            cbView.setChecked(true);
            cbView.jumpDrawablesToCurrentState();
            fileItem.setChecked(true);
            cbView.setEnabled(true);
            DisplayFragments.getInstance().activeSelectionsList.add(fileItem);
            DisplayFragments.getInstance().curSelectionCount++;
            Log.i(LOGTAG, "Selected next checkbox (file item)");
            Log.i(LOGTAG, String.format(Locale.getDefault(), "RETURNING PERFORM CLICK: (selected, max allowed) = (%d, %d)",
                    DisplayFragments.getInstance().curSelectionCount, DisplayFragments.getInstance().maxAllowedSelections));
            return true;
        }

        @Override
        public void onCheckedChanged(CompoundButton btnView, boolean isChecked) {
            btnView.jumpDrawablesToCurrentState();
            CheckBox cbView = (CheckBox) btnView;
            DisplayTypes.FileType fileItem = BaseViewHolder.getFileItemForView((View) btnView.getParent().getParent());
            if(fileItem != null) {
                performNewFileItemClick(cbView, fileItem);
            }
        }

    }

}
