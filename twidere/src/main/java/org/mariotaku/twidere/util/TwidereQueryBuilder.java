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

import org.mariotaku.querybuilder.Columns;
import org.mariotaku.querybuilder.Columns.Column;
import org.mariotaku.querybuilder.OrderBy;
import org.mariotaku.querybuilder.SQLQueryBuilder;
import org.mariotaku.querybuilder.Selectable;
import org.mariotaku.querybuilder.Tables;
import org.mariotaku.querybuilder.Where;
import org.mariotaku.querybuilder.query.SQLSelectQuery;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Conversation;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationEntries;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Inbox;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Outbox;

public class TwidereQueryBuilder {

	public static final class ConversationQueryBuilder {

		public static final String buildByConversationId(final String[] projection, final long account_id,
				final long conversationId, final String selection, final String sortOrder) {
			final Selectable select = Utils.getColumnsFromProjection(projection);
			final SQLSelectQuery.Builder qb = SQLQueryBuilder.select(select);
			qb.from(new Tables(DirectMessages.TABLE_NAME));
			final Where accountIdWhere = Where.equals(DirectMessages.ACCOUNT_ID, account_id);
			final Where incomingWhere = Where.and(Where.notEquals(DirectMessages.IS_OUTGOING, 1),
					Where.equals(DirectMessages.SENDER_ID, conversationId));
			final Where outgoingWhere = Where.and(Where.equals(DirectMessages.IS_OUTGOING, 1),
					Where.equals(DirectMessages.RECIPIENT_ID, conversationId));
			final Where conversationWhere = Where.or(incomingWhere, outgoingWhere);
			if (selection != null) {
				qb.where(Where.and(accountIdWhere, conversationWhere, new Where(selection)));
			} else {
				qb.where(Where.and(accountIdWhere, conversationWhere));
			}
			qb.orderBy(new OrderBy(sortOrder != null ? sortOrder : Conversation.DEFAULT_SORT_ORDER));
			return qb.build().getSQL();
		}

		public static final String buildByScreenName(final String[] projection, final long account_id,
				final String screen_name, final String selection, final String sortOrder) {
			final Selectable select = Utils.getColumnsFromProjection(projection);
			final SQLSelectQuery.Builder qb = SQLQueryBuilder.select(select);
			qb.select(select);
			qb.from(new Tables(DirectMessages.TABLE_NAME));
			final Where accountIdWhere = Where.equals(DirectMessages.ACCOUNT_ID, account_id);
			final Where incomingWhere = Where.and(Where.notEquals(DirectMessages.IS_OUTGOING, 1),
					Where.equals(new Column(DirectMessages.SENDER_SCREEN_NAME), screen_name));
			final Where outgoingWhere = Where.and(Where.equals(DirectMessages.IS_OUTGOING, 1),
					Where.equals(new Column(DirectMessages.RECIPIENT_SCREEN_NAME), screen_name));
			if (selection != null) {
				qb.where(Where.and(accountIdWhere, incomingWhere, outgoingWhere, new Where(selection)));
			} else {
				qb.where(Where.and(accountIdWhere, incomingWhere, outgoingWhere));
			}
			qb.orderBy(new OrderBy(sortOrder != null ? sortOrder : Conversation.DEFAULT_SORT_ORDER));
			return qb.build().getSQL();
		}

	}

	public static class ConversationsEntryQueryBuilder {

		public static SQLSelectQuery build() {
			return build(null);
		}

