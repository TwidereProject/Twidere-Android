package org.mariotaku.twidere.view.holder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.text.BidiFormatter;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang3.ArrayUtils;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.graphic.like.LikeAnimationDrawable;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableLocationUtils;
import org.mariotaku.twidere.model.util.ParcelableStatusUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.HtmlSpanBuilder;
import org.mariotaku.twidere.util.MediaLoaderWrapper;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwitterCardUtils;
import org.mariotaku.twidere.util.UnitConvertUtils;
import org.mariotaku.twidere.util.UserColorNameManager;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.ActionIconThemedTextView;
import org.mariotaku.twidere.view.CardMediaContainer;
import org.mariotaku.twidere.view.ForegroundColorView;
import org.mariotaku.twidere.view.IconActionView;
import org.mariotaku.twidere.view.NameView;
import org.mariotaku.twidere.view.ShortTimeView;
import org.mariotaku.twidere.view.holder.iface.IStatusViewHolder;
import org.mariotaku.twidere.view.iface.IColorLabelView;

import java.lang.ref.WeakReference;

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;

/**
 * IDE gives me warning if I don't change default comment, so I wrote this XD
 * <p>
 * Created by mariotaku on 14/11/19.
 */
public class StatusViewHolder extends ViewHolder implements Constants, IStatusViewHolder {

    @NonNull
    private final IStatusesAdapter<?> adapter;

    private final ImageView statusInfoIcon;
    private final ImageView profileImageView;
    private final ImageView profileTypeView;
    private final ImageView extraTypeView;
    private final TextView textView;
    private final TextView quotedTextView;
    private final NameView nameView;
    private final NameView quotedNameView;
    private final TextView statusInfoLabel;
    private final ShortTimeView timeView;
    private final CardMediaContainer mediaPreview, quoteMediaPreview;
    private final View mediaLabel, quoteMediaLabel;
    private final TextView mediaLabelText, quoteMediaLabelText;
    private final IconActionView replyIconView, retweetIconView, favoriteIconView;
    private final TextView replyCountView, retweetCountView, favoriteCountView;
    private final View replyView, retweetView, favoriteView;
    private final IColorLabelView itemContent;
    private final ForegroundColorView quoteIndicator;
    private final View actionButtons;
    private final View itemMenu;
    private final View profileImageSpace;
    private final View statusContentUpperSpace, statusContentLowerSpace;
    private final View mediaLabelSpace = null, quotedMediaLabelSpace = null;
    private final View quoteIndicatorAnchorTop, quoteIndicatorAnchorBottom;
    private final EventListener eventListener;

    private StatusClickListener statusClickListener;

