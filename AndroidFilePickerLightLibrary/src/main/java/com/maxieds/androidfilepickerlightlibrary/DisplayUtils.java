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

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.Layout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class DisplayUtils {

    private static String LOGTAG = DisplayUtils.class.getSimpleName();

    private static void displayToastMessage(Activity activityInst, String toastMsg, int msgDuration) {
        Toast toastDisplay = Toast.makeText(
                activityInst,
                toastMsg,
                msgDuration
        );
        toastDisplay.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 25);
        toastDisplay.getView().setPadding(10, 10, 10, 10);
        int toastBackgroundColor = FileChooserActivity.getColorVariantFromTheme(R.attr.colorAccent);
        int toastTextColor = FileChooserActivity.getColorVariantFromTheme(R.attr.colorPrimaryDark);
        toastDisplay.getView().getBackground().setColorFilter(toastBackgroundColor, PorterDuff.Mode.SRC_IN);
        TextView toastTextMsg = toastDisplay.getView().findViewById(android.R.id.message);
        if(toastTextMsg != null) {
            toastTextMsg.setTextColor(toastTextColor);
            toastTextMsg.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            toastTextMsg.setTypeface(toastTextMsg.getTypeface(), Typeface.BOLD_ITALIC);
        }
        toastDisplay.getView().setAlpha(0.75f);
        toastDisplay.show();
    }

    public static void displayToastMessageShort(Activity activityInst, String toastMsg) {
        displayToastMessage(activityInst, toastMsg, Toast.LENGTH_SHORT);
    }

    public static void displayToastMessageShort(String toastMsg) {
        displayToastMessageShort(FileChooserActivity.getInstance(), toastMsg);
    }

    public static void displayToastMessageLong(Activity activityInst, String toastMsg) {
        displayToastMessage(activityInst, toastMsg, Toast.LENGTH_LONG);
    }

    public static void displayToastMessageLong(String toastMsg) {
        displayToastMessageLong(FileChooserActivity.getInstance(), toastMsg);
    }

    private static int[] PROGRESS_BAR_VISUAL_MARKERS = new int[] {
            R.drawable.progressbar_0,
            R.drawable.progressbar_1,
            R.drawable.progressbar_2,
            R.drawable.progressbar_3,
            R.drawable.progressbar_4,
            R.drawable.progressbar_5,
            R.drawable.progressbar_6,
            R.drawable.progressbar_7,
            R.drawable.progressbar_8,
    };

    private static final int STATUS_TOAST_DISPLAY_TIME = Toast.LENGTH_SHORT;
    private static final int STATUS_TOAST_DISPLAY_REFRESH_TIME = 250;
    private static boolean toastsDismissed = true;
    private static int progressBarPos, progressBarTotal;
    private static String progressBarSliderName;
    private static Toast progressBarToast = null;
    private static Activity activityInst = FileChooserActivity.getInstance();
    private static Handler progressBarDisplayHandler = new Handler();
    private static Runnable progressBarDisplayRunnable = new Runnable() {
        public void run() {
            if (!toastsDismissed && progressBarToast != null) {
                DisplayProgressBar(activityInst, progressBarSliderName, progressBarPos, progressBarTotal);
            }
        }
    };

    public static void DisplayProgressBar(Activity activityInstInput, String thingsName, int curPos, int totalPos) {
        if(!thingsName.equals(progressBarSliderName) || curPos != progressBarPos || totalPos != progressBarTotal) {
            if(!toastsDismissed) {
                progressBarDisplayHandler.removeCallbacks(progressBarDisplayRunnable);
            }
        }
        activityInst = activityInstInput;
        progressBarSliderName = thingsName;
        progressBarPos = curPos;
        progressBarTotal = totalPos;
        final int statusBarMarkerIdx = Math.min((int) ((curPos - 1) * PROGRESS_BAR_VISUAL_MARKERS.length / totalPos),
                                       PROGRESS_BAR_VISUAL_MARKERS.length - 1);
        final String statusBarMsg = String.format(Locale.getDefault(), "%s % 3d / % 3d (% .2g %%)",
                thingsName, curPos, totalPos, (float) curPos / totalPos * 100.0);
        final Activity mainAppActivity = activityInst;
        mainAppActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBarToast = Toast.makeText(mainAppActivity, statusBarMsg, STATUS_TOAST_DISPLAY_TIME);
                progressBarToast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 65);
                LayoutInflater layoutInflater = mainAppActivity.getLayoutInflater();
                View toastProgressView = layoutInflater.inflate(R.layout.progress_bar_layout, null);
                Drawable statusBarMarkerImage = mainAppActivity.getResources().getDrawable(PROGRESS_BAR_VISUAL_MARKERS[statusBarMarkerIdx]);
                ((ImageView) toastProgressView.findViewById(R.id.progressBarImageMarker)).setImageDrawable(statusBarMarkerImage);
                ((TextView) toastProgressView.findViewById(R.id.progressBarText)).setText(statusBarMsg);
                progressBarToast.setView(toastProgressView);
                if(!toastsDismissed) {
                    progressBarDisplayHandler.postDelayed(progressBarDisplayRunnable, STATUS_TOAST_DISPLAY_REFRESH_TIME);
                }
                progressBarToast.show();
            }
        });
    }

    public static void EnableProgressBarDisplay(boolean enableRedisplay) {
        toastsDismissed = !enableRedisplay;
        if(toastsDismissed) {
            progressBarDisplayHandler.removeCallbacks(progressBarDisplayRunnable);
        }
    }

}
