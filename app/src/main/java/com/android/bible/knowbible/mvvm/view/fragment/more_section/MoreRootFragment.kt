package com.apps.oleg.bibleverses.mvvm.view.fragment.more_section

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.android.bible.knowbible.R

class MoreRootFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val myView: View = inflater.inflate(R.layout.fragment_container_more, container, false)

        childFragmentManager.let {
            val moreFragment = MoreFragment()
            moreFragment.setRootFragmentManager(childFragmentManager)

            val transaction: FragmentTransaction = it.beginTransaction()
            transaction.replace(R.id.fragment_container_more, moreFragment)
            transaction.commit()
        }
        return myView
    }
}