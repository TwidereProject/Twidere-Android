package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import com.desmond.asyncmanager.AsyncManager;
import com.desmond.asyncmanager.TaskRunnable;
import com.squareup.otto.Subscribe;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AbsStatusesAdapter;
import org.mariotaku.twidere.adapter.AbsStatusesAdapter.StatusAdapterListener;
import org.mariotaku.twidere.loader.iface.IExtendedLoader;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler;
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutCallback;
import org.mariotaku.twidere.util.RecyclerViewNavigationHelper;
import org.mariotaku.twidere.util.RecyclerViewUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.imageloader.PauseRecyclerViewOnScrollListener;
import org.mariotaku.twidere.util.message.StatusListChangedEvent;
import org.mariotaku.twidere.view.holder.GapViewHolder;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import edu.tsinghua.hotmobi.HotMobiLogger;
import edu.tsinghua.hotmobi.model.MediaEvent;
import edu.tsinghua.hotmobi.model.ScrollRecord;
import edu.tsinghua.hotmobi.model.TimelineType;

import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

/**
 * Created by mariotaku on 14/11/5.
 */
public abstract class AbsStatusesFragment<Data> extends AbsContentRecyclerViewFragment<AbsStatusesAdapter<Data>>
        implements LoaderCallbacks<Data>, StatusAdapterListener, KeyboardShortcutCallback {

    private final Object mStatusesBusCallback;
    private final OnScrollListener mHotMobiScrollTracker = new OnScrollListener() {

        public List<ScrollRecord> mRecords;
        private long mFirstVisibleId = -1, mFirstVisibleAccountId = -1;
        private int mFirstVisiblePosition = -1;
        private int mScrollState;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            final int firstVisiblePosition = layoutManager.findFirstVisibleItemPosition();
            if (firstVisiblePosition != mFirstVisiblePosition && firstVisiblePosition >= 0) {
                //noinspection unchecked
                final AbsStatusesAdapter<Data> adapter = (AbsStatusesAdapter<Data>) recyclerView.getAdapter();
                final ParcelableStatus status = adapter.getStatus(firstVisiblePosition);
                if (status != null) {
                    final long id = status.id, accountId = status.account_id;
                    if (id != mFirstVisibleId || accountId != mFirstVisibleAccountId) {
                        if (mRecords == null) mRecords = new ArrayList<>();
                        final long time = System.currentTimeMillis();
                        mRecords.add(ScrollRecord.create(id, accountId, time,
                                TimeZone.getDefault().getOffset(time), mScrollState));
                    }
                    mFirstVisibleId = id;
                    mFirstVisibleAccountId = accountId;
                }
            }
            mFirstVisiblePosition = firstVisiblePosition;
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            mScrollState = newState;
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (mRecords != null) {
                    HotMobiLogger.getInstance(getActivity()).logList(mRecords, HotMobiLogger.ACCOUNT_ID_NOT_NEEDED, "scroll");
                }
                mRecords = null;
            }
        }
    };
    private SharedPreferences mPreferences;
    private PopupMenu mPopupMenu;
    private final OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                final LinearLayoutManager layoutManager = getLayoutManager();
                saveReadPosition(layoutManager.findFirstVisibleItemPosition());
            }
        }
    };
    private RecyclerViewNavigationHelper mNavigationHelper;
    private ParcelableStatus mSelectedStatus;
    private OnMenuItemClickListener mOnStatusMenuItemClickListener = new OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            final ParcelableStatus status = mSelectedStatus;
            if (status == null) return false;
            if (item.getItemId() == R.id.share) {
                final Intent shareIntent = Utils.createStatusShareIntent(getActivity(), status);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share_status)));
                return true;
            }
            return Utils.handleMenuItemClick(getActivity(), AbsStatusesFragment.this,
                    getFragmentManager(), mTwitterWrapper, status, item);
        }
    };
    private OnScrollListener mPauseOnScrollListener;
    private OnScrollListener mActiveHotMobiScrollTracker;

    protected AbsStatusesFragment() {
        mStatusesBusCallback = createMessageBusCallback();
    }

    public SharedPreferences getSharedPreferences() {
        if (mPreferences != null) return mPreferences;
        return mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public abstract boolean getStatuses(long[] accountIds, long[] maxIds, long[] sinceIds);

    @Override
    public boolean handleKeyboardShortcutSingle(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_REFRESH.equals(action)) {
            triggerRefresh();
            return true;
        }
        final RecyclerView recyclerView = getRecyclerView();
        final LinearLayoutManager layoutManager = getLayoutManager();
        if (recyclerView == null || layoutManager == null) return false;
        final View focusedChild = RecyclerViewUtils.findRecyclerViewChild(recyclerView,
                layoutManager.getFocusedChild());
        int position = -1;
        if (focusedChild != null && focusedChild.getParent() == recyclerView) {
            position = recyclerView.getChildLayoutPosition(focusedChild);
        }
        if (position != -1) {
            final ParcelableStatus status = getAdapter().getStatus(position);
            if (status == null) return false;
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                Utils.openStatus(getActivity(), status, null);
                return true;
            }
            if (action == null) {
                action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState);
            }
            if (action == null) return false;
            switch (action) {
                case ACTION_STATUS_REPLY: {
                    final Intent intent = new Intent(INTENT_ACTION_REPLY);
                    intent.putExtra(EXTRA_STATUS, status);
                    startActivity(intent);
                    return true;
                }
                case ACTION_STATUS_RETWEET: {
                    RetweetQuoteDialogFragment.show(getFragmentManager(), status);
                    return true;
                }
                case ACTION_STATUS_FAVORITE: {
                    final AsyncTwitterWrapper twitter = mTwitterWrapper;
                    if (status.is_favorite) {
                        twitter.destroyFavoriteAsync(status.account_id, status.id);
                    } else {
                        twitter.createFavoriteAsync(status.account_id, status.id);
                    }
                    return true;
                }
            }
        }
        return mNavigationHelper.handleKeyboardShortcutSingle(handler, keyCode, event, metaState);
    }

    @Override
    public boolean isKeyboardShortcutHandled(@NonNull KeyboardShortcutsHandler handler, int keyCode, @NonNull KeyEvent event, int metaState) {
        String action = handler.getKeyAction(CONTEXT_TAG_NAVIGATION, keyCode, event, metaState);
        if (ACTION_NAVIGATION_REFRESH.equals(action)) {
            return true;
        }
        if (action == null) {
            action = handler.getKeyAction(CONTEXT_TAG_STATUS, keyCode, event, metaState);
        }
        if (action == null) return false;
        switch (action) {
            case ACTION_STATUS_REPLY:
            case ACTION_STATUS_RETWEET:
            case ACTION_STATUS_FAVORITE:
                return true;
        }
        return mNavigationHelper.isKeyboardShortcutHandled(handler, keyCode, event, metaState);
    }

    @Override
    public boolean handleKeyboardShortcutRepeat(@NonNull KeyboardShortcutsHandler handler, final int keyCode, final int repeatCount,
                                                @NonNull final KeyEvent event, int metaState) {
        return mNavigationHelper.handleKeyboardShortcutRepeat(handler, keyCode, repeatCount, event, metaState);
    }

    @Override
    public final Loader<Data> onCreateLoader(int id, Bundle args) {
        final boolean fromUser = args.getBoolean(EXTRA_FROM_USER);
        args.remove(EXTRA_FROM_USER);
        return onCreateStatusesLoader(getActivity(), args, fromUser);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            final LinearLayoutManager layoutManager = getLayoutManager();
            if (layoutManager != null) {
                saveReadPosition(layoutManager.findFirstVisibleItemPosition());
            }
        }
    }

    @Override
    public final void onLoadFinished(Loader<Data> loader, Data data) {
        final AbsStatusesAdapter<Data> adapter = getAdapter();
        final SharedPreferences preferences = getSharedPreferences();
        final boolean rememberPosition = preferences.getBoolean(KEY_REMEMBER_POSITION, false);
        final boolean readFromBottom = preferences.getBoolean(KEY_READ_FROM_BOTTOM, false);
        final long lastReadId;
        final int lastVisiblePos, lastVisibleTop;
        final String tag = getCurrentReadPositionTag();
        final LinearLayoutManager layoutManager = getLayoutManager();
        if (readFromBottom) {
            lastVisiblePos = layoutManager.findLastVisibleItemPosition();
        } else {
            lastVisiblePos = layoutManager.findFirstVisibleItemPosition();
        }
        if (lastVisiblePos != RecyclerView.NO_POSITION && lastVisiblePos < adapter.getItemCount()) {
            lastReadId = adapter.getStatusId(lastVisiblePos);
            final View positionView = layoutManager.findViewByPosition(lastVisiblePos);
            lastVisibleTop = positionView != null ? positionView.getTop() : 0;
        } else if (rememberPosition && tag != null) {
            lastReadId = mReadStateManager.getPosition(tag);
            lastVisibleTop = 0;
        } else {
            lastReadId = -1;
            lastVisibleTop = 0;
        }
        adapter.setData(data);
        setRefreshEnabled(true);
        if (!(loader instanceof IExtendedLoader) || ((IExtendedLoader) loader).isFromUser()) {
            adapter.setLoadMoreSupported(hasMoreData(data));
            int pos = -1;
            for (int i = 0, j = adapter.getItemCount(); i < j; i++) {
                if (lastReadId != -1 && lastReadId == adapter.getStatusId(i)) {
                    pos = i;
                    break;
                }
            }
            if (pos != -1 && adapter.isStatus(pos) && (readFromBottom || lastVisiblePos != 0)) {
                if (layoutManager.getHeight() == 0) {
                    // RecyclerView has not currently laid out, ignore padding.
                    layoutManager.scrollToPositionWithOffset(pos, lastVisibleTop);
                } else {
                    layoutManager.scrollToPositionWithOffset(pos, lastVisibleTop - layoutManager.getPaddingTop());
                }
            }
        }
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
        onLoadingFinished();
    }

    @Override
    public void onLoaderReset(Loader<Data> loader) {
        if (loader instanceof IExtendedLoader) {
            ((IExtendedLoader) loader).setFromUser(false);
        }
    }

    @Override
    public void onGapClick(GapViewHolder holder, int position) {
        final AbsStatusesAdapter<Data> adapter = getAdapter();
        final ParcelableStatus status = adapter.getStatus(position);
        final ParcelableStatus sinceStatus = position + 1 < adapter.getStatusesCount() ? adapter.getStatus(position + 1) : null;
        final long sinceId = sinceStatus != null ? sinceStatus.id : -1;
        final long[] accountIds = {status.account_id};
        final long[] maxIds = {status.id};
//        final long[] sinceIds = {sinceId};
        final long[] sinceIds = null;
        getStatuses(accountIds, maxIds, sinceIds);
    }

    @Override
    public void onMediaClick(StatusViewHolder holder, View view, ParcelableMedia media, int position) {
        final AbsStatusesAdapter<Data> adapter = getAdapter();
        final ParcelableStatus status = adapter.getStatus(position);
        if (status == null) return;
        final Bundle options = Utils.createMediaViewerActivityOption(view);
        Utils.openMedia(getActivity(), status, media, options);
        // BEGIN HotMobi
        final MediaEvent event = MediaEvent.create(getActivity(), status, media, TimelineType.OTHER,
                adapter.isMediaPreviewEnabled());
        HotMobiLogger.getInstance(getActivity()).log(status.account_id, event);
        // END HotMobi
    }

    @Override
    public void onStatusActionClick(StatusViewHolder holder, int id, int position) {
        final AbsStatusesAdapter<Data> adapter = getAdapter();
        final ParcelableStatus status = adapter.getStatus(position);
        if (status == null) return;
        final FragmentActivity activity = getActivity();
        switch (id) {
            case R.id.reply_count: {
                final Intent intent = new Intent(INTENT_ACTION_REPLY);
                intent.setPackage(activity.getPackageName());
                intent.putExtra(EXTRA_STATUS, status);
                activity.startActivity(intent);
                break;
            }
            case R.id.retweet_count: {
                RetweetQuoteDialogFragment.show(getFragmentManager(), status);
                break;
            }
            case R.id.favorite_count: {
                final AsyncTwitterWrapper twitter = mTwitterWrapper;
                if (twitter == null) return;
                if (status.is_favorite) {
                    twitter.destroyFavoriteAsync(status.account_id, status.id);
                } else {
                    twitter.createFavoriteAsync(status.account_id, status.id);
                }
                break;
            }
        }
    }

    @Override
    public void onStatusClick(StatusViewHolder holder, int position) {
        final AbsStatusesAdapter<Data> adapter = getAdapter();
        Utils.openStatus(getActivity(), adapter.getStatus(position), null);
    }

    @Override
    public boolean onStatusLongClick(StatusViewHolder holder, int position) {
        //TODO handle long click event
        return true;
    }

    @Override
    public void onStatusMenuClick(StatusViewHolder holder, View menuView, int position) {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
        final AbsStatusesAdapter<Data> adapter = getAdapter();
        final PopupMenu popupMenu = new PopupMenu(adapter.getContext(), menuView,
                Gravity.NO_GRAVITY, R.attr.actionOverflowMenuStyle, 0);
        popupMenu.setOnMenuItemClickListener(mOnStatusMenuItemClickListener);
        popupMenu.inflate(R.menu.action_status);
        final ParcelableStatus status = adapter.getStatus(position);
        setMenuForStatus(adapter.getContext(), popupMenu.getMenu(), status);
        popupMenu.show();
        mPopupMenu = popupMenu;
        mSelectedStatus = status;
    }

    @Override
    public void onUserProfileClick(StatusViewHolder holder, ParcelableStatus status, int position) {
        final FragmentActivity activity = getActivity();
        final View profileImageView = holder.getProfileImageView();
        final View profileTypeView = holder.getProfileTypeView();
        final Bundle options = Utils.makeSceneTransitionOption(activity,
                new Pair<>(profileImageView, UserFragment.TRANSITION_NAME_PROFILE_IMAGE),
                new Pair<>(profileTypeView, UserFragment.TRANSITION_NAME_PROFILE_TYPE));
        Utils.openUserProfile(activity, status.account_id, status.user_id, status.user_screen_name, options);
    }

    @Override
    public void onStart() {
        super.onStart();
        final RecyclerView recyclerView = getRecyclerView();
        recyclerView.addOnScrollListener(mOnScrollListener);
        recyclerView.addOnScrollListener(mPauseOnScrollListener);
        final TaskRunnable<Object, Boolean, RecyclerView> task = new TaskRunnable<Object, Boolean, RecyclerView>() {
            @Override
            public Boolean doLongOperation(Object params) throws InterruptedException {
                final Context context = getContext();
                final SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
                        Context.MODE_PRIVATE);
                if (!prefs.getBoolean(KEY_USAGE_STATISTICS, false)) return false;
                final File logFile = HotMobiLogger.getLogFile(context, HotMobiLogger.ACCOUNT_ID_NOT_NEEDED, "scroll");
                return logFile.length() < 131072;
            }

            @Override
            public void callback(RecyclerView recyclerView, Boolean result) {
                if (result) {
                    recyclerView.addOnScrollListener(mActiveHotMobiScrollTracker = mHotMobiScrollTracker);
                }
            }
        };
        task.setResultHandler(recyclerView);
        AsyncManager.runBackgroundTask(task);
        mBus.register(mStatusesBusCallback);
    }

    @Override
    public void onStop() {
        mBus.unregister(mStatusesBusCallback);
        final RecyclerView recyclerView = getRecyclerView();
        if (mActiveHotMobiScrollTracker != null) {
            recyclerView.removeOnScrollListener(mActiveHotMobiScrollTracker);
        }
        mActiveHotMobiScrollTracker = null;
        recyclerView.removeOnScrollListener(mPauseOnScrollListener);
        recyclerView.removeOnScrollListener(mOnScrollListener);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (mPopupMenu != null) {
            mPopupMenu.dismiss();
        }
        super.onDestroyView();
    }

    @Override
    public final boolean scrollToStart() {
        final boolean result = super.scrollToStart();
        if (result) {
            saveReadPosition(0);
        }
        return result;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final AbsStatusesAdapter<Data> adapter = getAdapter();
        final RecyclerView recyclerView = getRecyclerView();
        final LinearLayoutManager layoutManager = getLayoutManager();
        mNavigationHelper = new RecyclerViewNavigationHelper(recyclerView, layoutManager,
                adapter, this);

        adapter.setListener(this);
        mPauseOnScrollListener = new PauseRecyclerViewOnScrollListener(adapter.getMediaLoader().getImageLoader(), false, true);

        final Bundle loaderArgs = new Bundle(getArguments());
        loaderArgs.putBoolean(EXTRA_FROM_USER, true);
        getLoaderManager().initLoader(0, loaderArgs, this);
        showProgress();
    }

    protected Object createMessageBusCallback() {
        return new StatusesBusCallback();
    }

    protected abstract long[] getAccountIds();

    protected Data getAdapterData() {
        final AbsStatusesAdapter<Data> adapter = getAdapter();
        return adapter.getData();
    }

    protected void setAdapterData(Data data) {
        final AbsStatusesAdapter<Data> adapter = getAdapter();
        adapter.setData(data);
    }

    protected String getReadPositionTag() {
        return null;
    }

    protected abstract boolean hasMoreData(Data data);

    protected abstract Loader<Data> onCreateStatusesLoader(final Context context, final Bundle args,
                                                           final boolean fromUser);

    protected abstract void onLoadingFinished();

    protected void saveReadPosition(int position) {
        final String readPositionTag = getReadPositionTagWithAccounts();
        if (readPositionTag == null) return;
        if (position == RecyclerView.NO_POSITION) return;
        final AbsStatusesAdapter<Data> adapter = getAdapter();
        final ParcelableStatus status = adapter.getStatus(position);
        if (status == null) return;
        mReadStateManager.setPosition(readPositionTag, status.id);
        mReadStateManager.setPosition(getCurrentReadPositionTag(), status.id, true);
    }

    @NonNull
    @Override
    protected Rect getExtraContentPadding() {
        final int paddingVertical = getResources().getDimensionPixelSize(R.dimen.element_spacing_small);
        return new Rect(0, paddingVertical, 0, paddingVertical);
    }

    private String getCurrentReadPositionTag() {
        final String tag = getReadPositionTagWithAccounts();
        if (tag == null) return null;
        return tag + "_current";
    }

    private String getReadPositionTagWithAccounts() {
        return Utils.getReadPositionTagWithAccounts(getReadPositionTag(), getAccountIds());
    }

    protected final class StatusesBusCallback {

        protected StatusesBusCallback() {
        }

        @Subscribe
        public void notifyStatusListChanged(StatusListChangedEvent event) {
            final AbsStatusesAdapter<Data> adapter = getAdapter();
            adapter.notifyDataSetChanged();
        }

    }
}
