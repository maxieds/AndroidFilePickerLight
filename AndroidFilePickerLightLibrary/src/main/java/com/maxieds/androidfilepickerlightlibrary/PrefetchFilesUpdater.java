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
 */
public class PrefetchFilesUpdater extends Thread implements FileChooserRecyclerView.RecyclerViewSlidingContextWindow {

    private static String LOGTAG = PrefetchFilesUpdater.class.getSimpleName();

    public static class UpdateDataStruct {

        public enum UpdateDataType {

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

        public UpdateDataStruct() {
            postedDataBlockType = UpdateDataType.APPEND_DATA_TO_BOTTOM;
            nextFileNamesList = new ArrayList<String>();
            nextFileItemsList = new ArrayList<DisplayTypes.FileType>();
        }

        public UpdateDataStruct(UpdateDataType dataType, List<String> fileNames, List<DisplayTypes.FileType> fileItems) {
            postedDataBlockType = dataType;
            nextFileNamesList = fileNames;
            nextFileItemsList = fileItems;
        }

    }

    private int BalancedBufferSize;
    private boolean isInit;
    private int topBufferSize;
    private int bottomBufferSize;

    public PrefetchFilesUpdater() {
        // Set a sane default: My testing Android phone comfortably fits 12-15 layout items.
        BalancedBufferSize = 15;
        isInit = false;
        topBufferSize = bottomBufferSize = 0;
    }

    public void initializeFromRenderedLayout() {
        topBufferSize = 0;
        bottomBufferSize = getActiveLayoutItemsCount() - getLayoutVisibleDisplaySize();
        isInit = true;
    }

