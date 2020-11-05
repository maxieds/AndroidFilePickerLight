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

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

public class DisplayListeners {

    private static String LOGTAG = DisplayListeners.class.getSimpleName();

    public static class RecyclerViewTouchListener implements RecyclerView.OnItemTouchListener {

        public interface ClickListener {
            void onClick(View view, int position);
            void onLongClick(View view, RecyclerView recyclerView, int position);
        }

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerViewTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if(child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rview, MotionEvent mevt) {
            View child = rview.findChildViewUnder(mevt.getX(), mevt.getY());
            if(child != null && clickListener != null && gestureDetector.onTouchEvent(mevt)) {
                clickListener.onClick(child, rview.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rview, MotionEvent mevt) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean state) {}

    }

}
