package org.mariotaku.jsonserializer;

import org.mariotaku.twidere.extension.streaming.BuildConfig;

public class JSONSerializerUtils {

	public static boolean isDebugBuild() {
		return BuildConfig.DEBUG;
	}

}
