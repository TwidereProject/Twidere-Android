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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.mariotaku.twidere.util.accessor.ViewAccessor;

/*
 * Based on an article by Cyril Mottier (http://android.cyrilmottier.com/?p=773) <br>
 */

/**
 * Displays information in a non-invasive context related manner. Like
 * {@link android.widget.Toast}, but better.
 * <p/>
 * <b>Important: </b> Call {@link Crouton#clearCroutonsForActivity(Activity)}
 * within {@link android.app.Activity#onDestroy()} to avoid {@link Context}
 * leaks.
 */
public final class Crouton {
	private static final int IMAGE_ID = 0x100;
	private static final int TEXT_ID = 0x101;
	private final CharSequence text;
	private final CroutonStyle style;
	private CroutonConfiguration configuration = null;
	private final View customView;

	private OnClickListener onClickListener;

	private Activity activity;
	private ViewGroup viewGroup;
	private FrameLayout croutonView;
	private Animation inAnimation;
	private Animation outAnimation;
	private CroutonLifecycleCallback lifecycleCallback = null;

	/**
	 * Creates the {@link Crouton}.
	 * 
	 * @param activity The {@link Activity} that the {@link Crouton} should be
	 *            attached to.
	 * @param text The text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 */
	private Crouton(final Activity activity, final CharSequence text, final CroutonStyle style) {
		if (activity == null || text == null || style == null)
			throw new IllegalArgumentException("Null parameters are not accepted");

		this.activity = activity;
		viewGroup = null;
		this.text = text;
		this.style = style;
		customView = null;
	}

	/**
	 * Creates the {@link Crouton}.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param text The text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @param viewGroup The {@link ViewGroup} that this {@link Crouton} should
	 *            be added to.
	 */
	private Crouton(final Activity activity, final CharSequence text, final CroutonStyle style,
			final ViewGroup viewGroup) {
		if (activity == null || text == null || style == null)
			throw new IllegalArgumentException("Null parameters are not accepted");

		this.activity = activity;
		this.text = text;
		this.style = style;
		this.viewGroup = viewGroup;
		customView = null;
	}

	/**
	 * Creates the {@link Crouton}.
	 * 
	 * @param activity The {@link Activity} that the {@link Crouton} should be
	 *            attached to.
	 * @param customView The custom {@link View} to display
	 */
	private Crouton(final Activity activity, final View customView) {
		if (activity == null || customView == null)
			throw new IllegalArgumentException("Null parameters are not accepted");

		this.activity = activity;
		viewGroup = null;
		this.customView = customView;
		style = new CroutonStyle.Builder().build();
		text = null;
	}

	/**
	 * Creates the {@link Crouton}.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param customView The custom {@link View} to display
	 * @param viewGroup The {@link ViewGroup} that this {@link Crouton} should
	 *            be added to.
	 */
	private Crouton(final Activity activity, final View customView, final ViewGroup viewGroup) {
		this(activity, customView, viewGroup, CroutonConfiguration.DEFAULT);
	}

	/**
	 * Creates the {@link Crouton}.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param customView The custom {@link View} to display
	 * @param viewGroup The {@link ViewGroup} that this {@link Crouton} should
	 *            be added to.
	 * @param configuration The {@link CroutonConfiguration} for this
	 *            {@link Crouton}.
	 */
	private Crouton(final Activity activity, final View customView, final ViewGroup viewGroup,
			final CroutonConfiguration configuration) {
		if (activity == null || customView == null)
			throw new IllegalArgumentException("Null parameters are not accepted");

		this.activity = activity;
		this.customView = customView;
		this.viewGroup = viewGroup;
		style = new CroutonStyle.Builder().build();
		text = null;
		this.configuration = configuration;
	}

	/** Cancels a {@link Crouton} immediately. */
	public void cancel() {
		final CroutonManager manager = CroutonManager.getInstance();
		manager.removeCroutonImmediately(this);
	}

