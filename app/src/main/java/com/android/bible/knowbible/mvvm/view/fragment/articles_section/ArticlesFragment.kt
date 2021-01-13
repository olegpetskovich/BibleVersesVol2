package com.android.bible.knowbible.mvvm.view.fragment.articles_section

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.bible.knowbible.App
import com.android.bible.knowbible.R
import com.android.bible.knowbible.mvvm.model.ArticleModel
import com.android.bible.knowbible.mvvm.view.adapter.ArticlesRVAdapter
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IActivityCommunicationListener
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IChangeFragment
import com.android.bible.knowbible.mvvm.view.callback_interfaces.IThemeChanger
import com.android.bible.knowbible.mvvm.view.theme_editor.ThemeManager
import com.android.bible.knowbible.mvvm.viewmodel.ArticlesViewModel
import com.android.bible.knowbible.utility.Utility
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.grpc.Status
import kotlinx.android.synthetic.main.fragment_articles.*
import java.net.ConnectException
import javax.net.ssl.SSLHandshakeException


class ArticlesFragment : Fragment(), IThemeChanger, IChangeFragment {
    private lateinit var listener: IActivityCommunicationListener

    private lateinit var myFragmentManager: FragmentManager

    private val firestoreDB = FirebaseFirestore.getInstance() //Cloud Firestore DB field
    private val dataRefArticles = firestoreDB.collection("articles")

    private lateinit var articlesViewModel: ArticlesViewModel

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utility.log("onCreate()")
        retainInstance = true //Без этого кода не будет срабатывать поворот экрана
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView = inflater.inflate(R.layout.fragment_articles, container, false)
        Utility.log("onCreateView()")
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такой решение
        recyclerView = myView.findViewById(R.id.recyclerView)

//        loadData(context!!) //Загружать данные в onResume, потому что загружая в onCreate выдаёт ошибку

        myFragmentManager.let {
//            val transaction: FragmentTransaction = it.beginTransaction()
//            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
//
//
//            transaction.replace(R.id.fragment_container_more, )
//            transaction.addToBackStack(null)
//            transaction.commit()
        }
        return myView
    }

    //В данном методем реализован алгоритм кеширования, в котором данные сохраняются при первой их загрузке, а потом загружаются из локальной БД. При закрытии приложения локальная БД удаляется.
    private fun loadData(context: Context) {
        //Выставляем количество столбиков в RecyclerView
        val orientation = context.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) recyclerView.layoutManager = GridLayoutManager(context, 2)
        else recyclerView.layoutManager = GridLayoutManager(context, 3)

        //Этот вариант может понадобиться в случае, если будет настройка "Сохранить статьи локально"
        //Получаем данные. Если данные ещё не закешированы, то получаем их с сети, если закешировано, то с локальной БД
