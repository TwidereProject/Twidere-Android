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

package org.mariotaku.microblog.library.twitter.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * CRLFLineReader implements a readLine() method that requires
 * exactly CRLF to terminate an input line.
 * This is required for IMAP, which allows bare CR and LF.
 *
 * @since 3.0
 */
public final class CRLFLineReader extends BufferedReader
{
    private static final char LF = '\n';
    private static final char CR = '\r';

    /**
     * Creates a CRLFLineReader that wraps an existing Reader
     * input source.
     * @param reader  The Reader input source.
     */
    public CRLFLineReader(Reader reader)
    {
        super(reader);
    }

    /**
     * Read a line of text.
     * A line is considered to be terminated by carriage return followed immediately by a linefeed.
     * This contrasts with BufferedReader which also allows other combinations.
     * @since 3.0
     */
    @Override
    public String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int intch;
        boolean prevWasCR = false;
        synchronized(lock) { // make thread-safe (hopefully!)
            while((intch = read()) != -1)
            {
                if (prevWasCR && intch == LF) {
                    return sb.substring(0, sb.length()-1);
                }
                prevWasCR = intch == CR;
                sb.append((char) intch);
            }
        }
        String string = sb.toString();
        if (string.length() == 0) { // immediate EOF
            return null;
        }
        return string;
    }
}