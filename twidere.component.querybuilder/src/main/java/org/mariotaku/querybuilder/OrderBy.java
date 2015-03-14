/**
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */

package org.mariotaku.querybuilder;


public class OrderBy implements SQLLang {

    private final String[] orderBy;
    private final boolean[] ascending;

    public OrderBy(final String[] orderBy, final boolean[] ascending) {
        this.orderBy = orderBy;
        this.ascending = ascending;
    }

    public OrderBy(final String... orderBy) {
        this(orderBy, null);
    }

    public OrderBy(final String orderBy, final boolean ascending) {
        this.orderBy = new String[]{orderBy};
        this.ascending = new boolean[]{ascending};
    }

    @Override
    public String getSQL() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0, j = orderBy.length; i < j; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(orderBy[i]);
            if (ascending != null) {
                sb.append(ascending[i] ? " ASC" : " DESC");
            }
        }
        return sb.toString();
    }

}
