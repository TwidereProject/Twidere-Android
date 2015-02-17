package org.mariotaku.twidere.view.holder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.ContentCardClickListener;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableStatus.CursorIndices;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.SimpleValueSerializer;
import org.mariotaku.twidere.util.TwitterCardUtils;
import org.mariotaku.twidere.util.UserColorNameUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.CardMediaContainer;
import org.mariotaku.twidere.view.ShapedImageView;
import org.mariotaku.twidere.view.ShortTimeView;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.util.Locale;

import twitter4j.TranslationResult;

import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;

/**
 * Created by mariotaku on 14/11/19.
 */
public class StatusViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final IStatusesAdapter<?> adapter;

    private final ImageView replyRetweetIcon;
    private final ShapedImageView profileImageView;
    private final ImageView profileTypeView;
    private final ImageView extraTypeView;
    private final TextView textView;
    private final TextView nameView, screenNameView;
    private final TextView replyRetweetView;
    private final ShortTimeView timeView;
    private final CardMediaContainer mediaPreviewContainer;
    private final TextView replyCountView, retweetCountView, favoriteCountView;
    private final IColorLabelView itemContent;

    private StatusClickListener statusClickListener;


    public StatusViewHolder(View itemView) {
        this(null, itemView);
    }

    public StatusViewHolder(IStatusesAdapter<?> adapter, View itemView) {
        super(itemView);
        this.adapter = adapter;
        itemContent = (IColorLabelView) itemView.findViewById(R.id.item_content);
        profileImageView = (ShapedImageView) itemView.findViewById(R.id.profile_image);
        profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
        extraTypeView = (ImageView) itemView.findViewById(R.id.extra_type);
        textView = (TextView) itemView.findViewById(R.id.text);
        nameView = (TextView) itemView.findViewById(R.id.name);
        screenNameView = (TextView) itemView.findViewById(R.id.screen_name);
        replyRetweetIcon = (ImageView) itemView.findViewById(R.id.reply_retweet_icon);
        replyRetweetView = (TextView) itemView.findViewById(R.id.reply_retweet_status);
        timeView = (ShortTimeView) itemView.findViewById(R.id.time);

        mediaPreviewContainer = (CardMediaContainer) itemView.findViewById(R.id.media_preview_container);

        replyCountView = (TextView) itemView.findViewById(R.id.reply_count);
        retweetCountView = (TextView) itemView.findViewById(R.id.retweet_count);
        favoriteCountView = (TextView) itemView.findViewById(R.id.favorite_count);
        //TODO
        // profileImageView.setSelectorColor(ThemeUtils.getUserHighlightColor(itemView.getContext()));
    }

    public void displaySampleStatus() {
        nameView.setText("User");
        screenNameView.setText("@user");
        timeView.setTime(System.currentTimeMillis());
        textView.setText(R.string.sample_status_text);
        mediaPreviewContainer.displayMedia(R.drawable.profile_image_nyan_sakamoto,
                R.drawable.profile_image_nyan_sakamoto_santa);
    }

    public void displayStatus(final ParcelableStatus status, final boolean displayInReplyTo) {
        displayStatus(adapter.getContext(), adapter.getImageLoader(),
                adapter.getImageLoadingHandler(), adapter.getTwitterWrapper(),
                adapter.isMediaPreviewEnabled(), adapter.shouldShowAccountsColor(),
                displayInReplyTo, adapter.isNameFirst(), adapter.isNicknameOnly(),
                adapter.getProfileImageStyle(), adapter.getMediaPreviewStyle(),
                status, null);
    }

    public void displayStatus(@NonNull final Context context,
                              @NonNull final ImageLoaderWrapper loader,
                              @NonNull final ImageLoadingHandler handler,
                              @NonNull final AsyncTwitterWrapper twitter,
                              final boolean displayMediaPreview, final boolean displayAccountsColor,
                              final boolean displayInReplyTo, boolean nameFirst, boolean nicknameOnly,
                              final int profileImageStyle, final int mediaPreviewStyle,
                              @NonNull final ParcelableStatus status,
                              @Nullable final TranslationResult translation) {
        final ParcelableMedia[] media = status.media;

        replyRetweetIcon.setColorFilter(replyRetweetView.getCurrentTextColor(), Mode.SRC_ATOP);
        if (status.retweet_id > 0) {
            final String retweetedBy = UserColorNameUtils.getDisplayName(context, status.retweeted_by_id,
                    status.retweeted_by_name, status.retweeted_by_screen_name, nameFirst, nicknameOnly);
            replyRetweetView.setText(context.getString(R.string.name_retweeted, retweetedBy));
            replyRetweetIcon.setImageResource(R.drawable.ic_activity_action_retweet);
            replyRetweetView.setVisibility(View.VISIBLE);
            replyRetweetIcon.setVisibility(View.VISIBLE);
        } else if (status.in_reply_to_status_id > 0 && status.in_reply_to_user_id > 0 && displayInReplyTo) {
            final String inReplyTo = UserColorNameUtils.getDisplayName(context, status.in_reply_to_user_id,
                    status.in_reply_to_name, status.in_reply_to_screen_name, nameFirst, nicknameOnly);
            replyRetweetView.setText(context.getString(R.string.in_reply_to_name, inReplyTo));
            replyRetweetIcon.setImageResource(R.drawable.ic_activity_action_reply);
            replyRetweetView.setVisibility(View.VISIBLE);
            replyRetweetIcon.setVisibility(View.VISIBLE);
        } else {
            replyRetweetView.setText(null);
            replyRetweetView.setVisibility(View.GONE);
            replyRetweetIcon.setVisibility(View.GONE);
        }

        final int typeIconRes = getUserTypeIconRes(status.user_is_verified, status.user_is_protected);
        if (typeIconRes != 0) {
            profileTypeView.setImageResource(typeIconRes);
            profileTypeView.setVisibility(View.VISIBLE);
        } else {
            profileTypeView.setImageDrawable(null);
            profileTypeView.setVisibility(View.GONE);
        }

        nameView.setText(status.user_name);
        screenNameView.setText("@" + status.user_screen_name);
        timeView.setTime(status.timestamp);

        final int userColor = UserColorNameUtils.getUserColor(context, status.user_id);
        itemContent.drawStart(userColor);

        if (displayAccountsColor) {
            itemContent.drawEnd(Utils.getAccountColor(context, status.account_id));
        } else {
            itemContent.drawEnd();
        }
        profileImageView.setStyle(profileImageStyle);

        loader.displayProfileImage(profileImageView, status.user_profile_image_url);

        if (displayMediaPreview) {
            mediaPreviewContainer.setStyle(mediaPreviewStyle);
            if (media != null && media.length > 0) {
                mediaPreviewContainer.setVisibility(View.VISIBLE);
            } else {
                mediaPreviewContainer.setVisibility(View.GONE);
            }
            mediaPreviewContainer.displayMedia(media, loader, status.account_id, null, handler);
        } else {
            mediaPreviewContainer.setVisibility(View.GONE);
        }
        if (translation != null) {
            textView.setText(translation.getText());
        } else {
            textView.setText(status.text_unescaped);
        }

        if (status.reply_count > 0) {
            replyCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), status.reply_count));
        } else {
            replyCountView.setText(null);
        }

        final long retweet_count;
        if (twitter.isDestroyingStatus(status.account_id, status.my_retweet_id)) {
            retweetCountView.setActivated(false);
            retweet_count = Math.max(0, status.favorite_count - 1);
        } else {
            final boolean creatingRetweet = twitter.isCreatingRetweet(status.account_id, status.id);
            retweetCountView.setActivated(creatingRetweet || Utils.isMyRetweet(status));
            retweet_count = status.retweet_count + (creatingRetweet ? 1 : 0);
        }
        if (retweet_count > 0) {
            retweetCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), retweet_count));
        } else {
            retweetCountView.setText(null);
        }
        retweetCountView.setEnabled(!status.user_is_protected);

        final long favorite_count;
        if (twitter.isDestroyingFavorite(status.account_id, status.id)) {
            favoriteCountView.setActivated(false);
            favorite_count = Math.max(0, status.favorite_count - 1);
        } else {
            final boolean creatingFavorite = twitter.isCreatingFavorite(status.account_id, status.id);
            favoriteCountView.setActivated(creatingFavorite || status.is_favorite);
            favorite_count = status.favorite_count + (creatingFavorite ? 1 : 0);
        }
        if (favorite_count > 0) {
            favoriteCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), favorite_count));
        } else {
            favoriteCountView.setText(null);
        }
        displayExtraTypeIcon(status.card_name, status.media != null ? status.media.length : 0);
    }

    public void displayStatus(@NonNull Cursor cursor, @NonNull CursorIndices indices,
                              final boolean displayInReplyTo) {
        final ImageLoaderWrapper loader = adapter.getImageLoader();
        final AsyncTwitterWrapper twitter = adapter.getTwitterWrapper();
        final Context context = adapter.getContext();
        final boolean nameFirst = adapter.isNameFirst();
        final boolean nicknameOnly = adapter.isNicknameOnly();

        final long reply_count = cursor.getLong(indices.reply_count);
        final long retweet_count;
        final long favorite_count;

        final long account_id = cursor.getLong(indices.account_id);
        final long timestamp = cursor.getLong(indices.status_timestamp);
        final long user_id = cursor.getLong(indices.user_id);
        final long status_id = cursor.getLong(indices.status_id);
        final long retweet_id = cursor.getLong(indices.retweet_id);
        final long my_retweet_id = cursor.getLong(indices.my_retweet_id);
        final long retweeted_by_id = cursor.getLong(indices.retweeted_by_user_id);
        final long in_reply_to_status_id = cursor.getLong(indices.in_reply_to_status_id);
        final long in_reply_to_user_id = cursor.getLong(indices.in_reply_to_user_id);

        final boolean user_is_protected = cursor.getInt(indices.is_protected) == 1;

        final String user_name = cursor.getString(indices.user_name);
        final String user_screen_name = cursor.getString(indices.user_screen_name);
        final String user_profile_image_url = cursor.getString(indices.user_profile_image_url);
        final String retweeted_by_name = cursor.getString(indices.retweeted_by_user_name);
        final String retweeted_by_screen_name = cursor.getString(indices.retweeted_by_user_screen_name);
        final String in_reply_to_name = cursor.getString(indices.in_reply_to_user_name);
        final String in_reply_to_screen_name = cursor.getString(indices.in_reply_to_user_screen_name);
        final String card_name = cursor.getString(indices.card_name);

        final ParcelableMedia[] media = SimpleValueSerializer.fromSerializedString(cursor.getString(indices.media), ParcelableMedia.SIMPLE_CREATOR);

        if (retweet_id > 0) {
            final String retweetedBy = UserColorNameUtils.getDisplayName(context, retweeted_by_id,
                    retweeted_by_name, retweeted_by_screen_name, nameFirst, nicknameOnly);
            replyRetweetView.setText(context.getString(R.string.name_retweeted, retweetedBy));
            replyRetweetIcon.setImageResource(R.drawable.ic_activity_action_retweet);
            replyRetweetView.setVisibility(View.VISIBLE);
            replyRetweetIcon.setVisibility(View.VISIBLE);
        } else if (in_reply_to_status_id > 0 && in_reply_to_user_id > 0 && displayInReplyTo) {
            final String inReplyTo = UserColorNameUtils.getDisplayName(context, in_reply_to_user_id,
                    in_reply_to_name, in_reply_to_screen_name, nameFirst, nicknameOnly);
            replyRetweetView.setText(context.getString(R.string.in_reply_to_name, inReplyTo));
            replyRetweetIcon.setImageResource(R.drawable.ic_activity_action_reply);
            replyRetweetView.setVisibility(View.VISIBLE);
            replyRetweetIcon.setVisibility(View.VISIBLE);
        } else {
            replyRetweetView.setVisibility(View.GONE);
            replyRetweetIcon.setVisibility(View.GONE);
        }

        final int typeIconRes = getUserTypeIconRes(cursor.getInt(indices.is_verified) == 1,
                user_is_protected);
        if (typeIconRes != 0) {
            profileTypeView.setImageResource(typeIconRes);
            profileTypeView.setVisibility(View.VISIBLE);
        } else {
            profileTypeView.setImageDrawable(null);
            profileTypeView.setVisibility(View.GONE);
        }

        nameView.setText(user_name);
        screenNameView.setText("@" + user_screen_name);
        timeView.setTime(timestamp);

        final int userColor = UserColorNameUtils.getUserColor(context, user_id);
        itemContent.drawStart(userColor);

        if (adapter.shouldShowAccountsColor()) {
            itemContent.drawEnd(Utils.getAccountColor(context, account_id));
        } else {
            itemContent.drawEnd();
        }

        profileImageView.setStyle(adapter.getProfileImageStyle());

        loader.displayProfileImage(profileImageView, user_profile_image_url);

        final String text_unescaped = cursor.getString(indices.text_unescaped);
        if (adapter.isMediaPreviewEnabled()) {
            mediaPreviewContainer.setStyle(adapter.getMediaPreviewStyle());
            mediaPreviewContainer.setVisibility(media != null && media.length > 0 ? View.VISIBLE : View.GONE);
            mediaPreviewContainer.displayMedia(media, loader, account_id, null,
                    adapter.getImageLoadingHandler());
        } else {
            mediaPreviewContainer.setVisibility(View.GONE);
        }
        textView.setText(text_unescaped);

        if (reply_count > 0) {
            replyCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), reply_count));
        } else {
            replyCountView.setText(null);
        }

        if (twitter.isDestroyingStatus(account_id, my_retweet_id)) {
            retweetCountView.setActivated(false);
            retweet_count = Math.max(0, cursor.getLong(indices.retweet_count) - 1);
        } else {
            final boolean creatingRetweet = twitter.isCreatingRetweet(account_id, status_id);
            retweetCountView.setActivated(creatingRetweet || Utils.isMyRetweet(account_id,
                    retweeted_by_id, my_retweet_id));
            retweet_count = cursor.getLong(indices.retweet_count) + (creatingRetweet ? 1 : 0);
        }
        if (retweet_count > 0) {
            retweetCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), retweet_count));
        } else {
            retweetCountView.setText(null);
        }
        retweetCountView.setEnabled(!user_is_protected);

        favoriteCountView.setActivated(cursor.getInt(indices.is_favorite) == 1);
        if (twitter.isDestroyingFavorite(account_id, status_id)) {
            favoriteCountView.setActivated(false);
            favorite_count = Math.max(0, cursor.getLong(indices.favorite_count) - 1);
        } else {
            final boolean creatingFavorite = twitter.isCreatingFavorite(account_id, status_id);
            favoriteCountView.setActivated(creatingFavorite || cursor.getInt(indices.is_favorite) == 1);
            favorite_count = cursor.getLong(indices.favorite_count) + (creatingFavorite ? 1 : 0);
        }
        if (favorite_count > 0) {
            favoriteCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), favorite_count));
        } else {
            favoriteCountView.setText(null);
        }
        displayExtraTypeIcon(card_name, media != null ? media.length : 0);
    }

    public CardView getCardView() {
        return (CardView) itemView.findViewById(R.id.card);
    }

    public ShapedImageView getProfileImageView() {
        return profileImageView;
    }

    public ImageView getProfileTypeView() {
        return profileTypeView;
    }

    @Override
    public void onClick(View v) {
        if (statusClickListener == null) return;
        final int position = getPosition();
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

    public void setOnClickListeners() {
        setStatusClickListener(adapter);
    }

    public void setStatusClickListener(StatusClickListener listener) {
        statusClickListener = listener;
        itemView.findViewById(R.id.item_content).setOnClickListener(this);
        itemView.findViewById(R.id.item_menu).setOnClickListener(this);

        itemView.setOnClickListener(this);
        profileImageView.setOnClickListener(this);
        mediaPreviewContainer.setOnClickListener(this);
        replyCountView.setOnClickListener(this);
        retweetCountView.setOnClickListener(this);
        favoriteCountView.setOnClickListener(this);
    }

    public void setTextSize(final float textSize) {
        nameView.setTextSize(textSize);
        textView.setTextSize(textSize);
        screenNameView.setTextSize(textSize * 0.85f);
        timeView.setTextSize(textSize * 0.85f);
        replyRetweetView.setTextSize(textSize * 0.75f);
        replyCountView.setTextSize(textSize);
        replyCountView.setTextSize(textSize);
        favoriteCountView.setTextSize(textSize);
    }

    public void setupViewOptions() {
        setTextSize(adapter.getTextSize());
    }

    private void displayExtraTypeIcon(String cardName, int mediaLength) {
        if (TwitterCardUtils.CARD_NAME_AUDIO.equals(cardName)) {
            extraTypeView.setImageResource(R.drawable.ic_action_music);
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (TwitterCardUtils.CARD_NAME_ANIMATED_GIF.equals(cardName)) {
            extraTypeView.setImageResource(R.drawable.ic_action_movie);
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (TwitterCardUtils.CARD_NAME_PLAYER.equals(cardName)) {
            extraTypeView.setImageResource(R.drawable.ic_action_play_circle);
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (mediaLength > 0) {
            extraTypeView.setImageResource(R.drawable.ic_action_gallery);
            extraTypeView.setVisibility(View.VISIBLE);
        } else {
            extraTypeView.setVisibility(View.GONE);
        }
    }

    public static interface StatusClickListener extends ContentCardClickListener {

        void onStatusClick(StatusViewHolder holder, int position);

        void onUserProfileClick(StatusViewHolder holder, int position);
    }
}
