package com.android.bible.knowbible.mvvm.view.fragment.articles_section

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.android.bible.knowbible.R

class ArticlesRootFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_container_articles, container, false)

        childFragmentManager.let {
            val articlesFragment = ArticlesFragment()
            articlesFragment.setRootFragmentManager(childFragmentManager)

            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.replace(R.id.fragment_container_articles, articlesFragment)
            transaction.commit()
        }
        return myView
    }
}