package org.mariotaku.twidere.activity

import android.os.Bundle
import android.support.v4.app.Fragment

/**
 * Created by mariotaku on 2017/1/3.
 */

class FragmentContentActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = intent.getStringExtra(EXTRA_TITLE)
        val fragment = Fragment.instantiate(this, intent.getStringExtra(EXTRA_FRAGMENT),
                intent.getBundleExtra(EXTRA_FRAGMENT_ARGUMENTS))
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(android.R.id.content, fragment)
        ft.commit()
    }

    companion object {
        const val EXTRA_FRAGMENT = "FCA:fragment"
        const val EXTRA_TITLE = "FCA:title"
        const val EXTRA_FRAGMENT_ARGUMENTS = "FCA:fragment_arguments"
    }
}
