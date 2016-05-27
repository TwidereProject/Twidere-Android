package org.mariotaku.twidere.fragment;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

import org.mariotaku.microblog.library.MicroBlog;
import org.mariotaku.microblog.library.MicroBlogException;
import org.mariotaku.microblog.library.statusnet.model.Group;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.SupportTabsAdapter;
import org.mariotaku.twidere.model.ParcelableGroup;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.model.UserKey;
import org.mariotaku.twidere.model.util.ParcelableGroupUtils;
import org.mariotaku.twidere.util.MicroBlogAPIFactory;
import org.mariotaku.twidere.util.Utils;

/**
 * Created by mariotaku on 16/3/23.
 */
public class GroupFragment extends AbsToolbarTabPagesFragment implements
        LoaderCallbacks<SingleResponse<ParcelableGroup>> {
    private ParcelableGroup mGroup;
    private boolean mGroupLoaderInitialized;

    @Override
    protected void addTabs(SupportTabsAdapter adapter) {
        final Bundle args = getArguments();
        adapter.addTab(GroupTimelineFragment.class, args, getString(R.string.statuses), 0, 0, "statuses");
        adapter.addTab(GroupMembersFragment.class, args, getString(R.string.members), 0, 1, "members");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Utils.setNdefPushMessageCallback(getActivity(), new NfcAdapter.CreateNdefMessageCallback() {

            @Override
            public NdefMessage createNdefMessage(NfcEvent event) {
                final ParcelableGroup group = getGroup();
                if (group == null || group.url == null) return null;
                return new NdefMessage(new NdefRecord[]{
                        NdefRecord.createUri(group.url),
                });
            }
        });

        getGroupInfo(false);
    }

    @Override
    public Loader<SingleResponse<ParcelableGroup>> onCreateLoader(int id, Bundle args) {
        final UserKey accountKey = args.getParcelable(EXTRA_ACCOUNT_KEY);
        final String groupId = args.getString(EXTRA_GROUP_ID);
        final String groupName = args.getString(EXTRA_GROUP_NAME);
        final boolean omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
        return new ParcelableGroupLoader(getContext(), omitIntentExtra, getArguments(), accountKey,
                groupId, groupName);
    }

    @Override
    public void onLoadFinished(Loader<SingleResponse<ParcelableGroup>> loader, SingleResponse<ParcelableGroup> data) {
        if (data.hasData()) {
            displayGroup(data.getData());
        }
    }

    @Override
    public void onLoaderReset(Loader<SingleResponse<ParcelableGroup>> loader) {

    }

    public void displayGroup(final ParcelableGroup group) {
        final FragmentActivity activity = getActivity();
        if (activity == null) return;
        getLoaderManager().destroyLoader(0);
        mGroup = group;

        if (group != null) {
            activity.setTitle(group.fullname);
        } else {
            activity.setTitle(R.string.user_list);
        }
        invalidateOptionsMenu();
    }


    public void getGroupInfo(final boolean omitIntentExtra) {
        final LoaderManager lm = getLoaderManager();
        lm.destroyLoader(0);
        final Bundle args = new Bundle(getArguments());
        args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omitIntentExtra);
        if (!mGroupLoaderInitialized) {
            lm.initLoader(0, args, this);
            mGroupLoaderInitialized = true;
        } else {
            lm.restartLoader(0, args, this);
        }
    }

    public ParcelableGroup getGroup() {
        return mGroup;
    }

    static class ParcelableGroupLoader extends AsyncTaskLoader<SingleResponse<ParcelableGroup>> {

        private final boolean mOmitIntentExtra;
        private final Bundle mExtras;
        private final UserKey mAccountKey;
        private final String mGroupId;
        private final String mGroupName;

        private ParcelableGroupLoader(final Context context, final boolean omitIntentExtra,
                                      final Bundle extras, final UserKey accountKey,
                                      final String groupId, final String groupName) {
            super(context);
            mOmitIntentExtra = omitIntentExtra;
            mExtras = extras;
            mAccountKey = accountKey;
            mGroupId = groupId;
            mGroupName = groupName;
        }

        @Override
        public SingleResponse<ParcelableGroup> loadInBackground() {
            if (!mOmitIntentExtra && mExtras != null) {
                final ParcelableGroup cache = mExtras.getParcelable(EXTRA_GROUP);
                if (cache != null) return SingleResponse.getInstance(cache);
            }
            final MicroBlog twitter = MicroBlogAPIFactory.getInstance(getContext(), mAccountKey,
                    true);
            if (twitter == null) return SingleResponse.getInstance();
            try {
                final Group group;
                if (mGroupId != null) {
                    group = twitter.showGroup(mGroupId);
                } else if (mGroupName != null) {
                    group = twitter.showGroupByName(mGroupName);
                } else {
                    return SingleResponse.getInstance();
                }
                return SingleResponse.getInstance(ParcelableGroupUtils.from(group, mAccountKey, 0,
                        group.isMember()));
            } catch (final MicroBlogException e) {
                return SingleResponse.getInstance(e);
            }
        }

        @Override
        public void onStartLoading() {
            forceLoad();
        }

    }
}
