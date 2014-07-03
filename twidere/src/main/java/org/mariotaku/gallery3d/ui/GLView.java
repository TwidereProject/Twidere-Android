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

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.view.MotionEvent;

import org.mariotaku.gallery3d.anim.CanvasAnimation;
import org.mariotaku.gallery3d.util.GalleryUtils;

import java.util.ArrayList;

// GLView is a UI component. It can render to a GLCanvas and accept touch
// events. A GLView may have zero or more child GLView and they form a tree
// structure. The rendering and event handling will pass through the tree
// structure.
//
// A GLView tree should be attached to a GLRoot before event dispatching and
// rendering happens. GLView asks GLRoot to re-render or re-layout the
// GLView hierarchy using requestRender() and requestLayoutContentPane().
//
// The render() method is called in a separate thread. Before calling
// dispatchTouchEvent() and layout(), GLRoot acquires a lock to avoid the
// rendering thread running at the same time. If there are other entry points
// from main thread (like a Handler) in your GLView, you need to call
// lockRendering() if the rendering thread should not run at the same time.
//
public class GLView {
	public static final int VISIBLE = 0;
	public static final int INVISIBLE = 1;

	private static final int FLAG_INVISIBLE = 1;
	private static final int FLAG_LAYOUT_REQUESTED = 4;

	protected final Rect mBounds = new Rect();

	protected final Rect mPaddings = new Rect();
	private GLRoot mRoot;

	protected GLView mParent;
	private ArrayList<GLView> mComponents;
	private GLView mMotionTarget;
	private CanvasAnimation mAnimation;

	private int mViewFlags = 0;

	protected int mMeasuredWidth = 0;

	protected int mMeasuredHeight = 0;
	protected int mScrollY = 0;

	protected int mScrollX = 0;
	private float[] mBackgroundColor;

	// Adds a child to this GLView.
	public void addComponent(final GLView component) {
		// Make sure the component doesn't have a parent currently.
		if (component.mParent != null) throw new IllegalStateException();

		// Build parent-child links
		if (mComponents == null) {
			mComponents = new ArrayList<GLView>();
		}
		mComponents.add(component);
		component.mParent = this;

		// If this is added after we have a root, tell the component.
		if (mRoot != null) {
			component.onAttachToRoot(mRoot);
		}
	}

	// This should only be called on the content pane (the topmost GLView).
	public void attachToRoot(final GLRoot root) {
		GalleryUtils.assertTrue(mParent == null && mRoot == null);
		onAttachToRoot(root);
	}

	// This should only be called on the content pane (the topmost GLView).
	public void detachFromRoot() {
		GalleryUtils.assertTrue(mParent == null && mRoot != null);
		onDetachFromRoot();
	}

	public float[] getBackgroundColor() {
		return mBackgroundColor;
	}

	// Returns the number of children of the GLView.
	public int getComponentCount() {
		return mComponents == null ? 0 : mComponents.size();
	}

	public GLRoot getGLRoot() {
		return mRoot;
	}

	public int getHeight() {
		return mBounds.bottom - mBounds.top;
	}

	public int getMeasuredHeight() {
		return mMeasuredHeight;
	}

	public int getMeasuredWidth() {
		return mMeasuredWidth;
	}

	public Rect getPaddings() {
		return mPaddings;
	}

	// Returns GLView.VISIBLE or GLView.INVISIBLE
	public int getVisibility() {
		return (mViewFlags & FLAG_INVISIBLE) == 0 ? VISIBLE : INVISIBLE;
	}

	public int getWidth() {
		return mBounds.right - mBounds.left;
	}

	// Request re-rendering of the view hierarchy.
	// This is used for animation or when the contents changed.
	public void invalidate() {
		final GLRoot root = getGLRoot();
		if (root != null) {
			root.requestRender();
		}
	}

	@SuppressLint("WrongCall")
	public void layout(final int left, final int top, final int right, final int bottom) {
		final boolean sizeChanged = setBounds(left, top, right, bottom);
		mViewFlags &= ~FLAG_LAYOUT_REQUESTED;
		// We call onLayout no matter sizeChanged is true or not because the
		// orientation may change without changing the size of the View (for
		// example, rotate the device by 180 degrees), and we want to handle
		// orientation change in onLayout.
		onLayout(sizeChanged, left, top, right, bottom);
	}

	public void setBackgroundColor(final float[] color) {
		mBackgroundColor = color;
	}

