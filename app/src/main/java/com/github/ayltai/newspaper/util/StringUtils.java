package com.github.ayltai.newspaper.util;

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

import java.util.ArrayList;
import java.util.List;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * <p>Operations on {@link java.lang.String} that are {@code null} safe.</p>
 * <ul>
 *     <li>
 *         <b>SubstringBefore/SubstringAfter/SubstringBetween</b>
 *         - substring extraction relative to other strings
 *     </li>
 * </ul>
 * <p>{@code StringUtils} handles {@code null} input Strings quietly. That is to say that a {@code null} input will return {@code null}. Where a {@code boolean} or {@code int} is being returned details vary by method.</p>
 * <p>A side effect of the {@code null} handling is that a {@code NullPointerException} should be considered a bug in {@code StringUtils}.</p>
 * <p>Methods in this class give sample code to explain their operation. The symbol {@code *} is used to indicate any input including {@code null}.</p>
 * <p>#ThreadSafe#</p>
 * @see java.lang.String
 * @since 1.0
 */
//@Immutable
public final class StringUtils {
    /**
     * Represents a failed index search.
     * @since 2.1
     */
    private static final int INDEX_NOT_FOUND = -1;

    private StringUtils() {
    }

    /**
     * <p>Gets the String that is nested in between two Strings. Only the first match is returned.</p>
     * <p>A {@code null} input String returns {@code null}. A {@code null} open/close returns {@code null} (no match). An empty ("") open and close returns an empty string.</p>
     * <pre>
     * StringUtils.substringBetween("wx[b]yz", "[", "]")    = "b"
     * StringUtils.substringBetween(null, *, *)             = null
     * StringUtils.substringBetween(*, null, *)             = null
     * StringUtils.substringBetween(*, *, null)             = null
     * StringUtils.substringBetween("", "", "")             = ""
     * StringUtils.substringBetween("", "", "]")            = null
     * StringUtils.substringBetween("", "[", "]")           = null
     * StringUtils.substringBetween("yabcz", "", "")        = ""
     * StringUtils.substringBetween("yabcz", "y", "z")      = "abc"
     * StringUtils.substringBetween("yabczyabcz", "y", "z") = "abc"
     * </pre>
     * @param str The String containing the substring, may be null
     * @param open The String before the substring, may be null
     * @param close The String after the substring, may be null
     * @return The substring, {@code null} if no match
     * @since 2.0
     */
    @Nullable
    public static String substringBetween(final String str, final String open, final String close) {
        if (str == null || open == null || close == null) return null;

        final int start = str.indexOf(open);

        if (start != INDEX_NOT_FOUND) {
            final int end = str.indexOf(close, start + open.length());

            if (end != INDEX_NOT_FOUND) return str.substring(start + open.length(), end);
        }

        return null;
    }

    /**
     * <p>Searches a String for substrings delimited by a start and end tag, returning all matching substrings in an array.</p>
     * <p>A {@code null} input String returns {@code null}. A {@code null} open/close returns {@code null} (no match). An empty ("") open/close returns {@code null} (no match).</p>
     * <pre>
     * StringUtils.substringsBetween("[a][b][c]", "[", "]") = ["a","b","c"]
     * StringUtils.substringsBetween(null, *, *)            = null
     * StringUtils.substringsBetween(*, null, *)            = null
     * StringUtils.substringsBetween(*, *, null)            = null
     * StringUtils.substringsBetween("", "[", "]")          = []
     * </pre>
     * @param str The String containing the substrings, null returns null, empty returns empty
     * @param open The String identifying the start of the substring, empty returns null
     * @param close The String identifying the end of the substring, empty returns null
     * @return A String Array of substrings, or {@code null} if no match
     * @since 2.3
     */
    @NonNull
    public static String[] substringsBetween(final String str, final String open, final String close) {
        if (str == null || TextUtils.isEmpty(open) || TextUtils.isEmpty(close)) return null;

        final int strLen = str.length();

        if (strLen == 0) return new String[0];

        final int          closeLen = close.length();
        final int          openLen  = open.length();
        final List<String> list     = new ArrayList<>();

        int pos = 0;

        while (pos < strLen - closeLen) {
            int start = str.indexOf(open, pos);
            if (start < 0) break;

            start += openLen;

            final int end = str.indexOf(close, start);
            if (end < 0) break;

            list.add(str.substring(start, end));

            pos = end + closeLen;
        }

        if (list.isEmpty()) return null;

        return list.toArray(new String [list.size()]);
    }
}
