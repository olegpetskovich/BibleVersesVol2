package com.android.bible.knowbible.mvvm.view.fragment.bible_section

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.android.bible.knowbible.R

class BibleRootFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_container_bible, container, false)

        childFragmentManager.let {
            val fragment = SelectTestamentFragment()
            fragment.setRootFragmentManager(childFragmentManager)
            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.replace(R.id.fragment_container_bible, fragment)
            transaction.commit()
        }
        return myView
    }
}