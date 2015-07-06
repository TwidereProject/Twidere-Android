/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util;

import org.mariotaku.sqliteqb.library.Columns;
import org.mariotaku.sqliteqb.library.Columns.Column;
import org.mariotaku.sqliteqb.library.Expression;
import org.mariotaku.sqliteqb.library.Join;
import org.mariotaku.sqliteqb.library.Join.Operation;
import org.mariotaku.sqliteqb.library.OrderBy;
import org.mariotaku.sqliteqb.library.SQLQueryBuilder;
import org.mariotaku.sqliteqb.library.Selectable;
import org.mariotaku.sqliteqb.library.Table;
import org.mariotaku.sqliteqb.library.Tables;
import org.mariotaku.sqliteqb.library.query.SQLSelectQuery;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedRelationships;
import org.mariotaku.twidere.provider.TwidereDataStore.CachedUsers;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Conversation;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.ConversationEntries;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Inbox;
import org.mariotaku.twidere.provider.TwidereDataStore.DirectMessages.Outbox;

import java.util.Locale;

public class TwidereQueryBuilder {

    public static final class CachedUsersQueryBuilder {

        public static SQLSelectQuery withRelationship(final String[] projection,
                                                      final String selection,
                                                      final String sortOrder,
                                                      final long accountId) {
            return withRelationship(Utils.getColumnsFromProjection(projection), selection,
                    sortOrder, accountId);
        }

        public static SQLSelectQuery withRelationship(final Selectable select,
                                                      final String selection,
                                                      final String sortOrder,
                                                      final long accountId) {
            final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
            qb.select(select).from(new Tables(CachedUsers.TABLE_NAME));
            final Column relationshipsUserId = new Column(new Table(CachedRelationships.TABLE_NAME),
                    CachedRelationships.USER_ID);
            final Column usersUserId = new Column(new Table(CachedUsers.TABLE_NAME),
                    CachedRelationships.USER_ID);
            final Column relationshipsAccountId = new Column(new Table(CachedRelationships.TABLE_NAME),
                    CachedRelationships.ACCOUNT_ID);
            final Expression on = Expression.and(
                    Expression.equals(relationshipsUserId, usersUserId),
                    Expression.equals(relationshipsAccountId, accountId)
            );
            qb.join(new Join(false, Operation.LEFT, new Table(CachedRelationships.TABLE_NAME), on));
            if (selection != null) {
                qb.where(new Expression(selection));
            }
            if (sortOrder != null) {
                qb.orderBy(new OrderBy(sortOrder));
            }
            return qb.build();
        }

        public static SQLSelectQuery withScore(final String[] projection,
                                               final String selection,
                                               final String sortOrder,
                                               final long accountId) {
            final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
            final Selectable select = Utils.getColumnsFromProjection(projection);
            final Column[] columns = new Column[CachedUsers.COLUMNS.length + 1];
            for (int i = 0, j = columns.length - 1; i < j; i++) {
                final String column = CachedUsers.COLUMNS[i];
                if (CachedUsers._ID.equals(column) || CachedUsers.USER_ID.equals(column)) {
                    columns[i] = new Column(new Table(CachedUsers.TABLE_NAME), column, column);
                } else {
                    columns[i] = new Column(column);
                }
            }
            final String expr = String.format(Locale.ROOT, "%s * 100 + %s * 50 - %s * 100 - %s * 100 - %s * 100",
                    valueOrZero(CachedRelationships.FOLLOWING, CachedRelationships.FOLLOWED_BY,
                            CachedRelationships.BLOCKING, CachedRelationships.BLOCKED_BY,
                            CachedRelationships.MUTING));
            columns[columns.length - 1] = new Column(expr, "score");
            qb.select(select);
            qb.from(withRelationship(new Columns(columns), null, null, accountId));
            if (selection != null) {
                qb.where(new Expression(selection));
            }
            if (sortOrder != null) {
                qb.orderBy(new OrderBy(sortOrder));
            }
            return qb.build();
        }

        private static Object[] valueOrZero(String... columns) {
            final String[] result = new String[columns.length];
            for (int i = 0, j = columns.length; i < j; i++) {
                result[i] = String.format(Locale.ROOT, "CASE WHEN %s IS NULL THEN 0 ELSE %s END",
                        columns[i], columns[i]);
            }
            return result;
        }

    }

    public static final class ConversationQueryBuilder {

