package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.StatusAdapterLinkClickHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwidereLinkify.HighlightStyle;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.CardMediaContainer.PreviewStyle;
import org.mariotaku.twidere.view.ShapedImageView.ShapeStyle;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.LoadIndicatorViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

/**
 * Created by mariotaku on 14/11/19.
 */
public abstract class AbsStatusesAdapter<D> extends LoadMoreSupportAdapter<ViewHolder> implements Constants,
        IStatusesAdapter<D> {

    public static final int ITEM_VIEW_TYPE_STATUS = 2;

    private final Context mContext;
    private final LayoutInflater mInflater;
    private final MediaLoadingHandler mLoadingHandler;
    private final TwidereLinkify mLinkify;

    private StatusAdapterListener mStatusAdapterListener;

    private final int mCardBackgroundColor;
    private final int mTextSize;
    @ShapeStyle
    private final int mProfileImageStyle;
    @PreviewStyle
    private final int mMediaPreviewStyle;
    @HighlightStyle
    private final int mLinkHighlightingStyle;

    private final boolean mCompactCards;
    private final boolean mNameFirst;
    private final boolean mDisplayMediaPreview;
    private final boolean mDisplayProfileImage;
    private final boolean mSensitiveContentEnabled;
    private final boolean mHideCardActions;

    private boolean mShowInReplyTo;
    private boolean mShowAccountsColor;

    public AbsStatusesAdapter(Context context, boolean compact) {
        super(context);
        mContext = context;
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context, ThemeUtils.getThemeBackgroundOption(context), ThemeUtils.getUserThemeBackgroundAlpha(context));
        mInflater = LayoutInflater.from(context);
        mLoadingHandler = new MediaLoadingHandler(R.id.media_preview_progress);
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
        mLinkify = new TwidereLinkify(new StatusAdapterLinkClickHandler<>(this));
        setShowInReplyTo(true);
    }

    public abstract D getData();

    @Override
    public abstract void setData(D data);

    @Override
    public boolean shouldShowAccountsColor() {
        return mShowAccountsColor;
    }

    @NonNull
    @Override
    public final MediaLoaderWrapper getMediaLoader() {
        return mMediaLoader;
    }

    @NonNull
    @Override
    public final Context getContext() {
        return mContext;
    }

    @Override
    public final MediaLoadingHandler getMediaLoadingHandler() {
        return mLoadingHandler;
    }

    @Override
    public final int getProfileImageStyle() {
        return mProfileImageStyle;
    }

    @Override
    public final int getMediaPreviewStyle() {
        return mMediaPreviewStyle;
    }

    @NonNull
    @Override
    public final AsyncTwitterWrapper getTwitterWrapper() {
        return mTwitterWrapper;
    }

    @NonNull
    @Override
    public UserColorNameManager getUserColorNameManager() {
        return mUserColorNameManager;
    }

    @Override
    public final float getTextSize() {
        return mTextSize;
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
    public boolean onStatusLongClick(StatusViewHolder holder, int position) {
        return mStatusAdapterListener != null && mStatusAdapterListener.onStatusLongClick(holder, position);
    }

    @Override
    public final void onStatusClick(StatusViewHolder holder, int position) {
        if (mStatusAdapterListener == null) return;
        mStatusAdapterListener.onStatusClick(holder, position);
    }

    @Override
    public void onMediaClick(StatusViewHolder holder, View view, final ParcelableMedia media, int position) {
        if (mStatusAdapterListener == null) return;
        mStatusAdapterListener.onMediaClick(holder, view, media, position);
    }

    @Override
    public void onUserProfileClick(final StatusViewHolder holder, final int position) {
        if (mStatusAdapterListener == null) return;
        final ParcelableStatus status = getStatus(position);
        if (status == null) return;
        mStatusAdapterListener.onUserProfileClick(holder, status, position);
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
        return position < getStatusesCount();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM_VIEW_TYPE_STATUS: {
                final View view;
                if (mCompactCards) {
                    view = mInflater.inflate(R.layout.card_item_status_compact, parent, false);
                    final View itemContent = view.findViewById(R.id.item_content);
                    itemContent.setBackgroundColor(mCardBackgroundColor);
                } else {
                    view = mInflater.inflate(R.layout.card_item_status, parent, false);
                    final CardView cardView = (CardView) view.findViewById(R.id.card);
                    cardView.setCardBackgroundColor(mCardBackgroundColor);
                }
                final StatusViewHolder holder = new StatusViewHolder(this, view);
                holder.setOnClickListeners();
                holder.setupViewOptions();
                return holder;
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

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case ITEM_VIEW_TYPE_STATUS: {
                bindStatus(((StatusViewHolder) holder), position);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getStatusesCount()) {
            return ITEM_VIEW_TYPE_LOAD_INDICATOR;
        } else if (isGapItem(position)) {
            return ITEM_VIEW_TYPE_GAP;
        }
        return ITEM_VIEW_TYPE_STATUS;
    }

    @Override
    public final int getItemCount() {
        return getStatusesCount() + (isLoadMoreIndicatorVisible() ? 1 : 0);
    }

    @Override
    public final void onGapClick(ViewHolder holder, int position) {
        if (mStatusAdapterListener == null) return;
        mStatusAdapterListener.onGapClick((GapViewHolder) holder, position);
    }

    @Override
    public void onItemActionClick(ViewHolder holder, int id, int position) {
        if (mStatusAdapterListener == null) return;
        mStatusAdapterListener.onStatusActionClick((StatusViewHolder) holder, id, position);
    }

    @Override
    public void onItemMenuClick(ViewHolder holder, View menuView, int position) {
        if (mStatusAdapterListener == null) return;
        mStatusAdapterListener.onStatusMenuClick((StatusViewHolder) holder, menuView, position);
    }

    public void setListener(StatusAdapterListener listener) {
        mStatusAdapterListener = listener;
    }

    public void setShowAccountsColor(boolean showAccountsColor) {
        if (mShowAccountsColor == showAccountsColor) return;
        mShowAccountsColor = showAccountsColor;
        notifyDataSetChanged();
    }

    protected abstract void bindStatus(StatusViewHolder holder, int position);

    public interface StatusAdapterListener {
        void onGapClick(GapViewHolder holder, int position);

        void onMediaClick(StatusViewHolder holder, View view, ParcelableMedia media, int position);

        void onStatusActionClick(StatusViewHolder holder, int id, int position);

        void onStatusClick(StatusViewHolder holder, int position);

        boolean onStatusLongClick(StatusViewHolder holder, int position);

        void onStatusMenuClick(StatusViewHolder holder, View menuView, int position);

        void onUserProfileClick(StatusViewHolder holder, ParcelableStatus status, int position);
    }

}
