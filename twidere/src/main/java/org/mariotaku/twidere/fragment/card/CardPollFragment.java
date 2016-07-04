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

package org.mariotaku.twidere.fragment.card;

import android.content.Context;
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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TextView;

import org.apache.commons.lang3.math.NumberUtils;
import org.mariotaku.abstask.library.AbstractTask;
import org.mariotaku.abstask.library.TaskStarter;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.twitter.TwitterCaps;
import org.mariotaku.microblog.library.twitter.model.CardDataMap;
import org.mariotaku.microblog.library.twitter.model.CardEntity;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.BaseSupportFragment;
import org.mariotaku.twidere.model.ParcelableCardEntity;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableCardEntityUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.support.ViewSupport;

import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mariotaku on 15/12/20.
 */
public class CardPollFragment extends BaseSupportFragment implements
        LoaderManager.LoaderCallbacks<ParcelableCardEntity>, View.OnClickListener {

    public static final Pattern PATTERN_POLL_TEXT_ONLY = Pattern.compile("poll([\\d]+)choice_text_only");
    private TableLayout mPollContainer;
    private TextView mPollSummary;
    private ParcelableCardEntity mCard;


    public static CardPollFragment show(ParcelableStatus status) {
        final CardPollFragment fragment = new CardPollFragment();
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_STATUS, status);
        args.putParcelable(EXTRA_CARD, status.card);
        fragment.setArguments(args);
        return fragment;
    }

    public static boolean isPoll(@NonNull ParcelableCardEntity card) {
        return PATTERN_POLL_TEXT_ONLY.matcher(card.name).matches() && !TextUtils.isEmpty(card.url);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initChoiceView(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPollContainer = (TableLayout) view.findViewById(R.id.poll_container);
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

        for (int i = 0; i < choicesCount; i++) {
            inflater.inflate(R.layout.layout_poll_item, mPollContainer, true);
        }

        displayPoll(card, status);
    }

    private void displayPoll(final ParcelableCardEntity card, final ParcelableStatus status) {
        final Context context = getContext();
        if (card == null || status == null || context == null) return;
        mCard = card;
        final int choicesCount = getChoicesCount(card);
        int votesSum = 0;
        final boolean countsAreFinal = ParcelableCardEntityUtils.getAsBoolean(card, "counts_are_final", false);
        final int selectedChoice = ParcelableCardEntityUtils.getAsInteger(card, "selected_choice", -1);
        final Date endDatetimeUtc = ParcelableCardEntityUtils.getAsDate(card, "end_datetime_utc", new Date());
        final boolean hasChoice = selectedChoice != -1;
        final boolean isMyPoll = status.account_key.equals(status.user_key);
        final boolean showResult = countsAreFinal || isMyPoll || hasChoice;
        for (int i = 0; i < choicesCount; i++) {
            final int choiceIndex = i + 1;
            votesSum += ParcelableCardEntityUtils.getAsInteger(card, "choice" + choiceIndex + "_count", 0);
        }

        final View.OnClickListener clickListener = new View.OnClickListener() {
            private boolean clickedChoice;

            @Override
            public void onClick(View v) {
                if (hasChoice || clickedChoice) return;
                for (int i = 0, j = mPollContainer.getChildCount(); i < j; i++) {
                    final View pollItem = mPollContainer.getChildAt(i);
                    pollItem.setClickable(false);
                    clickedChoice = true;
                    final RadioButton choiceRadioButton = (RadioButton) pollItem.findViewById(R.id.choice_button);
                    final boolean checked = v == pollItem;
                    choiceRadioButton.setChecked(checked);
                    if (checked) {
                        final CardDataMap cardData = new CardDataMap();
                        cardData.putLong("original_tweet_id", NumberUtils.toLong(status.id));
                        cardData.putString("card_uri", card.url);
                        cardData.putString("cards_platform", MicroBlogAPIFactory.CARDS_PLATFORM_ANDROID_12);
                        cardData.putString("response_card_name", card.name);
                        cardData.putString("selected_choice", String.valueOf(i + 1));
                        AbstractTask<CardDataMap, ParcelableCardEntity, CardPollFragment> task
                                = new AbstractTask<CardDataMap, ParcelableCardEntity, CardPollFragment>() {

                            @Override
                            public void afterExecute(CardPollFragment handler, ParcelableCardEntity result) {
                                handler.displayAndReloadPoll(result, status);
                            }

                            @Override
                            public ParcelableCardEntity doLongOperation(CardDataMap cardDataMap) {
                                final Context context = getContext();
                                if (context == null) return null;
                                final TwitterCaps caps = MicroBlogAPIFactory.getInstance(context,
                                        card.account_key, true, true, TwitterCaps.class);
                                if (caps == null) return null;
                                try {
                                    final CardEntity cardEntity = caps.sendPassThrough(cardDataMap).getCard();
                                    return ParcelableCardEntityUtils.fromCardEntity(cardEntity,
                                            card.account_key);
                                } catch (MicroBlogException e) {
                                    Log.w(LOGTAG, e);
                                }
                                return null;
                            }
                        };
                        task.setCallback(CardPollFragment.this);
                        task.setParams(cardData);
                        TaskStarter.execute(task);
                    }
                }
            }
        };

        final int color = ContextCompat.getColor(context, R.color.material_light_blue_a200);
        final float radius = getResources().getDimension(R.dimen.element_spacing_small);
        for (int i = 0; i < choicesCount; i++) {
            final View pollItem = mPollContainer.getChildAt(i);

            final TextView choicePercentView = (TextView) pollItem.findViewById(R.id.choice_percent);
            final TextView choiceLabelView = (TextView) pollItem.findViewById(R.id.choice_label);
            final RadioButton choiceRadioButton = (RadioButton) pollItem.findViewById(R.id.choice_button);

            final int choiceIndex = i + 1;
            final String label = ParcelableCardEntityUtils.getAsString(card, "choice" + choiceIndex + "_label", null);
            final int value = ParcelableCardEntityUtils.getAsInteger(card, "choice" + choiceIndex + "_count", 0);
            if (label == null) throw new NullPointerException();
            final float choicePercent = votesSum == 0 ? 0 : value / (float) votesSum;
            choiceLabelView.setText(label);
            choicePercentView.setText(String.format(Locale.US, "%d%%", Math.round(choicePercent * 100)));

            pollItem.setOnClickListener(clickListener);

            final boolean isSelected = selectedChoice == choiceIndex;

            if (showResult) {
                choicePercentView.setVisibility(View.VISIBLE);
                choiceRadioButton.setVisibility(hasChoice && isSelected ? View.VISIBLE : View.INVISIBLE);
                ViewSupport.setBackground(choiceLabelView, new PercentDrawable(choicePercent, radius, color));
            } else {
                choicePercentView.setVisibility(View.GONE);
                choiceRadioButton.setVisibility(View.VISIBLE);
                ViewSupport.setBackground(choiceLabelView, null);
            }

            choiceRadioButton.setChecked(isSelected);
            pollItem.setClickable(selectedChoice == -1);

        }

        final String nVotes = getResources().getQuantityString(R.plurals.N_votes, votesSum, votesSum);

        final CharSequence timeLeft = DateUtils.getRelativeTimeSpanString(context,
                endDatetimeUtc.getTime(), true);
        mPollSummary.setText(getString(R.string.poll_summary_format, nVotes, timeLeft));
    }

    private void displayAndReloadPoll(ParcelableCardEntity result, ParcelableStatus status) {
        if (getHost() == null) return;
        displayPoll(result, status);
        getLoaderManager().restartLoader(0, null, this);
    }

    private int getChoicesCount(ParcelableCardEntity card) {
        final Matcher matcher = PATTERN_POLL_TEXT_ONLY.matcher(card.name);
        if (!matcher.matches()) throw new IllegalStateException();
        return NumberUtils.toInt(matcher.group(1));
    }

    @NonNull
    private ParcelableCardEntity getCard() {
        if (mCard != null) return mCard;
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

    @Override
    public Loader<ParcelableCardEntity> onCreateLoader(int id, Bundle args) {
        final ParcelableCardEntity card = getCard();
        return new ParcelableCardEntityLoader(getContext(), card.account_key, card.url, card.name);
    }

    @Override
    public void onLoadFinished(Loader<ParcelableCardEntity> loader, ParcelableCardEntity data) {
        if (data == null) return;
        displayPoll(data, getStatus());
    }

    @Override
    public void onLoaderReset(Loader<ParcelableCardEntity> loader) {

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

    public static class ParcelableCardEntityLoader extends AsyncTaskLoader<ParcelableCardEntity> {
        private final UserKey mAccountKey;
        private final String mCardUri;
        private final String mCardName;

        public ParcelableCardEntityLoader(Context context, UserKey accountKey,
                                          String cardUri, String cardName) {
            super(context);
            mAccountKey = accountKey;
            mCardUri = cardUri;
            mCardName = cardName;
        }

        @Override
        public ParcelableCardEntity loadInBackground() {
            final TwitterCaps caps = MicroBlogAPIFactory.getInstance(getContext(), mAccountKey,
                    true, true, TwitterCaps.class);
            if (caps == null) return null;
            try {
                final CardDataMap params = new CardDataMap();
                params.putString("card_uri", mCardUri);
                params.putString("cards_platform", MicroBlogAPIFactory.CARDS_PLATFORM_ANDROID_12);
                params.putString("response_card_name", mCardName);
                final CardEntity card = caps.getPassThrough(params).getCard();
                if (card == null || card.getName() == null) {
                    return null;
                }
                return ParcelableCardEntityUtils.fromCardEntity(card, mAccountKey);
            } catch (MicroBlogException e) {
                return null;
            }
        }

        @Override
        protected void onStartLoading() {
            forceLoad();
        }
    }
}
