package org.mariotaku.twidere.model.tab.conf;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.UserListSelectorActivity;
import org.mariotaku.twidere.model.tab.TabConfiguration;

import static org.mariotaku.twidere.constant.IntentConstants.INTENT_ACTION_SELECT_USER;
import static org.mariotaku.twidere.constant.IntentConstants.INTENT_ACTION_SELECT_USER_LIST;

/**
 * Created by mariotaku on 2016/11/28.
 */

public class UserListExtraConfiguration extends TabConfiguration.ExtraConfiguration {
    public UserListExtraConfiguration(String key) {
        super(key);
    }


    @NonNull
    @Override
    public View onCreateView(Context context, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_simple_user_list, parent, false);
    }

    @Override
    public void onViewCreated(@NonNull final Context context, @NonNull final View view, @NonNull final DialogFragment fragment) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(INTENT_ACTION_SELECT_USER_LIST);
                intent.setClass(context, UserListSelectorActivity.class);
                fragment.startActivity(intent);
            }
        });
    }
}