    public StatusViewHolder(@NonNull final IStatusesAdapter<?> adapter, @NonNull final View itemView) {
        super(itemView);
        this.adapter = adapter;
        this.eventListener = new EventListener(this);
        itemContent = (IColorLabelView) itemView.findViewById(R.id.item_content);
        profileImageView = (ImageView) itemView.findViewById(R.id.profile_image);
        profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
        extraTypeView = (ImageView) itemView.findViewById(R.id.extra_type);
        textView = (TextView) itemView.findViewById(R.id.text);
        quotedTextView = (TextView) itemView.findViewById(R.id.quoted_text);
        nameView = (NameView) itemView.findViewById(R.id.name);
        quotedNameView = (NameView) itemView.findViewById(R.id.quoted_name);
        statusInfoIcon = (ImageView) itemView.findViewById(R.id.status_info_icon);
        statusInfoLabel = (TextView) itemView.findViewById(R.id.status_info_label);
        timeView = (ShortTimeView) itemView.findViewById(R.id.time);
        profileImageSpace = itemView.findViewById(R.id.profile_image_space);

        mediaPreview = (CardMediaContainer) itemView.findViewById(R.id.media_preview);
        quoteMediaPreview = (CardMediaContainer) itemView.findViewById(R.id.quoted_media_preview);
        mediaLabel = itemView.findViewById(R.id.media_label);
        quoteMediaLabel = itemView.findViewById(R.id.quoted_media_label);
        mediaLabelText = (TextView) itemView.findViewById(R.id.media_label_text);
        quoteMediaLabelText = (TextView) itemView.findViewById(R.id.quoted_media_label_text);

        quoteIndicator = (ForegroundColorView) itemView.findViewById(R.id.quote_indicator);

        itemMenu = itemView.findViewById(R.id.item_menu);
        actionButtons = itemView.findViewById(R.id.action_buttons);

        replyView = itemView.findViewById(R.id.reply);
        retweetView = itemView.findViewById(R.id.retweet);
        favoriteView = itemView.findViewById(R.id.favorite);

        replyIconView = (IconActionView) itemView.findViewById(R.id.reply_icon);
        retweetIconView = (IconActionView) itemView.findViewById(R.id.retweet_icon);
        favoriteIconView = (IconActionView) itemView.findViewById(R.id.favorite_icon);

        replyCountView = (ActionIconThemedTextView) itemView.findViewById(R.id.reply_count);
        retweetCountView = (ActionIconThemedTextView) itemView.findViewById(R.id.retweet_count);
        favoriteCountView = (ActionIconThemedTextView) itemView.findViewById(R.id.favorite_count);

        statusContentUpperSpace = itemView.findViewById(R.id.status_content_upper_space);
        statusContentLowerSpace = itemView.findViewById(R.id.status_content_lower_space);

        quoteIndicatorAnchorTop = itemView.findViewById(R.id.quote_indicator_anchor_top);
        quoteIndicatorAnchorBottom = itemView.findViewById(R.id.quote_indicator_anchor_bottom);

        //TODO
        // profileImageView.setSelectorColor(ThemeUtils.getUserHighlightColor(itemView.getContext()));

        if (adapter.isMediaPreviewEnabled()) {
            View.inflate(mediaPreview.getContext(), R.layout.layout_card_media_preview, mediaPreview);
            View.inflate(quoteMediaPreview.getContext(), R.layout.layout_card_media_preview, quoteMediaPreview);
        }
    }

    public void displaySampleStatus() {
        final boolean profileImageEnabled = adapter.isProfileImageEnabled();
        profileImageView.setVisibility(profileImageEnabled ? View.VISIBLE : View.GONE);
        if (profileImageSpace != null) {
            profileImageSpace.setVisibility(profileImageEnabled ? View.VISIBLE : View.GONE);
        }
        if (statusContentUpperSpace != null) {
            statusContentUpperSpace.setVisibility(View.VISIBLE);
        }

        profileImageView.setImageResource(R.mipmap.ic_launcher);
        nameView.setName(TWIDERE_PREVIEW_NAME);
        nameView.setScreenName("@" + TWIDERE_PREVIEW_SCREEN_NAME);
        nameView.updateText(adapter.getBidiFormatter());
        if (adapter.getLinkHighlightingStyle() == VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
            final TwidereLinkify linkify = adapter.getTwidereLinkify();
            final Spannable text = HtmlSpanBuilder.fromHtml(TWIDERE_PREVIEW_TEXT_HTML);
            linkify.applyAllLinks(text, null, -1, false, adapter.getLinkHighlightingStyle(), true);
            textView.setText(text);
        } else {
            textView.setText(toPlainText(TWIDERE_PREVIEW_TEXT_HTML));
        }
        timeView.setTime(System.currentTimeMillis());
        final boolean showCardActions = isCardActionsShown();
        if (adapter.isMediaPreviewEnabled()) {
            mediaPreview.setVisibility(View.VISIBLE);
            mediaLabel.setVisibility(View.GONE);
        } else {
            mediaPreview.setVisibility(View.GONE);
            mediaLabel.setVisibility(View.VISIBLE);
        }
        quoteMediaLabel.setVisibility(View.GONE);
        actionButtons.setVisibility(showCardActions ? View.VISIBLE : View.GONE);
        itemMenu.setVisibility(showCardActions ? View.VISIBLE : View.GONE);
        if (statusContentLowerSpace != null) {
            statusContentLowerSpace.setVisibility(showCardActions ? View.GONE : View.VISIBLE);
        }
        quoteMediaPreview.setVisibility(View.GONE);
        mediaPreview.displayMedia(R.drawable.nyan_stars_background);
        extraTypeView.setImageResource(R.drawable.ic_action_gallery);
    }

