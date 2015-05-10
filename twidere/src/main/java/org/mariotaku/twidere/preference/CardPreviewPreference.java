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
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder.DummyStatusHolderAdapter;

public class CardPreviewPreference extends Preference implements Constants, OnSharedPreferenceChangeListener {

    private final LayoutInflater mInflater;
    private final SharedPreferences mPreferences;
    private final TwidereLinkify mLinkify;
    private StatusViewHolder mHolder;
    private boolean mCompactModeChanged;
    private DummyStatusHolderAdapter mAdapter;

    public CardPreviewPreference(final Context context) {
        this(context, null);
    }

    public CardPreviewPreference(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardPreviewPreference(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mInflater = LayoutInflater.from(context);
        mLinkify = new TwidereLinkify(null);
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
        mAdapter = new DummyStatusHolderAdapter(context);
    }

    @Override
    public View getView(final View convertView, final ViewGroup parent) {
        if (mCompactModeChanged) return super.getView(null, parent);
        return super.getView(convertView, parent);
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
        if (mHolder == null) return;
        if (KEY_COMPACT_CARDS.equals(key)) {
            mCompactModeChanged = true;
        }
        mAdapter.updateOptions();
        notifyChanged();
    }

    @Override
    protected void onBindView(@NonNull final View view) {
        if (mHolder == null) return;
        mCompactModeChanged = false;
        mHolder.setupViewOptions();
        mHolder.displaySampleStatus();
        super.onBindView(view);
    }

    @Override
    protected View onCreateView(final ViewGroup parent) {
        final View statusView;
        if (mPreferences != null && mPreferences.getBoolean(KEY_COMPACT_CARDS, false)) {
            statusView = mInflater.inflate(R.layout.card_item_status_compact, parent, false);
        } else {
            statusView = mInflater.inflate(R.layout.card_item_status, parent, false);
        }
        mHolder = new StatusViewHolder(mAdapter, statusView);
        return statusView;
    }

}