        public static SQLSelectQuery buildByConversationId(final String[] projection, final long account_id,
                                                           final long conversationId, final String selection, final String sortOrder) {
            final Selectable select = Utils.getColumnsFromProjection(projection);
            final SQLSelectQuery.Builder qb = SQLQueryBuilder.select(select);
            qb.from(new Tables(DirectMessages.TABLE_NAME));
            final Expression accountIdWhere = Expression.equals(DirectMessages.ACCOUNT_ID, account_id);
            final Expression incomingWhere = Expression.and(Expression.notEquals(DirectMessages.IS_OUTGOING, 1),
                    Expression.equals(DirectMessages.SENDER_ID, conversationId));
            final Expression outgoingWhere = Expression.and(Expression.equals(DirectMessages.IS_OUTGOING, 1),
                    Expression.equals(DirectMessages.RECIPIENT_ID, conversationId));
            final Expression conversationWhere = Expression.or(incomingWhere, outgoingWhere);
            if (selection != null) {
                qb.where(Expression.and(accountIdWhere, conversationWhere, new Expression(selection)));
            } else {
                qb.where(Expression.and(accountIdWhere, conversationWhere));
            }
            qb.orderBy(new OrderBy(sortOrder != null ? sortOrder : Conversation.DEFAULT_SORT_ORDER));
            return qb.build();
        }

        public static SQLSelectQuery buildByScreenName(final String[] projection, final long account_id,
                                                       final String screen_name, final String selection, final String sortOrder) {
            final Selectable select = Utils.getColumnsFromProjection(projection);
            final SQLSelectQuery.Builder qb = SQLQueryBuilder.select(select);
            qb.select(select);
            qb.from(new Tables(DirectMessages.TABLE_NAME));
            final Expression accountIdWhere = Expression.equals(DirectMessages.ACCOUNT_ID, account_id);
            final Expression incomingWhere = Expression.and(Expression.notEquals(DirectMessages.IS_OUTGOING, 1),
                    Expression.equals(new Column(DirectMessages.SENDER_SCREEN_NAME), screen_name));
            final Expression outgoingWhere = Expression.and(Expression.equals(DirectMessages.IS_OUTGOING, 1),
                    Expression.equals(new Column(DirectMessages.RECIPIENT_SCREEN_NAME), screen_name));
            if (selection != null) {
                qb.where(Expression.and(accountIdWhere, incomingWhere, outgoingWhere, new Expression(selection)));
            } else {
                qb.where(Expression.and(accountIdWhere, incomingWhere, outgoingWhere));
            }
            qb.orderBy(new OrderBy(sortOrder != null ? sortOrder : Conversation.DEFAULT_SORT_ORDER));
            return qb.build();
        }

    }

    public static class ConversationsEntryQueryBuilder {

        public static SQLSelectQuery build() {
            return build(null);
        }

