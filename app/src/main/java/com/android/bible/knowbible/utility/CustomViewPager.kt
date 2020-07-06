package com.android.bible.knowbible.utility

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {
    private var isSwipingEnabled = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return isSwipingEnabled && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return isSwipingEnabled && super.onInterceptTouchEvent(event)
    }

    fun setSwipeState(isSwipingEnabled: Boolean) {
        this.isSwipingEnabled = isSwipingEnabled
    }
}