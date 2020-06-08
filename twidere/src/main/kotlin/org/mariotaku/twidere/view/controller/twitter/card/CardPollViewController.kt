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

package org.mariotaku.twidere.view.controller.twitter.card

import android.accounts.AccountManager
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.TextView
import kotlinx.android.synthetic.main.layout_twitter_card_poll.view.*
import nl.komponents.kovenant.task
import nl.komponents.kovenant.ui.successUi
import org.mariotaku.abstask.library.AbstractTask
import org.mariotaku.abstask.library.TaskStarter
import org.mariotaku.ktextension.spannable
import org.mariotaku.ktextension.toLongOr
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.twitter.TwitterCaps
import org.mariotaku.microblog.library.twitter.model.CardDataMap
import org.mariotaku.twidere.Constants.LOGTAG
import org.mariotaku.twidere.R
import org.mariotaku.twidere.extension.model.*
import org.mariotaku.twidere.model.ParcelableCardEntity
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.util.AccountUtils
import org.mariotaku.twidere.util.MicroBlogAPIFactory
import org.mariotaku.twidere.util.TwitterCardUtils
import org.mariotaku.twidere.util.support.ViewSupport
import org.mariotaku.twidere.view.ContainerView
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.roundToInt

/**
 * Created by mariotaku on 15/12/20.
 */
class CardPollViewController : ContainerView.ViewController() {

    private lateinit var status: ParcelableStatus
    private var fetchedCard: ParcelableCardEntity? = null
    private val card: ParcelableCardEntity
        get() = fetchedCard ?: status.card!!

    override fun onCreate() {
        super.onCreate()
        initChoiceView()
        loadCardPoll()
    }

    override fun onCreateView(parent: ContainerView): View {
        return LayoutInflater.from(context).inflate(R.layout.layout_twitter_card_poll, parent, false)
    }

    private fun initChoiceView() {
        val choicesCount = TwitterCardUtils.getChoicesCount(card)
        val inflater = LayoutInflater.from(context)

        for (i in 0 until choicesCount) {
            inflater.inflate(R.layout.layout_poll_item, view.pollContainer, true)
        }

        displayPoll(card, status)
    }

    private fun displayAndReloadPoll(result: ParcelableCardEntity, status: ParcelableStatus) {
        if (!attached) return
        displayPoll(result, status)
        loadCardPoll()
    }

    private fun loadCardPoll() {
        val weakThis = WeakReference(this)
        task {
            val vc = weakThis.get() ?: throw IllegalStateException()
            val card = vc.card
            val details = AccountUtils.getAccountDetails(AccountManager.get(vc.context), card.account_key, true)!!
            val caps = details.newMicroBlogInstance(vc.context, cls = TwitterCaps::class.java)
            val params = CardDataMap()
            params.putString("card_uri", card.url)
            params.putString("cards_platform", MicroBlogAPIFactory.CARDS_PLATFORM_ANDROID_12)
            params.putString("response_card_name", card.name)
            val cardResponse = caps.getPassThrough(params).card
            if (cardResponse == null || cardResponse.name == null) {
                throw IllegalStateException()
            }
            return@task cardResponse.toParcelable(details.key, details.type)
        }.successUi { data ->
            weakThis.get()?.displayPoll(data, status)
        }
    }

