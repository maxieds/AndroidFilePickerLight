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
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class FilePickerChooserActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle lastSettingsBundle) {
        super.onCreate(lastSettingsBundle);
    }

    @Override
    public void onNewIntent(Intent broadcastIntent) {
        super.onNewIntent(broadcastIntent);
    }

    // Check permissions and call the permissions handler if necessary ...
    // Create layout ... (including setting defaults, set default theme, etc. ) ...
    // When launch by intent, need to obtain the original Builder cfg object ...
    // Need fragments for loading files in the UI nav (see the forked library code) ...
    // Need to updated Android 11 interface for accessing files and their storage component data ...

}
