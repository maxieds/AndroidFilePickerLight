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
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

public class DisplayAdapters {

    private static String LOGTAG = DisplayAdapters.class.getSimpleName();

    public static class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {

        public static FileFilter.FileFilterInterface localFilesListFilter = null;
        public static FileTypes.FileItemsListSortFunc localFilesListSortFunc = null;

        private List<String> fileListData;
        public FileListAdapter(List<String> data){
            this.fileListData = data;
        }

        @Override
        public FileListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_file_entry_item, parent, false);
            return new ViewHolder(rowItem);
        }

        @Override
        public void onBindViewHolder(FileListAdapter.ViewHolder holder, int position) {
            holder.textView.setText(fileListData.get(position));
        }

        @Override
        public int getItemCount() {
            return fileListData.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView textView;
            public ViewHolder(View view) {
                super(view);
                view.setOnClickListener(this);
                this.textView = view.findViewById(R.id.fileEntryBaseName);
            }

            @Override
            public void onClick(View view) {
                DisplayUtils.displayToastMessageShort(String.format(Locale.getDefault(), "POS @ %d && TEXT @ %s", getLayoutPosition(), this.textView.getText()));
            }
        }

        public void displayNextDirectoryFilesList(List<FileTypes.FileType> workingDirContentsList) {
            List<FileTypes.FileType> filteredFileContents = FilePickerBuilder.filterAndSortFileItemsList(workingDirContentsList, localFilesListFilter, localFilesListSortFunc);
            DisplayFragments.FolderNavigationFragment.dirsOneBackText.setText("----");
            DisplayFragments.FolderNavigationFragment.dirsTwoBackText.setText("----");
            DisplayFragments.FileListItemFragment.rvAdapter.displayNextDirectoryFilesList(filteredFileContents);
        }

    }




}
