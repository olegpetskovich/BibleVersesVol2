package com.android.bible.knowbible.mvvm.view.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.android.bible.knowbible.R
import com.android.bible.knowbible.data.local.BibleTextInfoDBHelper
import com.android.bible.knowbible.mvvm.model.BibleTextModel
import com.android.bible.knowbible.mvvm.model.DataToRestoreModel
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.fragment.bible_section.BibleTextFragment.Companion.DATA_TO_RESTORE
import com.android.bible.knowbible.utility.SaveLoadData
import com.android.bible.knowbible.utility.Utility
import com.google.gson.Gson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ViewPager2Adapter(private val context: Context, private val chaptersTextList: ArrayList<ArrayList<BibleTextModel>>, private val myFragmentManager: FragmentManager) : RecyclerView.Adapter<ViewPager2Adapter.PagerVH>() {
    lateinit var dataToRestoreData: DataToRestoreModel //Поле для определения и сохранения скролла в Ресайклере

    private val saveLoadData = SaveLoadData(context)

    private lateinit var rvAdapter: BibleTextRVAdapter

    private var mapRV = hashMapOf<Int, RecyclerView>()

    interface IFragmentCommunication {
        fun saveScrollPosition(bookNumber: Int, chapterNumber: Int, scrollPosition: Int)
    }

    private lateinit var fragmentCommunication: IFragmentCommunication
    fun setIFragmentCommunicationListener(fragmentCommunication: IFragmentCommunication) {
        this.fragmentCommunication = fragmentCommunication
    }

    private lateinit var themeChanger: IThemeChanger
    fun setRecyclerViewThemeChangerListener(themeChanger: IThemeChanger) {
        this.themeChanger = themeChanger
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager_two, parent, false)
        return PagerVH(view)
    }

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH =
//            PagerVH(LayoutInflater.from(parent.context).inflate(R.layout.item_view_pager_two, parent, false))

    override fun getItemCount(): Int = chaptersTextList.size

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: PagerVH, position: Int) {
        if (!mapRV.containsKey(position))
            mapRV[position] = holder.recyclerView

        val bibleTextInfoDBHelper = BibleTextInfoDBHelper.getInstance(context)!!
        bibleTextInfoDBHelper
//                .loadBibleTextInfo(dataToRestoreData.bookNumber, models[0].chapter) //Просто берём данные из самого первого элемента в коллекции. Неважно из какого элемента брать, главное, что каждый из них хранит значение book_number и chapter
                .loadBibleTextInfo(dataToRestoreData.bookNumber) //Просто берём данные из самого первого элемента в коллекции. Неважно из какого элемента брать, главное, что каждый из них хранит значение book_number и chapter
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { textInfoList ->
                    val textList = chaptersTextList[position]

                    if (textInfoList.size != 0) {
                        for (textInfoItem in textInfoList) {
                            for (item in textList) {
                                if (textInfoItem.bookNumber == item.book_number && textInfoItem.chapterNumber == item.chapter && textInfoItem.verseNumber == item.verse) {
                                    item.id = textInfoItem.id //id этого значения в БД. Это поле нужно для того, чтобы удалять данные из БД.
                                    item.textColorHex = textInfoItem.textColorHex
                                    item.isTextBold = textInfoItem.isTextBold
                                    item.isTextUnderline = textInfoItem.isTextUnderline
                                    item.isTextToDailyVerse = textInfoItem.isTextToDailyVerse
                                }
                            }
                        }
                    }

                    Utility.log("BibleTextFragment: onBindViewHolder")
                    rvAdapter = BibleTextRVAdapter(context, textList, myFragmentManager)
                    rvAdapter.setRecyclerViewThemeChangerListener(themeChanger) //Для RecyclerView тему нужно обновлять отдельно от смены темы для всего фрагмента. Если менять тему только для всего фрагмента, не меняя при этом тему для списка, то в списке тема не поменяется.
                    val rvItem = holder.recyclerView
                    rvItem.adapter = rvAdapter

                    rvItem.layoutManager = LinearLayoutManager(context)
                    rvItem.itemAnimator = DefaultItemAnimator()

                    rvItem.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                            super.onScrollStateChanged(recyclerView, newState)
                            fragmentCommunication.saveScrollPosition(dataToRestoreData.bookNumber, dataToRestoreData.chapterNumber, (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition())
                        }
                    })
                }
    }

    fun getScrollPosition(posPage: Int): Int {
        return if (mapRV[posPage]?.layoutManager != null) {
            val linearLayoutManager = mapRV[posPage]?.layoutManager as LinearLayoutManager
            linearLayoutManager.findFirstVisibleItemPosition()
        } else {
            0
        }
    }

    //Ничего не менять
    fun scrollTo(posPage: Int, smoothScroll: Boolean) {
        //Тут выставляется позиция скролла для RecyclerView, которая была сохранена
        //__________________________________________________________________________________________
        val jsonScrollData = saveLoadData.loadString(DATA_TO_RESTORE)
        if (jsonScrollData != null && jsonScrollData.isNotEmpty()) {
            val dataToRestoreModel: DataToRestoreModel = Gson().fromJson(jsonScrollData, DataToRestoreModel::class.java)
            Utility.log("dataToRestoreModel.bookNumber: " + dataToRestoreModel.bookNumber + ", dataToRestoreData.bookNumber: " + dataToRestoreData.bookNumber)
            Utility.log("dataToRestoreModel.chapterNumber: " + (dataToRestoreModel.chapterNumber - 1) + ", dataToRestoreData.chapterNumber: " + dataToRestoreData.chapterNumber)
            Utility.log("dataToRestoreModel.scrollPosition: " + dataToRestoreModel.scrollPosition)

            if (dataToRestoreModel.bookNumber == dataToRestoreData.bookNumber //Проверяем, совпадает ли выбранная книга с сохранённой ранее
                    && dataToRestoreModel.chapterNumber - 1 /*Ничего не менять, так надо*/ == dataToRestoreData.chapterNumber //Проверяем, совпадает ли выбранная глава с сохранённой ранее
                    && dataToRestoreModel.scrollPosition != -1 //Проверяем, сохранена ли какая-то позиция ранее или же значение равно -1 (то есть ничего не было сохранено)
            ) {
                val linearLayoutManager = mapRV[posPage]!!.layoutManager as LinearLayoutManager
                //Плавный скролл
                if (smoothScroll) {
                    val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {
                        override fun getVerticalSnapPreference(): Int {
                            return SNAP_TO_START
                        }
                    }
                    smoothScroller.targetPosition = dataToRestoreModel.scrollPosition
                    linearLayoutManager.startSmoothScroll(smoothScroller)
                } else linearLayoutManager.scrollToPositionWithOffset(dataToRestoreModel.scrollPosition, 0) //Резкий скролл
            }
        }
        //__________________________________________________________________________________________
    }

    inner class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
    }
}

