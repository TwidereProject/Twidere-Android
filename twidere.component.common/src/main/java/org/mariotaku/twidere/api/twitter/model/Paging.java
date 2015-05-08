package org.mariotaku.twidere.api.twitter.model;

import org.mariotaku.simplerestapi.http.ValueMap;

/**
 * Created by mariotaku on 15/2/6.
 */
public class Paging implements ValueMap {
    private long sinceId;
    private long maxId;
    private long cursor;
    private int count, page;

    public Paging() {
        setSinceId(-1);
        setMaxId(-1);
        setCount(-1);
        setPage(-1);
        setCursor(0);
    }

    public void setSinceId(long sinceId) {
        this.sinceId = sinceId;
    }

    public void setMaxId(long maxId) {
        this.maxId = maxId;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setCursor(long cursor) {
        this.cursor = cursor;
    }

    public long getSinceId() {
        return sinceId;
    }

    public long getMaxId() {
        return maxId;
    }

    public long getCursor() {
        return cursor;
    }

    public int getCount() {
        return count;
    }

    public int getPage() {
        return page;
    }

    public Paging sinceId(long sinceId) {
        this.sinceId = sinceId;
        return this;
    }

    public Paging maxId(long maxId) {
        this.maxId = maxId;
        return this;
    }

    public Paging count(int count) {
        this.count = count;
        return this;
    }

    public Paging page(int page) {
        this.page = page;
        return this;
    }

    public Paging cursor(long cursor) {
        this.cursor = cursor;
        return this;
    }

    @Override
    public boolean has(String key) {
        switch (key) {
            case "since_id": {
                return sinceId != -1;
            }
            case "max_id": {
                return maxId != -1;
            }
            case "count": {
                return count != -1;
            }
            case "page": {
                return page != -1;
            }
            case "cursor": {
                return cursor != 0;
            }
        }
        return false;
    }

    @Override
    public String get(String key) {
        switch (key) {
            case "since_id": {
                if (sinceId != -1) return String.valueOf(sinceId);
            }
            case "max_id": {
                if (maxId != -1) return String.valueOf(maxId);
            }
            case "count": {
                if (count != -1) return String.valueOf(count);
            }
            case "page": {
                if (page != -1) return String.valueOf(page);
            }
            case "cursor": {
                if (cursor != 0) return String.valueOf(cursor);
            }
        }
        return null;
    }

    @Override
    public String[] keys() {
        return new String[]{"since_id", "max_id", "count", "page", "cursor"};
    }
}
