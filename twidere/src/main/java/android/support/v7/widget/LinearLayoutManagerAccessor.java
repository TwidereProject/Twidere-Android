package android.support.v7.widget;

import android.view.View;

/**
 * Created by mariotaku on 16/2/4.
 */
public class LinearLayoutManagerAccessor {

    public static OrientationHelper getOrientationHelper(LinearLayoutManager llm) {
        return llm.mOrientationHelper;
    }

    public static void ensureLayoutState(LinearLayoutManager llm) {
        llm.ensureLayoutState();
    }

    public static boolean getShouldReverseLayout(LinearLayoutManager llm) {
        return llm.mShouldReverseLayout;
    }

    public static View findOneVisibleChild(LinearLayoutManager llm, int fromIndex, int toIndex, boolean completelyVisible, boolean acceptPartiallyVisible) {
        return llm.findOneVisibleChild(fromIndex, toIndex, completelyVisible, acceptPartiallyVisible);
    }
}
