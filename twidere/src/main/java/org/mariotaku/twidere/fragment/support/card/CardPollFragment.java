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

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TextView;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.support.BaseSupportFragment;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatus.ParcelableCardEntity;
import org.mariotaku.twidere.util.support.ViewSupport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mariotaku on 15/12/20.
 */
public class CardPollFragment extends BaseSupportFragment implements View.OnClickListener {

    public static final Pattern PATTERN_POLL_TEXT_ONLY = Pattern.compile("poll([\\d]+)choice_text_only");
    private TableLayout mPollContainer;
    private Button mVoteButton;
    private TextView mPollSummary;

    public static CardPollFragment show(ParcelableStatus status) {
        final CardPollFragment fragment = new CardPollFragment();
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_STATUS, status);
        args.putParcelable(EXTRA_CARD, status.card);
        fragment.setArguments(args);
        return fragment;
    }

    public static boolean isPoll(@NonNull String name) {
        return PATTERN_POLL_TEXT_ONLY.matcher(name).matches();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mVoteButton.setOnClickListener(this);
        initChoiceView(savedInstanceState);
    }

    @Override
    public void onBaseViewCreated(View view, Bundle savedInstanceState) {
        super.onBaseViewCreated(view, savedInstanceState);
        mPollContainer = (TableLayout) view.findViewById(R.id.poll_container);
        mVoteButton = (Button) view.findViewById(R.id.vote);
        mPollSummary = (TextView) view.findViewById(R.id.poll_summary);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_card_poll, container, false);
    }

    @Override
    protected void fitSystemWindows(Rect insets) {
        // No-op
    }


    private void initChoiceView(@Nullable Bundle savedInstanceState) {
        final ParcelableCardEntity card = getCard();
        final ParcelableStatus status = getStatus();
        final int choicesCount = getChoicesCount(card);
        final LayoutInflater inflater = getLayoutInflater(savedInstanceState);
        int votesSum = 0;
        final boolean countsAreFinal = card.getAsBoolean("counts_are_final", false);
        final boolean showResult = countsAreFinal || status.account_id == status.user_id;
        for (int i = 0; i < choicesCount; i++) {
            final int choiceIndex = i + 1;
            votesSum += card.getAsInteger("choice" + choiceIndex + "_count", 0);
        }

        final View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0, j = mPollContainer.getChildCount(); i < j; i++) {
                    final View pollItem = mPollContainer.getChildAt(i);
                    final RadioButton choiceRadioButton = (RadioButton) pollItem.findViewById(R.id.choice_button);
                    choiceRadioButton.setChecked(v == pollItem);
                }
            }
        };

        final int color = ContextCompat.getColor(getContext(), R.color.material_light_blue_a200);
        final float radius = getResources().getDimension(R.dimen.element_spacing_small);
        for (int i = 0; i < choicesCount; i++) {
            final View pollItem = inflater.inflate(R.layout.layout_poll_item, mPollContainer, false);

            final TextView choicePercentView = (TextView) pollItem.findViewById(R.id.choice_percent);
            final TextView choiceLabelView = (TextView) pollItem.findViewById(R.id.choice_label);
            final RadioButton choiceRadioButton = (RadioButton) pollItem.findViewById(R.id.choice_button);

            final int choiceIndex = i + 1;
            final String label = card.getAsString("choice" + choiceIndex + "_label", null);
            final int value = card.getAsInteger("choice" + choiceIndex + "_count", 0);
            if (label == null) throw new NullPointerException();
            final float choicePercent = votesSum == 0 ? 0 : value / (float) votesSum;
            choiceLabelView.setText(label);
            ViewSupport.setBackground(choiceLabelView, new PercentDrawable(choicePercent, radius, color));
            choicePercentView.setText(String.format("%d%%", Math.round(choicePercent * 100)));

            pollItem.setOnClickListener(clickListener);

            if (showResult) {
                choicePercentView.setVisibility(View.VISIBLE);
                choiceRadioButton.setVisibility(View.GONE);
            } else {
                choicePercentView.setVisibility(View.GONE);
                choiceRadioButton.setVisibility(View.VISIBLE);
            }

            mPollContainer.addView(pollItem);
        }

        if (showResult) {
            mVoteButton.setVisibility(View.GONE);
        } else {
            mVoteButton.setVisibility(View.VISIBLE);
        }
        final String nVotes = getResources().getQuantityString(R.plurals.N_votes, votesSum, votesSum);
        mPollSummary.setText(getString(R.string.poll_summary_format, nVotes, "final result"));
    }

    private int getChoicesCount(ParcelableCardEntity card) {
        final Matcher matcher = PATTERN_POLL_TEXT_ONLY.matcher(card.name);
        if (!matcher.matches()) throw new IllegalStateException();
        return NumberUtils.toInt(matcher.group(1));
    }

    private ParcelableCardEntity getCard() {
        final ParcelableCardEntity card = getArguments().getParcelable(EXTRA_CARD);
        assert card != null && card.name != null;
        return card;
    }

    private ParcelableStatus getStatus() {
        return getArguments().getParcelable(EXTRA_STATUS);
    }

    @Override
    public void onClick(View v) {

    }

    private static class PercentDrawable extends Drawable {

        private final Paint mPaint;
        private final RectF mBounds;
        private final float mPercent;
        private final float mRadius;

        PercentDrawable(float percent, float radius, int color) {
            mPercent = percent;
            mRadius = radius;
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setColor(color);
            mBounds = new RectF();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawRoundRect(mBounds, mRadius, mRadius, mPaint);
        }

        @Override
        protected void onBoundsChange(Rect bounds) {
            mBounds.set(bounds);
            mBounds.right = mBounds.left + mBounds.width() * mPercent;
            super.onBoundsChange(bounds);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
