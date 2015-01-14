/*
 * Twidere - Twitter client for Android
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

package org.mariotaku.querybuilder;

/**
 * Created by mariotaku on 15/1/12.
 */
public class Join implements SQLLang {

    private final boolean natural;
    private final Operation operation;
    private final Selectable source;
    private final Expression on;

    public Join(boolean natural, Operation operation, Selectable source, Expression on) {
        this.natural = natural;
        this.operation = operation;
        this.source = source;
        this.on = on;
    }

    @Override
    public String getSQL() {
        if (operation == null) throw new IllegalArgumentException("operation can't be null!");
        if (source == null) throw new IllegalArgumentException("source can't be null!");
        final StringBuilder builder = new StringBuilder();
        if (natural) {
            builder.append("NATURAL ");
        }
        builder.append(operation.getSQL());
        builder.append(" JOIN ");
        builder.append(source.getSQL());
        if (on != null) {
            builder.append(" ON ");
            builder.append(on.getSQL());
        }
        return builder.toString();
    }

    public enum Operation implements SQLLang {
        LEFT("LEFT"), LEFT_OUTER("LEFT OUTER"), INNER("INNER"), CROSS("CROSS");
        private final String op;

        Operation(String op) {
            this.op = op;
        }

        @Override
        public String getSQL() {
            return op;
        }

    }
}
