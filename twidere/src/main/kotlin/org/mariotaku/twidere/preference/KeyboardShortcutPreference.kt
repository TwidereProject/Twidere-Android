package org.mariotaku.twidere.preference

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.LOGTAG
import org.mariotaku.twidere.fragment.ThemedPreferenceDialogFragmentCompat
import org.mariotaku.twidere.preference.iface.IDialogPreference
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutSpec
import org.mariotaku.twidere.util.dagger.GeneralComponent
import javax.inject.Inject

/**
 * Created by mariotaku on 16/3/15.
 */
class KeyboardShortcutPreference(context: Context, attrs: AttributeSet? = null) :
        DialogPreference(context, attrs), IDialogPreference {

    private val preferencesChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

    val contextTag: String?
    val action: String

    @Inject
    lateinit var keyboardShortcutsHandler: KeyboardShortcutsHandler

    init {
        GeneralComponent.get(context).inject(this)
        val a = context.obtainStyledAttributes(attrs, R.styleable.KeyboardShortcutPreference)
        contextTag = a.getString(R.styleable.KeyboardShortcutPreference_android_tag)
        action = a.getString(R.styleable.KeyboardShortcutPreference_android_action)!!
        a.recycle()

        key = action

        dialogLayoutResource = R.layout.dialog_keyboard_shortcut_input
        isPersistent = false
        dialogTitle = KeyboardShortcutsHandler.getActionLabel(context, action)
        title = KeyboardShortcutsHandler.getActionLabel(context, action)
        preferencesChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> updateSummary() }
        updateSummary()
    }

    override fun onPrepareForRemoval() {
        keyboardShortcutsHandler.unregisterOnSharedPreferenceChangeListener(preferencesChangeListener)
        super.onPrepareForRemoval()
    }

    override fun onAttached() {
        super.onAttached()
        keyboardShortcutsHandler.registerOnSharedPreferenceChangeListener(preferencesChangeListener)
    }

    private fun updateSummary() {
        val spec = keyboardShortcutsHandler.findKey(action)
        summary = spec?.toKeyString()
    }

    override fun displayDialog(fragment: PreferenceFragmentCompat) {
        val df = KeyboardShortcutDialogFragment.newInstance(action)
        df.setTargetFragment(fragment, 0)
        fragment.parentFragmentManager.let { df.show(it, action) }
    }

    class KeyboardShortcutDialogFragment : ThemedPreferenceDialogFragmentCompat(), DialogInterface.OnKeyListener {

        private lateinit var keysLabel: TextView
        private lateinit var conflictLabel: TextView

        private var keySpec: KeyboardShortcutSpec? = null
        private var modifierStates: Int = 0


        override fun onDialogClosed(positiveResult: Boolean) {

        }

        override fun onPrepareDialogBuilder(builder: AlertDialog.Builder?) {
            builder!!.setPositiveButton(android.R.string.ok, this)
            builder.setNegativeButton(android.R.string.cancel, this)
            builder.setNeutralButton(R.string.action_clear, this)
            builder.setOnKeyListener(this)
        }

        override fun onKey(dialog: DialogInterface, keyCode: Int, event: KeyEvent): Boolean {
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (KeyEvent.isModifierKey(keyCode)) {
                    modifierStates = modifierStates or KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode)
                }
            } else if (event.action != KeyEvent.ACTION_UP) {
                return false
            }
            if (KeyEvent.isModifierKey(keyCode)) {
                modifierStates = modifierStates and KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode).inv()
            }
            val preference = preference as KeyboardShortcutPreference
            val action = preference.action
            val contextTag = preference.contextTag
            val handler = preference.keyboardShortcutsHandler

            val spec = KeyboardShortcutsHandler.getKeyboardShortcutSpec(contextTag,
                    keyCode, event, KeyEvent.normalizeMetaState(modifierStates or event.metaState))
            if (spec == null || !spec.isValid) {
                Log.d(LOGTAG, String.format("Invalid key %s", event), Exception())
                return false
            }
            keySpec = spec
            keysLabel.text = spec.toKeyString()
            val oldAction = handler.findAction(spec)
            val context = context
            if (action == oldAction || TextUtils.isEmpty(oldAction)) {
                conflictLabel.visibility = View.GONE
                (dialog as? AlertDialog)?.getButton(DialogInterface.BUTTON_POSITIVE)?.setText(android.R.string.ok)
            } else {
                conflictLabel.visibility = View.VISIBLE
                val label = KeyboardShortcutsHandler.getActionLabel(context, oldAction)
                conflictLabel.text = context?.getString(R.string.conflicts_with_name, label)
                (dialog as? AlertDialog)?.getButton(DialogInterface.BUTTON_POSITIVE)?.setText(R.string.overwrite)
            }
            return true
        }

        override fun onBindDialogView(view: View) {
            super.onBindDialogView(view)
            keysLabel = view.findViewById(R.id.keys_label)
            conflictLabel = view.findViewById(R.id.conflict_label)
            conflictLabel.visibility = View.GONE
        }


        override fun onClick(dialog: DialogInterface?, which: Int) {
            val preference = preference as KeyboardShortcutPreference
            val action = preference.action
            val handler = preference.keyboardShortcutsHandler
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    if (keySpec == null) return
                    handler.register(keySpec, action)
                }
                DialogInterface.BUTTON_NEUTRAL -> {
                    handler.unregister(action)
                }
            }
        }

        companion object {

            fun newInstance(key: String): KeyboardShortcutDialogFragment {
                val df = KeyboardShortcutDialogFragment()
                val args = Bundle()
                args.putString(ARG_KEY, key)
                df.arguments = args
                return df
            }
        }

    }
}
