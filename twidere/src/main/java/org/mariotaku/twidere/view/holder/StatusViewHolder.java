package org.mariotaku.twidere.view.holder;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.ContentCardClickListener;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.api.twitter.model.TranslationResult;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.MediaLoadingHandler;
import org.mariotaku.twidere.util.SharedPreferencesWrapper;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwitterCardUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.dagger.ApplicationModule;
import org.mariotaku.twidere.util.dagger.DaggerGeneralComponent;
import org.mariotaku.twidere.view.CardMediaContainer;
import org.mariotaku.twidere.view.CardMediaContainer.OnMediaClickListener;
import org.mariotaku.twidere.view.ForegroundColorView;
import org.mariotaku.twidere.view.NameView;
import org.mariotaku.twidere.view.ShortTimeView;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.util.Locale;

import javax.inject.Inject;

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;

/**
 * IDE gives me warning if I don't change default comment, so I wrote this XD
 * <p/>
 * Created by mariotaku on 14/11/19.
 */
public class StatusViewHolder extends ViewHolder implements Constants, OnClickListener,
        OnMediaClickListener, OnLongClickListener {

    @NonNull
    private final IStatusesAdapter<?> adapter;

    private final ImageView replyRetweetIcon;
    private final ImageView profileImageView;
    private final ImageView profileTypeView;
    private final ImageView extraTypeView;
    private final TextView textView;
    private final TextView quotedTextView;
    private final NameView nameView;
    private final NameView quotedNameView;
    private final TextView replyRetweetView;
    private final ShortTimeView timeView;
    private final CardMediaContainer mediaPreview;
    private final TextView replyCountView, retweetCountView, favoriteCountView;
    private final IColorLabelView itemContent;
    private final ForegroundColorView quoteIndicator;
    private final View actionButtons;
    private final View itemMenu;

    private StatusClickListener statusClickListener;

    public StatusViewHolder(@NonNull final IStatusesAdapter<?> adapter, @NonNull final View itemView) {
        super(itemView);
        this.adapter = adapter;
        itemContent = (IColorLabelView) itemView.findViewById(R.id.item_content);
        profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
        profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
        extraTypeView = (ImageView) itemView.findViewById(R.id.extra_type);
        textView = (TextView) itemView.findViewById(R.id.text);
        quotedTextView = (TextView) itemView.findViewById(R.id.quoted_text);
        nameView = (NameView) itemView.findViewById(R.id.name);
        quotedNameView = (NameView) itemView.findViewById(R.id.quoted_name);
        replyRetweetIcon = (ImageView) itemView.findViewById(R.id.reply_retweet_icon);
        replyRetweetView = (TextView) itemView.findViewById(R.id.reply_retweet_status);
        timeView = (ShortTimeView) itemView.findViewById(R.id.time);

        mediaPreview = (CardMediaContainer) itemView.findViewById(R.id.media_preview);

        quoteIndicator = (ForegroundColorView) itemView.findViewById(R.id.quote_indicator);

        itemMenu = itemView.findViewById(R.id.item_menu);
        actionButtons = itemView.findViewById(R.id.action_buttons);

        replyCountView = (TextView) itemView.findViewById(R.id.reply_count);
        retweetCountView = (TextView) itemView.findViewById(R.id.retweet_count);
        favoriteCountView = (TextView) itemView.findViewById(R.id.favorite_count);
        //TODO
        // profileImageView.setSelectorColor(ThemeUtils.getUserHighlightColor(itemView.getContext()));

        if (adapter.isMediaPreviewEnabled()) {
            View.inflate(mediaPreview.getContext(), R.layout.layout_card_media_preview, mediaPreview);
        }
    }

    public void displaySampleStatus() {
        profileImageView.setVisibility(adapter.isProfileImageEnabled() ? View.VISIBLE : View.GONE);
        profileImageView.setImageResource(R.mipmap.ic_launcher);
        nameView.setName(TWIDERE_PREVIEW_NAME);
        nameView.setScreenName("@" + TWIDERE_PREVIEW_SCREEN_NAME);
        nameView.updateText();
        if (adapter.getLinkHighlightingStyle() == VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
            textView.setText(Html.fromHtml(TWIDERE_PREVIEW_TEXT_HTML));
            adapter.getTwidereLinkify().applyAllLinks(textView, -1, -1, false, adapter.getLinkHighlightingStyle());
        } else {
            textView.setText(toPlainText(TWIDERE_PREVIEW_TEXT_HTML));
        }
        textView.setMovementMethod(null);
        timeView.setTime(System.currentTimeMillis());
        mediaPreview.setVisibility(adapter.isMediaPreviewEnabled() ? View.VISIBLE : View.GONE);
        mediaPreview.displayMedia(R.drawable.nyan_stars_background);
        extraTypeView.setImageResource(R.drawable.ic_action_gallery);
    }

    public void displayStatus(final ParcelableStatus status, final boolean displayInReplyTo) {
        displayStatus(status, null, displayInReplyTo, true);
    }

    public void displayStatus(@NonNull final ParcelableStatus status, @Nullable final TranslationResult translation,
                              final boolean displayInReplyTo, final boolean shouldDisplayExtraType) {
        final MediaLoaderWrapper loader = adapter.getMediaLoader();
        final AsyncTwitterWrapper twitter = adapter.getTwitterWrapper();
        final TwidereLinkify linkify = adapter.getTwidereLinkify();
        final UserColorNameManager manager = adapter.getUserColorNameManager();
        final Context context = adapter.getContext();
        final boolean nameFirst = adapter.isNameFirst();

        final long reply_count = status.reply_count;
        final long retweet_count;
        final long favorite_count;

        if (status.retweet_id > 0) {
            final String retweetedBy = manager.getDisplayName(status.retweeted_by_user_id,
                    status.retweeted_by_user_name, status.retweeted_by_user_screen_name, nameFirst, false);
            replyRetweetView.setText(context.getString(R.string.name_retweeted, retweetedBy));
            replyRetweetIcon.setImageResource(R.drawable.ic_activity_action_retweet);
            replyRetweetView.setVisibility(View.VISIBLE);
            replyRetweetIcon.setVisibility(View.VISIBLE);
        } else if (status.in_reply_to_status_id > 0 && status.in_reply_to_user_id > 0 && displayInReplyTo) {
            final String inReplyTo = manager.getDisplayName(status.in_reply_to_user_id,
                    status.in_reply_to_name, status.in_reply_to_screen_name, nameFirst, false);
            replyRetweetView.setText(context.getString(R.string.in_reply_to_name, inReplyTo));
            replyRetweetIcon.setImageResource(R.drawable.ic_activity_action_reply);
            replyRetweetView.setVisibility(View.VISIBLE);
            replyRetweetIcon.setVisibility(View.VISIBLE);
        } else {
            replyRetweetView.setVisibility(View.GONE);
            replyRetweetIcon.setVisibility(View.GONE);
        }


        if (status.is_quote && ArrayUtils.isEmpty(status.media)) {

            quotedNameView.setVisibility(View.VISIBLE);
            quotedTextView.setVisibility(View.VISIBLE);
            quoteIndicator.setVisibility(View.VISIBLE);

            quotedNameView.setName(manager.getUserNickname(status.quoted_user_id, status.quoted_user_name, false));
            quotedNameView.setScreenName("@" + status.quoted_user_screen_name);

            if (adapter.getLinkHighlightingStyle() != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE
                    && !TextUtils.isEmpty(status.quoted_text_html)) {
                final Spanned text = Html.fromHtml(status.quoted_text_html);
                quotedTextView.setText(text);
                linkify.applyAllLinks(quotedTextView, status.account_id, getLayoutPosition(),
                        status.is_possibly_sensitive, adapter.getLinkHighlightingStyle());
                quotedTextView.setMovementMethod(null);
            } else {
                final String text = status.quoted_text_unescaped;
                quotedTextView.setText(text);
            }

            quoteIndicator.setColor(manager.getUserColor(status.user_id, false));
            itemContent.drawStart(manager.getUserColor(status.quoted_user_id, false),
                    manager.getUserColor(status.user_id, false));
        } else {

            quotedNameView.setVisibility(View.GONE);
            quotedTextView.setVisibility(View.GONE);
            quoteIndicator.setVisibility(View.GONE);

            if (status.is_retweet) {
                itemContent.drawStart(manager.getUserColor(status.retweeted_by_user_id, false),
                        manager.getUserColor(status.user_id, false));
            } else {
                itemContent.drawStart(manager.getUserColor(status.user_id, false));
            }
        }

        if (status.is_retweet) {
            timeView.setTime(status.retweet_timestamp);
        } else {
            timeView.setTime(status.timestamp);
        }
        nameView.setName(manager.getUserNickname(status.user_id, status.user_name, false));
        nameView.setScreenName("@" + status.user_screen_name);


        if (adapter.isProfileImageEnabled()) {
            profileImageView.setVisibility(View.VISIBLE);
            final String user_profile_image_url = status.user_profile_image_url;

            loader.displayProfileImage(profileImageView, user_profile_image_url);

            profileTypeView.setImageResource(getUserTypeIconRes(status.user_is_verified, status.user_is_protected));
            profileTypeView.setVisibility(View.VISIBLE);
        } else {
            profileTypeView.setVisibility(View.GONE);
            profileImageView.setVisibility(View.GONE);

            loader.cancelDisplayTask(profileImageView);

            profileTypeView.setImageDrawable(null);
            profileTypeView.setVisibility(View.GONE);
        }

        if (adapter.shouldShowAccountsColor()) {
            itemContent.drawEnd(Utils.getAccountColor(context, status.account_id));
        } else {
            itemContent.drawEnd();
        }

        final ParcelableMedia[] media = Utils.getPrimaryMedia(status);

        if (adapter.isMediaPreviewEnabled()) {
            mediaPreview.setStyle(adapter.getMediaPreviewStyle());
            final boolean hasMedia = !ArrayUtils.isEmpty(media);
            if (hasMedia && (adapter.isSensitiveContentEnabled() || !status.is_possibly_sensitive)) {
                mediaPreview.setVisibility(View.VISIBLE);
                mediaPreview.displayMedia(media, loader, status.account_id, this,
                        adapter.getMediaLoadingHandler());
            } else {
                mediaPreview.setVisibility(View.GONE);
            }
        } else {
            mediaPreview.setVisibility(View.GONE);
        }
        if (translation != null) {
            textView.setText(translation.getText());
        } else if (adapter.getLinkHighlightingStyle() == VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
            textView.setText(status.text_unescaped);
        } else {
            textView.setText(Html.fromHtml(status.text_html));
            linkify.applyAllLinks(textView, status.account_id, getLayoutPosition(),
                    status.is_possibly_sensitive,
                    adapter.getLinkHighlightingStyle());
            textView.setMovementMethod(null);
        }

        final Locale locale = Locale.getDefault();
        if (reply_count > 0) {
            replyCountView.setText(Utils.getLocalizedNumber(locale, reply_count));
        } else {
            replyCountView.setText(null);
        }

        if (twitter.isDestroyingStatus(status.account_id, status.my_retweet_id)) {
            retweetCountView.setActivated(false);
            retweet_count = Math.max(0, status.retweet_count - 1);
        } else {
            final boolean creatingRetweet = twitter.isCreatingRetweet(status.account_id, status.id);
            retweetCountView.setActivated(creatingRetweet || Utils.isMyRetweet(status.account_id,
                    status.retweeted_by_user_id, status.my_retweet_id));
            retweet_count = status.retweet_count + (creatingRetweet ? 1 : 0);
        }
        if (retweet_count > 0) {
            retweetCountView.setText(Utils.getLocalizedNumber(locale, retweet_count));
        } else {
            retweetCountView.setText(null);
        }
        if (twitter.isDestroyingFavorite(status.account_id, status.id)) {
            favoriteCountView.setActivated(false);
            favorite_count = Math.max(0, status.favorite_count - 1);
        } else {
            final boolean creatingFavorite = twitter.isCreatingFavorite(status.account_id, status.id);
            favoriteCountView.setActivated(creatingFavorite || status.is_favorite);
            favorite_count = status.favorite_count + (creatingFavorite ? 1 : 0);
        }
        if (favorite_count > 0) {
            favoriteCountView.setText(Utils.getLocalizedNumber(locale, favorite_count));
        } else {
            favoriteCountView.setText(null);
        }
        if (shouldDisplayExtraType) {
            displayExtraTypeIcon(status.card_name, media, status.location, status.place_full_name,
                    status.is_possibly_sensitive);
        } else {
            extraTypeView.setVisibility(View.GONE);
        }

        nameView.updateText();
        quotedNameView.updateText();
    }

    public ImageView getProfileImageView() {
        return profileImageView;
    }

    public ImageView getProfileTypeView() {
        return profileTypeView;
    }

    @Override
    public void onClick(View v) {
        if (statusClickListener == null) return;
        final int position = getLayoutPosition();
        switch (v.getId()) {
            case R.id.item_content: {
                statusClickListener.onStatusClick(this, position);
                break;
            }
            case R.id.item_menu: {
                statusClickListener.onItemMenuClick(this, v, position);
                break;
            }
            case R.id.profile_image: {
                statusClickListener.onUserProfileClick(this, position);
                break;
            }
            case R.id.reply_count:
            case R.id.retweet_count:
            case R.id.favorite_count: {
                statusClickListener.onItemActionClick(this, v.getId(), position);
                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (statusClickListener == null) return false;
        final int position = getLayoutPosition();
        switch (v.getId()) {
            case R.id.item_content: {
                return statusClickListener.onStatusLongClick(this, position);
            }
        }
        return false;
    }

    @Override
    public void onMediaClick(View view, ParcelableMedia media, long accountId) {
        if (statusClickListener == null) return;
        final int position = getLayoutPosition();
        statusClickListener.onMediaClick(this, view, media, position);
    }

    public void setOnClickListeners() {
        setStatusClickListener(adapter);
    }

    public void setStatusClickListener(StatusClickListener listener) {
        statusClickListener = listener;
        ((View) itemContent).setOnClickListener(this);
        ((View) itemContent).setOnLongClickListener(this);

        itemMenu.setOnClickListener(this);
        profileImageView.setOnClickListener(this);
        replyCountView.setOnClickListener(this);
        retweetCountView.setOnClickListener(this);
        favoriteCountView.setOnClickListener(this);
    }

    public void setTextSize(final float textSize) {
        nameView.setPrimaryTextSize(textSize);
        quotedNameView.setPrimaryTextSize(textSize);
        textView.setTextSize(textSize);
        quotedTextView.setTextSize(textSize);
        nameView.setSecondaryTextSize(textSize * 0.85f);
        quotedNameView.setSecondaryTextSize(textSize * 0.85f);
        timeView.setTextSize(textSize * 0.85f);
        replyRetweetView.setTextSize(textSize * 0.75f);
        replyCountView.setTextSize(textSize);
        retweetCountView.setTextSize(textSize);
        favoriteCountView.setTextSize(textSize);
    }

    public void setupViewOptions() {
        setTextSize(adapter.getTextSize());
        mediaPreview.setStyle(adapter.getMediaPreviewStyle());
//        profileImageView.setStyle(adapter.getProfileImageStyle());
        actionButtons.setVisibility(adapter.isCardActionsHidden() ? View.GONE : View.VISIBLE);
        itemMenu.setVisibility(adapter.isCardActionsHidden() ? View.GONE : View.VISIBLE);

        final boolean nameFirst = adapter.isNameFirst();
        nameView.setNameFirst(nameFirst);
        quotedNameView.setNameFirst(nameFirst);
    }

    private void displayExtraTypeIcon(String cardName, ParcelableMedia[] media, ParcelableLocation location, String placeFullName, boolean sensitive) {
        if (TwitterCardUtils.CARD_NAME_AUDIO.equals(cardName)) {
            extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_music);
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (TwitterCardUtils.CARD_NAME_ANIMATED_GIF.equals(cardName)) {
            extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_movie);
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (TwitterCardUtils.CARD_NAME_PLAYER.equals(cardName)) {
            extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_play_circle);
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (media != null && media.length > 0) {
            if (hasVideo(media)) {
                extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_movie);
            } else {
                extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_gallery);
            }
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (ParcelableLocation.isValidLocation(location) || !TextUtils.isEmpty(placeFullName)) {
            extraTypeView.setImageResource(R.drawable.ic_action_location);
            extraTypeView.setVisibility(View.VISIBLE);
        } else {
            extraTypeView.setVisibility(View.GONE);
        }
    }

    private boolean hasVideo(ParcelableMedia[] media) {
        for (ParcelableMedia mediaItem : media) {
            if (mediaItem.type == ParcelableMedia.TYPE_VIDEO
                    || mediaItem.type == ParcelableMedia.TYPE_ANIMATED_GIF)
                return true;
        }
        return false;
    }

    public interface StatusClickListener extends ContentCardClickListener {

        void onMediaClick(StatusViewHolder holder, View view, ParcelableMedia media, int position);

        void onStatusClick(StatusViewHolder holder, int position);

        boolean onStatusLongClick(StatusViewHolder holder, int position);

        void onUserProfileClick(StatusViewHolder holder, int position);
    }

    public static final class DummyStatusHolderAdapter implements IStatusesAdapter<Object> {

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

        public DummyStatusHolderAdapter(Context context) {
            DaggerGeneralComponent.builder().applicationModule(ApplicationModule.get(context)).build().inject(this);
            this.context = context;
            preferences = SharedPreferencesWrapper.getInstance(context, SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
            final TwidereApplication app = TwidereApplication.getInstance(context);
            handler = new MediaLoadingHandler(R.id.media_preview_progress);
            linkify = new TwidereLinkify(null);
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
        public Context getContext() {
            return context;
        }

        @Override
        public MediaLoadingHandler getMediaLoadingHandler() {
            return handler;
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
        public boolean isLoadMoreIndicatorVisible() {
            return false;
        }

        @Override
        public void setLoadMoreIndicatorVisible(boolean enabled) {

        }

        @Override
        public boolean isLoadMoreSupported() {
            return false;
        }

        @Override
        public void setLoadMoreSupported(boolean supported) {

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
        public boolean shouldShowAccountsColor() {
            return shouldShowAccountsColor;
        }

        @Override
        public boolean isGapItem(int position) {
            return false;
        }

        @Override
        public void onGapClick(ViewHolder holder, int position) {

        }

        @Override
        public boolean isProfileImageEnabled() {
            return displayProfileImage;
        }

        @Override
        public void onStatusClick(StatusViewHolder holder, int position) {

        }

        @Override
        public boolean onStatusLongClick(StatusViewHolder holder, int position) {
            return false;
        }

        @Override
        public void onMediaClick(StatusViewHolder holder, View view, ParcelableMedia media, int position) {

        }

        @Override
        public void onUserProfileClick(StatusViewHolder holder, int position) {

        }

        @Override
        public void onItemActionClick(ViewHolder holder, int id, int position) {

        }

        @Override
        public void onItemMenuClick(ViewHolder holder, View menuView, int position) {

        }

        public void updateOptions() {
            profileImageStyle = Utils.getProfileImageStyle(preferences.getString(KEY_PROFILE_IMAGE_STYLE, null));
            mediaPreviewStyle = Utils.getMediaPreviewStyle(preferences.getString(KEY_MEDIA_PREVIEW_STYLE, null));
            textSize = preferences.getInt(KEY_TEXT_SIZE, context.getResources().getInteger(R.integer.default_text_size));
            nameFirst = preferences.getBoolean(KEY_NAME_FIRST, true);
            displayProfileImage = preferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true);
            displayMediaPreview = preferences.getBoolean(KEY_MEDIA_PREVIEW, false);
            sensitiveContentEnabled = preferences.getBoolean(KEY_DISPLAY_SENSITIVE_CONTENTS, false);
            hideCardActions = preferences.getBoolean(KEY_HIDE_CARD_ACTIONS, false);
            linkHighlightStyle = Utils.getLinkHighlightingStyleInt(preferences.getString(KEY_LINK_HIGHLIGHT_OPTION, null));
        }
    }
}
