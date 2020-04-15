package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.FixedLinearLayoutManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonUtils;
import org.mariotaku.chameleon.ChameleonView;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.decorator.ExtendedDividerItemDecoration;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.iface.PagerIndicator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by mariotaku on 14/10/21.
 */
public class TabPagerIndicator extends RecyclerView implements PagerIndicator, ChameleonView {


    private final int mStripHeight;
    private final TabPagerIndicatorAdapter mIndicatorAdapter;
    private final TabLayoutManager mLayoutManager;
    private final ExtendedDividerItemDecoration mItemDecoration;
    private ViewPager mViewPager;
    private PagerAdapter mPagerProvider;

    private OnPageChangeListener mPageChangeListener;
    private int mOption;
    private int mHorizontalPadding, mVerticalPadding;
    private int mColumns;

    public TabPagerIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ViewCompat.setLayoutDirection(this, ViewCompat.LAYOUT_DIRECTION_LTR);
        final Resources res = getResources();
        mIndicatorAdapter = new TabPagerIndicatorAdapter(this);
        mItemDecoration = new ExtendedDividerItemDecoration(context, HORIZONTAL);
        mStripHeight = res.getDimensionPixelSize(R.dimen.element_spacing_small);
        setOverScrollMode(OVER_SCROLL_NEVER);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setLayoutManager(mLayoutManager = new TabLayoutManager(context, this));
        setItemContext(context);
        setAdapter(mIndicatorAdapter);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabPagerIndicator);
        setTabExpandEnabled(a.getBoolean(R.styleable.TabPagerIndicator_tabExpandEnabled, false));
        setHorizontalPadding(a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabHorizontalPadding, 0));
        setVerticalPadding(a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabVerticalPadding, 0));
        setStripColor(a.getColor(R.styleable.TabPagerIndicator_tabStripColor, 0));
        setIconColor(a.getColor(R.styleable.TabPagerIndicator_tabIconColor, 0));
        setLabelColor(a.getColor(R.styleable.TabPagerIndicator_tabLabelColor, ThemeUtils.INSTANCE.getTextColorPrimary(context)));
        //noinspection WrongConstant
        setTabDisplayOption(a.getInt(R.styleable.TabPagerIndicator_tabDisplayOption, DisplayOption.ICON));
        setTabShowDivider(a.getBoolean(R.styleable.TabPagerIndicator_tabShowDivider, false));
        final int dividerVerticalPadding = a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabDividerVerticalPadding, 0);
        final int dividerHorizontalPadding = a.getDimensionPixelSize(R.styleable.TabPagerIndicator_tabDividerHorizontalPadding, 0);
        mItemDecoration.setPadding(dividerHorizontalPadding, dividerVerticalPadding,
                dividerHorizontalPadding, dividerVerticalPadding);
        mItemDecoration.setDecorationStart(0);
        mItemDecoration.setDecorationEndOffset(1);
        a.recycle();
    }

    public int getCount() {
        return mIndicatorAdapter.getItemCount();
    }

    public Context getItemContext() {
        return mIndicatorAdapter.getItemContext();
    }

    public int getColumns() {
        if (mColumns > 0) return mColumns;
        return 1;
    }

    public void setColumns(int columns) {
        mColumns = columns;
        notifyDataSetChanged();
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
        notifyDataSetChanged();
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

    public void clearBadge() {
        mIndicatorAdapter.clearBadge();
    }

    public void setDisplayBadge(boolean display) {
        mIndicatorAdapter.setDisplayBadge(display);
    }

    public void setIconColor(int color) {
        mIndicatorAdapter.setIconColor(color);
    }

    public void setLabelColor(int color) {
        mIndicatorAdapter.setLabelColor(color);
    }

    public void setStripColor(int color) {
        mIndicatorAdapter.setStripColor(color);
    }

    public void setTabDisplayOption(@DisplayOption int flags) {
        mOption = flags;
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
        view.addOnPageChangeListener(this);
        mIndicatorAdapter.setTabProvider((TabProvider) adapter);
    }

    @Override
    public boolean isPostApplyTheme() {
        return false;
    }

    @Nullable
    @Override
    public Appearance createAppearance(@NonNull Context context, @NonNull AttributeSet attributeSet, @NonNull Chameleon.Theme theme) {
        final Appearance appearance = new Appearance();
        final int toolbarColor = theme.getColorToolbar();
        final int itemColor = ChameleonUtils.getColorDependent(toolbarColor);
        appearance.setLabelColor(itemColor);
        appearance.setIconColor(itemColor);
        if (theme.isToolbarColored()) {
            appearance.setStripColor(itemColor);
        } else {
            appearance.setStripColor(theme.getColorAccent());
        }
        return appearance;
    }

    @Override
    public void applyAppearance(@NonNull ChameleonView.Appearance appearance) {
        Appearance a = (Appearance) appearance;
        setIconColor(a.getIconColor());
        setLabelColor(a.getLabelColor());
        setStripColor(a.getStripColor());
        updateAppearance();
    }

    public void updateAppearance() {
        final int positionStart = mLayoutManager.findFirstVisibleItemPosition();
        final int itemCount = mLayoutManager.findLastVisibleItemPosition() - positionStart + 1;
        mIndicatorAdapter.notifyItemRangeChanged(positionStart, itemCount);
    }

    private int getTabHorizontalPadding() {
        return mHorizontalPadding;
    }

    private int getTabVerticalPadding() {
        return mVerticalPadding;
    }

    private boolean isIconDisplayed() {
        return (mOption & DisplayOption.ICON) != 0;
    }

    private boolean isLabelDisplayed() {
        return (mOption & DisplayOption.LABEL) != 0;
    }

    private boolean isTabExpandEnabled() {
        return mLayoutManager.isTabExpandEnabled();
    }

    public void setTabExpandEnabled(boolean expandEnabled) {
        mLayoutManager.setTabExpandEnabled(expandEnabled);
    }

    private void setHorizontalPadding(int padding) {
        mHorizontalPadding = padding;
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
    }

    private boolean isTabSelected(int position) {
        final int current = getCurrentItem();
        final int columns = getColumns();
        final int count = getCount();
        if (current + columns > count) {
            return position >= count - columns;
        }
        return position >= current && position < current + columns;
    }

    public static class Appearance implements ChameleonView.Appearance {
        @ColorInt
        int iconColor, labelColor, stripColor;

        public int getIconColor() {
            return iconColor;
        }

        public void setIconColor(int iconColor) {
            this.iconColor = iconColor;
        }

        public int getLabelColor() {
            return labelColor;
        }

        public void setLabelColor(int labelColor) {
            this.labelColor = labelColor;
        }

        public int getStripColor() {
            return stripColor;
        }

        public void setStripColor(int stripColor) {
            this.stripColor = stripColor;
        }
    }

    @IntDef({DisplayOption.ICON, DisplayOption.LABEL, DisplayOption.BOTH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface DisplayOption {
        int LABEL = 0x1;
        int ICON = 0x2;
        int BOTH = LABEL | ICON;
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
                final int width = getWidth(), height = getHeight();
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

        TabItemHolder(TabPagerIndicator indicator, View itemView) {
            super(itemView);
            this.indicator = indicator;
            this.itemView = (ItemLayout) itemView;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            iconView = itemView.findViewById(R.id.tab_icon);
            labelView = itemView.findViewById(R.id.tab_label);
            badgeView = itemView.findViewById(R.id.unread_indicator);
        }

        @Override
        public void onClick(View v) {
            indicator.dispatchTabClick(getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            final int position = getLayoutPosition();
            if (position == RecyclerView.NO_POSITION) return false;
            return indicator.dispatchTabLongClick(position);
        }

        void setBadge(int count, boolean display) {
            badgeView.setText(String.valueOf(count));
            badgeView.setVisibility(display && count > 0 ? VISIBLE : GONE);
        }

        void setDisplayOption(boolean iconDisplayed, boolean labelDisplayed) {
            iconView.setVisibility(iconDisplayed ? VISIBLE : GONE);
            labelView.setVisibility(labelDisplayed ? VISIBLE : GONE);
        }

        void setIconColor(int color) {
            if (color != 0) {
                iconView.setColorFilter(color);
            } else {
                iconView.clearColorFilter();
            }
        }

        void setLabelColor(int color) {
            labelView.setTextColor(color);
        }

        void setPadding(int horizontalPadding, int verticalPadding) {
            itemView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        }

        void setStripColor(int color) {
            itemView.setStripColor(color);
        }

        void setStripHeight(int stripHeight) {
            itemView.setStripHeight(stripHeight);
        }

        void setTabData(Drawable icon, CharSequence title, boolean activated) {
            itemView.setContentDescription(title);
            iconView.setImageDrawable(icon);
            labelView.setText(title);
            itemView.setIsCurrent(activated);
        }
    }

    private static class TabLayoutManager extends FixedLinearLayoutManager {

        private boolean mTabExpandEnabled;
        private final RecyclerView mRecyclerView;

        TabLayoutManager(Context context, RecyclerView recyclerView) {
            super(context, HORIZONTAL, false);
            mRecyclerView = recyclerView;
            setAutoMeasureEnabled(true);
        }

        @Override
        public void measureChildWithMargins(View child, int widthUsed, int heightUsed) {
            // first get default measured size
            super.measureChildWithMargins(child, widthUsed, heightUsed);
            if (!isTabExpandEnabled()) return;
            final int count = getItemCount();
            if (count == 0) return;
            final int parentHeight = mRecyclerView.getHeight(), parentWidth = mRecyclerView.getWidth();
            final int decoratedWidth = getDecoratedMeasuredWidth(child);
            final int measuredWidth = child.getMeasuredWidth();
            final int decoratorWidth = decoratedWidth - measuredWidth;
            final int width = Math.max(measuredWidth, parentWidth / count - decoratorWidth);
            final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.EXACTLY);
            final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            child.measure(widthMeasureSpec, heightMeasureSpec);
        }

        @Override
        protected boolean isLayoutRTL() {
            return false;
        }

        boolean isTabExpandEnabled() {
            return mTabExpandEnabled;
        }

        void setTabExpandEnabled(boolean tabExpandEnabled) {
            mTabExpandEnabled = tabExpandEnabled;
        }
    }

    private static class TabPagerIndicatorAdapter extends Adapter<TabItemHolder> {

        private final TabPagerIndicator mIndicator;
        private final SparseIntArray mUnreadCounts;
        private Context mItemContext;
        private LayoutInflater mInflater;

        private TabProvider mTabProvider;
        private int mStripColor, mIconColor, mLabelColor;
        private boolean mDisplayBadge;

        TabPagerIndicatorAdapter(TabPagerIndicator indicator) {
            mIndicator = indicator;
            mUnreadCounts = new SparseIntArray();
        }

        Context getItemContext() {
            return mItemContext;
        }

        void setItemContext(Context itemContext) {
            mItemContext = itemContext;
            mInflater = LayoutInflater.from(itemContext);
        }

        @Override
        public TabItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (mIndicator == null) throw new IllegalStateException("item context not set");
            final View view = mInflater.inflate(R.layout.layout_tab_item, parent, false);
            final TabItemHolder holder = new TabItemHolder(mIndicator, view);
            holder.setStripHeight(mIndicator.getStripHeight());
            holder.setPadding(mIndicator.getTabHorizontalPadding(), mIndicator.getTabVerticalPadding());
            holder.setDisplayOption(mIndicator.isIconDisplayed(), mIndicator.isLabelDisplayed());
            return holder;
        }

        @Override
        public void onBindViewHolder(TabItemHolder holder, int position) {
            final Drawable icon = mTabProvider.getPageIcon(position);
            final CharSequence title = mTabProvider.getPageTitle(position);
            holder.setTabData(icon, title, mIndicator.isTabSelected(position));
            holder.setBadge(mUnreadCounts.get(position, 0), mDisplayBadge);

            holder.setStripColor(mStripColor);
            holder.setIconColor(mIconColor);
            holder.setLabelColor(mLabelColor);
        }

        @Override
        public int getItemCount() {
            if (mTabProvider == null) return 0;
            return mTabProvider.getCount();
        }

        void setBadge(int position, int count) {
            mUnreadCounts.put(position, count);
            notifyItemChanged(position);
        }

        void clearBadge() {
            mUnreadCounts.clear();
            notifyDataSetChanged();
        }

        void setDisplayBadge(boolean display) {
            mDisplayBadge = display;
        }

        void setIconColor(int color) {
            mIconColor = color;
        }

        void setLabelColor(int color) {
            mLabelColor = color;
        }

        void setStripColor(int color) {
            mStripColor = color;
        }

        void setTabProvider(TabProvider tabProvider) {
            mTabProvider = tabProvider;
        }
    }
}
