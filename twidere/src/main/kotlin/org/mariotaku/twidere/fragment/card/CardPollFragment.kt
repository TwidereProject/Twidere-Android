/*
 *                 Twidere - Twitter client for Android
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

package org.mariotaku.twidere.fragment.card

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.ContextCompat
import android.support.v4.content.Loader
import android.text.TextUtils
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_card_poll.*
import org.apache.commons.lang3.math.NumberUtils
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterCaps
import org.mariotaku.microblog.library.twitter.model.CardDataMap
import org.mariotaku.twidere.Constants.EXTRA_CARD
import org.mariotaku.twidere.Constants.LOGTAG
import org.mariotaku.twidere.R
import org.mariotaku.twidere.constant.IntentConstants
import org.mariotaku.twidere.constant.IntentConstants.EXTRA_STATUS
import org.mariotaku.twidere.fragment.BaseSupportFragment
import org.mariotaku.twidere.model.ParcelableCardEntity
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.util.ParcelableCardEntityUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.support.ViewSupport
import java.util.*
import java.util.regex.Pattern

/**
 * Created by mariotaku on 15/12/20.
 */
class CardPollFragment : BaseSupportFragment(), LoaderManager.LoaderCallbacks<ParcelableCardEntity?>, View.OnClickListener {
    private var fetchedCard: ParcelableCardEntity? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initChoiceView(savedInstanceState)

