package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.text.BidiFormatter;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.constant.SharedPreferenceConstants;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.GeneralComponentHelper;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;

import javax.inject.Inject;

/**
 * Created by mariotaku on 16/1/22.
 */
public final class DummyStatusHolderAdapter implements IStatusesAdapter<Object> {

    private final Context context;
    private final SharedPreferencesWrapper preferences;
    private final TwidereLinkify linkify;
    private final MediaLoadingHandler handler;
    @Inject
    MediaLoaderWrapper loader;
    @Inject
    AsyncTwitterWrapper twitter;
    @Inject
    UserColorNameManager manager;
    @Inject
    BidiFormatter formatter;

    private int profileImageStyle;
    private int mediaPreviewStyle;
    private int textSize;
    private int linkHighlightStyle;
    private boolean nameFirst;
    private boolean displayProfileImage;
    private boolean sensitiveContentEnabled;
    private boolean hideCardActions;
    private boolean displayMediaPreview;
    private boolean shouldShowAccountsColor;
    private boolean useStarsForLikes;

    public DummyStatusHolderAdapter(Context context) {
        this(context, new TwidereLinkify(null));
    }

    public DummyStatusHolderAdapter(Context context, TwidereLinkify linkify) {
        GeneralComponentHelper.build(context).inject(this);
        this.context = context;
        preferences = SharedPreferencesWrapper.getInstance(context, TwidereConstants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        handler = new MediaLoadingHandler(R.id.media_preview_progress);
        this.linkify = linkify;
        updateOptions();
    }

    public void setShouldShowAccountsColor(boolean shouldShowAccountsColor) {
        this.shouldShowAccountsColor = shouldShowAccountsColor;
    }

    @NonNull
    @Override
    public MediaLoaderWrapper getMediaLoader() {
        return loader;
    }

    @NonNull
    @Override
    public BidiFormatter getBidiFormatter() {
        return formatter;
    }

    @NonNull
    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public MediaLoadingHandler getMediaLoadingHandler() {
        return handler;
    }

    @Nullable
    @Override
    public IStatusViewHolder.StatusClickListener getStatusClickListener() {
        return null;
    }

    @Nullable
    @Override
    public StatusAdapterListener getStatusAdapterListener() {
        return null;
    }

    @NonNull
    @Override
    public UserColorNameManager getUserColorNameManager() {
        return manager;
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    @Override
    public int getProfileImageStyle() {
        return profileImageStyle;
    }

    @Override
    public int getMediaPreviewStyle() {
        return mediaPreviewStyle;
    }

    @NonNull
    @Override
    public AsyncTwitterWrapper getTwitterWrapper() {
        return twitter;
    }

    @Override
    public float getTextSize() {
        return textSize;
    }

    @Override
    @IndicatorPosition
    public int getLoadMoreIndicatorPosition() {
        return IndicatorPosition.NONE;
    }

    @Override
    public void setLoadMoreIndicatorPosition(@IndicatorPosition int position) {

    }

    @Override
    @IndicatorPosition
    public int getLoadMoreSupportedPosition() {
        return IndicatorPosition.NONE;
    }

    @Override
    public void setLoadMoreSupportedPosition(@IndicatorPosition int supported) {

    }

    @Override
    public ParcelableStatus getStatus(int position) {
        return null;
    }

    @Override
    public int getStatusesCount() {
        return 0;
    }

    @Override
    public long getStatusId(int position) {
        return 0;
    }

    @Override
    public long getAccountId(int position) {
        return 0;
    }

    @Nullable
    @Override
    public ParcelableStatus findStatusById(long accountId, long statusId) {
        return null;
    }

    @Override
    public TwidereLinkify getTwidereLinkify() {
        return linkify;
    }

    @Override
    public boolean isMediaPreviewEnabled() {
        return displayMediaPreview;
    }

    public void setMediaPreviewEnabled(boolean enabled) {
        displayMediaPreview = enabled;
    }

    @Override
    public int getLinkHighlightingStyle() {
        return linkHighlightStyle;
    }

    @Override
    public boolean isNameFirst() {
        return nameFirst;
    }

    @Override
    public boolean isSensitiveContentEnabled() {
        return sensitiveContentEnabled;
    }

    @Override
    public boolean isCardActionsHidden() {
        return hideCardActions;
    }

    @Override
    public void setData(Object o) {

    }

    @Override
    public boolean shouldUseStarsForLikes() {
        return useStarsForLikes;
    }

    public void setUseStarsForLikes(boolean useStarsForLikes) {
        this.useStarsForLikes = useStarsForLikes;
    }

    @Override
    public boolean shouldShowAccountsColor() {
        return shouldShowAccountsColor;
    }

    @Override
    public boolean isGapItem(int position) {
        return false;
    }

    @Override
    public GapClickListener getGapClickListener() {
        return null;
    }

    @Override
    public boolean isProfileImageEnabled() {
        return displayProfileImage;
    }

    public void updateOptions() {
        profileImageStyle = Utils.getProfileImageStyle(preferences.getString(SharedPreferenceConstants.KEY_PROFILE_IMAGE_STYLE, null));
        mediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(SharedPreferenceConstants.KEY_MEDIA_PREVIEW_STYLE, null));
        textSize = preferences.getInt(SharedPreferenceConstants.KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
        nameFirst = preferences.getBoolean(SharedPreferenceConstants.KEY_NAME_FIRST, true);
        displayProfileImage = preferences.getBoolean(SharedPreferenceConstants.KEY_DISPLAY_PROFILE_IMAGE, true);
        displayMediaPreview = preferences.getBoolean(SharedPreferenceConstants.KEY_MEDIA_PREVIEW, false);
        sensitiveContentEnabled = preferences.getBoolean(SharedPreferenceConstants.KEY_DISPLAY_SENSITIVE_CONTENTS, false);
        hideCardActions = preferences.getBoolean(SharedPreferenceConstants.KEY_HIDE_CARD_ACTIONS, false);
        linkHighlightStyle = Utils.getLinkHighlightingStyleInt(preferences.getString(SharedPreferenceConstants.KEY_LINK_HIGHLIGHT_OPTION, null));
        useStarsForLikes = preferences.getBoolean(SharedPreferenceConstants.KEY_I_WANT_MY_STARS_BACK);
    }
}