    @Override
    public void displayStatus(final ParcelableStatus status, final boolean displayInReplyTo) {
        displayStatus(status, displayInReplyTo, true);
    }

    @Override
    public void displayStatus(@NonNull final ParcelableStatus status, final boolean displayInReplyTo,
                              final boolean shouldDisplayExtraType) {

        final MediaLoaderWrapper loader = adapter.getMediaLoader();
        final AsyncTwitterWrapper twitter = adapter.getTwitterWrapper();
        final TwidereLinkify linkify = adapter.getTwidereLinkify();
        final BidiFormatter formatter = adapter.getBidiFormatter();
        final Context context = adapter.getContext();
        final boolean nameFirst = adapter.isNameFirst();
        final boolean showCardActions = isCardActionsShown();

        actionButtons.setVisibility(showCardActions ? View.VISIBLE : View.GONE);
        itemMenu.setVisibility(showCardActions ? View.VISIBLE : View.GONE);
        if (statusContentLowerSpace != null) {
            statusContentLowerSpace.setVisibility(showCardActions ? View.GONE : View.VISIBLE);
        }

        final long replyCount = status.reply_count;
        final long retweetCount;
        final long favoriteCount;

        if (TwitterCardUtils.isPoll(status)) {
            statusInfoLabel.setText(R.string.label_poll);
            statusInfoIcon.setImageResource(R.drawable.ic_activity_action_poll);
            statusInfoLabel.setVisibility(View.VISIBLE);
            statusInfoIcon.setVisibility(View.VISIBLE);

            if (statusContentUpperSpace != null) {
                statusContentUpperSpace.setVisibility(View.GONE);
            }
        } else if (status.retweet_id != null) {
            final String retweetedBy = UserColorNameManager.decideDisplayName(status.retweet_user_nickname,
                    status.retweeted_by_user_name, status.retweeted_by_user_screen_name, nameFirst);
            statusInfoLabel.setText(context.getString(R.string.name_retweeted, formatter.unicodeWrap(retweetedBy)));
            statusInfoIcon.setImageResource(R.drawable.ic_activity_action_retweet);
            statusInfoLabel.setVisibility(View.VISIBLE);
            statusInfoIcon.setVisibility(View.VISIBLE);

            if (statusContentUpperSpace != null) {
                statusContentUpperSpace.setVisibility(View.GONE);
            }
        } else if (status.in_reply_to_status_id != null && status.in_reply_to_user_id != null && displayInReplyTo) {
            final String inReplyTo = UserColorNameManager.decideDisplayName(status.in_reply_to_user_nickname,
                    status.in_reply_to_name, status.in_reply_to_screen_name, nameFirst);
            statusInfoLabel.setText(context.getString(R.string.in_reply_to_name, formatter.unicodeWrap(inReplyTo)));
            statusInfoIcon.setImageResource(R.drawable.ic_activity_action_reply);
            statusInfoLabel.setVisibility(View.VISIBLE);
            statusInfoIcon.setVisibility(View.VISIBLE);

            if (statusContentUpperSpace != null) {
                statusContentUpperSpace.setVisibility(View.GONE);
            }
        } else {
            statusInfoLabel.setVisibility(View.GONE);
            statusInfoIcon.setVisibility(View.GONE);

            if (statusContentUpperSpace != null) {
                statusContentUpperSpace.setVisibility(View.VISIBLE);
            }
        }

        boolean skipLinksInText = status.extras != null && status.extras.support_entities;
        if (status.is_quote) {

            quoteIndicator.setVisibility(View.VISIBLE);
            quoteIndicatorAnchorTop.setVisibility(View.VISIBLE);
            quoteIndicatorAnchorBottom.setVisibility(View.VISIBLE);

            boolean quoteContentAvailable = status.quoted_text_plain != null
                    && status.quoted_text_unescaped != null;
            if (quoteContentAvailable) {

                quotedNameView.setVisibility(View.VISIBLE);
                quotedTextView.setVisibility(View.VISIBLE);

                quotedNameView.setName(UserColorNameManager.decideNickname(status.quoted_user_nickname,
                        status.quoted_user_name));
                quotedNameView.setScreenName("@" + status.quoted_user_screen_name);

                int quotedDisplayEnd = -1;
                if (status.extras.quoted_display_text_range != null) {
                    quotedDisplayEnd = status.extras.quoted_display_text_range[1];
                }
                final CharSequence quotedText;
                if (adapter.getLinkHighlightingStyle() != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
                    quotedText = SpannableStringBuilder.valueOf(status.quoted_text_unescaped);
                    ParcelableStatusUtils.applySpans((Spannable) quotedText, status.quoted_spans);
                    linkify.applyAllLinks((Spannable) quotedText, status.account_key, getLayoutPosition(),
                            status.is_possibly_sensitive, adapter.getLinkHighlightingStyle(),
                            skipLinksInText);
                } else {
                    quotedText = status.quoted_text_unescaped;
                }
                if (quotedDisplayEnd != -1 && quotedDisplayEnd <= quotedText.length()) {
                    quotedTextView.setText(quotedText.subSequence(0, quotedDisplayEnd));
                } else {
                    quotedTextView.setText(quotedText);
                }

                if (quotedTextView.length() == 0) {
                    // No text
                    quotedTextView.setVisibility(View.GONE);
                } else {
                    quotedTextView.setVisibility(View.VISIBLE);
                }

                quoteIndicator.setColor(status.quoted_user_color);
            } else {
                quotedNameView.setVisibility(View.GONE);
                quotedTextView.setVisibility(View.VISIBLE);

                // Not available
                final SpannableString string = SpannableString.valueOf(context.getString(R.string.status_not_available_text));
                string.setSpan(new ForegroundColorSpan(ThemeUtils.getColorFromAttribute(context,
                        android.R.attr.textColorTertiary, textView.getCurrentTextColor())), 0,
                        string.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                quotedTextView.setText(string);

                quoteIndicator.setColor(0);
            }

            itemContent.drawStart(status.user_color);
        } else {

            quoteIndicatorAnchorTop.setVisibility(View.GONE);
            quoteIndicatorAnchorBottom.setVisibility(View.GONE);
            quotedNameView.setVisibility(View.GONE);
            quotedTextView.setVisibility(View.GONE);
            quoteIndicator.setVisibility(View.GONE);

            if (status.is_retweet) {
                final int retweetUserColor = status.retweet_user_color;
                final int userColor = status.user_color;
                if (retweetUserColor == 0) {
                    itemContent.drawStart(userColor);
                } else if (userColor == 0) {
                    itemContent.drawStart(retweetUserColor);
                } else {
                    itemContent.drawStart(retweetUserColor, userColor);
                }
            } else {
                itemContent.drawStart(status.user_color);
            }
        }

        if (status.is_retweet) {
            timeView.setTime(status.retweet_timestamp);
        } else {
            timeView.setTime(status.timestamp);
        }
        nameView.setName(UserColorNameManager.decideNickname(status.user_nickname, status.user_name));
        nameView.setScreenName("@" + status.user_screen_name);

        if (adapter.isProfileImageEnabled()) {
            profileImageView.setVisibility(View.VISIBLE);
            if (profileImageSpace != null) {
                profileImageSpace.setVisibility(View.VISIBLE);
            }
            loader.displayProfileImage(profileImageView, status);

            profileTypeView.setImageResource(getUserTypeIconRes(status.user_is_verified, status.user_is_protected));
            profileTypeView.setVisibility(View.VISIBLE);
        } else {
            profileTypeView.setVisibility(View.GONE);
            profileImageView.setVisibility(View.GONE);
            if (profileImageSpace != null) {
                profileImageSpace.setVisibility(View.GONE);
            }

            loader.cancelDisplayTask(profileImageView);

            profileTypeView.setImageDrawable(null);
            profileTypeView.setVisibility(View.GONE);
        }

        if (adapter.shouldShowAccountsColor()) {
            itemContent.drawEnd(status.account_color);
        } else {
            itemContent.drawEnd();
        }

        final boolean hasQuotedMedia = !ArrayUtils.isEmpty(status.quoted_media);
        final boolean hasPrimaryMedia = !hasQuotedMedia && !ArrayUtils.isEmpty(status.media);


        if (!hasPrimaryMedia && !hasQuotedMedia) {
            // No media, hide all related views
            mediaLabel.setVisibility(View.GONE);
            quoteMediaLabel.setVisibility(View.GONE);
            mediaPreview.setVisibility(View.GONE);
            quoteMediaPreview.setVisibility(View.GONE);

        } else {

            if (!adapter.isSensitiveContentEnabled() && status.is_possibly_sensitive) {
                // Sensitive content, show label instead of media view
                mediaLabel.setVisibility(hasPrimaryMedia ? View.VISIBLE : View.GONE);
                quoteMediaLabel.setVisibility(hasQuotedMedia ? View.VISIBLE : View.GONE);

                mediaPreview.setVisibility(View.GONE);
                quoteMediaPreview.setVisibility(View.GONE);

            } else if (!adapter.isMediaPreviewEnabled()) {
                // Media preview disabled, just show label
                mediaLabel.setVisibility(hasPrimaryMedia ? View.VISIBLE : View.GONE);
                quoteMediaLabel.setVisibility(hasQuotedMedia ? View.VISIBLE : View.GONE);

                mediaPreview.setVisibility(View.GONE);
                quoteMediaPreview.setVisibility(View.GONE);

            } else {
                // Show media

                mediaLabel.setVisibility(View.GONE);
                quoteMediaLabel.setVisibility(View.GONE);

                mediaPreview.setStyle(adapter.getMediaPreviewStyle());
                quoteMediaPreview.setStyle(adapter.getMediaPreviewStyle());

                mediaPreview.setVisibility(hasPrimaryMedia ? View.VISIBLE : View.GONE);
                quoteMediaPreview.setVisibility(hasQuotedMedia ? View.VISIBLE : View.GONE);

                mediaPreview.displayMedia(status.media, loader, status.account_key, -1, this,
                        adapter.getMediaLoadingHandler());
                quoteMediaPreview.displayMedia(status.quoted_media, loader, status.account_key, -1, this,
                        adapter.getMediaLoadingHandler());
            }
        }

        int displayEnd = -1;
        if (status.extras.display_text_range != null) {
            displayEnd = status.extras.display_text_range[1];
        }

        final CharSequence text;
        if (adapter.getLinkHighlightingStyle() != VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE) {
            text = SpannableStringBuilder.valueOf(status.text_unescaped);
            ParcelableStatusUtils.applySpans((Spannable) text, status.spans);
            linkify.applyAllLinks((Spannable) text, status.account_key, getLayoutPosition(),
                    status.is_possibly_sensitive, adapter.getLinkHighlightingStyle(),
                    skipLinksInText);
        } else {
            text = status.text_unescaped;
        }

        if (displayEnd != -1 && displayEnd <= text.length()) {
            textView.setText(text.subSequence(0, displayEnd));
        } else {
            textView.setText(text);
        }
        if (textView.length() == 0) {
            // No text
            textView.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
        }

        if (replyCount > 0) {
            replyCountView.setText(UnitConvertUtils.calculateProperCount(replyCount));
            replyCountView.setVisibility(View.VISIBLE);
        } else {
            replyCountView.setText(null);
            replyCountView.setVisibility(View.GONE);
        }

        if (twitter.isDestroyingStatus(status.account_key, status.my_retweet_id)) {
            retweetIconView.setActivated(false);
            retweetCount = Math.max(0, status.retweet_count - 1);
        } else {
            final boolean creatingRetweet = twitter.isCreatingRetweet(status.account_key, status.id);
            retweetIconView.setActivated(creatingRetweet || status.retweeted ||
                    Utils.isMyRetweet(status.account_key, status.retweeted_by_user_key,
                            status.my_retweet_id));
            retweetCount = status.retweet_count + (creatingRetweet ? 1 : 0);
        }

        if (retweetCount > 0) {
            retweetCountView.setText(UnitConvertUtils.calculateProperCount(retweetCount));
            retweetCountView.setVisibility(View.VISIBLE);
        } else {
            retweetCountView.setText(null);
            retweetCountView.setVisibility(View.GONE);
        }
        if (twitter.isDestroyingFavorite(status.account_key, status.id)) {
            favoriteIconView.setActivated(false);
            favoriteCount = Math.max(0, status.favorite_count - 1);
        } else {
            final boolean creatingFavorite = twitter.isCreatingFavorite(status.account_key, status.id);
            favoriteIconView.setActivated(creatingFavorite || status.is_favorite);
            favoriteCount = status.favorite_count + (creatingFavorite ? 1 : 0);
        }
        if (favoriteCount > 0) {
            favoriteCountView.setText(UnitConvertUtils.calculateProperCount(favoriteCount));
            favoriteCountView.setVisibility(View.VISIBLE);
        } else {
            favoriteCountView.setText(null);
            favoriteCountView.setVisibility(View.GONE);
        }
        if (shouldDisplayExtraType) {
            displayExtraTypeIcon(status.card_name, status.media, status.location,
                    status.place_full_name, status.is_possibly_sensitive);
        } else {
            extraTypeView.setVisibility(View.GONE);
        }

        nameView.updateText(formatter);
        quotedNameView.updateText(formatter);

    }

    @Override
    public ImageView getProfileImageView() {
        return profileImageView;
    }

    @Override
    public ImageView getProfileTypeView() {
        return profileTypeView;
    }

    @Override
    public void onMediaClick(View view, ParcelableMedia media, UserKey accountId, long extraId) {
        if (statusClickListener == null) return;
        final int position = getLayoutPosition();
        statusClickListener.onMediaClick(this, view, media, position);
    }


    public void setOnClickListeners() {
        setStatusClickListener(adapter.getStatusClickListener());
    }

    @Override
    public void setStatusClickListener(StatusClickListener listener) {
        statusClickListener = listener;
        ((View) itemContent).setOnClickListener(eventListener);
        ((View) itemContent).setOnLongClickListener(eventListener);

        itemMenu.setOnClickListener(eventListener);
        profileImageView.setOnClickListener(eventListener);
        replyView.setOnClickListener(eventListener);
        retweetView.setOnClickListener(eventListener);
        favoriteView.setOnClickListener(eventListener);
    }

    @Override
    public void setTextSize(final float textSize) {
        nameView.setPrimaryTextSize(textSize);
        quotedNameView.setPrimaryTextSize(textSize);
        textView.setTextSize(textSize);
        quotedTextView.setTextSize(textSize);
        nameView.setSecondaryTextSize(textSize * 0.85f);
        quotedNameView.setSecondaryTextSize(textSize * 0.85f);
        timeView.setTextSize(textSize * 0.85f);
        statusInfoLabel.setTextSize(textSize * 0.75f);

        mediaLabelText.setTextSize(textSize * 0.95f);
        quoteMediaLabelText.setTextSize(textSize * 0.95f);

        replyCountView.setTextSize(textSize);
        retweetCountView.setTextSize(textSize);
        favoriteCountView.setTextSize(textSize);
    }

    public void setupViewOptions() {
        setTextSize(adapter.getTextSize());
        mediaPreview.setStyle(adapter.getMediaPreviewStyle());
//        profileImageView.setStyle(adapter.getProfileImageStyle());

        final boolean nameFirst = adapter.isNameFirst();
        nameView.setNameFirst(nameFirst);
        quotedNameView.setNameFirst(nameFirst);

        final int favIcon, favStyle, favColor;
        final Context context = adapter.getContext();
        if (adapter.shouldUseStarsForLikes()) {
            favIcon = R.drawable.ic_action_star;
            favStyle = LikeAnimationDrawable.Style.FAVORITE;
            favColor = ContextCompat.getColor(context, R.color.highlight_favorite);
        } else {
            favIcon = R.drawable.ic_action_heart;
            favStyle = LikeAnimationDrawable.Style.LIKE;
            favColor = ContextCompat.getColor(context, R.color.highlight_like);
        }
        final Drawable icon = ContextCompat.getDrawable(context, favIcon);
        final LikeAnimationDrawable drawable = new LikeAnimationDrawable(icon,
                favoriteCountView.getTextColors().getDefaultColor(), favColor, favStyle);
        drawable.mutate();
        favoriteIconView.setImageDrawable(drawable);
        timeView.setShowAbsoluteTime(adapter.isShowAbsoluteTime());

        favoriteIconView.setActivatedColor(favColor);
    }

    @Override
    public void playLikeAnimation(@NonNull LikeAnimationDrawable.OnLikedListener listener) {
        boolean handled = false;
        final Drawable drawable = favoriteIconView.getDrawable();
        if (drawable instanceof LikeAnimationDrawable) {
            ((LikeAnimationDrawable) drawable).setOnLikedListener(listener);
            ((LikeAnimationDrawable) drawable).start();
            handled = true;
        }
        if (!handled) {
            listener.onLiked();
        }
    }

    private boolean isCardActionsShown() {
        return adapter.isCardActionsShown(getLayoutPosition());
    }

    private void showCardActions() {
        adapter.showCardActions(getLayoutPosition());
    }

    private boolean hideTempCardActions() {
        adapter.showCardActions(RecyclerView.NO_POSITION);
        return !adapter.isCardActionsShown(RecyclerView.NO_POSITION);
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
        } else if (!ArrayUtils.isEmpty(media)) {
            if (hasVideo(media)) {
                extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_movie);
            } else {
                extraTypeView.setImageResource(sensitive ? R.drawable.ic_action_warning : R.drawable.ic_action_gallery);
            }
            extraTypeView.setVisibility(View.VISIBLE);
        } else if (ParcelableLocationUtils.isValidLocation(location) || !TextUtils.isEmpty(placeFullName)) {
            extraTypeView.setImageResource(R.drawable.ic_action_location);
            extraTypeView.setVisibility(View.VISIBLE);
        } else {
            extraTypeView.setVisibility(View.GONE);
        }
    }

    private boolean hasVideo(ParcelableMedia[] media) {
        if (media == null) return false;
        for (ParcelableMedia item : media) {
            if (item == null) continue;
            switch (item.type) {
                case ParcelableMedia.Type.VIDEO:
                case ParcelableMedia.Type.ANIMATED_GIF:
                case ParcelableMedia.Type.EXTERNAL_PLAYER:
                    return true;
            }
        }
        return false;
    }

    static class EventListener implements OnClickListener, OnLongClickListener {

        final WeakReference<StatusViewHolder> holderRef;

        EventListener(StatusViewHolder holder) {
            this.holderRef = new WeakReference<>(holder);
        }

        @Override
        public void onClick(View v) {
            StatusViewHolder holder = holderRef.get();
            if (holder == null) return;
            StatusClickListener listener = holder.statusClickListener;
            if (listener == null) return;
            final int position = holder.getLayoutPosition();
            switch (v.getId()) {
                case R.id.item_content: {
                    listener.onStatusClick(holder, position);
                    break;
                }
                case R.id.item_menu: {
                    listener.onItemMenuClick(holder, v, position);
                    break;
                }
                case R.id.profile_image: {
                    listener.onUserProfileClick(holder, position);
                    break;
                }
                case R.id.reply_count:
                case R.id.reply_icon:
                case R.id.reply: {
                    listener.onItemActionClick(holder, R.id.reply, position);
                    break;
                }
                case R.id.retweet_count:
                case R.id.retweet_icon:
                case R.id.retweet: {
                    listener.onItemActionClick(holder, R.id.retweet, position);
                    break;
                }
                case R.id.favorite_count:
                case R.id.favorite_icon:
                case R.id.favorite: {
                    listener.onItemActionClick(holder, R.id.favorite, position);
                    break;
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            StatusViewHolder holder = holderRef.get();
            if (holder == null) return false;
            StatusClickListener listener = holder.statusClickListener;
            if (listener == null) return false;
            final int position = holder.getLayoutPosition();
            switch (v.getId()) {
                case R.id.item_content: {
                    if (!holder.isCardActionsShown()) {
                        holder.showCardActions();
                        return true;
                    } else if (holder.hideTempCardActions()) {
                        return true;
                    }
                    return listener.onStatusLongClick(holder, position);
                }
            }
            return false;
        }
    }


}
