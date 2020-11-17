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
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.graphics.ColorUtils;

import java.util.Locale;

public class DisplayUtils {

    private static String LOGTAG = DisplayUtils.class.getSimpleName();

    public static int getColorVariantFromTheme(Activity activityRef, int attrID) throws FileChooserException.InvalidActivityContextException {
        if(activityRef == null) {
            throw new FileChooserException.InvalidThemeResourceException();
        }
        return activityRef.getTheme().obtainStyledAttributes(new int[] { attrID }).getColor(0, attrID);
    }

    public static int getColorVariantFromTheme(int attrID) throws FileChooserException.InvalidActivityContextException {
        return getColorVariantFromTheme(FileChooserActivity.getInstance(), attrID);
    }

    public static int getColorFromResource(Activity activityRef, int colorRefID) throws FileChooserException.InvalidActivityContextException {
        if(activityRef == null) {
            throw new FileChooserException.InvalidThemeResourceException();
        }
        return activityRef.getResources().getColor(colorRefID, activityRef.getTheme());
    }

    public static int getColorFromResource(int colorRefID) throws FileChooserException.InvalidActivityContextException {
        return getColorFromResource(FileChooserActivity.getInstance(), colorRefID);
    }

    public static Drawable getDrawableFromResource(Activity activityRef, int drawableRefID) throws FileChooserException.InvalidActivityContextException {
        if(activityRef == null) {
            throw new FileChooserException.InvalidThemeResourceException();
        }
        return activityRef.getResources().getDrawable(drawableRefID, activityRef.getTheme());
    }

    public static Drawable getDrawableFromResource(int drawableRefID) throws FileChooserException.InvalidActivityContextException {
        return getDrawableFromResource(FileChooserActivity.getInstance(), drawableRefID);
    }

    public static String getStringFromResource(Activity activityRef, int strRefID) throws FileChooserException.InvalidActivityContextException {
        if(activityRef == null) {
            throw new FileChooserException.InvalidThemeResourceException();
        }
        return activityRef.getString(strRefID);
    }

    public static String getStringFromResource(int strRefID) throws FileChooserException.InvalidActivityContextException {
        return getStringFromResource(FileChooserActivity.getInstance(), strRefID);
    }

    public static String resolveStringFromAttribute(Activity activityRef, int attrID) throws FileChooserException.InvalidActivityContextException {
        if(activityRef == null) {
            throw new FileChooserException.InvalidThemeResourceException();
        }
        TypedValue typedValueAttr = new TypedValue();
        activityRef.getTheme().resolveAttribute(attrID, typedValueAttr, true);
        return activityRef.getString(typedValueAttr.resourceId);
    }

    public static String resolveStringFromAttribute(int attrID) throws FileChooserException.InvalidActivityContextException {
        return resolveStringFromAttribute(FileChooserActivity.getInstance(), attrID);
    }

    public static String resolveStringFromResId(Activity activityCtxRef, @StringRes int resId) {
        try {
            return DisplayUtils.getStringFromResource(activityCtxRef, resId);
        } catch(Exception ex) {
            try {
                return DisplayUtils.resolveStringFromAttribute(activityCtxRef, resId);
            } catch(Exception ex2) {
                return null;
            }
        }
    }

    public static String resolveStringFromResId(@StringRes int resId) {
        return resolveStringFromResId(FileChooserActivity.getInstance(), resId);
    }

    public static int resolveColorFromAttribute(Activity activityRef, int attrID) throws FileChooserException.InvalidActivityContextException {
        if(activityRef == null) {
            throw new FileChooserException.InvalidThemeResourceException();
        }
        TypedValue typedValueAttr = new TypedValue();
        activityRef.getTheme().resolveAttribute(attrID, typedValueAttr, true);
        return typedValueAttr.data;
    }

    public static int resolveColorFromAttribute(int attrID) throws FileChooserException.InvalidActivityContextException {
        return resolveColorFromAttribute(FileChooserActivity.getInstance(), attrID);
    }

    public static int resolveColorFromResId(Activity activityRef, int resID) throws FileChooserException.InvalidActivityContextException {
        if(activityRef == null) {
            throw new FileChooserException.InvalidThemeResourceException();
        }
        try {
            return getColorVariantFromTheme(activityRef, resID);
        } catch(Exception ex) {
            try {
                return getColorFromResource(activityRef, resID);
            } catch(Exception ex2) {
                try {
                    return resolveColorFromAttribute(activityRef, resID);
                }
                catch(Exception ex3) {
                    return resID;
                }
            }
        }
    }

