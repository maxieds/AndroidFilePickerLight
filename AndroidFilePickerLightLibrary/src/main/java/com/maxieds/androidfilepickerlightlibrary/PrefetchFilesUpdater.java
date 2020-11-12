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

    public PrefetchFilesUpdater() {
        // Set a sane default: My testing Android phone comfortably fits 12-15 layout items.
        BalancedBufferSize = 15;
    }

    private synchronized boolean postUpdateNotifyToRecyclerView(UpdateDataStruct updateDataBlock, FileChooserRecyclerView mainRV) {
        final UpdateDataStruct updateDataInit = updateDataBlock;
        mainRV.post(new Runnable() {
            @Override
            public void run() {
                final UpdateDataStruct updateData = updateDataInit;
                FileChooserActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // ?? TODO: Look back over this logic one more time ... ???
                        FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
                        FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
                        DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();

                        if(updateData.postedDataBlockType.equals(UpdateDataStruct.UpdateDataType.APPEND_DATA_TO_BOTTOM)) {
                            rvLayoutManager.setAppendToBackMode();
                            DisplayFragments.RecyclerViewUtils.appendItemsToBack(
                                    updateData.nextFileNamesList.size(),
                                    updateData.nextFileNamesList,
                                    updateData.nextFileItemsList
                            );
                            rvAdapter.reloadDataSets(updateData.nextFileNamesList, updateData.nextFileItemsList, false);
                            rvAdapter.notifyItemRangeInserted(
                                    getActiveLayoutItemsCount() - updateData.nextFileItemsList.size(),
                                    getActiveLayoutItemsCount()
                            );
                            rvAdapter.notifyItemRangeChanged(0, getActiveLayoutItemsCount());
                            DisplayFragments.RecyclerViewUtils.removeItemsAtTop(updateData.nextFileItemsList.size());
                            rvAdapter.reloadDataSets(updateData.nextFileNamesList, updateData.nextFileItemsList, false);
                            rvAdapter.notifyItemRangeRemoved(0, updateData.nextFileItemsList.size());
                            rvAdapter.notifyItemRangeChanged(0, getActiveLayoutItemsCount());
                            rvLayoutManager.restoreDefaultMode();
                        }
                        else { // Prepend items to top, trim the extra from the bottom:
                            rvLayoutManager.setInsertAtFrontMode();
                            DisplayFragments.RecyclerViewUtils.insertItemsAtTop(
                                    updateData.nextFileNamesList.size(),
                                    updateData.nextFileNamesList,
                                    updateData.nextFileItemsList
                            );
                            rvAdapter.reloadDataSets(updateData.nextFileNamesList, updateData.nextFileItemsList, false);
                            rvAdapter.notifyItemRangeInserted(0, updateData.nextFileItemsList.size());
                            rvAdapter.notifyItemRangeChanged(0, getActiveLayoutItemsCount());
                            DisplayFragments.RecyclerViewUtils.removeItemsFromBack(updateData.nextFileItemsList.size());
                            rvAdapter.reloadDataSets(updateData.nextFileNamesList, updateData.nextFileItemsList, false);
                            rvAdapter.notifyItemRangeRemoved(
                                    getActiveLayoutItemsCount() - updateData.nextFileItemsList.size(),
                                    getActiveLayoutItemsCount()
                            );
                            rvAdapter.notifyItemRangeChanged(0, getActiveLayoutItemsCount());
                            rvLayoutManager.restoreDefaultMode();
                        }

                    }
                });
            }
        });
        return true;
    }

    private static final long THREAD_PAUSE_TIMEOUT = 50; // milliseconds

    @Override
    public synchronized void run() {

        while(true) {

            Log.i(LOGTAG, String.format(Locale.getDefault(),
                    "PrefetchUpdaterThread: CHECK FOR UPDATES: Visible[%d, %d] ;; ToBalance[%d, %d] ;; ItemsCount = %d, DirLen = %d",
                    getLayoutFirstVisibleItemIndex(), getLayoutLastVisibleItemIndex(),
                    getActiveCountToBalanceTop(), getActiveCountToBalanceBottom(),
                    getActiveLayoutItemsCount(), getActiveFolderContentsSize()));
            // TODO: Need to fix these references in case are scrolling ???

            // TODO: Check these indices again ...
            /*int itemsCountToAppend = getActiveCountToBalanceBottom();
            if(itemsCountToAppend > 0) { // Need to append to the bottom, trim the extra buffered layout up top:
                int startQueryIndex = getLayoutLastVisibleItemIndex() + 1;
                int endQueryIndex = getLayoutVisibleDisplaySize() - 1;
                DisplayFragments displayFragmentsCtx = DisplayFragments.getInstance();
                displayFragmentsCtx.getCwdFolderContext().computeDirectoryContents(
                        startQueryIndex, endQueryIndex, itemsCountToAppend,
                        0, itemsCountToAppend, true
                );
                List<DisplayTypes.FileType> nextFileItemsList = displayFragmentsCtx.getCwdFolderContext().getWorkingDirectoryContents();
                displayFragmentsCtx.displayNextDirectoryFilesList(nextFileItemsList);
                List<String> nextFileNamesList = new ArrayList<String>();
                for (DisplayTypes.FileType fileItem : nextFileItemsList) {
                    nextFileNamesList.add(fileItem.getBaseName());
                }
                UpdateDataStruct updateDataBlock = new UpdateDataStruct(
                        UpdateDataStruct.UpdateDataType.APPEND_DATA_TO_BOTTOM,
                        nextFileNamesList,
                        nextFileItemsList
                );
                Log.i(LOGTAG, String.format(Locale.getDefault(), "POSTING update to RecyclerView: APPEND #%d data items to BOTTOM", itemsCountToAppend));
                postUpdateNotifyToRecyclerView(updateDataBlock, displayFragmentsCtx.getMainRecyclerView());
            }*/

            /*itemsCountToAppend = getActiveCountToBalanceTop();
            if(itemsCountToAppend > 0) { // Prepend at top, trim from bottom:
                int startQueryIndex = Math.min(0, getLayoutFirstVisibleItemIndex() + 1 - itemsCountToAppend);
                int endQueryIndex = Math.min(getActiveLayoutItemsCount() - 1, startQueryIndex + itemsCountToAppend - 1);
                DisplayFragments displayFragmentsCtx = DisplayFragments.getInstance();
                displayFragmentsCtx.getCwdFolderContext().computeDirectoryContents(
                        startQueryIndex, endQueryIndex, itemsCountToAppend,
                        itemsCountToAppend, 0, true
                );
                List<DisplayTypes.FileType> nextFileItemsList = displayFragmentsCtx.getCwdFolderContext().getWorkingDirectoryContents();
                displayFragmentsCtx.displayNextDirectoryFilesList(nextFileItemsList);
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
                postUpdateNotifyToRecyclerView(updateDataBlock, displayFragmentsCtx.getMainRecyclerView());
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

    public int getActiveCountToBalanceTop() { // ??? TODO ???
        if(getLayoutFirstVisibleItemIndex() == 0) {
            return 0;
        }
        else if(getLayoutFirstVisibleItemIndex() + 1 < BalancedBufferSize) {
            return getLayoutFirstVisibleItemIndex();
        }
        else {
            return BalancedBufferSize - getActiveTopBufferSize();
        }
    }

    public int getActiveTopBufferSize() {
        return getLayoutFirstVisibleItemIndex() + 1;
    }

    public int getActiveCountToBalanceBottom() {
        if(getActiveFolderContentsSize() <= getActiveLayoutItemsCount()) {
            return 0;
        }
        else if(getActiveFolderContentsSize() - getLayoutLastVisibleItemIndex() - 1 < BalancedBufferSize) {
            return getActiveFolderContentsSize() - getLayoutLastVisibleItemIndex() - 1 - getActiveBottomBufferSize();
        }
        else {
            return BalancedBufferSize - getActiveBottomBufferSize();
        }
    }

    public int getActiveBottomBufferSize() { // ??? TODO ???
        int totalItemsCount = getActiveLayoutItemsCount();
        int firstVisibleIndex = getLayoutFirstVisibleItemIndex();
        int displayVisibleSize = Math.min(getActiveFolderContentsSize(), getLayoutVisibleDisplaySize());
        return totalItemsCount - displayVisibleSize - firstVisibleIndex + 1; // ??? Check ???
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
        FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
        return rvLayoutManager.getItemCount();
    }

}
