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

import java.util.List;
import java.util.regex.Pattern;

public class FileFilter {

    private static String LOGTAG = FileFilterInterface.class.getSimpleName();

    public interface FileFilterInterface {

        static final boolean INCLUDE_FILES_IN_FILTER_PATTERN = FilePickerBuilder.INCLUDE_FILES_IN_FILTER_PATTERN;
        static final boolean EXCLUDE_FILES_IN_FILTER_PATTERN = FilePickerBuilder.EXCLUDE_FILES_IN_FILTER_PATTERN;

        void    setIncludeExcludeMatchesOption(boolean includeExcludeParam);
        boolean includeFileInSearchResults(FileTypes.FileType fileItem);
        boolean fileMatchesFilter(FileTypes.FileType fileItem);

    }

    public static abstract class FileFilterBase implements FileFilterInterface {

        private boolean includeExcludeMatches = INCLUDE_FILES_IN_FILTER_PATTERN;

        public void setIncludeExcludeMatchesOption(boolean includeExcludeParam) {
            includeExcludeMatches = includeExcludeParam;
        }

        public boolean includeFileInSearchResults(FileTypes.FileType fileItem) {
            boolean filterMatch = fileMatchesFilter(fileItem);
            if((filterMatch && includeExcludeMatches == INCLUDE_FILES_IN_FILTER_PATTERN) ||
                    (!filterMatch && includeExcludeMatches == EXCLUDE_FILES_IN_FILTER_PATTERN)) {
                return true;
            }
            return false;
        }

        abstract public boolean fileMatchesFilter(FileTypes.FileType fileItem);

    }

    public static class FileFilterByMimeType extends FileFilterBase {
        private List<String> mimeTypesList;
        public FileFilterByMimeType(List<String> mimeTypesList, boolean inclExcl) {
            this.mimeTypesList = mimeTypesList;
            setIncludeExcludeMatchesOption(inclExcl);
        }
        public boolean fileMatchesFilter(FileTypes.FileType fileItem) {
            for(int mtIdx = 0; mtIdx < mimeTypesList.size(); mtIdx++) {
                if(FileUtils.getFileMimeType(fileItem.getAbsolutePath()).equals(mimeTypesList.get(mtIdx))) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class FileFilterByRegex extends FileFilterBase {
        private Pattern patternSpec;
        public FileFilterByRegex(String regexPatternSpec, boolean inclExcl) {
            patternSpec = Pattern.compile(regexPatternSpec);
            setIncludeExcludeMatchesOption(inclExcl);
        }
        public boolean fileMatchesFilter(FileTypes.FileType fileItem) {
            if(patternSpec.matcher(fileItem.getAbsolutePath()).matches()) {
                return true;
            }
            return false;
        }
    }

    public static class FileFilterByDefaultTypesList extends FileFilterBase {
        private List<FileTypes.DefaultFileTypes> defaultTypesList;
        public FileFilterByDefaultTypesList(List<FileTypes.DefaultFileTypes> defaultTypesList, boolean inclExcl) {
            this.defaultTypesList = defaultTypesList;
            setIncludeExcludeMatchesOption(inclExcl);
        }
        public boolean fileMatchesFilter(FileTypes.FileType fileItem) {
            for(int mtIdx = 0; mtIdx < defaultTypesList.size(); mtIdx++) {
                if(true) {
                    throw new FilePickerException.NotImplementedException();
                }
            }
            return false;
        }
    }

}
