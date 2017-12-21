/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mariotaku.twidere.util.emoji

import org.apache.commons.text.translate.CharSequenceTranslator
import org.mariotaku.microblog.library.model.mastodon.Emoji
import java.io.IOException
import java.io.Writer
import java.util.*

class CustomEmojiTranslator(
        emojis: Array<Emoji>,
        val handler: (index: Int, emoji: Emoji) -> Unit
) : CharSequenceTranslator() {

    /** The mapping to be used in translation.  */
    private val lookupMap: MutableMap<String, Emoji>
    /** The first character of each key in the lookupMap.  */
    private val prefixSet: HashSet<Char>
    /** The length of the shortest key in the lookupMap.  */
    private val shortest: Int
    /** The length of the longest key in the lookupMap.  */
    private val longest: Int

    init {
        this.lookupMap = HashMap()
        this.prefixSet = HashSet()
        var currentShortest = Integer.MAX_VALUE
        var currentLongest = 0

        emojis.forEach { emoji ->
            val key = ":${emoji.shortCode}:"
            this.lookupMap.put(key, emoji)
            this.prefixSet.add(key[1])
            val sz = key.length
            if (sz < currentShortest) {
                currentShortest = sz
            }
            if (sz > currentLongest) {
                currentLongest = sz
            }
        }
        this.shortest = currentShortest
        this.longest = currentLongest
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun translate(input: CharSequence, index: Int, out: Writer): Int {
        val inputLength = input.length
        if (index + 1 >= inputLength) return 0
        // check if translation exists for the input at position index
        if (':' == input[index] && prefixSet.contains(input[index + 1])) {
            var max = longest
            if (index + longest > inputLength) {
                max = inputLength - index
            }
            // implement greedy algorithm by trying maximum match first
            for (i in max downTo shortest) {
                val subSeq = input.subSequence(index, index + i).toString()
                val result = lookupMap[subSeq]

                if (result != null) {
                    out.write(subSeq)
                    handler(index, result)
                    return i
                }
            }
        }
        return 0
    }
}