    public static int resolveColorFromResId(int resID) throws FileChooserException.InvalidActivityContextException {
        return resolveColorFromResId(FileChooserActivity.getInstance(), resID);
    }

    public static Drawable resolveDrawableFromAttribute(Activity activityRef, int attrID) throws FileChooserException.InvalidActivityContextException {
        if(activityRef == null) {
            throw new FileChooserException.InvalidThemeResourceException();
        }
        TypedValue typedValueAttr = new TypedValue();
        activityRef.getTheme().resolveAttribute(attrID, typedValueAttr, true);
        return activityRef.getDrawable(typedValueAttr.resourceId);
    }

    public static Drawable resolveDrawableFromAttribute(int attrID) throws FileChooserException.InvalidActivityContextException {
        return resolveDrawableFromAttribute(FileChooserActivity.getInstance(), attrID);
    }

    public static Drawable resolveDrawableFromResId(Activity activityCtxRef, @DrawableRes int resId) {
        try {
            return DisplayUtils.getDrawableFromResource(activityCtxRef, resId);
        } catch(Exception ex) {
            try {
                return DisplayUtils.resolveDrawableFromAttribute(activityCtxRef, resId);
            } catch(Exception ex2) {
                return null;
            }
        }
    }

    public static Drawable resolveDrawableFromResId(@DrawableRes int resId) {
        return resolveDrawableFromResId(FileChooserActivity.getInstance(), resId);
    }

    public static int lightenColor(int color, float percentToLighten) {
        if(percentToLighten < 0.0f || percentToLighten > 1.0f) {
            return color;
        }
        return ColorUtils.blendARGB(color, Color.WHITE, percentToLighten);
    }

    public static int darkenColor(int color, float percentToDarken) {
        if(percentToDarken < 0.0f || percentToDarken > 1.0f) {
            return color;
        }
        return ColorUtils.blendARGB(color, Color.BLACK, percentToDarken);
    }

    public static boolean checkIconDimensions(Drawable iconInput, int dimsHeight, int dimsWidth) {
        if(iconInput == null) {
            return false;
        }
        return (iconInput.getIntrinsicHeight() == dimsHeight) && (iconInput.getIntrinsicWidth() == dimsWidth);
    }

    public static boolean checkIconDimensions(Drawable iconInput, int dimsHW) {
        return checkIconDimensions(iconInput, dimsHW, dimsHW);
    }

    public static <T extends Object> T firstNonNull(T firstObj, T secondObj) {
        if(firstObj != null) {
            return firstObj;
        }
        else {
            return secondObj;
        }
    }

    private static void displayToastMessage(Activity activityInst, String toastMsg, int msgDuration, CustomThemeBuilder.FileItemLayoutStylizer layoutStylizer) {
        Toast toastDisplay = Toast.makeText(
                activityInst,
                toastMsg,
                msgDuration
        );
        toastDisplay.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 25);
        toastDisplay.getView().setPadding(10, 10, 10, 10);
        int toastBackgroundColor = getColorVariantFromTheme(R.attr.__colorAccent);
        int toastTextColor = getColorVariantFromTheme(R.attr.__colorPrimaryDark);
        if(layoutStylizer != null) {
            toastBackgroundColor = layoutStylizer.getThemeColorizer().getColorAccent();
            toastTextColor = layoutStylizer.getThemeColorizer().getColorPrimaryDark();
        }
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

    private static void displayToastMessage(Activity activityInst, String toastMsg, int msgDuration) {
        displayToastMessage(activityInst, toastMsg, msgDuration, null);
    }

    public static void displayToastMessageShort(Activity activityInst, String toastMsg, CustomThemeBuilder.FileItemLayoutStylizer layoutStylizer) {
        displayToastMessage(activityInst, toastMsg, Toast.LENGTH_SHORT, layoutStylizer);
    }

    public static void displayToastMessageShort(Activity activityInst, String toastMsg) {
        displayToastMessageShort(activityInst, toastMsg, null);
    }

    public static void displayToastMessageShort(String toastMsg) {
        displayToastMessageShort(FileChooserActivity.getInstance(), toastMsg, DisplayFragments.getInstance().getFileItemLayoutStylizer());
    }

    public static void displayToastMessageLong(Activity activityInst, String toastMsg, CustomThemeBuilder.FileItemLayoutStylizer layoutStylizer) {
        displayToastMessage(activityInst, toastMsg, Toast.LENGTH_LONG, layoutStylizer);
    }

