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

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/*
 * The logic in this class should be the reason the scolling action
 * in the RecyclerView used to display the files listings graphically works well.
 * The idea and problem is the following: In a large directory of image screenshots
 * (several GB worth, and spread across ~100 entries), it is infeasible to load all of the
 * files to list into the UI layout views at once. The standard solution for this on
 * Android is approximated by the use of a RecyclerView. The semi non-standard usage of this
 * functionality requires fast, fling-based scrolling through the large file list.
 * To be release-type production quality, at minimum, it has to work, look good, and not
 * freeze with perplexing runtime errors.
 *
 * The initial approach, which may well be acceptable for smaller sized data sets that do not move around
 * much, suggests to update the RecyclerView within an OnScroll handler for the View.
 * What results is jarring graphical presentation delays and hard-to-debug issues with the
 * view inserting items in underspecified orders (and then some). The obvious solution to detect
 * when we are at the top (resp.) bottom edges of the layout and of the need to load more files to show the
 * user by observing the raw scroller data DOES NOT work! Complicating the task at hand is the fact that
 * the exposed APIs for getting this type of raw scroller data are limited, e.g., see the methods available
 * to track a LinearSmoothScroller object once it is active.
 *
 * The next link is enlightening, but still very much shy on a solution that works for this use case:
 * https://medium.com/google-developers/recyclerview-prefetch-c2f269075710
 * (Maybe Android needs a mature container class API in 11 for this so that developers need only specify a
 * layout to perform this type of functional pattern with their data, as usual ... )
 *
 * This class views the situation as (visualized in a horizontal orientation view):
 * | -- Top Buffered Views (not visible) -- | <RecyclerView items that are visible> | -- Bottom Buffered Views (also not visible) -- |
 * Since fast paced flings can happen quickly in either direction while the user is scrolling, we make a "balanced" design
 * heuristic to try to keep an evenly balanced (by size) buffer of file items to the left (right) / top (bottom).
 * With the FileProvider required in recent Android 10-11+, and the heavy layout presence, it takes a noticeable
 * lag to feed in the scroller requested views in realtime (c.f., the short pause in launching the File Chooser from the
 * demo application). The next Thread derivative runner / utility class operates by periodically polling the state of the
 * RecyclerView's LayoutManager to see where the the first and last visible item indices are positioned in the RecyclerView.
 * The balancing operation is maintained by (pre)appending fresh items to balance out a top versus bottom heavy new scroller
 * result with a new chunk of new files (loaded in small sized sublists). Then we trim the fat, so to speak, off of the other side,
 * and post notifications to the adapter. The scroller (and its listener) know what to do by default when it can just
 * smooth scroll through a linear layout with items already available. What results is a nothing too fancy interface to
 * avoid the messy UI intensive work in the scroll handler.
 *
 */
public class PrefetchFilesUpdater extends Thread implements FileChooserRecyclerView.RecyclerViewSlidingContextWindow {

    private static String LOGTAG = PrefetchFilesUpdater.class.getSimpleName();

    public static class UpdateDataStruct {

        public enum UpdateDataType {

            ACTION_NONE,
            PREPEND_DATA_AT_TOP,
            APPEND_DATA_TO_BOTTOM;

            public boolean equals(UpdateDataType rhsDataType) {
                return this.ordinal() == rhsDataType.ordinal();
            }

            private static HashMap<String, UpdateDataType> dataTypeByNameMap = new HashMap<>();
            private static HashMap<Integer, UpdateDataType> dataTypeByIndex = new HashMap<>();
            static {
                for(UpdateDataType dataType : values()) {
                    dataTypeByNameMap.put(dataType.name(), dataType);
                    dataTypeByIndex.put(dataType.ordinal(), dataType);
                }
            }

            public static UpdateDataType getUpdateDataTypeByName(String constName) {
                return dataTypeByNameMap.get(constName);
            }

            public static UpdateDataType getUpdateDataTypeByIndex(int indexPos) {
                return dataTypeByIndex.get(Integer.valueOf(indexPos));
            }

        }

