package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.iface.PagerIndicator;

/**
 * Created by mariotaku on 14/10/21.
 */
public class TabPagerIndicator extends RecyclerView implements PagerIndicator {

    private final TabPagerIndicatorAdapter mAdapter;
    private PagerAdapter mTabProvider;
    private ViewPager mViewPager;
    private OnPageChangeListener mPageChangeListener;

    public TabPagerIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new TabPagerIndicatorAdapter(this);
        setAdapter(mAdapter);
    }

    public TabPagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabPagerIndicator(Context context) {
        this(context, null);
    }

    @Override
    public void notifyDataSetChanged() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void setCurrentItem(int item) {

    }

    @Override
    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mPageChangeListener = listener;
    }

    @Override
    public void setViewPager(ViewPager view) {
        setViewPager(view, view.getCurrentItem());
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        final PagerAdapter adapter = view.getAdapter();
        if (!(adapter instanceof TabProvider)) {
            throw new IllegalArgumentException();
        }
        mViewPager = view;
        view.setOnPageChangeListener(this);
        mAdapter.setTabProvider((TabProvider) adapter);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPageChangeListener == null) return;
        mPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        if (mPageChangeListener == null) return;
        mPageChangeListener.onPageSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mPageChangeListener == null) return;
        mPageChangeListener.onPageScrollStateChanged(state);
    }

    public int getCount() {
        return mAdapter.getItemCount();
    }

    public void setBadge(int position, int count) {

    }

    public void setDisplayLabel(boolean display) {

    }

    public void setDisplayIcon(boolean display) {

    }

    private static class TabPagerIndicatorAdapter extends Adapter<TabItemHolder> {

        private final TabPagerIndicator mIndicator;
        private final LayoutInflater mInflater;

        private TabProvider mTabProvider;

        public TabPagerIndicatorAdapter(TabPagerIndicator indicator) {
            mIndicator = indicator;
            mInflater = LayoutInflater.from(indicator.getContext());
        }

        @Override
        public TabItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TabItemHolder(mInflater.inflate(R.layout.tab_item_home, parent, false));
        }

        @Override
        public void onBindViewHolder(TabItemHolder holder, int position) {
            holder.setTabData(mTabProvider.getPageIcon(position), mTabProvider.getPageTitle(position));
        }

        @Override
        public int getItemCount() {
            if (mTabProvider == null) return 0;
            return mTabProvider.getCount();
        }

        public void setTabProvider(TabProvider tabProvider) {
            mTabProvider = tabProvider;
            notifyDataSetChanged();
        }
    }

    private static class TabItemHolder extends ViewHolder {

        private final ImageView iconView;

        public TabItemHolder(View itemView) {
            super(itemView);
            iconView = (ImageView) itemView.findViewById(android.R.id.icon);
        }


        public void setTabData(Drawable icon, CharSequence title) {
            iconView.setImageDrawable(icon);
            iconView.setContentDescription(title);
        }
    }
}
