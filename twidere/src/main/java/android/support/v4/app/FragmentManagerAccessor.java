package android.support.v4.app;

import android.support.v4.view.LayoutInflaterFactory;

public class FragmentManagerAccessor {

    private FragmentManagerAccessor() {
    }

    public static boolean isStateSaved(final FragmentManager fm) {
        if (fm instanceof FragmentManagerImpl) return ((FragmentManagerImpl) fm).mStateSaved;
        return false;
    }

    public static LayoutInflaterFactory getLayoutInflaterFactory(final FragmentManager fm) {
        if (fm instanceof FragmentManagerImpl)
            return ((FragmentManagerImpl) fm).getLayoutInflaterFactory();
        return null;
    }
}
