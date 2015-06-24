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
 * Created by mariotaku on 15/3/30.
 */
public class Constraint implements SQLLang {
    private final String name;
    private final String type;
    private final SQLQuery constraint;

    public Constraint(String name, String type, SQLQuery constraint) {
        this.name = name;
        this.type = type;
        this.constraint = constraint;
    }

    @Override
    public String getSQL() {
        final StringBuilder sb = new StringBuilder();
        if (name != null) {
            sb.append("CONSTRAINT ");
            sb.append(name);
            sb.append(" ");
        }
        sb.append(type);
        sb.append(" ");
        sb.append(constraint.getSQL());
        return sb.toString();
    }

    public static Constraint unique(String name, Columns columns, OnConflict onConflict) {
        return new Constraint(name, "UNIQUE", new ColumnConflictConstaint(columns, onConflict));
    }

    public static Constraint unique(Columns columns, OnConflict onConflict) {
        return unique(null, columns, onConflict);
    }

    private static final class ColumnConflictConstaint implements SQLQuery {

        private final Columns columns;
        private final OnConflict onConflict;

        public ColumnConflictConstaint(Columns columns, OnConflict onConflict) {
            this.columns = columns;
            this.onConflict = onConflict;
        }

        @Override
        public String getSQL() {
            final StringBuilder sb = new StringBuilder();
            sb.append("(");
            sb.append(columns.getSQL());
            sb.append(") ");
            sb.append("ON CONFLICT ");
            sb.append(onConflict.getAction());
            return sb.toString();
        }
    }

}
