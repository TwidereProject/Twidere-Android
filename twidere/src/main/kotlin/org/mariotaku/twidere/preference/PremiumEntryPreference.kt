package org.mariotaku.twidere.preference

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceClickListener
import android.util.AttributeSet
import org.mariotaku.chameleon.ChameleonUtils
import org.mariotaku.twidere.R
import org.mariotaku.twidere.TwidereConstants.REQUEST_PURCHASE_EXTRA_FEATURES
import org.mariotaku.twidere.extension.findParent
import org.mariotaku.twidere.fragment.ExtraFeaturesIntroductionDialogFragment
import org.mariotaku.twidere.util.dagger.GeneralComponent
import org.mariotaku.twidere.util.premium.ExtraFeaturesService
import javax.inject.Inject

/**
 * Created by mariotaku on 2017/1/12.
 */

class PremiumEntryPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs) {

    @Inject
    internal lateinit var extraFeaturesService: ExtraFeaturesService

    init {
        GeneralComponent.get(context).inject(this)
        val a = context.obtainStyledAttributes(attrs, R.styleable.PremiumEntryPreference)
        val requiredFeature = a.getString(R.styleable.PremiumEntryPreference_requiredFeature)
        a.recycle()
        isEnabled = extraFeaturesService.isSupported()
        onPreferenceClickListener = OnPreferenceClickListener l@{
            if (requiredFeature != null && !extraFeaturesService.isEnabled(requiredFeature)) {
                val activity = ChameleonUtils.getActivity(context)
                if (activity is FragmentActivity) {
                    ExtraFeaturesIntroductionDialogFragment.show(fm = activity.supportFragmentManager,
                            feature = requiredFeature, source = "preference:${key}",
                            requestCode = REQUEST_PURCHASE_EXTRA_FEATURES)
                }
                return@l true
            }
            return@l false
        }
    }

    override fun onAttached() {
        super.onAttached()
        if (!extraFeaturesService.isSupported()) {
            preferenceManager.preferenceScreen?.let { screen ->
                findParent(screen)?.removePreference(this)
            }
        }
    }
}
