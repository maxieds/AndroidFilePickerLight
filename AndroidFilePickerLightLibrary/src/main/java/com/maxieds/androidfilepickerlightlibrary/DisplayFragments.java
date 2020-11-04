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

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

public class DisplayFragments {

    private static String LOGTAG = DisplayFragments.class.getSimpleName();

    public static class FileListItemFragment {

        private View layoutContainer;
        private FileTypes.FileType localFileItem;
        private int displayPositionIndex;
        private boolean isCheckable;

        public FileListItemFragment(FileTypes.FileType fileItem, int displayPosition) {
            displayPositionIndex = displayPosition;
            isCheckable = true;
            localFileItem = fileItem;
            layoutContainer = View.inflate(FileChooserActivity.getInstance(), R.layout.single_file_entry_item, null);
            resetLayout(fileItem, displayPositionIndex);
        }

        public void resetLayout(FileTypes.FileType fileItem, int displayPosition) {
            displayPositionIndex = displayPosition;
            ImageView fileTypeIcon = layoutContainer.findViewById(R.id.fileTypeIcon);
            fileTypeIcon.setImageDrawable(localFileItem.getFileTypeIcon());
            TextView fileSizeText = layoutContainer.findViewById(R.id.fileEntrySizeText);
            fileSizeText.setText(localFileItem.getFileSizeString());
            TextView filePermsSummary = layoutContainer.findViewById(R.id.fileEntryPermsSummaryText);
            filePermsSummary.setText(localFileItem.getChmodStylePermissions());
            TextView fileBaseNameDisplayText = layoutContainer.findViewById(R.id.fileEntryBaseName);
            fileBaseNameDisplayText.setText(localFileItem.getBaseName());
            CheckBox selectFileCheckBox = layoutContainer.findViewById(R.id.fileSelectCheckBox);
            if(!isCheckable) {
                selectFileCheckBox.setEnabled(false);
                selectFileCheckBox.setVisibility(View.INVISIBLE);
            }
        }

        public void setSelectable(boolean enabled, boolean updateUI) {
            isCheckable = enabled;
            if(updateUI && !enabled) {
                CheckBox selectFileCheckBox = layoutContainer.findViewById(R.id.fileSelectCheckBox);
                selectFileCheckBox.setEnabled(false);
                selectFileCheckBox.setVisibility(View.INVISIBLE);
            }
            else if(updateUI) {
                CheckBox selectFileCheckBox = layoutContainer.findViewById(R.id.fileSelectCheckBox);
                selectFileCheckBox.setEnabled(true);
                selectFileCheckBox.setVisibility(View.VISIBLE);
            }
        }

        public View getLayoutContainer() {
            return layoutContainer;
        }

    }

}
