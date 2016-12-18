package org.mariotaku.chameleon.internal;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.view.LayoutInflaterFactory;
import android.support.v7.app.AppCompatDelegate;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import org.mariotaku.chameleon.Chameleon;
import org.mariotaku.chameleon.ChameleonView;
import org.mariotaku.chameleon.view.ChameleonAutoCompleteTextView;
import org.mariotaku.chameleon.view.ChameleonEditText;
import org.mariotaku.chameleon.view.ChameleonFloatingActionButton;
import org.mariotaku.chameleon.view.ChameleonMultiAutoCompleteTextView;
import org.mariotaku.chameleon.view.ChameleonSwipeRefreshLayout;
import org.mariotaku.chameleon.view.ChameleonTextView;
import org.mariotaku.chameleon.view.ChameleonToolbar;

/**
 * Created by mariotaku on 2016/12/18.
 */

public class ChameleonInflationFactory implements LayoutInflaterFactory {

    @NonNull
    private final LayoutInflater mInflater;
    @Nullable
    private final Activity mActivity;
    @Nullable
    private final AppCompatDelegate mDelegate;
    @Nullable
    private final Chameleon.Theme mTheme;
    private final ArrayMap<ChameleonView, ChameleonView.Appearance> mPostApplyViews;


    public ChameleonInflationFactory(@NonNull LayoutInflater inflater,
                                     @Nullable Activity activity,
                                     @Nullable AppCompatDelegate delegate,
                                     @Nullable Chameleon.Theme theme,
                                     @NonNull ArrayMap<ChameleonView, ChameleonView.Appearance> postApplyViews) {
        this.mInflater = inflater;
        this.mActivity = activity;
        this.mDelegate = delegate;
        this.mTheme = theme;
        this.mPostApplyViews = postApplyViews;
    }

    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        View view = null;
        if (shouldSkipTheming(parent)) {

        } else switch (name) {
            case "TextView":
            case "android.support.v7.widget.AppCompatTextView": {
                view = new ChameleonTextView(context, attrs);
                break;
            }
            case "EditText":
            case "android.support.v7.widget.AppCompatEditText": {
                view = new ChameleonEditText(context, attrs);
                break;
            }
            case "AutoCompleteTextView":
            case "android.support.v7.widget.AppCompatAutoCompleteTextView": {
                view = new ChameleonAutoCompleteTextView(context, attrs);
                break;
            }
            case "MultiAutoCompleteTextView":
            case "android.support.v7.widget.AppCompatMultiAutoCompleteTextView": {
                view = new ChameleonMultiAutoCompleteTextView(context, attrs);
                break;
            }
            case "android.support.design.widget.FloatingActionButton": {
                view = new ChameleonFloatingActionButton(context, attrs);
                break;
            }
            case "android.support.v7.widget.Toolbar": {
                view = new ChameleonToolbar(context, attrs);
                break;
            }
            case "android.support.v4.widget.SwipeRefreshLayout": {
                view = new ChameleonSwipeRefreshLayout(context, attrs);
                break;
            }
        }

        if (view == null) {
            // First, check if the AppCompatDelegate will give us a view, usually (maybe always) null.
            if (mDelegate != null) {
                view = mDelegate.createView(parent, name, context, attrs);
                if (view == null && mActivity != null)
                    view = mActivity.onCreateView(parent, name, context, attrs);
                else view = null;
            } else {
                view = null;
            }

            if (isExcluded(name))
                return view;

            // Mimic code of LayoutInflater using reflection tricks (this would normally be run when this factory returns null).
            // We need to intercept the default behavior rather than allowing the LayoutInflater to handle it after this method returns.
            if (view == null) {
                try {
                    Context viewContext;
                    final boolean inheritContext = false; // TODO will this ever need to be true?
                    //noinspection PointlessBooleanExpression,ConstantConditions
                    if (parent != null && inheritContext) {
                        viewContext = parent.getContext();
                    } else {
                        viewContext = mInflater.getContext();
                    }
                    Context wrappedContext = LayoutInflaterInternal.getThemeWrapper(viewContext, attrs);
                    if (wrappedContext != null) {
                        viewContext = wrappedContext;
                    }

                    Object[] mConstructorArgs = LayoutInflaterInternal.getConstructorArgs(mInflater);

                    final Object lastContext = mConstructorArgs[0];
                    mConstructorArgs[0] = viewContext;
                    try {
                        if (-1 == name.indexOf('.')) {
                            view = LayoutInflaterInternal.onCreateView(mInflater, parent, name, attrs);
                        } else {
                            view = LayoutInflaterInternal.createView(mInflater, name, null, attrs);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        mConstructorArgs[0] = lastContext;
                    }
                } catch (Throwable t) {
                    throw new RuntimeException(String.format("An error occurred while inflating View %s: %s", name, t.getMessage()), t);
                }
            }
        }


        if (view instanceof ChameleonView) {
            final ChameleonView cv = (ChameleonView) view;
            ChameleonView.Appearance appearance = cv.createAppearance(view.getContext(), attrs, mTheme);
            if (appearance != null) {
                if (cv.isPostApplyTheme()) {
                    mPostApplyViews.put(cv, appearance);
                } else {
                    cv.applyAppearance(appearance);
                }
            }
        }
        return view;
    }

    private boolean isExcluded(@NonNull String name) {
        switch (name) {
            case "android.support.design.internal.NavigationMenuItemView":
            case "ViewStub":
            case "fragment":
            case "include":
                return true;
            default:
                return false;
        }
    }

    private boolean shouldSkipTheming(View parent) {
        if (parent == null) return false;
        return "ignore".equals(parent.getTag());
    }
}
