package org.mariotaku.twidere.view.holder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.constant.IntentConstants;
import org.mariotaku.twidere.fragment.support.StatusMenuDialogFragment;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.view.CircularImageView;
import org.mariotaku.twidere.view.ShortTimeView;

import java.util.Locale;

import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;

/**
 * Created by mariotaku on 14/11/19.
 */
public class StatusViewHolder extends RecyclerView.ViewHolder implements OnClickListener {

    private final IStatusesAdapter adapter;

    private final ImageView retweetProfileImageView;
    private final CircularImageView profileImageView;
    private final ImageView profileTypeView;
    private final ImageView mediaPreviewView;
    private final TextView textView;
    private final TextView nameView, screenNameView;
    private final TextView replyRetweetView;
    private final ShortTimeView timeView;
    private final View mediaPreviewContainer;
    private final TextView replyCountView, retweetCountView, favoriteCountView;

    public StatusViewHolder(IStatusesAdapter adapter, View itemView) {
        super(itemView);
        this.adapter = adapter;
        itemView.findViewById(R.id.item_content).setOnClickListener(this);
        itemView.findViewById(R.id.menu).setOnClickListener(this);
        profileImageView = (CircularImageView) itemView.findViewById(R.id.profile_image);
        profileTypeView = (ImageView) itemView.findViewById(R.id.profile_type);
        textView = (TextView) itemView.findViewById(R.id.text);
        nameView = (TextView) itemView.findViewById(R.id.name);
        screenNameView = (TextView) itemView.findViewById(R.id.screen_name);
        retweetProfileImageView = (ImageView) itemView.findViewById(R.id.retweet_profile_image);
        replyRetweetView = (TextView) itemView.findViewById(R.id.reply_retweet_status);
        timeView = (ShortTimeView) itemView.findViewById(R.id.time);

        mediaPreviewContainer = itemView.findViewById(R.id.media_preview_container);
        mediaPreviewView = (ImageView) itemView.findViewById(R.id.media_preview);

        replyCountView = (TextView) itemView.findViewById(R.id.reply_count);
        retweetCountView = (TextView) itemView.findViewById(R.id.retweet_count);
        favoriteCountView = (TextView) itemView.findViewById(R.id.favorite_count);
//TODO
//        profileImageView.setSelectorColor(ThemeUtils.getUserHighlightColor(itemView.getContext()));

        itemView.setOnClickListener(this);
        profileImageView.setOnClickListener(this);
        mediaPreviewContainer.setOnClickListener(this);
        retweetCountView.setOnClickListener(this);
        retweetCountView.setOnClickListener(this);
        favoriteCountView.setOnClickListener(this);
    }

    public void displayStatus(ParcelableStatus status) {
        final ImageLoaderWrapper loader = adapter.getImageLoader();
        final Context context = adapter.getContext();
        final ParcelableMedia[] media = status.media;

        if (status.retweet_id > 0) {
            if (status.retweet_count == 2) {
                replyRetweetView.setText(context.getString(R.string.name_and_another_retweeted,
                        status.retweeted_by_name));
            } else if (status.retweet_count > 2) {
                replyRetweetView.setText(context.getString(R.string.name_and_count_retweeted,
                        status.retweeted_by_name, status.retweet_count - 1));
            } else {
                replyRetweetView.setText(context.getString(R.string.name_retweeted, status.retweeted_by_name));
            }
            replyRetweetView.setVisibility(View.VISIBLE);
//            replyRetweetView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_retweet, 0, 0, 0);
            retweetProfileImageView.setVisibility(View.GONE);
        } else if (status.in_reply_to_status_id > 0 && status.in_reply_to_user_id > 0) {
            replyRetweetView.setText(context.getString(R.string.in_reply_to_name, status.in_reply_to_name));
            replyRetweetView.setVisibility(View.VISIBLE);
//            replyRetweetView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_reply, 0, 0, 0);
            retweetProfileImageView.setVisibility(View.GONE);
        } else {
            replyRetweetView.setText(null);
            replyRetweetView.setVisibility(View.GONE);
            replyRetweetView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            retweetProfileImageView.setVisibility(View.GONE);
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

//            final int userColor = UserColorNicknameUtils.getUserColor(context, status.user_id);
//            profileImageView.setBorderColor(userColor);

        loader.displayProfileImage(profileImageView, status.user_profile_image_url);

        if (media != null && media.length > 0) {
            final ParcelableMedia firstMedia = media[0];
            if (status.text_plain.codePointCount(0, status.text_plain.length()) == firstMedia.end) {
                textView.setText(status.text_unescaped.substring(0, firstMedia.start));
            } else {
                textView.setText(status.text_unescaped);
            }
            loader.displayPreviewImageWithCredentials(mediaPreviewView, firstMedia.media_url,
                    status.account_id, adapter.getImageLoadingHandler());
            mediaPreviewContainer.setVisibility(View.VISIBLE);
        } else {
            loader.cancelDisplayTask(mediaPreviewView);
            textView.setText(status.text_unescaped);
            mediaPreviewContainer.setVisibility(View.GONE);
        }

        if (status.reply_count > 0) {
            replyCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), status.reply_count));
        } else {
            replyCountView.setText(null);
        }
        if (status.retweet_count > 0) {
            retweetCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), status.retweet_count));
        } else {
            retweetCountView.setText(null);
        }
        if (status.favorite_count > 0) {
            favoriteCountView.setText(Utils.getLocalizedNumber(Locale.getDefault(), status.favorite_count));
        } else {
            favoriteCountView.setText(null);
        }

        retweetCountView.setEnabled(!status.user_is_protected);

        retweetCountView.setActivated(status.is_retweet);
        favoriteCountView.setActivated(status.is_favorite);
    }

    @Override
    public void onClick(View v) {
        final Context context = itemView.getContext();
        final ParcelableStatus status = adapter.getStatus(getPosition());
        switch (v.getId()) {
            case R.id.item_content: {
                Utils.openStatus(context, status);
                break;
            }
            case R.id.menu: {
                if (context instanceof FragmentActivity) {
                    final Bundle args = new Bundle();
                    args.putParcelable(IntentConstants.EXTRA_STATUS, status);
                    final StatusMenuDialogFragment f = new StatusMenuDialogFragment();
                    f.setArguments(args);
                    f.show(((FragmentActivity) context).getSupportFragmentManager(), "status_menu");
                }
                break;
            }
            case R.id.profile_image: {
                Utils.openUserProfile(context, status.account_id, status.user_id, status.user_screen_name);
                break;
            }
            case R.id.reply_count: {
                final Intent intent = new Intent(IntentConstants.INTENT_ACTION_REPLY);
                intent.setPackage(context.getPackageName());
                intent.putExtra(IntentConstants.EXTRA_STATUS, status);
                context.startActivity(intent);
                break;
            }
        }
    }
}