	// Sets the visiblity of this GLView (either GLView.VISIBLE or
	// GLView.INVISIBLE).
	public void setVisibility(final int visibility) {
		if (visibility == getVisibility()) return;
		if (visibility == VISIBLE) {
			mViewFlags &= ~FLAG_INVISIBLE;
		} else {
			mViewFlags |= FLAG_INVISIBLE;
		}
		onVisibilityChanged(visibility);
		invalidate();
	}

	protected boolean dispatchTouchEvent(final MotionEvent event) {
		final int x = (int) event.getX();
		final int y = (int) event.getY();
		final int action = event.getAction();
		if (mMotionTarget != null) {
			if (action == MotionEvent.ACTION_DOWN) {
				final MotionEvent cancel = MotionEvent.obtain(event);
				cancel.setAction(MotionEvent.ACTION_CANCEL);
				dispatchTouchEvent(cancel, x, y, mMotionTarget, false);
				mMotionTarget = null;
			} else {
				dispatchTouchEvent(event, x, y, mMotionTarget, false);
				if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
					mMotionTarget = null;
				}
				return true;
			}
		}
		if (action == MotionEvent.ACTION_DOWN) {
			// in the reverse rendering order
			for (int i = getComponentCount() - 1; i >= 0; --i) {
				final GLView component = getComponent(i);
				if (component.getVisibility() != GLView.VISIBLE) {
					continue;
				}
				if (dispatchTouchEvent(event, x, y, component, true)) {
					mMotionTarget = component;
					return true;
				}
			}
		}
		return onTouch(event);
	}

	protected void onLayout(final boolean changeSize, final int left, final int top, final int right, final int bottom) {
	}

	protected boolean onTouch(final MotionEvent event) {
		return false;
	}

	protected void render(final GLCanvas canvas) {
		renderBackground(canvas);
		canvas.save();
		for (int i = 0, n = getComponentCount(); i < n; ++i) {
			renderChild(canvas, getComponent(i));
		}
		canvas.restore();
	}

	protected void renderChild(final GLCanvas canvas, final GLView component) {
		if (component.getVisibility() != GLView.VISIBLE && component.mAnimation == null) return;

		final int xoffset = component.mBounds.left - mScrollX;
		final int yoffset = component.mBounds.top - mScrollY;

		canvas.translate(xoffset, yoffset);

		final CanvasAnimation anim = component.mAnimation;
		if (anim != null) {
			canvas.save(anim.getCanvasSaveFlags());
			if (anim.calculate(AnimationTime.get())) {
				invalidate();
			} else {
				component.mAnimation = null;
			}
			anim.apply(canvas);
		}
		component.render(canvas);
		if (anim != null) {
			canvas.restore();
		}
		canvas.translate(-xoffset, -yoffset);
	}

	private boolean dispatchTouchEvent(final MotionEvent event, final int x, final int y, final GLView component,
			final boolean checkBounds) {
		final Rect rect = component.mBounds;
		final int left = rect.left;
		final int top = rect.top;
		if (!checkBounds || rect.contains(x, y)) {
			event.offsetLocation(-left, -top);
			if (component.dispatchTouchEvent(event)) {
				event.offsetLocation(left, top);
				return true;
			}
			event.offsetLocation(left, top);
		}
		return false;
	}

	// Returns the children for the given index.
	private GLView getComponent(final int index) {
		if (mComponents == null) throw new ArrayIndexOutOfBoundsException(index);
		return mComponents.get(index);
	}

	private void onAttachToRoot(final GLRoot root) {
		mRoot = root;
		for (int i = 0, n = getComponentCount(); i < n; ++i) {
			getComponent(i).onAttachToRoot(root);
		}
	}

	private void onDetachFromRoot() {
		for (int i = 0, n = getComponentCount(); i < n; ++i) {
			getComponent(i).onDetachFromRoot();
		}
		mRoot = null;
	}

	private void onVisibilityChanged(final int visibility) {
		for (int i = 0, n = getComponentCount(); i < n; ++i) {
			final GLView child = getComponent(i);
			if (child.getVisibility() == GLView.VISIBLE) {
				child.onVisibilityChanged(visibility);
			}
		}
	}

	private void renderBackground(final GLCanvas view) {
		if (mBackgroundColor != null) {
			view.clearBuffer(mBackgroundColor);
		}
	}

	private boolean setBounds(final int left, final int top, final int right, final int bottom) {
		final boolean sizeChanged = right - left != mBounds.right - mBounds.left
				|| bottom - top != mBounds.bottom - mBounds.top;
		mBounds.set(left, top, right, bottom);
		return sizeChanged;
	}
}
