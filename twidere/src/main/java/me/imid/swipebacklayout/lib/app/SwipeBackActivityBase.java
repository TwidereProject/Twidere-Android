package me.imid.swipebacklayout.lib.app;

import me.imid.swipebacklayout.lib.SwipeBackLayout;

/**
 * @author Yrom
 */
public interface SwipeBackActivityBase {
	/**
	 * @return the SwipeBackLayout associated with this activity.
	 */
	public abstract SwipeBackLayout getSwipeBackLayout();

	/**
	 * Scroll out contentView and finish the activity
	 */
	public abstract void scrollToFinishActivity();

	public abstract void setSwipeBackEnable(boolean enable);

}
