package android.support.v4.app;

public class FragmentManagerTrojan {

	public static boolean isStateSaved(final FragmentManager fm) {
		if (fm instanceof FragmentManagerImpl) return ((FragmentManagerImpl) fm).mStateSaved;
		return false;
	}
}
