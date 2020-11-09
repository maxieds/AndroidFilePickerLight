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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DisplayFragments {

    private static String LOGTAG = DisplayFragments.class.getSimpleName();

    private static DisplayFragments localStaticInst = new DisplayFragments();
    private FileChooserRecyclerView recyclerView = null;

    private Drawable folderIconInst;
    private Drawable fileIconInst;
    private Drawable hiddenFileIconInst;

    public FileFilter.FileFilterBase localFilesListFilter;
    public FileFilter.FileItemsSortFunc localFilesListSortFunc;

    public DisplayFragments() {
        folderIconInst = DrawUtils.getDrawableFromResource(R.drawable.folder_icon32);
        fileIconInst = DrawUtils.getDrawableFromResource(R.drawable.generic_file_icon32);
        hiddenFileIconInst = DrawUtils.getDrawableFromResource(R.drawable.hidden_file_icon32);
        localFilesListFilter = null;
        localFilesListSortFunc = null;
    }

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

    public static final int SCROLL_QUEUE_BUFFER_SIZE = 2;
    public static final int DEFAULT_VIEWPORT_FILE_ITEMS_COUNT = 50 + SCROLL_QUEUE_BUFFER_SIZE; // set large enough to overfill the window on first load
    private int viewportMaxFileItemsCount = DEFAULT_VIEWPORT_FILE_ITEMS_COUNT;
    public int fileItemDisplayHeight = 0;

    public static int getViewportMaxFilesCount() {
        return getInstance().viewportMaxFileItemsCount;
    }

    private void setViewportMaxFilesCount(int viewportFilesCap) {
        viewportMaxFileItemsCount = viewportFilesCap;
    }

    public void resetViewportMaxFilesCount(View parentViewContainer) {
        Log.i(LOGTAG, "resetViewportMaxFilesCount");
        if(!viewportCapacityMesaured) {
            int viewportDisplayHeight = parentViewContainer.getMeasuredHeight();
            if(fileItemDisplayHeight == 0) {
                return;
            }
            setViewportMaxFilesCount(SCROLL_QUEUE_BUFFER_SIZE + (int) Math.floor((double) viewportDisplayHeight / fileItemDisplayHeight));
            Log.i(LOGTAG, String.format("DELAYED RESPONSE: VP Height = %d, FItemDisp Height = %d   ====>  %d (with +%d buffer extra)",
                    viewportDisplayHeight, fileItemDisplayHeight, getViewportMaxFilesCount(), SCROLL_QUEUE_BUFFER_SIZE));
            getMainRecyclerView().setItemViewCacheSize(getViewportMaxFilesCount()); // ??? TODO ???
            viewportCapacityMesaured = true;
        }
    }

    private static final String EMPTY_FOLDER_HISTORY_PATH = "<NONE>";
    private static String folderHistoryOneBackPath = EMPTY_FOLDER_HISTORY_PATH;
    private static String folderHistoryTwoBackPath = EMPTY_FOLDER_HISTORY_PATH;

    public static void updateFolderHistoryPaths(String nextFolderEntryPointPath, boolean initNewFileTree) {
        if(nextFolderEntryPointPath == null || nextFolderEntryPointPath.equals("")) {
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

    public void initializeRecyclerViewLayout(FileChooserRecyclerView rview) {
        if(!recyclerViewAdapterInit) {
            rview.setupRecyclerViewLayout(this);
            setRecyclerView(rview);
            fileItemBasePathsList = new ArrayList<String>();
            activeSelectionsList = new ArrayList<DisplayTypes.FileType>();
            activeFileItemsDataList = new ArrayList<DisplayTypes.FileType>();
            DisplayAdapters.FileListAdapter rvAdapter = new DisplayAdapters.FileListAdapter(fileItemBasePathsList, activeFileItemsDataList);
            rview.setAdapter(rvAdapter);
            resetRecyclerViewLayoutContext();
            recyclerViewAdapterInit = true;
        }
    }

    public void resetRecyclerViewLayoutContext() {
        activeSelectionsList.clear();
        activeFileItemsDataList.clear();
        fileItemBasePathsList.clear();
        lastFileDataStartIndex = 0;
        lastFileDataEndIndex = DEFAULT_VIEWPORT_FILE_ITEMS_COUNT - 1;
        viewportMaxFileItemsCount = DEFAULT_VIEWPORT_FILE_ITEMS_COUNT;
        recyclerViewAdapterInit = false;
        viewportCapacityMesaured = false;
        pathHistoryStack = new Stack<DisplayTypes.DirectoryResultContext>();
        if(BasicFileProvider.getInstance() != null) {
            BasicFileProvider.getInstance().setCustomFileFilter(null);
            BasicFileProvider.getInstance().setCustomFolderSort(null);
        }
    }

    public void descendIntoNextDirectory(boolean initNewFileTree) {
        if(pathHistoryStack.empty()) {
            cancelAllOperationsInProgress();
            FileChooserException.GenericRuntimeErrorException rte = new FileChooserException.GenericRuntimeErrorException("Empty context for folder history ( no more history ??? )");
            FileChooserActivity.getInstance().postSelectedFilesActivityResult(rte);
        }
        DisplayTypes.DirectoryResultContext nextFolder = pathHistoryStack.peek();
        if(nextFolder != null) {
            setCwdFolderContext(nextFolder);
            lastFileDataStartIndex = 0;
            lastFileDataEndIndex = lastFileDataStartIndex + getViewportMaxFilesCount() - 1;
            getCwdFolderContext().computeDirectoryContents(lastFileDataStartIndex, lastFileDataEndIndex);
            displayNextDirectoryFilesList(getCwdFolderContext().getWorkingDirectoryContents());
            DisplayFragments.FolderNavigationFragment.dirsOneBackText.setText(folderHistoryOneBackPath);
            DisplayFragments.FolderNavigationFragment.dirsTwoBackText.setText(folderHistoryTwoBackPath);
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
        DisplayTypes.DirectoryResultContext cwdFolderContext = DisplayTypes.DirectoryResultContext.probeAtCursoryFolderQuery(initBaseFolder);
        setCwdFolderContext(cwdFolderContext);
        Log.i(LOGTAG, "CWD CTX: " + getCwdFolderContext());
        pathHistoryStack.clear();
        pathHistoryStack.push(cwdFolderContext);
        lastFileDataStartIndex = 0;
        lastFileDataEndIndex = lastFileDataStartIndex + getViewportMaxFilesCount() - 1;
        getCwdFolderContext().computeDirectoryContents(lastFileDataStartIndex, lastFileDataEndIndex);
        displayNextDirectoryFilesList(getCwdFolderContext().getWorkingDirectoryContents());
        DisplayFragments.FolderNavigationFragment.dirsOneBackText.setText(folderHistoryOneBackPath);
        DisplayFragments.FolderNavigationFragment.dirsTwoBackText.setText(folderHistoryTwoBackPath);
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

    public static class RecyclerViewUtils {

        public static int findFileItemIndexByLayout(View layoutDisplayView) {
            int fileItemIndex = 0;
            for(DisplayTypes.FileType fileItem : DisplayFragments.getInstance().activeFileItemsDataList) {
                // ??? TODO: Does this comparison method work ???
                if(fileItem.getLayoutContainer() != null && fileItem.getLayoutContainer() == layoutDisplayView) {
                    return fileItemIndex;
                }
                ++fileItemIndex;
            }
            return -1;
        }

        public static boolean insertItemsAtTop(int itemCount,
                                               List<String> fileNamesList, List<DisplayTypes.FileType> fileItemsList) {

            FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
            FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
            DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();
            rvLayoutManager.setInsertAtFrontMode();
            // REMOVE THE VIEWS MANUALLY ...
            rvLayoutManager.restoreDefaultMode();
            return true;

        }

        public static boolean appendItemsToBack(int itemCount,
                                                List<String> fileNamesList, List<DisplayTypes.FileType> fileItemsList) {

            FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
            FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
            DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();
            rvLayoutManager.setAppendToBackMode();
            // REMOVE THE VIEWS MANUALLY ...
            rvLayoutManager.restoreDefaultMode();
            return true;

        }

        public static boolean removeItemsAtTop(int itemCount) {

            FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
            FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
            DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();
            rvLayoutManager.setAppendToBackMode();
            // REMOVE THE VIEWS MANUALLY ...
            rvLayoutManager.restoreDefaultMode();
            return true;

        }

        public static boolean removeItemsFromBack(int itemCount) {

            FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
            FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
            DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();
            rvLayoutManager.setAppendToBackMode();
            // REMOVE THE VIEWS MANUALLY ...
            rvLayoutManager.restoreDefaultMode();
            return true;

        }

        /*public static boolean appendItemsToBackTrimmedFromFront(int itemCount,
                                                                List<String> fileNamesList, List<DisplayTypes.FileType> fileItemsList) {

            FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
            FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
            DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();
            rvLayoutManager.setAppendToBackMode();
            rvAdapter.reloadDataSets(fileNamesList, fileItemsList, false);
            rvAdapter.notifyItemRangeRemoved(0, itemCount);
            rvAdapter.notifyItemRangeChanged(0, fileItemsList.size());
            rvAdapter.notifyItemRangeInserted(fileItemsList.size() - itemCount, itemCount);
            rvLayoutManager.restoreDefaultMode();
            return true;

        }

        public static boolean prependItemsAtTopTrimmedFromBack(int itemCount,
                                                               List<String> fileNamesList, List<DisplayTypes.FileType> fileItemsList) {

            FileChooserRecyclerView mainRV = DisplayFragments.getInstance().getMainRecyclerView();
            FileChooserRecyclerView.LayoutManager rvLayoutManager = (FileChooserRecyclerView.LayoutManager) mainRV.getLayoutManager();
            DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainRV.getAdapter();
            rvLayoutManager.setAppendToBackMode();
            rvAdapter.reloadDataSets(fileNamesList, fileItemsList, false);
            rvAdapter.notifyItemRangeRemoved(fileNamesList.size() - itemCount, itemCount);
            rvAdapter.notifyItemRangeChanged(0, fileItemsList.size());
            rvAdapter.notifyItemRangeInserted(0, itemCount);
            rvLayoutManager.restoreDefaultMode();
            return true;

        }*/

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
            resetLayout(layoutContainer, fileItem, displayPositionIndex);
        }

        public View getLayoutContainer() {
            return layoutContainer;
        }

        public static void resetLayout(View layoutContainer, DisplayTypes.FileType fileItem, int displayPosition) {

            ImageView fileTypeIcon = layoutContainer.findViewById(R.id.fileTypeIcon);
            if(!fileItem.isDirectory()) {
                if(!fileItem.isHidden()) {
                    fileTypeIcon.setImageDrawable(getInstance().fileIconInst);
                }
                else {
                    fileTypeIcon.setImageDrawable(getInstance().hiddenFileIconInst);
                }
            }
            else {
                fileTypeIcon.setImageDrawable(getInstance().folderIconInst);
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
            /*final int cboxDisplayIndexPos = displayPosition;
            selectionBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View cboxView) {
                    int fileItemIndex = getInstance().findFileItemIndexByLayout((View) cboxView.getParent());
                    if(fileItemIndex < 0) {
                        return;
                    }
                    DisplayTypes.FileType fileItemForCB = getInstance().activeFileItemsDataList.get(fileItemIndex);
                    if(fileItemForCB == null) {
                        return;
                    }
                    RecyclerView mainFileListRecyclerView = getMainRecyclerView();
                    CheckBox cbItem = (CheckBox) cboxView;
                    DisplayAdapters.BaseViewHolder.performNewFileItemClick(cbItem, fileItemForCB);
                }
            });*/
            /*selectionBox.setOnTouchListener(new View.OnTouchListener() {
                final WeakReference<DisplayTypes.FileType> fileItemRef = new WeakReference<>(fileItem);
                @Override
                public boolean onTouch(View cboxView, MotionEvent event) {
                    RecyclerView mainFileListRecyclerView = getMainRecyclerView();
                    if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
                        CheckBox cbItem = (CheckBox) cboxView;
                        int fileItemIndex = getInstance().findFileItemIndexByLayout((View) cboxView.getParent());
                        if(fileItemIndex < 0) {
                            return false;
                        }
                        DisplayTypes.FileType fileItemForCB = getInstance().activeFileItemsDataList.get(fileItemIndex);
                        if(fileItemForCB == null) {
                            return false;
                        }
                        DisplayAdapters.BaseViewHolder.performNewFileItemClick(cbItem, fileItemForCB);
                        return true; // prevents the checkbox from changing state automatically
                    }
                    return false;
                }
            });*/

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
                     ImageButton navBtn = (ImageButton) btnView;
                     String baseFolderTypeName = btnView.getTag().toString();
                     FileChooserBuilder.BaseFolderPathType baseFolderPathType = FileChooserBuilder.BaseFolderPathType.getInstanceByName(baseFolderTypeName);
                     BasicFileProvider.getInstance().selectBaseDirectoryByType(baseFolderPathType);
                     getInstance().initiateNewFolderLoad(baseFolderPathType);
                }
            };
            globalNavBackBtn.setOnClickListener(backBtnClickListener);
            return folderNavFragment;
        }
    }

    public void cancelAllOperationsInProgress() {
        pathHistoryStack.clear();
    }

}
