package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwidereLinkify.HighlightStyle;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.CardMediaContainer.PreviewStyle;
import org.mariotaku.twidere.view.ShapedImageView.ShapeStyle;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

import java.lang.ref.WeakReference;

/**
 * Created by mariotaku on 14/11/19.
 */
public abstract class AbsStatusesAdapter<D> extends LoadMoreSupportAdapter<ViewHolder> implements Constants,
        IStatusesAdapter<D> {

    public static final int ITEM_VIEW_TYPE_STATUS = 2;

    final LayoutInflater mInflater;
    final MediaLoadingHandler mLoadingHandler;
    final TwidereLinkify mLinkify;
    final int mCardBackgroundColor;
    final int mTextSize;
    @ShapeStyle
    final int mProfileImageStyle;
    @PreviewStyle
    final int mMediaPreviewStyle;
    @HighlightStyle
    final int mLinkHighlightingStyle;
    final boolean mCompactCards;
    final boolean mNameFirst;
    final boolean mDisplayMediaPreview;
    final boolean mDisplayProfileImage;
    final boolean mSensitiveContentEnabled;
    final boolean mHideCardActions;
    final boolean mUseStarsForLikes;
    final boolean mShowAbsoluteTime;
    final EventListener mEventListener;
    @Nullable
    StatusAdapterListener mStatusAdapterListener;
    boolean mShowInReplyTo;
    boolean mShowAccountsColor;

    public AbsStatusesAdapter(Context context, boolean compact) {
        super(context);
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context, ThemeUtils.getThemeBackgroundOption(context), ThemeUtils.getUserThemeBackgroundAlpha(context));
        mInflater = LayoutInflater.from(context);
        mLoadingHandler = new MediaLoadingHandler(getProgressViewIds());
        mEventListener = new EventListener(this);
        mTextSize = mPreferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
        mCompactCards = compact;
        mProfileImageStyle = Utils.getProfileImageStyle(mPreferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
        mMediaPreviewStyle = Utils.getMediaPreviewStyle(mPreferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
        mLinkHighlightingStyle = Utils.getLinkHighlightingStyleInt(mPreferences.getString(KEY_LINK_HIGHLIGHT_OPTION, null));
        mNameFirst = mPreferences.getBoolean(KEY_NAME_FIRST, true);
        mDisplayProfileImage = mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
        mDisplayMediaPreview = mPreferences.getBoolean(KEY_MEDIA_PREVIEW, false);
        mSensitiveContentEnabled = mPreferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false);
        mHideCardActions = mPreferences.getBoolean(KEY_HIDE_CARD_ACTIONS, false);
        mUseStarsForLikes = mPreferences.getBoolean(KEY_I_WANT_MY_STARS_BACK);
        mShowAbsoluteTime = mPreferences.getBoolean(KEY_SHOW_ABSOLUTE_TIME, false);
        mLinkify = new TwidereLinkify(new StatusAdapterLinkClickHandler<>(this));
        setShowInReplyTo(true);
    }

    protected abstract int[] getProgressViewIds();

    public abstract D getData();

    @Override
    public abstract void setData(D data);

    @Override
    public boolean shouldShowAccountsColor() {
        return mShowAccountsColor;
    }


    @Override
    public final MediaLoadingHandler getMediaLoadingHandler() {
        return mLoadingHandler;
    }

    @Nullable
    @Override
    public IStatusViewHolder.StatusClickListener getStatusClickListener() {
        return mEventListener;
    }

    @Override
    public final int getProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public final int getMediaPreviewStyle() {
        return mMediaPreviewStyle;
    }

    @Override
    public final float getTextSize() {
        return mTextSize;
    }

    @Nullable
    @Override
    public StatusAdapterListener getStatusAdapterListener() {
        return mStatusAdapterListener;
    }

    @Override

    public TwidereLinkify getTwidereLinkify() {
        return mLinkify;
    }

    @Override
    public boolean isMediaPreviewEnabled() {
        return mDisplayMediaPreview;
    }

    @Override
    public int getLinkHighlightingStyle() {
        return mLinkHighlightingStyle;
    }

    @Override
    public boolean isNameFirst() {
        return mNameFirst;
    }

    @Override
    public boolean isSensitiveContentEnabled() {
        return mSensitiveContentEnabled;
    }

    @Override
    public boolean isCardActionsHidden() {
        return mHideCardActions;
    }

    @Override
    public boolean isProfileImageEnabled() {
        return mDisplayProfileImage;
    }

    @Override
    public boolean shouldUseStarsForLikes() {
        return mUseStarsForLikes;
    }


    public boolean isShowInReplyTo() {
        return mShowInReplyTo;
    }

    public void setShowInReplyTo(boolean showInReplyTo) {
        if (mShowInReplyTo == showInReplyTo) return;
        mShowInReplyTo = showInReplyTo;
        notifyDataSetChanged();
    }

    public boolean isStatus(int position) {
        return position < getStatusCount();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_STATUS: {
                return (ViewHolder) onCreateStatusViewHolder(parent, mCompactCards);
            }
            case ITEM_VIEW_TYPE_GAP: {
                final View view = mInflater.inflate(R.layout.card_item_gap, parent, false);
                return new GapViewHolder(this, view);
            }
            case ITEM_VIEW_TYPE_LOAD_INDICATOR: {
                final View view = mInflater.inflate(R.layout.card_item_load_indicator, parent, false);
                return new LoadIndicatorViewHolder(view);
            }
        }
        throw new IllegalStateException("Unknown view type " + viewType);
    }

    protected LayoutInflater getInflater() {
        return mInflater;
    }

    protected int getCardBackgroundColor() {
        return mCardBackgroundColor;
    }

    @NonNull
    protected abstract IStatusViewHolder onCreateStatusViewHolder(ViewGroup parent, boolean compact);

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_VIEW_TYPE_STATUS: {
                bindStatus(((IStatusViewHolder) holder), position);
                break;
            }
        }
    }

    @Override
    public boolean isShowAbsoluteTime() {
        return mShowAbsoluteTime;
    }

    @Override
    public int getItemViewType(int position) {
        if ((getLoadMoreIndicatorPosition() & IndicatorPosition.START) != 0 && position == 0) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        }
        if (position == getStatusCount()) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        } else if (isGapItem(position)) {
            return ITEM_VIEW_TYPE_GAP;
        }
        return ITEM_VIEW_TYPE_STATUS;
    }

    @Override
    public final int getItemCount() {
        final int position = getLoadMoreIndicatorPosition();
        int count = 0;
        if ((position & IndicatorPosition.START) != 0) {
            count += 1;
        }
        count += getStatusCount();
        if ((position & IndicatorPosition.END) != 0) {
            count += 1;
        }
        return count;
    }


    public void setListener(@Nullable StatusAdapterListener listener) {
        mStatusAdapterListener = listener;
    }

    public void setShowAccountsColor(boolean showAccountsColor) {
        if (mShowAccountsColor == showAccountsColor) return;
        mShowAccountsColor = showAccountsColor;
        notifyDataSetChanged();
    }


    @Nullable
    @Override
    public ParcelableStatus findStatusById(long accountId, long statusId) {
        for (int i = 0, j = getStatusCount(); i < j; i++) {
            if (accountId == getAccountId(i) && statusId == getStatusId(i)) return getStatus(i);
        }
        return null;
    }

    protected void bindStatus(IStatusViewHolder holder, int position) {
        holder.displayStatus(getStatus(position), isShowInReplyTo());
    }

    @Nullable
    @Override
    public GapClickListener getGapClickListener() {
        return mEventListener;
    }

    public int getStatusStartIndex() {
        final int position = getLoadMoreIndicatorPosition();
        int start = 0;
        if ((position & IndicatorPosition.START) != 0) {
            start += 1;
        }
        return start;
    }

    public static class EventListener implements GapClickListener, IStatusViewHolder.StatusClickListener {

        private final WeakReference<IStatusesAdapter<?>> adapterRef;

        public EventListener(IStatusesAdapter<?> adapter) {
            adapterRef = new WeakReference<IStatusesAdapter<?>>(adapter);
        }

        @Override
        public final void onStatusClick(IStatusViewHolder holder, int position) {
            final IStatusesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            final StatusAdapterListener listener = adapter.getStatusAdapterListener();
            if (listener == null) return;
            listener.onStatusClick(holder, position);
        }

        @Override
        public void onMediaClick(IStatusViewHolder holder, View view, final ParcelableMedia media, int statusPosition) {
            final IStatusesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            final StatusAdapterListener listener = adapter.getStatusAdapterListener();
            if (listener == null) return;
            listener.onMediaClick(holder, view, media, statusPosition);
        }

        @Override
        public void onUserProfileClick(final IStatusViewHolder holder, final int position) {
            final IStatusesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            final StatusAdapterListener listener = adapter.getStatusAdapterListener();
            if (listener == null) return;
            final ParcelableStatus status = adapter.getStatus(position);
            if (status == null) return;
            listener.onUserProfileClick(holder, status, position);
        }

        @Override
        public boolean onStatusLongClick(IStatusViewHolder holder, int position) {
            final IStatusesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return false;
            final StatusAdapterListener listener = adapter.getStatusAdapterListener();
            return listener != null && listener.onStatusLongClick(holder, position);
        }

        @Override
        public void onItemActionClick(ViewHolder holder, int id, int position) {
            final IStatusesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            final StatusAdapterListener listener = adapter.getStatusAdapterListener();
            if (listener == null) return;
            listener.onStatusActionClick((IStatusViewHolder) holder, id, position);
        }

        @Override
        public void onItemMenuClick(ViewHolder holder, View menuView, int position) {
            final IStatusesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            final StatusAdapterListener listener = adapter.getStatusAdapterListener();
            if (listener == null) return;
            listener.onStatusMenuClick((IStatusViewHolder) holder, menuView, position);
        }

        @Override
        public final void onGapClick(ViewHolder holder, int position) {
            final IStatusesAdapter<?> adapter = adapterRef.get();
            if (adapter == null) return;
            final StatusAdapterListener listener = adapter.getStatusAdapterListener();
            if (listener == null) return;
            listener.onGapClick((GapViewHolder) holder, position);
        }

    }

}