    private boolean postUpdateNotifyToRecyclerView(UpdateDataStruct updateDataBlock, FileChooserRecyclerView mainRV) {

        DisplayFragments displayCtx = DisplayFragments.getInstance();
        if(updateDataBlock.postedDataBlockType.equals(UpdateDataStruct.UpdateDataType.APPEND_DATA_TO_BOTTOM)) {
            //rvLayoutManager.setAppendToBackMode();
            Log.i(LOGTAG, "BEFORE append: " + displayCtx.fileItemBasePathsList.size());
            displayCtx.appendItemsToBack(
                    updateDataBlock.nextFileNamesList,
                    updateDataBlock.nextFileItemsList
            );
            Log.i(LOGTAG, "AFTER append: " + displayCtx.fileItemBasePathsList.size());
            //mainRV.setAdapter(new DisplayAdapters.FileListAdapter(displayCtx.fileItemBasePathsList, displayCtx.activeFileItemsDataList));
            //rvAdapter.reloadDataSets(displayCtx.fileItemBasePathsList, displayCtx.activeFileItemsDataList, true);
                            /*rvAdapter.notifyItemRangeInserted(
                                    displayCtx.fileItemBasePathsList.size() - updateData.nextFileItemsList.size(),
                                    updateData.nextFileItemsList.size()
                            );*/
            //rvAdapter.notifyItemRangeChanged(displayCtx.fileItemBasePathsList.size() - 1 - updateData.nextFileNamesList.size(), updateData.nextFileNamesList.size());
            /////mainRV.smoothScrollToPosition(curFirstVisibleIndex);
            // TODO: Somewhere, need to prune this ...
            //DisplayFragments.RecyclerViewUtils.removeItemsAtTop(updateData.nextFileItemsList.size());
            //rvAdapter.reloadDataSets(updateData.nextFileNamesList, updateData.nextFileItemsList, false);
            //rvAdapter.notifyItemRangeRemoved(0, updateData.nextFileItemsList.size());
            //rvAdapter.notifyItemRangeChanged(0, getActiveLayoutItemsCount());
            //rvLayoutManager.restoreDefaultMode();
        }

        final UpdateDataStruct updateData = updateDataBlock;
        final Thread prefetchUpdatesThread = this;
        FileChooserActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainRV.post(new Runnable() {
                    @Override
                    public void run() {

                        FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
                        DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();
                        DisplayFragments displayCtx = DisplayFragments.getInstance();

                        int prevFirstVisibleIndex = rvLayoutManager.findFirstVisibleItemPosition() - updateData.nextFileNamesList.size();
                        rvAdapter.reloadDataSets(displayCtx.fileItemBasePathsList, displayCtx.activeFileItemsDataList, true);
                        mainRV.smoothScrollToPosition(prevFirstVisibleIndex);

                        /*else { // Prepend items to top, trim the extra from the bottom:
                            rvLayoutManager.setInsertAtFrontMode();
                            DisplayFragments.RecyclerViewUtils.insertItemsAtTop(
                                    updateData.nextFileNamesList.size(),
                                    updateData.nextFileNamesList,
                                    updateData.nextFileItemsList
                            );
                            rvAdapter.reloadDataSets(updateData.nextFileNamesList, updateData.nextFileItemsList, false);
                            rvAdapter.notifyItemRangeInserted(0, updateData.nextFileItemsList.size());
                            rvAdapter.notifyItemRangeChanged(0, getActiveLayoutItemsCount());
                            // TODO: Somewhere, need to prune this ...
                            //DisplayFragments.RecyclerViewUtils.removeItemsFromBack(updateData.nextFileItemsList.size());
                            //rvAdapter.reloadDataSets(updateData.nextFileNamesList, updateData.nextFileItemsList, false);
                            //rvAdapter.notifyItemRangeRemoved(
                            //        getActiveLayoutItemsCount() - updateData.nextFileItemsList.size(),
                            //        getActiveLayoutItemsCount()
                            //);
                            //rvAdapter.notifyItemRangeChanged(0, getActiveLayoutItemsCount());
                            //rvLayoutManager.restoreDefaultMode();
                        }*/
                        prefetchUpdatesThread.interrupt();

                    }
                });
            }
        });
        return true;
    }

    private class PrefetchFilesAsyncTask extends AsyncTask<Void, Integer, Long> {

        private int balanceBottomCount, balanceTopCount;

        public PrefetchFilesAsyncTask() {
            balanceTopCount = balanceBottomCount = 0;
        }

        protected Long doInBackground(Void... urls) {
            //publishProgress((int) ((i / (float) count) * 100));
            return Long.valueOf(0);
        }

        protected void onPreExecute() {
            balanceBottomCount = getActiveCountToBalanceBottom();
            balanceTopCount = getActiveCountToBalanceTop();
            Log.i(LOGTAG, String.format(Locale.getDefault(),
                    "PrefetchUpdaterThread: CHECK FOR UPDATES: Visible[%d, %d] ;; ToBalance[%d, %d] (%d, %d);; ItemsCount = %d, DirLen = %d",
                    getLayoutFirstVisibleItemIndex(), getLayoutLastVisibleItemIndex(),
                    balanceTopCount, balanceBottomCount,
                    topBufferSize, bottomBufferSize,
                    getActiveLayoutItemsCount(), getActiveFolderContentsSize()));
        }

        protected void onProgressUpdate(Integer... progress) {
            //publishProgress((int) ((i / (float) count) * 100));
            //setProgressBar(progress[0]);
        }

        protected void onPostExecute(Long result) {
            //showDialog("Fetched: " + result + " file items");
        }

    }


    private static final long THREAD_INIT_PAUSE_TIMEOUT = 150; // milliseconds
    private static final long THREAD_PAUSE_TIMEOUT = 100; // milliseconds

    private void pauseUntilInterrupted() {
        try {
            while (true) {
                sleep(THREAD_PAUSE_TIMEOUT);
            }
        } catch(InterruptedException ie) {
            Log.i(LOGTAG, "Prefetch thread caught interrupted exception");
        }
    }

    @Override
    public void run() {

        DisplayFragments displayCtx = DisplayFragments.getInstance();
        while(true) {

            if(!isInit) {

                Log.i(LOGTAG, "PRFETCH NOT INIT ...");
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
                            initializeFromRenderedLayout();
                        }
                    }
                });
                try {
                    Thread.sleep(THREAD_INIT_PAUSE_TIMEOUT);
                    continue;
                } catch(InterruptedException ie) {
                    ie.printStackTrace();
                    break;
                }

            }

            int balBottomCount = getActiveCountToBalanceBottom();
            int balTopCount = getActiveCountToBalanceTop();
            Log.i(LOGTAG, String.format(Locale.getDefault(),
                    "PrefetchUpdaterThread: CHECK FOR UPDATES: Visible[%d, %d] ;; ToBalance[%d, %d] (%d, %d);; ItemsCount = %d, DirLen = %d",
                    getLayoutFirstVisibleItemIndex(), getLayoutLastVisibleItemIndex(),
                    balTopCount, balBottomCount,
                    topBufferSize, bottomBufferSize,
                    getActiveLayoutItemsCount(), getActiveFolderContentsSize()));

            if(displayCtx.getCwdFolderContext() == null) {
                try {
                    Thread.sleep(THREAD_INIT_PAUSE_TIMEOUT);
                    continue;
                } catch(InterruptedException ie) {
                    ie.printStackTrace();
                    break;
                }
            }

            int itemsCountToAppend = balBottomCount;
            if(itemsCountToAppend > 0) { // Need to append to the bottom, trim the extra buffered layout up top:
                int startQueryIndex = displayCtx.lastFileDataStartIndex;
                int endQueryIndex = displayCtx.lastFileDataEndIndex + itemsCountToAppend;
                displayCtx.getCwdFolderContext().computeDirectoryContents(
                        startQueryIndex, endQueryIndex,
                        0, 0,
                        itemsCountToAppend, true
                );
                List<DisplayTypes.FileType> nextFileItemsList = displayCtx.getCwdFolderContext().getWorkingDirectoryContents();
                nextFileItemsList = nextFileItemsList.subList(nextFileItemsList.size() - 1 - itemsCountToAppend, nextFileItemsList.size() - 1);
                List<String> nextFileNamesList = new ArrayList<String>();
                for (DisplayTypes.FileType fileItem : nextFileItemsList) {
                    nextFileNamesList.add(fileItem.getBaseName());
                }
                Log.i(LOGTAG, "Next append size: " + nextFileItemsList.size());
                UpdateDataStruct updateDataBlock = new UpdateDataStruct(
                        UpdateDataStruct.UpdateDataType.APPEND_DATA_TO_BOTTOM,
                        nextFileNamesList,
                        nextFileItemsList
                );
                Log.i(LOGTAG, String.format(Locale.getDefault(), "POSTING update to RecyclerView: APPEND #%d data items to BOTTOM", itemsCountToAppend));
                postUpdateNotifyToRecyclerView(updateDataBlock, displayCtx.getMainRecyclerView());
                pauseUntilInterrupted();
            }

            /*itemsCountToAppend = getActiveCountToBalanceTop();
            if(itemsCountToAppend > 0) { // Prepend at top, trim from bottom:
                int startQueryIndex = Math.min(0, getLayoutFirstVisibleItemIndex() + 1 - itemsCountToAppend);
                int endQueryIndex = Math.min(getActiveLayoutItemsCount() - 1, startQueryIndex + itemsCountToAppend - 1);
                displayCtx.getCwdFolderContext().computeDirectoryContents(
                        startQueryIndex, endQueryIndex, itemsCountToAppend,
                        itemsCountToAppend, 0, true
                );
                List<DisplayTypes.FileType> nextFileItemsList = displayCtx.getCwdFolderContext().getWorkingDirectoryContents();

                List<String> nextFileNamesList = new ArrayList<String>();
                for (DisplayTypes.FileType fileItem : nextFileItemsList) {
                    nextFileNamesList.add(fileItem.getBaseName());
                }
                UpdateDataStruct updateDataBlock = new UpdateDataStruct(
                        UpdateDataStruct.UpdateDataType.PREPEND_DATA_AT_TOP,
                        nextFileNamesList,
                        nextFileItemsList
                );
                Log.i(LOGTAG, String.format(Locale.getDefault(), "POSTING update to RecyclerView: PREPEND #%d data items to TOP", itemsCountToAppend));
                postUpdateNotifyToRecyclerView(updateDataBlock, displayCtx.getMainRecyclerView());
            }*/

            try {
                Thread.sleep(THREAD_PAUSE_TIMEOUT);
            } catch(InterruptedException ie) {
                ie.printStackTrace();
                break;
            }

        }
    }

    public void setWeightBufferSize(int size) throws IllegalStateException {
        if(isAlive()) {
            throw new IllegalStateException("The buffer size parameter must be reset _BEFORE_ starting the updater thread");
        }
        BalancedBufferSize = size;
    }

    /*
     * Note: Since we cannot load files before the zeroth index in the directory, we need to do some
     *       accounting to make sure that we really are keeping things balanced as planned.
     *       Similarly, we cannot load files beyond the last indexed file in the current folder.
     */

    public int getActiveCountToBalanceTop() {
        return 0;
    }

    public int getTopBufferPosition() {
        DisplayFragments displayCtx = DisplayFragments.getInstance();
        return displayCtx.lastFileDataStartIndex;
    }

    public int getActiveCountToBalanceBottom() {
        if(getActiveLayoutItemsCount() == 0 || getLayoutVisibleDisplaySize() >= getActiveFolderContentsSize() - topBufferSize) {
            // If layout not initialized, or fits into single window, do not grow the padding buffer:
            return 0;
        }
        if(!isInit && bottomBufferSize < Math.min(Math.max(0, getActiveFolderContentsSize() - topBufferSize - getLayoutVisibleDisplaySize()), BalancedBufferSize)) {
            // Handles the initialization case (loading the buffer the first time):
            int nextBalanceCount = Math.min(Math.max(0, getActiveFolderContentsSize() - topBufferSize - getLayoutVisibleDisplaySize()), BalancedBufferSize) - bottomBufferSize;
            bottomBufferSize = Math.min(Math.max(0, getActiveFolderContentsSize() - topBufferSize - getLayoutVisibleDisplaySize()), BalancedBufferSize);
            return nextBalanceCount;
        }
        int lastVisibleItemIndex = getLayoutLastVisibleItemIndex(); // avoiding a potential race condition when scrolling
        if(lastVisibleItemIndex <= getBottomBufferPosition() &&
                getBottomBufferPosition() - lastVisibleItemIndex < Math.min(getActiveFolderContentsSize() - getBottomBufferPosition(), BalancedBufferSize)) {
            return Math.min(getActiveFolderContentsSize() - getBottomBufferPosition(), BalancedBufferSize) - getBottomBufferPosition() + lastVisibleItemIndex;
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
        return rvLayoutManager.findFirstVisibleItemPosition();
    }

    public int getLayoutLastVisibleItemIndex() {
        FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
        FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
        return rvLayoutManager.findLastVisibleItemPosition();
    }

    public int getActiveLayoutItemsCount() {
        FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
        DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();
        return rvAdapter.getItemCount();
    }

}
