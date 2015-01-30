package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.decorator.DividerItemDecoration;
import org.mariotaku.twidere.view.iface.PagerIndicator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 14/10/21.
 */
public class TabPagerIndicator extends RecyclerView implements PagerIndicator {

    public static final int ICON = 0x1;
    public static final int LABEL = 0x2;
    public static final int BOTH = ICON | LABEL;

    private final int mStripHeight;
    private final TabPagerIndicatorAdapter mIndicatorAdapter;
    private final TabLayoutManager mLayoutManager;
    private final DividerItemDecoration mItemDecoration;
    private ViewPager mViewPager;
    private PagerAdapter mPagerProvider;

    private OnPageChangeListener mPageChangeListener;
    private int mOption;
    private boolean mTabExpandEnabled;
    private int mHorizontalPadding, mVerticalPadding;

    public TabPagerIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final Resources res = getResources();
        mIndicatorAdapter = new TabPagerIndicatorAdapter(this);
        mItemDecoration = new DividerItemDecoration(context, HORIZONTAL);
        mStripHeight = res.getDimensionPixelSize(R.dimen.element_spacing_small);
        ViewCompat.setOverScrollMode(this, ViewCompat.OVER_SCROLL_NEVER);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setLayoutManager(mLayoutManager = new TabLayoutManager(this));
        setItemContext(context);
        setAdapter(mIndicatorAdapter);
        setTabDisplayOption(ICON);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabPagerIndicator);
        setTabExpandEnabled(a.getBoolean(R.styleable.TabPagerIndicator_tabExpandEnabled, false));
        setHorizontalPadding(a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabHorizontalPadding, 0));
        setVerticalPadding(a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabHorizontalPadding, 0));
        setStripColor(a.getColor(R.styleable.TabPagerIndicator_tabStripColor, 0));
        setIconColor(a.getColor(R.styleable.TabPagerIndicator_tabIconColor, 0));
        setTabDisplayOption(a.getInt(R.styleable.TabPagerIndicator_tabDisplayOption, ICON));
        setTabShowDivider(a.getBoolean(R.styleable.TabPagerIndicator_tabShowDivider, false));
        final int dividerVerticalPadding = a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabDividerVerticalPadding, 0);
        final int dividerHorizontalPadding = a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabDividerHorizontalPadding, 0);
        mItemDecoration.setPadding(dividerHorizontalPadding, dividerVerticalPadding,
                dividerHorizontalPadding, dividerVerticalPadding);
        mItemDecoration.setDecorationStart(0);
        mItemDecoration.setDecorationEndOffset(1);
        a.recycle();
    }

    public TabPagerIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public TabPagerIndicator(Context context) {
        this(context, null);
    }

    public int getCount() {
        return mIndicatorAdapter.getItemCount();
    }

    public Context getItemContext() {
        return mIndicatorAdapter.getItemContext();
    }

    public void setItemContext(Context context) {
        mIndicatorAdapter.setItemContext(context);
    }

    public int getStripHeight() {
        return mStripHeight;
    }

    @Override
    public void notifyDataSetChanged() {
        mIndicatorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mPageChangeListener == null) return;
        mPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        mIndicatorAdapter.notifyDataSetChanged();
        if (mPageChangeListener == null) return;
        smoothScrollToPosition(position);
        mPageChangeListener.onPageSelected(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (mPageChangeListener == null) return;
        mPageChangeListener.onPageScrollStateChanged(state);
    }

    public void setBadge(int position, int count) {
        mIndicatorAdapter.setBadge(position, count);
    }

    public void setDisplayBadge(boolean display) {
        mIndicatorAdapter.setDisplayBadge(display);
    }

    public void setIconColor(int color) {
        mIndicatorAdapter.setIconColor(color);
    }

    public void setStripColor(int color) {
        mIndicatorAdapter.setStripColor(color);
    }

    @DisplayOption
    public void setTabDisplayOption(int flags) {
        mOption = flags;
        notifyDataSetChanged();
    }

    private void dispatchTabClick(int position) {
        final int currentItem = getCurrentItem();
        setCurrentItem(position);
        if (mPagerProvider instanceof TabListener) {
            if (currentItem != position) {
                ((TabListener) mPagerProvider).onPageSelected(position);
            } else {
                ((TabListener) mPagerProvider).onPageReselected(position);
            }
        }
    }

    private boolean dispatchTabLongClick(int position) {
        if (mPagerProvider instanceof TabListener) {
            return ((TabListener) mPagerProvider).onTabLongClick(position);
        }
        return false;
    }

    private int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    @Override
    public void setCurrentItem(int item) {
        mViewPager.setCurrentItem(item);
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
        mPagerProvider = adapter;
        view.setOnPageChangeListener(this);
        mIndicatorAdapter.setTabProvider((TabProvider) adapter);
    }

    private int getTabHorizontalPadding() {
        return mHorizontalPadding;
    }

    private int getTabVerticalPadding() {
        return mVerticalPadding;
    }

    private boolean isIconDisplayed() {
        return (mOption & ICON) != 0;
    }

    private boolean isLabelDisplayed() {
        return (mOption & LABEL) != 0;
    }

    private boolean isTabExpandEnabled() {
        return mTabExpandEnabled;
    }

    public void setTabExpandEnabled(boolean expandEnabled) {
        mTabExpandEnabled = expandEnabled;
    }

    private void setHorizontalPadding(int padding) {
        mHorizontalPadding = padding;
        notifyDataSetChanged();
    }

    private void setTabShowDivider(boolean showDivider) {
        if (showDivider) {
            addItemDecoration(mItemDecoration);
        } else {
            removeItemDecoration(mItemDecoration);
        }
    }

    private void setVerticalPadding(int padding) {
        mVerticalPadding = padding;
        notifyDataSetChanged();
    }

    @IntDef({ICON, LABEL, BOTH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DisplayOption {
    }

    public static final class ItemLayout extends RelativeLayout {

        private final Paint mStripPaint;

        private boolean mIsCurrent;
        private int mStripColor;
        private int mStripHeight;

        public ItemLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            mStripPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            setWillNotDraw(false);
        }

        public void setIsCurrent(boolean isCurrent) {
            if (mIsCurrent == isCurrent) return;
            mIsCurrent = isCurrent;
            invalidate();
        }

        public void setStripColor(int stripColor) {
            if (mStripColor == stripColor) return;
            mStripColor = stripColor;
            mStripPaint.setColor(stripColor);
            invalidate();
        }

        public void setStripHeight(int stripHeight) {
            if (mStripHeight == stripHeight) return;
            mStripHeight = stripHeight;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            if (mIsCurrent) {
                final int width = canvas.getWidth(), height = canvas.getHeight();
                canvas.drawRect(0, height - mStripHeight, width, height, mStripPaint);
            }
            super.onDraw(canvas);
        }
    }

    private static class TabItemHolder extends ViewHolder implements OnClickListener, OnLongClickListener {

        private final TabPagerIndicator indicator;
        private final ItemLayout itemView;
        private final ImageView iconView;
        private final TextView labelView;
        private final BadgeView badgeView;

        public TabItemHolder(TabPagerIndicator indicator, View itemView) {
            super(itemView);
            this.indicator = indicator;
            this.itemView = (ItemLayout) itemView;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            iconView = (ImageView) itemView.findViewById(R.id.tab_icon);
            labelView = (TextView) itemView.findViewById(R.id.tab_label);
            badgeView = (BadgeView) itemView.findViewById(R.id.unread_indicator);
        }

        @Override
        public void onClick(View v) {
            indicator.dispatchTabClick(getPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            return indicator.dispatchTabLongClick(getPosition());
        }

        public void setBadge(int count, boolean display) {
            badgeView.setText(String.valueOf(count));
            badgeView.setVisibility(display && count > 0 ? VISIBLE : GONE);
        }

        public void setDisplayOption(boolean iconDisplayed, boolean labelDisplayed) {
            iconView.setVisibility(iconDisplayed ? VISIBLE : GONE);
            labelView.setVisibility(labelDisplayed ? VISIBLE : GONE);
        }

        public void setIconColor(int color) {
            if (color != 0) {
                iconView.setColorFilter(color);
            } else {
                iconView.clearColorFilter();
            }
        }

        public void setPadding(int horizontalPadding, int verticalPadding) {
            itemView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        }

        public void setStripColor(int color) {
            itemView.setStripColor(color);
        }

        public void setStripHeight(int stripHeight) {
            itemView.setStripHeight(stripHeight);
        }

        public void setTabData(Drawable icon, CharSequence title, boolean activated) {
            itemView.setContentDescription(title);
            iconView.setImageDrawable(icon);
            labelView.setText(title);
            itemView.setIsCurrent(activated);
        }
    }

    private static class TabLayoutManager extends LinearLayoutManager {

        private final TabPagerIndicator mIndicator;

        public TabLayoutManager(TabPagerIndicator indicator) {
            super(indicator.getContext(), HORIZONTAL, false);
            mIndicator = indicator;
        }

        @Override
        public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
            // first get default measured size
            super.measureChildWithMargins(child, widthUsed, heightUsed);
            if (!mIndicator.isTabExpandEnabled()) return;
            final int count = mIndicator.getCount();
            if (count == 0) return;
            final int parentHeight = mIndicator.getHeight(), parentWidth = mIndicator.getWidth();
            final int decoratedWidth = getDecoratedMeasuredWidth(child);
            final int decoratorWidth = decoratedWidth - child.getMeasuredWidth();
            final int width = Math.max(parentWidth / count - decoratorWidth, decoratedWidth);
            final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.EXACTLY);
            final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            child.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private static class TabPagerIndicatorAdapter extends Adapter<TabItemHolder> {

        private final TabPagerIndicator mIndicator;
        private final SparseIntArray mUnreadCounts;
        private Context mItemContext;
        private LayoutInflater mInflater;

        private TabProvider mTabProvider;
        private int mStripColor, mIconColor;
        private boolean mDisplayBadge;

        public TabPagerIndicatorAdapter(TabPagerIndicator indicator) {
            mIndicator = indicator;
            mUnreadCounts = new SparseIntArray();
        }

        public Context getItemContext() {
            return mItemContext;
        }

        public void setItemContext(Context itemContext) {
            mItemContext = itemContext;
            mInflater = LayoutInflater.from(itemContext);
        }

        @Override
        public TabItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (mIndicator == null) throw new IllegalStateException("item context not set");
            final View view = mInflater.inflate(R.layout.layout_tab_item, parent, false);
            return new TabItemHolder(mIndicator, view);
        }

        @Override
        public void onBindViewHolder(TabItemHolder holder, int position) {
            final Drawable icon = mTabProvider.getPageIcon(position);
            final CharSequence title = mTabProvider.getPageTitle(position);
            holder.setTabData(icon, title, mIndicator.getCurrentItem() == position);
            holder.setPadding(mIndicator.getTabHorizontalPadding(), mIndicator.getTabVerticalPadding());
            holder.setStripHeight(mIndicator.getStripHeight());
            holder.setStripColor(mStripColor);
            holder.setIconColor(mIconColor);
            holder.setBadge(mUnreadCounts.get(position, 0), mDisplayBadge);
            holder.setDisplayOption(mIndicator.isIconDisplayed(), mIndicator.isLabelDisplayed());
        }

        @Override
        public int getItemCount() {
            if (mTabProvider == null) return 0;
            return mTabProvider.getCount();
        }

        public void setBadge(int position, int count) {
            mUnreadCounts.put(position, count);
            notifyDataSetChanged();
        }

        public void setDisplayBadge(boolean display) {
            mDisplayBadge = display;
            notifyDataSetChanged();
        }

        public void setIconColor(int color) {
            mIconColor = color;
            notifyDataSetChanged();
        }

        public void setStripColor(int color) {
            mStripColor = color;
            notifyDataSetChanged();
        }

        public void setTabProvider(TabProvider tabProvider) {
            mTabProvider = tabProvider;
            notifyDataSetChanged();
        }
    }
}
