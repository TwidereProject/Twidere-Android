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

package org.mariotaku.twidere.view.iface;

import android.graphics.drawable.Drawable;
import androidx.viewpager.widget.ViewPager;

/**
 * A PageIndicator is responsible to show an visual indicator on the total views
 * number and the current visible view.
 */
@SuppressWarnings("unused")
public interface PagerIndicator extends ViewPager.OnPageChangeListener {
    /**
     * Notify the indicator that the fragment list has changed.
     */
    void notifyDataSetChanged();

    /**
     * <p>
     * Set the current page of both the ViewPager and indicator.
     * </p>
     * <p/>
     * <p>
     * This <strong>must</strong> be used if you need to set the page before the
     * views are drawn on screen (e.g., default start page).
     * </p>
     */
    void setCurrentItem(int item);

    /**
     * Set a page change listener which will receive forwarded events.
     */
    void setOnPageChangeListener(ViewPager.OnPageChangeListener listener);

    /**
     * Bind the indicator to a ViewPager.
     */
    void setViewPager(ViewPager view);

    /**
     * Bind the indicator to a ViewPager.
     */
    void setViewPager(ViewPager view, int initialPosition);

    interface TabListener {

        void onPageReselected(int position);

        void onPageSelected(int position);

        boolean onTabLongClick(int position);
    }

    /**
     * A TitleProvider provides the title to display according to a view.
     */
    interface TabProvider {

        int getCount();

        /**
         * Returns the icon of the view at position
         */
        Drawable getPageIcon(int position);

        /**
         * Returns the title of the view at position
         */
        CharSequence getPageTitle(int position);

        float getPageWidth(int position);
    }
}