    private fun displayPoll(card: ParcelableCardEntity?, status: ParcelableStatus?) {
        if (card == null || status == null) return
        fetchedCard = card
        val choicesCount = TwitterCardUtils.getChoicesCount(card)
        var votesSum = 0
        val countsAreFinal = card.getAsBoolean("counts_are_final", false)
        val selectedChoice = card.getAsInteger("selected_choice", -1)
        val endDatetimeUtc = card.getAsDate("end_datetime_utc", Date())
        val hasChoice = selectedChoice != -1
        val isMyPoll = status.account_key == status.user_key
        val showResult = countsAreFinal || isMyPoll || hasChoice
        for (i in 0 until choicesCount) {
            val choiceIndex = i + 1
            votesSum += card.getAsInteger("choice${choiceIndex}_count", 0)
        }

        val clickListener = object : View.OnClickListener {
            private var clickedChoice: Boolean = false

            override fun onClick(v: View) {
                if (hasChoice || clickedChoice) return
                for (i in 0 until view.pollContainer.childCount) {
                    val pollItem = view.pollContainer.getChildAt(i)
                    pollItem.isClickable = false
                    clickedChoice = true
                    val choiceRadioButton: RadioButton = pollItem.findViewById(R.id.choice_button)
                    val checked = v === pollItem
                    choiceRadioButton.isChecked = checked
                    if (checked) {
                        val cardData = CardDataMap()
                        cardData.putLong("original_tweet_id", status.id.toLongOr(-1L))
                        cardData.putString("card_uri", card.url)
                        cardData.putString("cards_platform", MicroBlogAPIFactory.CARDS_PLATFORM_ANDROID_12)
                        cardData.putString("response_card_name", card.name)
                        cardData.putString("selected_choice", (i + 1).toString())
                        val task = object : AbstractTask<CardDataMap, ParcelableCardEntity, CardPollViewController>() {

                            override fun afterExecute(callback: CardPollViewController?, results: ParcelableCardEntity?) {
                                results ?: return
                                callback?.displayAndReloadPoll(results, status)
                            }

                            override fun doLongOperation(cardDataMap: CardDataMap): ParcelableCardEntity? {
                                val details = AccountUtils.getAccountDetails(AccountManager.get(context),
                                        card.account_key, true) ?: return null
                                val caps = details.newMicroBlogInstance(context, cls = TwitterCaps::class.java)
                                try {
                                    val cardEntity = caps.sendPassThrough(cardDataMap).card ?: run {
                                        return null
                                    }
                                    return cardEntity.toParcelable(card.account_key, details.type)
                                } catch (e: MicroBlogException) {
                                    Log.w(LOGTAG, e)
                                }

                                return null
                            }
                        }
                        task.callback = this@CardPollViewController
                        task.params = cardData
                        TaskStarter.execute(task)
                    }
                }
            }
        }

        val color = ContextCompat.getColor(context, R.color.material_light_blue_a200)
        val radius = context.resources.getDimension(R.dimen.element_spacing_small)
        for (i in 0 until choicesCount) {
            val pollItem = view.pollContainer.getChildAt(i)

            val choicePercentView: TextView = pollItem.findViewById(R.id.choice_percent)
            val choiceLabelView: TextView = pollItem.findViewById(R.id.choice_label)
            val choiceRadioButton: RadioButton = pollItem.findViewById(R.id.choice_button)

            val choiceIndex = i + 1
            val label = card.getAsString("choice${choiceIndex}_label", null)
            val value = card.getAsInteger("choice${choiceIndex}_count", 0)
            if (label == null) throw NullPointerException()
            val choicePercent = if (votesSum == 0) 0f else value / votesSum.toFloat()
            choiceLabelView.spannable = label
            choicePercentView.text = String.format(Locale.US, "%d%%",
                (choicePercent * 100).roundToInt()
            )

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

        val nVotes = context.resources.getQuantityString(R.plurals.N_votes, votesSum, votesSum)

        val timeLeft = DateUtils.getRelativeTimeSpanString(context, endDatetimeUtc.time, true)
        view.pollSummary.spannable = context.getString(R.string.poll_summary_format, nVotes, timeLeft)
    }

    private class PercentDrawable internal constructor(
            private val percent: Float,
            private val radius: Float,
            color: Int
    ) : Drawable() {

        private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
        }
        private val boundsF: RectF = RectF()

        override fun draw(canvas: Canvas) {
            canvas.drawRoundRect(boundsF, radius, radius, paint)
        }

        override fun onBoundsChange(bounds: Rect) {
            boundsF.set(bounds)
            boundsF.right = boundsF.left + boundsF.width() * percent
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

    companion object {

        fun show(status: ParcelableStatus): CardPollViewController {
            val vc = CardPollViewController()
            vc.status = status
            return vc
        }

    }
}
