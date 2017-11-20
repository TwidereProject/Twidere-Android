package org.mariotaku.twidere.model

data class ObjectId<T>(val accountKey: UserKey, val id: T)