	public Animation getInAnimation() {
		if (null == inAnimation && null != activity) {
			if (getConfiguration().inAnimationResId > 0) {
				inAnimation = AnimationUtils.loadAnimation(getActivity(), getConfiguration().inAnimationResId);
			} else {
				measureCroutonView();
				inAnimation = DefaultAnimationsBuilder.buildDefaultSlideInDownAnimation(getView());
			}
		}

		return inAnimation;
	}

	public Animation getOutAnimation() {
		if (null == outAnimation && null != activity) {
			if (getConfiguration().outAnimationResId > 0) {
				outAnimation = AnimationUtils.loadAnimation(getActivity(), getConfiguration().outAnimationResId);
			} else {
				outAnimation = DefaultAnimationsBuilder.buildDefaultSlideOutUpAnimation(getView());
			}
		}

		return outAnimation;
	}

	/**
	 * Set the {@link CroutonConfiguration} on this {@link Crouton}, prior to
	 * showing it.
	 * 
	 * @param configuration a {@link CroutonConfiguration} built using the
	 *            {@link CroutonConfiguration.Builder}.
	 * @return this {@link Crouton}.
	 */
	public Crouton setConfiguration(final CroutonConfiguration configuration) {
		this.configuration = configuration;
		return this;
	}

	/**
	 * @param lifecycleCallback Callback object for notable events in the life
	 *            of a Crouton.
	 */
	public void setLifecycleCallback(final CroutonLifecycleCallback lifecycleCallback) {
		this.lifecycleCallback = lifecycleCallback;
	}

	/**
	 * Allows setting of an {@link OnClickListener} directly to a
	 * {@link Crouton} without having to use a custom view.
	 * 
	 * @param onClickListener The {@link OnClickListener} to set.
	 * @return this {@link Crouton}.
	 */
	public Crouton setOnClickListener(final OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
		return this;
	}

	/**
	 * Displays the {@link Crouton}. If there's another {@link Crouton} visible
	 * at the time, this {@link Crouton} will be displayed afterwards.
	 */
	public void show() {
		CroutonManager.getInstance().add(this);
	}

	@Override
	public String toString() {
		return "Crouton{" + "text=" + text + ", style=" + style + ", configuration=" + configuration + ", customView="
				+ customView + ", onClickListener=" + onClickListener + ", activity=" + activity + ", viewGroup="
				+ viewGroup + ", croutonView=" + croutonView + ", inAnimation=" + inAnimation + ", outAnimation="
				+ outAnimation + ", lifecycleCallback=" + lifecycleCallback + '}';
	}

