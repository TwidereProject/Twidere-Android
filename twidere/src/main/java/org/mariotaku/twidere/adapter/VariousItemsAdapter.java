package org.mariotaku.twidere.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.holder.StatusViewHolder;
import org.mariotaku.twidere.view.holder.UserListViewHolder;
import org.mariotaku.twidere.view.holder.UserViewHolder;

import java.util.List;

/**
 * Created by mariotaku on 16/3/20.
 */
public class VariousItemsAdapter extends LoadMoreSupportAdapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_STATUS = 1;
    public static final int VIEW_TYPE_USER = 2;
    public static final int VIEW_TYPE_USER_LIST = 3;

    private final boolean mCompact;
    private final LayoutInflater mInflater;
    private final int mCardBackgroundColor;
    private final DummyItemAdapter mDummyAdapter;

    private List<?> mData;

    public VariousItemsAdapter(Context context, boolean compact) {
        super(context);
        mCompact = compact;
        mInflater = LayoutInflater.from(context);
        mCardBackgroundColor = ThemeUtils.getCardBackgroundColor(context,
                ThemeUtils.getThemeBackgroundOption(context),
                ThemeUtils.getUserThemeBackgroundAlpha(context));
        mDummyAdapter = new DummyItemAdapter(context, new TwidereLinkify(null), this);
        mDummyAdapter.updateOptions();
        setLoadMoreIndicatorPosition(IndicatorPosition.NONE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_STATUS: {
                return ListParcelableStatusesAdapter.createStatusViewHolder(mDummyAdapter,
                        mInflater, parent, mCompact, mCardBackgroundColor);
            }
            case VIEW_TYPE_USER: {
                return ParcelableUsersAdapter.createUserViewHolder(mDummyAdapter, mInflater, parent,
                        mCardBackgroundColor);
            }
            case VIEW_TYPE_USER_LIST: {
                return ParcelableUserListsAdapter.createUserListViewHolder(mDummyAdapter, mInflater,
                        parent, mCardBackgroundColor);
            }
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final Object obj = mData.get(position);
        switch (getItemViewType(obj)) {
            case VIEW_TYPE_STATUS: {
                ((StatusViewHolder) holder).displayStatus((ParcelableStatus) obj, true);
                break;
            }
            case VIEW_TYPE_USER: {
                ((UserViewHolder) holder).displayUser((ParcelableUser) obj);
                break;
            }
            case VIEW_TYPE_USER_LIST: {
                ((UserListViewHolder) holder).displayUserList((ParcelableUserList) obj);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = mData.get(position);
        return getItemViewType(obj);
    }

    protected int getItemViewType(Object obj) {
        if (obj instanceof ParcelableStatus) {
            return VIEW_TYPE_STATUS;
        } else if (obj instanceof ParcelableUser) {
            return VIEW_TYPE_USER;
        } else if (obj instanceof ParcelableUserList) {
            return VIEW_TYPE_USER_LIST;
        }
        throw new UnsupportedOperationException("Unsupported object " + obj);
    }

    public void setData(List<?> data) {
        mData = data;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mData == null) return 0;
        return mData.size();
    }

    public Object getItem(int position) {
        return mData.get(position);
    }

    public DummyItemAdapter getDummyAdapter() {
        return mDummyAdapter;
    }
}
