package android.support.v4.app;

import android.os.Bundle;

public class FragmentTrojan {

	public static Bundle getSavedFragmentState(final Fragment f) {
		return f.mSavedFragmentState;
	}

}
