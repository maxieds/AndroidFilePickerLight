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

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public class FileFilter {

    private static String LOGTAG = FileFilter.class.getSimpleName();

    static final boolean INCLUDE_FILES_IN_FILTER_PATTERN = FileChooserBuilder.INCLUDE_FILES_IN_FILTER_PATTERN;
    static final boolean EXCLUDE_FILES_IN_FILTER_PATTERN = FileChooserBuilder.EXCLUDE_FILES_IN_FILTER_PATTERN;

    public static abstract class FileFilterBase implements FilenameFilter {

        protected boolean includeExcludeMatches = INCLUDE_FILES_IN_FILTER_PATTERN;

        public void setIncludeExcludeMatchesOption(boolean includeExcludeParam) {
            includeExcludeMatches = includeExcludeParam;
        }

        @Override
        public boolean accept(File parentDir, String fileBaseName) {
            return fileMatchesFilter(parentDir.getAbsolutePath() + FileUtils.FILE_PATH_SEPARATOR + fileBaseName);
        }

        abstract public boolean fileMatchesFilter(String fileAbsName);

    }

    public static class FileFilterByMimeType extends FileFilterBase {
        private List<String> mimeTypesList;
        public FileFilterByMimeType(List<String> mimeTypesList, boolean inclExcl) {
            this.mimeTypesList = mimeTypesList;
            setIncludeExcludeMatchesOption(inclExcl);
        }
        public boolean fileMatchesFilter(String fileAbsName) {
            for(int mtIdx = 0; mtIdx < mimeTypesList.size(); mtIdx++) {
                if(FileUtils.getFileMimeType(fileAbsName).equals(mimeTypesList.get(mtIdx))) {
                    return includeExcludeMatches == INCLUDE_FILES_IN_FILTER_PATTERN;
                }
            }
            return includeExcludeMatches == EXCLUDE_FILES_IN_FILTER_PATTERN;
        }
    }

    public static class FileFilterByRegex extends FileFilterBase {
        private Pattern patternSpec;
        public FileFilterByRegex(String regexPatternSpec, boolean inclExcl) {
            patternSpec = Pattern.compile(regexPatternSpec);
            setIncludeExcludeMatchesOption(inclExcl);
        }
        public boolean fileMatchesFilter(String fileAbsName) {
            if(patternSpec.matcher(fileAbsName).matches()) {
                return includeExcludeMatches == INCLUDE_FILES_IN_FILTER_PATTERN;
            }
            return includeExcludeMatches == EXCLUDE_FILES_IN_FILTER_PATTERN;
        }
    }

    public static class FileFilterByDefaultTypesList extends FileFilterBase {
        private List<FileChooserBuilder.DefaultFileTypes> defaultTypesList;
        public FileFilterByDefaultTypesList(List<FileChooserBuilder.DefaultFileTypes> defaultTypesList, boolean inclExcl) {
            this.defaultTypesList = defaultTypesList;
            setIncludeExcludeMatchesOption(inclExcl);
        }
        public boolean fileMatchesFilter(String fileAbsName) {
            for(int mtIdx = 0; mtIdx < defaultTypesList.size(); mtIdx++) {
                if(true) {
                    throw new FileChooserException.NotImplementedException();
                }
            }
            return false;
        }
    }

    public static class FileItemsSortFunc implements Comparator<File> {
        public File[] sortFileItemsList(File[] folderContentsList) {
            Arrays.sort(folderContentsList, this);
            return folderContentsList;
        }
        @Override
        public int compare(File f1, File f2) {
            // default is standard lexicographical ordering (override the compare functor base classes for customized sorting):
            return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
        }
    }

}
