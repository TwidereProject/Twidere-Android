package androidx.fragment.app;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class FragmentAccessor {

	private FragmentAccessor() {
	}

	public static Bundle getSavedFragmentState(final Fragment f) {
		return f.mSavedFragmentState;
	}

}