    public static void displayToastMessageLong(Activity activityInst, String toastMsg) {
        displayToastMessageLong(activityInst, toastMsg, null);
    }

    public static void displayToastMessageLong(String toastMsg) {
        displayToastMessageLong(FileChooserActivity.getInstance(), toastMsg, DisplayFragments.getInstance().getFileItemLayoutStylizer());
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
    private static int progressBarTintColor = Color.LTGRAY;
    private static boolean useProgressBarTintColor = false;
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

    public static boolean SetProgressBarMarkerTintColor(Activity activityInst, @ColorRes int colorTintResId) {
        progressBarTintColor = resolveColorFromResId(activityInst, colorTintResId);
        useProgressBarTintColor = true;
        return true;
    }

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
        final String statusBarMsg = String.format(Locale.getDefault(), "%s\n% 3d / % 3d (% .2g %%)",
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
                if(useProgressBarTintColor) {
                    statusBarMarkerImage.setTint(Color.WHITE);
                    statusBarMarkerImage.setTint(progressBarTintColor);
                }
                ((ImageView) toastProgressView.findViewById(R.id.progressBarImageMarker)).setImageDrawable(statusBarMarkerImage);
                ((TextView) toastProgressView.findViewById(R.id.progressBarText)).setTextColor(lightenColor(progressBarTintColor, 0.75f));
                ((TextView) toastProgressView.findViewById(R.id.progressBarText)).setText(statusBarMsg);
                GradientDrawable bgGradient = GradientDrawableBuilder.GetStockGradientFromBaseColor(progressBarTintColor);
                toastProgressView.setBackgroundDrawable(bgGradient);
                toastProgressView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        EnableProgressBarDisplay(false); // dismiss the toast when it is touched
                        return false;
                    }
                });
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
            if(progressBarToast != null) {
                progressBarToast.cancel();
            }
            progressBarDisplayHandler.removeCallbacks(progressBarDisplayRunnable);
        }
    }

    public static void ResetProgressBarDisplay(boolean enableRedisplay) {
        progressBarTintColor = Color.LTGRAY;
        useProgressBarTintColor = false;
        EnableProgressBarDisplay(false);
    }

    public enum GradientMethodSpec {
        GRADIENT_METHOD_SWEEP,
        GRADIENT_METHOD_LINEAR,
        GRADIENT_METHOD_RADIAL,
        GRADIENT_METHOD_RECTANGLE,
        GRADIENT_METHOD_RING_LIKE
    };

    private static int getGradientTypeFromEnum(int gradTypeConst) {
        if(gradTypeConst == GradientMethodSpec.GRADIENT_METHOD_LINEAR.ordinal())
            return GradientDrawable.LINEAR_GRADIENT;
        else if(gradTypeConst == GradientMethodSpec.GRADIENT_METHOD_RADIAL.ordinal())
            return GradientDrawable.RADIAL_GRADIENT;
        else
            return GradientDrawable.SWEEP_GRADIENT;
    }

    private static int getGradientShapeFromEnum(int gradientShapeConst) {
        if(gradientShapeConst == GradientMethodSpec.GRADIENT_METHOD_RECTANGLE.ordinal())
            return GradientDrawable.RECTANGLE;
        else if(gradientShapeConst == GradientMethodSpec.GRADIENT_METHOD_RADIAL.ordinal())
            return GradientDrawable.OVAL;
        else if(gradientShapeConst == GradientMethodSpec.GRADIENT_METHOD_RING_LIKE.ordinal())
            return GradientDrawable.RING;
        else if(gradientShapeConst == GradientMethodSpec.GRADIENT_METHOD_SWEEP.ordinal())
            return GradientDrawable.RING;
        else
            return GradientDrawable.LINE;
    }

    public enum GradientTypeSpec {
        GRADIENT_FILL_TYPE_BL_TR,
        GRADIENT_FILL_TYPE_BOTTOM_TOP,
        GRADIENT_FILL_TYPE_BR_TL,
        GRADIENT_FILL_TYPE_LEFT_RIGHT,
        GRADIENT_FILL_TYPE_RIGHT_LEFT,
        GRADIENT_FILL_TYPE_TL_BR,
        GRADIENT_FILL_TYPE_TOP_BOTTOM,
        GRADIENT_FILL_TYPE_TR_BL,
    };

    private static GradientDrawable.Orientation getOrientationFromEnum(int orientConstant) {
        if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_BL_TR.ordinal()) {
            return GradientDrawable.Orientation.BL_TR;
        }
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_BOTTOM_TOP.ordinal()) {
            return GradientDrawable.Orientation.BOTTOM_TOP;
        }
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_BR_TL.ordinal()) {
            return GradientDrawable.Orientation.BR_TL;
        }
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_LEFT_RIGHT.ordinal()) {
            return GradientDrawable.Orientation.LEFT_RIGHT;
        }
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_RIGHT_LEFT.ordinal()) {
            return GradientDrawable.Orientation.RIGHT_LEFT;
        }
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_TL_BR.ordinal()) {
            return GradientDrawable.Orientation.TL_BR;
        }
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_TOP_BOTTOM.ordinal()) {
            return GradientDrawable.Orientation.TOP_BOTTOM;
         }
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_TR_BL.ordinal()){
            return GradientDrawable.Orientation.TR_BL;
        }
        else {
            return GradientDrawable.Orientation.BL_TR;
        }
    }

    public enum BorderStyleSpec {
        BORDER_STYLE_SOLID,
        BORDER_STYLE_DASHED,
        BORDER_STYLE_DASHED_LONG,
        BORDER_STYLE_DASHED_SHORT,
        BORDER_STYLE_NONE,
    };

    private static GradientDrawable generateGradientByType(GradientMethodSpec gmethodSpec,
                                                           GradientTypeSpec gfillTypeSpec,
                                                           BorderStyleSpec borderStyleSpec,
                                                           int borderColor,
                                                           int borderSize,
                                                           int[] colorList) {
        GradientDrawable gradientDrawObj = new GradientDrawable(
                    getOrientationFromEnum(gfillTypeSpec.ordinal()),
                    colorList
            );
        gradientDrawObj.setGradientCenter(0.5f, 0.5f);
        gradientDrawObj.setGradientType(getGradientTypeFromEnum(gmethodSpec.ordinal()));
        gradientDrawObj.setCornerRadius(3 * borderSize);
        gradientDrawObj.setShape(GradientDrawable.RECTANGLE);
        if(borderStyleSpec == BorderStyleSpec.BORDER_STYLE_SOLID) {
            gradientDrawObj.setStroke(borderSize, borderColor);
        }
        else if(borderStyleSpec == BorderStyleSpec.BORDER_STYLE_DASHED) {
            gradientDrawObj.setStroke(borderSize, borderColor, 2 * borderSize, 2 * borderSize);
        }
        else if(borderStyleSpec == BorderStyleSpec.BORDER_STYLE_DASHED_LONG) {
            gradientDrawObj.setStroke(borderSize, borderColor, 5 * borderSize, 2 * borderSize);
        }
        else if(borderStyleSpec == BorderStyleSpec.BORDER_STYLE_DASHED_SHORT) {
            gradientDrawObj.setStroke(borderSize, borderColor, borderSize, borderSize);
        }
        else if(borderStyleSpec == BorderStyleSpec.BORDER_STYLE_NONE) {
            gradientDrawObj.setStroke(borderSize, borderColor, 0, 0);
        }
        return gradientDrawObj;
    }

    public static int[] generateGradientColorsListFromBaseColor(int baseColor, int numColorStates) {
        if(numColorStates < 1) {
            return null;
        }
        int[] colorStates = new int[numColorStates];
        int beforeCount = numColorStates / 2, afterCount = numColorStates - 1 - beforeCount;
        for(int cidx = 0; cidx < beforeCount; cidx++) {
            colorStates[cidx] = ColorUtils.blendARGB(baseColor, Color.WHITE, 0.25f + (0.50f / beforeCount) * cidx);
        }
        colorStates[beforeCount] = baseColor;
        for(int cidx = beforeCount + 1; cidx < numColorStates; cidx++) {
            colorStates[cidx] = ColorUtils.blendARGB(baseColor, Color.BLACK, 0.25f + (0.50f / afterCount) * cidx);
        }
        return colorStates;
    }

    public static int[] generateGradientColorsListFromBaseColor(int baseColor, int blendColor, int numColorStates) {
        if(numColorStates < 1) {
            return null;
        }
        int[] colorStates = new int[numColorStates];
        for(int cidx = 0; cidx < numColorStates; cidx++) {
            colorStates[cidx] = ColorUtils.blendARGB(baseColor, blendColor, (0.80f / numColorStates) * cidx);
        }
        return colorStates;
    }

    public static class GradientDrawableBuilder {

        private int[] colorsList;
        private GradientMethodSpec gradientType;
        private int borderColor, borderSize;
        private BorderStyleSpec borderStyle;
        private GradientTypeSpec gradientFillStyle;

        public GradientDrawableBuilder() {
            colorsList = new int[] {};
            gradientType = GradientMethodSpec.GRADIENT_METHOD_LINEAR;
            borderColor = 0;
            borderSize = 5;
            borderStyle = BorderStyleSpec.BORDER_STYLE_SOLID;
            gradientFillStyle = GradientTypeSpec.GRADIENT_FILL_TYPE_BL_TR;
        }

        public GradientDrawableBuilder setColorsArray(int[] colorsArray) {
            colorsList = colorsArray;
            return this;
        }

        public GradientDrawableBuilder setColorsArray(int baseColor, int numStates) {
            colorsList = generateGradientColorsListFromBaseColor(baseColor, numStates);
            return this;
        }

        public GradientDrawableBuilder setColorsArray(int baseColor) {
            return setColorsArray(baseColor, 3);
        }

        public GradientDrawableBuilder setGradientType(GradientMethodSpec gradType) {
            gradientType = gradType;
            return this;
        }

        public GradientDrawableBuilder setBorderColor(int bdrColor) {
            borderColor = bdrColor;
            return this;
        }

        public GradientDrawableBuilder setBorderSize(int bdrSize) {
            borderSize = bdrSize;
            return this;
        }

        public GradientDrawableBuilder setBorderStyle(BorderStyleSpec bdrStyle) {
            borderStyle = bdrStyle;
            return this;
        }

        public GradientDrawableBuilder setFillStyle(GradientTypeSpec gradFillStyle) {
            gradientFillStyle = gradFillStyle;
            return this;
        }

        public GradientDrawableBuilder setAngle(float gradientAngle) {
            if(gradientAngle == 0.0f) {
                return setFillStyle(GradientTypeSpec.GRADIENT_FILL_TYPE_LEFT_RIGHT);
            }
            else if(gradientAngle == 45.0f) {
                return setFillStyle(GradientTypeSpec.GRADIENT_FILL_TYPE_TL_BR);
            }
            else if(gradientAngle == 90.0f) {
                return setFillStyle(GradientTypeSpec.GRADIENT_FILL_TYPE_TOP_BOTTOM);
            }
            else if(gradientAngle ==  135.f) {
                return setFillStyle(GradientTypeSpec.GRADIENT_FILL_TYPE_TR_BL);
            }
            else if(gradientAngle == -45.f) {
                return setFillStyle(GradientTypeSpec.GRADIENT_FILL_TYPE_RIGHT_LEFT);
            }
            else if(gradientAngle ==  -90.f) {
                return setFillStyle(GradientTypeSpec.GRADIENT_FILL_TYPE_BR_TL);
            }
            else if(gradientAngle == 135.f) {
                return setFillStyle(GradientTypeSpec.GRADIENT_FILL_TYPE_BOTTOM_TOP);
            }
            return this;
        }

        public GradientDrawable make() {
            return generateGradientByType(gradientType, gradientFillStyle, borderStyle, borderColor, borderSize, colorsList);
        }

        public static GradientDrawable GetStockGradientFromBaseColor(int baseColor) {
            return new GradientDrawableBuilder()
                    .setGradientType(GradientMethodSpec.GRADIENT_METHOD_LINEAR)
                    .setBorderColor(darkenColor(baseColor, 0.5f))
                    .setBorderSize(5)
                    .setBorderStyle(BorderStyleSpec.BORDER_STYLE_SOLID)
                    .setFillStyle(GradientTypeSpec.GRADIENT_FILL_TYPE_LEFT_RIGHT)
                    .setColorsArray(baseColor, 3)
                    .make();
        }

        public static GradientDrawable GetStockGradientFromBaseColor(int baseColor, int blendColor) {
            return new GradientDrawableBuilder()
                    .setGradientType(GradientMethodSpec.GRADIENT_METHOD_LINEAR)
                    .setBorderColor(darkenColor(baseColor, 0.5f))
                    .setBorderSize(0)
                    .setBorderStyle(BorderStyleSpec.BORDER_STYLE_SOLID)
                    .setFillStyle(GradientTypeSpec.GRADIENT_FILL_TYPE_RIGHT_LEFT)
                    .setColorsArray(generateGradientColorsListFromBaseColor(baseColor, blendColor, 3))
                    .make();
        }

    }

}
