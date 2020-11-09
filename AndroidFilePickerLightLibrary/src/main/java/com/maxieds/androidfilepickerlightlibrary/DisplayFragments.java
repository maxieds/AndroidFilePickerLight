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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DisplayFragments {

    private static String LOGTAG = DisplayFragments.class.getSimpleName();

    private static DisplayFragments localStaticInst = new DisplayFragments();
    private RecyclerView recyclerView = null;

    private Drawable folderIconInst;
    private Drawable fileIconInst;
    private Drawable hiddenFileIconInst;

    public FileFilter.FileFilterBase localFilesListFilter;
    public FileFilter.FileItemsSortFunc localFilesListSortFunc;

    public DisplayFragments() {
        folderIconInst = GradientDrawableFactory.getDrawableFromResource(R.drawable.folder_icon32);
        fileIconInst = GradientDrawableFactory.getDrawableFromResource(R.drawable.generic_file_icon32);
        hiddenFileIconInst = GradientDrawableFactory.getDrawableFromResource(R.drawable.hidden_file_icon32);
        localFilesListFilter = null;
        localFilesListSortFunc = null;
    }

    public static RecyclerView getMainRecyclerView() {
        return getInstance().recyclerView;
    }

    public void setRecyclerView(RecyclerView rview) { recyclerView = rview; }

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
    private List<String> fileItemBasePathsList = new ArrayList<String>();
    public Stack<DisplayTypes.DirectoryResultContext> pathHistoryStack;

    public int findFileItemIndexByLayout(View layoutDisplayView) {
        int fileItemIndex = 0;
        for(DisplayTypes.FileType fileItem : activeFileItemsDataList) {
            if(fileItem.getLayoutContainer() != null && fileItem.getLayoutContainer().equals(layoutDisplayView)) { // ??? TODO: Does this work ???
                return fileItemIndex;
            }
            ++fileItemIndex;
        }
        return -1;
    }

    public static final int SCROLL_QUEUE_BUFFER_SIZE = 8;
    public static final int DEFAULT_VIEWPORT_FILE_ITEMS_COUNT = 16 + SCROLL_QUEUE_BUFFER_SIZE; // large enough to overfill the window on first load
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

    public void initializeRecyclerViewLayout(RecyclerView rview) {

        if(!recyclerViewAdapterInit) {
            setRecyclerView(rview);
            fileItemBasePathsList = new ArrayList<String>();
            activeSelectionsList = new ArrayList<DisplayTypes.FileType>();
            activeFileItemsDataList = new ArrayList<DisplayTypes.FileType>();
            rview.setHasFixedSize(false);
            rview.setItemViewCacheSize(0);
            rview.setNestedScrollingEnabled(false);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            rview.setLayoutParams(layoutParams);
            LinearLayoutManager rvLayoutManager = new LinearLayoutManager(FileChooserActivity.getInstance()) {
                @Override
                public boolean isAutoMeasureEnabled() {
                    return true;
                }
            };
            rvLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            rvLayoutManager.setAutoMeasureEnabled(true);
            rvLayoutManager.setStackFromEnd(true);
            rview.setLayoutManager((RecyclerView.LayoutManager) rvLayoutManager);
            //rview.addItemDecoration(
            //        new CustomDividerItemDecoration(R.drawable.rview_file_item_divider)
            //);
            DisplayAdapters.FileListAdapter rvAdapter = new DisplayAdapters.FileListAdapter(fileItemBasePathsList, activeFileItemsDataList);
            rview.setAdapter(rvAdapter);
            resetRecyclerViewLayoutContext();
            resetRecyclerViewScrollListeners();
            recyclerViewAdapterInit = true;
        }

    }

    private void resetRecyclerViewScrollListeners() {
        RecyclerView mainFileListRecyclerView = getMainRecyclerView();
        mainFileListRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            private static final int SCROLL_BY_ITEMS = SCROLL_QUEUE_BUFFER_SIZE / 2;
            boolean haveProcessedInitScroll = false;

            private boolean invokeNewDataLoader() {
                RecyclerView mainFileListRecyclerView = getMainRecyclerView();
                LinearLayoutManager rvLayoutManager = (LinearLayoutManager) mainFileListRecyclerView.getLayoutManager();
                BasicFileProvider fpInst = BasicFileProvider.getInstance();
                DisplayTypes.DirectoryResultContext cwdFolderContextLocal = getCwdFolderContext();
                if(cwdFolderContextLocal == null) {
                    Log.i(LOGTAG, "invokeNewDataLoader: CWD CONTEXT IS NULL!");
                    return false;
                }
                int nextFileItemsLength = getViewportMaxFilesCount();
                fpInst.setFilesListLength(SCROLL_BY_ITEMS);
                if (rvLayoutManager.findLastCompletelyVisibleItemPosition() >= nextFileItemsLength - 1) {
                    // Have reached the last item in the list (queue more files below to trigger scrolling):
                    Log.i(LOGTAG, "onScrollStateChanged: SCROLLING DOWN CASE");
                    getInstance().lastFileDataEndIndex += SCROLL_BY_ITEMS;
                    getInstance().lastFileDataStartIndex += SCROLL_BY_ITEMS;
                    cwdFolderContextLocal.computeDirectoryContents(getInstance().lastFileDataStartIndex, getInstance().lastFileDataEndIndex, SCROLL_BY_ITEMS, 0, SCROLL_BY_ITEMS, true);
                    displayNextDirectoryFilesList(cwdFolderContextLocal.getWorkingDirectoryContents());
                    return true;
                }
                else if (rvLayoutManager.findFirstCompletelyVisibleItemPosition() == 0 &&
                         fileItemBasePathsList.size() >= nextFileItemsLength) {
                    // Have reached the first item in the list (queue more files above to trigger scrolling):
                    Log.i(LOGTAG, "onScrollStateChanged: SCROLLING UP CASE");
                    if (getInstance().lastFileDataStartIndex == 0) { // cannot scroll more above:
                        return false;
                    }
                    getInstance().lastFileDataStartIndex = Math.max(0, getInstance().lastFileDataStartIndex - SCROLL_BY_ITEMS);
                    getInstance().lastFileDataEndIndex = Math.max(0, getInstance().lastFileDataEndIndex - SCROLL_BY_ITEMS);
                    cwdFolderContextLocal.computeDirectoryContents(getInstance().lastFileDataStartIndex, getInstance().lastFileDataEndIndex, 0, SCROLL_BY_ITEMS, -SCROLL_BY_ITEMS, true);
                    displayNextDirectoryFilesList(cwdFolderContextLocal.getWorkingDirectoryContents());
                    fpInst.setFilesListLength(getViewportMaxFilesCount());
                    return true;
                }
                return false;
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int nextState) {
                Log.i(LOGTAG, "onScrollStateChanged");
                DisplayTypes.DirectoryResultContext cwdFolderContextLocal = getCwdFolderContext();
                if(cwdFolderContextLocal == null) {
                    Log.i(LOGTAG, "onScrollStateChanged: CWD CONTEXT IS NULL!");
                    return;
                }
                BasicFileProvider fpInst = BasicFileProvider.getInstance();
                int prevFileItemsLength = getInstance().viewportMaxFileItemsCount;
                if(!getInstance().viewportCapacityMesaured && getInstance().getMainRecyclerView().getLayoutManager().getChildCount() != 0) {
                    fileItemDisplayHeight = getMainRecyclerView().getLayoutManager().getChildAt(0).getMeasuredHeight();
                    if (fileItemDisplayHeight > 0) {
                        getInstance().resetViewportMaxFilesCount(FileChooserActivity.getInstance().findViewById(R.id.mainRecyclerViewContainer));
                    }
                }
                int indexingByNewSizeDiff = getInstance().viewportMaxFileItemsCount - prevFileItemsLength;
                // First, we need to to adjust the working list by any differences in size
                // caused by reshaping the viewportsize. This needs to be done as soon as possible
                // to prevent odd behaviors when we wait until we should actually perform the
                // scrolling operation.
                if(indexingByNewSizeDiff != 0) {
                    Log.i(LOGTAG, "Renormalizing the display size to viewport filling count");
                    int initTrimFromBackCount = indexingByNewSizeDiff < 0 ? -indexingByNewSizeDiff : 0;
                    fpInst.setFilesListLength(Math.abs(indexingByNewSizeDiff));
                    cwdFolderContextLocal.computeDirectoryContents(
                            getInstance().lastFileDataStartIndex,
                            Math.min(getInstance().lastFileDataStartIndex, getInstance().lastFileDataEndIndex + indexingByNewSizeDiff),
                            0, initTrimFromBackCount, Math.abs(indexingByNewSizeDiff), true
                    );
                    displayNextDirectoryFilesList(cwdFolderContextLocal.getWorkingDirectoryContents());
                }
                // Check a corner case to ensure smoother scrolling:
                if (nextState == RecyclerView.SCROLL_STATE_SETTLING) {
                    getMainRecyclerView().stopScroll();
                    //getMainRecyclerView().stopNestedScroll(ViewCompat.TYPE_TOUCH);
                }
                invokeNewDataLoader();
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int deltaX, int deltaY) {
                /*if(haveProcessedInitScroll) {
                    invokeNewDataLoader();
                }
                else {
                    haveProcessedInitScroll = true;
                }*/
                super.onScrolled(recyclerView, deltaX, deltaY);
            }
        });
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
            DisplayFragments.FolderNavigationFragment.dirsOneBackText.setText(folderHistoryOneBackPath); // TODO ???
            DisplayFragments.FolderNavigationFragment.dirsTwoBackText.setText(folderHistoryTwoBackPath); // TODO ???
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
        DisplayFragments.FolderNavigationFragment.dirsOneBackText.setText(folderHistoryOneBackPath); // TODO ???
        DisplayFragments.FolderNavigationFragment.dirsTwoBackText.setText(folderHistoryTwoBackPath); // TODO ???
    }

    private static final int VIEW_TYPE_FILE_ITEM = 0;

    public void displayNextDirectoryFilesList(List<DisplayTypes.FileType> workingDirContentsList) {

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
        LinearLayoutManager rvLayoutManager = (LinearLayoutManager) mainFileListRecyclerView.getLayoutManager();
        //rvLayoutManager.removeAllViews();
        DisplayAdapters.FileListAdapter rvAdapter = (DisplayAdapters.FileListAdapter) mainFileListRecyclerView.getAdapter();

        final List<DisplayTypes.FileType> filteredFileContents = workingDirContentsList;
        for(int fidx = 0; fidx < filteredFileContents.size(); fidx++) {
            DisplayTypes.FileType fileItem = filteredFileContents.get(fidx);
            fileItemBasePathsList.add(fileItem.getBaseName());
            activeFileItemsDataList.add(fileItem);
        }
        //getMainRecyclerView().setAdapter(new DisplayAdapters.FileListAdapter(fileItemBasePathsList, activeFileItemsDataList));
        //rvAdapter.notifyDataSetChanged();
        rvAdapter.reloadDataSets(fileItemBasePathsList, activeFileItemsDataList);
        /*mainFileListRecyclerView.post(new Runnable() {
            public void run() {
                ((DisplayAdapters.FileListAdapter) mainFileListRecyclerView.getAdapter()).reloadDataSets(fileItemBasePathsList, activeFileItemsDataList);
            }
        });*/

    }

    public static class CustomDividerItemDecoration extends RecyclerView.ItemDecoration {

        private static final int[] DIVIDER_DEFAULT_ATTRS = new int[]{
                android.R.attr.listDivider,
                android.R.attr.verticalDivider,
                android.R.attr.horizontalDivider
        };
        private Drawable listingsDivider;

        public static final int LIST_DIVIDER_STYLE_INDEX = 0;
        public static final int DEFAULT_DIVIDER_STYLE_INDEX = 1;

        public CustomDividerItemDecoration(Context ctx, int dividerTypeIndex, boolean dividerTypeIsVertical) {
            final TypedArray styledDefaultAttributes = ctx.obtainStyledAttributes(DIVIDER_DEFAULT_ATTRS);
            if(dividerTypeIndex != LIST_DIVIDER_STYLE_INDEX) {
                dividerTypeIndex = DEFAULT_DIVIDER_STYLE_INDEX + (dividerTypeIsVertical ? 0 : 1);
            }
            listingsDivider = styledDefaultAttributes.getDrawable(dividerTypeIndex);
            styledDefaultAttributes.recycle();
        }

        public CustomDividerItemDecoration(int resId) {
            listingsDivider = GradientDrawableFactory.getDrawableFromResource(resId);
        }

        public static void setMarginAdjustments(int leftAdjust, int topAdjust, int rightAdjust, int bottomAdjust) {
            MARGIN_RIGHT_ADJUST = rightAdjust;
            MARGIN_LEFT_ADJUST = leftAdjust;
            MARGIN_TOP_ADJUST = topAdjust;
            MARGIN_BOTTOM_ADJUST = bottomAdjust;
        }

        private static int MARGIN_RIGHT_ADJUST = 35;
        private static int MARGIN_LEFT_ADJUST = 35;
        private static int MARGIN_TOP_ADJUST = 0;
        private static int MARGIN_BOTTOM_ADJUST = 0;

        @Override
        public void onDraw(Canvas displayCanvas, RecyclerView parentContainerView, RecyclerView.State rvState) {

            int leftMargin = parentContainerView.getPaddingLeft() + MARGIN_LEFT_ADJUST;
            int rightMargin = parentContainerView.getWidth() - parentContainerView.getPaddingRight() - MARGIN_RIGHT_ADJUST;

            for (int i = 0; i < parentContainerView.getChildCount(); i++) {

                View childView = parentContainerView.getChildAt(i);
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childView.getLayoutParams();
                int topMargin = childView.getBottom() + params.bottomMargin + MARGIN_TOP_ADJUST;
                int bottomMargin = topMargin + listingsDivider.getIntrinsicHeight() + MARGIN_BOTTOM_ADJUST;
                listingsDivider.setBounds(leftMargin, topMargin, rightMargin, bottomMargin);
                listingsDivider.draw(displayCanvas);

            }

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
            final int cboxDisplayIndexPos = displayPosition;
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
            });
            selectionBox.setOnTouchListener(new View.OnTouchListener() {
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
            });

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
