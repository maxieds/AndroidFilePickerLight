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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class DisplayFragments {

    private static String LOGTAG = DisplayFragments.class.getSimpleName();

    public static RecyclerView mainFileListRecyclerView = null;
    public static LinearLayoutManager rvLayoutManager = null;
    public static DisplayAdapters.FileListAdapter rvAdapter = null;
    public static boolean recyclerViewAdapterInit = false;

    public static int maxAllowedSelections = 0;
    public static int curSelectionCount = 0;
    public static boolean allowSelectFiles = true;
    public static boolean allowSelectFolders = true;
    private static boolean isLoadingFileData = false;

    private static  DisplayTypes.DirectoryResultContext cwdFolderContext = null;
    public static   List<DisplayTypes.FileType> activeSelectionsList = new ArrayList<DisplayTypes.FileType>();
    public static  List<DisplayTypes.FileType> activeFileItemsDataList = new ArrayList<DisplayTypes.FileType>();
    private static  List<String> fileItemBasePathsList = new ArrayList<String>();

    private static final int DEFAULT_VIEWPORT_FILE_ITEMS_COUNT = 10;
    private static int viewportMaxFileItemsCount = DEFAULT_VIEWPORT_FILE_ITEMS_COUNT;
    private static int lastFileDataStartIndex = 0, lastFileDataEndIndex = -1;

    public static int getViewportMaxFilesCount() {
        return viewportMaxFileItemsCount;
    }
    public static void setViewportMaxFilesCount(int viewportFilesCap) {
        viewportMaxFileItemsCount = viewportFilesCap;
    }

    public static View tempViewportLayoutParam = null;

    public static void resetViewportMaxFilesCount(View parentViewContainer) {
        View fileItemDisplay = View.inflate(FileChooserActivity.getInstance(), R.layout.single_file_entry_item, null);
        tempViewportLayoutParam = fileItemDisplay;
        final LinearLayout subjLayout = (LinearLayout) parentViewContainer;
        final ViewTreeObserver vtObserver = subjLayout.getViewTreeObserver();
        vtObserver.addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int fileItemDisplayHeight = DisplayFragments.tempViewportLayoutParam.getMeasuredHeight();
                        int viewportDisplayHeight = parentViewContainer.getMeasuredHeight();
                        DisplayFragments.setViewportMaxFilesCount((int) Math.ceil((double) viewportDisplayHeight / fileItemDisplayHeight));
                        Log.i(LOGTAG, String.format("VP Height = %d, FItemDisp Height = %d   ====>  %d", viewportDisplayHeight, fileItemDisplayHeight, DisplayFragments.getViewportMaxFilesCount()));
                        vtObserver.removeOnGlobalLayoutListener(this);
                        DisplayFragments.tempViewportLayoutParam = null;
                        FileChooserActivity.loadInitialBaseFolderHandler.post(FileChooserActivity.loadInitialBaseFolderRunner);
                    }
                });
    }

    private static final String EMPTY_FOLDER_HISTORY_PATH = "----";

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

    public static void initializeRecyclerViewLayout(RecyclerView rview) {
        if(!recyclerViewAdapterInit) {
            fileItemBasePathsList = new ArrayList<String>();
            activeFileItemsDataList = new ArrayList<DisplayTypes.FileType>();
            mainFileListRecyclerView = rview;
            mainFileListRecyclerView.setHasFixedSize(true);
            rvAdapter = new DisplayAdapters.FileListAdapter(fileItemBasePathsList);
            mainFileListRecyclerView.setAdapter(rvAdapter);
            rvLayoutManager = new LinearLayoutManager(rview.getContext());
            rvLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            mainFileListRecyclerView.setLayoutManager(rvLayoutManager);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            mainFileListRecyclerView.setLayoutParams(layoutParams);
            mainFileListRecyclerView.addItemDecoration(
                    new CustomDividerItemDecoration(R.drawable.rview_file_item_divider)
            );
            //mainFileListRecyclerView.setNestedScrollingEnabled(false);
            resetRecyclerViewScrollListener();
            recyclerViewAdapterInit = true;
        }
    }

    private static void resetRecyclerViewScrollListener() {
        mainFileListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int nextState) {
                super.onScrollStateChanged(recyclerView, nextState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int deltaX, int deltaY) {
                super.onScrolled(recyclerView, deltaX, deltaY);
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!cwdFolderContext.getRunningDataThreadStatus()) {
                    if (linearLayoutManager != null && linearLayoutManager.findLastCompletelyVisibleItemPosition() == activeFileItemsDataList.size() - 1) {
                        cwdFolderContext.computeDirectoryContents(lastFileDataEndIndex + 1, lastFileDataEndIndex + getViewportMaxFilesCount());
                        isLoadingFileData = true;
                    }
                }
            }
        });
    }
    public static void resetRecyclerViewLayoutContext() {
        activeSelectionsList.clear();
        activeFileItemsDataList.clear();
        fileItemBasePathsList.clear();
        isLoadingFileData = false;
        cwdFolderContext = null;
        lastFileDataStartIndex = 0;
        lastFileDataEndIndex = -1;
        mainFileListRecyclerView = null;
        rvLayoutManager = null;
        viewportMaxFileItemsCount = DEFAULT_VIEWPORT_FILE_ITEMS_COUNT;
        rvAdapter = null;
        recyclerViewAdapterInit = false;
    }

    public static FileFilter.FileFilterInterface localFilesListFilter = null;
    public static FileFilter.FileItemsListSortFunc localFilesListSortFunc = null;

    public static void descendIntoNextDirectory(boolean initNewFileTree) {
        if(DisplayTypes.DirectoryResultContext.pathHistoryStack.empty()) {
            DisplayFragments.cancelAllOperationsInProgress();
            FileChooserException.GenericRuntimeErrorException rte = new FileChooserException.GenericRuntimeErrorException("Empty context for folder history ( no more history ??? )");
            FileChooserActivity.getInstance().postSelectedFilesActivityResult(rte);
        }
        DisplayTypes.DirectoryResultContext nextFolder = DisplayTypes.DirectoryResultContext.pathHistoryStack.peek();
        nextFolder.computeDirectoryContents(lastFileDataEndIndex + 1, lastFileDataEndIndex + getViewportMaxFilesCount());
        //DisplayFragments.displayNextDirectoryFilesList(nextFolder, nextFolder.getWorkingDirectoryContents());
    }

    /* Re-initiate the inquisition: Static reusable wrapper function to invoke loading a new directory
     * from scratch (reinitializing objects, starting the initial root query, and launching the
     * RecyclerView pattern making compendia on a whole new dataset):
     */
    public static void initiateNewFolderLoad(FileChooserBuilder.BaseFolderPathType initBaseFolder) {
        DisplayTypes.DirectoryResultContext newCwdContext = DisplayTypes.DirectoryResultContext.probeAtCursoryFolderQuery(initBaseFolder);
        DisplayTypes.DirectoryResultContext.pathHistoryStack.push(newCwdContext);
        newCwdContext.computeDirectoryContents(lastFileDataEndIndex + 1, lastFileDataEndIndex + getViewportMaxFilesCount());
        //DisplayFragments.displayNextDirectoryFilesList(newCwdContext, newCwdContext.getWorkingDirectoryContents());
    }

    private static final int VIEW_TYPE_FILE_ITEM = 0;

    public static void displayNextDirectoryFilesList(DisplayTypes.DirectoryResultContext cwdCtx, List<DisplayTypes.FileType> workingDirContentsList) {

        if(!recyclerViewAdapterInit) {
            initializeRecyclerViewLayout(mainFileListRecyclerView);
        }
        DisplayFragments.FolderNavigationFragment.dirsOneBackText.setText(folderHistoryOneBackPath);
        DisplayFragments.FolderNavigationFragment.dirsTwoBackText.setText(folderHistoryTwoBackPath);
        activeSelectionsList.clear();
        fileItemBasePathsList.clear();
        activeFileItemsDataList = workingDirContentsList;
        cwdFolderContext = cwdCtx;
        lastFileDataEndIndex = lastFileDataStartIndex + workingDirContentsList.size();
        lastFileDataStartIndex = lastFileDataEndIndex--; // reset so we have accurate indices for the next load operation

        //List<DisplayTypes.FileType> filteredFileContents = FileChooserBuilder.filterAndSortFileItemsList(workingDirContentsList, localFilesListFilter, localFilesListSortFunc); // TODO: Deal with this use case later ...
        List<DisplayTypes.FileType> filteredFileContents = workingDirContentsList;
        fileItemBasePathsList.clear();
        //rvLayoutManager.removeAllViews();
        //rvAdapter.notifyDataSetChanged();

        int fileItemIndex = 0;
        for(DisplayTypes.FileType fileItem : filteredFileContents) {
            fileItemBasePathsList.add(fileItem.getBaseName());
            //rvAdapter.notifyDataSetChanged();
            //DisplayFragments.FileListItemFragment fileItemUIFragment = new DisplayFragments.FileListItemFragment(fileItem, fileItemIndex);
            //fileItem.setLayoutContainer(fileItemUIFragment.getLayoutContainer());
            //DisplayAdapters.BaseViewHolder viewHolderAtIndex = rvAdapter.createViewHolder((ViewGroup) fileItemUIFragment.getLayoutContainer(), VIEW_TYPE_FILE_ITEM);
            //viewHolderAtIndex.setFileItemData(fileItem);
            //rvAdapter.bindViewHolder(viewHolderAtIndex, fileItemIndex);
            //rvAdapter.notifyDataSetChanged();
        }
        //rvAdapter.notifyItemRangeInserted(0, fileItemBasePathsList.size() - 1);
        //rvAdapter.notifyDataSetChanged();
        rvAdapter = new DisplayAdapters.FileListAdapter(fileItemBasePathsList);
        mainFileListRecyclerView.setAdapter(rvAdapter);
        rvLayoutManager = new LinearLayoutManager(mainFileListRecyclerView.getContext());
        rvLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //rvLayoutManager.setAutoMeasureEnabled(false);
        mainFileListRecyclerView.setLayoutManager(rvLayoutManager);

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

        public static void resetLayout(View layoutContainer, DisplayTypes.FileType fileItem, int displayPosition) {

            ImageView fileTypeIcon = layoutContainer.findViewById(R.id.fileTypeIcon);
            fileTypeIcon.setImageDrawable(fileItem.getFileTypeIcon());
            TextView fileSizeText = layoutContainer.findViewById(R.id.fileEntrySizeText);
            fileSizeText.setText(fileItem.getFileSizeString());
            TextView filePermsSummary = layoutContainer.findViewById(R.id.fileEntryPermsSummaryText);
            filePermsSummary.setText(fileItem.getChmodStylePermissions());
            TextView fileBaseNameDisplayText = layoutContainer.findViewById(R.id.fileEntryBaseName);
            fileBaseNameDisplayText.setText(fileItem.getBaseName());

            boolean displaySelectionBox = (fileItem.isDirectory() && allowSelectFolders) || (!fileItem.isDirectory() && allowSelectFiles);
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
                    CheckBox cbItem = (CheckBox) cboxView;
                    DisplayTypes.FileType fileItemForCB = ((DisplayAdapters.BaseViewHolder)
                            mainFileListRecyclerView.findViewHolderForAdapterPosition(cboxDisplayIndexPos)).getFileItemReference();
                    DisplayAdapters.BaseViewHolder.performNewFileItemClick(cbItem, fileItemForCB);
                }
            });
            selectionBox.setOnTouchListener(new View.OnTouchListener() {
                final WeakReference<DisplayTypes.FileType> fileItemRef = new WeakReference<>(fileItem);
                @Override
                public boolean onTouch(View cboxView, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_UP) {
                        CheckBox cbItem = (CheckBox) cboxView;
                        DisplayTypes.FileType fileItemForCB = ((DisplayAdapters.BaseViewHolder)
                                mainFileListRecyclerView.findViewHolderForAdapterPosition(cboxDisplayIndexPos)).getFileItemReference();
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
                     DisplayFragments.initiateNewFolderLoad(baseFolderPathType);
                }
            };
            globalNavBackBtn.setOnClickListener(backBtnClickListener);
            return folderNavFragment;
        }
    }

    public static void cancelAllOperationsInProgress() {
        DisplayTypes.DirectoryResultContext cwdCtx = DisplayTypes.DirectoryResultContext.pathHistoryStack.peek();
        if(cwdCtx != null) {
            cwdCtx.interruptFetchDataThread(DisplayTypes.DirectoryResultContext.DEFAULT_TIMEOUT);
        }
    }

}
