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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;

public class GradientDrawableFactory {

    private static String LOGTAG = GradientDrawableFactory.class.getSimpleName();

    private static Activity defaultActivityContextRef = FileChooserActivity.getInstance();
    public static void setDefaultActivityContext(Activity activityContextRef) {
        defaultActivityContextRef = activityContextRef;
    }

    public static int getColorFromResource(int colorRefID) throws FileChooserException.InvalidActivityContextException {
        if(defaultActivityContextRef != null) {
            return defaultActivityContextRef.getResources().getColor(colorRefID, defaultActivityContextRef.getTheme());
        }
        throw new FileChooserException.InvalidActivityContextException();
    }

    public static Drawable getDrawableFromResource(int drawableRefID) throws FileChooserException.InvalidActivityContextException {
        if(defaultActivityContextRef != null) {
            return defaultActivityContextRef.getResources().getDrawable(drawableRefID, defaultActivityContextRef.getTheme());
        }
        throw new FileChooserException.InvalidActivityContextException();
    }

    public static String getStringFromResource(int strRefID) throws FileChooserException.InvalidActivityContextException {
        if(defaultActivityContextRef != null) {
            return defaultActivityContextRef.getString(strRefID);
        }
        throw new FileChooserException.InvalidActivityContextException();
    }

    public static int resolveColorFromAttribute(int attrID) {
        if(FileChooserActivity.getInstance() == null) {
            throw new FileChooserException.InvalidThemeResourceException();
        }
        TypedValue typedValueAttr = new TypedValue();
        FileChooserActivity.getInstance().getTheme().resolveAttribute(attrID, typedValueAttr, true);
        return typedValueAttr.data;
    }

