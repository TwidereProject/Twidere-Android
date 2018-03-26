/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.view.pagerindicator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v7.widget.FixedLinearLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.SparseIntArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import org.mariotaku.chameleon.Chameleon
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.chameleon.ChameleonView
import org.mariotaku.ktextension.contains
import org.mariotaku.twidere.R
import org.mariotaku.twidere.adapter.decorator.ExtendedDividerItemDecoration
import org.mariotaku.twidere.annotation.DisplayOption
import org.mariotaku.twidere.util.ThemeUtils
import org.mariotaku.twidere.view.BadgeView
import org.mariotaku.twidere.view.iface.PagerIndicator

class TabPagerIndicator(context: Context, attrs: AttributeSet? = null) : RecyclerView(context, attrs),
        PagerIndicator, ChameleonView {


    val stripHeight: Int


    val count: Int
        get() = (adapter as TabPagerIndicatorAdapter).itemCount

    var itemContext: Context?
        get() = (adapter as TabPagerIndicatorAdapter).itemContext
        set(context) {
            (adapter as TabPagerIndicatorAdapter).itemContext = context
        }

    var columns: Int
        get() = if (mColumns > 0) mColumns else 1
        set(columns) {
            mColumns = columns
            notifyDataSetChanged()
        }

    private var pageChangeListener: OnPageChangeListener? = null
    private val itemDecoration: ExtendedDividerItemDecoration
    private lateinit var viewPager: ViewPager

    private var option: Int = 0
    private var tabHorizontalPadding: Int = 0
    private var tabVerticalPadding: Int = 0
    private var mColumns: Int = 0

    private val currentItem: Int
        get() = viewPager.currentItem

    private val isIconDisplayed: Boolean
        get() = DisplayOption.ICON in option

    private val isLabelDisplayed: Boolean
        get() = DisplayOption.LABEL in option

    private val indicatorAdapter: TabPagerIndicatorAdapter
        get() = adapter as TabPagerIndicatorAdapter

    var isTabExpandEnabled: Boolean
        get() = (layoutManager as TabLayoutManager).isTabExpandEnabled
        set(expandEnabled) {
            (layoutManager as TabLayoutManager).isTabExpandEnabled = expandEnabled
        }

    init {
        ViewCompat.setLayoutDirection(this, ViewCompat.LAYOUT_DIRECTION_LTR)
        adapter = TabPagerIndicatorAdapter(this)
        itemDecoration = ExtendedDividerItemDecoration(context, RecyclerView.HORIZONTAL)

        overScrollMode = View.OVER_SCROLL_NEVER
        isHorizontalScrollBarEnabled = false
        isVerticalScrollBarEnabled = false
        layoutManager = TabLayoutManager(context, this)
        itemContext = context
        adapter = adapter as TabPagerIndicatorAdapter
        val a = context.obtainStyledAttributes(attrs, R.styleable.TabPagerIndicator)
        isTabExpandEnabled = a.getBoolean(R.styleable.TabPagerIndicator_tabExpandEnabled, false)
        stripHeight = a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabStripHeight, resources.getDimensionPixelSize(R.dimen.element_spacing_small))
        setHorizontalPadding(a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabHorizontalPadding, 0))
        setVerticalPadding(a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabVerticalPadding, 0))
        setStripColor(a.getColor(R.styleable.TabPagerIndicator_tabStripColor, 0))
        setIconColor(a.getColor(R.styleable.TabPagerIndicator_tabIconColor, 0))
        setLabelColor(a.getColor(R.styleable.TabPagerIndicator_tabLabelColor, ThemeUtils.getTextColorPrimary(context)))
        setTabDisplayOption(a.getInt(R.styleable.TabPagerIndicator_tabDisplayOption, DisplayOption.ICON))
        setTabShowDivider(a.getBoolean(R.styleable.TabPagerIndicator_tabShowDivider, false))
        val dividerVerticalPadding = a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabDividerVerticalPadding, 0)
        val dividerHorizontalPadding = a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabDividerHorizontalPadding, 0)
        itemDecoration.setPadding(dividerHorizontalPadding, dividerVerticalPadding,
                dividerHorizontalPadding, dividerVerticalPadding)
        itemDecoration.setDecorationStart(0)
        itemDecoration.setDecorationEndOffset(1)
        a.recycle()

        if (isInEditMode) {
            indicatorAdapter.tabProvider = DemoTabProvider()
        }
    }

    override fun notifyDataSetChanged() {
        indicatorAdapter.notifyDataSetChanged()
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        pageChangeListener?.onPageScrolled(position, positionOffset, positionOffsetPixels)
    }

    override fun onPageSelected(position: Int) {
        notifyDataSetChanged()
        smoothScrollToPosition(position)
        pageChangeListener?.onPageSelected(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
        pageChangeListener?.onPageScrollStateChanged(state)
    }

    override fun setCurrentItem(item: Int) {
        viewPager.currentItem = item
    }

    override fun setOnPageChangeListener(listener: OnPageChangeListener?) {
        pageChangeListener = listener
    }

    override fun setViewPager(view: ViewPager, initialPosition: Int) {
        viewPager = view
        indicatorAdapter.tabProvider = view.adapter as PagerIndicator.TabProvider
        view.addOnPageChangeListener(this)
    }

    override fun isPostApplyTheme(): Boolean {
        return false
    }

    override fun createAppearance(context: Context, attributeSet: AttributeSet, theme: Chameleon.Theme): Appearance? {
        val appearance = Appearance()
        val toolbarColor = theme.colorToolbar
        val itemColor = ChameleonUtils.getColorDependent(toolbarColor)
        appearance.labelColor = itemColor
        appearance.iconColor = itemColor
        if (theme.isToolbarColored) {
            appearance.stripColor = itemColor
        } else {
            appearance.stripColor = theme.colorAccent
        }
        return appearance
    }

    override fun applyAppearance(appearance: ChameleonView.Appearance) {
        val a = appearance as Appearance
        setIconColor(a.iconColor)
        setLabelColor(a.labelColor)
        setStripColor(a.stripColor)
        updateAppearance()
    }

    fun updateAppearance() {
        val positionStart = (layoutManager as TabLayoutManager).findFirstVisibleItemPosition()
        val itemCount = (layoutManager as TabLayoutManager).findLastVisibleItemPosition() - positionStart + 1
        (adapter as TabPagerIndicatorAdapter).notifyItemRangeChanged(positionStart, itemCount)
    }

    fun setBadge(position: Int, count: Int) {
        (adapter as TabPagerIndicatorAdapter).setBadge(position, count)
    }

    fun clearBadge() {
        (adapter as TabPagerIndicatorAdapter).clearBadge()
    }

    fun setDisplayBadge(display: Boolean) {
        (adapter as TabPagerIndicatorAdapter).setDisplayBadge(display)
    }

    fun setIconColor(color: Int) {
        (adapter as TabPagerIndicatorAdapter).setIconColor(color)
    }

    fun setLabelColor(color: Int) {
        (adapter as TabPagerIndicatorAdapter).setLabelColor(color)
    }

    fun setStripColor(color: Int) {
        (adapter as TabPagerIndicatorAdapter).setStripColor(color)
    }

    fun setTabDisplayOption(@DisplayOption flags: Int) {
        option = flags
    }

    private fun dispatchTabClick(position: Int) {
        val currentItem = currentItem
        setCurrentItem(position)
        val tabProvider = indicatorAdapter.tabProvider
        if (tabProvider is PagerIndicator.TabListener) {
            if (currentItem != position) {
                tabProvider.onTabClick(position)
            } else {
                tabProvider.onSelectedTabClick(position)
            }
        }
    }

    private fun dispatchTabLongClick(position: Int): Boolean {
        val tabProvider = indicatorAdapter.tabProvider
        return if (tabProvider is PagerIndicator.TabListener) {
            tabProvider.onTabLongClick(position)
        } else false
    }

    private fun setHorizontalPadding(padding: Int) {
        tabHorizontalPadding = padding
    }

    private fun setTabShowDivider(showDivider: Boolean) {
        if (showDivider) {
            addItemDecoration(itemDecoration)
        } else {
            removeItemDecoration(itemDecoration)
        }
    }

    private fun setVerticalPadding(padding: Int) {
        tabVerticalPadding = padding
    }

    private fun isTabSelected(position: Int): Boolean {
        val current = currentItem
        val columns = columns
        val count = count
        return if (current + columns > count) {
            position >= count - columns
        } else position >= current && position < current + columns
    }

    class Appearance : ChameleonView.Appearance {
        @ColorInt
        var iconColor: Int = 0
        @ColorInt
        var labelColor: Int = 0
        @ColorInt
        var stripColor: Int = 0
    }

    class ItemLayout(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

        private val stripPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        private var isCurrent: Boolean = false
        private var stripColor: Int = 0
        private var stripHeight: Int = 0

        init {
            setWillNotDraw(false)
        }

        fun setIsCurrent(isCurrent: Boolean) {
            if (this.isCurrent == isCurrent) return
            this.isCurrent = isCurrent
            invalidate()
        }

        fun setStripColor(stripColor: Int) {
            if (this.stripColor == stripColor) return
            this.stripColor = stripColor
            stripPaint.color = stripColor
            invalidate()
        }

        fun setStripHeight(stripHeight: Int) {
            if (this.stripHeight == stripHeight) return
            this.stripHeight = stripHeight
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            if (isCurrent) {
                val width = width
                val height = height
                canvas.drawRect(0f, (height - stripHeight).toFloat(), width.toFloat(), height.toFloat(), stripPaint)
            }
            super.onDraw(canvas)
        }
    }

    private class TabItemHolder(
            private val indicator: TabPagerIndicator,
            itemView: View
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        private val iconView: ImageView
        private val labelView: TextView
        private val badgeView: BadgeView

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
            iconView = itemView.findViewById(R.id.tab_icon)
            labelView = itemView.findViewById(R.id.tab_label)
            badgeView = itemView.findViewById(R.id.unread_indicator)
        }

        override fun onClick(v: View) {
            indicator.dispatchTabClick(layoutPosition)
        }

        override fun onLongClick(v: View): Boolean {
            val position = layoutPosition
            return if (position == RecyclerView.NO_POSITION) false else indicator.dispatchTabLongClick(position)
        }

        internal fun setBadge(count: Int, display: Boolean) {
            badgeView.text = count.toString()
            badgeView.visibility = if (display && count > 0) View.VISIBLE else View.GONE
        }

        internal fun setDisplayOption(iconDisplayed: Boolean, labelDisplayed: Boolean) {
            iconView.visibility = if (iconDisplayed) View.VISIBLE else View.GONE
            labelView.visibility = if (labelDisplayed) View.VISIBLE else View.GONE
        }

        internal fun setIconColor(color: Int) {
            if (color != 0) {
                iconView.setColorFilter(color)
            } else {
                iconView.clearColorFilter()
            }
        }

        internal fun setLabelColor(color: Int) {
            labelView.setTextColor(color)
        }

        internal fun setPadding(horizontalPadding: Int, verticalPadding: Int) {
            itemView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        }

        internal fun setStripColor(color: Int) {
            (itemView as ItemLayout).setStripColor(color)
        }

        internal fun setStripHeight(stripHeight: Int) {
            (itemView as ItemLayout).setStripHeight(stripHeight)
        }

        internal fun setTabData(icon: Drawable?, title: CharSequence?, activated: Boolean) {
            itemView.contentDescription = title
            iconView.setImageDrawable(icon)
            labelView.text = title
            (itemView as ItemLayout).setIsCurrent(activated)
        }
    }

    private class TabLayoutManager(
            context: Context,
            private val recyclerView: RecyclerView
    ) : FixedLinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {

        internal var isTabExpandEnabled: Boolean = false

        override fun measureChildWithMargins(child: View, widthUsed: Int, heightUsed: Int) {
            // first get default measured size
            super.measureChildWithMargins(child, widthUsed, heightUsed)
            if (!isTabExpandEnabled) return
            val count = itemCount
            if (count == 0) return
            val parentHeight = recyclerView.height
            val parentWidth = recyclerView.width
            val decoratedWidth = getDecoratedMeasuredWidth(child)
            val measuredWidth = child.measuredWidth
            val decoratorWidth = decoratedWidth - measuredWidth
            val width = Math.max(measuredWidth, parentWidth / count - decoratorWidth)
            val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(parentHeight, View.MeasureSpec.EXACTLY)
            val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY)
            child.measure(widthMeasureSpec, heightMeasureSpec)
        }

        override fun isLayoutRTL(): Boolean = false

        override fun isAutoMeasureEnabled(): Boolean = true
    }

    private class TabPagerIndicatorAdapter internal constructor(private val indicator: TabPagerIndicator?) : RecyclerView.Adapter<TabItemHolder>() {
        var itemContext: Context? = null
            set(itemContext) {
                field = itemContext
                inflater = LayoutInflater.from(itemContext)
            }

        lateinit var tabProvider: PagerIndicator.TabProvider

        private val unreadCounts = SparseIntArray()

        private var inflater: LayoutInflater? = null
        private var stripColor: Int = 0
        private var iconColor: Int = 0
        private var labelColor: Int = 0
        private var displayBadge: Boolean = false

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TabItemHolder {
            if (indicator == null) throw IllegalStateException("item context not set")
            val view = inflater!!.inflate(R.layout.layout_tab_item, parent, false)
            val holder = TabItemHolder(indicator, view)
            holder.setStripHeight(indicator.stripHeight)
            holder.setPadding(indicator.tabHorizontalPadding, indicator.tabVerticalPadding)
            holder.setDisplayOption(indicator.isIconDisplayed, indicator.isLabelDisplayed)
            return holder
        }

        override fun onBindViewHolder(holder: TabItemHolder, position: Int) {
            val icon = tabProvider.getPageIcon(position)
            val title = tabProvider.getPageTitle(position)
            holder.setTabData(icon, title, indicator!!.isTabSelected(position))
            holder.setBadge(unreadCounts.get(position, 0), displayBadge)

            holder.setStripColor(stripColor)
            holder.setIconColor(iconColor)
            holder.setLabelColor(labelColor)
        }

        override fun getItemCount(): Int {
            if (!this::tabProvider.isInitialized) return 0
            return tabProvider.getCount()
        }

        internal fun setBadge(position: Int, count: Int) {
            unreadCounts.put(position, count)
            notifyItemChanged(position)
        }

        internal fun clearBadge() {
            unreadCounts.clear()
            notifyDataSetChanged()
        }

        internal fun setDisplayBadge(display: Boolean) {
            displayBadge = display
        }

        internal fun setIconColor(color: Int) {
            iconColor = color
        }

        internal fun setLabelColor(color: Int) {
            labelColor = color
        }

        internal fun setStripColor(color: Int) {
            stripColor = color
        }

    }

    private class DemoTabProvider : PagerIndicator.TabProvider {

        override fun getCount(): Int = 4

        override fun getPageIcon(position: Int): Drawable? {
            return null
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return "Title " + position
        }

        override fun getPageWidth(position: Int): Float {
            return 1f
        }
    }
}
