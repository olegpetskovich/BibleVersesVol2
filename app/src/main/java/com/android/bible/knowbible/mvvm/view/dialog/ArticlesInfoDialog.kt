package com.android.bible.knowbible.mvvm.view.dialog

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.utility.SaveLoadData
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment
import com.google.android.material.button.MaterialButton

class ArticlesInfoDialog(private val listener: DialogListener) : AppCompatDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = context?.let { AlertDialog.Builder(it) }!!
        val inflater = LayoutInflater.from(context)
        val myView: View = inflater.inflate(R.layout.dialog_articles_info, null)

        //По непонятной причине в диалогах тема не меняется, поэтому приходится менять их в каждом диалоге
        when (SaveLoadData(context!!).loadString(ThemeModeFragment.THEME_NAME_KEY)) {
            ThemeModeFragment.LIGHT_THEME -> ThemeManager.theme = ThemeManager.Theme.LIGHT
            ThemeModeFragment.DARK_THEME -> ThemeManager.theme = ThemeManager.Theme.DARK
            ThemeModeFragment.BOOK_THEME -> ThemeManager.theme = ThemeManager.Theme.BOOK
        }

        val btnTelegramChannel: ImageView = myView.findViewById(R.id.btnTelegramChannel)
        btnTelegramChannel.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.telegram_channel_link)))) }

        val btnInstagramChannel: ImageView = myView.findViewById(R.id.btnInstagramChannel)
        btnInstagramChannel.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.instagram_channel_link)))) }

        val btnDismissDialog: MaterialButton = myView.findViewById(R.id.btnDismissDialog)
        btnDismissDialog.setOnClickListener { listener.dismissDialog() }

        builder.setView(myView)
        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        //Устанавливаем закругленные края диалогу, ещё одна обязательная строка находится перед вызовом super.onCreate(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_corners);
    }

    //Метод для связи с активити
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        listener = try {
//            context as LanguageDialogListener
//        } catch (e: ClassCastException) {
//            throw ClassCastException(context.toString() + "must implement LanguageDialogListener")
//        }
//    }
}
