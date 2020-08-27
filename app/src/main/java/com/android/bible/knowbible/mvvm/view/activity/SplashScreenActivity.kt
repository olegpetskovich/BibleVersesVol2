package com.android.bible.knowbible.mvvm.view.activity

import android.animation.Animator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.utility.SaveLoadData
import com.android.bible.knowbible.utility.Utility
import com.android.bible.knowbible.utility.Utility.Companion.viewAnimatorY

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var textView: TextView
    private lateinit var iV: ImageView

    private lateinit var saveLoadData: SaveLoadData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        saveLoadData = SaveLoadData(this)

        var themeName: String? = saveLoadData.loadString(ThemeModeFragment.THEME_NAME_KEY)
        //Устаналиваем дефолтное значение, если ничего не установлено
        if (themeName != null) {
            if (themeName.isEmpty()) {
                themeName = ThemeModeFragment.LIGHT_THEME
                saveLoadData.saveString(ThemeModeFragment.THEME_NAME_KEY, ThemeModeFragment.LIGHT_THEME)
            }
        }

        when (themeName) {
            ThemeModeFragment.LIGHT_THEME -> setTheme(ThemeManager.Theme.LIGHT)
            ThemeModeFragment.DARK_THEME -> setTheme(ThemeManager.Theme.DARK)
            ThemeModeFragment.BOOK_THEME -> setTheme(ThemeManager.Theme.BOOK)
        }

        iV = findViewById(R.id.iV)
        textView = findViewById(R.id.tV)

        val animationAppTitle = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val animationLogo = AnimationUtils.loadAnimation(this, R.anim.zoom_in_logo)
        animationLogo.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                val anim = viewAnimatorY(Utility.convertDbInPx(this@SplashScreenActivity, -70f), iV, 450)
                anim?.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        textView.startAnimation(animationAppTitle)
                        textView.visibility = View.VISIBLE

                    }

                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationStart(animation: Animator?) {}
                })
                anim?.start()
            }

            override fun onAnimationStart(animation: Animation?) {}
        })
        iV.startAnimation(animationLogo)
        iV.visibility = View.VISIBLE

        val run1 = Thread {
            try {
                Thread.sleep(1400)
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
        run1.start()
    }

    private fun setTheme(theme: ThemeManager.Theme) {
        ThemeManager.theme = theme

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            when (theme) {
                ThemeManager.Theme.LIGHT -> window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.colorStatusBarLightTheme)
                ThemeManager.Theme.DARK -> window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.colorStatusBarDarkTheme)
                ThemeManager.Theme.BOOK -> window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.colorStatusBarBookTheme)
            }
        }
    }
}
