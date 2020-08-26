package com.android.bible.knowbible.mvvm.view.activity

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.android.bible.knowbible.App
import com.android.bible.knowbible.R
import com.android.bible.knowbible.data.local.NotesDBHelper
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.model.BibleTranslationModel
import com.android.bible.knowbible.mvvm.view.adapter.BibleTextRVAdapter.Companion.isMultiSelectionEnabled
import com.android.bible.knowbible.mvvm.view.adapter.BibleTranslationsRVAdapter
import com.android.bible.knowbible.mvvm.view.adapter.ViewPagerAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.DialogListener
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.dialog.AddNoteDialog
import com.android.bible.knowbible.mvvm.view.dialog.ArticlesInfoDialog
import com.android.bible.knowbible.mvvm.view.fragment.articles_section.ArticlesRootFragment
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.BibleRootFragment
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.BibleTextFragment
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.BibleTranslationsFragment
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.BibleTranslationsFragment.Companion.TRANSLATION_DB_FILE_JSON_INFO
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.notes_subsection.AddEditNoteFragment
import com.android.bible.knowbible.mvvm.view.fragment.more_section.AppLanguageFragment
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.BOOK_THEME
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.DARK_THEME
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.LIGHT_THEME
import com.android.bible.knowbible.mvvm.view.fragment.more_section.ThemeModeFragment.Companion.THEME_NAME_KEY
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.BibleDataViewModel
import com.android.bible.knowbible.utility.SaveLoadData
import com.android.bible.knowbible.utility.Utility
import com.android.bible.knowbible.utility.Utility.Companion.convertDbInPx
import com.android.bible.knowbible.utility.Utility.Companion.viewAnimatorX
import com.apps.oleg.bibleverses.mvvm.view.fragment.more_section.MoreRootFragment
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), BibleTextFragment.OnViewPagerSwipeStateListener, IActivityCommunicationListener, AppLanguageFragment.IAppLanguageChangerListener, DialogListener {
    /* ОБЪЯСНЕНИЕ НАЛИЧИЯ ЭТОГО КОДА.
        *       Изначально проблема заключалась в том, что для всех фрагментов, даже если они расположены в отдельных табах, по умолчанию идёт один backStack.
        * То есть, если, к примеру, в первом табе перейти из Фрагмент1 в Фрагмент2, добавив при этом Фрагмент2 в backStack(методом addToBackStack(null)),
        * а потом перейти во второй таб и нажать кнопку "Назад"(при этом предполагая, что по логике приложение закроется),
        * то приложение не закроется, но вместо этого из backStack будет удалён Фрагмент2, и если мы перейдём обратно в первый таб, то увидим, что Фрагмент2 закрыт.
        * Другими словами, в первом табе мы перешли из Фрагмент1 в Фрагмент2, затем открыли второй таб, нажимаем кнопку "Назад", визуально перед пользователем ничего не происходит
        * (а в действительности в первом табе закрывается Фрагмент2, но пользователь этого не видит, потому что перед ним открыт второй таб)
        * и чтобы всё же закрыть приложение, нужно снова нажать кнопку "Назад".
        *       Решением этой проблемы стала реализация отдельного backStack для каждого таба, но реализовано это с некоторыми нюансами.
        * Сделано это вот каким образом: в каждом табе сделан первичный главный Фрагмент(RootFragment), и вместо того,
        * чтобы во всех фрагментах использовать общий FragmentManager(как это обычно делается), в данном случае в главном фрагмент каждого отдельного таба вызывается метод childFragmentManager.
        * Этот childFragmentManager в дальнейшем передаётся во всех последующие фрагменты, которые содержаться в главном фрагменте.
        * И когда в каждом отдельном табе используется свой childFragmentManager, то и backStack у каждого свой, потому что backStack мы получаем из childFragmentManager.
        * Но в этом решении есть нюанс — по непонятной причине, вызывая addToBackStack(null) фрагменты добавляются в backStack, но, нажимая кнопку "Назад",
        * фрагменты не закрываются по тому порядку, в котором они находятся в backStack, вместо этого приложение вовсе закрывается.
        * И чтобы решить эту проблему, пришлось использовать static поля и переопределять метод onBackPressed() в данном активити.
        * Поле var isBackStackNotEmpty: Boolean нужно для того, чтобы указать, есть ли фрагменты в backStack или их нет. Значение в это поле устанавливается в каждом фрагменте.
        * Указывается оно в методе фрагмента onResume() (Ни в каком другом, потому что onResume() вызывается сразу как открывается таб,
        * а нам как раз нужно устанавливать значение каждого таба как только он открыт, потому что onCreate, к примеру, срабатывает только при создании фрагмента, а не при каждом его открытии).
        * Если backStack пустой, то при нажатии "Назад" приложение закроется, если же backStack содержит фрагменты, то сработает код с использованием поля var myFragmentManager: FragmentManager.
        * Поле var myFragmentManager: FragmentManager нужно для того, чтобы присваивать ему childFragmentManager каждого отдельного RootFragment и таким образом очищать стэк в каждом табе поотдельности.
        * В конечном итоге логика работы такова: мы устанавливаем значение true в поле var isBackStackNotEmpty: Boolean в фрагменте,
        * который нужно удалить из backStack, закрывая его кнопкной "Назад" и присваиваем в поле myFragmentManager тот childFragmentManager, в фрагментах которого мы находимся.
        * В override методе onBackPressed() присходит проверка: если isBackStackNotEmpty true,
        * то вызывается метод удаления фрагмента из backStack. А вызывается метод удаления фрагмента в том childFragmentManager, который отправлен в поле myFragmentManager.
        * Если же isBackStackNotEmpty == false, то срабатывает стандартный код системного метода onBackPressed() и приложение просто закрывается.
        * Это единственный удобный способ без кучи кода, который я сам и придумал, перерыв до этого весь интернет по этому вопросу, официальной версии решения этого вопроса нет.*/
    private var isBackStackNotEmpty: Boolean = false
    private lateinit var myFragmentManager: FragmentManager
    private var isTranslationDownloaded: Boolean = true //Поле сугубо для BibleLanguageFragment, которое нужно, чтобы указывать скачан перевод или нет. Если не скачан, то при нажатии кнопки назад приложение закрывается, если же перевод есть, то при нажатии назад будет открыт предыдущий фрагмент.

    companion object {
        var isBackButtonClicked: Boolean = false //Поле сугубо для BibleTextFragment. Оно нужно, чтобы с помощью его значения при закрытии этого фрагмента кнопкой "Назад" очищать данные скролла.
    }

    private var tabNumber: Int = 1 //Номер таба нужен, чтобы при повороте экрана в вертикальное положение, устанавливать выделенную иконку в том табе, который выбран
    //По умолчанию номер таба 1, это чтобы при открытии приложения, выделялась иконка таба Библия

    private var noteId: Int = -1

    private lateinit var bibleTextFragment: BibleTextFragment //Объект необходим для управления BottomAppBar в BibleTextFragment

    private lateinit var multiSelectedTextsList: ArrayList<BibleTextModel> //Список данных необходим для обработки двух и более выбранных текстов Библии в режиме мульти выбора

    private var articlesInfoDialog: ArticlesInfoDialog? = null
    private var addNoteDialog: AddNoteDialog? = null

    private lateinit var saveLoadData: SaveLoadData

    override fun onCreate(savedInstanceState: Bundle?) {
//        LayoutInflaterCompat.setFactory2(
//                LayoutInflater.from(this),
//                MyLayoutInflater(delegate)
//        )

        super.onCreate(savedInstanceState)
        saveLoadData = SaveLoadData(this)
        loadLocale()

        setContentView(R.layout.activity_main)

        when (saveLoadData.loadString(THEME_NAME_KEY)) {
            LIGHT_THEME -> setTheme(ThemeManager.Theme.LIGHT, false)
            DARK_THEME -> setTheme(ThemeManager.Theme.DARK, false)
            BOOK_THEME -> setTheme(ThemeManager.Theme.BOOK, false)
        }


        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewPager.offscreenPageLimit = 2 /*ВАЖНО ПОМНИТЬ, ЕСЛИ КОЛИЧЕСТВО ТАБОВ РАСТЁТ, ТО И ЛИМИТ СОХРАНЁННЫХ ФРАГМЕНТОВ НУЖНО ПОВЫШАТЬ
                                           Объяснение вызова этого метода: https://stackoverflow.com/questions/27601920/android-viewpager-with-tabs-save-state, https://developer.android.com/reference/android/support/v4/view/ViewPager#setoffscreenpagelimit*/
        setupViewPager(viewPager)
        tabLayout.setupWithViewPager(viewPager)
        viewPager.currentItem = tabNumber //устанавливаем, чтобы при открытии приложения, сразу включался второй таб

        setupTabIconsContent()

        //BottomAppBar кнопки
        btnHome.setOnClickListener { bibleTextFragment.btnHomeClicked() }
        btnInterpretation.setOnClickListener { bibleTextFragment.btnInterpretationClicked() }
        btnNotes.setOnClickListener { bibleTextFragment.btnNotesClicked() }
        btnSearch.setOnClickListener { bibleTextFragment.btnSearchClicked() }

        btnBack.setOnClickListener { onBackPressed() }

        btnSelectTranslation.setOnClickListener {
            val fragment = BibleTranslationsFragment()
            fragment.setRootFragmentManager(myFragmentManager)
            val transaction: FragmentTransaction = myFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            transaction.replace(R.id.fragment_container_bible, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        btnArticlesInfo.setOnClickListener {
            articlesInfoDialog = ArticlesInfoDialog(this)
//            articlesInfoDialog.isCancelable = false
            articlesInfoDialog!!.show(myFragmentManager, "Articles Info Dialog")
        }

        btnDeleteNote.setOnClickListener {
            val mainHandler = Handler(mainLooper)
            if (noteId != -1) {
                //Специальный вызов, для вызова метода в другом потоке с помощью RX
                Completable
                        .fromAction {
                            NotesDBHelper(this).deleteVerse(noteId)
                            noteId = -1
                            onBackPressed() //Возвращаемся назад, чтобы закрыть фрагмент заметки
                            //Поскольку Toast можно вызывать только в главном потоке, отправляем его в главный поток с помощью Handler
                            val myRunnable = Runnable { Toast.makeText(this, getString(R.string.toast_note_deleted), Toast.LENGTH_SHORT).show() }
                            mainHandler.post(myRunnable)
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe()
            }
        }

        btnAddNoteFAB.setOnClickListener {
            val fragment = AddEditNoteFragment()
            fragment.isNoteToAdd = true
            fragment.setRootFragmentManager(myFragmentManager)
            val transaction: FragmentTransaction = myFragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
            transaction.replace(R.id.fragment_container_bible, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        //Кнопки панели множественного выбора текстов
        btnShare.setOnClickListener {
            val shareBody = getFormattedMultiSelectedText()

            val myIntent = Intent(Intent.ACTION_SEND)
            myIntent.type = "text/plain"
            myIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
            startActivity(Intent.createChooser(myIntent, getString(R.string.toast_share_verse)))

            //Отключаем режим множественного выбора после выполнения метода
            disableMultiSelection()
        }

        btnCopy.setOnClickListener {
            val textForCopy = getFormattedMultiSelectedText()

            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("label", textForCopy)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(this, getString(R.string.verse_copied), Toast.LENGTH_SHORT).show()

            //Отключаем режим множественного выбора после выполнения метода
            disableMultiSelection()
        }

        btnAddNote.setOnClickListener {
            addNoteDialog = AddNoteDialog(this)
            addNoteDialog!!.setVerse(getFormattedMultiSelectedText())
            addNoteDialog!!.show(myFragmentManager, "Add Note Dialog") //По непонятной причине открыть диалог вызываемым здесь childFragmentManager-ом не получается, поэтому приходится использовать переданный объект fragmentManager из другого класса
        }

        btnHighlight.setOnClickListener {}

        //Открываем БД с ранее используемым переводом Библии и в кнопку выбора переводов устанавливаем аббревиатуру перевода, который был ранее выбран пользователем
        val jsonBibleInfo = saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO)
        if (jsonBibleInfo != null && jsonBibleInfo.isNotEmpty()) {
            val gson = Gson()
            val bibleTranslationInfo: BibleTranslationModel = gson.fromJson(jsonBibleInfo, BibleTranslationModel::class.java)

            //Проверка на то, скачан ли перевод, выбранный ранее, или же перевод удалён и в saveLoadData хранится имя скачанного файла, но его самого не существует.
            //Если эту проверку не осуществлять, то в случае удаления выбранного перевода, программа будет пытаться открыть его, но не сможет,
            //потому что в действительности он будет удалён
            if (Utility.isSelectedTranslationDownloaded(this, bibleTranslationInfo)) {
                val bibleInfoViewModel = ViewModelProvider(this).get(BibleDataViewModel::class.java)
                bibleInfoViewModel.openDatabase(getExternalFilesDir(getString(R.string.folder_name)).toString() + "/" + bibleTranslationInfo.translationDBFileName)

                btnSelectTranslation.text = bibleTranslationInfo.abbreviationTranslationName
            } else {
                btnSelectTranslation.visibility = View.GONE //Если не один перевод не выбран и не скачан, то скрываем кнопку выбора перевода
                btnSelectTranslation.isEnabled = false
            }
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
//                && checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
//                && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE), 1000)
//        }
    }

    override fun disableMultiSelection() {
        if (isMultiSelectionEnabled) {
            //Отключаем режим множественного выбора текстов и убираем видимость кнопок
            isMultiSelectionEnabled = false
            setShowHideMultiSelectionPanel(false)

            //Устанавливаем параметру isTextSelected значение false, чтобы при обновлении списка снялось выделение с выбранных айтемов
            for (selectedText in multiSelectedTextsList) selectedText.isTextSelected = false
            bibleTextFragment.notifyDataSetChanged()
        }
    }

    //Этот метод вызывается в случае, если была нажата кнопка btnHome в BottomAppBar. Его отличие от метода disableMultiSelection заключается в том,
    //что в данном методе вызывается метод closeMultiSelectionPanelIfButtonHomeClicked в отличие от вызова метода setShowHideMultiSelectionPanel(false) в disableMultiSelection.
    //Вызов метода closeMultiSelectionPanelIfButtonHomeClicked точно так же убирает панель множественного выбора, но, в отличие от вызова метода setShowHideMultiSelectionPanel(false),
    //метод closeMultiSelectionPanelIfButtonHomeClicked не задаёт кнопке btnSelectTranslation видимость VISIBLE,
    //потому что нажатие кнопки btnHome обеспечивает переход из BibleTextFragment в SelectTestamentFragment.
    //А в фрагменте SelectTestamentFragment не должна быть видна кнопка btnSelectTranslation.
    override fun disableMultiSelectionIfButtonHomeClicked() {
        if (isMultiSelectionEnabled) {
            //Отключаем режим множественного выбора текстов и убираем видимость кнопок
            isMultiSelectionEnabled = false
            closeMultiSelectionPanelIfButtonHomeClicked()

            //Устанавливаем параметру isTextSelected значение false, чтобы при обновлении списка снялось выделение с выбранных айтемов
            for (selectedText in multiSelectedTextsList) selectedText.isTextSelected = false
            bibleTextFragment.notifyDataSetChanged()
        }
    }

    //Метод, для форматирования выбранных текстов Библии.
    //Поскольку этот код используется несколько раз, для предотвращения дубликации кода этот код был вынесен в отдельный метод
    private fun getFormattedMultiSelectedText(): String {
        var selectedText = ""
        for (selectedTextModel in multiSelectedTextsList) {
            selectedText += selectedTextModel.text
        }

        return "«" + selectedText + "»" + " (" + tvSelectedBibleBook.text + tvSelectedBibleChapter.text + tvSelectedBibleVerse.text + ")"
    }

    //Анимация появления кнопки назад в Toolbar
    override fun setShowHideToolbarBackButton(backButtonVisibility: Int) {
        //Этот фрагмент кода нужен, чтобы тулбар не анимировался каждый раз при переходе между табами. Если в одном табе стрелка включена и во втором табе тоже,
        //то в таком случае выходим из метода без всяких анимаций, чтобы анимация не запускалась без надобности.
        val btnBack: ImageView = findViewById(R.id.btnBack)
        val btnBackVisibility = btnBack.visibility
        if (btnBackVisibility == backButtonVisibility) {
            return
        }

//        val params: RelativeLayout.LayoutParams = toolbarTitle.layoutParams as RelativeLayout.LayoutParams

        val animationTitle: ObjectAnimator?
        val animationSelectedText: ObjectAnimator?
        val pixels: Float
        if (backButtonVisibility == View.VISIBLE) {
            //Конвертируем db в пиксели, потому что метод анимации на вход принимает пиксели, а нужно устанавливать значение в db
            pixels = convertDbInPx(this, 55f)
            animationSelectedText = viewAnimatorX(pixels, layTvSelectedBibleText, 250) //Анимируем TextView выбранного текст Библии вместе с title тулбара
            animationTitle = viewAnimatorX(pixels, toolbarTitle, 250)
            animationTitle?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {
                    btnBack.startAnimation(AnimationUtils.loadAnimation(this@MainActivity, R.anim.zoom_in))
                    btnBack.visibility = View.VISIBLE
                }

                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {}
            })
        } else {
            //Конвертируем db в пиксели, потому что метод анимации на вход принимает пиксели, а нужно устанавливать значение в db
            pixels = convertDbInPx(this, 14f)
            animationSelectedText = viewAnimatorX(pixels, layTvSelectedBibleText, 250) //Анимируем TextView выбранного текст Библии вместе с title тулбара
            animationTitle = viewAnimatorX(pixels, toolbarTitle, 250)
            animationTitle?.addListener(object : Animator.AnimatorListener {
                override fun onAnimationRepeat(animation: Animator?) {}
                override fun onAnimationEnd(animation: Animator?) {}
                override fun onAnimationCancel(animation: Animator?) {}
                override fun onAnimationStart(animation: Animator?) {
                    btnBack.startAnimation(AnimationUtils.loadAnimation(this@MainActivity, R.anim.zoom_out))
                    btnBack.visibility = View.GONE
                }
            })
        }
        animationSelectedText?.start()
        animationTitle?.start()
    }

    override fun setNoteIdForDelete(noteId: Int) {
        this.noteId = noteId
    }

    override fun setShowHideMultiSelectionPanel(isVisible: Boolean) {
        val animationBtnSelectTranslation: Animation

        val animationBtnShare: Animation
        val animationBtnCopy: Animation
        val animationBtnAddNote: Animation
        val animationBtnHighlight: Animation

        if (isVisible) {
            animationBtnSelectTranslation = AnimationUtils.loadAnimation(this, R.anim.fade_out)

            animationBtnShare = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
            animationBtnCopy = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
            animationBtnAddNote = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
            animationBtnHighlight = AnimationUtils.loadAnimation(this, R.anim.zoom_in)

            tvSelectedBibleVerse.visibility = View.VISIBLE

            btnSelectTranslation.startAnimation(animationBtnSelectTranslation)
            btnSelectTranslation.visibility = View.GONE
            btnSelectTranslation.isEnabled = false
            animationBtnSelectTranslation?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {}

                override fun onAnimationStart(animation: Animation?) {
                    btnShare.startAnimation(animationBtnShare)
                    btnShare.visibility = View.VISIBLE
                    btnShare.isEnabled = true
                    animationBtnShare.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationRepeat(animation: Animation?) {}

                        override fun onAnimationEnd(animation: Animation?) {
                            btnCopy.startAnimation(animationBtnCopy)
                            btnCopy.visibility = View.VISIBLE
                            btnCopy.isEnabled = true
                            animationBtnCopy?.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationRepeat(animation: Animation?) {}

                                override fun onAnimationEnd(animation: Animation?) {
                                    btnAddNote.startAnimation(animationBtnAddNote)
                                    btnAddNote.visibility = View.VISIBLE
                                    btnAddNote.isEnabled = true
                                    animationBtnAddNote.setAnimationListener(object : Animation.AnimationListener {
                                        override fun onAnimationRepeat(animation: Animation?) {}

                                        override fun onAnimationEnd(animation: Animation?) {
                                            btnHighlight.startAnimation(animationBtnHighlight)
                                            btnHighlight.visibility = View.VISIBLE
                                            btnHighlight.isEnabled = true
                                        }

                                        override fun onAnimationStart(animation: Animation?) {}
                                    })
                                }

                                override fun onAnimationStart(animation: Animation?) {}
                            })
                        }

                        override fun onAnimationStart(animation: Animation?) {}
                    })
                }
            })
        } else {
            animationBtnSelectTranslation = AnimationUtils.loadAnimation(this, R.anim.fade_in)

            animationBtnShare = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
            animationBtnCopy = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
            animationBtnAddNote = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
            animationBtnHighlight = AnimationUtils.loadAnimation(this, R.anim.zoom_out)

            tvSelectedBibleVerse.visibility = View.GONE

            btnHighlight.startAnimation(animationBtnHighlight)
            btnHighlight.visibility = View.GONE
            btnHighlight.isEnabled = false
            animationBtnHighlight?.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}

                override fun onAnimationEnd(animation: Animation?) {
                    btnAddNote.startAnimation(animationBtnAddNote)
                    btnAddNote.visibility = View.GONE
                    btnAddNote.isEnabled = false
                    animationBtnAddNote.setAnimationListener(object : Animation.AnimationListener {
                        override fun onAnimationRepeat(animation: Animation?) {}

                        override fun onAnimationEnd(animation: Animation?) {
                            btnCopy.startAnimation(animationBtnCopy)
                            btnCopy.visibility = View.GONE
                            btnCopy.isEnabled = false
                            animationBtnCopy?.setAnimationListener(object : Animation.AnimationListener {
                                override fun onAnimationRepeat(animation: Animation?) {}

                                override fun onAnimationEnd(animation: Animation?) {
                                    btnShare.startAnimation(animationBtnShare)
                                    btnShare.visibility = View.GONE
                                    btnShare.isEnabled = false
                                    animationBtnShare.setAnimationListener(object : Animation.AnimationListener {
                                        override fun onAnimationRepeat(animation: Animation?) {}

                                        override fun onAnimationEnd(animation: Animation?) {}

                                        override fun onAnimationStart(animation: Animation?) {
                                            btnSelectTranslation.startAnimation(animationBtnSelectTranslation)
                                            btnSelectTranslation.visibility = View.VISIBLE
                                            btnSelectTranslation.isEnabled = true
                                        }
                                    })
                                }

                                override fun onAnimationStart(animation: Animation?) {}
                            })
                        }

                        override fun onAnimationStart(animation: Animation?) {}
                    })
                }

                override fun onAnimationStart(animation: Animation?) {}
            })
        }
    }

    //Метод специально для класса BibleTextFragment. При нажатии на кнопку btnHome в BottomAppBar в случае если активирован режим множественного выбора текстов,
    //нужно скрыть панель множественного выбора текстов, но при этом не показывать кнопку выбора переводов Библии (как это реализовано в методе setShowHideMultiSelectionPanel),
    //потому при нажатии на кнопку btnHome идёт возврат в главный фрагмент SelectTestamentFragment где не кнопка выбора переводов не должна отображаться
    private fun closeMultiSelectionPanelIfButtonHomeClicked() {
        val animationBtnShare: Animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        val animationBtnCopy: Animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        val animationBtnAddNote: Animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        val animationBtnHighlight: Animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out)

        tvSelectedBibleVerse.visibility = View.GONE

        btnHighlight.startAnimation(animationBtnHighlight)
        btnHighlight.visibility = View.GONE
        btnHighlight.isEnabled = false
        animationBtnHighlight.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                btnAddNote.startAnimation(animationBtnAddNote)
                btnAddNote.visibility = View.GONE
                btnAddNote.isEnabled = false
                animationBtnAddNote.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {}

                    override fun onAnimationEnd(animation: Animation?) {
                        btnCopy.startAnimation(animationBtnCopy)
                        btnCopy.visibility = View.GONE
                        btnCopy.isEnabled = false
                        animationBtnCopy.setAnimationListener(object : Animation.AnimationListener {
                            override fun onAnimationRepeat(animation: Animation?) {}

                            override fun onAnimationEnd(animation: Animation?) {
                                btnShare.startAnimation(animationBtnShare)
                                btnShare.visibility = View.GONE
                                btnShare.isEnabled = false

                            }

                            override fun onAnimationStart(animation: Animation?) {}
                        })
                    }

                    override fun onAnimationStart(animation: Animation?) {}
                })
            }

            override fun onAnimationStart(animation: Animation?) {}
        })

    }

    override fun sendMultiSelectedTextsData(multiSelectedTextsList: ArrayList<BibleTextModel>) {
        this.multiSelectedTextsList = multiSelectedTextsList
        setSelectedVerses(multiSelectedTextsList)
    }

    //Метод, форматирующий текст нужным образом при выделении стихов.
    //Например, если выделены несколько текстов, идущих подряд друг за другом (например, 4,5,6,7),
    //то отрезок будет отформатирован с помощью дефиза, вот так: 4-7 , то есть с 4 по 7 стихи.
    //Если же выбираются тексты на следующий друг за другом, то они буду разделяться запятой.
    //К примеру, пользователь выделил 4 стих, а потом 7 стих, на выходе будет выглядеть вот так: 4,7, то есть отдельно 4 стих и отдельно 7 стих
    private fun setSelectedVerses(multiSelectedTextsList: ArrayList<BibleTextModel>) {
        var selectedVerses = ":" + multiSelectedTextsList[0].verse_number
        for ((index, bibleTextModel) in multiSelectedTextsList.withIndex()) {
            if (multiSelectedTextsList.size > 1 && index != 0) {
                selectedVerses = if ((bibleTextModel.verse_number - multiSelectedTextsList[index - 1].verse_number) == 1) {
                    if (index + 1 != multiSelectedTextsList.size && (multiSelectedTextsList[index + 1].verse_number - bibleTextModel.verse_number) == 1) {
                        continue
                    }
                    selectedVerses + "-" + bibleTextModel.verse_number
                } else {
                    selectedVerses + "," + bibleTextModel.verse_number
                }
            }
        }
        tvSelectedBibleVerse.visibility = View.VISIBLE
        tvSelectedBibleVerse.text = selectedVerses
    }

    override fun setShowHideArticlesInfoButton(articlesInfoBtnVisibility: Int) {
        //Этот фрагмент кода нужен, чтобы btnArticlesInfo не анимировался каждый раз при переходе между табами.
        //Если в одном табе установлена видимость такая же, как и в другом, то всё остаётся на своих местах и ничего не анимируется.
        //Анимирование происходит только в случае, когда значение btnSelectTranslationVisibility меняется
        val btnArticlesInfoVisibility = btnArticlesInfo.visibility
        if (btnArticlesInfoVisibility == articlesInfoBtnVisibility) {
            return
        }

        val animation: Animation?
        if (articlesInfoBtnVisibility == View.VISIBLE) {
            btnArticlesInfo.visibility = View.VISIBLE
            btnArticlesInfo.isEnabled = true
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        } else {
            btnArticlesInfo.visibility = View.GONE
            btnArticlesInfo.isEnabled = false //Нужно отключать кнопку, потому что в противном случае по какой-то причине кнопка продолжает нажиматься даже с видимостью GONE
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        }
        btnArticlesInfo.startAnimation(animation)
    }

    override fun setShowHideAddNoteButtonFAB(addNoteFABBtnVisibility: Int) {
        //Этот фрагмент кода нужен, чтобы btnAddNoteFAB не анимировался каждый раз при переходе между табами.
        //Если в одном табе установлена видимость такая же, как и в другом, то всё остаётся на своих местах и ничего не анимируется.
        //Анимирование происходит только в случае, когда значение btnSelectTranslationVisibility меняется
        val btnAddNoteFABVisibility = btnAddNoteFAB.visibility
        if (btnAddNoteFABVisibility == addNoteFABBtnVisibility) {
            return
        }

        val animation: Animation?
        if (addNoteFABBtnVisibility == View.VISIBLE) {
            btnAddNoteFAB.visibility = View.VISIBLE
            btnAddNoteFAB.isEnabled = true
            animation = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        } else {
            btnAddNoteFAB.visibility = View.GONE
            btnAddNoteFAB.isEnabled = false //Нужно отключать кнопку, потому что в противном случае по какой-то причине кнопка продолжает нажиматься даже с видимостью GONE
            animation = AnimationUtils.loadAnimation(this, R.anim.zoom_out)
        }
        btnAddNoteFAB.startAnimation(animation)
    }

    override fun setShowHideDeleteNoteButton(deleteNoteBtnVisibility: Int) {
        //Этот фрагмент кода нужен, чтобы btnDeleteNote не анимировался каждый раз при переходе между табами.
        //Если в одном табе установлена видимость такая же, как и в другом, то всё остаётся на своих местах и ничего не анимируется.
        //Анимирование происходит только в случае, когда значение btnSelectTranslationVisibility меняется
        val btnDeleteNoteVisibility = btnDeleteNote.visibility
        if (btnDeleteNoteVisibility == deleteNoteBtnVisibility) {
            return
        }

        val animation: Animation?
        if (deleteNoteBtnVisibility == View.VISIBLE) {
            btnDeleteNote.visibility = View.VISIBLE
            btnDeleteNote.isEnabled = true
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        } else {
            btnDeleteNote.visibility = View.GONE
            btnDeleteNote.isEnabled = false //Нужно отключать кнопку, потому что в противном случае по какой-то причине кнопка продолжает нажиматься даже с видимостью GONE
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        }
        btnDeleteNote.startAnimation(animation)
    }

    override fun setBtnSelectTranslationVisibility(visibility: Int) {
        //Этот фрагмент кода нужен, чтобы btnSelectTranslation не анимировался каждый раз при переходе между табами.
        //Если в одном табе установлена видимость такая же, как и в другом, то всё остаётся на своих местах и ничего не анимируется.
        //Анимирование происходит только в случае, когда значение btnSelectTranslationVisibility меняется
        val btnSelectTranslationVisibility = btnSelectTranslation.visibility
        if (btnSelectTranslationVisibility == visibility) {
            return
        }

        val animation: Animation?
        if (visibility == View.VISIBLE) {
            btnSelectTranslation.visibility = View.VISIBLE
            btnSelectTranslation.isEnabled = true
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        } else {
            btnSelectTranslation.visibility = View.GONE
            btnSelectTranslation.isEnabled = false //Нужно отключать кнопку, потому что в противном случае по какой-то причине кнопка продолжает нажиматься даже с видимостью GONE
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        }
        btnSelectTranslation.startAnimation(animation)
    }

    //Устанавливаем в текст кнопки выбора переводов аббревиатуру выбранного перевода
    override fun setBtnSelectTranslationText(selectedTranslation: String) {
        btnSelectTranslation.text = selectedTranslation
    }

    override fun setBtnSelectTranslationClickableState(clickableState: Boolean) {
        btnSelectTranslation.isClickable = clickableState
    }

    //Метод нужен, чтобы сменять текст названия приложения на текст выбранного текста Библии
    override fun setTvSelectedBibleTextVisibility(selectedTextVisibility: Int) {
        //Этот фрагмент кода нужен, чтобы tvSelectedBibleText и toolbarTitle не анимировались каждый раз при переходе между табами.
        //Если в одном табе установлена видимость такая же, как и в другом, то всё остаётся на своих местах и ничего не анимируется.
        //Анимирование происходит только в случае, когда значение selectedTextVisibility меняется
        val tvSelectedBibleTextVisibility = layTvSelectedBibleText.visibility
        if (tvSelectedBibleTextVisibility == selectedTextVisibility) {
            return
        }

        val animation: Animation?
        if (selectedTextVisibility == View.VISIBLE) {
            layTvSelectedBibleText.visibility = View.VISIBLE
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationStart(animation: Animation?) {
                    toolbarTitle.startAnimation(AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out))
                    toolbarTitle.visibility = View.GONE
                }
            })
            layTvSelectedBibleText.startAnimation(animation)
        } else {
            toolbarTitle.visibility = View.VISIBLE
            animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {}
                override fun onAnimationEnd(animation: Animation?) {}
                override fun onAnimationStart(animation: Animation?) {
                    layTvSelectedBibleText.startAnimation(AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out))
                    layTvSelectedBibleText.visibility = View.GONE
                }
            })
            toolbarTitle.startAnimation(animation)
        }
    }

    override fun setTvSelectedBibleText(selectedText: String, isBook: Boolean) {
        //Здесь проихводится проверка на то, название книги сюда приходит, или же номер выбранной главы. Если книга, то устанавливаем в нужный TextView, если номер, то соответственно
        if (isBook) tvSelectedBibleBook.text = selectedText
        else tvSelectedBibleChapter.text = selectedText
    }

    override fun setMyFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun setIsBackStackNotEmpty(isBackStackNotEmpty: Boolean) {
        this.isBackStackNotEmpty = isBackStackNotEmpty
    }

    override fun setIsTranslationDownloaded(isTranslationDownloaded: Boolean) {
        this.isTranslationDownloaded = isTranslationDownloaded
    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        when (requestCode) {
//            1000 -> {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//
//                }
//            }
//        }
//    }

//    private fun isExternalStorageReadOnly(): Boolean {
//        val extStorageState: String = Environment.getExternalStorageState()
//        return Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState
//    }
//
//    private fun isExternalStorageAvailable(): Boolean {
//        val extStorageState: String = Environment.getExternalStorageState()
//        return Environment.MEDIA_MOUNTED == extStorageState
//    }

    //Устанавливаем табы в ViewPager, создавая фрагменты каждого таба
    private fun setupViewPager(viewPager: ViewPager) {
        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(ArticlesRootFragment(), getString(R.string.title_articles))
        adapter.addFragment(BibleRootFragment(), getString(R.string.title_bible))
        adapter.addFragment(MoreRootFragment(), getString(R.string.title_more))
        viewPager.adapter = adapter
    }

    //Устанавливаем иконки и их цвет
    private fun setupTabIconsContent() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.icon != null) {
                    if (ThemeManager.theme == ThemeManager.Theme.BOOK) tab.icon?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(applicationContext, R.color.colorTabIndicatorBookTheme), PorterDuff.Mode.SRC_IN)
                    else tab.icon?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(applicationContext, R.color.colorIconTabLightTheme), PorterDuff.Mode.SRC_IN)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                if (tab.icon != null) {
                    if (ThemeManager.theme == ThemeManager.Theme.BOOK) tab.icon?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(applicationContext, R.color.colorUnselectedTabTextBookTheme), PorterDuff.Mode.SRC_IN)
                    else tab.icon?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(applicationContext, R.color.colorGray), PorterDuff.Mode.SRC_IN)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        setIconForTabs()
    }

    private fun setIconForTabs() {
        val tabIcons = intArrayOf(
                R.drawable.ic_aticles,
                R.drawable.ic_bible,
                R.drawable.ic_more)
        for (i in 0..3) {

            //Задаём видимость для иконок в табах, чтобы в случае горизонтальной ориентации иконки пропадали, потому что в горизонтальном режиме иконки могут неудобно заслонять текст
            val orientation: Int = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //Отключаем иконки, устаналивая на каждый таб иконкам значение null
                tabLayout.getTabAt(i)?.icon = null
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                tabLayout.getTabAt(i)?.setIcon(tabIcons[i])
            }

            if (ThemeManager.theme == ThemeManager.Theme.BOOK) tabLayout.getTabAt(i)?.icon?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this@MainActivity, R.color.colorUnselectedTabTextBookTheme), PorterDuff.Mode.SRC_IN)
            else tabLayout.getTabAt(i)?.icon?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this@MainActivity, R.color.colorGray), PorterDuff.Mode.SRC_IN)
            if (i == tabNumber)
                if (ThemeManager.theme == ThemeManager.Theme.BOOK) tabLayout.getTabAt(i)?.icon?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this@MainActivity, R.color.colorIconTabBookTheme), PorterDuff.Mode.SRC_IN)
                else tabLayout.getTabAt(i)?.icon?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this@MainActivity, R.color.colorIconTabLightTheme), PorterDuff.Mode.SRC_IN)
        }
    }

    //Обновляем цвета иконок и текста на табах, потому что если в тёмной и светлой теме цвета на текста и иконок одинаковые в TabLayout,
    //то в теме BOOK они отличаются, поэтому их нужно обновлять
    override fun updateTabIconAndTextColor() {
        setIconForTabs()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setIconForTabs() //Здесь этот метод вызывается для того, чтобы включить или отключить иконки в зависимости от выбранной ориентации экрана
    }

    override fun setViewPagerSwipeState(viewPagerSwipeState: Boolean) {
        viewPager.setSwipeState(viewPagerSwipeState)
    }

    override fun setTabNumber(tabNumber: Int) {
        this.tabNumber = tabNumber
    }

    override fun setBibleTextFragment(bibleTextFragment: BibleTextFragment) {
        this.bibleTextFragment = bibleTextFragment
    }

    override fun setBottomAppBarVisibility(visibility: Int) {
        appBar.visibility = visibility
        btnFAB.visibility = visibility
        if (visibility == View.VISIBLE) {
            appBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
            btnFAB.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in_slow))
        } else {
            appBar.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_out))
            btnFAB.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_out_slow))
        }
    }

    //Метод для установления видимости во время скрола списка
    override fun setFABVisibilityWhenScroll(fabVisibility: Boolean) {
        if (fabVisibility) {
            btnFAB.visibility = View.VISIBLE
            btnFAB.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_in_slow))
        } else {
            btnFAB.visibility = View.GONE
            btnFAB.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoom_out_slow))
        }
    }

    override fun isFabShown(): Boolean {
        return btnFAB.isShown
    }

    override fun onStop() {
        super.onStop()
        Utility.log("Activity onStop()")
        //Скорее всего этот код можно будет вовсе удалить, но пока оставляю на всякий случай.
        //Закрываем подключение к Базе данных Статей и удаляем БД. Делать это нужно именно в здесь в активити, потому что onStop вызывается именно при закрытии активити,
        // то есть так, как и надо. Если вызывать в onStop фрагмента, то он будет пересоздавать при каждом открытии и другого фрагмента, а это не подходящий вариант,
        // потому что таким образом трафик толком не экономится
//        ViewModelProvider(this).get(ArticlesViewModel::class.java).closeArticlesDB()
//        deleteDatabase(ARTICLES_DATA_BASE_NAME)


    }

    override fun onDestroy() {
        super.onDestroy()
        Utility.log("Activity onDestroy()")
        App.articlesData = null //Очищаем переменную, хранящую данные статьей при закрытии приложения
    }

    override fun setTheme(theme: ThemeManager.Theme, animate: Boolean) {
        ThemeManager.theme = theme

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            when (theme) {
                ThemeManager.Theme.LIGHT -> window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.colorStatusBarLightTheme)
                ThemeManager.Theme.DARK -> window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.colorStatusBarDarkTheme)
                ThemeManager.Theme.BOOK -> window.statusBarColor = ContextCompat.getColor(applicationContext, R.color.colorStatusBarBookTheme)
            }
        }

        if (!animate) return //Код, отключающий анимацию

        val animation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        container.startAnimation(animation)
    }

    override fun onBackPressed() {
        //КОД НЕ МЕНЯТЬ!
        /*Получаем имя Фрагмента, из которого мы получили FragmentManager. Если это FragmentManager из BibleRootFragment, то тогда конкретно для этого раздела мы устанавливаем такие условия:
        Если перевод не скачан, то при нажатии кнопки назад приложение закрывается, если же перевод есть, то при нажатии назад будет открыт предыдущий фрагмент.
        В противном случае, если не проверять FragmentManager на имя, то условие будет установлено для всех разделов, а это значит, что в любом из разделов при попытке вернуться назад,
        приложение будет закрываться. Нам же нужно, чтобы это было реализовно только в разделе Библии и в случае отсутствия переводов. Чтобы если перевода нету,
        юзер не мог открыть раздел Библии*/
        if (saveLoadData.loadBoolean(BibleTranslationsRVAdapter.isTranslationDownloading)) {
            Toast.makeText(this, getString(R.string.toast_please_wait), Toast.LENGTH_SHORT).show()
            return
        }

        val myFragmentManagerName = myFragmentManager.toString()
        //Вызываем метод contains, потому что помимо имени фрагмента, из которого был взят FragmentManager, там ещё содержатся другая информация, которая не нужна
        if (myFragmentManagerName.contains("BibleRootFragment")) {
            //Если никакой перевод не скачан или скаченный перевод был удалён, то при нажатии кнопки назад в фрагмент BibleTranslationFragment приложение будет закрыто
            if (!isTranslationDownloaded || saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO) != null
                    && saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO)!!.isNotEmpty()
                    && !Utility.isSelectedTranslationDownloaded(this, Gson().fromJson(saveLoadData.loadString(TRANSLATION_DB_FILE_JSON_INFO), BibleTranslationModel::class.java))) {
                super.onBackPressed()
                return
            }
        }

        Utility.hideKeyboard(this) //Вызываем этот метод, чтобы при нажатии стрелки назад закрывать клавиатуру, если она открыта

        //Устанавливаем на переменную isBackButtonClicked значение true, когда кнопка "Назад" была нажата. Это значение нужно для BibleTextFragment.
        //А по скольку onBackPressed срабатывает раньше, чем onPause, то после того, как пользователь нажал кнопку "Назад" и сработал onBackPressed,
        //то в onPause, при закрытии BibleTextFragment, происходит проверка и если определяется, что метод onPause срабатывает после нажатия кнопки "Назад",
        //а не после закрытия приложения через диспетчер задач, то данные о сохранённом скролле очищаются в onPause и при следующем открытии,
        //приложение будет открыто в начальном экране SelectTestamentFragment
        isBackButtonClicked = true

        //Если открыт таб "Библия" и включён режим множественного выбора, то при нажатии кнопки "Назад" сначала а выключается режим множественного выбора,
        //и только после этого можно как обычно вернуться на предыдущий фрагмент тем же нажатием кнопки "Назад"
        if (myFragmentManagerName.contains("BibleRootFragment") && isMultiSelectionEnabled) {
            disableMultiSelection()
            return
        }

        if (isBackStackNotEmpty) myFragmentManager.popBackStack()
        else super.onBackPressed()
    }

    override fun changeLanguage(languageCode: String) {
        saveLoadData.saveString(AppLanguageFragment.APP_LANGUAGE_CODE_KEY, languageCode)//Сохраняем выбранный язык, что при повторном запуске приложения загружать его.
        //Сохраняем мы его именно здесь, чтобы язык сохранялся только тогда, когда человек выберет его. Если же человек не выбрал его, то автоматически будет загружаться нужный язык.
        //К примеру, в приложении предусмотрено 3 языка (en, uk, ru), а язык телефона выбран немецкий. Человек заходит в приложение и язык приложения включиться дефолтный английский,
        //потому что немецкий в приложении не предусмотрен. Затем пользователь меняет язык телефона на русский и когда он снова включит приложение,
        //то прописанный алгоритм в методе главного активити onBackPressed вычислит, что данный(русский) язык предусмотрен в приложении и откроет приложение уже на русском языке.
        //Если бы выбранный язык сохранялся в методе setLocale(как это было раньше), то при первом запуске приложения за неимением немецкого языка, по дефолту был бы выбран и сразу сохранён английский язык.
        //И даже если бы пользователь потом переключил бы язык телефона на русский и запустил бы приложение, то приложение осталось бы на английском языке, потому что английский язык сохранился бы при первом запуске.
        setLocale(languageCode)
        recreate()
//        startActivity(intent)
//        finish()
    }

    private fun setLocale(languageCode: String) {
//        saveLoadData.saveString(AppLanguageFragment.APP_LANGUAGE_CODE_FOR_LANGUAGE_LIST_KEY, languageCode)
        saveLoadData.saveString(AppLanguageFragment.APP_LANGUAGE_CODE_KEY, languageCode)

        val activityRes = resources
        val activityConf = activityRes.configuration
        val newLocale = Locale(languageCode)
        activityConf.setLocale(newLocale)
        activityRes.updateConfiguration(activityConf, activityRes.displayMetrics)

        val applicationRes = applicationContext.resources
        val applicationConf = applicationRes.configuration
        applicationConf.setLocale(newLocale)
        applicationRes.updateConfiguration(applicationConf, applicationRes.displayMetrics)
    }

    //Метод, в котором загружается выбранный язык приложения. Если язык не выбран, то, если язык телефона совпадает с имеющимся языком из предоставленных в приложении,
    //значит устанавливается он. Если же язык телефона не совпадает ни с каким из предоставленных в приложении, то по дефолту ставится английский
    private fun loadLocale() {
        // Сделать проверку, если язык телефона совпадает с существующим переводом приложения в приложении, то установить его, если такого перевода нет в приложении, то устанавливать дефолтный(английский)
        val languageList = arrayOf("en", "ru", "uk")

        val languageCodeOfSelectedLanguage = saveLoadData.loadString(AppLanguageFragment.APP_LANGUAGE_CODE_KEY) //Код языка, который выбраз пользоваетль при переключении языка приложения
        if (languageCodeOfSelectedLanguage == null || languageCodeOfSelectedLanguage.isEmpty()) {
            val languageCode = Locale.getDefault().language
            var isLanguageInList = true
            for (item in languageList) {
                if (languageCode == item) {
                    setLocale(languageCode)
                    isLanguageInList = true
                    break
                } else {
                    isLanguageInList = false
                }
            }

            //Если язык не находится в списке предусмотренных языков в приложении, то ставится язык по умолчанию - Английский
            if (!isLanguageInList) {
                setLocale("en")
            }
        } else
            setLocale(languageCodeOfSelectedLanguage)
    }

    override fun dismissDialog() {
        //Не смотря на жёлтое выделение оператора if, этот код должен быть именно таким.
        //Видимо, система предполагает, что если данный метод вызывается из объекта диалога, то объект диалога не может иметь значение null.
        //Но дело в том, что этот один и тот же метод dismissDialog может быть вызван только в одном из созданных открытых диалогов, в то время как другие диалоги не буду созданы.
        //И если для одного объекта диалога сработает метод dismiss(), то для другого нет, потому как он даже не будет создан.
        //Этот код реализован таким образом для того, чтобы не писать для каждого диалога свой обработчик.
        articlesInfoDialog?.dismiss()

        addNoteDialog?.dismiss()
        //Отключаем режим множественного выбора после добавления заметки
        if (isMultiSelectionEnabled) disableMultiSelection()
    }
}
