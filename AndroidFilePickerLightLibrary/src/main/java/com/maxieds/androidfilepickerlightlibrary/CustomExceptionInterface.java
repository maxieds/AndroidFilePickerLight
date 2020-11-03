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

import android.content.Intent;

import java.io.File;
import java.net.URI;
import java.util.List;

interface CustomExceptionInterface {

    /* constructors -- building a new descriptive exception from the usual causal data: */
    public CustomExceptionInterface newExceptionInstance(Exception javaBaseExcpt);
    public CustomExceptionInterface newExceptionInstance(String errorMsg);
    public CustomExceptionInterface newExceptionInstance(String errorMsg, Exception javaBaseExcpt);

    /* Standard-ish Exception class handling and methods: */
    public String getExceptionMessage();
    public String[] getStackTraceAsStringArray();
    public void printStackTrace();
    public String toString();

    /* For uses of communicating other information by exception: */
    public boolean isError();
    public int getErrorCode();
    public Intent getAsIntent();

    /* Custom error messages and printing methods: */
    public String getExceptionName();
    public String getExceptionBaseDesc();
    public String getErrorMessage();
    public String getErrorMessageFull();
    public String getInvokingSourceFile();
    public int getInvokingLineNumber();
    public String prettyPrintException(boolean verboseStackTrace);

    /* Obtaining file selection data passed by the exceptional instance: */
    public boolean hasDataItems();
    public int dataTypeItemsCount();
    public <DataTypeT extends Object> DataTypeT getTypedDataSingle();
    public <DataTypeT extends Object> List<DataTypeT> getTypedDataAsList();
    public List<String> packageDataItemsFromIntent(Intent fileItemsIntent);

    /* Custom formatting and packaging/preparation of the returned data expected by the
     * client application (basically a rough serialization to byte buffer type object spec):
     */
    public interface ExceptionDataFieldFormatter {

        public <DataTypeT extends Object> List<DataTypeT> packageDataItemsFromFileType(List<File> fileItems);
        public <DataTypeT extends Object> List<DataTypeT> packageDataItemsFromStringType(List<String> fileItems);
        public <DataTypeT extends Object> List<DataTypeT> packageDataItemsFromURIType(List<URI> fileItems);
        public <DataTypeT extends Object> String toString(List<DataTypeT> lstView);

        public static <DataTypeT extends Object> byte[] toSerializedDataBuffer(List<DataTypeT> lstView) {
            return null;
        }

        public static <DataTypeT extends Object> List<DataTypeT> recoverDataItemsList(byte[] serializedBufferData) {
            return null;
        }

    }

    public <DataTypeT extends Object> void setDataItemsFieldFormatterInterface(ExceptionDataFieldFormatter dataFormatter);

}
