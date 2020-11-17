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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileChooserException {

    private static String LOGTAG = FileChooserException.class.getSimpleName();

    public static Object DEFAULT_DATA_ITEMS_TYPE = String.class;

    private static int nextUniqueErrorCode = 1;
    private static int claimNextUniqueErrorCode() {
        nextUniqueErrorCode <<= 1;
        return nextUniqueErrorCode;
    }

    /* Custom formatting and packaging/preparation of the returned data expected by the
     * client application (basically a rough serialization to byte buffer type object spec):
     *
     * Leaving this in as a spec for later. Maybe it will become more useful as the library
     * grows and Android 11+ forces users to use newer functionality to access files on the
     * local disk.
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

    public static class AndroidFilePickerLightException extends RuntimeException {

        private boolean isError;
        private int errorCode;
        protected static String errorDesc = "";
        private String errorMsg;
        private Exception invokingJavaExcpt;
        private Object defaultDataItemsFmtType;
        private ExceptionDataFieldFormatter dataItemsFormatter;

        /* Constructors -- building a new descriptive exception from the usual causal data: */
        public AndroidFilePickerLightException() {
            isError = false;
            errorCode = 0;
            errorMsg = "";
            invokingJavaExcpt = null;
            defaultDataItemsFmtType = DEFAULT_DATA_ITEMS_TYPE;
            dataItemsFormatter = null;
        }

        protected static AndroidFilePickerLightException getNewInstance() {
            return new AndroidFilePickerLightException();
        }

        public AndroidFilePickerLightException newExceptionInstance(Exception javaBaseExcpt) {
            AndroidFilePickerLightException baseExcptInst = getNewInstance();
            baseExcptInst.invokingJavaExcpt = javaBaseExcpt;
            return baseExcptInst;
        }

        public AndroidFilePickerLightException newExceptionInstance(String errorMsg) {
            AndroidFilePickerLightException baseExcptInst = getNewInstance();
            baseExcptInst.errorMsg = errorMsg;
            return baseExcptInst;
        }

        public AndroidFilePickerLightException newExceptionInstance(String errorMsg, Exception javaBaseExcpt) {
            AndroidFilePickerLightException baseExcptInst = getNewInstance();
            baseExcptInst.invokingJavaExcpt = javaBaseExcpt;
            baseExcptInst.errorMsg = errorMsg;
            return baseExcptInst;
        }

        protected void configureExceptionParams(int errorCode, String baseExcptDesc, boolean defaultIsError,
                                                Object defaultDataTypeT, ExceptionDataFieldFormatter dataFmtObjType) {
            this.errorCode = errorCode;
            this.errorMsg = baseExcptDesc;
            this.isError = defaultIsError;
        }

        /* Standard-ish Exception class handling and methods: */
        @Override
        public String getMessage() {
            return getErrorMessage();
        }
        public String toString() { return getMessage(); }

        /* For uses of communicating other information by exception: */
        public boolean isError() { return isError; }
        public int getErrorCode() { return errorCode; }
        public Intent packageAsIntent() { return null; }
        public String getCauseAsString() { return this.getClass().getName(); }
        public String getErrorMessage() { return errorMsg; }

        private static String resolveFilePathFromObjectType(Object dataItem) {
            if(dataItem instanceof String) {
                return (String) dataItem;
            }
            else if(dataItem instanceof File) {
                return ((File) dataItem).getAbsolutePath();
            }
            else if(dataItem instanceof URI) {
                URI dataItemAsURI = (URI) dataItem;
                return dataItemAsURI.getPath();
            }
            throw new UnsupportedTypeException();
        }

        public List<String> packageDataItemsFromIntent(Intent fileItemsIntent) {
            ArrayList<String> selectedFilePathsList = fileItemsIntent.getStringArrayListExtra(FileChooserBuilder.FILE_PICKER_INTENT_DATA_PAYLOAD_KEY);
            return selectedFilePathsList;
        }

        public <DataTypeT extends Object> void setDataItemsFieldFormatterInterface(ExceptionDataFieldFormatter dataFormatterObj) {
            dataItemsFormatter = dataFormatterObj;
        }

    }

    public static class GenericRuntimeErrorException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new GenericRuntimeErrorException();
        }

        public GenericRuntimeErrorException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                                     "Generic exception happened while running the file picker",
                                      true, DEFAULT_DATA_ITEMS_TYPE, null);
        }

        public GenericRuntimeErrorException(String causalWhyMsg) {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    String.format(Locale.getDefault(), "Generic exception: %s", causalWhyMsg),
                    true, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class FileIOException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new FileIOException();
        }

        public FileIOException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "File I/O error (file/dir path not found, or other issue)",
                    true, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class InvalidIndexException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new InvalidIndexException();
        }

        public InvalidIndexException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "Index out of expected range",
                    true, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class PermissionsErrorException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new PermissionsErrorException();
        }

        public PermissionsErrorException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "Android storage or file system permissions error",
                    true, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class AbortedByTimeoutException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new AbortedByTimeoutException();
        }

        public AbortedByTimeoutException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "The file picker terminated from being idle for too long (no data to return)",
                    false, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class AbortedByUserActionException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new AbortedByUserActionException();
        }

        public AbortedByUserActionException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "The user canceled the file picker without selecting any path(s) (no data to return)",
                    false, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class CommunicateSelectionDataException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new CommunicateSelectionDataException();
        }

        public CommunicateSelectionDataException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "No selections by the user",
                    false, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class CommunicateNoDataException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new CommunicateNoDataException();
        }

        public CommunicateNoDataException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "File picker closed normally, but no data selected to return",
                    false, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class InvalidInitialPathException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new InvalidInitialPathException();
        }

        public InvalidInitialPathException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "The specified initial directory path for the file picker is flawed",
                    true, DEFAULT_DATA_ITEMS_TYPE, null);
        }

        public InvalidInitialPathException(String causalWhyMsg) {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    String.format(Locale.getDefault(), "Invalid path: %s", causalWhyMsg),
                    true, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class InvalidThemeResourceException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new InvalidThemeResourceException();
        }

        public InvalidThemeResourceException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "Problem resolving a passed resource for the display/UI theme",
                    true, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class InvalidActivityContextException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new InvalidActivityContextException();
        }

        public InvalidActivityContextException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "The library does not have a handle on a valio activity or context reference object",
                    true, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class UnsupportedTypeException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new UnsupportedTypeException();
        }

        public UnsupportedTypeException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "Attempted to convert or return selected data items in an unsupported format",
                    true, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static class NotImplementedException extends AndroidFilePickerLightException {

        private static int UNIQUE_ERROR_CODE = claimNextUniqueErrorCode();

        protected static AndroidFilePickerLightException getNewInstance() {
            return new NotImplementedException();
        }

        public NotImplementedException() {
            configureExceptionParams(UNIQUE_ERROR_CODE,
                    "The invoked feature is reserved for future use",
                    true, DEFAULT_DATA_ITEMS_TYPE, null);
        }

    }

    public static AndroidFilePickerLightException getExceptionForExitCause(String classNameCausalId, String emsg) {
        try {
            Class excptSubclass = Class.forName(classNameCausalId);
            if(emsg != null) {
                return ((AndroidFilePickerLightException) excptSubclass.newInstance()).newExceptionInstance(emsg);
            }
            else {
                return (AndroidFilePickerLightException) excptSubclass.newInstance();
            }
        } catch(Exception excpt) {
            excpt.printStackTrace();
            return null;
        }
    }

}
