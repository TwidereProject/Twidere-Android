/*
 *
 *  https://github.com/fhucho/simple-disk-cache
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cz.fhucho.android.util;

import android.support.annotation.NonNull;

import com.bluelinelabs.logansquare.LoganSquare;
import com.jakewharton.disklrucache.DiskLruCache;

import org.mariotaku.twidere.util.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapted from https://github.com/fhucho/simple-disk-cache
 * License Apache 2.0
 */
public class SimpleDiskCache {

    private static final int VALUE_IDX = 0;
    private static final int METADATA_IDX = 1;
    private static final List<File> usedDirs = new ArrayList<>();

    private com.jakewharton.disklrucache.DiskLruCache diskLruCache;
    private int mAppVersion;

    private SimpleDiskCache(File dir, int appVersion, long maxSize) throws IOException {
        mAppVersion = appVersion;
        diskLruCache = DiskLruCache.open(dir, appVersion, 2, maxSize);
    }

    public static synchronized SimpleDiskCache open(File dir, int appVersion, long maxSize)
            throws IOException {
        if (usedDirs.contains(dir)) {
            throw new IllegalStateException("Cache dir " + dir.getAbsolutePath() + " was used before.");
        }

        usedDirs.add(dir);

        return new SimpleDiskCache(dir, appVersion, maxSize);
    }

    /**
     * User should be sure there are no outstanding operations.
     *
     * @throws IOException
     */
    public void clear() throws IOException {
        File dir = diskLruCache.getDirectory();
        long maxSize = diskLruCache.getMaxSize();
        diskLruCache.delete();
        diskLruCache = DiskLruCache.open(dir, mAppVersion, 2, maxSize);
    }

    public DiskLruCache getCache() {
        return diskLruCache;
    }

    public InputStreamEntry getInputStream(String key) throws IOException {
        DiskLruCache.Snapshot snapshot = diskLruCache.get(toInternalKey(key));
        if (snapshot == null) return null;
        return new InputStreamEntry(snapshot, readMetadata(snapshot));
    }

    public StringEntry getString(String key) throws IOException {
        DiskLruCache.Snapshot snapshot = diskLruCache.get(toInternalKey(key));
        if (snapshot == null) return null;

        try {
            return new StringEntry(snapshot.getString(VALUE_IDX), readMetadata(snapshot));
        } finally {
            snapshot.close();
        }
    }

    public boolean contains(String key) throws IOException {
        DiskLruCache.Snapshot snapshot = diskLruCache.get(toInternalKey(key));
        if (snapshot == null) return false;

        snapshot.close();
        return true;
    }

    public OutputStream openStream(String key) throws IOException {
        return openStream(key, new HashMap<String, String>());
    }

    public OutputStream openStream(String key, @NonNull Map<String, String> metadata)
            throws IOException {
        DiskLruCache.Editor editor = diskLruCache.edit(toInternalKey(key));
        try {
            writeMetadata(metadata, editor);
            BufferedOutputStream bos = new BufferedOutputStream(editor.newOutputStream(VALUE_IDX));
            return new CacheOutputStream(bos, editor);
        } catch (IOException e) {
            editor.abort();
            throw e;
        }
    }

    public void put(String key, InputStream is) throws IOException {
        put(key, is, new HashMap<String, String>());
    }

    public void put(String key, InputStream is, @NonNull Map<String, String> annotations)
            throws IOException {
        OutputStream os = null;
        try {
            os = openStream(key, annotations);
            Utils.copyStream(is, os);
        } finally {
            if (os != null) os.close();
        }
    }

    public void put(String key, String value) throws IOException {
        put(key, value, new HashMap<String, String>());
    }

    public void put(String key, String value, @NonNull Map<String, String> annotations)
            throws IOException {
        OutputStream cos = null;
        try {
            cos = openStream(key, annotations);
            cos.write(value.getBytes());
        } finally {
            if (cos != null) cos.close();
        }

    }

    public boolean remove(String key) {
        try {
            return diskLruCache.remove(toInternalKey(key));
        } catch (IOException e) {
            return false;
        }
    }

    private void writeMetadata(@NonNull Map<String, String> metadata, DiskLruCache.Editor editor)
            throws IOException {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(editor.newOutputStream(METADATA_IDX));
            LoganSquare.serialize(metadata, os, String.class);
        } finally {
            Utils.closeSilently(os);
        }
    }

    @NonNull
    private Map<String, String> readMetadata(DiskLruCache.Snapshot snapshot)
            throws IOException {
        InputStream is = null;
        try {
            is = new BufferedInputStream(snapshot.getInputStream(METADATA_IDX));
            return LoganSquare.parseMap(is, String.class);
        } finally {
            Utils.closeSilently(is);
        }
    }

    private String toInternalKey(String key) {
        return md5(key);
    }

    private String md5(String s) {
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes("UTF-8"));
            byte[] digest = m.digest();
            BigInteger bigInt = new BigInteger(1, digest);
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError();
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError();
        }
    }

    private class CacheOutputStream extends FilterOutputStream {

        private final DiskLruCache.Editor editor;
        private boolean failed = false;

        private CacheOutputStream(OutputStream os, DiskLruCache.Editor editor) {
            super(os);
            this.editor = editor;
        }

        @Override
        public void close() throws IOException {
            IOException closeException = null;
            try {
                super.close();
            } catch (IOException e) {
                closeException = e;
            }

            if (failed) {
                editor.abort();
            } else {
                editor.commit();
            }

            if (closeException != null) throw closeException;
        }

        @Override
        public void flush() throws IOException {
            try {
                super.flush();
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }

        @Override
        public void write(int oneByte) throws IOException {
            try {
                super.write(oneByte);
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }

        @Override
        public void write(@NonNull byte[] buffer) throws IOException {
            try {
                super.write(buffer);
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }

        @Override
        public void write(@NonNull byte[] buffer, int offset, int length) throws IOException {
            try {
                super.write(buffer, offset, length);
            } catch (IOException e) {
                failed = true;
                throw e;
            }
        }
    }

    public static class InputStreamEntry {
        private final DiskLruCache.Snapshot snapshot;
        @NonNull
        private final Map<String, String> metadata;
        private final long length;

        public InputStreamEntry(DiskLruCache.Snapshot snapshot, @NonNull Map<String, String> metadata) {
            this.metadata = metadata;
            this.snapshot = snapshot;
            this.length = snapshot.getLength(VALUE_IDX);
        }

        public InputStream getInputStream() {
            return snapshot.getInputStream(VALUE_IDX);
        }

        @NonNull
        public Map<String, String> getMetadata() {
            return metadata;
        }

        public long getLength() {
            return length;
        }

        public void close() {
            snapshot.close();
        }

    }

    public static class StringEntry {
        private final String string;
        @NonNull
        private final Map<String, String> metadata;

        public StringEntry(String string, @NonNull Map<String, String> metadata) {
            this.string = string;
            this.metadata = metadata;
        }

        public String getString() {
            return string;
        }

        @NonNull
        public Map<String, String> getMetadata() {
            return metadata;
        }
    }

}