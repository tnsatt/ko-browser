/*Copyright by MonnyLab*/

package com.xlab.vbrowser.browser

import android.content.Context
import com.google.android.material.appbar.AppBarLayout
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import com.xlab.vbrowser.databinding.BrowserDisplayToolbarBinding

/**
 * The toolbar of the BrowserFragment; displaying the URL and other controls.
 */
class DisplayToolbar(context: Context, attrs: AttributeSet) : AppBarLayout(context, attrs), AppBarLayout.OnOffsetChangedListener {
    lateinit var binding: BrowserDisplayToolbarBinding
    private val collapsedProgressTranslationY: Float by lazy {
        TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 2f, resources.displayMetrics)
    }

    init {
        addOnOffsetChangedListener(this)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        binding = BrowserDisplayToolbarBinding.bind(this)
        // When scrolling the toolbar away we want to fade out the content on the toolbar
        // with an alpha animation. This will avoid that the text clashes with the status bar.

        val totalScrollRange = appBarLayout.totalScrollRange
        val isCollapsed = Math.abs(verticalOffset) == totalScrollRange

        // If the toolbar is collapsed then we will move the progress view so that it's
        // still fully visible.
        binding.progress.translationY = if (isCollapsed) collapsedProgressTranslationY else 0f

        if (verticalOffset == 0 || isCollapsed) {
            // If the app bar is completely expanded or collapsed we want full opacity. We
            // even want full opacity for a collapsed app bar because while loading a website
            // the toolbar sometimes pops out when the URL changes. Without setting it to
            // opaque the toolbar content might be invisible in this case (See issue #1126)
            binding.toolbarContent.root.alpha = 1f
            return
        }

        // The toolbar content should have 100% alpha when the AppBarLayout is expanded and
        // should have 0 alpha when the toolbar is collapsed 50% or more.
        var alpha = -1 * (100f / (totalScrollRange * 0.5f) * verticalOffset / 100)

        // We never want to go lower than 0 or higher than 1.
        alpha = Math.max(0f, alpha)
        alpha = Math.min(1f, alpha)

        // The calculated value is reversed and we need to invert it (e.g. 0.8 -> 0.2)
        alpha = 1 - alpha

        binding.toolbarContent.root.alpha = alpha
    }
}
