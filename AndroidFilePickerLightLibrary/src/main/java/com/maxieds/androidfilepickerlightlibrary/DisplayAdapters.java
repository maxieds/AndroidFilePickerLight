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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DisplayAdapters {

    private static String LOGTAG = DisplayAdapters.class.getSimpleName();

    public static class FileListAdapter extends RecyclerView.Adapter<BaseViewHolder> {

        private List<String> fileListData;
        public FileListAdapter(List<String> data){
            fileListData = data;
        }

        @Override
        public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_file_entry_item, parent, false);
            return new BaseViewHolder(rowItem);
        }

        @Override
        public void onBindViewHolder(BaseViewHolder holder, int position) {
            holder.getDisplayText().setText(fileListData.get(position));
        }

        @Override
        public int getItemCount() {
            return fileListData.size();
        }

    }

    public static class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        public View iconView;
        public TextView displayText;
        public FileTypes.FileType fileItem;

        public BaseViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            iconView = v.findViewById(R.id.fileTypeIcon);
            displayText = (TextView) v.findViewById(R.id.fileEntryBaseName);
        }

        public void setFileItemData(FileTypes.FileType storedFileItem) {
            fileItem = storedFileItem;
        }

        public TextView getDisplayText() { return displayText; }

        public boolean performNewFileItemClick(FileTypes.FileType fileItem) {
            if(!fileItem.isDirectory() && !DisplayFragments.allowSelectFiles) {
                return false;
            }
            else if(fileItem.isDirectory() && !DisplayFragments.allowSelectFolders) {
                return false;
            }
            else if(fileItem.isChecked()) {
                // Deselect: uncheck GUI widget item and remove the fileItem from the active selections list:
                CheckBox selectionMarker = fileItem.getLayoutContainer().findViewById(R.id.fileSelectCheckBox);
                selectionMarker.setChecked(false);
                selectionMarker.setEnabled(true);
                DisplayFragments.activeSelectionsList.remove(fileItem);
                DisplayFragments.curSelectionCount--;
                return true;
            }
            else if(DisplayFragments.curSelectionCount >= DisplayFragments.maxAllowedSelections) {
                return false;
            }
            CheckBox selectionMarker = fileItem.getLayoutContainer().findViewById(R.id.fileSelectCheckBox);
            selectionMarker.setChecked(true);
            selectionMarker.setEnabled(true);
            DisplayFragments.activeSelectionsList.add(fileItem);
            DisplayFragments.curSelectionCount++;
            return true;
        }

        @Override
        public void onClick(View v) {
            if(!fileItem.isDirectory()) {
                if(performNewFileItemClick(fileItem)) {
                    String displaySelectMsg = String.format(Locale.getDefault(), "Selected FILE \"%s\".", fileItem.getBaseName());
                    DisplayUtils.displayToastMessageShort(displaySelectMsg);
                }
            }
            //DisplayUtils.displayToastMessageShort(String.format(Locale.getDefault(), "ON-CLICK -- POS @ %d && TEXT @ %s", getLayoutPosition(), this.getDisplayText()));
        }

        @Override
        public boolean onLongClick(View v) {
            //DisplayUtils.displayToastMessageShort(String.format(Locale.getDefault(), "ON-LONG-CLICK -- POS @ %d && TEXT @ %s", getLayoutPosition(), this.getDisplayText()));
            if(!fileItem.isDirectory()) {
                if(performNewFileItemClick(fileItem)) {
                    String displaySelectMsg = String.format(Locale.getDefault(), "Selected FILE \"%s\".", fileItem.getBaseName());
                    DisplayUtils.displayToastMessageShort(displaySelectMsg);
                    return true;
                }
                return false;
            }
            // Otherwise, descend recursively into the clicked directory location:
            DisplayFragments.descendIntoNextDirectory();
            String displayRecurseMsg = String.format(Locale.getDefault(), "Descending recusrively into DIR \"%s\".", fileItem.getBaseName());
            DisplayUtils.displayToastMessageShort(displayRecurseMsg);
            return true;
        }

    }

}
