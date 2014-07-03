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

package org.apache.commons.lang3.text.translate;

import java.io.IOException;
import java.io.Writer;

/**
 * Executes a sequence of translators one after the other. Execution ends
 * whenever the first translator consumes codepoints from the input.
 * 
 * @since 3.0
 * @version $Id: AggregateTranslator.java 1088899 2011-04-05 05:31:27Z bayard $
 */
public class AggregateTranslator extends CharSequenceTranslator {

	private final CharSequenceTranslator[] translators;

	/**
	 * Specify the translators to be used at creation time.
	 * 
	 * @param translators CharSequenceTranslator array to aggregate
	 */
	public AggregateTranslator(final CharSequenceTranslator... translators) {
		this.translators = clone(translators);
	}

	/**
	 * The first translator to consume codepoints from the input is the
	 * 'winner'. Execution stops with the number of consumed codepoints being
	 * returned. {@inheritDoc}
	 */
	@Override
	public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
		for (final CharSequenceTranslator translator : translators) {
			final int consumed = translator.translate(input, index, out);
			if (consumed != 0) return consumed;
		}
		return 0;
	}

	/**
	 * <p>
	 * Shallow clones an array returning a typecast result and handling
	 * {@code null}.
	 * </p>
	 * <p>
	 * The objects in the array are not cloned, thus there is no special
	 * handling for multi-dimensional arrays.
	 * </p>
	 * <p>
	 * This method returns {@code null} for a {@code null} input array.
	 * </p>
	 * 
	 * @param <T> the component type of the array
	 * @param array the array to shallow clone, may be {@code null}
	 * @return the cloned array, {@code null} if {@code null} input
	 */
	private static <T> T[] clone(final T[] array) {
		if (array == null) return null;
		return array.clone();
	}

}
