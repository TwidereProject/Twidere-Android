package org.mariotaku.twidere.fragment

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AlertDialog
import org.mariotaku.twidere.R

/**
 * Created by mariotaku on 2016/12/25.
 */

class ExtraFeaturesIntroductionDialogFragment : BaseDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.title_extra_features)
        builder.setView(R.layout.dialog_extra_features_introduction)
        builder.setPositiveButton(R.string.action_purchase) { dialog, which ->

        }
        builder.setNegativeButton(R.string.action_later) { dialog, which ->

        }
        builder.setNeutralButton(R.string.action_restore_purchase) { dialog, which ->

        }
        return builder.create()
    }
}