	private RelativeLayout initializeContentView(final Resources resources) {
		final RelativeLayout contentView = new RelativeLayout(activity);
		contentView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));

		// set padding
		int padding = style.paddingInPixels;

		// if a padding dimension has been set, this will overwrite any padding
		// in pixels
		if (style.paddingDimensionResId > 0) {
			padding = resources.getDimensionPixelSize(style.paddingDimensionResId);
		}
		contentView.setPadding(padding, padding, padding, padding);

		// only setup image if one is requested
		ImageView image = null;
		if (null != style.imageDrawable || 0 != style.imageResId) {
			image = initializeImageView();
			contentView.addView(image, image.getLayoutParams());
		}

		final TextView text = initializeTextView(resources);

		final RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		if (null != image) {
			textParams.addRule(RelativeLayout.RIGHT_OF, image.getId());
		}
		contentView.addView(text, textParams);
		return contentView;
	}

	private void initializeCroutonView() {
		final Resources resources = activity.getResources();

		croutonView = initializeCroutonViewGroup(resources);

		// create content view
		final RelativeLayout contentView = initializeContentView(resources);
		croutonView.addView(contentView);
	}

	private FrameLayout initializeCroutonViewGroup(final Resources resources) {
		final FrameLayout croutonView = new FrameLayout(activity);

		if (null != onClickListener) {
			croutonView.setOnClickListener(onClickListener);
		}

		final int height;
		if (style.heightDimensionResId > 0) {
			height = resources.getDimensionPixelSize(style.heightDimensionResId);
		} else {
			height = style.heightInPixels;
		}

		final int width;
		if (style.widthDimensionResId > 0) {
			width = resources.getDimensionPixelSize(style.widthDimensionResId);
		} else {
			width = style.widthInPixels;
		}

		croutonView.setLayoutParams(new FrameLayout.LayoutParams(width != 0 ? width
				: FrameLayout.LayoutParams.MATCH_PARENT, height));

		// set background
		if (style.backgroundColorValue != -1) {
			croutonView.setBackgroundColor(style.backgroundColorValue);
		} else {
			croutonView.setBackgroundColor(resources.getColor(style.backgroundColorResourceId));
		}

		// set the background drawable if set. This will override the background
		// color.
		if (style.backgroundDrawableResourceId != 0) {
			final Bitmap background = BitmapFactory.decodeResource(resources, style.backgroundDrawableResourceId);
			final BitmapDrawable drawable = new BitmapDrawable(resources, background);
			if (style.isTileEnabled) {
				drawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
			}
			ViewAccessor.setBackground(croutonView, drawable);
		}
		return croutonView;
	}

	private ImageView initializeImageView() {
		ImageView image;
		image = new ImageView(activity);
		image.setId(IMAGE_ID);
		image.setAdjustViewBounds(true);
		image.setScaleType(style.imageScaleType);

		// set the image drawable if not null
		if (null != style.imageDrawable) {
			image.setImageDrawable(style.imageDrawable);
		}

		// set the image resource if not 0. This will overwrite the drawable
		// if both are set
		if (style.imageResId != 0) {
			image.setImageResource(style.imageResId);
		}

		final RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		imageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
		imageParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

		image.setLayoutParams(imageParams);

		return image;
	}

	private TextView initializeTextView(final Resources resources) {
		final TextView text = new TextView(activity);
		text.setId(TEXT_ID);
		text.setText(this.text);
		text.setContentDescription(this.text);
		text.setTypeface(Typeface.DEFAULT_BOLD);
		text.setGravity(style.gravity);

		// set the text color if set
		if (style.textColorResourceId != 0) {
			text.setTextColor(resources.getColor(style.textColorResourceId));
		}

		// Set the text size. If the user has set a text size and text
		// appearance, the text size in the text appearance
		// will override this.
		if (style.textSize != 0) {
			text.setTextSize(TypedValue.COMPLEX_UNIT_SP, style.textSize);
		}

		// Setup the shadow if requested
		if (style.textShadowColorResId != 0) {
			initializeTextViewShadow(resources, text);
		}

		// Set the text appearance
		if (style.textAppearanceResId != 0) {
			text.setTextAppearance(activity, style.textAppearanceResId);
		}
		return text;
	}

	private void initializeTextViewShadow(final Resources resources, final TextView text) {
		final int textShadowColor = resources.getColor(style.textShadowColorResId);
		final float textShadowRadius = style.textShadowRadius;
		final float textShadowDx = style.textShadowDx;
		final float textShadowDy = style.textShadowDy;
		text.setShadowLayer(textShadowRadius, textShadowDx, textShadowDy, textShadowColor);
	}

	private boolean isCroutonViewNotNull() {
		return null != croutonView && null != croutonView.getParent();
	}

	private boolean isCustomViewNotNull() {
		return null != customView && null != customView.getParent();
	}

	private void measureCroutonView() {
		final View view = getView();
		int widthSpec;
		if (viewGroup != null) {
			widthSpec = View.MeasureSpec.makeMeasureSpec(viewGroup.getMeasuredWidth(), View.MeasureSpec.AT_MOST);
		} else {
			widthSpec = View.MeasureSpec.makeMeasureSpec(activity.getWindow().getDecorView().getMeasuredWidth(),
					View.MeasureSpec.AT_MOST);
		}
		view.measure(widthSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
	}

	/** Removes the activity reference this {@link Crouton} is holding */
	void detachActivity() {
		activity = null;
	}

	/** Removes the lifecycleCallback reference this {@link Crouton} is holding */
	void detachLifecycleCallback() {
		lifecycleCallback = null;
	}

	/** Removes the viewGroup reference this {@link Crouton} is holding */
	void detachViewGroup() {
		viewGroup = null;
	}

	/** @return the activity */
	Activity getActivity() {
		return activity;
	}

	/** @return this croutons configuration */
	CroutonConfiguration getConfiguration() {
		if (null == configuration) {
			configuration = getStyle().configuration;
		}
		return configuration;
	}

	/** @return the lifecycleCallback */
	CroutonLifecycleCallback getLifecycleCallback() {
		return lifecycleCallback;
	}

	/** @return the style */
	CroutonStyle getStyle() {
		return style;
	}

	/** @return the text */
	CharSequence getText() {
		return text;
	}

	/** @return the view */
	View getView() {
		// return the custom view if one exists
		if (null != customView) return customView;

		// if already setup return the view
		if (null == croutonView) {
			initializeCroutonView();
		}

		return croutonView;
	}

	/** @return the viewGroup */
	ViewGroup getViewGroup() {
		return viewGroup;
	}

	/**
	 * @return <code>true</code> if the {@link Crouton} is being displayed, else
	 *         <code>false</code>.
	 */
	boolean isShowing() {
		return null != activity && (isCroutonViewNotNull() || isCustomViewNotNull());
	}

	/**
	 * Cancels all queued {@link Crouton}s. If there is a {@link Crouton}
	 * displayed currently, it will be the last one displayed.
	 */
	public static void cancelAllCroutons() {
		CroutonManager.getInstance().clearCroutonQueue();
	}

	/**
	 * Clears (and removes from {@link Activity}'s content view, if necessary)
	 * all croutons for the provided activity
	 * 
	 * @param activity - The {@link Activity} to clear the croutons for.
	 */
	public static void clearCroutonsForActivity(final Activity activity) {
		CroutonManager.getInstance().clearCroutonsForActivity(activity);
	}

	/**
	 * Convenience method to get the license text for embedding within your
	 * application.
	 * 
	 * @return The license text.
	 */
	public static String getLicenseText() {
		return "This application uses the Crouton library.\n\n" + "Copyright 2012 - 2013 Benjamin Weiss \n"
				+ "Copyright 2012 Neofonie Mobile GmbH\n" + "\n"
				+ "Licensed under the Apache License, Version 2.0 (the \"License\");\n"
				+ "you may not use this file except in compliance with the License.\n"
				+ "You may obtain a copy of the License at\n" + "\n"
				+ "   http://www.apache.org/licenses/LICENSE-2.0\n" + "\n"
				+ "Unless required by applicable law or agreed to in writing, software\n"
				+ "distributed under the License is distributed on an \"AS IS\" BASIS,\n"
				+ "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
				+ "See the License for the specific language governing permissions and\n"
				+ "limitations under the License.";
	}

	/**
	 * Allows hiding of a previously displayed {@link Crouton}.
	 * 
	 * @param crouton The {@link Crouton} you want to hide.
	 */
	public static void hide(final Crouton crouton) {
		CroutonManager.getInstance().removeCrouton(crouton);
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// You have reached the internal API of Crouton.
	// If you do not plan to develop for Crouton there is nothing of interest
	// below here.
	// ////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Creates a {@link Crouton} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity The {@link Activity} that the {@link Crouton} should be
	 *            attached to.
	 * @param customView The custom {@link View} to display
	 * @return The created {@link Crouton}.
	 */
	public static Crouton make(final Activity activity, final View customView) {
		return new Crouton(activity, customView);
	}

	/**
	 * Creates a {@link Crouton} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param customView The custom {@link View} to display
	 * @param viewGroupResId The resource id of the {@link ViewGroup} that this
	 *            {@link Crouton} should be added to.
	 * @return The created {@link Crouton}.
	 */
	public static Crouton make(final Activity activity, final View customView, final int viewGroupResId) {
		return new Crouton(activity, customView, (ViewGroup) activity.findViewById(viewGroupResId));
	}

	/**
	 * Creates a {@link Crouton} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param customView The custom {@link View} to display
	 * @param viewGroupResId The resource id of the {@link ViewGroup} that this
	 *            {@link Crouton} should be added to.
	 * @param configuration The configuration for this crouton.
	 * @return The created {@link Crouton}.
	 */
	public static Crouton make(final Activity activity, final View customView, final int viewGroupResId,
			final CroutonConfiguration configuration) {
		return new Crouton(activity, customView, (ViewGroup) activity.findViewById(viewGroupResId), configuration);
	}

	/**
	 * Creates a {@link Crouton} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param customView The custom {@link View} to display
	 * @param viewGroup The {@link ViewGroup} that this {@link Crouton} should
	 *            be added to.
	 * @return The created {@link Crouton}.
	 */
	public static Crouton make(final Activity activity, final View customView, final ViewGroup viewGroup) {
		return new Crouton(activity, customView, viewGroup);
	}

	/**
	 * Creates a {@link Crouton} with provided text and style for a given
	 * activity.
	 * 
	 * @param activity The {@link Activity} that the {@link Crouton} should be
	 *            attached to.
	 * @param text The text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @return The created {@link Crouton}.
	 */
	public static Crouton makeText(final Activity activity, final CharSequence text, final CroutonStyle style) {
		return new Crouton(activity, text, style);
	}

	/**
	 * Creates a {@link Crouton} with provided text and style for a given
	 * activity.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param text The text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @param viewGroupResId The resource id of the {@link ViewGroup} that this
	 *            {@link Crouton} should be added to.
	 * @return The created {@link Crouton}.
	 */
	public static Crouton makeText(final Activity activity, final CharSequence text, final CroutonStyle style,
			final int viewGroupResId) {
		return new Crouton(activity, text, style, (ViewGroup) activity.findViewById(viewGroupResId));
	}

	/**
	 * Creates a {@link Crouton} with provided text and style for a given
	 * activity.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param text The text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @param viewGroup The {@link ViewGroup} that this {@link Crouton} should
	 *            be added to.
	 * @return The created {@link Crouton}.
	 */
	public static Crouton makeText(final Activity activity, final CharSequence text, final CroutonStyle style,
			final ViewGroup viewGroup) {
		return new Crouton(activity, text, style, viewGroup);
	}

	/**
	 * Creates a {@link Crouton} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity The {@link Activity} that the {@link Crouton} should be
	 *            attached to.
	 * @param textResourceId The resource id of the text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @return The created {@link Crouton}.
	 */
	public static Crouton makeText(final Activity activity, final int textResourceId, final CroutonStyle style) {
		return makeText(activity, activity.getString(textResourceId), style);
	}

	/**
	 * Creates a {@link Crouton} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param textResourceId The resource id of the text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @param viewGroupResId The resource id of the {@link ViewGroup} that this
	 *            {@link Crouton} should be added to.
	 * @return The created {@link Crouton}.
	 */
	public static Crouton makeText(final Activity activity, final int textResourceId, final CroutonStyle style,
			final int viewGroupResId) {
		return makeText(activity, activity.getString(textResourceId), style,
				(ViewGroup) activity.findViewById(viewGroupResId));
	}

	/**
	 * Creates a {@link Crouton} with provided text-resource and style for a
	 * given activity.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param textResourceId The resource id of the text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @param viewGroup The {@link ViewGroup} that this {@link Crouton} should
	 *            be added to.
	 * @return The created {@link Crouton}.
	 */
	public static Crouton makeText(final Activity activity, final int textResourceId, final CroutonStyle style,
			final ViewGroup viewGroup) {
		return makeText(activity, activity.getString(textResourceId), style, viewGroup);
	}

	/**
	 * Creates a {@link Crouton} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity The {@link android.app.Activity} that the {@link Crouton}
	 *            should be attached to.
	 * @param customView The custom {@link View} to display
	 */
	public static void show(final Activity activity, final View customView) {
		make(activity, customView).show();
	}

	/**
	 * Creates a {@link Crouton} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param customView The custom {@link View} to display
	 * @param viewGroupResId The resource id of the {@link ViewGroup} that this
	 *            {@link Crouton} should be added to.
	 */
	public static void show(final Activity activity, final View customView, final int viewGroupResId) {
		make(activity, customView, viewGroupResId).show();
	}

	/**
	 * Creates a {@link Crouton} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param customView The custom {@link View} to display
	 * @param viewGroup The {@link ViewGroup} that this {@link Crouton} should
	 *            be added to.
	 */
	public static void show(final Activity activity, final View customView, final ViewGroup viewGroup) {
		make(activity, customView, viewGroup).show();
	}

	/**
	 * Creates a {@link Crouton} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity The {@link android.app.Activity} that the {@link Crouton}
	 *            should be attached to.
	 * @param text The text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 */
	public static void showText(final Activity activity, final CharSequence text, final CroutonStyle style) {
		makeText(activity, text, style).show();
	}

	/**
	 * Creates a {@link Crouton} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param text The text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @param viewGroupResId The resource id of the {@link ViewGroup} that this
	 *            {@link Crouton} should be added to.
	 */
	public static void showText(final Activity activity, final CharSequence text, final CroutonStyle style,
			final int viewGroupResId) {
		makeText(activity, text, style, (ViewGroup) activity.findViewById(viewGroupResId)).show();
	}

	/**
	 * Creates a {@link Crouton} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param text The text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @param viewGroupResId The resource id of the {@link ViewGroup} that this
	 *            {@link Crouton} should be added to.
	 * @param configuration The configuration for this Crouton.
	 */
	public static void showText(final Activity activity, final CharSequence text, final CroutonStyle style,
			final int viewGroupResId, final CroutonConfiguration configuration) {
		makeText(activity, text, style, (ViewGroup) activity.findViewById(viewGroupResId)).setConfiguration(
				configuration).show();
	}

	/**
	 * Creates a {@link Crouton} with provided text and style for a given
	 * activity and displays it directly.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param text The text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @param viewGroup The {@link ViewGroup} that this {@link Crouton} should
	 *            be added to.
	 */
	public static void showText(final Activity activity, final CharSequence text, final CroutonStyle style,
			final ViewGroup viewGroup) {
		makeText(activity, text, style, viewGroup).show();
	}

	/**
	 * Creates a {@link Crouton} with provided text-resource and style for a
	 * given activity and displays it directly.
	 * 
	 * @param activity The {@link Activity} that the {@link Crouton} should be
	 *            attached to.
	 * @param textResourceId The resource id of the text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 */
	public static void showText(final Activity activity, final int textResourceId, final CroutonStyle style) {
		showText(activity, activity.getString(textResourceId), style);
	}

	/**
	 * Creates a {@link Crouton} with provided text-resource and style for a
	 * given activity and displays it directly.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param textResourceId The resource id of the text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @param viewGroupResId The resource id of the {@link ViewGroup} that this
	 *            {@link Crouton} should be added to.
	 */
	public static void showText(final Activity activity, final int textResourceId, final CroutonStyle style,
			final int viewGroupResId) {
		showText(activity, activity.getString(textResourceId), style, viewGroupResId);
	}

	/**
	 * Creates a {@link Crouton} with provided text-resource and style for a
	 * given activity and displays it directly.
	 * 
	 * @param activity The {@link Activity} that represents the context in which
	 *            the Crouton should exist.
	 * @param textResourceId The resource id of the text you want to display.
	 * @param style The style that this {@link Crouton} should be created with.
	 * @param viewGroup The {@link ViewGroup} that this {@link Crouton} should
	 *            be added to.
	 */
	public static void showText(final Activity activity, final int textResourceId, final CroutonStyle style,
			final ViewGroup viewGroup) {
		showText(activity, activity.getString(textResourceId), style, viewGroup);
	}
}