		public static SQLSelectQuery build(final String selection) {
			final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
			qb.select(new Columns(new Column(DirectMessages._ID), new Column(ConversationEntries.MESSAGE_TIMESTAMP),
					new Column(DirectMessages.MESSAGE_ID), new Column(DirectMessages.ACCOUNT_ID), new Column(
							DirectMessages.IS_OUTGOING), new Column(ConversationEntries.NAME), new Column(
							ConversationEntries.SCREEN_NAME), new Column(ConversationEntries.PROFILE_IMAGE_URL),
					new Column(ConversationEntries.TEXT_HTML), new Column(ConversationEntries.CONVERSATION_ID)));
			final SQLSelectQuery.Builder entryIds = new SQLSelectQuery.Builder();
			entryIds.select(new Columns(new Column(DirectMessages._ID), new Column(
					ConversationEntries.MESSAGE_TIMESTAMP), new Column(DirectMessages.MESSAGE_ID), new Column(
					DirectMessages.ACCOUNT_ID), new Column("0", DirectMessages.IS_OUTGOING), new Column(
					DirectMessages.SENDER_NAME, ConversationEntries.NAME), new Column(
					DirectMessages.SENDER_SCREEN_NAME, ConversationEntries.SCREEN_NAME), new Column(
					DirectMessages.SENDER_PROFILE_IMAGE_URL, ConversationEntries.PROFILE_IMAGE_URL), new Column(
					ConversationEntries.TEXT_HTML), new Column(DirectMessages.SENDER_ID,
					ConversationEntries.CONVERSATION_ID)));
			entryIds.from(new Tables(Inbox.TABLE_NAME));
			entryIds.union();
			entryIds.select(new Columns(new Column(DirectMessages._ID), new Column(
					ConversationEntries.MESSAGE_TIMESTAMP), new Column(DirectMessages.MESSAGE_ID), new Column(
					DirectMessages.ACCOUNT_ID), new Column("1", DirectMessages.IS_OUTGOING), new Column(
					DirectMessages.RECIPIENT_NAME, ConversationEntries.NAME), new Column(
					DirectMessages.RECIPIENT_SCREEN_NAME, ConversationEntries.SCREEN_NAME), new Column(
					DirectMessages.RECIPIENT_PROFILE_IMAGE_URL, ConversationEntries.PROFILE_IMAGE_URL), new Column(
					ConversationEntries.TEXT_HTML), new Column(DirectMessages.RECIPIENT_ID,
					ConversationEntries.CONVERSATION_ID)));
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
			conversationIds.where(Where.in(new Column(DirectMessages.MESSAGE_ID), recent_inbox_msg_ids.build()));
			conversationIds.union();
			conversationIds.select(new Columns(new Column(DirectMessages.MESSAGE_ID), new Column(
					DirectMessages.RECIPIENT_ID, ConversationEntries.CONVERSATION_ID)));
			conversationIds.from(new Tables(Outbox.TABLE_NAME));
			conversationIds.where(Where.in(new Column(DirectMessages.MESSAGE_ID), recent_outbox_msg_ids.build()));
			final SQLSelectQuery.Builder groupedConversationIds = new SQLSelectQuery.Builder();
			groupedConversationIds.select(new Column(DirectMessages.MESSAGE_ID));
			groupedConversationIds.from(conversationIds.build());
			groupedConversationIds.groupBy(new Column(ConversationEntries.CONVERSATION_ID));
			final Where groupedWhere = Where.in(new Column(DirectMessages.MESSAGE_ID), groupedConversationIds.build());
			final Where where;
			if (selection != null) {
				where = Where.and(groupedWhere, new Where(selection));
			} else {
				where = groupedWhere;
			}
			qb.where(where);
			qb.groupBy(Utils.getColumnsFromProjection(ConversationEntries.CONVERSATION_ID, DirectMessages.ACCOUNT_ID));
			qb.orderBy(new OrderBy(ConversationEntries.MESSAGE_TIMESTAMP + " DESC"));
			return qb.build();
		}

	}

	public static final class DirectMessagesQueryBuilder {

		public static final SQLSelectQuery build() {
			return build(null, null, null);
		}

		public static final SQLSelectQuery build(final String[] projection, final String selection,
				final String sortOrder) {
			final SQLSelectQuery.Builder qb = new SQLSelectQuery.Builder();
			final Selectable select = Utils.getColumnsFromProjection(projection);
			qb.select(select).from(new Tables(DirectMessages.Inbox.TABLE_NAME));
			if (selection != null) {
				qb.where(new Where(selection));
			}
			qb.union();
			qb.select(select).from(new Tables(DirectMessages.Outbox.TABLE_NAME));
			if (selection != null) {
				qb.where(new Where(selection));
			}
			qb.orderBy(new OrderBy(sortOrder != null ? sortOrder : DirectMessages.DEFAULT_SORT_ORDER));
			return qb.build();
		}

	}

}