        public static SQLSelectQuery build(final String selection) {
            final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
            qb.select(new Columns(new Column(ConversationEntries._ID), new Column(ConversationEntries.MESSAGE_TIMESTAMP),
                    new Column(ConversationEntries.MESSAGE_ID), new Column(ConversationEntries.ACCOUNT_ID), new Column(
                    ConversationEntries.IS_OUTGOING), new Column(ConversationEntries.NAME), new Column(
                    ConversationEntries.SCREEN_NAME), new Column(ConversationEntries.PROFILE_IMAGE_URL),
                    new Column(ConversationEntries.TEXT_HTML), new Column(ConversationEntries.CONVERSATION_ID)));
            final SQLSelectQuery.Builder entryIds = new SQLSelectQuery.Builder();
            entryIds.select(new Columns(new Column(DirectMessages._ID),
                    new Column(DirectMessages.MESSAGE_TIMESTAMP),
                    new Column(DirectMessages.MESSAGE_ID),
                    new Column(DirectMessages.ACCOUNT_ID),
                    new Column("0", DirectMessages.IS_OUTGOING),
                    new Column(DirectMessages.SENDER_NAME, ConversationEntries.NAME),
                    new Column(DirectMessages.SENDER_SCREEN_NAME, ConversationEntries.SCREEN_NAME),
                    new Column(DirectMessages.SENDER_PROFILE_IMAGE_URL, ConversationEntries.PROFILE_IMAGE_URL),
                    new Column(DirectMessages.TEXT_HTML),
                    new Column(DirectMessages.SENDER_ID, ConversationEntries.CONVERSATION_ID)));
            entryIds.from(new Tables(Inbox.TABLE_NAME));
            entryIds.union();
            entryIds.select(new Columns(new Column(DirectMessages._ID),
                    new Column(DirectMessages.MESSAGE_TIMESTAMP),
                    new Column(DirectMessages.MESSAGE_ID),
                    new Column(DirectMessages.ACCOUNT_ID),
                    new Column("1", DirectMessages.IS_OUTGOING),
                    new Column(DirectMessages.RECIPIENT_NAME, ConversationEntries.NAME),
                    new Column(DirectMessages.RECIPIENT_SCREEN_NAME, ConversationEntries.SCREEN_NAME),
                    new Column(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL, ConversationEntries.PROFILE_IMAGE_URL),
                    new Column(DirectMessages.TEXT_HTML),
                    new Column(DirectMessages.RECIPIENT_ID, ConversationEntries.CONVERSATION_ID)));
            entryIds.from(new Tables(Outbox.TABLE_NAME));
            qb.from(entryIds.build());
            final SQLSelectQuery.Builder recent_inbox_msg_ids = SQLQueryBuilder
                    .select(new Column("MAX(" + DirectMessages.MESSAGE_ID + ")")).from(new Tables(Inbox.TABLE_NAME))
                    .groupBy(new Column(DirectMessages.SENDER_ID));
            final SQLSelectQuery.Builder recent_outbox_msg_ids = SQLQueryBuilder
                    .select(new Column("MAX(" + DirectMessages.MESSAGE_ID + ")")).from(new Tables(Outbox.TABLE_NAME))
                    .groupBy(new Column(DirectMessages.RECIPIENT_ID));
            final SQLSelectQuery.Builder conversationIds = new SQLSelectQuery.Builder();
            conversationIds.select(new Columns(new Column(DirectMessages.MESSAGE_ID), new Column(
                    DirectMessages.SENDER_ID, ConversationEntries.CONVERSATION_ID)));
            conversationIds.from(new Tables(Inbox.TABLE_NAME));
            conversationIds.where(Expression.in(new Column(DirectMessages.MESSAGE_ID), recent_inbox_msg_ids.build()));
            conversationIds.union();
            conversationIds.select(new Columns(new Column(DirectMessages.MESSAGE_ID), new Column(
                    DirectMessages.RECIPIENT_ID, ConversationEntries.CONVERSATION_ID)));
            conversationIds.from(new Tables(Outbox.TABLE_NAME));
            conversationIds.where(Expression.in(new Column(DirectMessages.MESSAGE_ID), recent_outbox_msg_ids.build()));
            final SQLSelectQuery.Builder groupedConversationIds = new SQLSelectQuery.Builder();
            groupedConversationIds.select(new Column(DirectMessages.MESSAGE_ID));
            groupedConversationIds.from(conversationIds.build());
            groupedConversationIds.groupBy(new Column(ConversationEntries.CONVERSATION_ID));
            final Expression groupedWhere = Expression.in(new Column(DirectMessages.MESSAGE_ID), groupedConversationIds.build());
            final Expression where;
            if (selection != null) {
                where = Expression.and(groupedWhere, new Expression(selection));
            } else {
                where = groupedWhere;
            }
            qb.where(where);
            qb.groupBy(Utils.getColumnsFromProjection(ConversationEntries.CONVERSATION_ID, DirectMessages.ACCOUNT_ID));
            qb.orderBy(new OrderBy(ConversationEntries.MESSAGE_TIMESTAMP ,false));
            return qb.build();
        }

    }

    public static final class DirectMessagesQueryBuilder {

        public static SQLSelectQuery build() {
            return build(null, null, null);
        }

        public static SQLSelectQuery build(final String[] projection, final String selection,
                                           final String sortOrder) {
            final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
            final Selectable select = Utils.getColumnsFromProjection(projection);
            qb.select(select).from(new Tables(DirectMessages.Inbox.TABLE_NAME));
            if (selection != null) {
                qb.where(new Expression(selection));
            }
            qb.union();
            qb.select(select).from(new Tables(DirectMessages.Outbox.TABLE_NAME));
            if (selection != null) {
                qb.where(new Expression(selection));
            }
            qb.orderBy(new OrderBy(sortOrder != null ? sortOrder : DirectMessages.DEFAULT_SORT_ORDER));
            return qb.build();
        }

    }

}
