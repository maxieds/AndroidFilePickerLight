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

public interface DisplayConfigInterface {

    public static final String LOGTAG = DisplayConfigInterface.class.getSimpleName();

    public static DisplayConfigInterface getDefaultsInstance() {
        return null;
    }

    // pickerTitleText
    // navBarPrefixText
    // doneActionBtnText, check mark icon
    // cancelActionBtnTex, X mark icon
    // bgColor, fgColor, fgAccentColor
    // useGradientDisplays: Create the tool bar and main file display gradients with
    // the GradientDrawableFactory.Builder class ...
    // setup / configure the stock directory path nav btn icons
    // reset the default file type and directory folder display icons

}
