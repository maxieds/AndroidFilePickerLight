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

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CustomThemeBuilder {

    private static final String LOGTAG = CustomThemeBuilder.class.getSimpleName();

    private static final int NULL_RESOURCE_ID = 0;

    private Activity activityCtx;

    public CustomThemeBuilder(Activity activityCtxRef) {
        activityCtx = activityCtxRef;
        pickerTitleTextResId = NULL_RESOURCE_ID;
        navBarPrefixTextResId = NULL_RESOURCE_ID;
        doneActionBtnTextResId = NULL_RESOURCE_ID;
        cancelActionBtnTextResId = NULL_RESOURCE_ID;
        globalBackBtnIconResId = NULL_RESOURCE_ID;
        doneActionBtnIconResId = NULL_RESOURCE_ID;
        cancelActionBtnIconResId = NULL_RESOURCE_ID;
        themeColorScheme = null;
        toolbarIconResId = NULL_RESOURCE_ID;
        navBtnIconResIdMap = new HashMap<FileChooserBuilder.BaseFolderPathType, Integer>();
        fileIconResId = fileHiddenIconResId = folderIconResId = NULL_RESOURCE_ID;
    }

    private int pickerTitleTextResId;
    private int navBarPrefixTextResId;
    private int doneActionBtnTextResId;
    private int cancelActionBtnTextResId;

    public CustomThemeBuilder setPickerTitleText(@StringRes int titleTextResId) {
        pickerTitleTextResId = titleTextResId;
        return this;
    }

    public CustomThemeBuilder setNavBarPrefixText(@StringRes int prefixTextResId) {
        navBarPrefixTextResId = prefixTextResId;
        return this;
    }

    public CustomThemeBuilder setDoneActionButtonText(@StringRes int btnTextResId) {
        doneActionBtnTextResId = btnTextResId;
        return this;
    }

    public CustomThemeBuilder setCancelActionButtonText(@StringRes int btnTextResId) {
        cancelActionBtnTextResId = btnTextResId;
        return this;
    }

    private int globalBackBtnIconResId;
    private int doneActionBtnIconResId;
    private int cancelActionBtnIconResId;

    public CustomThemeBuilder setGlobalBackButtonIcon(@DrawableRes int backIconResId) {
        globalBackBtnIconResId = backIconResId;
        return this;
    }

    public static int getGlobalBackButtonIconDimension() {
        return 32; // pixels
    }

    public CustomThemeBuilder setDoneActionButtonIcon(@DrawableRes int checkMarkResId) {
        doneActionBtnIconResId = checkMarkResId;
        return this;
    }

    public CustomThemeBuilder setCancelActionButtonIcon(@DrawableRes int xMarkResId) {
        cancelActionBtnIconResId = xMarkResId;
        return this;
    }

    public static int getActionButtonIconDimension() {
        return 24; // pixels
    }

    public static final int COLOR_PRIMARY = 0;
    public static final int COLOR_PRIMARY_DARK = 1;
    public static final int COLOR_PRIMARY_VERY_DARK = 2;
    public static final int COLOR_ACCENT = 3;
    public static final int COLOR_ACCENT_MEDIUM = 4;
    public static final int COLOR_ACCENT_LIGHT = 5;
    public static final int COLOR_TOOLBAR_BG = 6;
    public static final int COLOR_TOOLBAR_FG = 7;
    public static final int COLOR_TOOLBAR_NAV = 8;
    public static final int COLOR_TOOLBAR_DIVIDER = 9;

    public static class FileChooserColorScheme {

        private Activity activityCtx;
        private int[] themeColorsList;

        public FileChooserColorScheme(Activity activityCtxRef, int[] colorsList) throws FileChooserException.AndroidFilePickerLightException {
            if(activityCtxRef == null) {
                throw new FileChooserException.InvalidActivityContextException();
            }
            activityCtx = activityCtxRef;
            if(colorsList.length != 10) {
                throw new FileChooserException.InvalidIndexException();
            }
            themeColorsList = colorsList;
        }

        public int getColorPrimary() {
            return DisplayUtils.resolveColorFromResId(activityCtx, themeColorsList[COLOR_PRIMARY]);
        }

        public int getColorPrimaryDark() {
            return DisplayUtils.resolveColorFromResId(activityCtx, themeColorsList[COLOR_PRIMARY_DARK]);
        }

        public int getColorPrimaryVeryDark() {
            return DisplayUtils.resolveColorFromResId(activityCtx, themeColorsList[COLOR_PRIMARY_VERY_DARK]);
        }

        public int getColorAccent() {
            return DisplayUtils.resolveColorFromResId(activityCtx, themeColorsList[COLOR_ACCENT]);
        }

        public int getColorAccentMedium() {
            return DisplayUtils.resolveColorFromResId(activityCtx, themeColorsList[COLOR_ACCENT_MEDIUM]);
        }

        public int getColorAccentLight() {
            return DisplayUtils.resolveColorFromResId(activityCtx, themeColorsList[COLOR_ACCENT_LIGHT]);
        }

        public int getColorToolbarBG() {
            return DisplayUtils.resolveColorFromResId(activityCtx, themeColorsList[COLOR_TOOLBAR_BG]);
        }

        public int getColorToolbarFG() {
            return DisplayUtils.resolveColorFromResId(activityCtx, themeColorsList[COLOR_TOOLBAR_FG]);
        }

        public int getColorToolbarNav() {
            return DisplayUtils.resolveColorFromResId(activityCtx, themeColorsList[COLOR_TOOLBAR_NAV]);
        }

        public int getColorToolbarDivider() {
            return DisplayUtils.resolveColorFromResId(activityCtx, themeColorsList[COLOR_TOOLBAR_DIVIDER]);
        }

        public static int[] GenerateThemeColorsList(Activity activityCtxRef, @ColorRes int baseColorResId) {
            int resolvedColor = baseColorResId;
            try {
                resolvedColor =  DisplayUtils.getColorFromResource(activityCtxRef, baseColorResId);
            } catch(Exception ex) {
                try {
                    resolvedColor =  DisplayUtils.resolveColorFromAttribute(activityCtxRef, baseColorResId);
                } catch (Exception ex2) {}
            }
            return new int[] {
                    resolvedColor,
                    DisplayUtils.darkenColor(resolvedColor, 0.75f),
                    DisplayUtils.darkenColor(resolvedColor, 0.86f),
                    DisplayUtils.lightenColor(resolvedColor, 0.73f),
                    DisplayUtils.lightenColor(resolvedColor, 0.50f),
                    DisplayUtils.lightenColor(resolvedColor, 0.85f),
                    DisplayUtils.darkenColor(resolvedColor, 0.90f),
                    DisplayUtils.lightenColor(resolvedColor, 0.87f),
                    DisplayUtils.lightenColor(resolvedColor, 0.54f),
                    DisplayUtils.lightenColor(resolvedColor, 0.27f)
            };
        }

    }

    private FileChooserColorScheme themeColorScheme;

    public CustomThemeBuilder setThemeColors(@ColorRes int[] colorsList) {
        themeColorScheme = new FileChooserColorScheme(activityCtx, colorsList);
        return this;
    }

    public CustomThemeBuilder generateThemeColors(@ColorRes int baseColorResId) {
        int[] themeColorsList = FileChooserColorScheme.GenerateThemeColorsList(activityCtx, baseColorResId);
        themeColorScheme = new FileChooserColorScheme(activityCtx, themeColorsList);
        return this;
    }

    private int toolbarIconResId;

    public CustomThemeBuilder setActivityToolbarIcon(@DrawableRes int customIconResId) {
        toolbarIconResId = customIconResId;
        return this;
    }

    public static int getToolbarIconDimension() {
        return 48; // pixels
    }

    private Map<FileChooserBuilder.BaseFolderPathType, Integer> navBtnIconResIdMap;

    public CustomThemeBuilder setNavigationByPathButtonIcon(@DrawableRes int iconResId, FileChooserBuilder.BaseFolderPathType whichBtnType) {
        navBtnIconResIdMap.put(whichBtnType, iconResId);
        return this;
    }

    public static int getNavigationPathButtonIconDimension() {
        return 32; // pixels
    }

    public interface FileChooserActivityMainLayoutStylizer {
        boolean styleNavigationDisplayToolbar(Toolbar toolbar);
        boolean styleNavigationByPathsBaseDisplay(TextView tvNavBtnsDirective);
        boolean styleDefaultPathNavigationButton(ImageButton dirNavBtn, FileChooserBuilder.BaseFolderPathType baseFolderBtnType) throws RuntimeException;
        boolean styleBottomNavigationActionButtons(Button doneActionBtn, Button cancelActionBtn);
        boolean styleMainActivityLayout(View parentContainerLayoutView) throws RuntimeException;
        boolean styleMainActivityWindow(Window mainActivityWin) throws RuntimeException;
    }

    public FileChooserActivityMainLayoutStylizer createActivityMainLayoutStylizer() {

        final Drawable _toolbarLogoIconFinal = DisplayUtils.firstNonNull(
                DisplayUtils.resolveDrawableFromResId(activityCtx, toolbarIconResId),
                DisplayUtils.resolveDrawableFromResId(R.drawable.file_chooser_default_toolbar_icon48)
        );
        final String _pickerTitleTextFinal = DisplayUtils.firstNonNull(
                DisplayUtils.resolveStringFromResId(activityCtx, pickerTitleTextResId),
                DisplayUtils.resolveStringFromResId(R.string.filePickerTitleText)
        );
        final String _navBarPrefixTextFinal = DisplayUtils.firstNonNull(
                DisplayUtils.resolveStringFromResId(activityCtx, navBarPrefixTextResId),
                DisplayUtils.resolveStringFromResId(R.string.filePickerNavBarText)
        );
        final String _doneActionBtnTextFinal = DisplayUtils.firstNonNull(
                DisplayUtils.resolveStringFromResId(activityCtx, doneActionBtnTextResId),
                DisplayUtils.resolveStringFromResId(R.string.filePickerDoneActionButtonText)
        );
        final String _cancelActionBtnTextFinal = DisplayUtils.firstNonNull(
                DisplayUtils.resolveStringFromResId(activityCtx, cancelActionBtnTextResId),
                DisplayUtils.resolveStringFromResId(R.string.filePickerCancelActionButtonText)
        );
        final Drawable _globalBackBtnIconFinal = DisplayUtils.firstNonNull(
                DisplayUtils.resolveDrawableFromResId(activityCtx, globalBackBtnIconResId),
                DisplayUtils.resolveDrawableFromResId(R.drawable.nav_back_button_icon32)
        );
        final Drawable _doneActionBtnIconFinal = DisplayUtils.firstNonNull(
                DisplayUtils.resolveDrawableFromResId(activityCtx, doneActionBtnIconResId),
                DisplayUtils.resolveDrawableFromResId(R.drawable.done_button_check_icon24)
        );
        final Drawable _cancelActionBtnIconFinal = DisplayUtils.firstNonNull(
                DisplayUtils.resolveDrawableFromResId(activityCtx, cancelActionBtnIconResId),
                DisplayUtils.resolveDrawableFromResId(R.drawable.cancel_button_x_icon24)
        );
        if (activityCtx == null || themeColorScheme == null || _pickerTitleTextFinal == null ||
                _navBarPrefixTextFinal == null || _doneActionBtnTextFinal == null || _cancelActionBtnTextFinal == null ||
                _toolbarLogoIconFinal == null || !DisplayUtils.checkIconDimensions(_toolbarLogoIconFinal, getToolbarIconDimension()) ||
                _globalBackBtnIconFinal == null || !DisplayUtils.checkIconDimensions(_globalBackBtnIconFinal, getGlobalBackButtonIconDimension()) ||
                _doneActionBtnIconFinal == null || !DisplayUtils.checkIconDimensions(_doneActionBtnIconFinal, getActionButtonIconDimension()) ||
                _cancelActionBtnIconFinal == null || !DisplayUtils.checkIconDimensions(_cancelActionBtnIconFinal, getActionButtonIconDimension())) {
            return null;
        }
        return new FileChooserActivityMainLayoutStylizer() {

            private final Activity _activityCtx = activityCtx;
            private final FileChooserColorScheme _themeColorScheme = themeColorScheme;
            private final Map<FileChooserBuilder.BaseFolderPathType, Integer> _navBtnIconResIdMap = navBtnIconResIdMap;
            private final Drawable _toolbarLogoIcon = _toolbarLogoIconFinal;
            private final String _pickerTitleText = _pickerTitleTextFinal;
            private final String _navBarPrefixText = _navBarPrefixTextFinal;
            private final String _doneActionBtnText = _doneActionBtnTextFinal;
            private final String _cancelActionBtnText = _cancelActionBtnTextFinal;
            private final Drawable _globalBackBtnIcon = _globalBackBtnIconFinal;
            private final Drawable _doneActionBtnIcon = _doneActionBtnIconFinal;
            private final Drawable _cancelActionBtnIcon = _cancelActionBtnIconFinal;

            @Override
            public boolean styleNavigationDisplayToolbar(Toolbar toolbar) {
                if(toolbar == null) {
                    return false;
                }
                toolbar.setSubtitle(String.format(Locale.getDefault(), "    ⇤%s⇥", _pickerTitleText));
                toolbar.setTitleTextColor(_themeColorScheme.getColorToolbarFG());
                toolbar.setSubtitleTextColor(_themeColorScheme.getColorToolbarFG());
                toolbar.setLogo(_toolbarLogoIcon);
                return true;
            }

            @Override
            public boolean styleDefaultPathNavigationButton(ImageButton dirNavBtn, FileChooserBuilder.BaseFolderPathType baseFolderBtnType) throws RuntimeException {
                if(dirNavBtn == null || baseFolderBtnType == null) {
                    return false;
                }
                dirNavBtn.setBackgroundColor(themeColorScheme.getColorToolbarNav());
                try {
                    boolean status = true;
                    Drawable navBtnIcon;
                    if (_navBtnIconResIdMap.get(baseFolderBtnType) != null) {
                        navBtnIcon = DisplayUtils.resolveDrawableFromResId(_activityCtx, _navBtnIconResIdMap.get(baseFolderBtnType));
                    } else {
                        status = false;
                        int defaultIconResId = FileChooserBuilder.DefaultNavFoldersType.NAV_FOLDER_ICON_RESIDS_MAP.get(
                                FileChooserBuilder.DefaultNavFoldersType.NAV_FOLDER_PATHS_REVMAP.get(baseFolderBtnType).ordinal()
                        ).intValue();
                        navBtnIcon = DisplayUtils.resolveDrawableFromAttribute(defaultIconResId);
                    }
                    dirNavBtn.setImageDrawable(navBtnIcon);
                    return status;
                } catch(NullPointerException npe) {
                    return false;
                }
            }

            @Override
            public boolean styleNavigationByPathsBaseDisplay(TextView tvNavBtnsDirective) {
                if(tvNavBtnsDirective == null) {
                    return false;
                }
                tvNavBtnsDirective.setText(_navBarPrefixText);
                tvNavBtnsDirective.setTextColor(_themeColorScheme.getColorPrimaryDark());
                return true;
            }

            @Override
            public boolean styleBottomNavigationActionButtons(Button doneActionBtn, Button cancelActionBtn) {
                if(doneActionBtn == null || cancelActionBtn == null) {
                    return false;
                }
                doneActionBtn.setBackgroundColor(_themeColorScheme.getColorAccentLight());
                doneActionBtn.setTextColor(_themeColorScheme.getColorAccentMedium());
                doneActionBtn.setText(_doneActionBtnText);
                doneActionBtn.setCompoundDrawables(_doneActionBtnIcon, null, null, null);
                doneActionBtn.setBackgroundColor(_themeColorScheme.getColorAccentLight());
                doneActionBtn.setTextColor(_themeColorScheme.getColorAccentMedium());
                doneActionBtn.setText(_cancelActionBtnText);
                doneActionBtn.setCompoundDrawables(_cancelActionBtnIcon, null, null, null);
                return true;
            }

            @Override
            public boolean styleMainActivityLayout(View parentContainerLayout) throws RuntimeException {

                if(parentContainerLayout == null) {
                    return false;
                }
                boolean opStatus = true;
                opStatus = opStatus && styleNavigationDisplayToolbar(parentContainerLayout.findViewById(R.id.mainLayoutToolbarActionBar));
                opStatus = opStatus && styleNavigationByPathsBaseDisplay(parentContainerLayout.findViewById(R.id.mainFileNavBtnsDirectiveText));
                opStatus = opStatus && styleBottomNavigationActionButtons(
                        parentContainerLayout.findViewById(R.id.mainNavBtnActionDone),
                        parentContainerLayout.findViewById(R.id.mainNavBtnActionCancel)
                );
                if(!opStatus) {
                    throw new RuntimeException("Unable to style main layout!");
                }

                ImageButton globalNavBackBtn = parentContainerLayout.findViewById(R.id.mainDirNavGlobalBackBtn);
                TextView tvPathHistoryOneBack = parentContainerLayout.findViewById(R.id.mainDirNavBackOnePathDisplayText);
                if(globalNavBackBtn == null || tvPathHistoryOneBack == null) {
                    throw new RuntimeException("Unable to style main layout!");
                }
                globalNavBackBtn.setImageDrawable(_globalBackBtnIcon);
                tvPathHistoryOneBack.setTextColor(_themeColorScheme.getColorPrimaryDark());

                // Set the remaining complete list of colors:
                // NOTE: This is probably the most tedious part of having to style things by hand ...
                LinearLayout parentLinearLayout = parentContainerLayout.findViewById(R.id.fileChooserActivityMainLayoutParentContainer);
                try {
                    if (parentLinearLayout == null) {
                        parentLinearLayout = (LinearLayout) parentContainerLayout;
                    }
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                View toolbarDivider1 = parentContainerLayout.findViewById(R.id.mainLayoutToolbarFirstShortDivider);
                LinearLayout navBtnsLinearLayout = parentContainerLayout.findViewById(R.id.mainFileNavBtnsContainer);
                View layoutDivider2 = parentContainerLayout.findViewById(R.id.mainLayoutShortDivider2);
                LinearLayout pathsHistoryContainer = parentContainerLayout.findViewById(R.id.mainDirPrevPathsNavContainer);
                View layoutDivider3 = parentContainerLayout.findViewById(R.id.mainLayoutShortDivider3);
                LinearLayout mainRecyclerViewContainerLayout = parentContainerLayout.findViewById(R.id.mainRecyclerViewContainer);
                View layoutDivider4 = parentContainerLayout.findViewById(R.id.mainLayoutShortDivider4);
                LinearLayout bottomActionBtnsContainerLayout = parentContainerLayout.findViewById(R.id.bottomActionButtonsNavLayoutContainer);
                if(parentLinearLayout == null || toolbarDivider1 == null || navBtnsLinearLayout == null ||
                        layoutDivider2 == null || pathsHistoryContainer == null ||
                        layoutDivider3 == null || mainRecyclerViewContainerLayout == null ||
                        layoutDivider4 == null || bottomActionBtnsContainerLayout == null) {
                    throw new RuntimeException("Unable to style main layout!");
                }
                parentLinearLayout.setBackgroundColor(_themeColorScheme.getColorAccent());
                toolbarDivider1.setBackgroundColor(_themeColorScheme.getColorToolbarDivider());
                navBtnsLinearLayout.setBackgroundColor(_themeColorScheme.getColorToolbarNav());
                layoutDivider2.setBackgroundColor(_themeColorScheme.getColorToolbarDivider());
                pathsHistoryContainer.setBackgroundColor(_themeColorScheme.getColorAccentLight());
                layoutDivider3.setBackgroundColor(_themeColorScheme.getColorToolbarDivider());
                mainRecyclerViewContainerLayout.setBackgroundColor(_themeColorScheme.getColorPrimary());
                layoutDivider4.setBackgroundColor(_themeColorScheme.getColorToolbarDivider());
                bottomActionBtnsContainerLayout.setBackgroundColor(_themeColorScheme.getColorAccentMedium());
                return true;

            }

            public boolean styleMainActivityWindow(Window mainActivityWin) throws RuntimeException {
                if(mainActivityWin == null) {
                    throw new RuntimeException("Unable to style main activity window!");
                }
                mainActivityWin.setTitleColor(_themeColorScheme.getColorToolbarBG());
                mainActivityWin.setStatusBarColor(_themeColorScheme.getColorPrimaryDark());
                mainActivityWin.setNavigationBarColor(_themeColorScheme.getColorPrimaryDark());
                return true;
            }

        };

    }

    private int fileIconResId;
    private int fileHiddenIconResId;
    private int folderIconResId;

    public CustomThemeBuilder setDefaultFileIcon(@DrawableRes int fileIconResId) {
        this.fileIconResId = fileIconResId;
        return this;
    }

    public CustomThemeBuilder setDefaultHiddenFileIcon(@DrawableRes int hfileIconResId) {
        this.fileHiddenIconResId = hfileIconResId;
        return this;
    }

    public CustomThemeBuilder setDefaultFolderIcon(@DrawableRes int folderIconResId) {
        this.folderIconResId = folderIconResId;
        return this;
    }

    public static int getFileItemEntryIconDimension() {
        return 16; // pixels
    }

    public interface FileItemLayoutStylizer {
        boolean setFileTypeIcon(ImageView imgBtn, DisplayTypes.FileType fileItemEntry);
        boolean styleSelectionBox(CompoundButton selectBox);
        boolean applyStyleToLayout(View parentViewContainer, DisplayTypes.FileType fileItemEntry) throws RuntimeException;
    }

    public FileItemLayoutStylizer createFileItemLayoutStylizer() {

        if(themeColorScheme == null || fileIconResId == NULL_RESOURCE_ID ||
                fileHiddenIconResId  == NULL_RESOURCE_ID || folderIconResId  == NULL_RESOURCE_ID) {
            return null;
        }
        final Drawable fileIconFinal = DisplayUtils.resolveDrawableFromResId(activityCtx, fileIconResId);
        final Drawable fileHiddenIconFinal = DisplayUtils.resolveDrawableFromResId(activityCtx, fileHiddenIconResId);
        final Drawable folderIconFinal = DisplayUtils.resolveDrawableFromResId(activityCtx, folderIconResId);
        Drawable[] drawablesCheckList = {
                fileIconFinal,
                fileHiddenIconFinal,
                folderIconFinal
        };
        for(int didx = 0; didx < drawablesCheckList.length; didx++) {
            Drawable drawInst = drawablesCheckList[didx];
            if(drawInst == null || !DisplayUtils.checkIconDimensions(drawInst, getFileItemEntryIconDimension())) {
                return null;
            }
        }
        return new FileItemLayoutStylizer() {

            private final Drawable _fileIcon = fileIconFinal;
            private final Drawable _fileHiddenIcon = fileHiddenIconFinal;
            private final Drawable _folderIcon = folderIconFinal;
            private final FileChooserColorScheme _themeColorScheme = themeColorScheme;

            @Override
            public boolean setFileTypeIcon(ImageView fileTypeIcon, DisplayTypes.FileType fileItemEntry) {
                if(fileTypeIcon == null || fileItemEntry == null) {
                    return false;
                }
                if(!fileItemEntry.isDirectory()) {
                    if(!fileItemEntry.isHidden()) {
                        fileTypeIcon.setImageDrawable(_fileIcon);
                    }
                    else {
                        fileTypeIcon.setImageDrawable(_fileHiddenIcon);
                    }
                }
                else {
                    fileTypeIcon.setImageDrawable(_folderIcon);
                }
                return true;
            }

            @Override
            public boolean styleSelectionBox(CompoundButton selectBox) {
                if(selectBox == null) {
                    return false;
                }
                int[][] cboxStatesList = new int[][] {
                        new int[] { -android.R.attr.state_checked },
                        new int[] { android.R.attr.state_checked },
                };
                int[] cboxTrackColors = new int[] {
                        _themeColorScheme.getColorPrimaryDark(),
                        _themeColorScheme.getColorPrimaryDark(),
                };
                selectBox.setButtonTintList(new ColorStateList(cboxStatesList, cboxTrackColors));
                return true;
            }

            @Override
            public boolean applyStyleToLayout(View parentViewContainer, DisplayTypes.FileType fileItemEntry) throws RuntimeException {
                if(parentViewContainer == null || fileItemEntry == null) {
                    throw new RuntimeException("Unable to style file item layout!");
                }
                LinearLayout containerLayout = parentViewContainer.findViewById(R.id.fileItemSingleEntryMainLayoutContainer);
                TextView tvPermsSummary = parentViewContainer.findViewById(R.id.fileEntryPermsSummaryText);
                TextView tvPropsDivider = parentViewContainer.findViewById(R.id.fileEntryPropsDividerText);
                TextView tvFileSize = parentViewContainer.findViewById(R.id.fileEntrySizeText);
                TextView tvFileBaseName = parentViewContainer.findViewById(R.id.fileEntryBaseName);
                if(containerLayout == null || tvPermsSummary == null || tvPropsDivider == null ||
                        tvFileSize == null || tvFileBaseName == null) {
                    throw new RuntimeException("Unable to style file item layout!");
                }
                else {
                    containerLayout.setBackgroundColor(_themeColorScheme.getColorPrimary());
                    tvPermsSummary.setTextColor(_themeColorScheme.getColorAccent());
                    tvPropsDivider.setTextColor(_themeColorScheme.getColorAccent());
                    tvFileSize.setTextColor(_themeColorScheme.getColorAccent());
                    tvFileBaseName.setTextColor(_themeColorScheme.getColorAccent());
                }
                boolean opStatus = true;
                opStatus = opStatus && styleSelectionBox(parentViewContainer.findViewById(R.id.fileSelectCheckBox));
                opStatus = opStatus && setFileTypeIcon(parentViewContainer.findViewById(R.id.fileTypeIcon), fileItemEntry);
                if(!opStatus) {
                    throw new RuntimeException("Unable to style file item layout!");
                }
                return true;
            }

        };

    }

}
