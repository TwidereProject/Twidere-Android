/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.extension.text.twitter

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.twitter.twittertext.Extractor
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mariotaku.twidere.model.ParcelableStatus
import org.mariotaku.twidere.model.ParcelableUserMention
import org.mariotaku.twidere.test.R
import org.mariotaku.twidere.util.JsonSerializer

/**
 * Created by mariotaku on 2017/4/2.
 */
@RunWith(AndroidJUnit4::class)
class ExtractorExtensionsTest {

    private val extractor = Extractor()
    private lateinit var inReplyTo: ParcelableStatus

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().context

        // This is a tweet by @t_deyarmin, mentioning @nixcraft
        inReplyTo = context.resources.openRawResource(R.raw.parcelable_status_848051071444410368).use {
            JsonSerializer.parse(it, ParcelableStatus::class.java)
        }
    }

    @Test
    fun testExtractReplyTextAndMentionsReplyToAll() {
        // This reply to all users, which is the normal case
        extractor.extractReplyTextAndMentions("@t_deyarmin @nixcraft @mariotaku lol", inReplyTo).let {
            Assert.assertEquals("lol", it.replyText)
            Assert.assertTrue("extraMentions.isEmpty()", it.extraMentions.isEmpty())
            Assert.assertTrue("excludedMentions.isEmpty()", it.excludedMentions.isEmpty())
            Assert.assertTrue("replyToOriginalUser", it.replyToOriginalUser)
        }
    }


    @Test
    fun testExtractReplyTextAndMentionsReplyToAllExtraMentions() {
        // This reply to all users, which is the normal case
        extractor.extractReplyTextAndMentions("@t_deyarmin @nixcraft @mariotaku @TwidereProject lol", inReplyTo).let {
            Assert.assertEquals("@TwidereProject lol", it.replyText)
            Assert.assertTrue("extraMentions.containsAll(TwidereProject)",
                    it.extraMentions.entitiesContainsAll("TwidereProject"))
            Assert.assertTrue("excludedMentions.isEmpty()", it.excludedMentions.isEmpty())
            Assert.assertTrue("replyToOriginalUser", it.replyToOriginalUser)
        }
    }

    @Test
    fun testExtractReplyTextAndMentionsReplyToAllSuffixMentions() {
        // This reply to all users, which is the normal case
        extractor.extractReplyTextAndMentions("@t_deyarmin @nixcraft @mariotaku lol @TwidereProject", inReplyTo).let {
            Assert.assertEquals("lol @TwidereProject", it.replyText)
            Assert.assertTrue("extraMentions.containsAll(TwidereProject)",
                    it.extraMentions.entitiesContainsAll("TwidereProject"))
            Assert.assertTrue("excludedMentions.isEmpty()", it.excludedMentions.isEmpty())
            Assert.assertTrue("replyToOriginalUser", it.replyToOriginalUser)
        }
    }

    @Test
    fun testExtractReplyTextAndMentionsAuthorOnly() {
        // This reply removed @nixcraft and replying to author only
        extractor.extractReplyTextAndMentions("@t_deyarmin lol", inReplyTo).let {
            Assert.assertEquals("lol", it.replyText)
            Assert.assertTrue("extraMentions.isEmpty()", it.extraMentions.isEmpty())
            Assert.assertTrue("excludedMentions.containsAll(nixcraft, mariotaku)",
                    it.excludedMentions.mentionsContainsAll("nixcraft", "mariotaku"))
            Assert.assertTrue("replyToOriginalUser", it.replyToOriginalUser)
        }
    }

    @Test
    fun testExtractReplyTextAndMentionsAuthorOnlyExtraMentions() {
        // This reply removed @nixcraft and replying to author only
        extractor.extractReplyTextAndMentions("@t_deyarmin @TwidereProject lol", inReplyTo).let {
            Assert.assertEquals("@TwidereProject lol", it.replyText)
            Assert.assertTrue("extraMentions.containsAll(TwidereProject)",
                    it.extraMentions.entitiesContainsAll("TwidereProject"))
            Assert.assertTrue("excludedMentions.containsAll(nixcraft, mariotaku)",
                    it.excludedMentions.mentionsContainsAll("nixcraft", "mariotaku"))
            Assert.assertTrue("replyToOriginalUser", it.replyToOriginalUser)
        }
    }

    @Test
    fun testExtractReplyTextAndMentionsAuthorOnlySuffixMention() {
        // This reply removed @nixcraft and replying to author only
        extractor.extractReplyTextAndMentions("@t_deyarmin lol @TwidereProject", inReplyTo).let {
            Assert.assertEquals("lol @TwidereProject", it.replyText)
            Assert.assertTrue("extraMentions.containsAll(TwidereProject)",
                    it.extraMentions.entitiesContainsAll("TwidereProject"))
            Assert.assertTrue("excludedMentions.containsAll(nixcraft, mariotaku)",
                    it.excludedMentions.mentionsContainsAll("nixcraft", "mariotaku"))
            Assert.assertTrue("replyToOriginalUser", it.replyToOriginalUser)
        }
    }

    @Test
    fun testExtractReplyTextAndMentionsNoAuthor() {
        // This reply removed author @t_deyarmin
        extractor.extractReplyTextAndMentions("@nixcraft lol", inReplyTo).let {
            Assert.assertEquals("@nixcraft lol", it.replyText)
            Assert.assertTrue("extraMentions.isEmpty()", it.extraMentions.isEmpty())
            Assert.assertTrue("excludedMentions.isEmpty()", it.excludedMentions.isEmpty())
            Assert.assertFalse("replyToOriginalUser", it.replyToOriginalUser)
        }
    }

    @Test
    fun testExtractReplyTextAndMentionsNoAuthorExtraMentions() {
        // This reply removed author @t_deyarmin
        extractor.extractReplyTextAndMentions("@nixcraft @TwidereProject lol", inReplyTo).let {
            Assert.assertEquals("@nixcraft @TwidereProject lol", it.replyText)
            Assert.assertTrue("extraMentions.containsAll(TwidereProject)",
                    it.extraMentions.entitiesContainsAll("TwidereProject"))
            Assert.assertTrue("excludedMentions.isEmpty()", it.excludedMentions.isEmpty())
            Assert.assertFalse("replyToOriginalUser", it.replyToOriginalUser)
        }
    }

    @Test
    fun testExtractReplyTextAndMentionsNoAuthorTextOnly() {
        // This reply removed author @t_deyarmin
        extractor.extractReplyTextAndMentions("lol", inReplyTo).let {
            Assert.assertEquals("lol", it.replyText)
            Assert.assertTrue("extraMentions.isEmpty()", it.extraMentions.isEmpty())
            Assert.assertTrue("excludedMentions.isEmpty()", it.excludedMentions.isEmpty())
            Assert.assertFalse("replyToOriginalUser", it.replyToOriginalUser)
        }
    }

    @Test
    fun testExtractReplyTextAndMentionsNoAuthorEmptyText() {
        // This reply removed author @t_deyarmin
        extractor.extractReplyTextAndMentions("", inReplyTo).let {
            Assert.assertEquals("", it.replyText)
            Assert.assertTrue("extraMentions.isEmpty()", it.extraMentions.isEmpty())
            Assert.assertTrue("excludedMentions.isEmpty()", it.excludedMentions.isEmpty())
            Assert.assertFalse("replyToOriginalUser", it.replyToOriginalUser)
        }
    }

    private fun List<Extractor.Entity>.entitiesContainsAll(vararg screenNames: String): Boolean {
        return all { entity ->
            screenNames.any { expectation ->
                expectation.equals(entity.value, ignoreCase = true)
            }
        }
    }

    private fun List<ParcelableUserMention>.mentionsContainsAll(vararg screenNames: String): Boolean {
        return all { mention ->
            screenNames.any { expectation ->
                expectation.equals(mention.screen_name, ignoreCase = true)
            }
        }
    }
}