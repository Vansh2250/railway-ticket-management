package com.example.railwayticketmanagement.utils

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Utility to safely apply system status bar insets to header views,
 * preventing any overlap with the system status bar, camera notch, or clock.
 */
object InsetsUtil {

    fun applyStatusBarPadding(view: View?) {
        if (view == null) return
        val initialPaddingTop = view.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.updatePadding(top = initialPaddingTop + statusBarInsets.top)
            insets
        }

        if (view.isAttachedToWindow) {
            view.requestApplyInsets()
        } else {
            view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    v.requestApplyInsets()
                }

                override fun onViewDetachedFromWindow(v: View) {}
            })
        }
    }
}
