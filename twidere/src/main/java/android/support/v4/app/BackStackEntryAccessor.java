package android.support.v4.app;

public class BackStackEntryAccessor {

	private BackStackEntryAccessor() {
	}

	public static Fragment getFragmentInBackStackRecord(final FragmentManager.BackStackEntry entry) {
		if (entry instanceof BackStackRecord) return ((BackStackRecord) entry).mHead.fragment;
		return null;
	}
}
