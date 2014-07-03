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

import android.content.Context;
import android.graphics.Matrix;

public interface GLRoot {

	public void addOnGLIdleListener(OnGLIdleListener listener);

	public void freeze();

	public int getCompensation();

	public Matrix getCompensationMatrix();

	public Context getContext();

	public int getDisplayRotation();

	public void lockRenderThread();

	public void requestLayoutContentPane();

	public void requestRender();

	public void setContentPane(GLView content);

	public void unfreeze();

	public void unlockRenderThread();

	// Listener will be called when GL is idle AND before each frame.
	// Mainly used for uploading textures.
	public static interface OnGLIdleListener {
		public boolean onGLIdle(GLCanvas canvas, boolean renderRequested);
	}
}
