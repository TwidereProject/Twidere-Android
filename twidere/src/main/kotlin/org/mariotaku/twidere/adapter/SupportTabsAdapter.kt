/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback
import org.mariotaku.twidere.model.SupportTabSpec
import org.mariotaku.twidere.util.CustomTabUtils.getTabIconDrawable
import org.mariotaku.twidere.util.Utils.announceForAccessibilityCompat
import org.mariotaku.twidere.view.iface.PagerIndicator
import org.mariotaku.twidere.view.iface.PagerIndicator.TabListener
import org.mariotaku.twidere.view.iface.PagerIndicator.TabProvider
import java.util.*

class SupportTabsAdapter @JvmOverloads constructor(
        private val context: Context,
        fm: FragmentManager,
        private val indicator: PagerIndicator? = null,
        private val columns: Int = 1
) : SupportFixedFragmentStatePagerAdapter(fm), TabProvider, TabListener {

    private val mTabs = ArrayList<SupportTabSpec>()

    init {
        clear()
    }

    fun addTab(cls: Class<out Fragment>, args: Bundle?, name: String,
               icon: Int?, position: Int, tag: String?) {
        addTab(SupportTabSpec(name = name, icon = icon, cls = cls, args = args,
                position = position, tag = tag))
    }

    fun addTab(cls: Class<out Fragment>, args: Bundle?, name: String,
               icon: Int?, type: String, position: Int, tag: String?) {
        addTab(SupportTabSpec(name, icon, type, cls, args, position, tag))
    }

    fun addTab(spec: SupportTabSpec) {
        mTabs.add(spec)
        notifyDataSetChanged()
    }

    fun addTabs(specs: Collection<SupportTabSpec>) {
        mTabs.addAll(specs)
        notifyDataSetChanged()
    }

    fun clear() {
        mTabs.clear()
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return mTabs.size
    }

    override fun getItemPosition(obj: Any?): Int {
        if (obj !is Fragment) return PagerAdapter.POSITION_NONE
        val args = obj.arguments ?: return PagerAdapter.POSITION_NONE
        return args.getInt(EXTRA_ADAPTER_POSITION, PagerAdapter.POSITION_NONE)
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        indicator?.notifyDataSetChanged()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return mTabs[position].name
    }

    override fun getPageWidth(position: Int): Float {
        return 1.0f / columns
    }

    override fun getItem(position: Int): Fragment {
        val fragment = Fragment.instantiate(context, mTabs[position].cls.name)
        fragment.arguments = getPageArguments(mTabs[position], position)
        return fragment
    }

    override fun startUpdate(container: ViewGroup) {
        super.startUpdate(container)
    }

    override fun getPageIcon(position: Int): Drawable {
        return getTabIconDrawable(context, mTabs[position].icon)
    }

    fun getTab(position: Int): SupportTabSpec {
        return mTabs[position]
    }

    val tabs: List<SupportTabSpec>
        get() = mTabs

    override fun onPageReselected(position: Int) {
        if (context !is SupportFragmentCallback) return
        val f = context.currentVisibleFragment
        if (f is RefreshScrollTopInterface) {
            f.scrollToStart()
        }
    }

    override fun onPageSelected(position: Int) {
        if (indicator == null || position < 0 || position >= count) return
        announceForAccessibilityCompat(context, indicator as View?, getPageTitle(position), javaClass)
    }

    override fun onTabLongClick(position: Int): Boolean {
        if (context !is SupportFragmentCallback) return false
        if (context.triggerRefresh(position)) return true
        val f = context.currentVisibleFragment
        if (f is RefreshScrollTopInterface)
            return f.triggerRefresh()
        return false
    }

    fun setTabLabel(position: Int, label: CharSequence) {
        for (spec in mTabs) {
            if (position == spec.position)
                spec.name = label
        }
        notifyDataSetChanged()
    }

    private fun getPageArguments(spec: SupportTabSpec, position: Int): Bundle {
        val args = spec.args ?: Bundle()
        args.putInt(EXTRA_ADAPTER_POSITION, position)
        return args
    }

    companion object {

        private val EXTRA_ADAPTER_POSITION = "adapter_position"
    }
}
