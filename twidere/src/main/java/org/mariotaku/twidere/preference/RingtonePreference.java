/*
 * Copyright (C) 2008 The Android Open Source Project
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

package org.mariotaku.twidere.preference;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore.Audio;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.AttributeSet;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.preference.iface.IDialogPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class RingtonePreference extends DialogPreference implements IDialogPreference {

    private final int mRingtoneType;
    private final boolean mShowDefault;
    private final boolean mShowSilent;

    public RingtonePreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RingtonePreference);
        mRingtoneType = a.getInt(R.styleable.RingtonePreference_android_ringtoneType,
                RingtoneManager.TYPE_RINGTONE);
        mShowDefault = a.getBoolean(R.styleable.RingtonePreference_android_showDefault, true);
        mShowSilent = a.getBoolean(R.styleable.RingtonePreference_android_showSilent, true);
        a.recycle();
    }

    public int getRingtoneType() {
        return mRingtoneType;
    }

    public boolean isShowDefault() {
        return mShowDefault;
    }

    public boolean isShowSilent() {
        return mShowSilent;
    }

    @Override
    public void displayDialog(PreferenceFragmentCompat fragment) {
        RingtonePreferenceDialogFragment df = RingtonePreferenceDialogFragment.newInstance(getKey());
        df.setTargetFragment(fragment, 0);
        df.show(fragment.getFragmentManager(), getKey());
    }

    public static class RingtonePreferenceDialogFragment extends PreferenceDialogFragmentCompat
            implements LoaderManager.LoaderCallbacks<Cursor> {
        private MediaPlayer mMediaPlayer;
        private SimpleCursorAdapter mAdapter;
        private Uri mCurrentUri;

        public static RingtonePreferenceDialogFragment newInstance(String key) {
            final RingtonePreferenceDialogFragment df = new RingtonePreferenceDialogFragment();
            final Bundle args = new Bundle();
            args.putString(ARG_KEY, key);
            df.setArguments(args);
            return df;
        }

        @Override
        public void onDialogClosed(boolean positive) {
            if (positive && mCurrentUri != null) {
                final RingtonePreference preference = (RingtonePreference) getPreference();
                if (preference.isPersistent()) {
                    preference.persistString(mCurrentUri.toString());
                }
            }
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getContext();
            final TypedArray a = context.obtainStyledAttributes(null, android.support.v7.appcompat.R.styleable.AlertDialog,
                    android.support.v7.appcompat.R.attr.alertDialogStyle, 0);
            @SuppressLint("PrivateResource")
            final int layout = a.getResourceId(android.support.v7.appcompat.R.styleable.AlertDialog_singleChoiceItemLayout, 0);
            a.recycle();
            mAdapter = new SimpleCursorAdapter(context, layout, null, new String[]{Audio.Media.TITLE}, new int[]{android.R.id.text1}, 0);

            final MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
            builder.title(getPreference().getDialogTitle());
            builder.positiveText(android.R.string.ok);
            builder.negativeText(android.R.string.cancel);
            builder.onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    RingtonePreferenceDialogFragment.this.onClick(materialDialog, DialogInterface.BUTTON_POSITIVE);
                }
            });
            builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    RingtonePreferenceDialogFragment.this.onClick(materialDialog, DialogInterface.BUTTON_NEGATIVE);
                }
            });
            builder.cancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    RingtonePreferenceDialogFragment.this.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                }
            });
            builder.adapter(mAdapter, new MaterialDialog.ListCallback() {
                @Override
                public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.stop();
                        }
                        mMediaPlayer.release();
                    }
                    final Cursor cursor = mAdapter.getCursor();
                    if (!cursor.moveToPosition(which)) return;
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setLooping(false);
                    final String ringtone = cursor.getString(cursor.getColumnIndex(Audio.Media.DATA));
                    final Uri defUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    final Uri uri = isEmpty(ringtone) ? defUri : Uri.parse(ringtone);
                    mCurrentUri = uri;
                    try {
                        mMediaPlayer.setDataSource(getContext(), uri);
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                    } catch (final Exception e) {
                        // Ignore
                    }
                }
            });
            getLoaderManager().initLoader(0, null, this);
            return builder.build();
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            final String[] cols = new String[]{Audio.Media._ID, Audio.Media.DATA, Audio.Media.TITLE};
            RingtonePreference preference = (RingtonePreference) getPreference();
            int ringtoneType = preference.getRingtoneType();
            List<Expression> expressions = new ArrayList<>();
            if ((ringtoneType & RingtoneManager.TYPE_NOTIFICATION) != 0) {
                expressions.add(Expression.equalsArgs(Audio.Media.IS_NOTIFICATION));
            }
            if ((ringtoneType & RingtoneManager.TYPE_RINGTONE) != 0) {
                expressions.add(Expression.equalsArgs(Audio.Media.IS_RINGTONE));
            }
            if ((ringtoneType & RingtoneManager.TYPE_ALARM) != 0) {
                expressions.add(Expression.equalsArgs(Audio.Media.IS_ALARM));
            }
            final String selection;
            final String[] selectionArgs;
            if (expressions.isEmpty()) {
                selection = null;
                selectionArgs = null;
            } else {
                final int size = expressions.size();
                selection = Expression.or(expressions.toArray(new Expression[size])).getSQL();
                selectionArgs = new String[size];
                Arrays.fill(selectionArgs, "1");
            }
            return new CursorLoader(getContext(), Audio.Media.INTERNAL_CONTENT_URI, cols, selection,
                    selectionArgs, Audio.Media.DEFAULT_SORT_ORDER);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.changeCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.changeCursor(null);
        }
    }
}