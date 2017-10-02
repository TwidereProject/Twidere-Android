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
package org.apache.commons.text.translate;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * Translate XML numeric entities of the form &amp;#[xX]?\d+;? to
 * the specific codepoint.
 *
 * Note that the semi-colon is optional.
 *
 * @since 1.0
 */
public class NumericEntityUnescaper extends CharSequenceTranslator {

    /** NumericEntityUnescaper option enum. */
    public enum OPTION { semiColonRequired, semiColonOptional, errorIfNoSemiColon }

    /** EnumSet of OPTIONS, given from the constructor. */
    private final EnumSet<OPTION> options;

    /**
     * Create a UnicodeUnescaper.
     *
     * The constructor takes a list of options, only one type of which is currently
     * available (whether to allow, error or ignore the semi-colon on the end of a
     * numeric entity to being missing).
     *
     * For example, to support numeric entities without a ';':
     *    new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.semiColonOptional)
     * and to throw an IllegalArgumentException when they're missing:
     *    new NumericEntityUnescaper(NumericEntityUnescaper.OPTION.errorIfNoSemiColon)
     *
     * Note that the default behaviour is to ignore them.
     *
     * @param options to apply to this unescaper
     */
    public NumericEntityUnescaper(final OPTION... options) {
        if (options.length > 0) {
            this.options = EnumSet.copyOf(Arrays.asList(options));
        } else {
            this.options = EnumSet.of(OPTION.semiColonRequired);
        }
    }

    /**
     * Whether the passed in option is currently set.
     *
     * @param option to check state of
     * @return whether the option is set
     */
    public boolean isSet(final OPTION option) {
        return options != null && options.contains(option);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
        final int seqEnd = input.length();
        // Uses -2 to ensure there is something after the &#
        if (input.charAt(index) == '&' && index < seqEnd - 2 && input.charAt(index + 1) == '#') {
            int start = index + 2;
            boolean isHex = false;

            final char firstChar = input.charAt(start);
            if (firstChar == 'x' || firstChar == 'X') {
                start++;
                isHex = true;

                // Check there's more than just an x after the &#
                if (start == seqEnd) {
                    return 0;
                }
            }

            int end = start;
            // Note that this supports character codes without a ; on the end
            while (end < seqEnd && (input.charAt(end) >= '0' && input.charAt(end) <= '9'
                                    || input.charAt(end) >= 'a' && input.charAt(end) <= 'f'
                                    || input.charAt(end) >= 'A' && input.charAt(end) <= 'F')) {
                end++;
            }

            final boolean semiNext = end != seqEnd && input.charAt(end) == ';';

            if (!semiNext) {
                if (isSet(OPTION.semiColonRequired)) {
                    return 0;
                } else {
                    if (isSet(OPTION.errorIfNoSemiColon)) {
                        throw new IllegalArgumentException("Semi-colon required at end of numeric entity");
                    }
                }
            }

            int entityValue;
            try {
                if (isHex) {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 16);
                } else {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 10);
                }
            } catch (final NumberFormatException nfe) {
                return 0;
            }

            if (entityValue > 0xFFFF) {
                final char[] chrs = Character.toChars(entityValue);
                out.write(chrs[0]);
                out.write(chrs[1]);
            } else {
                out.write(entityValue);
            }

            return 2 + end - start + (isHex ? 1 : 0) + (semiNext ? 1 : 0);
        }
        return 0;
    }
}