    public static Drawable resolveDrawableFromAttribute(int attrID) {
        if(FileChooserActivity.getInstance() == null) {
            throw new FileChooserException.InvalidThemeResourceException();
        }
        TypedValue typedValueAttr = new TypedValue();
        FileChooserActivity.getInstance().getTheme().resolveAttribute(attrID, typedValueAttr, true);
        return FileChooserActivity.getInstance().getDrawable(typedValueAttr.resourceId);
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
        if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_BL_TR.ordinal())
            return GradientDrawable.Orientation.BL_TR;
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_BOTTOM_TOP.ordinal())
            return GradientDrawable.Orientation.BOTTOM_TOP;
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_BR_TL.ordinal())
            return GradientDrawable.Orientation.BR_TL;
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_LEFT_RIGHT.ordinal())
            return GradientDrawable.Orientation.LEFT_RIGHT;
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_RIGHT_LEFT.ordinal())
            return GradientDrawable.Orientation.RIGHT_LEFT;
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_TL_BR.ordinal())
            return GradientDrawable.Orientation.TL_BR;
        else if(orientConstant == GradientTypeSpec.GRADIENT_FILL_TYPE_TOP_BOTTOM.ordinal())
            return GradientDrawable.Orientation.TOP_BOTTOM;
        else
            return GradientDrawable.Orientation.TR_BL;
    }

    public enum BorderStyleSpec {
        BORDER_STYLE_SOLID,
        BORDER_STYLE_DASHED,
        BORDER_STYLE_DASHED_LONG,
        BORDER_STYLE_DASHED_SHORT,
        BORDER_STYLE_NONE,
    };

    public enum NamedGradientColorThemes {
        NAMED_COLOR_SCHEME_TURQUOISE,
        NAMED_COLOR_SCHEME_YELLOW_TO_BLUE,
        NAMED_COLOR_SCHEME_GREEN_YELLOW_GREEN,
        NAMED_COLOR_SCHEME_METAL_STREAK_BILINEAR,
        NAMED_COLOR_SCHEME_SILVER_BALLS,
        NAMED_COLOR_SCHEME_EVENING_SKYLINE,
        NAMED_COLOR_SCHEME_RAINBOW_STREAK,
        NAMED_COLOR_SCHEME_STEEL_BLUE,
        NAMED_COLOR_SCHEME_FIRE_BRIMSTONE,
    };

    public static GradientDrawable generateNamedGradientType(GradientMethodSpec gmethodSpec,
                                                            GradientTypeSpec gfillTypeSpec,
                                                            BorderStyleSpec borderStyleSpec,
                                                            float gradientAngleSpec,
                                                            int borderColor,
                                                            int[] colorList) {
        GradientDrawable gradientDrawObj = new GradientDrawable(getOrientationFromEnum(gfillTypeSpec.ordinal()), colorList);
        float centerX = 0.5f, centerY = 0.5f;
        int borderWidth = 5;
        gradientDrawObj.setGradientCenter(centerX, centerY);
        gradientDrawObj.setGradientType(getGradientTypeFromEnum(gmethodSpec.ordinal()));
        gradientDrawObj.setCornerRadius(gradientAngleSpec);
        gradientDrawObj.setShape(getGradientShapeFromEnum(gmethodSpec.ordinal()));
        if(borderStyleSpec == BorderStyleSpec.BORDER_STYLE_SOLID)
            gradientDrawObj.setStroke(borderWidth, borderColor);
        else if(borderStyleSpec == BorderStyleSpec.BORDER_STYLE_DASHED)
            gradientDrawObj.setStroke(borderWidth, borderColor, 10, 10);
        else if(borderStyleSpec == BorderStyleSpec.BORDER_STYLE_DASHED_LONG)
            gradientDrawObj.setStroke(borderWidth, borderColor, 25, 10);
        else if(borderStyleSpec == BorderStyleSpec.BORDER_STYLE_DASHED_SHORT)
            gradientDrawObj.setStroke(borderWidth, borderColor, 4, 10);
        gradientDrawObj.setUseLevel(true);
        return gradientDrawObj;
    }

    public static GradientDrawable generateNamedGradientType(BorderStyleSpec borderStyleSpec,
                                                            int borderColor,
                                                            NamedGradientColorThemes namedColorTheme) {
        GradientMethodSpec gmethodSpec;
        GradientTypeSpec gfillTypeSpec;
        float angleSpec = 0.5f;
        int[] colorList;
        switch(namedColorTheme) {
            case NAMED_COLOR_SCHEME_TURQUOISE:
                gmethodSpec = GradientMethodSpec.GRADIENT_METHOD_LINEAR;
                gfillTypeSpec = GradientTypeSpec.GRADIENT_FILL_TYPE_BR_TL;
                colorList = new int[] {
                        0xFF99DAFF,
                        0xFF008080
                };
                break;
            case NAMED_COLOR_SCHEME_YELLOW_TO_BLUE:
                gmethodSpec = GradientMethodSpec.GRADIENT_METHOD_LINEAR;
                gfillTypeSpec = GradientTypeSpec.GRADIENT_FILL_TYPE_BL_TR;
                angleSpec = 45.0f;
                colorList = new int[] {
                        0xFFFFFF00,
                        0xFF008080,
                };
                break;
            case NAMED_COLOR_SCHEME_GREEN_YELLOW_GREEN:
                gmethodSpec = GradientMethodSpec.GRADIENT_METHOD_LINEAR;
                gfillTypeSpec = GradientTypeSpec.GRADIENT_FILL_TYPE_BL_TR;
                angleSpec = 45.0f;
                colorList = new int[] {
                        0xFF008000,
                        0xFFffff00,
                        0xFF008000,
                };
                break;
            case NAMED_COLOR_SCHEME_METAL_STREAK_BILINEAR:
                gmethodSpec = GradientMethodSpec.GRADIENT_METHOD_LINEAR;
                gfillTypeSpec = GradientTypeSpec.GRADIENT_FILL_TYPE_BL_TR;
                angleSpec = 45.0f;
                colorList = new int[] {
                        0xFF000000,
                        0xFFffffff,
                        0xFF000000
                };
                break;
            case NAMED_COLOR_SCHEME_SILVER_BALLS:
                gmethodSpec = GradientMethodSpec.GRADIENT_METHOD_RADIAL;
                gfillTypeSpec = GradientTypeSpec.GRADIENT_FILL_TYPE_BOTTOM_TOP;
                colorList = new int[] {
                        0xFF000000,
                        0xFFffffff,
                };
                break;
            case NAMED_COLOR_SCHEME_EVENING_SKYLINE:
                gmethodSpec = GradientMethodSpec.GRADIENT_METHOD_LINEAR;
                gfillTypeSpec = GradientTypeSpec.GRADIENT_FILL_TYPE_RIGHT_LEFT;
                angleSpec = 45.0f;
                colorList = new int[] {
                        0xFFff00ff,
                        0xFF00ffff
                };
                break;
            case NAMED_COLOR_SCHEME_RAINBOW_STREAK:
                gmethodSpec = GradientMethodSpec.GRADIENT_METHOD_RECTANGLE;
                gfillTypeSpec = GradientTypeSpec.GRADIENT_FILL_TYPE_BL_TR;
                angleSpec = 45.0f;
                colorList = new int[] {
                        0xFFff0000,
                        0xFFffff00,
                        0xFFff0000
                };
                break;
            case NAMED_COLOR_SCHEME_STEEL_BLUE:
                gmethodSpec = GradientMethodSpec.GRADIENT_METHOD_LINEAR;
                gfillTypeSpec = GradientTypeSpec.GRADIENT_FILL_TYPE_LEFT_RIGHT;
                colorList = new int[] {
                        0xFF008080,
                        0xFFFFFFFF,
                        0xFF005757
                };
                break;
            case NAMED_COLOR_SCHEME_FIRE_BRIMSTONE:
                gmethodSpec = GradientMethodSpec.GRADIENT_METHOD_RADIAL;
                gfillTypeSpec = GradientTypeSpec.GRADIENT_FILL_TYPE_BL_TR;
                angleSpec = 45.0f;
                colorList = new int[] {
                        0xFFFF0000,
                        0xFFffff00,
                        0xFFff0000
                };
                break;
            default:
                return null;
        }
        return generateNamedGradientType(gmethodSpec, gfillTypeSpec, borderStyleSpec, angleSpec, borderColor, colorList);
    }

    public static GradientDrawable generateNamedGradientType(BorderStyleSpec borderStyleSpec,
                                                            NamedGradientColorThemes namedColorTheme) {
        return generateNamedGradientType(borderStyleSpec, resolveColorFromAttribute(R.attr.colorPrimaryDark), namedColorTheme);
    }

    public static class Builder {

        private int[] colorsList;
        private GradientMethodSpec gradientType;
        private float gradientAngle;
        private int borderColor;
        private BorderStyleSpec borderStyle;
        private GradientTypeSpec gradientFillStyle;
        private boolean useNamedColorTheme;
        private NamedGradientColorThemes namedColorTheme;


        public Builder() {
            colorsList = new int[] {};
            gradientType = GradientMethodSpec.GRADIENT_METHOD_LINEAR;
            gradientAngle = 90.0f;
            borderColor = 0;
            borderStyle = BorderStyleSpec.BORDER_STYLE_DASHED_LONG;
            gradientFillStyle = GradientTypeSpec.GRADIENT_FILL_TYPE_BL_TR;
            useNamedColorTheme = false;
            namedColorTheme = null;
        }

        public Builder setColorsArray(int[] colorsArray) {
            colorsList = colorsArray;
            return this;
        }

        public Builder setGradientType(GradientMethodSpec gradType) {
            gradientType = gradType;
            return this;
        }

        public Builder setGradientAngle(float gradAngle) {
            gradientAngle = gradAngle;
            return this;
        }

        public Builder setBorderColor(int bdrColor) {
            borderColor = bdrColor;
            return this;
        }

        public Builder setBorderStyle(BorderStyleSpec bdrStyle) {
            borderStyle = bdrStyle;
            return this;
        }

        public Builder setFileStyle(GradientTypeSpec gradFillStyle) {
            gradientFillStyle = gradFillStyle;
            return this;
        }

        public Builder setNamedColorScheme(NamedGradientColorThemes namedTheme) {
            if(namedTheme != null) {
                namedColorTheme = namedTheme;
                useNamedColorTheme = true;
            }
            else {
                namedColorTheme = null;
                useNamedColorTheme = false;
            }
            return this;
        }

        public GradientDrawable make() {
            if(useNamedColorTheme) {
                return generateNamedGradientType(borderStyle, borderColor, namedColorTheme);
            }
            return generateNamedGradientType(gradientType, gradientFillStyle, borderStyle, gradientAngle, borderColor, colorsList);
        }

    }

}
