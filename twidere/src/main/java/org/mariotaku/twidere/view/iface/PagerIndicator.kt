/*
 * Copyright (C) 2011 Patrik Akerfeldt
 * Copyright (C) 2011 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.view.iface

import android.graphics.drawable.Drawable
import android.support.v4.view.ViewPager

/**
 * A PageIndicator is responsible to show an visual indicator on the total views
 * number and the current visible view.
 */
interface PagerIndicator : ViewPager.OnPageChangeListener {
    /**
     * Notify the indicator that the fragment list has changed.
     */
    fun notifyDataSetChanged()

    /**
     *
     *
     * Set the current page of both the ViewPager and indicator.
     *
     *
     *
     *
     *
     * This **must** be used if you need to set the page before the
     * views are drawn on screen (e.g., default start page).
     *
     */
    fun setCurrentItem(item: Int)

    /**
     * Set a page change listener which will receive forwarded events.
     */
    fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener?)

    /**
     * Bind the indicator to a ViewPager.
     */
    fun setViewPager(view: ViewPager, initialPosition: Int = view.currentItem)

    interface TabListener {

        fun onTabClick(position: Int)

        fun onSelectedTabClick(position: Int)

        fun onTabLongClick(position: Int): Boolean
    }

    /**
     * A TitleProvider provides the title to display according to a view.
     */
    interface TabProvider {

        fun getCount(): Int

        /**
         * Returns the icon of the view at position
         */
        fun getPageIcon(position: Int): Drawable?

        /**
         * Returns the title of the view at position
         */
        fun getPageTitle(position: Int): CharSequence?

        fun getPageWidth(position: Int): Float
    }
}