        public UpdateDataType postedDataBlockType;
        public List<String> nextFileNamesList;
        public List<DisplayTypes.FileType> nextFileItemsList;

        public UpdateDataStruct(UpdateDataType dataType, List<String> fileNames, List<DisplayTypes.FileType> fileItems) {
            postedDataBlockType = dataType;
            nextFileNamesList = fileNames;
            nextFileItemsList = fileItems;
        }

    }

    private int BalancedBufferSize;
    private int topBufferSize;
    private int bottomBufferSize;
    private boolean isInit;

    public PrefetchFilesUpdater() {

        // Set a sane default with some scroll buffer space:
        // My testing Android phone comfortably fits 12-15 layout items.
        // Let's buffer in 35 by default to not have to restruct the scroller
        // and default fling velocities too much:
        BalancedBufferSize = 35;
        topBufferSize = bottomBufferSize = 0;
        isInit = false;

        setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread threadRef, Throwable ex) {
                threadRef.stop();
            }
        });

    }

    public void initializeFromRenderedLayout() {
        topBufferSize = 0;
        bottomBufferSize = getActiveLayoutItemsCount() - getLayoutVisibleDisplaySize();
        isInit = true;
    }

    private class PrefetchFilesAsyncTask extends AsyncTask<Void, Integer, Long> {

        private int balanceBottomCount, balanceTopCount;
        private int firstVisibleIndex, lastVisibleIndex;
        private DisplayFragments displayCtx;
        private UpdateDataStruct.UpdateDataType updateDataType;
        private UpdateDataStruct updateDataBlock;
        private boolean prunedReverseEdge;
        private int itemsAppendedCount, itemsPrunedCount;

        public PrefetchFilesAsyncTask() {
            balanceTopCount = balanceBottomCount = 0;
            firstVisibleIndex = lastVisibleIndex = 0;
            displayCtx = null;
            updateDataType = UpdateDataStruct.UpdateDataType.ACTION_NONE;
            updateDataBlock = null;
            prunedReverseEdge = false;
            itemsAppendedCount = itemsPrunedCount = 0;
        }

        private void loadInDataAtBottomEdge() {
            if(balanceBottomCount > 0) { // Need to append to the bottom, trim the extra buffered layout up top:

                int itemsCountToAppend = balanceBottomCount;
                int topItemsToPruneCount = 0;
                if(displayCtx.lastFileDataStartIndex < BalancedBufferSize) {
                    topBufferSize = Math.max(0, firstVisibleIndex - 1);
                }
                else {
                    topItemsToPruneCount = Math.max(0, BalancedBufferSize - 1 - topBufferSize - itemsCountToAppend);
                    prunedReverseEdge = topItemsToPruneCount > 0;
                }
                itemsPrunedCount = topItemsToPruneCount;
                int startQueryIndex = displayCtx.lastFileDataStartIndex + topItemsToPruneCount;
                int endQueryIndex = displayCtx.lastFileDataEndIndex + itemsCountToAppend;
                displayCtx.getCwdFolderContext().computeDirectoryContents(
                        startQueryIndex, endQueryIndex,
                        topItemsToPruneCount, 0,
                        itemsCountToAppend, true
                );
                List<DisplayTypes.FileType> nextFileItemsList = displayCtx.getCwdFolderContext().getWorkingDirectoryContents();
                List<String> nextFileNamesList = new ArrayList<String>();
                for (DisplayTypes.FileType fileItem : nextFileItemsList) {
                    nextFileNamesList.add(fileItem.getBaseName());
                }
                updateDataType = UpdateDataStruct.UpdateDataType.APPEND_DATA_TO_BOTTOM;
                updateDataBlock = new UpdateDataStruct(
                        updateDataType,
                        nextFileNamesList,
                        nextFileItemsList
                );
                itemsAppendedCount = itemsCountToAppend;

            }
        }

        private void loadInDataAtTopEdge() {
            if(balanceTopCount > 0) { // Prepend at top, trim from bottom:

                int itemsCountToAppend = balanceTopCount;
                int bottomItemsToPrune = 0;
                if(displayCtx.lastFileDataEndIndex < getLayoutVisibleDisplaySize() + BalancedBufferSize) {
                    bottomBufferSize = Math.max(0, lastVisibleIndex + 1 - getLayoutVisibleDisplaySize());
                }
                else {
                    bottomItemsToPrune = Math.max(0, BalancedBufferSize - 1 - bottomBufferSize - itemsCountToAppend);
                    prunedReverseEdge = bottomItemsToPrune > 0;
                }
                itemsPrunedCount = bottomItemsToPrune;
                int startQueryIndex = Math.min(0, displayCtx.lastFileDataStartIndex - itemsCountToAppend);
                int endQueryIndex = Math.max(startQueryIndex, displayCtx.lastFileDataEndIndex - bottomItemsToPrune);
                displayCtx.getCwdFolderContext().computeDirectoryContents(
                        startQueryIndex, endQueryIndex,
                        0, bottomItemsToPrune,
                        itemsCountToAppend, true
                );
                List<DisplayTypes.FileType> nextFileItemsList = displayCtx.getCwdFolderContext().getWorkingDirectoryContents();
                List<String> nextFileNamesList = new ArrayList<String>();
                for (DisplayTypes.FileType fileItem : nextFileItemsList) {
                    nextFileNamesList.add(fileItem.getBaseName());
                }
                updateDataType = UpdateDataStruct.UpdateDataType.PREPEND_DATA_AT_TOP;
                updateDataBlock = new UpdateDataStruct(
                        updateDataType,
                        nextFileNamesList,
                        nextFileItemsList
                );
                itemsAppendedCount = itemsCountToAppend;

            }
        }

        protected Long doInBackground(Void... unusedArgsList) {

            if(balanceTopCount > 0 && balanceBottomCount > 0) {
                if(balanceTopCount > balanceBottomCount) {
                    loadInDataAtTopEdge();
                }
                else {
                    loadInDataAtBottomEdge();
                }
            }
            else if(balanceBottomCount > 0) {
                loadInDataAtBottomEdge();
            }
            else if(balanceTopCount > 0) {
                loadInDataAtTopEdge();
            }

            return Long.valueOf(0);

        }

        protected void onPreExecute() {

            balanceBottomCount = getActiveCountToBalanceBottom();
            balanceTopCount = getActiveCountToBalanceTop();
            firstVisibleIndex = getLayoutFirstVisibleItemIndex();
            lastVisibleIndex = getLayoutLastVisibleItemIndex();

            Log.d(LOGTAG, String.format(Locale.getDefault(),
                    "PrefetchUpdaterThread: CHECK FOR UPDATES: Visible[%d, %d] ;; ToBalance[%d, %d] (%d, %d);; ItemsCount = %d, DirLen = %d",
                    firstVisibleIndex, lastVisibleIndex,
                    balanceTopCount, balanceBottomCount,
                    topBufferSize, bottomBufferSize,
                    getActiveLayoutItemsCount(), getActiveFolderContentsSize()));

            displayCtx = DisplayFragments.getInstance();

            // In the meantime, display a quick positional status Toast message to the user since
            // scrollbars do not indicate the number of files left to view very well:
            //DisplayUtils.displayFolderScrollContents(
            //        displayCtx.lastFileDataStartIndex + 1,
            //        displayCtx.getCwdFolderContext().getFolderChildCount()
            //);

        }

        protected void onProgressUpdate(Integer... progress) {
            //publishProgress(inProgressValue); ... In the background function ... Gets passed back as arguments progress ...
        }

        protected void onPostExecute(Long result) {

            FileChooserRecyclerView mainRV = displayCtx.getMainRecyclerView();
            DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();

            if(updateDataType.equals(UpdateDataStruct.UpdateDataType.APPEND_DATA_TO_BOTTOM)) {

                Log.d(LOGTAG, String.format(Locale.getDefault(),
                        "POSTING update to RecyclerView: APPEND #%d data items to BOTTOM, RM #%d from TOP",
                        itemsAppendedCount, itemsPrunedCount));
                int prevFirstVisibleIndex = firstVisibleIndex;
                if(prunedReverseEdge) {
                    int lastOfFirstRangeIndex = updateDataBlock.nextFileNamesList.size() - itemsAppendedCount;
                    rvAdapter.reloadDataSets(
                            updateDataBlock.nextFileNamesList.subList(0, lastOfFirstRangeIndex),
                            updateDataBlock.nextFileItemsList.subList(0, lastOfFirstRangeIndex),
                            false
                    );
                    rvAdapter.notifyItemRangeRemoved(0, itemsPrunedCount);
                    topBufferSize -= itemsPrunedCount;
                }
                rvAdapter.reloadDataSets(updateDataBlock.nextFileNamesList, updateDataBlock.nextFileItemsList, false);
                rvAdapter.notifyItemRangeInserted(updateDataBlock.nextFileNamesList.size() - itemsAppendedCount, itemsAppendedCount);
                bottomBufferSize += itemsAppendedCount;
                rvAdapter.notifyDataSetChanged();
                mainRV.scrollToPosition(prevFirstVisibleIndex);

            }
            else if(updateDataType.equals(UpdateDataStruct.UpdateDataType.PREPEND_DATA_AT_TOP)) { // Prepend items to top, trim the extra from the bottom:

                Log.d(LOGTAG, String.format(Locale.getDefault(),
                        "POSTING update to RecyclerView: PREPEND #%d data items to TOP, RM #%02d from BOTTOM",
                        itemsAppendedCount, itemsPrunedCount));
                int prevFirstVisibleIndex = firstVisibleIndex;
                if(prunedReverseEdge) {
                    int lastOfFirstRangeIndex = itemsAppendedCount;
                    rvAdapter.reloadDataSets(
                            updateDataBlock.nextFileNamesList.subList(lastOfFirstRangeIndex, updateDataBlock.nextFileNamesList.size()),
                            updateDataBlock.nextFileItemsList.subList(lastOfFirstRangeIndex, updateDataBlock.nextFileNamesList.size()),
                            false
                    );
                    rvAdapter.notifyItemRangeRemoved(
                            updateDataBlock.nextFileNamesList.size() - itemsAppendedCount,
                            updateDataBlock.nextFileNamesList.size() - itemsAppendedCount + itemsPrunedCount
                    );
                    bottomBufferSize -= itemsPrunedCount;
                }
                rvAdapter.reloadDataSets(updateDataBlock.nextFileNamesList, updateDataBlock.nextFileItemsList, false);
                rvAdapter.notifyItemRangeInserted(0, itemsAppendedCount);
                topBufferSize += itemsAppendedCount;
                rvAdapter.notifyDataSetChanged();
                mainRV.scrollToPosition(prevFirstVisibleIndex);

            }

        }

    }

    private static final long THREAD_INIT_PAUSE_TIMEOUT = 125; // Milliseconds
    private static final long THREAD_PAUSE_TIMEOUT = 550; // Milliseconds

    @Override
    public void run() {

        DisplayFragments displayCtx = DisplayFragments.getInstance();
        while(true) {

            if(!isInit) {

                FileChooserActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!displayCtx.viewportCapacityMesaured && displayCtx.getMainRecyclerView().getLayoutManager().getChildCount() != 0) {
                            displayCtx.fileItemDisplayHeight = displayCtx.getMainRecyclerView().getLayoutManager().getChildAt(0).getMeasuredHeight();

                            if (displayCtx.resetViewportMaxFilesCount(displayCtx.getMainRecyclerView())) {
                                displayCtx.getMainRecyclerView().scrollToPosition(0);
                                initializeFromRenderedLayout();
                            }
                        } else if (displayCtx.viewportCapacityMesaured) {
                            displayCtx.getMainRecyclerView().scrollToPosition(0);
                            initializeFromRenderedLayout();
                        }
                    }
                });

                try {
                    Thread.sleep(THREAD_INIT_PAUSE_TIMEOUT);
                    continue;
                } catch(InterruptedException ie) {
                    break;
                }

            }
            else if(displayCtx.getCwdFolderContext() == null) {

                try {
                    Thread.sleep(THREAD_INIT_PAUSE_TIMEOUT);
                    continue;
                } catch(InterruptedException ie) {
                    break;
                }

            }

            PrefetchFilesAsyncTask nextPrefetchInBgTask = new PrefetchFilesAsyncTask();
            nextPrefetchInBgTask.execute();

            try {
                Thread.sleep(THREAD_PAUSE_TIMEOUT);
            } catch(InterruptedException ie) {
                break;
            }

        }

    }

    public void setWeightBufferSize(int size) throws IllegalStateException {
        if(isAlive()) {
            throw new IllegalStateException("The buffer size parameter must be reset _BEFORE_ starting the updater thread");
        }
        else if(size <= 0) {
            return;
        }
        BalancedBufferSize = size;
    }

    public int getActiveCountToBalanceTop() {
        if(getActiveLayoutItemsCount() == 0 || getLayoutVisibleDisplaySize() >= getActiveFolderContentsSize()) {
            return 0;
        }
        int firstVisibleItemIndex = getLayoutFirstVisibleItemIndex();
        if(firstVisibleItemIndex < getTopBufferPosition()) {
            return getTopBufferPosition() - firstVisibleItemIndex;
        }
        else {
            return 0;
        }
    }

    public int getTopBufferPosition() {
        DisplayFragments displayCtx = DisplayFragments.getInstance();
        return displayCtx.lastFileDataStartIndex;
    }

    public int getActiveCountToBalanceBottom() {
        if(getActiveLayoutItemsCount() == 0 || getLayoutVisibleDisplaySize() >= getActiveFolderContentsSize()) {
            // If layout not initialized, or fits into single window, do not grow the padding buffer:
            return 0;
        }
        int maxBottomBufferSize = Math.min(getActiveFolderContentsSize() - getLayoutVisibleDisplaySize(), BalancedBufferSize);
        if(getBottomBufferPosition() + 1 < maxBottomBufferSize) {
            return maxBottomBufferSize - 1 - getBottomBufferPosition();
        }
        // Otherwise, the bottom buffer has accumulated maximal size.
        // Now adjust when the scroller brings the last visible to less than this size from the last index:
        int lastVisibleItemIndex = getLayoutLastVisibleItemIndex();
        int maxAdjustedBottomBufferSize = Math.min(BalancedBufferSize,
                Math.max(0, getActiveFolderContentsSize() - Math.max(getLayoutVisibleDisplaySize(), lastVisibleItemIndex + 1)));
        if(lastVisibleItemIndex <= getBottomBufferPosition() &&
           getBottomBufferPosition() - lastVisibleItemIndex < maxAdjustedBottomBufferSize) {
            return maxAdjustedBottomBufferSize - getBottomBufferPosition() + lastVisibleItemIndex;
        }
        else {
            return 0;
        }
    }

    public int getBottomBufferPosition() {
        DisplayFragments displayCtx = DisplayFragments.getInstance();
        return displayCtx.lastFileDataEndIndex;
    }

    public int getActiveFolderContentsSize() {
        return DisplayFragments.getInstance().getCwdFolderContext().getFolderChildCount();
    }

    public int getLayoutVisibleDisplaySize() {
        return DisplayFragments.getInstance().getViewportMaxFilesCount();
    }

    public int getLayoutFirstVisibleItemIndex() {
        FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
        FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
        return rvLayoutManager.findFirstCompletelyVisibleItemPosition();
    }

    public int getLayoutLastVisibleItemIndex() {
        FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
        FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
        return rvLayoutManager.findLastCompletelyVisibleItemPosition();
    }

    public int getActiveLayoutItemsCount() {
        FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
        DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();
        return rvAdapter.getItemCount();
    }

}
