/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.gallery3d.ui;

import android.os.Handler;
import android.os.Message;

public class SynchronizedHandler extends Handler {

	protected final GLRoot mGLRoot;

	public SynchronizedHandler(final GLRoot glRoot) {
		mGLRoot = glRoot;
	}

	@Override
	public void dispatchMessage(final Message message) {
		if (mGLRoot != null) {
			mGLRoot.lockRenderThread();
		}
		try {
			super.dispatchMessage(message);
		} finally {
			if (mGLRoot != null) {
				mGLRoot.unlockRenderThread();
			}
		}
	}

}
