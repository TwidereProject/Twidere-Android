package org.mariotaku.twidere.activity

import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * Created by mariotaku on 2017/1/3.
 */

class FragmentContentActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra(EXTRA_TITLE)
        intent.getStringExtra(EXTRA_FRAGMENT)?.let {
            Fragment.instantiate(this, it,
                intent.getBundleExtra(EXTRA_FRAGMENT_ARGUMENTS))
        }?.let { fragment ->
            val ft = supportFragmentManager.beginTransaction()
            ft.replace(android.R.id.content, fragment)
            ft.commit()
        }
    }

    companion object {
        const val EXTRA_FRAGMENT = "FCA:fragment"
        const val EXTRA_TITLE = "FCA:title"
        const val EXTRA_FRAGMENT_ARGUMENTS = "FCA:fragment_arguments"
    }
}
