package com.salesrep.app.ui.home.adapter

import android.os.Parcelable
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.salesrep.app.ui.home.HomeRouteFragment

class ViewPagerAdapter(
    private  var fm: FragmentManager,
    private var fragList: ArrayList<HomeRouteFragment>,
    private var tabList: ArrayList<String>
) : FragmentStatePagerAdapter(
    fm,
    BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
) {

    private val mFragmentTags: MutableMap<Int, Fragment> = mutableMapOf()

    override fun getCount(): Int {
        return fragList.size
    }


    override fun getItem(position: Int): Fragment {
        return fragList[position]
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabList[position]
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val obj= super.instantiateItem(container, position)

        if (obj is Fragment){
            val tag= obj.tag
            mFragmentTags.put(position,obj)
        }
        return obj
    }

    fun getFragment( position: Int) : Fragment ? {
        var fragment:  Fragment? =  mFragmentTags.get(position)
//        if (tag != null ) {
//            fragment = fm.findFragmentByTag(tag)
//        }
        return fragment!!
    }

//
//   override fun instantiateItem(container: ViewGroup?, position: Int): Any {
//        val `object` = super.instantiateItem(container!!, position)
//        if (`object` is Fragment) {
//            val tag = `object`.tag
//            mFragmentTags.put(position, tag)
//        }
//        return `object`
//    }
    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        try {
            super.restoreState(state, loader)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}
