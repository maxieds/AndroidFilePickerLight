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

public class CustomThemeBuilder {

    private static final String LOGTAG = CustomThemeBuilder.class.getSimpleName();

    public static CustomThemeBuilder getDefaultsInstance() {
        return null;
    }

    public CustomThemeBuilder setPickerTitleText(String titleText) {
        return this;
    }

    public CustomThemeBuilder setNavBarPrefixText(String prefixText) {
        return this;
    }

    public CustomThemeBuilder setDoneActionButtonText(String btnText) {
        return this;
    }

    public CustomThemeBuilder setCancelActionButtonText(String btnText) {
        return this;
    }

    public CustomThemeBuilder setDoneActionButtonIcon(Drawable checkMark) {
        return this;
    }

    public CustomThemeBuilder setCancelActionButtonIcon(Drawable xMark) {
        return this;
    }

    public static final int COLOR_PRIMARY = 0;
    public static final int COLOR_PRIMARY_DARK = 1;
    public static final int COLOR_PRIMARY_VERY_DARK = 2;
    public static final int COLOR_ACCENT = 3;
    public static final int COLOR_ACCENT_MEDIUM = 4;
    public static final int COLOR_ACCENT_LIGHT = 5;
    public static final int COLOR_TOOLBAR_BG = 6;
    public static final int COLOR_TOOLBAR_FG = 7;

    public CustomThemeBuilder setThemeColors(int[] colorsList) {
        return this;
    }

    public CustomThemeBuilder useThemeGradientBackgrounds(boolean enable) {
        return this;
    }

    public CustomThemeBuilder setActivityToolbarIcon(Drawable customIcon) {
        return this;
    }

    // Should be 16x16 pixels and compressed with the WEBP format to save resources:
    public CustomThemeBuilder setDefaultFolderIcon(Drawable folderIcon) {
        return this;
    }

    // Should be 16x16 pixels and compressed with the WEBP format to save resources:
    public CustomThemeBuilder setDefaultFileIcon(Drawable folderIcon) {
        return this;
    }

    // Should be 16x16 pixels and compressed with the WEBP format to save resources:
    public CustomThemeBuilder setDefaultHidenFileIcon(Drawable folderIcon) {
        return this;
    }

    // TODO: setup / configure the stock directory path nav btn icons
    //       (excluding any custom file type icons, which the user can supply
    //       via a separate interface ... )

    public CustomThemeBuilder makeTheme() {
        return this; // should fill in defaults not defined by the client code
    }

}
