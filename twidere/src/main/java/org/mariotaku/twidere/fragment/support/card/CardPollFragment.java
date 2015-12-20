/*
 *                 Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.fragment.support.card;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.support.BaseSupportFragment;
import org.mariotaku.twidere.model.ParcelableStatus.ParcelableCardEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mariotaku on 15/12/20.
 */
public class CardPollFragment extends BaseSupportFragment {

    public static final Pattern PATTERN_POLL_TEXT_ONLY = Pattern.compile("poll([\\d]+)choice_text_only");
    private LinearLayout mPollChoices;

    public static CardPollFragment show(ParcelableCardEntity card) {
        final CardPollFragment fragment = new CardPollFragment();
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_CARD, card);
        fragment.setArguments(args);
        return fragment;
    }

    public static boolean isPoll(@NonNull String name) {
        return PATTERN_POLL_TEXT_ONLY.matcher(name).matches();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ParcelableCardEntity card = getArguments().getParcelable(EXTRA_CARD);
        assert card != null && card.name != null;
        final Matcher matcher = PATTERN_POLL_TEXT_ONLY.matcher(card.name);
        if (!matcher.matches()) throw new IllegalStateException();
        final int choicesCount = NumberUtils.toInt(matcher.group(1));
        for (int i = 0; i < choicesCount; i++) {
            TextView child = new TextView(getContext());
            final int choiceIndex = i + 1;
            final ParcelableCardEntity.ParcelableBindingValue label = card.getValue("choice" + choiceIndex + "_label");
            final ParcelableCardEntity.ParcelableBindingValue value = card.getValue("choice" + choiceIndex + "_count");
            child.setText(label.value + "(" + value.value + ")");
            mPollChoices.addView(child);
        }
    }

    @Override
    public void onBaseViewCreated(View view, Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mPollChoices = (LinearLayout) view.findViewById(R.id.poll_choices);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_poll, container, false);
    }
}