        loaderManager.initLoader(0, null, this)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_card_poll, container, false)
    }

    override fun fitSystemWindows(insets: Rect) {
        // No-op
    }


    private fun initChoiceView(savedInstanceState: Bundle?) {
        val card = card
        val status = status
        val choicesCount = getChoicesCount(card)
        val inflater = getLayoutInflater(savedInstanceState)

        for (i in 0 until choicesCount) {
            inflater.inflate(R.layout.layout_poll_item, pollContainer, true)
        }

        displayPoll(card, status)
    }

    private fun displayPoll(card: ParcelableCardEntity?, status: ParcelableStatus?) {
        val context = context
        if (card == null || status == null || context == null) return
        fetchedCard = card
        val choicesCount = getChoicesCount(card)
        var votesSum = 0
        val countsAreFinal = ParcelableCardEntityUtils.getAsBoolean(card, "counts_are_final", false)
        val selectedChoice = ParcelableCardEntityUtils.getAsInteger(card, "selected_choice", -1)
        val endDatetimeUtc = ParcelableCardEntityUtils.getAsDate(card, "end_datetime_utc", Date())
        val hasChoice = selectedChoice != -1
        val isMyPoll = status.account_key == status.user_key
        val showResult = countsAreFinal || isMyPoll || hasChoice
        for (i in 0..choicesCount - 1) {
            val choiceIndex = i + 1
            votesSum += ParcelableCardEntityUtils.getAsInteger(card, "choice" + choiceIndex + "_count", 0)
        }

        val clickListener = object : View.OnClickListener {
            private var clickedChoice: Boolean = false

            override fun onClick(v: View) {
                if (hasChoice || clickedChoice) return
                for (i in 0 until pollContainer.childCount) {
                    val pollItem = pollContainer.getChildAt(i)
                    pollItem.isClickable = false
                    clickedChoice = true
                    val choiceRadioButton = pollItem.findViewById(R.id.choice_button) as RadioButton
                    val checked = v === pollItem
                    choiceRadioButton.isChecked = checked
                    if (checked) {
                        val cardData = CardDataMap()
                        cardData.putLong("original_tweet_id", NumberUtils.toLong(status.id))
                        cardData.putString("card_uri", card.url)
                        cardData.putString("cards_platform", MicroBlogAPIFactory.CARDS_PLATFORM_ANDROID_12)
                        cardData.putString("response_card_name", card.name)
                        cardData.putString("selected_choice", (i + 1).toString())
                        val task = object : AbstractTask<CardDataMap, ParcelableCardEntity, CardPollFragment>() {

                            public override fun afterExecute(handler: CardPollFragment?, result: ParcelableCardEntity?) {
                                result ?: return
                                handler?.displayAndReloadPoll(result, status)
                            }

                            public override fun doLongOperation(cardDataMap: CardDataMap): ParcelableCardEntity? {
                                val caps = MicroBlogAPIFactory.getInstance(context, card.account_key,
                                        true, true, TwitterCaps::class.java) ?: return null
                                try {
                                    val cardEntity = caps.sendPassThrough(cardDataMap).card
                                    return ParcelableCardEntityUtils.fromCardEntity(cardEntity,
                                            card.account_key)
                                } catch (e: MicroBlogException) {
                                    Log.w(LOGTAG, e)
                                }

                                return null
                            }
                        }
                        task.callback = this@CardPollFragment
                        task.params = cardData
                        TaskStarter.execute(task)
                    }
                }
            }
        }

        val color = ContextCompat.getColor(context, R.color.material_light_blue_a200)
        val radius = resources.getDimension(R.dimen.element_spacing_small)
        for (i in 0..choicesCount - 1) {
            val pollItem = pollContainer.getChildAt(i)

            val choicePercentView = pollItem.findViewById(R.id.choice_percent) as TextView
            val choiceLabelView = pollItem.findViewById(R.id.choice_label) as TextView
            val choiceRadioButton = pollItem.findViewById(R.id.choice_button) as RadioButton

            val choiceIndex = i + 1
            val label = ParcelableCardEntityUtils.getAsString(card, "choice" + choiceIndex + "_label", null)
            val value = ParcelableCardEntityUtils.getAsInteger(card, "choice" + choiceIndex + "_count", 0)
            if (label == null) throw NullPointerException()
            val choicePercent = if (votesSum == 0) 0f else value / votesSum.toFloat()
            choiceLabelView.text = label
            choicePercentView.text = String.format(Locale.US, "%d%%", Math.round(choicePercent * 100))

            pollItem.setOnClickListener(clickListener)

            val isSelected = selectedChoice == choiceIndex

            if (showResult) {
                choicePercentView.visibility = View.VISIBLE
                choiceRadioButton.visibility = if (hasChoice && isSelected) View.VISIBLE else View.INVISIBLE
                ViewSupport.setBackground(choiceLabelView, PercentDrawable(choicePercent, radius, color))
            } else {
                choicePercentView.visibility = View.GONE
                choiceRadioButton.visibility = View.VISIBLE
                ViewSupport.setBackground(choiceLabelView, null)
            }

            choiceRadioButton.isChecked = isSelected
            pollItem.isClickable = selectedChoice == -1

        }

        val nVotes = resources.getQuantityString(R.plurals.N_votes, votesSum, votesSum)

        val timeLeft = DateUtils.getRelativeTimeSpanString(context, endDatetimeUtc.time, true)
        pollSummary.text = getString(R.string.poll_summary_format, nVotes, timeLeft)
    }

    private fun displayAndReloadPoll(result: ParcelableCardEntity, status: ParcelableStatus) {
        if (host == null) return
        displayPoll(result, status)
        loaderManager.restartLoader(0, null, this)
    }

    private fun getChoicesCount(card: ParcelableCardEntity): Int {
        val matcher = PATTERN_POLL_TEXT_ONLY.matcher(card.name)
        if (!matcher.matches()) throw IllegalStateException()
        return NumberUtils.toInt(matcher.group(1))
    }

    private val card: ParcelableCardEntity
        get() {
            val fetched = fetchedCard
            if (fetched != null) return fetched
            val card = arguments.getParcelable<ParcelableCardEntity>(EXTRA_CARD)!!
            assert(card.name != null)
            return card
        }

    private val status: ParcelableStatus
        get() = arguments.getParcelable<ParcelableStatus>(EXTRA_STATUS)

    override fun onClick(v: View) {

    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ParcelableCardEntity?> {
        val card = card
        return ParcelableCardEntityLoader(context, card.account_key, card.url, card.name)
    }

    override fun onLoadFinished(loader: Loader<ParcelableCardEntity?>, data: ParcelableCardEntity?) {
        if (data == null) return
        displayPoll(data, status)
    }

    override fun onLoaderReset(loader: Loader<ParcelableCardEntity?>) {

    }

    private class PercentDrawable internal constructor(
            private val percent: Float,
            private val radius: Float,
            color: Int
    ) : Drawable() {

        private val mPaint: Paint
        private val mBounds: RectF

        init {
            mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
            mPaint.color = color
            mBounds = RectF()
        }

        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(mBounds, radius, radius, mPaint)
        }

        override fun onBoundsChange(bounds: Rect) {
            mBounds.set(bounds)
            mBounds.right = mBounds.left + mBounds.width() * percent
            super.onBoundsChange(bounds)
        }

        override fun setAlpha(alpha: Int) {

        }

        override fun setColorFilter(colorFilter: ColorFilter?) {

        }

        override fun getOpacity(): Int {
            return PixelFormat.TRANSLUCENT
        }
    }

    class ParcelableCardEntityLoader(
            context: Context,
            private val accountKey: UserKey,
            private val cardUri: String,
            private val cardName: String
    ) : AsyncTaskLoader<ParcelableCardEntity?>(context) {

        override fun loadInBackground(): ParcelableCardEntity? {
            val caps = MicroBlogAPIFactory.getInstance(context, accountKey,
                    true, true, TwitterCaps::class.java) ?: return null
            try {
                val params = CardDataMap()
                params.putString("card_uri", cardUri)
                params.putString("cards_platform", MicroBlogAPIFactory.CARDS_PLATFORM_ANDROID_12)
                params.putString("response_card_name", cardName)
                val card = caps.getPassThrough(params).card
                if (card == null || card.name == null) {
                    return null
                }
                return ParcelableCardEntityUtils.fromCardEntity(card, accountKey)
            } catch (e: MicroBlogException) {
                return null
            }

        }

        override fun onStartLoading() {
            forceLoad()
        }
    }

    companion object {

        val PATTERN_POLL_TEXT_ONLY: Pattern = Pattern.compile("poll([\\d]+)choice_text_only")


        fun show(status: ParcelableStatus): CardPollFragment {
            val fragment = CardPollFragment()
            val args = Bundle()
            args.putParcelable(EXTRA_STATUS, status)
            args.putParcelable(IntentConstants.EXTRA_CARD, status.card)
            fragment.arguments = args
            return fragment
        }

        fun isPoll(card: ParcelableCardEntity): Boolean {
            return PATTERN_POLL_TEXT_ONLY.matcher(card.name).matches() && !TextUtils.isEmpty(card.url)
        }
    }
}
