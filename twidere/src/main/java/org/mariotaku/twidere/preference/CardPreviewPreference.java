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

package org.mariotaku.twidere.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.DummyItemAdapter;
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

import static org.mariotaku.twidere.TwidereConstants.SHARED_PREFERENCES_NAME;

public class CardPreviewPreference extends Preference implements OnSharedPreferenceChangeListener {

    private StatusViewHolder mHolder;
    private DummyItemAdapter mAdapter;

    public CardPreviewPreference(final Context context) {
        this(context, null);
    }

    public CardPreviewPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardPreviewPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                Context.MODE_PRIVATE);
        setLayoutResource(R.layout.layout_preferences_card_preview_compact);
        preferences.registerOnSharedPreferenceChangeListener(this);
        mAdapter = new DummyItemAdapter(context);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
        if (mHolder == null) return;
        mAdapter.updateOptions();
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        if (mHolder == null) {
            mHolder = new StatusViewHolder(mAdapter, holder.itemView);
        }
        mHolder.setupViewOptions();
        mHolder.displaySampleStatus();
        mHolder.setStatusClickListener(new IStatusViewHolder.SimpleStatusClickListener() {
            @Override
            public void onItemActionClick(RecyclerView.ViewHolder holder, int id, int position) {
                if (id == R.id.favorite) {
                    ((StatusViewHolder) holder).playLikeAnimation(new LikeAnimationDrawable.OnLikedListener() {
                        @Override
                        public boolean onLiked() {
                            return false;
                        }
                    });
                }
            }
        });
        super.onBindViewHolder(holder);
    }

}
