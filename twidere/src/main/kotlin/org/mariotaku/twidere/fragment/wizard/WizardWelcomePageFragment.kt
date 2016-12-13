package org.mariotaku.twidere.fragment.wizard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.mariotaku.twidere.R
import org.mariotaku.twidere.fragment.BaseSupportFragment

/**
 * Created by mariotaku on 2016/12/13.
 */

class WizardWelcomePageFragment : BaseSupportFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_wizard_page_welcome, container, false)
    }
}
