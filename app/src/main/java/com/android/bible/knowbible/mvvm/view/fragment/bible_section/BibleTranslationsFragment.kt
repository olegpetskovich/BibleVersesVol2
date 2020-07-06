package com.android.bible.knowbible.mvvm.view.fragment.bible_section

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.BibleTranslationModel
import com.android.bible.knowbible.mvvm.view.adapter.BibleTranslationsRVAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.dialog.BibleTranslationDialog
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.android.bible.knowbible.mvvm.viewmodel.BibleTranslationsViewModel
import com.android.bible.knowbible.utility.SaveLoadData
import com.android.bible.knowbible.utility.Utility
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.StorageTask
import com.google.gson.Gson
import java.io.File

class BibleTranslationsFragment : Fragment(), BibleTranslationsRVAdapter.IFragmentCommunication, DialogListener {
    companion object {
        const val TRANSLATION_DB_FILE_JSON_INFO = "TRANSLATION_DB_FILE_JSON_INFO"
    }

    private lateinit var listener: IActivityCommunicationListener

    private lateinit var myFragmentManager: FragmentManager

    private lateinit var bibleTranslationDialog: BibleTranslationDialog

    private lateinit var rvAdapter: BibleTranslationsRVAdapter

    private lateinit var saveLoadData: SaveLoadData

    private var fileForDeleting: File? = null
    private var fileToCancel: StorageTask<FileDownloadTask.TaskSnapshot>? = null

    private lateinit var bibleTranslationsViewModel: BibleTranslationsViewModel //Этот ViewModel используется сугубо для этого фрагмента
    private lateinit var bibleDataViewModel: BibleDataViewModel /*Этот ViewModel используется для тех остальных фрагментов, которые получают тексты Библии.
                                                                               Здесь он используется для того, чтобы открыть выбранную базу данных.
                                                                               Потом она сохранится и в дальнейшем в других фрагментах при открытии текстов Библии
                                                                               не придётся снова открывать БД,
                                                                               а будет использоваться эта раннее открытая в этом фрагмент и сохранённая в этом ViewModel БД*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Utility.log("onCreate")

        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
        saveLoadData = SaveLoadData(context!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_bible_translations, container, false)
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение

        Utility.log("onCreateView")
//        Log.d("MyTag", myFragmentManager.backStackEntryCount.toString())


        if (!Utility.isTranslationsDownloaded(context!!)) { //Проверяем, если в папке, которая содержит БД с переводами Библии, пусто, то открываем диалог, в котором говорится, что хотя бы один перевод нужно скачать
            bibleTranslationDialog = BibleTranslationDialog(this)
            bibleTranslationDialog.isCancelable = false
            bibleTranslationDialog.show(childFragmentManager, "Bible Translation Dialog") //Тут должен быть именно childFragmentManager
            listener.setIsTranslationDownloaded(false)
        }

        val recyclerView: RecyclerView = myView.findViewById(R.id.recyclerView)
        bibleTranslationsViewModel = ViewModelProvider(this).get(BibleTranslationsViewModel::class.java)
        bibleTranslationsViewModel
                .getTranslationsList()
                .observe(viewLifecycleOwner, Observer {
                    rvAdapter = BibleTranslationsRVAdapter(context!!, it)
                    rvAdapter.setRecyclerViewFragmentCommunicationListener(this) //Для RecyclerView тему нужно обновлять отдельно от смены темы для всего фрагмента. Если менять тему только для всего фрагмента, не меняя при этом тему для списка, то в списке тема не поменяется.

                    recyclerView.adapter = rvAdapter
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.itemAnimator = DefaultItemAnimator()
                })
        return myView
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun openDatabase(translationObject: BibleTranslationModel) {
        bibleDataViewModel = activity?.let { ViewModelProvider(requireActivity()).get(BibleDataViewModel::class.java) }!!
        bibleDataViewModel.openDatabase(context?.getExternalFilesDir(getString(R.string.folder_name)).toString() + "/" + translationObject.translationDBFileName)
        listener.setBtnSelectTranslationText(translationObject.abbreviationTranslationName) //Устанавливаем в текст кнопки выбора переводов аббревиатуру выбранного перевода
        saveLoadData.saveString(TRANSLATION_DB_FILE_JSON_INFO, Gson().toJson(translationObject))
    }

    override fun changeItemTheme() {
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение
    }

    override fun setIsTranslationDownloaded(isTranslationDownloaded: Boolean) {
        listener.setIsTranslationDownloaded(isTranslationDownloaded)
    }

    override fun setFilePathForDeleting(fileForDeleting: File?, fileToCancel: StorageTask<FileDownloadTask.TaskSnapshot>?) {
        this.fileForDeleting = fileForDeleting
        this.fileToCancel = fileToCancel
    }

    override fun dismissDialog() {
        bibleTranslationDialog.dismiss()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Utility.log("onAttach")

        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
    }

    override fun onResume() {
        super.onResume()
        Utility.log("onResume")

        rvAdapter.notifyDataSetChanged() //В случае если юзер свернёт приложение, когда этот фрагмент будет включён, потом удалит какой-то файл перевода и после этого снова откроет приложение, то список обновится и сразу будет отображено, что перевод удалён

        listener.setTabNumber(1)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(true)
        listener.setBtnSelectTranslationClickableState(false) //Отключаем возможность нажимать на кнопку выбора переводов, чтобы нельзя было открывать фрагмент постоянно. Устанавливать нужно именно в методе onResume, чтобы в случае, когда приложение выводится из свёрнутого, кнопка снова отключилась.

        //Поскольку кнопку перевода нужно показывать только в том случае, когда какой-то перевод уже выбран, проводится такая проверка:
        val jsonBibleInfo = saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO)
        if (jsonBibleInfo != null && jsonBibleInfo.isNotEmpty()) {
            listener.setBtnSelectTranslationVisibility(View.VISIBLE)
        } else {
            listener.setBtnSelectTranslationVisibility(View.GONE)
        }

        listener.setShowHideToolbarBackButton(View.VISIBLE)
    }

    //Сохранять состояние нужно именно в onStop, потому что в ситуации, когда приложение закрывается из фона, просто свайпом, вызывается именно этот метод, а, к примеру, onDestroy нет
    override fun onStop() {
        super.onStop()
        Utility.log("onStop")

        //В случае закрытия приложения в период скачивания, файл будет удалён, потому что скачался не полностью
        if (fileForDeleting != null && fileForDeleting!!.exists() && fileToCancel != null) {
            fileForDeleting!!.canonicalFile.delete()
            fileToCancel!!.cancel()

            //В данном случае сохранять состояние того, скачался ли файл или нет, можно и вне метода addOnCanceledListener, и внутри него, но лучше внутри.
            fileToCancel!!.addOnCanceledListener {
                saveLoadData.saveBoolean(BibleTranslationsRVAdapter.isTranslationDownloading, false) //Устанавливаем значение false, означает, что перевод не скачивается
                Utility.log("Canceled")
            }
            Toast.makeText(context!!, getString(R.string.tv_downloading_canceled), Toast.LENGTH_LONG).show()
        }

        listener.setBtnSelectTranslationClickableState(true) //Включаем возможность нажимать на кнопку выбора переводов
    }

    override fun onDetach() {
        super.onDetach()
        Utility.log("onDetach")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Utility.log("onDestroyView")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utility.log("onDestroy")
    }
}