//        if (!context.getDatabasePath(ARTICLES_DATA_BASE_NAME).exists()) {

        if (App.articlesData == null) {
            if (Utility.isNetworkAvailable(context)!!) { //Если интернет есть - грузим данные, если интернета нет, то работа метода прекращается вызовом return
                dataRefArticles
                        .get()
                        .addOnSuccessListener(OnSuccessListener<QuerySnapshot> { data ->
                            Utility.log("Internet request")
                            val articlesData = data.toObjects(ArticleModel::class.java) //Здесь ради удобства использования используется модел на Java

                            articlesData.forEachIndexed { index, articleModel ->
                                articlesViewModel
                                        .loadArticlePicture(articleModel.image)
                                        .observe(this, Observer { imageBitmap ->
                                            progressBar.visibility = View.GONE
                                            layNoInternet.visibility = View.GONE
                                            articleModel.imageBitmap = imageBitmap //Устаналиваем картинку в виде Bitmap, чтобы сразу отобразить в списке и сохранить в бд

                                            if (index == articlesData.size - 1) {
                                                val articlesRVAdapter = ArticlesRVAdapter(context, articlesData as ArrayList<ArticleModel>)
                                                articlesRVAdapter.setRecyclerViewThemeChangerListener(this)
                                                articlesRVAdapter.setFragmentChangerListener(this)
                                                recyclerView.adapter = articlesRVAdapter

                                                App.articlesData = articlesData //Сохраняем данные в статическое поле, чтобы закешировать данные на время работы приложения. Это сэкономит количество делаемых запросов для получения данных. Один раз получили данные и они используется на протяжении всего времени, когда приложение включено.

                                                //Этот вариант может понадобиться в случае, если будет настройка "Сохранить статьи локально"
//                                                articlesViewModel.createArticlesDB()
//                                                articlesViewModel.setArticles(articlesData) //Сохраняем данные в локальную БД, чтобы закешировать данные. Это сэкономит количество делаемых запросов для получения данных. Один раз получили данные и они используется на протяжении всего времени, когда приложение включено.
                                            }
                                        })
                            }

                        })
                        .addOnFailureListener(OnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            layNoInternet.visibility = View.GONE
                            Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show()
                            Utility.log(e.toString())
                        })
            } else {
                progressBar.visibility = View.GONE
                layNoInternet.visibility = View.VISIBLE
                Toast.makeText(context, context.getString(R.string.toast_no_internet_connection), Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            Utility.log("Local request")

            progressBar.visibility = View.GONE
            val articlesRVAdapter = ArticlesRVAdapter(context, App.articlesData!!)
            articlesRVAdapter.setRecyclerViewThemeChangerListener(this)
            articlesRVAdapter.setFragmentChangerListener(this)

            recyclerView.adapter = articlesRVAdapter


//            articlesViewModel = ViewModelProvider(this).get(ArticlesViewModel::class.java)
//            articlesViewModel
//                    .getArticles()
//                    .observe(viewLifecycleOwner, Observer { articlesData ->
//                        progressBar.visibility = View.GONE
//                        val articlesRVAdapter = ArticlesRVAdapter(context, articlesData)
//                        articlesRVAdapter.setRecyclerViewThemeChangerListener(this)
//                        articlesRVAdapter.setFragmentChangerListener(this)
//
//                        recyclerView.adapter = articlesRVAdapter
//                    })
        }
    }

    private fun isSslHandshakeError(status: Status): Boolean {
        val code: Status.Code = status.code
        val t: Throwable? = status.cause
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && code == Status.Code.UNAVAILABLE
                && (t is SSLHandshakeException
                || t is ConnectException && t.message!!.contains("EHOSTUNREACH")))
    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        //Устанавливаем нужный layout на отображаемую ориентацию экрана. Делать это по той причине, что обновление активити отключено при повороте экрана,
        //поэтому в случае необходимсти обновления xml, это нужно делать самому
        myFragmentManager.let {
            val articlesFragment = ArticlesFragment()
            articlesFragment.setRootFragmentManager(it)
            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.replace(R.id.fragment_container_articles, articlesFragment)
            transaction.commit()
        }
    }

    fun setRootFragmentManager(myFragmentManager: FragmentManager) {
        this.myFragmentManager = myFragmentManager
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is IActivityCommunicationListener) listener = context
        else throw RuntimeException("$context must implement IActivityCommunicationListener")
        articlesViewModel = ViewModelProvider(this).get(ArticlesViewModel::class.java)
    }

    override fun onStart() {
        super.onStart()
        loadData(context!!) //Загружать данные в onStart, потому что загружая в onCreate выдаёт ошибку | ЗДЕСЬ ЗАГРУЖАЕМ ДАННЫЕ В onStart(), А ОБНОВЛЯЕМ СПИСОК В onResume(), ЧТОБЫ ПОЛУЧАТЬ ДАННЫЕ ТОЛЬКО ОДНАЖДЫ, А ОБНОВЛЯТЬ СПИСОК ДЛЯ ОБНОВЛЕНИЯ ТЕМЫ АЙТЕМОВ
        Utility.log("onStart()")
    }

    override fun onResume() {
        super.onResume()
        Utility.log("onResume()")

        recyclerView.adapter?.notifyDataSetChanged()

        listener.setShowHideArticlesInfoButton(View.VISIBLE) //Устанавливаем видимость кнопки btnArticlesInfo

        listener.setTabNumber(0)
        listener.setMyFragmentManager(myFragmentManager)
        listener.setIsBackStackNotEmpty(false)

        listener.setBtnSelectTranslationVisibility(View.GONE)

        listener.setShowHideToolbarBackButton(View.GONE)

        listener.setTvSelectedBibleTextVisibility(View.GONE)
    }

    override fun onPause() {
        super.onPause()
        Utility.log("onPause()")
        listener.setShowHideArticlesInfoButton(View.GONE) //Устанавливаем видимость кнопки btnArticlesInfo
    }

    override fun changeItemTheme() {
        listener.setTheme(ThemeManager.theme, false) //Если не устанавливать тему каждый раз при открытии фрагмента, то по какой-то причине внешний вид View не обновляется, поэтому на данный момент только такое решение
    }

    override fun changeFragment(fragment: Fragment) {
        myFragmentManager.let {
            val myFragment = fragment as ArticleFragment
            myFragment.setRootFragmentManager(myFragmentManager)

            val transaction: FragmentTransaction = it.beginTransaction()
//            transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
//            transaction.setCustomAnimations( R.animator.slide_up, 0, 0, R.animator.slide_down)
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            transaction.addToBackStack(null)
            transaction.replace(R.id.fragment_container_articles, myFragment)
            transaction.commit()
        }
    }

    override fun onStop() {
        super.onStop()
        Utility.log("onStop()")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Utility.log("onDestroyView()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Utility.log("onDestroy()")
    }
}
