/*
 * Copyright 2012 - 2013 Benjamin Weiss
 * Copyright 2012 Neofonie Mobile GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.keyboardsurfer.android.widget.crouton;

import static org.mariotaku.twidere.util.Utils.announceForAccessibilityCompat;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/** Manages the lifecycle of {@link Crouton}s. */
final class CroutonManager extends Handler {
	private static CroutonManager INSTANCE;

	private final Queue<Crouton> croutonQueue;

	private CroutonManager() {
		croutonQueue = new LinkedBlockingQueue<Crouton>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler#handleMessage(android.os.Message)
	 */
	@Override
	public void handleMessage(final Message message) {
		final Crouton crouton = (Crouton) message.obj;

		switch (message.what) {
			case Messages.DISPLAY_CROUTON: {
				displayCrouton();
				break;
			}
			case Messages.ADD_CROUTON_TO_VIEW: {
				addCroutonToView(crouton);
				break;
			}
			case Messages.REMOVE_CROUTON: {
				removeCrouton(crouton);
				if (null != crouton.getLifecycleCallback()) {
					crouton.getLifecycleCallback().onRemoved();
				}
				break;
			}
			default: {
				super.handleMessage(message);
				break;
			}
		}
	}

	@Override
	public String toString() {
		return "CroutonManager{croutonQueue=" + croutonQueue + '}';
	}

	/**
	 * Removes the {@link Crouton}'s view after it's display
	 * durationInMilliseconds.
	 * 
	 * @param crouton The {@link Crouton} added to a {@link ViewGroup} and
	 *            should be removed.
	 */
	protected void removeCrouton(final Crouton crouton) {
		final View croutonView = crouton.getView();
		final ViewGroup croutonParentView = (ViewGroup) croutonView.getParent();

		if (croutonParentView == null) return;
		croutonView.startAnimation(crouton.getOutAnimation());

		// Remove the Crouton from the queue.
		final Crouton removed = croutonQueue.poll();

		// Remove the crouton from the view's parent.
		croutonParentView.removeView(croutonView);
		if (null != removed) {
			removed.detachActivity();
			removed.detachViewGroup();
			if (null != removed.getLifecycleCallback()) {
				removed.getLifecycleCallback().onRemoved();
			}
			removed.detachLifecycleCallback();
		}

		// Send a message to display the next crouton but delay it by the out
		// animation duration to make sure it finishes
		sendMessageDelayed(crouton, Messages.DISPLAY_CROUTON, crouton.getOutAnimation().getDuration());
	}

	/**
	 * Adds a {@link Crouton} to the {@link ViewParent} of it's {@link Activity}
	 * .
	 * 
	 * @param crouton The {@link Crouton} that should be added.
	 */
	private void addCroutonToView(final Crouton crouton) {
		// don't add if it is already showing
		if (crouton.isShowing()) return;

		final View croutonView = crouton.getView();
		if (null == croutonView.getParent()) {
			ViewGroup.LayoutParams params = croutonView.getLayoutParams();
			if (null == params) {
				params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
			}
			// display Crouton in ViewGroup is it has been supplied
			if (null != crouton.getViewGroup()) {
				// TODO implement add to last position feature (need to align
				// with how this will be requested for activity)
				if (crouton.getViewGroup() instanceof FrameLayout) {
					crouton.getViewGroup().addView(croutonView, params);
				} else {
					crouton.getViewGroup().addView(croutonView, 0, params);
				}
			} else {
				final Activity activity = crouton.getActivity();
				if (null == activity || activity.isFinishing()) return;
				activity.addContentView(croutonView, params);
			}
		}

		croutonView.requestLayout(); // This is needed so the animation can use
										// the measured with/height
		croutonView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ViewTreeObserverAccessor.removeOnGlobalLayoutListener(croutonView.getViewTreeObserver(), this);

				croutonView.startAnimation(crouton.getInAnimation());
				announceForAccessibilityCompat(crouton.getActivity(), crouton.getView(), crouton.getText(), getClass());
				if (CroutonConfiguration.DURATION_INFINITE != crouton.getConfiguration().durationInMilliseconds) {
					sendMessageDelayed(crouton, Messages.REMOVE_CROUTON,
							crouton.getConfiguration().durationInMilliseconds + crouton.getInAnimation().getDuration());
				}
			}
		});
	}

	private long calculateCroutonDuration(final Crouton crouton) {
		long croutonDuration = crouton.getConfiguration().durationInMilliseconds;
		croutonDuration += crouton.getInAnimation().getDuration();
		croutonDuration += crouton.getOutAnimation().getDuration();
		return croutonDuration;
	}

	/** Displays the next {@link Crouton} within the queue. */
	private void displayCrouton() {
		if (croutonQueue.isEmpty()) return;

		// First peek whether the Crouton has an activity.
		final Crouton currentCrouton = croutonQueue.peek();

		// If the activity is null we poll the Crouton off the queue.
		if (null == currentCrouton.getActivity()) {
			croutonQueue.poll();
		}

		if (!currentCrouton.isShowing()) {
			// Display the Crouton
			sendMessage(currentCrouton, Messages.ADD_CROUTON_TO_VIEW);
			if (null != currentCrouton.getLifecycleCallback()) {
				currentCrouton.getLifecycleCallback().onDisplayed();
			}
		} else {
			sendMessageDelayed(currentCrouton, Messages.DISPLAY_CROUTON, calculateCroutonDuration(currentCrouton));
		}
	}

	private void removeAllMessages() {
		removeMessages(Messages.ADD_CROUTON_TO_VIEW);
		removeMessages(Messages.DISPLAY_CROUTON);
		removeMessages(Messages.REMOVE_CROUTON);
	}

	private void removeAllMessagesForCrouton(final Crouton crouton) {
		removeMessages(Messages.ADD_CROUTON_TO_VIEW, crouton);
		removeMessages(Messages.DISPLAY_CROUTON, crouton);
		removeMessages(Messages.REMOVE_CROUTON, crouton);
	}

	/**
	 * Sends a {@link Crouton} within a {@link Message}.
	 * 
	 * @param crouton The {@link Crouton} that should be sent.
	 * @param messageId The {@link Message} id.
	 */
	private void sendMessage(final Crouton crouton, final int messageId) {
		final Message message = obtainMessage(messageId);
		message.obj = crouton;
		sendMessage(message);
	}

	/**
	 * Sends a {@link Crouton} within a delayed {@link Message}.
	 * 
	 * @param crouton The {@link Crouton} that should be sent.
	 * @param messageId The {@link Message} id.
	 * @param delay The delay in milliseconds.
	 */
	private void sendMessageDelayed(final Crouton crouton, final int messageId, final long delay) {
		final Message message = obtainMessage(messageId);
		message.obj = crouton;
		sendMessageDelayed(message, delay);
	}

	/**
	 * Inserts a {@link Crouton} to be displayed.
	 * 
	 * @param crouton The {@link Crouton} to be displayed.
	 */
	void add(final Crouton crouton) {
		croutonQueue.add(crouton);
		displayCrouton();
	}

	/** Removes all {@link Crouton}s from the queue. */
	void clearCroutonQueue() {
		removeAllMessages();
		if (croutonQueue == null) return;
		// remove any views that may already have been added to the activity's
		// content view
		for (final Crouton crouton : croutonQueue) {
			if (crouton.isShowing()) {
				((ViewGroup) crouton.getView().getParent()).removeView(crouton.getView());
			}
		}
		croutonQueue.clear();
	}

	/**
	 * Removes all {@link Crouton}s for the provided activity. This will remove
	 * crouton from {@link Activity}s content view immediately.
	 */
	void clearCroutonsForActivity(final Activity activity) {
		if (croutonQueue == null) return;
		final Iterator<Crouton> croutonIterator = croutonQueue.iterator();
		while (croutonIterator.hasNext()) {
			final Crouton crouton = croutonIterator.next();
			if (null != crouton.getActivity() && crouton.getActivity().equals(activity)) {
				// remove the crouton from the content view
				if (crouton.isShowing()) {
					((ViewGroup) crouton.getView().getParent()).removeView(crouton.getView());
				}
				removeAllMessagesForCrouton(crouton);
				// remove the crouton from the queue
				croutonIterator.remove();
			}
		}
	}

	/**
	 * Removes a {@link Crouton} immediately, even when it's currently being
	 * displayed.
	 * 
	 * @param crouton The {@link Crouton} that should be removed.
	 */
	void removeCroutonImmediately(final Crouton crouton) {
		// if Crouton has already been displayed then it may not be in the queue
		// (because it was popped).
		// This ensures the displayed Crouton is removed from its parent
		// immediately, whether another instance
		// of it exists in the queue or not.
		// Note: crouton.isShowing() is false here even if it really is showing,
		// as croutonView object in
		// Crouton seems to be out of sync with reality!
		if (null != crouton.getActivity() && null != crouton.getView() && null != crouton.getView().getParent()) {
			((ViewGroup) crouton.getView().getParent()).removeView(crouton.getView());

			// remove any messages pending for the crouton
			removeAllMessagesForCrouton(crouton);
		}
		// remove any matching croutons from queue
		if (croutonQueue == null) return;
		final Iterator<Crouton> croutonIterator = croutonQueue.iterator();
		while (croutonIterator.hasNext()) {
			final Crouton c = croutonIterator.next();
			if (c.equals(crouton) && null != c.getActivity()) {
				// remove the crouton from the content view
				if (crouton.isShowing()) {
					((ViewGroup) c.getView().getParent()).removeView(c.getView());
				}

				// remove any messages pending for the crouton
				removeAllMessagesForCrouton(c);

				// remove the crouton from the queue
				croutonIterator.remove();

				// we have found our crouton so just break
				break;
			}
		}
	}

	/** @return The currently used instance of the {@link Manager}. */
	static synchronized CroutonManager getInstance() {
		if (null == INSTANCE) {
			INSTANCE = new CroutonManager();
		}

		return INSTANCE;
	}

	private static final class Messages {
		public static final int DISPLAY_CROUTON = 0xc2007;

		public static final int ADD_CROUTON_TO_VIEW = 0xc20074dd;
		public static final int REMOVE_CROUTON = 0xc2007de1;

		private Messages() { /* no-op */
		}
	}

	private static class ViewTreeObserverAccessor {

		@SuppressWarnings("deprecation")
		private static void removeOnGlobalLayoutListener(final ViewTreeObserver observer,
				final OnGlobalLayoutListener listener) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				observer.removeGlobalOnLayoutListener(listener);
			} else {
				ViewTreeObserverAccessorJB.removeOnGlobalLayoutListener(observer, listener);
			}
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		private static class ViewTreeObserverAccessorJB {

			private static void removeOnGlobalLayoutListener(final ViewTreeObserver observer,
					final OnGlobalLayoutListener listener) {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) return;
				observer.removeOnGlobalLayoutListener(listener);
			}

		}
	}
}
