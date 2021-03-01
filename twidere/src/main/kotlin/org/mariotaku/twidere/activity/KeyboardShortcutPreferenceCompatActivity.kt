/*
 * Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2015 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.View.OnClickListener
import kotlinx.android.synthetic.main.activity_keyboard_shortcut_input.*
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.SharedPreferenceConstants.VALUE_THEME_BACKGROUND_DEFAULT
import org.mariotaku.twidere.util.KeyboardShortcutsHandler
import org.mariotaku.twidere.util.KeyboardShortcutsHandler.KeyboardShortcutSpec

/**
 * Created by mariotaku on 15/4/20.
 */
class KeyboardShortcutPreferenceCompatActivity : BaseActivity(), OnClickListener {

    private var keySpec: KeyboardShortcutSpec? = null
    private var metaState: Int = 0

    override val themeBackgroundOption: String
        get() = VALUE_THEME_BACKGROUND_DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keyboard_shortcut_input)
        title = KeyboardShortcutsHandler.getActionLabel(this, keyAction)

        buttonPositive.setOnClickListener(this)
        buttonNegative.setOnClickListener(this)
        buttonNeutral.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonPositive -> {
                if (keySpec == null) return
                keyboardShortcutsHandler.register(keySpec, keyAction)
                finish()
            }
            R.id.buttonNeutral -> {
                keyboardShortcutsHandler.unregister(keyAction)
                finish()
            }
            R.id.buttonNegative -> {
                finish()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (KeyEvent.isModifierKey(keyCode)) {
            metaState = metaState or KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (KeyEvent.isModifierKey(keyCode)) {
            metaState = metaState and KeyboardShortcutsHandler.getMetaStateForKeyCode(keyCode).inv()
        }
        val keyAction = keyAction ?: return false
        val spec = KeyboardShortcutsHandler.getKeyboardShortcutSpec(contextTag,
                keyCode, event, KeyEvent.normalizeMetaState(metaState or event.metaState))
        if (spec == null || !spec.isValid) {
            return super.onKeyUp(keyCode, event)
        }
        keySpec = spec
        keysLabel.text = spec.toKeyString()
        val oldAction = keyboardShortcutsHandler.findAction(spec)
        val copyOfSpec = spec.copy()
        copyOfSpec.contextTag = null
        val oldGeneralAction = keyboardShortcutsHandler.findAction(copyOfSpec)
        if (!TextUtils.isEmpty(oldAction) && keyAction != oldAction) {
            // Conflicts with keys in same context tag
            conflictLabel.visibility = View.VISIBLE
            val label = KeyboardShortcutsHandler.getActionLabel(this, oldAction)
            conflictLabel.text = getString(R.string.conflicts_with_name, label)

            buttonPositive.setText(R.string.overwrite)
        } else if (!TextUtils.isEmpty(oldGeneralAction) && keyAction != oldGeneralAction) {
            // Conflicts with keys in root context
            conflictLabel.visibility = View.VISIBLE
            val label = KeyboardShortcutsHandler.getActionLabel(this, oldGeneralAction)
            conflictLabel.text = getString(R.string.conflicts_with_name, label)
            buttonPositive.setText(R.string.overwrite)
        } else {
            conflictLabel.visibility = View.GONE
            buttonPositive.setText(android.R.string.ok)
        }
        return true
    }

    private val contextTag: String?
        get() = intent.getStringExtra(EXTRA_CONTEXT_TAG)

    private val keyAction: String?
        get() = intent.getStringExtra(EXTRA_KEY_ACTION)

    companion object {

        const val EXTRA_CONTEXT_TAG = "context_tag"
        const val EXTRA_KEY_ACTION = "key_action"
    }
}
