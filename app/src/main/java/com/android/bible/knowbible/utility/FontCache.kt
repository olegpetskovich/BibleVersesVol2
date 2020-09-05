package com.android.bible.knowbible.utility

import android.content.Context
import android.graphics.Typeface
import java.util.*

object FontCache {
    private val fontCache: Hashtable<String?, Typeface?>? = Hashtable()
    operator fun get(name: String?, context: Context?): Typeface? {
        var tf: Typeface? = fontCache?.get(name)
        if (tf == null) {
            tf = try {
                Typeface.createFromAsset(context?.assets, "fonts/$name")
            } catch (e: Exception) {
                return null
            }
            fontCache?.set(name, tf)
        }
        return tf
    }
}
