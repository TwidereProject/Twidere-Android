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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.PagerAdapter
import android.view.View
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback
import org.mariotaku.twidere.model.SupportTabSpec
import org.mariotaku.twidere.model.tab.DrawableHolder
import org.mariotaku.twidere.util.CustomTabUtils.getTabIconDrawable
import org.mariotaku.twidere.util.Utils.announceForAccessibilityCompat
import org.mariotaku.twidere.view.iface.PagerIndicator
import org.mariotaku.twidere.view.iface.PagerIndicator.TabListener
import org.mariotaku.twidere.view.iface.PagerIndicator.TabProvider
import java.util.*

class SupportTabsAdapter(
        private val context: Context,
        fm: FragmentManager,
        private val indicator: PagerIndicator? = null
) : SupportFixedFragmentStatePagerAdapter(fm), TabProvider, TabListener {

    var hasMultipleColumns: Boolean = false
    var preferredColumnWidth: Float = 0f

    var tabs = ArrayList<SupportTabSpec>()
        set(value) {
            field = tabs
            notifyDataSetChanged()
        }

    init {
        clear()
    }

    override fun getCount(): Int {
        return this.tabs.size
    }

    override fun getItemPosition(obj: Any): Int {
        if (obj !is Fragment) return PagerAdapter.POSITION_NONE
        val args = obj.arguments ?: return PagerAdapter.POSITION_NONE
        return args.getInt(EXTRA_ADAPTER_POSITION, PagerAdapter.POSITION_NONE)
    }

    override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
        indicator?.notifyDataSetChanged()
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return this.tabs[position].name
    }

    override fun getPageWidth(position: Int): Float {
        val columnCount = count
        if (columnCount == 0) return 1f
        if (hasMultipleColumns && preferredColumnWidth > 0) {
            val resources = context.resources
            val screenWidth = resources.displayMetrics.widthPixels
            val pageWidth = preferredColumnWidth / screenWidth
            if (columnCount * preferredColumnWidth < screenWidth) {
                return 1f / columnCount
            }
            return pageWidth
        }
        return 1f
    }

    override fun getItem(position: Int): Fragment {
        val fragment = Fragment.instantiate(context, this.tabs[position].cls.name)
        fragment.arguments = getPageArguments(this.tabs[position], position)
        return fragment
    }

    override fun getPageIcon(position: Int): Drawable {
        return getTabIconDrawable(context, this.tabs[position].icon)
    }

    override fun onPageReselected(position: Int) {
        if (context !is SupportFragmentCallback) return
        val f = context.currentVisibleFragment
        if (f is RefreshScrollTopInterface) {
            f.scrollToStart()
        }
    }

    override fun onPageSelected(position: Int) {
        if (position < 0 || position >= count) return
        val text = getPageTitle(position) ?: return
        val view = indicator as? View ?: return
        announceForAccessibilityCompat(context, view, text, javaClass)
    }

    override fun onTabLongClick(position: Int): Boolean {
        if (context !is SupportFragmentCallback) return false
        if (context.triggerRefresh(position)) return true
        val f = context.currentVisibleFragment
        if (f is RefreshScrollTopInterface)
            return f.triggerRefresh()
        return false
    }

    fun add(cls: Class<out Fragment>, args: Bundle? = null, name: String,
            icon: DrawableHolder? = null, type: String? = null, position: Int = 0, tag: String? = null) {
        add(SupportTabSpec(name = name, icon = icon, cls = cls, args = args,
                position = position, type = type, tag = tag))
    }

    fun add(spec: SupportTabSpec) {
        this.tabs.add(spec)
        notifyDataSetChanged()
    }

    fun addAll(specs: Collection<SupportTabSpec>) {
        this.tabs.addAll(specs)
        notifyDataSetChanged()
    }

    fun get(position: Int): SupportTabSpec {
        return this.tabs[position]
    }

    fun clear() {
        this.tabs.clear()
        notifyDataSetChanged()
    }

    fun setLabel(position: Int, label: CharSequence) {
        this.tabs.filter { position == it.position }.forEach { it.name = label }
        notifyDataSetChanged()
    }

    private fun getPageArguments(spec: SupportTabSpec, position: Int): Bundle {
        val args = spec.args ?: Bundle()
        args.putInt(EXTRA_ADAPTER_POSITION, position)
        return args
    }

    companion object {

        private const val EXTRA_ADAPTER_POSITION = "adapter_position"
    }
}
