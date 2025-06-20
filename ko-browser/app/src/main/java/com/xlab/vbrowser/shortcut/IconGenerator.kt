/*Copyright by MonnyLab*/

package com.xlab.vbrowser.shortcut

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Build
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import android.util.TypedValue

import com.xlab.vbrowser.R
import com.xlab.vbrowser.utils.UrlUtils

class IconGenerator {

    companion object {
        private val TEXT_SIZE_DP = 36f
        private val DEFAULT_ICON_CHAR = "?"

        /**
         * Use the given raw website icon and generate a launcher icon from it. If the given icon is null
         * or too small then an icon will be generated based on the website's URL. The icon will be drawn
         * on top of a generic launcher icon shape that we provide.
         */
        @JvmStatic
        fun generateLauncherIcon(context: Context, url: String?) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            generateAdaptiveLauncherIcon(context, url)
        } else {
            generateLauncherIconPreOreo(context, url)
        }

        /*
         * This method needs to be separate from generateAdaptiveLauncherIcon so that we can generate
         * the pre-Oreo icon to display in the Add To Home screen Dialog
         */
        @JvmStatic
        fun generateLauncherIconPreOreo(context: Context, url: String?): Bitmap {
            val options = BitmapFactory.Options()
            options.inMutable = true
            val shape = BitmapFactory.decodeResource(context.resources, R.drawable.ic_homescreen_shape, options)
            return drawCharacterOnBitmap(context, url, shape)
        }

        /**
         * Generates a launcher icon for versions of Android that support Adaptive Icons (Oreo+):
         * https://developer.android.com/guide/practices/ui_guidelines/icon_design_adaptive.html
         */
        private fun generateAdaptiveLauncherIcon(context: Context, url: String?): Bitmap {
            val res = context.resources
            val adaptiveIconDimen = res.getDimensionPixelSize(R.dimen.adaptive_icon_drawable_dimen)

            val bitmap = Bitmap.createBitmap(adaptiveIconDimen, adaptiveIconDimen, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Adaptive Icons have two layers: a background that fills the canvas and
            // a foreground that's centered. First, we draw the background...
            canvas.drawColor(ContextCompat.getColor(context, R.color.add_to_homescreen_icon_background))

            // Then draw the foreground
            return drawCharacterOnBitmap(context, url, bitmap)
        }

        private fun drawCharacterOnBitmap(context: Context, url: String?, bitmap: Bitmap): Bitmap {
            val canvas = Canvas(bitmap)

            val paint = Paint()

            val character = getRepresentativeCharacter(url)

            val textSize = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, TEXT_SIZE_DP, context.resources.displayMetrics)

            paint.color = Color.WHITE
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = textSize
            paint.isAntiAlias = true

            canvas.drawText(character,
                    canvas.width / 2.0f,
                    canvas.height / 2.0f - (paint.descent() + paint.ascent()) / 2.0f,
                    paint)

            return bitmap
        }

        /**
         * Get a representative character for the given URL.
         *
         * For example this method will return "f" for "http://m.facebook.com/foobar".
         */
        @VisibleForTesting internal fun getRepresentativeCharacter(url: String?): String {
            val firstChar = getRepresentativeSnippet(url)?.find { it.isLetterOrDigit() }?.toUpperCase()
            return (firstChar ?: DEFAULT_ICON_CHAR).toString()
        }

        /**
         * Get the representative part of the URL. Usually this is the host (without common prefixes).
         *
         * @return the representative snippet or null if one could not be found.
         */
        private fun getRepresentativeSnippet(url: String?): String? {
            if (url == null || url.isEmpty()) return null

            val uri = Uri.parse(url)
            val snippet = if (!uri.host.isNullOrEmpty()) {
                uri.host // cached by Uri class.
            } else if (!uri.path.isNullOrEmpty()) { // The uri may not have a host for e.g. file:// uri
                uri.path // cached by Uri class.
            } else {
                return null
            }

            // Strip common prefixes that we do not want to use to determine the representative characters
            return UrlUtils.stripCommonSubdomains(snippet)
        }
    }
}
